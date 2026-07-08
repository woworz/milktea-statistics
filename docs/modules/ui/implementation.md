# UI 层实现文档

本模块负责界面渲染、用户交互和状态管理。所有 UI 使用 **Jetpack Compose** 声明式框架实现。

## 文件清单

| 文件 | 路径 | 说明 |
|---|---|---|
| `MilkTeaViewModel.kt` | `ui/MilkTeaViewModel.kt` | 业务逻辑与状态管理 |
| `HomeScreen.kt` | `ui/HomeScreen.kt` | 首页 |
| `RecordsScreen.kt` | `ui/RecordsScreen.kt` | 记录列表页 |
| `StatsScreen.kt` | `ui/StatsScreen.kt` | 统计图表页 |
| `SettingsScreen.kt` | `ui/SettingsScreen.kt` | 设置页 |
| `TrendChart.kt` | `ui/TrendChart.kt` | 自定义图表组件（柱状图 + 折线图） |
| `Theme.kt` | `ui/theme/Theme.kt` | Miuix + Material3 主题配色方案 |
| `Dialogs.kt` | `ui/components/Dialogs.kt` | 弹窗组件 |
| `UiCommon.kt` | `ui/components/UiCommon.kt` | 通用页面标题、空状态、筛选胶囊、指标卡组件 |

---

## 1. MilkTeaViewModel.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/MilkTeaViewModel.kt`

### 1.1 ViewModel 类

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `MilkTeaViewModel` class | **第 28-179 行** | 继承 `AndroidViewModel`，管理 UI 状态并调度 Service |

### 1.2 依赖层

| 属性 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `db` | 第 32 行 | `MilkTeaDatabase` | Room 数据库单例（App Startup 已预初始化，无阻塞） |
| `repository` | 第 33-36 行 | `MilkTeaRepository` | 数据仓库实例 |
| `recordService` | 第 39 行 | `RecordService` | 记录业务服务 |
| `brandService` | 第 40 行 | `BrandService` | 品牌业务服务 |
| `analyticsService` | 第 41 行 | `AnalyticsService` | 分析业务服务 |

### 1.3 筛选状态

| 属性 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `_selectedBrand` | 第 45 行 | `MutableStateFlow<String?>` | 当前选中品牌（内部可变） |
| `selectedBrand` | 第 46 行 | `StateFlow<String?>` | 当前选中品牌（外部只读） |
| `_selectedDateRange` | 第 48 行 | `MutableStateFlow<DateRange>` | 当前日期范围（内部可变） |
| `selectedDateRange` | 第 49 行 | `StateFlow<DateRange>` | 当前日期范围（外部只读） |

### 1.4 品牌列表

| 属性 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `allBrands` | 第 53-54 行 | `StateFlow<List<String>>` | 所有品牌列表（由 `BrandService` 提供） |
| `commonBrands` | 第 58-59 行 | `StateFlow<List<CommonBrand>>` | 常用品牌列表（由 `BrandService` 提供） |

### 1.5 日期范围与筛选

| 属性 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `dateRangeMillis` | 第 71-77 行 | `StateFlow<Pair<Long, Long>>` | 日期范围转换为毫秒时间戳（由 `AnalyticsService.toMillis` 计算） |
| `filteredRecords` | 第 82-88 行 | `StateFlow<List<MilkTeaRecord>>` | 筛选后的记录列表（由 `AnalyticsService` 提供） |

### 1.6 今日统计

| 属性 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `todayStart` | 第 92 行 | `Long` | 今日起始时间戳 |
| `todayCount` | 第 94-95 行 | `StateFlow<Int>` | 今日记录数（由 `RecordService` 提供） |
| `todayRecords` | 第 97-98 行 | `StateFlow<List<MilkTeaRecord>>` | 今日记录列表（由 `RecordService` 提供） |

### 1.7 统计与趋势

