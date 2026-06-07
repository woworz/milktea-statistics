# 数据层原理文档

## 1. 架构定位

数据层位于应用架构的最底层，向上通过 `Repository` 暴露数据接口，向下通过 `Room` 操作 `SQLite` 数据库。所有数据操作均为**异步 + 响应式**，对外暴露 `Flow` 流。

```
UI Layer (Compose)
    ↑ (collectAsStateWithLifecycle)
ViewModel (StateFlow)
    ↑
Repository (Flow)
    ↑
DAO (Flow/Suspend)
    ↑
Room → SQLite
```

## 2. Room 框架原理

### 2.1 什么是 Room

Room 是 Google 推出的 SQLite 抽象层，通过注解编译时生成 SQL 操作代码，避免手写 SQL 的模板代码和拼写错误。

核心组件：
- **Entity**：数据实体类，`@Entity` 注解标记，映射到数据库表
- **DAO**：数据访问对象，`@Dao` 注解标记，定义增删改查接口
- **Database**：数据库持有者，`@Database` 注解标记，管理版本和实体

### 2.2 编译时 SQL 验证

Room 在编译阶段解析 `@Query` 中的 SQL 语句，检查语法正确性和字段匹配性。例如：

```kotlin
@Query("SELECT * FROM milk_tea_records ORDER BY timestamp DESC")
fun getAllRecords(): Flow<List<MilkTeaRecord>>
```

编译时 Room 会验证：
- `milk_tea_records` 表名是否匹配某个 `@Entity` 的 `tableName`
- `MilkTeaRecord` 的字段是否与查询结果列匹配
- SQL 语法是否正确

### 2.3 KSP (Kotlin Symbol Processing)

项目使用 **KSP**（而非 KAPT）作为注解处理器：

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.ksp)
}

ksp(libs.androidx.room.compiler)
```

KSP 相比 KAPT 的优势：
- 编译速度更快（直接处理 Kotlin AST，无需生成 Java Stub）
- 更好的 Kotlin 特性支持（如内联类、值类等）
- 更少的增量编译开销

## 3. 响应式数据流

### 3.1 Flow 查询

所有查询函数返回 `Flow<T>`，这是 Room 的内置能力：

```kotlin
@Query("SELECT * FROM milk_tea_records ORDER BY timestamp DESC")
fun getAllRecords(): Flow<List<MilkTeaRecord>>
```

**原理**：Room 通过 `InvalidationTracker` 监听 SQLite 的 `UPDATE/INSERT/DELETE` 操作。当相关表发生变更时，自动重新执行查询并发射新的结果到 Flow。

这意味着：
- 插入新记录 → `getAllRecords()` 自动重新发射更新后的列表
- 无需手动刷新，UI 自动更新

### 3.2 Suspend 写入

写入操作使用 `suspend` 修饰，在协程中执行：

```kotlin
@Insert
suspend fun insert(record: MilkTeaRecord)
```

**原理**：`@Insert/@Update/@Delete` 由 Room 生成实现代码，自动在后台线程执行数据库操作。`suspend` 确保调用方在协程上下文中非阻塞等待。

### 3.3 数据一致性

```kotlin
class MilkTeaRepository(private val dao: MilkTeaDao) {
    fun getAllRecords(): Flow<List<MilkTeaRecord>> = dao.getAllRecords()
    suspend fun insert(record: MilkTeaRecord) = dao.insert(record)
}
```

当调用 `repository.insert(record)` 时：
1. Room 在后台线程执行 `INSERT` SQL
2. `InvalidationTracker` 检测到 `milk_tea_records` 表变更
3. 自动重新执行 `getAllRecords()` 的 SQL
4. `Flow` 发射新列表
5. ViewModel 中 `StateFlow` 更新
6. Compose UI 重组，显示新记录

整个链条完全自动化，无需手动刷新。

## 4. Repository 模式

### 4.1 为什么使用 Repository

Repository 是介于 DAO 和 ViewModel 之间的抽象层：

| 层级 | 职责 |
|---|---|
| DAO | 定义原始 SQL 查询，与数据库 schema 强耦合 |
| Repository | 封装 DAO 调用，对外提供语义化的业务接口，屏蔽底层实现 |
| ViewModel | 持有 UI 状态，调用 Repository 获取/修改数据 |

**优势**：
- **单一职责**：ViewModel 不关心数据从哪来（本地数据库/网络/缓存）
- **可替换性**：未来如需切换数据源（如改为网络 API），只需修改 Repository 实现
- **可测试性**：Repository 可被 Mock，便于单元测试

### 4.2 本项目 Repository 设计

```kotlin
class MilkTeaRepository(
    private val dao: MilkTeaDao,
    private val commonBrandDao: CommonBrandDao,
) {
    // 直接透传 DAO 的 Flow 查询
    fun getAllRecords() = dao.getAllRecords()
    
    // 添加业务封装（如数据转换、默认值处理）
    suspend fun addCommonBrand(name: String) {
        commonBrandDao.insert(CommonBrand(name = name))
    }
}
```

当前 Repository 较为轻量，主要做透传。随着功能扩展，可以在此处添加：
- 数据缓存策略（内存缓存 + 数据库）
- 多数据源合并（本地 + 云端同步）
- 数据清洗和转换

## 5. 数据库 Schema 设计

### 5.1 实体关系

```
┌──────────────────────┐         ┌──────────────────────┐
│   MilkTeaRecord      │         │    CommonBrand       │
│  (milk_tea_records)  │         │   (common_brands)    │
├──────────────────────┤         ├──────────────────────┤
│ id (PK)              │         │ id (PK)              │
│ timestamp            │         │ name (Unique)         │
│ brand                │         └──────────────────────┘
│ drinkName            │
│ price                │
└──────────────────────┘
```

两表之间无直接外键关联，通过 `brand` 字符串字段实现逻辑关联。

### 5.2 设计决策

**为何不用外键？**
- 品牌名称由用户输入，自由度高，非预设枚举
- 历史记录的 `brand` 字段不应因 `CommonBrand` 删除而级联删除
- 简化查询逻辑，避免 JOIN 操作

**`DailyStats` 和 `DailySummary` 不是实体类**
- 它们是查询结果的映射类（DTO），无 `@Entity` 注解
- Room 根据查询结果列名自动映射到 data class 字段

### 5.3 聚合查询原理

```sql
SELECT 
    (timestamp / 86400000) * 86400000 AS dayStart,
    COUNT(*) AS count,
    COALESCE(SUM(price), 0) AS totalSpend,
    COALESCE(AVG(price), 0) AS avgPrice
