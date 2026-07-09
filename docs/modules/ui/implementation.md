# UI 层实现

UI 层基于 Jetpack Compose 和 Material 3。代码位于 `app/src/main/java/com/mason/milkteastatistics/ui/`。

## 文件

| 文件 | 职责 |
|---|---|
| `MilkTeaViewModel.kt` | UI 状态、Flow 组合、Service 调度 |
| `HomeScreen.kt` | 首页今日概览、今日记录、添加入口 |
| `RecordsScreen.kt` | 历史记录、日期/品牌筛选、搜索、编辑删除 |
| `StatsScreen.kt` | 统计指标、消费洞察、图表类型和指标切换 |
| `SettingsScreen.kt` | 常用品牌添加和删除 |
| `TrendChart.kt` | 柱状图、折线图和 Tooltip |
| `components/Dialogs.kt` | 添加/编辑记录弹窗、复购模板、品牌管理、紧凑日历和时间选择 |
| `components/UiCommon.kt` | 页面标题、空状态、筛选胶囊、指标卡 |
| `theme/Theme.kt` | Material 3 主题和动态取色 |
| `navigation/AppNavigation.kt` | 底部导航和路由 |

## ViewModel

`MilkTeaViewModel` 是 UI 层的状态中枢：

- 手动创建 `MilkTeaDatabase`、`MilkTeaRepository` 和三个 Service。
- 保存 `selectedBrand`、`selectedDateRange`、`editingRecord`。
- 将 Service 返回的 `Flow` 转成 `StateFlow`。
- 通过 `combine` 和 `flatMapLatest` 让筛选条件自动驱动记录、统计和趋势查询。
- 通过 `todayRange` 让首页今日数据跨午夜后自动刷新。
- 从全部历史记录派生 `purchaseTemplates`，供首页和记录页的添加弹窗快速填表。
- 组合统计页已有数据生成 `insights`。

写操作都在 `viewModelScope.launch` 中执行，页面只调用 ViewModel 方法。

## 页面

| 页面 | 主要状态 | 主要交互 |
|---|---|---|
| `HomeScreen` | `todayCount`、`todayRecords`、`purchaseTemplates` | 添加记录 |
| `RecordsScreen` | `filteredRecords`、`selectedDateRange`、`selectedBrand`、`purchaseTemplates` | 筛选、搜索、编辑、删除、添加 |
| `StatsScreen` | `stats`、`dailyAggregates`、`insights`、筛选状态 | 切换日期、品牌、指标和图表类型 |
| `SettingsScreen` | `commonBrands` | 添加/删除常用品牌 |

通用 UI 尽量放在 `UiCommon.kt`，避免四个页面重复实现空状态、筛选项和指标卡。

## 弹窗

`AddEditRecordDialog` 同时用于新增和编辑记录：

- 常用品牌快捷选择。
- 历史复购模板，一键填充品牌、饮品名和价格。
- 品牌、饮品名、价格输入。
- 内置紧凑日历选择日期，Material 3 `TimePicker` 选择时间。
- 内嵌常用品牌管理入口。
- 提交前做基础校验。

记录页删除操作会先确认，避免误触直接删除。

记录页搜索是本地过滤，不改变 ViewModel 的筛选状态。搜索范围包括品牌、饮品名、价格和记录时间显示文本。

## 图表

`TrendChart.kt` 提供两种趋势图：

- `TrendChart`：Canvas 绘制柱状图。
- `TrendLineChart`：Canvas 绘制平滑折线图。

`ChartMetric` 控制图表展示杯数或金额。两种图表都支持点击最近的数据点并显示 Tooltip。

## 主题

`MilkTeaTheme` 提供 Material 3 的 `MaterialTheme`。支持动态取色的平台默认使用系统动态色；关闭动态取色时使用应用内置的亮色/深色配色。

默认配置：

- `dynamicColor = true` 且系统支持时使用 Material 3 动态色。
- 关闭动态取色时，根据系统深色模式选择内置亮色或深色配色。

## UI 维护约定

- 页面只读 `StateFlow`，业务写入统一走 ViewModel。
- 新增跨页面复用的控件优先放 `UiCommon.kt`。
- 新增弹窗或表单时使用 Material 3 组件，并保持在 `MilkTeaTheme` 内。
- 新增图表指标时同步更新 `ChartMetric`、图表取值逻辑和统计页切换控件。
- 新增统计洞察时同步更新 `ConsumptionInsights`、`AnalyticsService` 和 `StatsScreen`。
