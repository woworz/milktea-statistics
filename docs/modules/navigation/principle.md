# 导航模块原理

## 四 Tab 架构

应用只有一层主导航：

```text
MainActivity
  ↓
MilkTeaTheme
  ↓
AppNavigation
  ├─ HomeScreen
  ├─ RecordsScreen
  ├─ StatsScreen
  └─ SettingsScreen
```

底部导航固定展示四个页面，当前没有二级页面和路由参数。

## 共享 ViewModel

`AppNavigation` 通过 Activity 级别的 `ViewModelStoreOwner` 获取 `MilkTeaViewModel`：

```kotlin
val viewModel: MilkTeaViewModel = viewModel(
    viewModelStoreOwner = LocalContext.current as ComponentActivity,
)
```

因此四个 Tab 使用同一个 ViewModel 实例。这样做的好处：

- 记录页和统计页共享日期/品牌筛选。
- 首页新增记录后，记录页和统计页自动更新。
- 常用品牌修改后，添加记录弹窗能立即看到变化。

如果未来某个页面有完全独立的状态，可以单独创建自己的 ViewModel，但要避免把共享筛选状态拆散。

## Tab 状态恢复

`saveState`、`restoreState` 和 `launchSingleTop` 共同保证：

- 点击当前 Tab 不重复创建页面。
- 切换 Tab 后尽量恢复之前的页面状态。
- 返回栈不会因为反复点击底部导航无限增长。

当前分支还显式关闭了 `NavHost` 转场动画。底部 Tab 是同级页面，直接切换比前进/后退动画更符合用户预期，也减少图表页和列表页切换时的视觉抖动。

## 何时使用路由参数

当前项目通过共享 ViewModel 传递筛选状态，不使用路由参数。只有当页面需要被外部深链接打开，或必须表达“某条记录详情”这类独立地址时，再考虑增加参数化路由。
