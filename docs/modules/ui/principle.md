# UI 层原理

## 状态驱动 UI

页面通过 `collectAsStateWithLifecycle()` 订阅 ViewModel 暴露的 `StateFlow`：

```kotlin
val todayCount by viewModel.todayCount.collectAsStateWithLifecycle()
```

数据变化时，Room Flow 重新发射，ViewModel 的 `StateFlow` 更新，Compose 自动重组相关 UI。页面不主动刷新列表或统计。

## 状态分层

| 状态位置 | 适合内容 |
|---|---|
| `remember` | 弹窗显隐、输入框临时内容、局部 UI 选择 |
| `MilkTeaViewModel` | 筛选条件、编辑记录、业务数据 |
| Service/Repository | 不保存 UI 状态，只提供查询和写入 |

这个分层让配置变更后关键状态仍由 ViewModel 保留，同时避免把临时表单状态扩散到业务层。

## 筛选联动

记录页和统计页共享同一组筛选状态：

```text
selectedDateRange + selectedBrand
  ↓
dateRangeMillis
  ↓
filteredRecords / stats / dailyAggregates / insights
```

`flatMapLatest` 会在筛选条件变化时切换到新的 Room 查询。记录页和统计页使用同一个 ViewModel，所以筛选条件天然同步。

记录页搜索只在当前 `filteredRecords` 上做本地过滤，不会影响统计页筛选。

## 今日数据

首页今日数据由 `todayRange` 驱动。ViewModel 会计算今天的起止时间，并在跨过午夜后发射新的范围，再由 `RecordService` 查询新的今日记录和杯数。

## 洞察和复购模板

`ConsumptionInsights` 是派生状态，不单独落库。ViewModel 组合记录列表、每日聚合、统计值和日期范围，再交给 `AnalyticsService` 计算活跃日均、最常买品牌、饮品偏好、常喝星期、最高单笔和本月预测。

`PurchaseTemplate` 也由历史记录派生。模板按品牌、饮品名和价格分组，按复购次数和最近购买时间排序，限制最多 3 个，避免添加弹窗变得拥挤。

## 图表交互

柱状图和折线图都在 Canvas 中绘制，流程大致相同：

1. 从 `DailySummary` 取杯数或金额。
2. 计算最大值和图表坐标。
3. 绘制网格、柱子或曲线。
4. 用 `detectTapGestures` 找到离点击位置最近的数据点。
5. 保存选中点并显示 Tooltip。

折线图使用 Cubic Bezier 控制点做平滑曲线；柱状图使用渐变填充突出数据强弱。

## 主题

UI 统一依赖 Material 3。`MilkTeaTheme` 提供 `MaterialTheme` 色板，并在支持的平台上启用动态取色；关闭动态取色时，根据系统深色模式选择内置的亮色或深色配色。

## 可访问性和交互

- 图标按钮需要提供明确的 `contentDescription`。
- 删除记录前二次确认。
- 空状态尽量提供下一步操作。
- 筛选选中态不能只依赖颜色，应同时用边框、字重或背景表达。
- 搜索框提供清空按钮；添加弹窗的日期模式和时间模式使用“返回”回到表单，避免误关闭整张表单。
