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
| `Theme.kt` | `ui/theme/Theme.kt` | 主题配色方案 |
| `Dialogs.kt` | `ui/components/Dialogs.kt` | 弹窗组件 |

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

## 2. HomeScreen.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/HomeScreen.kt`

### 2.1 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `HomeScreen(viewModel)` | **第 57-183 行** | 首页 Screen，展示今日概览和记录 |
| `GradientStatCard(todaySpend, todayCount)` | 第 186-248 行 | 渐变统计卡片（花费 + 杯数） |
| `RecentRecordsRow(records)` | 第 250-260 行 | 最近记录横向滚动行 |
| `RecentRecordCard(record)` | 第 262-310 行 | 最近记录卡片（品牌图标 + 价格 + 时间） |
| `TodayRecordCard(record, onDelete)` | 第 312-367 行 | 今日记录卡片（可删除） |
| `relativeTimeLabel(timestamp)` | 第 369-383 行 | 相对时间标签（刚刚 / x分钟前 / x小时前） |
| `brandEmoji(brand)` | 第 385-392 行 | 根据品牌返回对应 Emoji |

---

## 3. RecordsScreen.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/RecordsScreen.kt`

### 3.1 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `RecordsScreen(viewModel)` | **第 57-176 行** | 记录页 Screen，历史记录列表 + 筛选 |
| `RecordsFilterSection(...)` | 第 180-246 行 | 筛选栏（日期范围分段按钮 + 品牌下拉框） |
| `RecordCard(record, onEdit, onDelete)` | 第 250-316 行 | 记录卡片（可点击编辑，可删除） |

---

## 4. StatsScreen.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/StatsScreen.kt`

### 4.1 枚举类

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `ChartMetric` enum | **第 40-43 行** | 图表指标枚举：`COUNT`("杯数"), `SPEND`("金额") |

### 4.2 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `StatsScreen(viewModel)` | **第 44-220 行** | 统计页 Screen |
| `StatsRow(stats)` | 第 222-244 行 | 统计卡片行（总花费 / 杯数 / 均价） |
| `StatItem(label, value, modifier)` | 第 246-281 行 | 单个统计卡片 |

---

## 5. SettingsScreen.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/SettingsScreen.kt`

| 函数 | 行号 | 说明 |
|---|---|---|
| `SettingsScreen(viewModel)` | **第 33-128 行** | 设置页 Screen，常用品牌管理 |

---

## 6. TrendChart.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/TrendChart.kt`

### 6.1 枚举类

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `ChartMetric` enum | **第 40-43 行** | 图表指标：`COUNT`("杯数"), `SPEND`("金额") |

### 6.2 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `TrendChart(dailyData, metric, ...)` | **第 46-225 行** | 柱状趋势图（Canvas 绘制） |
| `TrendLineChart(dailyData, metric, ...)` | **第 227-473 行** | 折线趋势图（Canvas 绘制，Cubic Bézier 平滑） |

### 6.3 TrendChart 内部逻辑

| 逻辑 | 行号 | 说明 |
|---|---|---|
| 渐变柱状图绘制 | 第 147-168 行 | 使用 `Brush.verticalGradient` 绘制渐变柱子 |
| Y 轴网格线 | 第 122-144 行 | 4 条水平网格线 + 数值标签 |
| X 轴日期标签 | 第 170-183 行 | `MM/dd` 格式日期 |
| 点击检测 | 第 78-109 行 | `detectTapGestures` 检测最近柱子 |
| Tooltip 显示 | 第 187-223 行 | 选中后显示日期和数值的浮动提示框 |

### 6.4 TrendLineChart 内部逻辑

| 逻辑 | 行号 | 说明 |
|---|---|---|
| Cubic Bézier 曲线 | 第 338-400 行 | 使用控制点计算平滑曲线 |
| 渐变填充 | 第 359-393 行 | 曲线下方区域填充半透明渐变 |
| 数据点绘制 | 第 402-430 行 | 白边圆点标记 |
| 点击检测 | 第 255-294 行 | 基于曼哈顿距离寻找最近数据点 |
| Tooltip 显示 | 第 435-471 行 | 浮动提示框 |

---

## 7. Theme.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/theme/Theme.kt`

### 7.1 颜色定义

| 颜色常量 | 行号 | 色值 | 用途 |
|---|---|---|---|
| `MilkTeaBrown` | 第 16 行 | `#D4A574` | Primary - 奶茶棕 |
| `CreamWhite` | 第 17 行 | `#FFFFF8E7` | Background - 奶油白 |
| `MatchaGreen` | 第 18 行 | `#8FBC8F` | Secondary - 抹茶绿 |
| `MilkTeaBrownDark` | 第 21 行 | `#B8956A` | Primary 深色变体 |
| `MilkTeaBrownLight` | 第 22 行 | `#E8C9A8` | Primary Container |
| `DarkBackground` | 第 27 行 | `#1A1410` | 深色模式背景 |
| `DarkSurface` | 第 28 行 | `#2D2520` | 深色模式 Surface |

### 7.2 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `MilkTeaTheme(darkTheme, dynamicColor, content)` | **第 111-129 行** | 应用主题包装器 |

### 7.3 主题配置

