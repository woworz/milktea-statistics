# 奶茶统计 (MilkTea Statistics)

一款用于记录和统计奶茶/饮品消费习惯的 Android 应用。支持记录每次饮品的品牌、名称、价格和时间，提供消费统计和趋势图表分析。

## 功能特性

- **首页**：展示今日消费概览（总花费、杯数），最近记录快捷预览，今日记录列表
- **记录**：查看历史记录，支持按日期范围（本周/本月/上月）和品牌筛选，可编辑/删除记录
- **统计**：消费数据统计卡片（总花费、杯数、均价），柱状图/折线图趋势展示
- **设置**：常用品牌快捷标签管理，支持自定义添加和删除
- **添加记录弹窗**：支持品牌快捷选择、饮品名称、价格输入，可自定义饮用日期和时间
- **温暖治愈主题**：奶茶棕 + 抹茶绿配色，支持动态取色和深色模式

## 技术栈

| 层级 | 技术 |
|---|---|
| UI | Jetpack Compose + Material Design 3 |
| 架构 | MVVM (ViewModel + Repository + DAO) |
| 数据库 | Room (SQLite) + Flow 响应式查询 |
| 图表 | 自定义 Canvas 绘制（柱状图 + 平滑折线图） |
| 导航 | Jetpack Navigation Compose |
| 构建 | Gradle + Kotlin DSL + KSP |

## 项目结构

```
app/src/main/java/com/mason/milkteastatistics/
├── MainActivity.kt                 # 应用入口
├── data/                           # 数据层
│   ├── MilkTeaRecord.kt            # 数据实体类
│   ├── CommonBrand.kt              # 常用品牌实体
│   ├── MilkTeaDao.kt               # Room DAO 接口
│   ├── CommonBrandDao.kt           # 常用品牌 DAO
│   ├── MilkTeaDatabase.kt          # Room 数据库
│   └── MilkTeaRepository.kt        # 数据仓库
└── ui/                             # UI 层
    ├── MilkTeaViewModel.kt         # 业务逻辑与状态管理
    ├── HomeScreen.kt               # 首页
    ├── RecordsScreen.kt            # 记录列表页
    ├── StatsScreen.kt              # 统计图表页
    ├── SettingsScreen.kt           # 设置页
    ├── TrendChart.kt               # 自定义图表组件
    ├── navigation/
    │   └── AppNavigation.kt        # 底部导航栏 + NavHost
    ├── components/
    │   └── Dialogs.kt              # 添加/编辑弹窗、品牌管理弹窗、日期时间选择器
    └── theme/
        └── Theme.kt                # 主题配色（温暖治愈风格）
```

## 快速开始

1. 使用 Android Studio 打开项目
2. 同步 Gradle (`Sync Project with Gradle Files`)
3. 连接设备或启动模拟器（minSdk 35）
4. 点击 `Run` 运行应用

## 架构概览

应用采用 **MVVM 架构**，数据单向流动：

```
UI (Compose Screen)
    ↑
ViewModel (StateFlow)
    ↑
Repository
    ↑
DAO (Room) → SQLite Database
```

- **数据层**：Room 数据库提供本地持久化，Repository 封装数据操作，对外暴露 `Flow`
- **UI 层**：Compose 声明式 UI，`collectAsStateWithLifecycle` 订阅 ViewModel 状态
- **状态管理**：`StateFlow` + `combine`/`flatMapLatest` 实现筛选条件的响应式联动

## 文档索引

| 文档 | 说明 |
|---|---|
| [总览文档](overview.md) | 项目架构总览、模块划分、数据流说明 |
| [数据层实现文档](modules/data/implementation.md) | 数据层文件、类、函数清单（含行号） |
| [数据层原理文档](modules/data/principle.md) | Room 数据库设计、DAO 模式、Repository 模式原理 |
| [UI 层实现文档](modules/ui/implementation.md) | UI 层文件、类、函数清单（含行号） |
| [UI 层原理文档](modules/ui/principle.md) | Compose UI 架构、MVVM 状态管理、图表绘制原理 |
| [导航模块实现文档](modules/navigation/implementation.md) | 导航模块文件、函数清单（含行号） |
| [导航模块原理文档](modules/navigation/principle.md) | Jetpack Navigation 设计原理 |
