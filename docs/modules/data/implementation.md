# 数据层实现文档

本模块负责数据持久化，包括实体定义、数据库访问、数据仓库。所有实现基于 **Room** 框架。

## 文件清单

| 文件 | 路径 | 说明 |
|---|---|---|
| `MilkTeaRecord.kt` | `data/MilkTeaRecord.kt` | 数据实体类定义 |
| `CommonBrand.kt` | `data/CommonBrand.kt` | 常用品牌实体 |
| `MilkTeaDao.kt` | `data/MilkTeaDao.kt` | 饮品记录 DAO 接口 |
| `CommonBrandDao.kt` | `data/CommonBrandDao.kt` | 常用品牌 DAO 接口 |
| `MilkTeaDatabase.kt` | `data/MilkTeaDatabase.kt` | Room 数据库定义 |
| `MilkTeaRepository.kt` | `data/MilkTeaRepository.kt` | 数据仓库（Repository） |
| `DateRange.kt` | `model/DateRange.kt` | 日期范围枚举（供数据层和 UI 层共享） |

---

## 1. MilkTeaRecord.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/data/MilkTeaRecord.kt`

### 1.1 数据类

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `MilkTeaRecord` data class | **第 8-15 行** | 饮品记录实体，映射到 `milk_tea_records` 表 |
| `DailyStats` data class | **第 18-22 行** | 日/周/月聚合统计数据类 |
| `DailySummary` data class | **第 25-31 行** | 单日聚合数据（用于趋势图），映射 `dayStart` 列 |

### 1.2 MilkTeaRecord 字段

| 字段 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `id` | 第 10 行 | `Long` | 主键，自增 (`@PrimaryKey(autoGenerate = true)`) |
| `timestamp` | 第 11 行 | `Long` | 饮用时间戳（毫秒） |
| `brand` | 第 12 行 | `String` | 品牌名称 |
| `drinkName` | 第 13 行 | `String?` | 饮品名称（可选） |
| `price` | 第 14 行 | `Double` | 价格 |

### 1.3 DailySummary 字段

| 字段 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `dayStart` | 第 27 行 | `Long` | 日期起始时间戳（`@ColumnInfo(name = "dayStart")`） |
| `count` | 第 28 行 | `Int` | 当日记录数 |
| `totalSpend` | 第 29 行 | `Double` | 当日总花费 |
| `avgPrice` | 第 30 行 | `Double` | 当日均价 |

---

## 2. CommonBrand.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/data/CommonBrand.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `CommonBrand` data class | **第 11-15 行** | 常用品牌实体，映射到 `common_brands` 表 |

### 2.1 字段

| 字段 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `id` | 第 12-13 行 | `Long` | 主键，自增 |
| `name` | 第 14 行 | `String` | 品牌名称 |

### 2.2 注解

| 注解 | 行号 | 说明 |
|---|---|---|
| `@Entity(tableName = "common_brands")` | 第 7-10 行 | 实体注解，指定表名 |
| `indices = [Index(value = ["name"], unique = true)]` | 第 9 行 | `name` 字段唯一索引 |

---

## 3. MilkTeaDao.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/data/MilkTeaDao.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `MilkTeaDao` interface | **第 10-134 行** | 饮品记录 DAO 接口，标注 `@Dao` |

### 3.1 CRUD 操作

| 函数 | 行号 | 返回类型 | SQL / 说明 |
|---|---|---|---|
| `getAllRecords()` | 第 15-16 行 | `Flow<List<MilkTeaRecord>>` | 查询所有记录，按时间倒序 |
| `getRecordsForDay(startOfDay, endOfDay)` | 第 18-25 行 | `Flow<List<MilkTeaRecord>>` | 查询指定日期范围内的记录 |
| `getCountForDay(startOfDay, endOfDay)` | 第 27-33 行 | `Flow<Int>` | 查询指定日期范围内的记录数 |
| `insert(record)` | 第 35-36 行 | `suspend Unit` | 插入一条记录（`@Insert`） |
| `update(record)` | 第 38-39 行 | `suspend Unit` | 更新记录（`@Update`） |
| `delete(record)` | 第 41-42 行 | `suspend Unit` | 删除记录（`@Delete`） |