| 属性 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `stats` | 第 103-109 行 | `StateFlow<DailyStats>` | 筛选范围内的统计（由 `AnalyticsService` 提供） |
| `dailyAggregates` | 第 114-120 行 | `StateFlow<List<DailySummary>>` | 趋势数据（由 `AnalyticsService` 提供） |

### 1.8 编辑状态

| 属性 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `_editingRecord` | 第 124 行 | `MutableStateFlow<MilkTeaRecord?>` | 正在编辑的记录（内部可变） |
| `editingRecord` | 第 125 行 | `StateFlow<MilkTeaRecord?>` | 正在编辑的记录（外部只读） |

### 1.9 函数

| 函数 | 行号 | 返回类型 | 说明 |
|---|---|---|---|
| `addCommonBrand(name)` | 第 61-63 行 | `Unit` | 添加常用品牌（委托 `BrandService`） |
| `removeCommonBrand(id)` | 第 65-67 行 | `Unit` | 删除常用品牌（委托 `BrandService`） |
| `addRecord(brand, drinkName, price, timestamp)` | 第 129-145 行 | `Unit` | 添加记录（委托 `RecordService`） |
| `updateRecord(record)` | 第 147-149 行 | `Unit` | 更新记录（委托 `RecordService`） |
| `deleteRecord(record)` | 第 151-153 行 | `Unit` | 删除记录（委托 `RecordService`） |
| `setBrandFilter(brand)` | 第 155-157 行 | `Unit` | 设置品牌筛选 |
| `setDateRange(range)` | 第 159-161 行 | `Unit` | 设置日期范围筛选 |
| `startEdit(record)` | 第 163-165 行 | `Unit` | 开始编辑记录 |
| `cancelEdit()` | 第 167-169 行 | `Unit` | 取消编辑 |
| `getStartOfToday()` | 第 171-178 行 | `Long` | 计算今日起始时间戳 |

---

## 2. 通用 UI 组件

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/components/UiCommon.kt`

为提升界面一致性，首页、记录、统计、设置页共享以下基础组件：

| 函数 | 行号 | 说明 |
|---|---|---|
| `AppTopBar(title, subtitle)` | - | 统一页面顶部标题和说明文案，保留状态栏安全区，额外顶部间距压缩为 6dp |
| `EmptyStateCard(...)` | - | 统一空状态卡片，可选主操作按钮 |
| `FilterPill(label, selected, onClick)` | - | 用于日期范围、品牌、图表指标等筛选项，选中态更清晰 |
| `SectionHeader(title, trailing)` | - | 区块标题和右侧摘要信息 |
| `MetricCard(label, value, icon, valueColor)` | - | 首页和统计页共享的数据指标卡 |

---

## 3. HomeScreen.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/HomeScreen.kt`

### 3.1 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `HomeScreen(viewModel)` | - | 首页 Screen，展示今日概览、今日记录和添加入口 |
| `TodayOverview(todaySpend, todayCount)` | - | 两列指标卡，展示今日花费和今日杯数 |
| `TodayRecordCard(record)` | - | 今日记录卡片，突出品牌、价格、饮品名和时间 |
| `recentSummary(record)` | - | 生成最近一条记录的摘要文本 |

### 3.2 交互与视觉调整

- 顶部标题改为「今日奶茶」，副标题说明核心操作，降低用户理解成本。
- 今日花费和杯数改为 `MetricCard`，让关键数据在第一屏可快速扫读。
- 空状态使用 `EmptyStateCard`，并提供「添加记录」操作，减少用户寻找入口的时间。
- 今日记录卡片增加内部间距和信息分层，未填写饮品时显示「未填写饮品」而不是留白。

---

## 4. RecordsScreen.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/RecordsScreen.kt`

### 4.1 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `RecordsScreen(viewModel)` | - | 记录页 Screen，历史记录列表 + 筛选 + 添加/编辑弹窗 |
| `RecordsFilterSection(...)` | - | 日期范围和品牌筛选区，使用 `FilterPill` 表达选中态 |
| `RecordCard(record, onEdit, onDelete)` | - | 记录卡片，可点击编辑，删除前会弹出确认 |

