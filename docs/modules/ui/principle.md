# UI 层原理文档

## 1. 架构定位

UI 层是用户直接可见的部分，采用 **Jetpack Compose** 声明式 UI 框架，配合 **MVVM 模式** 实现状态驱动界面更新。

```
用户操作
    ↓ (onClick / onValueChange)
ViewModel (更新 StateFlow)
    ↓ (Flow 发射新值)
UI (Recomposition)
    ↓
Compose 渲染到屏幕
```

## 2. Jetpack Compose 核心原理

### 2.1 声明式 UI

传统的 View 系统是命令式（Imperative）：
```kotlin
// View 系统：命令式
textView.text = "Hello"
textView.visibility = View.VISIBLE
```

Compose 是声明式（Declarative）：
```kotlin
// Compose：声明式
@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name")  // 描述 UI 应该长什么样
}
```

**区别**：
- 命令式：程序员负责手动更新每个 UI 元素的状态
- 声明式：只需描述 UI 与数据的关系，框架自动处理更新

### 2.2 重组 (Recomposition)

Compose 通过 `@Composable` 函数构建 UI 树。当状态变化时，框架自动重新执行受影响的 Composable 函数，这就是**重组**。

```kotlin
@Composable
fun HomeScreen(viewModel: MilkTeaViewModel) {
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle()
    // 当 todayCount 变化时，使用它的这部分 UI 会自动重组
    Text("今日 $todayCount 杯")
}
```

重组的特点：
- **智能**：只重组读取了变化状态的 Composable
- **可跳过**：如果参数未变，Compose 可以跳过该函数的重新执行
- **非确定位置**：重组可能发生在任何线程，但总是在帧绘制前完成

### 2.3 remember 与状态提升

```kotlin
@Composable
fun RecordsScreen(viewModel: MilkTeaViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    // ...
}
```

- `remember`：在重组之间保持状态值
- `mutableStateOf`：创建可观察的状态容器
- `by` 委托语法：使读写更简洁

**本地状态 vs ViewModel 状态**：

| 状态位置 | 适用场景 | 生命周期 |
|---|---|---|
| `remember` 本地 | 弹窗显隐、输入框临时值、动画状态 | Composable 生命周期 |
| `ViewModel` | 业务数据、筛选条件、编辑状态 | 配置变更后存活 |

### 2.4 Side Effects

Compose 函数应该是无副作用的纯函数。需要副作用时使用 Effect API：

```kotlin
// LaunchedEffect：在重组时启动协程
LaunchedEffect(key1) {
    // 只在 key1 变化时执行
}

// DisposableEffect：需要清理的副作用
DisposableEffect(key1) {
    // 初始化...
    onDispose {
        // 清理...
    }
}
```

本项目未使用复杂 Side Effects，主要依赖 `collectAsStateWithLifecycle` 订阅 Flow。

## 3. MVVM 状态管理

### 3.1 模式结构

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│   View       │────→│   ViewModel      │────→│   Model      │
│  (Compose)   │←────│  (StateFlow)     │←────│ (Repository) │
└──────────────┘     └──────────────────┘     └──────────────┘
        ↑                    ↓
   用户交互            状态变更通知
