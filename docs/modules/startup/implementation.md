# 启动模块实现

启动模块使用 Jetpack App Startup 在应用启动阶段预初始化 Room 数据库，避免首次进入首页时才构建数据库。

## 文件

| 文件 | 职责 |
|---|---|
| `MilkTeaApplication.kt` | 自定义 Application，注册在 Manifest 中 |
| `startup/DatabaseInitializer.kt` | App Startup 初始化器，触发 `MilkTeaDatabase.getDatabase(context)` |
| `AndroidManifest.xml` | 注册 Application 和 `InitializationProvider` metadata |

## 初始化流程

```text
应用进程启动
  ↓
App Startup 的 InitializationProvider 创建
  ↓
读取 Manifest 中的 DatabaseInitializer metadata
  ↓
DatabaseInitializer.create(context)
  ↓
MilkTeaDatabase.getDatabase(context)
  ↓
Room 数据库单例创建
  ↓
MainActivity.onCreate()
```

`DatabaseInitializer.dependencies()` 返回空列表，表示没有前置初始化依赖。

## Manifest 配置

`application` 使用自定义 Application：

```xml
<application
    android:name=".MilkTeaApplication"
    ...>
```

`InitializationProvider` 通过 metadata 指向数据库初始化器：

```xml
<meta-data
    android:name="com.mason.milkteastatistics.startup.DatabaseInitializer"
    android:value="androidx.startup" />
```

## 维护注意

- 如果新增初始化器，按依赖关系填写 `dependencies()`。
- 不要在初始化器里做网络请求或长时间阻塞操作。
- 数据库预初始化只解决首次获取数据库实例的同步构建成本，具体查询仍由 Room/Flow 在后续执行。
