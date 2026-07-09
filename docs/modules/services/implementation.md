# 业务服务层实现

服务层位于 ViewModel 和 Repository 之间，用来按领域拆分业务能力。代码位于 `app/src/main/java/com/mason/milkteastatistics/service/`。

## 文件

| 文件 | 职责 |
|---|---|
| `RecordService.kt` | 记录增删改查、全部记录流、今日记录/杯数查询 |
| `BrandService.kt` | 历史品牌列表和常用品牌管理 |
| `AnalyticsService.kt` | 日期范围计算、筛选查询、统计和趋势聚合 |
| `model/DateRange.kt` | 本周、本月、上月三个日期范围 |

## RecordService

`RecordService` 只处理 `MilkTeaRecord`：

- `getTodayRecords(start, end)`：查询今日记录。
- `getTodayCount(start, end)`：查询今日杯数。
- `getAllRecords()`：返回全部历史记录，供 ViewModel 派生复购模板。
- `addRecord(record)`：新增记录。
- `updateRecord(record)`：更新记录。
- `deleteRecord(record)`：删除记录。

它不计算“今天”的起止时间，时间范围由 ViewModel 提供，这样服务本身保持简单。

## BrandService

`BrandService` 管理两个品牌来源：

- `getAllBrands()`：从历史记录里取所有出现过的品牌，用于筛选。
- `getCommonBrands()`：读取用户维护的常用品牌，用于添加记录时快捷选择。
- `addCommonBrand(name)`：新增常用品牌，重复名称由 DAO 忽略。
- `removeCommonBrand(id)`：删除常用品牌。

## AnalyticsService

`AnalyticsService` 集中处理记录页和统计页共享的分析逻辑：

- `getFilteredRecords(start, end, brand)`：按日期范围查询，可选品牌筛选。
- `getStats(start, end, brand)`：返回总杯数、总花费、均价。
- `getDailyAggregates(start, end, brand)`：返回每日趋势数据。
- `buildConsumptionInsights(records, dailyAggregates, stats, range)`：生成消费洞察。
- `toMillis(range)`：把 `DateRange` 转成 `[start, end)` 毫秒时间范围。

品牌参数为 `null` 时表示不过滤品牌；非空时调用对应的品牌筛选查询。

消费洞察包含活跃天数、有记录日期日均消费、日均杯数、最常买品牌、饮品偏好、常喝星期、最高单笔，以及本月预测消费。预测只在 `THIS_MONTH` 且已有消费时返回。

## DateRange

当前支持：

| 枚举 | 文案 | 范围 |
|---|---|---|
| `THIS_WEEK` | 本周 | 本周一 00:00 到下周一 00:00 |
| `THIS_MONTH` | 本月 | 本月 1 日 00:00 到下月 1 日 00:00 |
| `LAST_MONTH` | 上月 | 上月 1 日 00:00 到本月 1 日 00:00 |

这些范围都使用设备本地时区的 `Calendar` 计算。

## 与 ViewModel 的关系

ViewModel 负责：

- 创建并持有三个 Service。
- 将 Service 返回的 `Flow` 转成 `StateFlow`。
- 保存筛选条件和编辑状态。
- 从全部历史记录派生最多 3 个 `PurchaseTemplate`。
- 在 `viewModelScope` 中调用写入操作。

Service 不持有 UI 状态，也不依赖 Compose 生命周期。
