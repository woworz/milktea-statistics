# 业务服务层原理

## 为什么拆服务层

如果所有逻辑都放在 `MilkTeaViewModel`，ViewModel 会同时负责记录、品牌、统计、日期计算和 UI 状态，后续扩展很容易变成一个难维护的大类。

当前拆分遵循单一职责：

| 逻辑 | 位置 |
|---|---|
| 记录增删改查 | `RecordService` |
| 品牌查询和常用品牌管理 | `BrandService` |
| 日期范围、筛选、统计、趋势和洞察计算 | `AnalyticsService` |
| UI 筛选状态、编辑状态、协程作用域、复购模板派生 | `MilkTeaViewModel` |

## 服务保持无状态

Service 不持有 `StateFlow`、`LiveData` 或 Compose 状态，只暴露普通函数、`Flow` 查询和 `suspend` 写入：

```kotlin
class RecordService(private val repository: MilkTeaRepository) {
    fun getTodayRecords(start: Long, end: Long): Flow<List<MilkTeaRecord>>
    suspend fun addRecord(record: MilkTeaRecord)
}
```

好处是服务可以在 ViewModel、测试或未来后台任务中复用。

## ViewModel 负责组合

`MilkTeaViewModel` 把筛选状态组合成查询条件：

```text
selectedDateRange ─┐
                   ├─ combine ─ flatMapLatest ─ AnalyticsService ─ Room Flow
selectedBrand ─────┘
```

`flatMapLatest` 能在筛选条件快速变化时取消旧查询，避免旧结果覆盖新条件。

今日数据单独由 `todayRange` 驱动。它会计算今天的 `[start, end)`，并在接近明天 00:00 时重新发射新范围；如果等待时间异常小，则至少 60 秒后再次检查。

`insights` 由 `filteredRecords`、`dailyAggregates`、`stats` 和 `selectedDateRange` 组合而来。它不增加新的数据库查询，而是复用统计页已经需要的数据。

`purchaseTemplates` 由全部历史记录派生：按品牌、饮品名和价格分组，优先选择复购次数多、最近购买时间新的组合，最多展示 3 个。

## 扩展规则

- 新增记录相关能力，优先放 `RecordService`。
- 新增品牌图标或常用品牌管理能力，优先放 `BrandService`。
- 新增自定义时间范围、周/月聚合、同比环比或洞察指标，优先放 `AnalyticsService`。
- 新增复购模板排序/过滤规则，优先放 ViewModel 当前的模板派生逻辑；需要持久化时再下沉到数据层。
- 只有 UI 交互状态才放 ViewModel，例如弹窗显隐、正在编辑的记录、当前筛选项。

## 测试思路

Service 层可以用假的 Repository 做 JVM 单元测试，重点验证“调用了正确的数据接口”和“日期范围计算正确”。ViewModel 测试则关注 `StateFlow` 组合和 UI 操作是否触发对应服务。
