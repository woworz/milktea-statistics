# 项目总览

## 1. 应用定位

**奶茶统计** 是一款面向个人用户的饮品消费记录工具。核心目标是帮助用户：

1. 快速记录每次奶茶/饮品消费（品牌、饮品名、价格、时间）
2. 按日期范围和品牌维度查看历史记录
3. 通过统计卡片和趋势图表直观了解消费习惯和趋势
4. 通过常用品牌快捷标签提升记录效率

## 2. 模块划分

应用按职责划分为 6 个模块：

```
┌─────────────────────────────────────────────────────────────┐
│                        UI 层                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │ 首页     │  │ 记录页   │  │ 统计页   │  │ 设置页   │     │
│  │HomeScreen│  │Records   │  │Stats     │  │Settings  │     │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘     │
│       │             │             │             │            │
│  ┌────┴─────────────┴─────────────┴─────────────┴─────┐     │
│  │              MilkTeaViewModel                       │     │
│  │  (状态管理 + 业务逻辑调度 + 数据转换)                 │     │
│  └──────────────────────┬──────────────────────────────┘     │
│                         │                                  │
├─────────────────────────┼──────────────────────────────────┤
│                         ↓                                  │
│  ┌────────────────────────────────────────────────────┐   │
│  │              Navigation 模块                        │   │
│  │  AppNavigation.kt (底部导航 + 路由管理)             │   │
│  └────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                      业务服务层                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │RecordService │  │ BrandService │  │Analytics     │     │
│  │记录操作      │  │ 品牌管理     │  │Service       │     │
│  │              │  │              │  │统计/趋势     │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
│         │                │                  │              │
├─────────┼────────────────┼──────────────────┼──────────────┤
│         │                │                  │              │
│  ┌──────┴────────────────┴──────────────────┴──────┐       │
│  │              Repository / DAO / Database        │       │
│  │              数据层                              │       │
│  └─────────────────────────────────────────────────┘       │
├─────────────────────────────────────────────────────────────┤
│                      启动模块                                │
│  ┌────────────────────────────────────────────────────┐   │
│  │  DatabaseInitializer (App Startup)                 │   │
│  │  应用启动阶段预初始化 Room 数据库，消除冷启动卡顿    │   │
│  └────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 2.1 启动模块 (Startup)

职责：通过 Jetpack App Startup 在 Application 创建阶段预初始化耗时组件，消除冷启动卡顿。

- **`MilkTeaApplication`**：自定义 Application 类，注册 App Startup 框架
- **`DatabaseInitializer`**：实现 `Initializer<MilkTeaDatabase>`，在 ContentProvider 阶段后台构建 Room 数据库
- **效果**：数据库初始化从「首次进入首页时同步阻塞」提前到「应用启动时后台完成"

### 2.2 数据层 (Data Layer)

职责：数据持久化、查询、对外提供统一数据接口。

- **实体类**：`MilkTeaRecord`、`CommonBrand`、`DailyStats`、`DailySummary`
- **DAO 接口**：`MilkTeaDao`、`CommonBrandDao`，定义 SQL 查询
- **数据库**：`MilkTeaDatabase`，Room 数据库单例
- **Repository**：`MilkTeaRepository`，封装 DAO 调用，对外屏蔽底层实现

### 2.3 业务服务层 (Service Layer)

职责：按领域拆分业务逻辑，每个服务专注单一职责，方便后续独立扩展。

- **`RecordService`**：记录增删改查 + 今日统计
- **`BrandService`**：品牌列表 + 常用品牌管理
- **`AnalyticsService`**：日期范围计算 + 筛选查询 + 统计/趋势数据

### 2.4 UI 层 (UI Layer)

职责：界面渲染、用户交互、状态订阅。

- **UI 框架**：[Miuix](https://github.com/compose-miuix-ui/miuix) (v0.8.8) — 小米 HyperOS 设计规范的 Compose 组件库
- **Screen 组件**：`HomeScreen`、`RecordsScreen`、`StatsScreen`、`SettingsScreen`
- **状态管理**：`MilkTeaViewModel`，持有 `StateFlow` 状态，调度 Service 执行业务逻辑
- **自定义组件**：`TrendChart`（柱状图）、`TrendLineChart`（折线图）、`AddEditRecordDialog` 等
- **主题**：`MilkTeaTheme`，基于 `MiuixTheme` + `ThemeController`，支持 Monet 动态取色

### 2.5 导航模块 (Navigation)

职责：页面路由管理和底部导航栏。

- `AppNavigation.kt`：定义 4 个 Tab 路由，使用 miuix `NavigationBar` 组件管理 `NavController` 和底部导航

### 2.6 应用入口 (Application Entry)

- `MainActivity.kt`：设置 Edge-to-Edge 显示，加载 Compose 内容

## 3. 数据流设计

应用采用 **单向数据流 (Unidirectional Data Flow)**：

```
用户操作 (UI Event)
    ↓
ViewModel 更新 StateFlow / 调用 Service 方法
    ↓
