# 导航模块原理文档

## 1. 架构定位

导航模块是应用的**路由中枢**，负责管理 4 个 Tab 页面之间的切换，确保状态同步和导航体验流畅。

```
┌─────────────────────────────────────────────────────────────┐
│                        MainActivity                          │
│                         MilkTeaTheme                         │
│                              │                             │
│                         AppNavigation()                      │
│                              │                             │
│        ┌─────────────────────┼─────────────────────┐       │
│        │                     │                     │       │
│   ┌────┴────┐          ┌────┴────┐          ┌────┴────┐   │
│   │ Scaffold│          │ NavHost │          │ NavController│
│   │(Bottom  │          │ (路由表) │          │ (状态管理)  │
│   │NavBar)  │          │         │          │             │
│   └─────────┘          └────┬────┘          └─────────────┘
│                               │                             │
│        ┌────────┬────────┬───┴───┬────────┐               │
│        ↓        ↓        ↓       ↓        ↓                │
│    HomeScreen RecordsScreen StatsScreen SettingsScreen     │
│    (viewModel) (viewModel) (viewModel) (viewModel)          │
│         │         │         │         │                     │
│         └─────────┴─────────┴─────────┘                    │
│                    (同一 ViewModel 实例)                      │
└─────────────────────────────────────────────────────────────┘
```

## 2. Jetpack Navigation Compose 原理

### 2.1 核心组件

| 组件 | 职责 | 本项目对应 |
|---|---|---|
| `NavController` | 管理导航状态、处理导航指令 | `rememberNavController()` |
| `NavHost` | 承载不同路由对应的 Composable | `NavHost(navController, startDestination)` |
| `NavGraph` | 定义路由到 Composable 的映射 | `composable("home") { ... }` |
| `NavBackStackEntry` | 导航栈中的条目，保存状态 | `currentBackStackEntryAsState()` |

### 2.2 导航状态管理

```kotlin
val navController = rememberNavController()
val navBackStackEntry by navController.currentBackStackEntryAsState()
val currentRoute = navBackStackEntry?.destination?.route
```

- `rememberNavController()` 创建 NavController 并在重组间保持
- `currentBackStackEntryAsState()` 将导航状态转为 Compose 可观察的状态
- `currentRoute` 用于高亮底部导航栏当前选中的 Tab

### 2.3 导航指令

```kotlin
navController.navigate(dest.route) {
    popUpTo(navController.graph.startDestinationId) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```

**参数解析**：

| 参数 | 作用 |
|---|---|
| `popUpTo(startDestinationId) { saveState = true }` | 导航前弹出到起始页，但保存中间页面的状态 |
| `launchSingleTop = true` | 如果目标页面已在栈顶，不重复创建 |
| `restoreState = true` | 恢复之前保存的页面状态 |

**效果**：
- 用户在「记录」Tab 设置了筛选条件，切换到「统计」Tab，再切回「记录」Tab
- 「记录」Tab 的筛选条件仍然保留（因为 `saveState` + `restoreState`）

## 3. 共享 ViewModel 设计

### 3.1 为什么共享 ViewModel

4 个 Tab 共享同一个 `MilkTeaViewModel`：

```kotlin
val viewModel: MilkTeaViewModel = viewModel(
    viewModelStoreOwner = LocalContext.current as ComponentActivity,
)
```

**优势**：
- 筛选条件（日期范围、品牌）跨 Tab 同步
- 无需通过导航参数传递状态
- 减少重复数据查询

**对比：非共享方案**
```kotlin
// 如果每个 Screen 独立创建 ViewModel：
composable("records") { RecordsScreen(viewModel()) }
composable("stats") { StatsScreen(viewModel()) }
// 问题：切换 Tab 后筛选条件重置，用户体验差
```

### 3.2 ViewModelStoreOwner

```kotlin
val viewModelStoreOwner = LocalContext.current as ComponentActivity
```

- `viewModel()` 默认在当前 `NavBackStackEntry` 创建 ViewModel
- 显式指定 `ViewModelStoreOwner` 为 `ComponentActivity`，确保 Activity 级别单例
- 即使 NavHost 内的页面切换，ViewModel 不会被销毁

### 3.3 状态同步场景

