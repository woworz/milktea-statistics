# 数据层原理

## Room 与 Flow

Room 负责把 Kotlin 数据类和 SQLite 表连接起来。项目中的查询大多返回 `Flow`：

```kotlin
@Query("SELECT * FROM milk_tea_records ORDER BY timestamp DESC")
fun getAllRecords(): Flow<List<MilkTeaRecord>>
```

当对应表发生插入、更新或删除时，Room 会通过 `InvalidationTracker` 重新执行相关查询并发射新结果。ViewModel 将这些 `Flow` 转成 `StateFlow`，Compose 页面订阅后自动刷新。

写入使用 `suspend`：

```kotlin
@Insert
suspend fun insert(record: MilkTeaRecord)
```

调用方在 `viewModelScope` 中执行，避免阻塞 UI。

## Repository 的作用

Repository 是 DAO 和业务层之间的边界：

```text
Service / ViewModel
    ↓
MilkTeaRepository
    ↓
MilkTeaDao / CommonBrandDao
```

当前 Repository 大多透传 DAO，这仍然有价值：上层不需要知道具体 SQL 和 DAO 拆分。未来如果增加缓存、同步或数据清洗，也可以在这里扩展。

## 聚合查询约定

统计页面依赖两类 Room 聚合：

- `DailyStats`：指定范围内的总杯数、总花费、均价。
- `DailySummary`：按天聚合后的杯数、总花费、均价，用于趋势图。

每日聚合按本地自然日计算：

```sql
strftime('%s', timestamp / 1000, 'unixepoch', 'localtime', 'start of day', 'utc')
```

这样可以避免用户凌晨添加的记录因为 UTC 日期被分到前一天。

`ConsumptionInsights` 不通过 SQL 直接查询，而是在 `AnalyticsService` 中由 `filteredRecords`、`dailyAggregates` 和 `stats` 组合计算。`PurchaseTemplate` 也不落库，由 ViewModel 根据全部历史记录按品牌、饮品名和价格分组派生。

## Migration 约定

当前迁移：

- `MIGRATION_1_2`：新增 `milk_tea_records.drinkName`。
- `MIGRATION_2_3`：新增 `common_brands`，并创建 `name` 唯一索引。

新增数据库字段或表时需要同步做四件事：

1. 更新实体和 DAO 查询。
2. 提升 `@Database(version = ...)`。
3. 新增显式 Migration。
4. 检查统计 DTO 的列名是否仍与 SQL alias 一致。

## 性能注意

- 时间范围查询大量依赖 `timestamp`，数据量增大后可以考虑给 `milk_tea_records.timestamp` 加索引。
- `SELECT DISTINCT brand` 随记录变多可能变慢，后续可用品牌表或索引优化。
- 复购模板当前依赖 `getAllRecords()` 在内存分组，记录很多时可考虑改为 SQL 聚合或单独模板表。
- `exportSchema = false` 简化了当前项目，但正式发布前建议开启 schema 导出，便于迁移审查。
