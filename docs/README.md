# 奶茶统计

一款用于记录奶茶/饮品消费的 Android 应用。应用聚焦快速记录、历史检索、消费统计和复购效率。

## 功能

- 首页：今日花费、今日杯数、今日记录和快速添加入口。
- 记录：按本周/本月/上月、品牌和关键词筛选，支持编辑与删除记录。
- 统计：查看总花费、杯数、均价、消费洞察，并在柱状图/折线图间切换每日趋势。
- 添加记录：支持常用品牌、历史复购模板、紧凑日历和时间选择。
- 设置：维护常用品牌，添加记录时可快速选择。

## 技术栈

| 层级 | 技术 |
|---|---|
| UI | Jetpack Compose、Material 3 |
| 架构 | MVVM、ViewModel、Service、Repository |
| 数据 | Room、SQLite、Flow |
| 导航 | Navigation Compose、底部 Tab |
| 启动 | Jetpack App Startup |
| 构建 | Gradle Kotlin DSL、KSP |

## 项目结构

```text
app/src/main/java/com/mason/milkteastatistics/
├── MainActivity.kt
├── MilkTeaApplication.kt
├── data/                 # Entity、DAO、Room Database、Repository
├── model/                # 跨层共享模型，如 DateRange
├── service/              # 记录、品牌、统计分析服务
├── startup/              # App Startup 初始化器
└── ui/                   # Compose 页面、导航、主题、通用组件
```

## 架构概览

```text
Compose UI
    ↓ 用户操作 / ↑ StateFlow
MilkTeaViewModel
    ↓ 调度
RecordService / BrandService / AnalyticsService
    ↓
MilkTeaRepository
    ↓
Room DAO
    ↓
SQLite
```

ViewModel 只保留 UI 状态和调度逻辑；业务能力按领域放在 Service；Repository 封装 DAO；Room 查询返回 `Flow`，数据库变更后 UI 自动更新。

## 快速开始

1. 使用 Android Studio 打开项目。
2. 同步 Gradle。
3. 连接设备或启动模拟器，要求 `minSdk 35`。
4. 运行 `app`。

## 文档索引

| 文档 | 内容 |
|---|---|
| [项目总览](overview.md) | 应用定位、模块边界、数据流和关键设计 |
| [启动模块](modules/startup/implementation.md) | Application、App Startup 和数据库预初始化 |
| [启动原理](modules/startup/principle.md) | 为什么使用 App Startup，以及维护注意事项 |
| [数据层](modules/data/implementation.md) | Entity、DAO、Database、Repository |
| [数据层原理](modules/data/principle.md) | Room、Flow、迁移和聚合查询约定 |
| [服务层](modules/services/implementation.md) | RecordService、BrandService、AnalyticsService |
| [服务层原理](modules/services/principle.md) | 服务层职责边界和 ViewModel 协作方式 |
| [UI 层](modules/ui/implementation.md) | 页面、组件、弹窗、主题和图表 |
| [UI 层原理](modules/ui/principle.md) | Compose 状态、筛选联动、图表交互 |
| [导航模块](modules/navigation/implementation.md) | 底部 Tab 和路由表 |
| [导航原理](modules/navigation/principle.md) | 共享 ViewModel 和 Tab 状态恢复 |