### 4.2 交互与视觉调整

- 页面顶部说明「按时间和品牌筛选，点击卡片可编辑」，让编辑入口更符合直觉。
- 日期范围与品牌筛选改为胶囊控件，选中态通过背景、边框、文字权重共同表达。
- 列表上方增加记录数量摘要。
- 删除操作由直接删除改为二次确认，避免误触导致数据丢失。
- 空状态提供添加记录入口，同时提示可切换筛选条件。

---

## 5. StatsScreen.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/StatsScreen.kt`

### 5.1 枚举类

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `ChartMetric` enum | **第 40-43 行** | 图表指标枚举：`COUNT`("杯数"), `SPEND`("金额") |

### 5.2 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `StatsScreen(viewModel)` | - | 统计页 Screen，包含筛选、概览指标、趋势图 |
| `StatsRow(stats)` | - | 统计卡片组（总花费 / 杯数 / 平均单价） |

### 5.3 交互与视觉调整

- 筛选控件统一使用 `FilterPill`，与记录页保持一致。
- 统计指标改用 `MetricCard`，第一行展示总花费和杯数，第二行展示平均单价，避免三列在窄屏上拥挤。
- 图表指标和图表类型拆成两组控件，降低横向挤压。
- 图表卡片增加标题，明确当前展示的是「每日杯数」还是「每日金额」。
- 无趋势数据时使用统一空状态，而不是单行提示。

---

## 6. SettingsScreen.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/SettingsScreen.kt`

| 函数 | 行号 | 说明 |
|---|---|---|
| `SettingsScreen(viewModel)` | - | 设置页 Screen，常用品牌添加与删除 |

### 6.1 交互与视觉调整

- 顶部增加副标题，说明常用品牌会提升添加记录效率。
- 添加输入区改为卡片分组，和品牌列表形成清晰层级。
- 品牌列表改为卡片内列表，每项增加「添加记录时可快速选择」说明。
- 删除图标增加具体 `contentDescription`，便于无障碍读屏识别。
- 空状态说明常用品牌的价值，而不只是显示「还没有」。

---

## 7. TrendChart.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/TrendChart.kt`

### 7.1 枚举类

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `ChartMetric` enum | **第 40-43 行** | 图表指标：`COUNT`("杯数"), `SPEND`("金额") |

### 7.2 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `TrendChart(dailyData, metric, ...)` | **第 46-225 行** | 柱状趋势图（Canvas 绘制） |
| `TrendLineChart(dailyData, metric, ...)` | **第 227-473 行** | 折线趋势图（Canvas 绘制，Cubic Bézier 平滑） |

### 7.3 TrendChart 内部逻辑

| 逻辑 | 行号 | 说明 |
|---|---|---|
| 渐变柱状图绘制 | 第 147-168 行 | 使用 `Brush.verticalGradient` 绘制渐变柱子 |
| Y 轴网格线 | 第 122-144 行 | 4 条水平网格线 + 数值标签 |
| X 轴日期标签 | 第 170-183 行 | `MM/dd` 格式日期 |
| 点击检测 | 第 78-109 行 | `detectTapGestures` 检测最近柱子 |
| Tooltip 显示 | 第 187-223 行 | 选中后显示日期和数值的浮动提示框 |

### 7.4 TrendLineChart 内部逻辑

| 逻辑 | 行号 | 说明 |
|---|---|---|
| Cubic Bézier 曲线 | 第 338-400 行 | 使用控制点计算平滑曲线 |
| 渐变填充 | 第 359-393 行 | 曲线下方区域填充半透明渐变 |
| 数据点绘制 | 第 402-430 行 | 白边圆点标记 |
| 点击检测 | 第 255-294 行 | 基于曼哈顿距离寻找最近数据点 |
| Tooltip 显示 | 第 435-471 行 | 浮动提示框 |