FROM milk_tea_records 
WHERE timestamp >= :start AND timestamp < :end
GROUP BY dayStart 
ORDER BY dayStart ASC
```

- `(timestamp / 86400000) * 86400000`：将毫秒时间戳按天取整（86400000 = 24h * 60m * 60s * 1000ms）
- `GROUP BY dayStart`：按天分组聚合
- `COALESCE(SUM(price), 0)`：处理无记录时返回 0 而非 NULL

## 6. 数据库迁移策略

```kotlin
@Database(entities = [MilkTeaRecord::class, CommonBrand::class], version = 3)

Room.databaseBuilder(...)
    .fallbackToDestructiveMigration(dropAllTables = true)
    .build()
```

当前使用 **破坏性迁移** (`fallbackToDestructiveMigration`)：
- 数据库版本升级时，删除所有旧表，重新创建
- **适用场景**：开发阶段，快速迭代 schema
- **生产风险**：会丢失用户数据，上线前需改为 `Migration` 方案

生产环境应使用显式迁移：
```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE milk_tea_records ADD COLUMN drinkName TEXT")
    }
}
```

## 7. 单例模式

```kotlin
companion object {
    @Volatile
    private var INSTANCE: MilkTeaDatabase? = null

    fun getDatabase(context: Context): MilkTeaDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(...).build()
            INSTANCE = instance
            instance
        }
    }
}
```

- `@Volatile`：确保 `INSTANCE` 的可见性，防止多线程缓存不一致
- `synchronized(this)`：双检锁模式，保证单例线程安全
- 为何不直接 `by lazy`？`Application` 上下文需要传入，使用工厂方法更灵活

## 8. 性能考量

### 8.1 索引

```kotlin
@Entity(
    tableName = "common_brands",
    indices = [Index(value = ["name"], unique = true)]
)
```

- `common_brands.name` 添加唯一索引，加速查询并保证唯一性
- `milk_tea_records` 未显式添加索引，因 Room 会自动为主键和外键创建索引

### 8.2 查询优化

- 筛选查询中 `timestamp >= :start AND timestamp < :end` 的范围条件，配合主键索引（若 timestamp 有索引）可高效过滤
- `DISTINCT brand` 查询在品牌数量增长后可能需要索引优化

### 8.3 内存与线程

- Room 默认使用内部线程池执行数据库操作
- `Flow` 查询在主线程收集，但 Room 内部在后台线程执行 SQL
- `suspend` 函数自动切换至 IO 线程，无需额外 `withContext(Dispatchers.IO)`