```

### 3.2 StateFlow vs LiveData

本项目使用 `StateFlow` 替代传统的 `LiveData`：

| 特性 | StateFlow | LiveData |
|---|---|---|
| 生命周期感知 | 需配合 `collectAsStateWithLifecycle` | 内置 (`LifecycleOwner`) |
| 多平台 | Kotlin Multiplatform 支持 | Android Only |
| 操作符 | 丰富的 Flow 操作符 (`combine`, `flatMapLatest`) | Transformations 有限 |
| 默认值 | 必须有初始值 | 可延迟初始化 |
| 重复值过滤 | 自动过滤相同值 | 自动过滤相同值 |

### 3.3 状态订阅

```kotlin
val todayCount by viewModel.todayCount.collectAsStateWithLifecycle()
```

`collectAsStateWithLifecycle` 的作用：
- 在 `Lifecycle.State.STARTED` 时开始收集 Flow
- 在 `Lifecycle.State.STOPPED` 时停止收集
- 避免后台不必要的更新，节省资源
- 自动处理生命周期安全

等价于：
```kotlin
val lifecycleOwner = LocalLifecycleOwner.current
val todayCount by remember(viewModel.todayCount, lifecycleOwner) {
    viewModel.todayCount.flowWithLifecycle(lifecycleOwner.lifecycle)
}.collectAsState(initial = 0)
```

### 3.4 状态分层

ViewModel 中状态分为两类：

**UI 状态（StateFlow）**：
```kotlin
val todayCount: StateFlow<Int> = repository.getCountForDay(...)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
```

- `stateIn`：将冷 Flow 转为热 StateFlow
- `SharingStarted.WhileSubscribed(5_000)`：有订阅者时活跃，最后订阅者离开后 5 秒停止
- 避免配置变更（如旋转屏幕）导致重复查询

**内部可变状态（MutableStateFlow）**：
```kotlin
private val _selectedBrand = MutableStateFlow<String?>(null)
val selectedBrand: StateFlow<String?> = _selectedBrand.asStateFlow()
```

- 内部使用 `MutableStateFlow` 修改
- 对外暴露只读的 `StateFlow`
- 防止外部直接修改状态，确保数据流单向

## 4. 筛选联动原理

### 4.1 combine + flatMapLatest

```kotlin
val filteredRecords: StateFlow<List<MilkTeaRecord>> = dateRangeMillis
    .combine(_selectedBrand) { range, brand -> range to brand }
    .flatMapLatest { (range, brand) ->
        val (start, end) = range
        if (brand != null) {
            repository.getRecordsByDateRangeAndBrand(start, end, brand)
        } else {
            repository.getRecordsByDateRange(start, end)
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

**执行流程**：
1. `dateRangeMillis` 或 `_selectedBrand` 任一变化
2. `combine` 将两个最新值合并为 `Pair`
3. `flatMapLatest` 根据 Pair 值选择对应的 Repository 查询
4. Repository 返回新的 `Flow<List<MilkTeaRecord>>`
5. `stateIn` 将该 Flow 转为 StateFlow
6. UI 自动重组，显示新列表

**flatMapLatest 的作用**：
- 当筛选条件快速变化时，取消上一个未完成的查询
- 避免条件 A 的查询结果在条件 B 之后才返回，导致 UI 显示旧数据

### 4.2 时间戳计算

```kotlin
private fun DateRange.toMillis(): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return when (this) {
        DateRange.THIS_WEEK -> {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val start = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 7)
            start to cal.timeInMillis
        }
        // ...
    }
}
```

- 使用 `Calendar` 计算周/月起止时间戳
- 先将时间归零到 00:00:00.000，确保跨天边界一致
- 本周以周一为起始（`Calendar.MONDAY`）

## 5. 自定义 Canvas 图表

### 5.1 柱状图 (TrendChart)

```kotlin
Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
    // 1. 计算最大值和比例
    val maxValue = dailyData.maxOf { ... }
    val yMax = ceil(maxValue * 1.2f)
    
    // 2. 绘制 Y 轴网格线
    for (i in 0..gridLines) { ... }
    
    // 3. 绘制渐变柱子
    dailyData.forEachIndexed { index, summary ->
        val gradient = Brush.verticalGradient(...)
        drawRoundRect(brush = gradient, ...)
    }
}
```

**要点**：
- 柱宽 = `chartWidth / count * 0.6f`，间隙 = `chartWidth / count * 0.4f`
- Y 轴最大值上浮 20%，留出顶部空间
- 使用 `Brush.verticalGradient` 实现上下渐变效果

### 5.2 折线图 (TrendLineChart)

```kotlin
// 1. 构建数据点
val points = dailyData.mapIndexed { index, summary ->
    val x = index * stepX
    val y = chartHeight - (value / yMax) * chartHeight
    Offset(x, y)
}

// 2. 绘制平滑曲线（Cubic Bézier）
val curvePath = Path()
curvePath.moveTo(points[0].x, points[0].y)
for (i in 1 until points.size) {
    val cp1x = prev.x + (curr.x - prevPrev.x) * tension
    val cp1y = prev.y + (curr.y - prevPrev.y) * tension
    val cp2x = curr.x - (next.x - prev.x) * tension
    val cp2y = curr.y - (next.y - prev.y) * tension
    curvePath.cubicTo(cp1x, cp1y, cp2x, cp2y, curr.x, curr.y)
}
```

**Cubic Bézier 原理**：
- 每个线段使用两个控制点定义曲线形状
- `tension = 0.3f` 控制曲线平滑度（0 = 直线，1 = 最弯曲）
- 控制点通过相邻点计算，确保曲线连续且平滑

### 5.3 交互检测

```kotlin
.pointerInput(Unit) {
    detectTapGestures { tapOffset ->
        var nearestIndex: Int? = null
        var minDistance = Float.MAX_VALUE
        
        dailyData.forEachIndexed { index, _ ->
            val barCenterX = index * (barWidth + gap) + gap / 2 + barWidth / 2
            val distance = abs(tapOffset.x - barCenterX)
            if (distance < minDistance && distance < barWidth) {
                minDistance = distance
                nearestIndex = index
            }
        }
        
        selectedPointIndex = nearestIndex
        tooltipPosition = tapOffset
    }
}
```

- 使用 `detectTapGestures` 监听点击事件
- 计算点击位置与每个柱子/数据点的距离
- 选择最近的元素高亮显示
- 通过 `selectedPointIndex` 和 `tooltipPosition` 状态触发重组，显示 Tooltip

## 6. 导航设计

### 6.1 Jetpack Navigation Compose

```kotlin
val navController = rememberNavController()

Scaffold(
    bottomBar = { NavigationBar { ... } }
) { innerPadding ->
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(viewModel) }
        composable("records") { RecordsScreen(viewModel) }
        composable("stats") { StatsScreen(viewModel) }
        composable("settings") { SettingsScreen(viewModel) }
    }
}
```

**设计决策**：
- 4 个 Tab 共享同一个 `MilkTeaViewModel` 实例，确保筛选状态跨 Tab 同步
- 底部导航使用 `popUpTo(startDestination) { saveState = true }` + `restoreState = true`
- 切换 Tab 时恢复之前的状态（如记录页的筛选条件）
- `launchSingleTop = true` 防止重复导航到同一页面

### 6.2 共享 ViewModel

```kotlin
val viewModel: MilkTeaViewModel = viewModel(
    viewModelStoreOwner = LocalContext.current as ComponentActivity,
)
```

- 通过 `LocalContext.current as ComponentActivity` 获取 Activity 级别的 ViewModelStore
- 所有 Screen 共享同一 ViewModel 实例
- 筛选条件（如日期范围、品牌）在首页、记录页、统计页之间保持一致

## 7. 主题与配色

### 7.1 Miuix HyperOS 设计规范

本项目使用 [Miuix](https://github.com/compose-miuix-ui/miuix) (v0.8.8) 作为 UI 组件库，遵循小米 HyperOS 设计规范。

### 7.2 当前界面优化原则

本项目是轻量级消费记录工具，界面优化目标是「少解释、好扫读、少误触」。本轮 UI 统一采用以下原则：

| 原则 | 实现方式 | 目的 |
|---|---|---|
| 页面语义明确 | 每个页面使用 `AppTopBar` 显示标题和一句说明 | 用户进入页面后立即知道当前页面能做什么 |
| 关键数据前置 | 首页和统计页使用 `MetricCard` 展示花费、杯数、均价 | 让高频信息在第一屏可快速扫读 |
| 筛选控件一致 | 日期、品牌、图表指标统一使用 `FilterPill` | 降低学习成本，选中态更明显 |
| 空状态可行动 | 空状态统一使用 `EmptyStateCard`，必要时提供操作按钮 | 避免只告诉用户「没有数据」，同时给出下一步 |
| 误触保护 | 记录页删除前使用确认弹窗 | 防止小屏幕误触造成数据丢失 |
| 触控友好 | 筛选胶囊使用至少 44dp 的最小高度 | 符合移动端触控习惯 |
| 信息分组 | 设置页输入区、品牌列表、统计图表使用卡片分组 | 用空间和容器建立层级，而不是堆叠文本 |

### 7.3 ThemeController 配置

```kotlin
@Composable
fun MilkTeaTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val controller = ThemeController(
        colorSchemeMode = if (dynamicColor) {
            ColorSchemeMode.MonetSystem  // 动态取色
        } else if (darkTheme) {
            ColorSchemeMode.Dark
        } else {
            ColorSchemeMode.Light
        }
    )
    
    MiuixTheme(
        controller = controller,
        content = content
    )
}
```

### 7.4 ColorSchemeMode 枚举

| 模式 | 说明 |
|---|---|
| `System` | 跟随系统亮/暗模式 |
| `Light` | 强制浅色主题 |
| `Dark` | 强制深色主题 |
| `MonetSystem` | 动态取色 + 跟随系统亮/暗 |
| `MonetLight` | 动态取色 + 强制浅色 |
| `MonetDark` | 动态取色 + 强制深色 |

### 7.5 Miuix 组件使用

```kotlin
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 使用 MiuixTheme 中的颜色和文字样式
Text(
    text = "标题",
    style = MiuixTheme.textStyles.title3,
    color = MiuixTheme.colorScheme.onSurface,
)
```

### 7.6 文字样式 (TextStyles)

| 样式 | 字号 | 用途 |
|---|---|---|
| `title1` | 32sp | 大标题 |
| `title2` | 24sp | 中标题 |
| `title3` | 20sp | 小标题 |
| `body1` | 16sp | 正文 |
| `body2` | 14sp | 次要正文 |
| `footnote1` | 13sp | 脚注 |
| `footnote2` | 11sp | 小脚注 |

## 8. 弹窗设计

### 8.1 AlertDialog + 自定义内容

```kotlin
AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (isEdit) "编辑记录" else "添加奶茶记录") },
    text = { Column { /* 表单内容 */ } },
    confirmButton = { TextButton(onClick = { /* 校验并确认 */ }) { Text("添加") } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
)
```

### 8.2 日期/时间选择器

- 使用 Material 3 提供的 `DatePicker` 和 `TimePicker` 组件
- 通过 `rememberDatePickerState` / `rememberTimePickerState` 管理状态
- 自定义 `DatePickerDialog` 和 `AlertDialog` 包装以统一风格

### 8.3 嵌套弹窗

添加记录弹窗内嵌套品牌管理弹窗：
1. 点击「管理」→ 显示 `ManageBrandsDialog`
2. 管理完成后点击「完成」→ 关闭管理弹窗，返回添加弹窗
3. 常用品牌列表自动刷新（通过 `commonBrands` Flow）

## 9. 性能优化

### 9.1 LazyColumn 与 key

```kotlin
LazyColumn {
    items(todayRecords.sortedByDescending { it.timestamp }, key = { it.id }) { record ->
        TodayRecordCard(record = record, onDelete = { ... })
    }
}
```

- `key = { it.id }`：帮助 Compose 识别列表项，支持高效插入/删除动画
- `LazyColumn` 只渲染可视区域，适合长列表

### 9.2 derivedStateOf（未使用但可扩展）

对于复杂计算的状态，可以使用 `derivedStateOf` 缓存结果：
```kotlin
val sortedRecords by remember(records) {
    derivedStateOf { records.sortedByDescending { it.timestamp } }
}
```

当前项目计算量较小，直接使用 `collectAsStateWithLifecycle` 即可。

### 9.3 remember 与参数

```kotlin
val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
```

- `remember` 无参数：只初始化一次，后续重组复用
- 日期格式化器等昂贵的对象应在 `remember` 中创建

## 10. 可访问性

- 所有 Icon 组件提供 `contentDescription`
- 按钮和可点击卡片语义明确
- 颜色对比度满足 WCAG 标准（Material 3 默认配色已优化）
- 支持系统字体大小调整
- 删除常用品牌的图标会带上具体品牌名，便于读屏用户理解操作对象
- 筛选胶囊不只依赖颜色表达选中态，还通过边框和字重强化状态