---

## 8. Theme.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/theme/Theme.kt`

### 8.1 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `MilkTeaTheme(darkTheme, dynamicColor, content)` | - | 应用主题包装器，同时提供 MiuixTheme 和 MaterialTheme |

### 8.2 主题配置

| 配置 | 说明 |
|---|---|
| `ThemeController` | Miuix 主题控制器，管理颜色方案 |
| `ColorSchemeMode.MonetSystem` | 默认使用 Monet 动态取色 |
| `MiuixTheme` | Miuix 主题容器，提供 colors 和 textStyles |
| `MaterialTheme` | Material3 主题容器，颜色由 Miuix 语义色映射，供 `AlertDialog`、`DatePicker`、`TimePicker`、图表 Tooltip 等 Material3 组件使用 |
| `isSystemInDarkTheme()` | 作为 `darkTheme` 默认值，确保 Material3 组件随系统深色模式切换 |

### 8.3 深色模式同步

主界面大量使用 Miuix 组件，而添加记录、管理品牌、日期选择器、时间选择器等弹窗使用 Material3 组件。为避免主界面已进入深色模式但弹窗仍显示亮色，`MilkTeaTheme` 会在 `MiuixTheme` 内再包一层 `MaterialTheme`：

```kotlin
MiuixTheme(controller = controller) {
    val miuixColors = MiuixTheme.colorScheme
    val materialColors = if (darkTheme) {
        darkColorScheme(...)
    } else {
        lightColorScheme(...)
    }

    MaterialTheme(
        colorScheme = materialColors,
        content = content,
    )
}
```

Material3 的 `primary`、`surface`、`onSurface`、`error`、`outline` 等颜色来自 Miuix 语义色，因此添加页弹窗和日期/时间选择器会与 App 主界面一起跟随系统深色模式。

### 8.4 Miuix 组件依赖

在 `build.gradle.kts` 中添加：
```kotlin
implementation("top.yukonga.miuix.kmp:miuix:0.8.8")
```

---

## 9. Dialogs.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/components/Dialogs.kt`

### 9.1 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `AddEditRecordDialog(...)` | **第 47-245 行** | 添加/编辑记录弹窗 |
| `ManageBrandsDialog(...)` | 第 250-317 行 | 常用品牌管理弹窗 |
| `MilkTeaDatePickerDialog(...)` | 第 323-342 行 | 日期选择器弹窗 |
| `MilkTeaTimePickerDialog(...)` | 第 348-372 行 | 时间选择器弹窗（24小时制） |

### 9.2 AddEditRecordDialog 内部逻辑

| 逻辑 | 行号 | 说明 |
|---|---|---|
| 常用品牌快捷标签 | 第 76-103 行 | `FilterChip` 横向滚动列表 |
| 品牌输入框 | 第 105-112 行 | `OutlinedTextField` |
| 饮品名称输入框 | 第 114-121 行 | `OutlinedTextField`（可选） |
| 价格输入框 | 第 123-138 行 | `OutlinedTextField` + 数字键盘 + 校验 |
| 日期选择卡片 | 第 146-158 行 | 点击弹出 `MilkTeaDatePickerDialog` |
| 时间选择卡片 | 第 159-170 行 | 点击弹出 `MilkTeaTimePickerDialog` |
| 确认按钮 | 第 174-191 行 | 校验后回调 `onConfirm` |

---

## 索引总表

按字母排序，方便查找：