### 3.2 品牌查询

| 函数 | 行号 | 返回类型 | SQL / 说明 |
|---|---|---|---|
| `getAllBrands()` | 第 46-47 行 | `Flow<List<String>>` | 查询所有不重复品牌，按字母升序 |

### 3.3 筛选查询

| 函数 | 行号 | 返回类型 | SQL / 说明 |
|---|---|---|---|
| `getRecordsByDateRange(start, end)` | 第 51-58 行 | `Flow<List<MilkTeaRecord>>` | 按日期范围查询 |
| `getRecordsByDateRangeAndBrand(start, end, brand)` | 第 60-71 行 | `Flow<List<MilkTeaRecord>>` | 按日期范围 + 品牌联合筛选 |

### 3.4 统计查询

| 函数 | 行号 | 返回类型 | SQL / 说明 |
|---|---|---|---|
| `getStats(start, end)` | 第 75-85 行 | `Flow<DailyStats>` | 范围内总杯数、总花费、均价 |
| `getStatsByBrand(start, end, brand)` | 第 87-97 行 | `Flow<DailyStats>` | 按品牌筛选后的统计 |

### 3.5 趋势聚合查询

| 函数 | 行号 | 返回类型 | SQL / 说明 |
|---|---|---|---|
| `getDailyAggregates(start, end)` | 第 101-114 行 | `Flow<List<DailySummary>>` | 按天聚合（杯数、总花费、均价） |
| `getDailyAggregatesByBrand(start, end, brand)` | 第 116-133 行 | `Flow<List<DailySummary>>` | 按天聚合 + 品牌筛选 |

**注意**：聚合查询使用 SQLite `localtime`/`start of day` 以设备本地自然日分组，避免 UTC 天导致凌晨记录归到前一天。

---

## 4. CommonBrandDao.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/data/CommonBrandDao.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `CommonBrandDao` interface | **第 10-20 行** | 常用品牌 DAO 接口，标注 `@Dao` |

### 4.1 函数列表

| 函数 | 行号 | 返回类型 | SQL / 说明 |
|---|---|---|---|
| `getAll()` | 第 12-13 行 | `Flow<List<CommonBrand>>` | 查询所有常用品牌，按名称升序 |
| `insert(brand)` | 第 15-16 行 | `suspend Unit` | 插入品牌，冲突时忽略（`OnConflictStrategy.IGNORE`） |
| `deleteById(id)` | 第 18-19 行 | `suspend Unit` | 按 ID 删除品牌 |

---

## 5. MilkTeaDatabase.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/data/MilkTeaDatabase.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `MilkTeaDatabase` abstract class | **第 13-35 行** | Room 数据库抽象类 |

### 5.1 成员

| 成员 | 行号 | 说明 |
|---|---|---|
| `milkTeaDao()` | 第 15 行 | 抽象函数，返回 `MilkTeaDao` |
| `commonBrandDao()` | 第 16 行 | 抽象函数，返回 `CommonBrandDao` |
| `INSTANCE` | 第 20 行 | 单例引用（`@Volatile`） |
| `getDatabase(context)` | 第 22-32 行 | 双检锁单例获取函数 |

### 5.2 数据库配置

| 配置 | 行号 | 说明 |
|---|---|---|
| `@Database(entities = [...], version = 3)` | 第 8-12 行 | 包含 2 个实体，版本 3 |
| `.addMigrations(MIGRATION_1_2, MIGRATION_2_3)` | 第 47 行 | 显式迁移，保留用户数据 |
| 数据库文件名 `"milk_tea_database"` | 第 27 行 | SQLite 数据库文件名 |

---

## 6. MilkTeaRepository.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/data/MilkTeaRepository.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `MilkTeaRepository` class | **第 5-71 行** | 数据仓库，封装 DAO 调用 |

### 6.1 构造函数

| 参数 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `dao` | 第 6 行 | `MilkTeaDao` | 饮品记录 DAO |
| `commonBrandDao` | 第 7 行 | `CommonBrandDao` | 常用品牌 DAO |

