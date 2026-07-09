# 数据层实现

数据层负责本地持久化和查询封装，代码位于 `app/src/main/java/com/mason/milkteastatistics/data/`。

## 文件

| 文件 | 职责 |
|---|---|
| `MilkTeaRecord.kt` | 记录实体，以及统计、洞察、复购模板 DTO |
| `CommonBrand.kt` | 常用品牌实体，品牌名唯一 |
| `MilkTeaDao.kt` | 饮品记录的 CRUD、筛选、统计和每日聚合 SQL |
| `CommonBrandDao.kt` | 常用品牌查询、插入和删除 |
| `MilkTeaDatabase.kt` | Room 数据库、单例和 Migration |
| `MilkTeaRepository.kt` | 对 ViewModel/Service 暴露数据接口，隐藏 DAO 细节 |

## 实体

`MilkTeaRecord` 映射到 `milk_tea_records`：

| 字段 | 说明 |
|---|---|
| `id` | 自增主键 |
| `timestamp` | 饮用时间，毫秒时间戳 |
| `brand` | 品牌名 |
| `drinkName` | 饮品名，可为空 |
| `price` | 价格 |

`CommonBrand` 映射到 `common_brands`，包含自增 `id` 和唯一 `name`。

以下类型不是表实体：

| 类型 | 用途 |
|---|---|
| `DailyStats` | Room 统计查询结果：总杯数、总花费、均价 |
| `DailySummary` | Room 每日聚合结果，用于趋势图 |
| `ConsumptionInsights` | 由记录、趋势和统计数据派生的消费洞察 |
| `PurchaseTemplate` | 由历史记录归纳出的快速复购模板 |

## DAO 查询

`MilkTeaDao` 提供四类能力：

- 记录 CRUD：查询全部、查询某天、计数、插入、更新、删除。
- 品牌列表：从历史记录中查询不重复品牌。
- 筛选：按日期范围查询，可叠加品牌。
- 统计：范围内总杯数、总花费、均价，以及每日聚合趋势。

每日聚合使用 SQLite `localtime` 和 `start of day` 按设备本地自然日分组，图表日期与用户看到的日期保持一致。

`CommonBrandDao` 只管理常用品牌：

- `getAll()`：按名称升序返回。
- `insert()`：冲突时忽略，避免重复品牌。
- `deleteById()`：按主键删除。

## 数据库

`MilkTeaDatabase` 当前版本为 `3`：

```kotlin
@Database(
    entities = [MilkTeaRecord::class, CommonBrand::class],
    version = 3,
    exportSchema = false,
)
```

单例通过 `getDatabase(context)` 获取。应用启动阶段会由 `DatabaseInitializer` 预初始化一次，ViewModel 中再次调用通常只是返回已存在实例。

## Repository

`MilkTeaRepository` 目前是轻量封装，主要把 DAO 方法整理成上层可读的接口：

- `getAllRecords()`
- `getRecordsByDateRange(...)`
- `getRecordsByDateRangeAndBrand(...)`
- `getStats(...)`
- `getDailyAggregates(...)`
- `getCommonBrands()`
- `addCommonBrand(name)`

如果后续加入云同步、缓存、导入导出，优先放在 Repository 或 Service，避免 UI 直接依赖 DAO。