**场景 1：筛选条件同步**
1. 用户在「记录」页选择「本月」+「喜茶"
2. 切换到「统计」页，自动显示「本月」「喜茶」的统计数据和图表

**场景 2：添加记录同步**
1. 用户在「首页」添加一杯奶茶
2. 切换到「记录」页，新记录自动出现在列表中
3. 切换到「统计」页，统计数据和图表自动更新

## 4. 底部导航栏设计

### 4.1 Material 3 NavigationBar

```kotlin
NavigationBar {
    destinations.forEach { dest ->
        NavigationBarItem(
            icon = { Icon(dest.icon, contentDescription = dest.label) },
            label = { Text(dest.label) },
            selected = currentRoute == dest.route,
            onClick = { navController.navigate(...) },
        )
    }
}
```

### 4.2 导航栏状态

- `selected = currentRoute == dest.route`：根据当前路由高亮对应 Tab
- 点击已选中的 Tab：由于 `launchSingleTop = true`，不会重复导航
- 4 个 Tab 独立维护导航状态（BackStack）

### 4.3 图标选择

| Tab | 图标 | 说明 |
|---|---|---|
| 首页 | `Icons.Default.Home` | 主页图标 |
| 记录 | `Icons.AutoMirrored.Filled.List` | 列表图标（支持 RTL） |
| 统计 | `Icons.Default.DateRange` | 日历/日期范围图标 |
| 设置 | `Icons.Default.Settings` | 齿轮图标 |

使用 `Icons.AutoMirrored` 的图标在 RTL（从右到左）布局下会自动镜像翻转。

## 5. 页面路由设计

### 5.1 路由表

```kotlin
NavHost(navController = navController, startDestination = "home") {
    composable("home") { HomeScreen(viewModel = viewModel) }
    composable("records") { RecordsScreen(viewModel = viewModel) }
    composable("stats") { StatsScreen(viewModel = viewModel) }
    composable("settings") { SettingsScreen(viewModel = viewModel) }
}
```

| 路由 | 页面 | 功能 |
|---|---|---|
| `"home"` | `HomeScreen` | 今日概览、最近记录、快速添加 |
| `"records"` | `RecordsScreen` | 历史记录、筛选、编辑删除 |
| `"stats"` | `StatsScreen` | 统计卡片、趋势图表 |
| `"settings"` | `SettingsScreen` | 常用品牌管理 |

### 5.2 路由参数（当前未使用）

如需传递参数，可扩展为：
```kotlin
composable(
    "records?brand={brand}",
    arguments = listOf(navArgument("brand") { defaultValue = null })
) { backStackEntry ->
    val brand = backStackEntry.arguments?.getString("brand")
    RecordsScreen(viewModel = viewModel, initialBrand = brand)
}
```

当前通过共享 ViewModel 传递状态，避免了路由参数的复杂性。

## 6. Scaffold 布局

```kotlin
Scaffold(
    bottomBar = { NavigationBar { ... } },
) { innerPadding ->
    NavHost(
        modifier = Modifier.padding(innerPadding),
        ...
    )
}
```

- `Scaffold` 是 Material 3 提供的基础布局组件
- `bottomBar` 指定底部导航栏
- `innerPadding` 是 Scaffold 自动计算的 content padding，避免内容被底部导航栏遮挡
- `NavHost` 应用 `innerPadding` 确保内容在安全区域内显示

## 7. 导航最佳实践

### 7.1 深链接（Deep Link）扩展

如需支持外部跳转，可添加深链接：
```kotlin
composable(
    "records",
    deepLinks = listOf(navDeepLink { uriPattern = "milktea://records" })
) { RecordsScreen(viewModel) }
```

### 7.2 返回行为

当前 4 Tab 架构下：
- 按系统返回键：退出应用（因为导航栈已弹出到起始页）
- 如需实现「双击返回键退出」，可在 MainActivity 中处理 `onBackPressed`

### 7.3 配置变更处理

- `NavController` 通过 `rememberNavController` 在配置变更（如旋转屏幕）后保持状态
- `ViewModel` 通过 `AndroidViewModel` 在配置变更后存活
- 用户当前 Tab 和页面状态在旋转后完全恢复
