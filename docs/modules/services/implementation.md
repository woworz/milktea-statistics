# 业务服务层实现文档

本模块负责按领域拆分 `MilkTeaViewModel` 中的业务逻辑，每个服务专注单一职责，方便后续独立扩展和测试。

## 文件清单

| 文件 | 路径 | 说明 |
|---|---|---|
| `DateRange.kt` | `model/DateRange.kt` | 日期范围枚举（从 ViewModel 移出，供服务层共享） |
| `RecordService.kt` | `service/RecordService.kt` | 记录增删改查 + 今日统计 |
| `BrandService.kt` | `service/BrandService.kt` | 品牌列表 + 常用品牌管理 |
| `AnalyticsService.kt` | `service/AnalyticsService.kt` | 日期范围计算 + 筛选查询 + 统计/趋势 |

---

## 1. DateRange.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/model/DateRange.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `DateRange` enum | **第 3-7 行** | 日期范围枚举：`THIS_WEEK`, `THIS_MONTH`, `LAST_MONTH` |

### 1.1 字段

| 字段 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `label` | 第 3 行 | `String` | 显示文本（如 `"本月"`） |

### 1.2 枚举值

| 枚举值 | 行号 | label |
|---|---|---|
| `THIS_WEEK` | 第 4 行 | `"本周"` |
| `THIS_MONTH` | 第 5 行 | `"本月"` |
| `LAST_MONTH` | 第 6 行 | `"上月"` |

---

## 2. RecordService.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/service/RecordService.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `RecordService` class | **第 14-31 行** | 记录服务，专注记录 CRUD 和今日统计 |

### 2.1 构造函数

| 参数 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `repository` | 第 14 行 | `MilkTeaRepository` | 数据仓库实例 |

### 2.2 查询方法

| 函数 | 行号 | 返回类型 | 说明 |
|---|---|---|---|
| `getTodayRecords(todayStart, todayEnd)` | 第 16-17 行 | `Flow<List<MilkTeaRecord>>` | 查询指定日期范围内的记录 |
| `getTodayCount(todayStart, todayEnd)` | 第 19-20 行 | `Flow<Int>` | 查询指定日期范围内的记录数 |

### 2.3 操作方法

| 函数 | 行号 | 返回类型 | 说明 |
|---|---|---|---|
| `addRecord(record)` | 第 22 行 | `suspend Unit` | 插入一条记录 |
| `updateRecord(record)` | 第 24 行 | `suspend Unit` | 更新记录 |
| `deleteRecord(record)` | 第 26 行 | `suspend Unit` | 删除记录 |

---

## 3. BrandService.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/service/BrandService.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `BrandService` class | **第 14-27 行** | 品牌服务，专注品牌查询和常用品牌管理 |

### 3.1 构造函数

| 参数 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `repository` | 第 14 行 | `MilkTeaRepository` | 数据仓库实例 |

### 3.2 查询方法

| 函数 | 行号 | 返回类型 | 说明 |
|---|---|---|---|
| `getAllBrands()` | 第 16-17 行 | `Flow<List<String>>` | 查询所有不重复品牌 |
| `getCommonBrands()` | 第 19-20 行 | `Flow<List<CommonBrand>>` | 查询所有常用品牌 |

### 3.3 操作方法

| 函数 | 行号 | 返回类型 | 说明 |
|---|---|---|---|
| `addCommonBrand(name)` | 第 22-24 行 | `suspend Unit` | 添加常用品牌 |
| `removeCommonBrand(id)` | 第 26-28 行 | `suspend Unit` | 删除常用品牌 |

---

## 4. AnalyticsService.kt

**路径**: `app/src/main/java/com/mason/milkteastatistics/service/AnalyticsService.kt`

| 类/函数 | 行号 | 说明 |
|---|---|---|
| `AnalyticsService` class | **第 17-63 行** | 分析服务，专注日期计算、筛选查询、统计/趋势数据 |

### 4.1 构造函数

| 参数 | 行号 | 类型 | 说明 |
|---|---|---|---|
| `repository` | 第 17 行 | `MilkTeaRepository` | 数据仓库实例 |

### 4.2 筛选查询

| 函数 | 行号 | 返回类型 | 说明 |
|---|---|---|---|
| `getFilteredRecords(start, end, brand?)` | 第 19-25 行 | `Flow<List<MilkTeaRecord>>` | 按日期范围 ± 品牌筛选记录 |
| `getStats(start, end, brand?)` | 第 27-33 行 | `Flow<DailyStats>` | 按日期范围 ± 品牌统计 |
| `getDailyAggregates(start, end, brand?)` | 第 35-41 行 | `Flow<List<DailySummary>>` | 按日期范围 ± 品牌趋势聚合 |

### 4.3 日期范围计算

| 函数 | 行号 | 返回类型 | 说明 |
|---|---|---|---|
| `toMillis(range)` | 第 43-63 行 | `Pair<Long, Long>` | 将 `DateRange` 转换为起止时间戳 |

### 4.4 日期计算逻辑

| 分支 | 行号 | 说明 |
|---|---|---|
| `THIS_WEEK` | 第 50-53 行 | 本周一 00:00 到下周一同一时刻 |
| `THIS_MONTH` | 第 54-57 行 | 本月 1 日 00:00 到下月 1 日 00:00 |
| `LAST_MONTH` | 第 58-62 行 | 上月 1 日 00:00 到本月 1 日 00:00 |

---

## 索引总表

| 函数/类 | 所在文件 | 行号 | 说明 |
|---|---|---|---|
| `DateRange` enum | `DateRange.kt` | 3-7 | 日期范围枚举 |
| `RecordService` | `RecordService.kt` | 14-31 | 记录服务 |
| `getTodayRecords(start, end)` | `RecordService.kt` | 16-17 | 今日记录查询 |
| `getTodayCount(start, end)` | `RecordService.kt` | 19-20 | 今日记录数查询 |
| `addRecord(record)` | `RecordService.kt` | 22 | 添加记录 |
| `updateRecord(record)` | `RecordService.kt` | 24 | 更新记录 |
| `deleteRecord(record)` | `RecordService.kt` | 26 | 删除记录 |
| `BrandService` | `BrandService.kt` | 14-27 | 品牌服务 |
| `getAllBrands()` | `BrandService.kt` | 16-17 | 品牌列表查询 |
| `getCommonBrands()` | `BrandService.kt` | 19-20 | 常用品牌查询 |
| `addCommonBrand(name)` | `BrandService.kt` | 22-24 | 添加常用品牌 |
| `removeCommonBrand(id)` | `BrandService.kt` | 26-28 | 删除常用品牌 |
| `AnalyticsService` | `AnalyticsService.kt` | 17-63 | 分析服务 |
| `getFilteredRecords(start, end, brand?)` | `AnalyticsService.kt` | 19-25 | 筛选记录查询 |
| `getStats(start, end, brand?)` | `AnalyticsService.kt` | 27-33 | 统计查询 |
| `getDailyAggregates(start, end, brand?)` | `AnalyticsService.kt` | 35-41 | 趋势聚合查询 |
| `toMillis(range)` | `AnalyticsService.kt` | 43-63 | 日期范围转时间戳 |