### 6.2 CRUD 操作

| 函数 | 行号 | 返回类型 | 实现 |
|---|---|---|---|
| `getAllRecords()` | 第 12 行 | `Flow<List<MilkTeaRecord>>` | `dao.getAllRecords()` |
| `getRecordsForDay(startOfDay, endOfDay)` | 第 14-15 行 | `Flow<List<MilkTeaRecord>>` | `dao.getRecordsForDay(...)` |
| `getCountForDay(startOfDay, endOfDay)` | 第 17-18 行 | `Flow<Int>` | `dao.getCountForDay(...)` |
| `insert(record)` | 第 20 行 | `suspend Unit` | `dao.insert(record)` |
| `update(record)` | 第 22 行 | `suspend Unit` | `dao.update(record)` |
| `delete(record)` | 第 24 行 | `suspend Unit` | `dao.delete(record)` |

### 6.3 品牌操作

| 函数 | 行号 | 返回类型 | 实现 |
|---|---|---|---|
| `getAllBrands()` | 第 28 行 | `Flow<List<String>>` | `dao.getAllBrands()` |

### 6.4 筛选操作

| 函数 | 行号 | 返回类型 | 实现 |
|---|---|---|---|
| `getRecordsByDateRange(start, end)` | 第 32-33 行 | `Flow<List<MilkTeaRecord>>` | `dao.getRecordsByDateRange(...)` |
| `getRecordsByDateRangeAndBrand(start, end, brand)` | 第 35-39 行 | `Flow<List<MilkTeaRecord>>` | `dao.getRecordsByDateRangeAndBrand(...)` |

### 6.5 统计操作

| 函数 | 行号 | 返回类型 | 实现 |
|---|---|---|---|
| `getStats(start, end)` | 第 43-44 行 | `Flow<DailyStats>` | `dao.getStats(...)` |
| `getStatsByBrand(start, end, brand)` | 第 46-47 行 | `Flow<DailyStats>` | `dao.getStatsByBrand(...)` |

### 6.6 趋势操作

| 函数 | 行号 | 返回类型 | 实现 |
|---|---|---|---|
| `getDailyAggregates(start, end)` | 第 51-52 行 | `Flow<List<DailySummary>>` | `dao.getDailyAggregates(...)` |
| `getDailyAggregatesByBrand(start, end, brand)` | 第 54-58 行 | `Flow<List<DailySummary>>` | `dao.getDailyAggregatesByBrand(...)` |

### 6.7 常用品牌操作

| 函数 | 行号 | 返回类型 | 实现 |
|---|---|---|---|
| `getCommonBrands()` | 第 62 行 | `Flow<List<CommonBrand>>` | `commonBrandDao.getAll()` |
| `addCommonBrand(name)` | 第 64-66 行 | `suspend Unit` | 创建 `CommonBrand` 并插入 |
| `removeCommonBrand(id)` | 第 68-70 行 | `suspend Unit` | `commonBrandDao.deleteById(id)` |

---

## 索引总表

按字母排序，方便查找：

