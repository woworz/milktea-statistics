package com.mason.milkteastatistics.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mason.milkteastatistics.data.DailyStats
import com.mason.milkteastatistics.data.MilkTeaRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkTeaScreen(
    viewModel: MilkTeaViewModel = viewModel(),
) {
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle()
    val selectedDateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
    val allBrands by viewModel.allBrands.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val filteredRecords by viewModel.filteredRecords.collectAsStateWithLifecycle()
    val dailyAggregates by viewModel.dailyAggregates.collectAsStateWithLifecycle()
    val editingRecord by viewModel.editingRecord.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var chartMetric by remember { mutableStateOf(ChartMetric.COUNT) }
    var useLineChart by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🥤 奶茶统计")
                        Text(
                            text = "今日已喝 $todayCount 杯",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记录")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 日期范围 + 品牌筛选
            item {
                FilterSection(
                    selectedRange = selectedDateRange,
                    onRangeSelected = { viewModel.setDateRange(it) },
                    selectedBrand = selectedBrand,
                    allBrands = allBrands,
                    onBrandSelected = { viewModel.setBrandFilter(it) },
                )
            }

            // 统计卡片
            item {
                StatsCards(stats = stats)
            }

            // 图表
            item {
                ChartSection(
                    dailyData = dailyAggregates,
                    metric = chartMetric,
                    onMetricChange = { chartMetric = it },
                    useLineChart = useLineChart,
                    onChartTypeChange = { useLineChart = it },
                )
            }

            // 记录列表标题
            item {
                Text(
                    text = if (selectedBrand != null) "$selectedBrand 的记录" else "全部记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            // 列表
            if (filteredRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "暂无记录\n点击 + 添加",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(filteredRecords, key = { it.id }) { record ->
                    MilkTeaRecordCard(
                        record = record,
                        onEdit = { viewModel.startEdit(record) },
                        onDelete = { viewModel.deleteRecord(record) },
                    )
                }
            }

            // 底部留白
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // 添加弹窗
    if (showAddDialog) {
        AddEditRecordDialog(
            record = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { brand, price ->
                viewModel.addRecord(brand, price)
                showAddDialog = false
            },
        )
    }

    // 编辑弹窗
    editingRecord?.let { record ->
        AddEditRecordDialog(
            record = record,
            onDismiss = { viewModel.cancelEdit() },
            onConfirm = { brand, price ->
                viewModel.updateRecord(record.copy(brand = brand, price = price))
                viewModel.cancelEdit()
            },
        )
    }
}

// ==================== 筛选栏 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit,
    selectedBrand: String?,
    allBrands: List<String>,
    onBrandSelected: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 日期范围
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            DateRange.entries.forEach { range ->
                SegmentedButton(
                    selected = selectedRange == range,
                    onClick = { onRangeSelected(range) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = DateRange.entries.indexOf(range),
                        count = DateRange.entries.size,
                    ),
                ) {
                    Text(range.label)
                }
            }
        }

        // 品牌筛选
        var expanded by remember { mutableStateOf(false) }
        val displayText = selectedBrand ?: "全部品牌"

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                label = { Text("品牌") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("全部品牌") },
                    onClick = {
                        onBrandSelected(null)
                        expanded = false
                    },
                )
                allBrands.forEach { brand ->
                    DropdownMenuItem(
                        text = { Text(brand) },
                        onClick = {
                            onBrandSelected(brand)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

// ==================== 统计卡片 ====================

@Composable
private fun StatsCards(stats: DailyStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            label = "总花费",
            value = "¥%.1f".format(stats.totalSpend),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "杯数",
            value = "${stats.totalCount}",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "均价",
            value = "¥%.1f".format(stats.avgPrice),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ==================== 图表区 ====================

@Composable
private fun ChartSection(
    dailyData: List<com.mason.milkteastatistics.data.DailySummary>,
    metric: ChartMetric,
    onMetricChange: (ChartMetric) -> Unit,
    useLineChart: Boolean,
    onChartTypeChange: (Boolean) -> Unit,
) {
    if (dailyData.isEmpty()) return

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 切换栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ChartMetric.entries.forEach { m ->
                        FilterChip(
                            selected = metric == m,
                            onClick = { onMetricChange(m) },
                            label = { Text(m.label, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = !useLineChart,
                        onClick = { onChartTypeChange(false) },
                        label = { Text("柱状", style = MaterialTheme.typography.labelSmall) },
                    )
                    FilterChip(
                        selected = useLineChart,
                        onClick = { onChartTypeChange(true) },
                        label = { Text("折线", style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 图表
            if (useLineChart) {
                TrendLineChart(
                    dailyData = dailyData,
                    metric = metric,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                TrendChart(
                    dailyData = dailyData,
                    metric = metric,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ==================== 记录卡片 ====================

@Composable
private fun MilkTeaRecordCard(
    record: MilkTeaRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.brand,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.padding(2.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "¥%.2f".format(record.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = timeFormat.format(Date(record.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

// ==================== 添加/编辑弹窗 ====================

@Composable
private fun AddEditRecordDialog(
    record: MilkTeaRecord?,
    onDismiss: () -> Unit,
    onConfirm: (brand: String, price: Double) -> Unit,
) {
    val isEdit = record != null
    var brand by remember(record) { mutableStateOf(record?.brand ?: "") }
    var price by remember(record) { mutableStateOf(record?.price?.toString() ?: "") }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑记录" else "添加奶茶记录") },
        text = {
            Column {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("品牌") },
                    placeholder = { Text("例如：喜茶、奈雪") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = it.isNotEmpty() && it.toDoubleOrNull() == null
                    },
                    label = { Text("价格 (¥)") },
                    placeholder = { Text("例如：25") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceError,
                    supportingText = if (priceError) {
                        { Text("请输入有效数字") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val priceValue = price.toDoubleOrNull()
                    if (brand.isNotBlank() && priceValue != null && priceValue >= 0) {
                        onConfirm(brand.trim(), priceValue)
                    } else {
                        priceError = true
                    }
                },
            ) {
                Text(if (isEdit) "保存" else "添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