| 配置 | 行号 | 说明 |
|---|---|---|
| `LightColorScheme` | 第 32-69 行 | 浅色主题完整配色方案 |
| `DarkColorScheme` | 第 71-108 行 | 深色主题完整配色方案 |
| 动态取色逻辑 | 第 116-119 行 | Android 12+ 使用 `dynamicDarkColorScheme` / `dynamicLightColorScheme` |

---

## 8. Dialogs.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/components/Dialogs.kt`

### 8.1 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `AddEditRecordDialog(...)` | **第 47-245 行** | 添加/编辑记录弹窗 |
| `ManageBrandsDialog(...)` | 第 250-317 行 | 常用品牌管理弹窗 |
| `MilkTeaDatePickerDialog(...)` | 第 323-342 行 | 日期选择器弹窗 |
| `MilkTeaTimePickerDialog(...)` | 第 348-372 行 | 时间选择器弹窗（24小时制） |

### 8.2 AddEditRecordDialog 内部逻辑

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
| `AddEditRecordDialog(...)` | `Dialogs.kt` | 47-245 | 添加/编辑记录弹窗 |
| `addCommonBrand(name)` | `MilkTeaViewModel.kt` | 61-63 | 添加常用品牌 |
| `addRecord(brand, drinkName, price, timestamp)` | `MilkTeaViewModel.kt` | 129-145 | 添加记录 |
| `AppNavigation()` | `AppNavigation.kt` | 47-86 | 底部导航 + 路由 |
| `brandEmoji(brand)` | `HomeScreen.kt` | 385-392 | 品牌 Emoji 映射 |
| `cancelEdit()` | `MilkTeaViewModel.kt` | 167-169 | 取消编辑 |
| `ChartMetric` enum | `StatsScreen.kt` | 40-43 | 图表指标 |
| `ChartMetric` enum | `TrendChart.kt` | 40-43 | 图表指标 |
| `DateRange` enum | `model/DateRange.kt` | 3-7 | 日期范围枚举 |
| `deleteRecord(record)` | `MilkTeaViewModel.kt` | 151-153 | 删除记录 |
| `GradientStatCard(todaySpend, todayCount)` | `HomeScreen.kt` | 186-248 | 渐变统计卡片 |
| `HomeScreen(viewModel)` | `HomeScreen.kt` | 57-183 | 首页 |
| `MainActivity.onCreate()` | `MainActivity.kt` | 11-18 | 应用入口 |
| `ManageBrandsDialog(...)` | `Dialogs.kt` | 250-317 | 品牌管理弹窗 |
| `MilkTeaDatePickerDialog(...)` | `Dialogs.kt` | 323-342 | 日期选择器 |
| `MilkTeaTheme(darkTheme, dynamicColor, content)` | `Theme.kt` | 111-129 | 主题包装器 |
| `MilkTeaTimePickerDialog(...)` | `Dialogs.kt` | 348-372 | 时间选择器 |
| `MilkTeaViewModel(application)` | `MilkTeaViewModel.kt` | 28-179 | ViewModel（服务调度） |
| `NavDestination` data class | `AppNavigation.kt` | 32-36 | 导航目标数据类 |
| `RecentRecordCard(record)` | `HomeScreen.kt` | 262-310 | 最近记录卡片 |
| `RecentRecordsRow(records)` | `HomeScreen.kt` | 250-260 | 最近记录行 |
| `RecordCard(record, onEdit, onDelete)` | `RecordsScreen.kt` | 250-316 | 记录卡片 |
| `RecordsFilterSection(...)` | `RecordsScreen.kt` | 180-246 | 筛选栏 |
| `RecordsScreen(viewModel)` | `RecordsScreen.kt` | 57-176 | 记录页 |
| `relativeTimeLabel(timestamp)` | `HomeScreen.kt` | 369-383 | 相对时间标签 |
| `removeCommonBrand(id)` | `MilkTeaViewModel.kt` | 65-67 | 删除常用品牌 |
| `setBrandFilter(brand)` | `MilkTeaViewModel.kt` | 155-157 | 设置品牌筛选 |
| `setDateRange(range)` | `MilkTeaViewModel.kt` | 159-161 | 设置日期范围 |
| `SettingsScreen(viewModel)` | `SettingsScreen.kt` | 33-128 | 设置页 |
| `startEdit(record)` | `MilkTeaViewModel.kt` | 163-165 | 开始编辑 |
| `StatItem(label, value, modifier)` | `StatsScreen.kt` | 246-281 | 统计卡片 |
| `StatsRow(stats)` | `StatsScreen.kt` | 222-244 | 统计卡片行 |
| `StatsScreen(viewModel)` | `StatsScreen.kt` | 44-220 | 统计页 |
| `TodayRecordCard(record, onDelete)` | `HomeScreen.kt` | 312-367 | 今日记录卡片 |
| `toMillis(range)` | `AnalyticsService.kt` | 43-63 | 日期范围转毫秒 |
| `TrendChart(dailyData, metric, ...)` | `TrendChart.kt` | 46-225 | 柱状趋势图 |
| `TrendLineChart(dailyData, metric, ...)` | `TrendChart.kt` | 227-473 | 折线趋势图 |
| `updateRecord(record)` | `MilkTeaViewModel.kt` | 147-149 | 更新记录 |
