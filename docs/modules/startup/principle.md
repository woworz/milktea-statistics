# 启动模块原理

## 解决的问题

`MilkTeaViewModel` 创建时需要数据库实例：

```kotlin
private val db = MilkTeaDatabase.getDatabase(application)
```

如果数据库第一次在这里构建，首页初始化可能同时承担 Room 构建成本。现在通过 App Startup 把这一步提前到应用进程启动阶段，ViewModel 中再次调用时通常只是拿到已经存在的单例。

## App Startup 如何工作

App Startup 通过一个 `ContentProvider` 自动运行初始化器。Android 会在 `Application.onCreate()` 之前创建 Provider，因此 `DatabaseInitializer.create()` 会早于 `MainActivity.onCreate()` 执行。

初始化器返回值会由 App Startup 管理；本项目实际使用的数据库实例仍以 `MilkTeaDatabase` 自己的单例为准，两者方向一致。

## 为什么适合当前项目

- 初始化目标单一：只有 Room 数据库。
- 不需要引入 Hilt/Koin。
- 代码侵入小，Manifest metadata 清晰可见。

## 边界

- App Startup 不是后台线程调度框架，`create()` 仍应保持轻量。
- 不应把用户无关或可延迟的工作都塞进启动阶段。
- 如果未来初始化项变重，应考虑懒加载、后台预热或依赖注入框架。