| 函数名 | 所在文件 | 行号 | 说明 |
|---|---|---|---|
| `AddEditRecordDialog(...)` | `Dialogs.kt` | - | 添加/编辑记录弹窗 |
| `addCommonBrand(name)` | `MilkTeaViewModel.kt` | 61-63 | 添加常用品牌 |
| `addRecord(brand, drinkName, price, timestamp)` | `MilkTeaViewModel.kt` | 129-145 | 添加记录 |
| `AppNavigation()` | `AppNavigation.kt` | 47-86 | 底部导航 + 路由 |
| `AppTopBar(title, subtitle)` | `UiCommon.kt` | - | 页面顶部标题组件 |
| `cancelEdit()` | `MilkTeaViewModel.kt` | 167-169 | 取消编辑 |
| `ChartMetric` enum | `StatsScreen.kt` | 40-43 | 图表指标 |
| `ChartMetric` enum | `TrendChart.kt` | 40-43 | 图表指标 |
| `DateRange` enum | `model/DateRange.kt` | 3-7 | 日期范围枚举 |
| `deleteRecord(record)` | `MilkTeaViewModel.kt` | 151-153 | 删除记录 |
| `EmptyStateCard(...)` | `UiCommon.kt` | - | 统一空状态卡片 |
| `FilterPill(label, selected, onClick)` | `UiCommon.kt` | - | 统一筛选胶囊 |
| `HomeScreen(viewModel)` | `HomeScreen.kt` | - | 首页 |
| `MainActivity.onCreate()` | `MainActivity.kt` | 11-18 | 应用入口 |
| `ManageBrandsDialog(...)` | `Dialogs.kt` | 250-317 | 品牌管理弹窗 |
| `MetricCard(label, value, ...)` | `UiCommon.kt` | - | 统一指标卡 |
| `MilkTeaDatePickerDialog(...)` | `Dialogs.kt` | 323-342 | 日期选择器 |
| `MilkTeaTheme(darkTheme, dynamicColor, content)` | `Theme.kt` | - | Miuix + Material3 主题包装器 |
| `MilkTeaTimePickerDialog(...)` | `Dialogs.kt` | 348-372 | 时间选择器 |
| `MilkTeaViewModel(application)` | `MilkTeaViewModel.kt` | 28-179 | ViewModel（服务调度） |
| `NavDestination` data class | `AppNavigation.kt` | 32-36 | 导航目标数据类 |
| `recentSummary(record)` | `HomeScreen.kt` | - | 最近记录摘要 |
| `RecordCard(record, onEdit, onDelete)` | `RecordsScreen.kt` | - | 记录卡片 |
| `RecordsFilterSection(...)` | `RecordsScreen.kt` | - | 筛选栏 |
| `RecordsScreen(viewModel)` | `RecordsScreen.kt` | - | 记录页 |
| `removeCommonBrand(id)` | `MilkTeaViewModel.kt` | 65-67 | 删除常用品牌 |
| `SectionHeader(title, trailing)` | `UiCommon.kt` | - | 区块标题组件 |
| `setBrandFilter(brand)` | `MilkTeaViewModel.kt` | 155-157 | 设置品牌筛选 |
| `setDateRange(range)` | `MilkTeaViewModel.kt` | 159-161 | 设置日期范围 |
| `SettingsScreen(viewModel)` | `SettingsScreen.kt` | - | 设置页 |
| `startEdit(record)` | `MilkTeaViewModel.kt` | 163-165 | 开始编辑 |
| `StatsRow(stats)` | `StatsScreen.kt` | - | 统计卡片组 |
| `StatsScreen(viewModel)` | `StatsScreen.kt` | - | 统计页 |
| `TodayOverview(todaySpend, todayCount)` | `HomeScreen.kt` | - | 今日概览 |
| `TodayRecordCard(record)` | `HomeScreen.kt` | - | 今日记录卡片 |
| `toMillis(range)` | `AnalyticsService.kt` | 43-63 | 日期范围转毫秒 |
| `TrendChart(dailyData, metric, ...)` | `TrendChart.kt` | 46-225 | 柱状趋势图 |
| `TrendLineChart(dailyData, metric, ...)` | `TrendChart.kt` | 227-473 | 折线趋势图 |
| `updateRecord(record)` | `MilkTeaViewModel.kt` | 147-149 | 更新记录 |
