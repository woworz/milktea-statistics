# 业务服务层原理文档

## 1. 拆分动机

### 1.1 改造前的问题

改造前，`MilkTeaViewModel` 集中了所有业务逻辑（221 行）：

```
MilkTeaViewModel
├── 筛选状态管理 (selectedBrand, selectedDateRange)
├── 品牌管理 (allBrands, commonBrands, add/remove)
├── 记录 CRUD (add, update, delete)
├── 今日统计 (todayCount, todayRecords)
├── 日期范围计算 (toMillis)
├── 筛选查询 (filteredRecords)
├── 统计查询 (stats)
└── 趋势查询 (dailyAggregates)
```

**问题**：
- **God Object 反模式**：单一类职责过多，后续扩展困难
- **测试困难**：测试记录添加需要构造完整的 ViewModel（含所有 StateFlow）
- **复用困难**：统计逻辑无法在后台任务（如 Worker）中复用
- **协作冲突**：多人同时修改 ViewModel 时冲突率高

### 1.2 拆分原则

按 **单一职责原则 (Single Responsibility Principle)** 拆分：

| 原逻辑 | 归属 | 理由 |
|---|---|---|
| 记录增删改查 | `RecordService` | 数据操作，与品牌/统计无关 |
| 品牌查询/管理 | `BrandService` | 独立领域，可单独扩展（如品牌搜索、品牌图标） |
| 日期范围计算 + 筛选 + 统计/趋势 | `AnalyticsService` | 分析查询，高度相关的计算逻辑 |
| UI 状态（筛选条件、编辑状态） | 保留在 ViewModel | 这些是 UI 层的概念，不属于业务逻辑 |

## 2. 服务层架构

### 2.1 层级关系

```
UI Layer (Compose Screen)
    ↑ 订阅 StateFlow
ViewModel (状态管理 + 服务调度)
    ↑ 调用 Service 方法
Service Layer (业务逻辑)
    ↑ 调用 Repository
Data Layer (Repository → DAO → Database)
```

### 2.2 服务无状态设计

所有 Service 均不持有 `StateFlow` 或 `LiveData`：

```kotlin
class RecordService(private val repository: MilkTeaRepository) {
    // 返回 Flow，由调用方决定如何转换为 StateFlow
    fun getTodayRecords(start: Long, end: Long): Flow<List<MilkTeaRecord>> =
        repository.getRecordsForDay(start, end)
}
```

**优势**：
- 服务可在任何协程上下文中调用（ViewModel、Worker、测试）
- 不依赖 Android 生命周期组件
- 可在 JVM 单元测试中直接测试（无需 Android 环境）

### 2.3 ViewModel 作为协调者

改造后 ViewModel 的职责缩小为：

```kotlin
class MilkTeaViewModel(application: Application) : AndroidViewModel(application) {
    // 1. 持有服务实例
    private val recordService = RecordService(repository)
    private val brandService = BrandService(repository)
    private val analyticsService = AnalyticsService(repository)

    // 2. 管理 UI 状态
    private val _selectedBrand = MutableStateFlow<String?>(null)
    private val _selectedDateRange = MutableStateFlow(DateRange.THIS_MONTH)

    // 3. 通过 combine + flatMapLatest 自动联动
    val filteredRecords: StateFlow<List<MilkTeaRecord>> = dateRangeMillis
        .combine(_selectedBrand) { range, brand -> range to brand }
        .flatMapLatest { (range, brand) ->
            analyticsService.getFilteredRecords(range.first, range.second, brand)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // 4. 操作委托给服务
    fun addRecord(...) {
        viewModelScope.launch { recordService.addRecord(...) }
    }
}
```

## 3. 领域服务设计

### 3.1 RecordService（记录服务）

**职责边界**：只处理 `MilkTeaRecord` 实体的生命周期。

```kotlin
class RecordService(private val repository: MilkTeaRepository) {
    // 查询：返回 Flow，由 ViewModel 决定生命周期
    fun getTodayRecords(start: Long, end: Long): Flow<List<MilkTeaRecord>>
    fun getTodayCount(start: Long, end: Long): Flow<Int>

    // 操作：suspend 函数，由 ViewModel 在 viewModelScope 中调用
    suspend fun addRecord(record: MilkTeaRecord)
    suspend fun updateRecord(record: MilkTeaRecord)
    suspend fun deleteRecord(record: MilkTeaRecord)
}
```

**扩展场景**：
- 添加批量导入功能 → 在 RecordService 中新增 `suspend fun importRecords(records: List<MilkTeaRecord>)`
- 添加记录导出功能 → 在 RecordService 中新增 `suspend fun exportRecords(): String`

### 3.2 BrandService（品牌服务）

**职责边界**：只处理品牌相关的查询和管理。

