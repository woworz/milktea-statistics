# 导航模块实现

导航模块位于 `ui/navigation/AppNavigation.kt`，负责四个底部 Tab 和对应路由。

## 路由表

| 路由 | 标签 | 页面 | 图标 |
|---|---|---|---|
| `home` | 首页 | `HomeScreen` | `Icons.Default.Home` |
| `records` | 记录 | `RecordsScreen` | `Icons.AutoMirrored.Filled.List` |
| `stats` | 统计 | `StatsScreen` | `Icons.Default.DateRange` |
| `settings` | 设置 | `SettingsScreen` | `Icons.Default.Settings` |

这些目标由 `NavDestination(route, label, icon)` 描述，并集中放在 `destinations` 列表中。

## AppNavigation

`AppNavigation()` 做三件事：

1. 创建 `NavController`。
2. 以 Activity 为 `ViewModelStoreOwner` 获取共享的 `MilkTeaViewModel`。
3. 使用 Material 3 `Scaffold` + `NavigationBar` 承载底部导航，并用 `NavHost` 注册页面。
4. 关闭 Tab 间默认进入/退出动画，让底部导航切换更直接。

导航配置：

```kotlin
navController.navigate(dest.route) {
    popUpTo(navController.graph.startDestinationId) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```

这样点击 Tab 时不会重复创建栈顶页面，并尽量恢复之前的页面状态。

`NavigationBarItem` 使用 Material 3 的 `icon` 和 `label` slot：

```kotlin
NavigationBarItem(
    selected = currentRoute == dest.route,
    icon = { Icon(dest.icon, contentDescription = dest.label) },
    label = { Text(dest.label) },
    onClick = { ... },
)
```

`NavHost` 的 `enterTransition`、`exitTransition`、`popEnterTransition` 和 `popExitTransition` 都设为 `None`，避免 Tab 切换出现页面滑动感。

## 新增 Tab

新增底部 Tab 时需要同步：

1. 在 `destinations` 中添加 `NavDestination`。
2. 在 `NavHost` 中添加对应 `composable(route)`。
3. 判断新页面是否继续共享 `MilkTeaViewModel`，或需要独立 ViewModel。