| 函数名 | 所在文件 | 行号 | 返回类型 |
|---|---|---|---|
| `addCommonBrand(name)` | `MilkTeaRepository.kt` | 64-66 | `suspend Unit` |
| `cancelEdit()` | `MilkTeaViewModel.kt` | 167-169 | `Unit` |
| `commonBrandDao()` | `MilkTeaDatabase.kt` | 16 | `CommonBrandDao` |
| `delete(record)` | `MilkTeaDao.kt` | 41-42 | `suspend Unit` |
| `delete(record)` | `MilkTeaRepository.kt` | 24 | `suspend Unit` |
| `deleteById(id)` | `CommonBrandDao.kt` | 18-19 | `suspend Unit` |
| `getAll()` | `CommonBrandDao.kt` | 12-13 | `Flow<List<CommonBrand>>` |
| `getAllBrands()` | `MilkTeaDao.kt` | 46-47 | `Flow<List<String>>` |
| `getAllBrands()` | `MilkTeaRepository.kt` | 28 | `Flow<List<String>>` |
| `getAllRecords()` | `MilkTeaDao.kt` | 15-16 | `Flow<List<MilkTeaRecord>>` |
| `getAllRecords()` | `MilkTeaRepository.kt` | 12 | `Flow<List<MilkTeaRecord>>` |
| `getCommonBrands()` | `MilkTeaRepository.kt` | 62 | `Flow<List<CommonBrand>>` |
| `getCountForDay(startOfDay, endOfDay)` | `MilkTeaDao.kt` | 27-33 | `Flow<Int>` |
| `getCountForDay(startOfDay, endOfDay)` | `MilkTeaRepository.kt` | 17-18 | `Flow<Int>` |
| `getDailyAggregates(start, end)` | `MilkTeaDao.kt` | 101-114 | `Flow<List<DailySummary>>` |
| `getDailyAggregates(start, end)` | `MilkTeaRepository.kt` | 51-52 | `Flow<List<DailySummary>>` |
| `getDailyAggregatesByBrand(start, end, brand)` | `MilkTeaDao.kt` | 116-133 | `Flow<List<DailySummary>>` |
| `getDailyAggregatesByBrand(start, end, brand)` | `MilkTeaRepository.kt` | 54-58 | `Flow<List<DailySummary>>` |
| `getDatabase(context)` | `MilkTeaDatabase.kt` | 22-32 | `MilkTeaDatabase` |
| `getRecordsByDateRange(start, end)` | `MilkTeaDao.kt` | 51-58 | `Flow<List<MilkTeaRecord>>` |
| `getRecordsByDateRange(start, end)` | `MilkTeaRepository.kt` | 32-33 | `Flow<List<MilkTeaRecord>>` |
| `getRecordsByDateRangeAndBrand(start, end, brand)` | `MilkTeaDao.kt` | 60-71 | `Flow<List<MilkTeaRecord>>` |
| `getRecordsByDateRangeAndBrand(start, end, brand)` | `MilkTeaRepository.kt` | 35-39 | `Flow<List<MilkTeaRecord>>` |
| `getRecordsForDay(startOfDay, endOfDay)` | `MilkTeaDao.kt` | 18-25 | `Flow<List<MilkTeaRecord>>` |
| `getRecordsForDay(startOfDay, endOfDay)` | `MilkTeaRepository.kt` | 14-15 | `Flow<List<MilkTeaRecord>>` |
| `getStats(start, end)` | `MilkTeaDao.kt` | 75-85 | `Flow<DailyStats>` |
| `getStats(start, end)` | `MilkTeaRepository.kt` | 43-44 | `Flow<DailyStats>` |
| `getStatsByBrand(start, end, brand)` | `MilkTeaDao.kt` | 87-97 | `Flow<DailyStats>` |
| `getStatsByBrand(start, end, brand)` | `MilkTeaRepository.kt` | 46-47 | `Flow<DailyStats>` |
| `DateRange` enum | `model/DateRange.kt` | 3-7 | `DateRange` |
| `getStartOfToday()` | `MilkTeaViewModel.kt` | 171-178 | `Long` |
| `insert(brand)` | `CommonBrandDao.kt` | 15-16 | `suspend Unit` |
| `insert(record)` | `MilkTeaDao.kt` | 35-36 | `suspend Unit` |
| `insert(record)` | `MilkTeaRepository.kt` | 20 | `suspend Unit` |
| `milkTeaDao()` | `MilkTeaDatabase.kt` | 15 | `MilkTeaDao` |
| `removeCommonBrand(id)` | `MilkTeaRepository.kt` | 68-70 | `suspend Unit` |
| `setBrandFilter(brand)` | `MilkTeaViewModel.kt` | 155-157 | `Unit` |
| `setDateRange(range)` | `MilkTeaViewModel.kt` | 159-161 | `Unit` |
| `startEdit(record)` | `MilkTeaViewModel.kt` | 163-165 | `Unit` |
| `toMillis(range)` | `AnalyticsService.kt` | 43-63 | `Pair<Long, Long>` |
| `update(record)` | `MilkTeaDao.kt` | 38-39 | `suspend Unit` |
| `update(record)` | `MilkTeaRepository.kt` | 22 | `suspend Unit` |