```kotlin
class BrandService(private val repository: MilkTeaRepository) {
    fun getAllBrands(): Flow<List<String>>
    fun getCommonBrands(): Flow<List<CommonBrand>>
    suspend fun addCommonBrand(name: String)
    suspend fun removeCommonBrand(id: Long)
}
```

**扩展场景**：
- 品牌搜索 → 新增 `fun searchBrands(query: String): Flow<List<String>>`
- 品牌热度统计 → 新增 `fun getBrandPopularity(): Flow<Map<String, Int>>`
- 品牌图标系统 → 新增 `fun getBrandIcon(brand: String): ImageVector`

### 3.3 AnalyticsService（分析服务）

**职责边界**：日期计算 + 条件筛选 + 统计/趋势聚合。

```kotlin
class AnalyticsService(private val repository: MilkTeaRepository) {
    // 筛选查询：统一处理 brand 为 null（无筛选）和 non-null（按品牌筛选）的情况
    fun getFilteredRecords(start: Long, end: Long, brand: String?): Flow<List<MilkTeaRecord>>
    fun getStats(start: Long, end: Long, brand: String?): Flow<DailyStats>
    fun getDailyAggregates(start: Long, end: Long, brand: String?): Flow<List<DailySummary>>

    // 日期计算：纯函数，无副作用
    fun toMillis(range: DateRange): Pair<Long, Long>
}
```

**设计要点**：
- `brand: String?` 参数统一处理有无筛选的情况，避免 ViewModel 中重复 `if/else`
- `toMillis()` 是纯函数，输入确定则输出确定，便于测试
- 日期计算使用 `Calendar`，支持本地化（如周一作为周起始日）

**扩展场景**：
- 自定义日期范围 → 新增 `fun toMillis(startDate: LocalDate, endDate: LocalDate): Pair<Long, Long>`
- 周/月/年聚合 → 新增 `fun getWeeklyAggregates(...)` / `fun getMonthlyAggregates(...)`
- 同比环比 → 新增 `fun getYearOverYearStats(...)`

## 4. 与依赖注入的关系

### 4.1 当前方案：手动构造

```kotlin
private val db = MilkTeaDatabase.getDatabase(application)
private val repository = MilkTeaRepository(db.milkTeaDao(), db.commonBrandDao())
private val recordService = RecordService(repository)
private val brandService = BrandService(repository)
private val analyticsService = AnalyticsService(repository)
```

**优点**：零依赖，代码直观
**缺点**：ViewModel 知道所有依赖细节，新增服务时需要修改 ViewModel

### 4.2 未来演进：ServiceProvider

如需进一步解耦，可引入 `ServiceProvider`：

```kotlin
object ServiceProvider {
    fun recordService(): RecordService = RecordService(repository())
    fun brandService(): BrandService = BrandService(repository())
    fun analyticsService(): AnalyticsService = AnalyticsService(repository())

    private fun repository(): MilkTeaRepository {
        val db = MilkTeaDatabase.getDatabase(...)
        return MilkTeaRepository(db.milkTeaDao(), db.commonBrandDao())
    }
}
```

**更进一步的演进**：引入 Hilt/Koin 进行构造函数注入

```kotlin
@HiltViewModel
class MilkTeaViewModel @Inject constructor(
    private val recordService: RecordService,
    private val brandService: BrandService,
    private val analyticsService: AnalyticsService,
) : ViewModel() { ... }
```

## 5. 测试策略

### 5.1 Service 层单元测试

由于 Service 不依赖 Android 生命周期，可在 JVM 中测试：

```kotlin
class RecordServiceTest {
    private val mockRepository = mockk<MilkTeaRepository>()
    private val service = RecordService(mockRepository)

    @Test
    fun `addRecord delegates to repository`() = runTest {
        val record = MilkTeaRecord(timestamp = 0L, brand = "喜茶", price = 20.0)
        coEvery { mockRepository.insert(record) } returns Unit

        service.addRecord(record)

        coVerify { mockRepository.insert(record) }
    }
}
```

### 5.2 ViewModel 层测试

```kotlin
class MilkTeaViewModelTest {
    @Test
    fun `setDateRange updates selectedDateRange`() = runTest {
        val viewModel = MilkTeaViewModel(application)

        viewModel.setDateRange(DateRange.THIS_WEEK)

        assertEquals(DateRange.THIS_WEEK, viewModel.selectedDateRange.value)
    }
}
```

## 6. 改造前后对比

| 指标 | 改造前 | 改造后 |
|---|---|---|
| ViewModel 行数 | 221 行 | ~120 行（状态管理 + 服务调度） |
| ViewModel 职责数 | 10+ | 3（状态管理、服务调度、UI 操作转发） |
| 可独立测试的业务逻辑 | 0（全部耦合在 ViewModel） | 3 个 Service 均可独立测试 |
| 新增业务领域的工作量 | 修改 ViewModel（冲突风险） | 新增 Service（无冲突） |
| 代码复用性 | 低 | 高（Service 可在 Worker/后台任务中复用） |
