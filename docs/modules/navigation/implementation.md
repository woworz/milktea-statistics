# 导航模块实现文档

本模块负责应用页面路由管理和底部导航栏实现。使用 Miuix `NavigationBar` 组件。

## 文件清单

| 文件 | 路径 | 说明 |
|---|---|---|
| `AppNavigation.kt` | `ui/navigation/AppNavigation.kt` | 底部导航栏 + NavHost 路由管理 |

---

## 1. AppNavigation.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/ui/navigation/AppNavigation.kt`

### 1.1 数据类

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `NavDestination` data class | **第 29-33 行** | 导航目标定义，包含路由、标签、图标 |

### 1.2 NavDestination 字段

| 字段 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `route` | 第 30 行 | `String` | 路由标识符（如 `"home"`） |
| `label` | 第 31 行 | `String` | 导航标签文本（如 `"首页"`） |
| `icon` | 第 32 行 | `ImageVector` | 导航图标（Material Icons） |

### 1.3 导航目标列表

| 变量 | 行号 | 说明 |
|---|---|---|
| `destinations` | **第 35-40 行** | 4 个 Tab 的导航目标定义 |

**destinations 内容**：

| 索引 | 路由 | 标签 | 图标 |
|---|---|---|---|
| 0 | `"home"` | `"首页"` | `Icons.Default.Home` |
| 1 | `"records"` | `"记录"` | `Icons.AutoMirrored.Filled.List` |
| 2 | `"stats"` | `"统计"` | `Icons.Default.DateRange` |
| 3 | `"settings"` | `"设置"` | `Icons.Default.Settings` |

### 1.4 Composable 函数

| 函数 | 行号 | 说明 |
|---|---|---|
| `AppNavigation()` | **第 42-78 行** | 应用主导航组件，包含 miuix NavigationBar 和 NavHost |

### 1.5 AppNavigation 内部逻辑

| 逻辑 | 行号 | 说明 |
|---|---|---|
| `rememberNavController()` | 第 43 行 | 创建并记忆 NavController |
| `viewModel()` 获取 | 第 44-46 行 | 从 Activity 获取共享 ViewModel |
| `Scaffold` + `NavigationBar` | 第 48-66 行 | 使用 miuix Scaffold 和 NavigationBar |
| `currentBackStackEntryAsState()` | 第 50 行 | 获取当前路由状态 |
| `NavigationBarItem` 循环 | 第 52-63 行 | 遍历 destinations 生成导航项 |
| `navigate()` 配置 | 第 55-59 行 | 导航配置：`popUpTo` + `saveState` + `restoreState` |
| `NavHost` + `composable` | 第 68-75 行 | 4 个页面的路由注册 |

### 1.6 Miuix 组件使用

```kotlin
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem

Scaffold(
    bottomBar = {
        NavigationBar {
            destinations.forEach { dest ->
                NavigationBarItem(
                    selected = currentRoute == dest.route,
                    onClick = { /* 导航 */ },
                    icon = dest.icon,  // ImageVector
                    label = dest.label  // String
                )
            }
        }
    }
) { innerPadding ->
    NavHost(...)
}
```

---

## 索引总表

| 函数/类 | 所在文件 | 行号 | 说明 |
|---|---|---|---|
| `AppNavigation()` | `AppNavigation.kt` | 42-78 | 应用导航主组件 |
| `destinations` | `AppNavigation.kt` | 35-40 | 导航目标列表 |
| `NavDestination` data class | `AppNavigation.kt` | 29-33 | 导航目标数据类 |
