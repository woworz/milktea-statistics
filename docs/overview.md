# 项目总览

## 应用定位

奶茶统计是一个本地优先的个人消费记录工具。数据保存在设备端 Room 数据库中，主要面向高频、低成本的记录与回看：

1. 记录品牌、饮品名、价格和饮用时间。
2. 按日期范围和品牌查看历史记录。
3. 汇总总花费、杯数、均价和消费洞察。
4. 用每日趋势图观察消费变化。
5. 通过常用品牌和历史复购模板减少重复输入。

## 模块划分

| 模块 | 关键文件 | 职责 |
|---|---|---|
| 入口 | `MainActivity.kt`、`MilkTeaApplication.kt` | 设置主题、启动 Compose、注册 Application |
| Startup | `startup/DatabaseInitializer.kt` | 使用 App Startup 提前构建 Room 数据库 |
| Data | `data/*` | Room 实体、DAO、数据库和 Repository |
| Model | `model/DateRange.kt` | 跨 UI 和服务层共享的日期范围枚举 |
| Service | `service/*` | 按记录、品牌、分析拆分业务逻辑 |
| UI | `ui/*` | 页面、组件、弹窗、图表和主题 |
| Navigation | `ui/navigation/AppNavigation.kt` | 四个底部 Tab 和路由 |

## 数据流

```text
用户操作
  ↓
Compose 页面回调
  ↓
MilkTeaViewModel 更新筛选/编辑状态，或调用 Service
  ↓
Service 调用 Repository
  ↓
Repository 调用 Room DAO
  ↓
SQLite 数据变化
  ↓
Room Flow 重新发射
  ↓
ViewModel StateFlow 更新
  ↓
Compose 重组
```

项目没有引入依赖注入框架。`MilkTeaViewModel` 手动创建数据库、Repository 和三个 Service，优点是简单直观；如果后续功能明显增多，再考虑引入 Hilt/Koin 或轻量 Provider。

## 核心状态

| 状态 | 来源 | 用途 |
|---|---|---|
| `selectedDateRange` | ViewModel | 记录页和统计页共享日期筛选 |
| `selectedBrand` | ViewModel | 记录页和统计页共享品牌筛选 |
| `filteredRecords` | AnalyticsService | 记录页列表 |
| `stats` | AnalyticsService | 统计页指标卡 |
| `dailyAggregates` | AnalyticsService | 趋势图 |
| `insights` | AnalyticsService | 统计页消费洞察 |
| `todayCount`、`todayRecords` | RecordService | 首页今日数据 |
| `allBrands`、`commonBrands` | BrandService | 筛选项和快捷品牌 |
| `purchaseTemplates` | ViewModel | 添加弹窗的历史复购模板 |
| `editingRecord` | ViewModel | 添加/编辑弹窗 |

`todayCount` 和 `todayRecords` 由 `todayRange` 驱动，跨过午夜后会重新计算今日起止时间，避免应用常驻后台后“今日”仍停留在旧日期。

## 数据库

当前数据库版本为 `3`，包含两张表：

| 表 | 实体 | 说明 |
|---|---|---|
| `milk_tea_records` | `MilkTeaRecord` | 饮品消费记录 |
| `common_brands` | `CommonBrand` | 常用品牌，`name` 唯一 |

迁移历史：

- `1 -> 2`：给记录表增加可选字段 `drinkName`。
- `2 -> 3`：新增常用品牌表和品牌名唯一索引。

趋势聚合通过 SQLite 按设备本地自然日分组，避免 UTC 日期导致凌晨记录被归到前一天。消费洞察和复购模板不落库，均由当前记录列表在内存中派生。

## UI 结构

| Tab | 路由 | 页面 | 主要能力 |
|---|---|---|---|
| 首页 | `home` | `HomeScreen` | 今日概览、今日记录、快速添加 |
| 记录 | `records` | `RecordsScreen` | 历史列表、筛选、搜索、编辑、删除 |
| 统计 | `stats` | `StatsScreen` | 指标卡、消费洞察、柱状图、折线图 |
| 设置 | `settings` | `SettingsScreen` | 常用品牌管理 |

四个页面共享 Activity 级别的 `MilkTeaViewModel`，所以记录页和统计页的筛选条件保持一致，新增/删除记录后各页也会自动刷新。

## 维护约定

- 文档描述职责和关键行为，不再维护逐函数行号，避免代码移动后文档失真。
- 新增数据库字段时必须同步更新实体、DAO 查询映射、数据库版本和 Migration。
- 新增筛选维度优先放到 `AnalyticsService`，再由 ViewModel 转成 `StateFlow` 给 UI。
- 新增洞察指标优先扩展 `ConsumptionInsights` 和 `AnalyticsService.buildConsumptionInsights()`。
- 新增复购模板规则优先调整 `MilkTeaViewModel.toPurchaseTemplates()`，除非模板需要持久化。
- 新增页面优先在 `AppNavigation.kt` 中补路由，并明确是否共享现有 ViewModel。
