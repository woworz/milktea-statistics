# 启动模块实现文档

本模块负责通过 Jetpack App Startup 库在应用启动阶段预初始化 Room 数据库，消除冷启动时首页卡顿。

## 文件清单

| 文件 | 路径 | 说明 |
|---|---|---|
| `MilkTeaApplication.kt` | `MilkTeaApplication.kt` | 自定义 Application 类 |
| `DatabaseInitializer.kt` | `startup/DatabaseInitializer.kt` | App Startup 数据库初始化器 |

---

## 1. MilkTeaApplication.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/MilkTeaApplication.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `MilkTeaApplication` class | **第 12 行** | 自定义 Application 类，注册 App Startup 框架 |

### 1.1 作用

- 作为 `android:name` 注册在 `AndroidManifest.xml` 中
- Jetpack App Startup 库通过 ContentProvider 机制，在 `MilkTeaApplication.onCreate()` 之前自动调度 `DatabaseInitializer`
- 无需手动调用初始化代码

---

## 2. DatabaseInitializer.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/startup/DatabaseInitializer.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `DatabaseInitializer` class | **第 10-23 行** | 实现 `Initializer<MilkTeaDatabase>` 接口 |

### 2.1 方法

| 函数 | 行号 | 返回类型 | 说明 |
|---|---|---|---|
| `create(context)` | 第 13-15 行 | `MilkTeaDatabase` | 调用 `MilkTeaDatabase.getDatabase(context)` 触发数据库构建 |
| `dependencies()` | 第 17-20 行 | `List<Class<out Initializer<*>>>` | 返回空列表，表示无依赖，最早执行 |

### 2.2 初始化流程

```
Application 启动
    ↓
ContentProvider 初始化 (App Startup 自动注册)
    ↓
DatabaseInitializer.create(context)
    ↓
MilkTeaDatabase.getDatabase(context) → Room.databaseBuilder().build()
    ↓
数据库单例就绪
    ↓
MainActivity.onCreate()
    ↓
MilkTeaViewModel 构造时 getDatabase() 返回已就绪实例（无阻塞）
```

---

## 3. AndroidManifest.xml 配置

**路径**: `app/src/main/AndroidManifest.xml`

| 配置 | 行号 | 说明 |
|---|---|---|
| `android:name=".MilkTeaApplication"` | 第 6 行 | 注册自定义 Application 类 |

---

## 索引总表

| 函数/类 | 所在文件 | 行号 | 说明 |
|---|---|---|---|
| `MilkTeaApplication` | `MilkTeaApplication.kt` | 10 | 自定义 Application 类 |
| `DatabaseInitializer` | `DatabaseInitializer.kt` | 10-23 | App Startup 数据库初始化器 |
| `create(context)` | `DatabaseInitializer.kt` | 13-15 | 初始化数据库 |
| `dependencies()` | `DatabaseInitializer.kt` | 17-20 | 返回空依赖列表 |