Service 调用 Repository → DAO → Room 数据库
    ↓
数据库变更触发 Flow 重新发射
    ↓
ViewModel 中 StateFlow 更新
    ↓
UI 自动重组 (Recomposition)
```

### 3.1 服务层状态管理

ViewModel 持有 UI 状态（筛选条件、编辑状态），通过 Service 获取业务数据：

```
ViewModel
├── _selectedBrand (MutableStateFlow<String?>)
├── _selectedDateRange (MutableStateFlow<DateRange>)
├── _editingRecord (MutableStateFlow<MilkTeaRecord?>)
│
├── RecordService ──→ Repository → DAO → Database
├── BrandService ──→ Repository → DAO → Database
└── AnalyticsService ──→ Repository → DAO → Database
```

**设计原则**：
- Service 无状态，只提供返回 `Flow` 的查询方法
- ViewModel 通过 `stateIn` 将 `Flow` 转换为 `StateFlow`，管理生命周期
- 筛选状态变化时，`combine` + `flatMapLatest` 自动重新查询

### 3.2 状态列表

ViewModel 中所有可观察状态均使用 `StateFlow`：

| 状态 | 类型 | 说明 |
|---|---|---|
| `todayCount` | `StateFlow<Int>` | 今日杯数 |
| `todayRecords` | `StateFlow<List<MilkTeaRecord>>` | 今日记录列表 |
| `filteredRecords` | `StateFlow<List<MilkTeaRecord>>` | 筛选后记录 |
| `stats` | `StateFlow<DailyStats>` | 筛选范围统计 |
| `dailyAggregates` | `StateFlow<List<DailySummary>>` | 趋势数据（每日聚合） |
| `selectedBrand` | `StateFlow<String?>` | 当前选中品牌筛选 |
| `selectedDateRange` | `StateFlow<DateRange>` | 当前日期范围筛选 |
| `allBrands` | `StateFlow<List<String>>` | 所有品牌列表 |
| `commonBrands` | `StateFlow<List<CommonBrand>>` | 常用品牌列表 |
| `editingRecord` | `StateFlow<MilkTeaRecord?>` | 当前正在编辑的记录 |

### 3.3 服务职责分配

| 服务 | 职责 | 对应原 ViewModel 逻辑 |
|---|---|---|
| `RecordService` | 记录增删改查、今日记录/杯数统计 | `addRecord`, `updateRecord`, `deleteRecord`, `todayCount`, `todayRecords` |
| `BrandService` | 品牌列表查询、常用品牌增删 | `allBrands`, `commonBrands`, `addCommonBrand`, `removeCommonBrand` |
| `AnalyticsService` | 日期范围计算、筛选查询、统计/趋势 | `toMillis`, `filteredRecords`, `stats`, `dailyAggregates` |

### 3.4 筛选联动

筛选条件（日期范围 + 品牌）通过 `combine` + `flatMapLatest` 实现联动：

```
selectedDateRange ──┐
                    ├──→ combine ──→ flatMapLatest ──→ 查询 Repository ──→ StateFlow
selectedBrand ──────┘
```

当任一筛选条件变化时，自动重新查询并更新 UI，无需手动刷新。

## 4. 数据库设计

### 4.1 表结构

**milk_tea_records**（饮品记录表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long (PK) | 主键，自增 |
| timestamp | Long | 饮用时间戳（毫秒） |
| brand | String | 品牌名称 |
| drinkName | String? | 饮品名称（可选） |
| price | Double | 价格 |

**common_brands**（常用品牌表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long (PK) | 主键，自增 |
| name | String | 品牌名称（唯一索引） |

### 4.2 版本历史

- **v3** (当前)：添加 `CommonBrand` 实体，新增 `drinkName` 字段
- 采用 `fallbackToDestructiveMigration` 策略（开发阶段允许数据重建）

## 5. UI 设计要点

### 5.1 四 Tab 架构

| Tab | 路由 | 核心功能 |
|---|---|---|
| 首页 | `home` | 今日概览、最近记录、快速添加 |
| 记录 | `records` | 历史记录列表、日期/品牌筛选、编辑删除 |
| 统计 | `stats` | 统计卡片、趋势图表（柱状/折线） |
| 设置 | `settings` | 常用品牌管理 |

### 5.2 主题配色

采用 **Miuix HyperOS 设计规范**：

- **UI 组件库**：Miuix v0.8.8 (`top.yukonga.miuix.kmp:miuix:0.8.8`)
- **主题控制器**：`ThemeController`，支持 `ColorSchemeMode.MonetSystem` 动态取色
- **配色方案**：基于 Monet 动态取色，自动生成温暖治愈风格配色
- **深色模式**：`ColorSchemeMode.Dark` 自动适配

### 5.3 图表交互

- **柱状图**：Canvas 绘制渐变柱状图，支持点击显示 Tooltip
- **折线图**：Canvas 绘制 Cubic Bézier 平滑曲线，支持点击显示 Tooltip
- **指标切换**：杯数 / 金额 两种指标
- **图表类型切换**：柱状图 / 折线图
