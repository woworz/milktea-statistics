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
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import java.util.Calendar
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
            onConfirm = { brand, drinkName, price, timestamp ->
                viewModel.addRecord(brand, drinkName, price, timestamp)
                showAddDialog = false
            },
        )
    }

    // 编辑弹窗
    editingRecord?.let { record ->
        AddEditRecordDialog(
            record = record,
            onDismiss = { viewModel.cancelEdit() },
            onConfirm = { brand, drinkName, price, timestamp ->
                viewModel.updateRecord(record.copy(brand = brand, drinkName = drinkName, price = price, timestamp = timestamp))
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
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
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
                record.drinkName?.let { name ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditRecordDialog(
    record: MilkTeaRecord?,
    onDismiss: () -> Unit,
    onConfirm: (brand: String, drinkName: String?, price: Double, timestamp: Long) -> Unit,
) {
    val isEdit = record != null
    var brand by remember(record) { mutableStateOf(record?.brand ?: "") }
    var drinkName by remember(record) { mutableStateOf(record?.drinkName ?: "") }
    var price by remember(record) { mutableStateOf(record?.price?.toString() ?: "") }
    var priceError by remember { mutableStateOf(false) }
    var selectedTimestamp by remember(record) {
        mutableStateOf(record?.timestamp ?: System.currentTimeMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑记录" else "添加奶茶记录") },
        text = {
            Column {
                // 品牌
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("品牌") },
                    placeholder = { Text("例如：喜茶、奈雪") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))

                // 饮品名称（可选）
                OutlinedTextField(
                    value = drinkName,
                    onValueChange = { drinkName = it },
                    label = { Text("饮品（可选）") },
                    placeholder = { Text("例如：柠檬水、珍珠奶茶") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))

                // 价格
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
                Spacer(Modifier.height(12.dp))

                // 日期/时间选择
                Text(
                    text = "时间",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true },
                    ) {
                        Text(
                            text = dateFormat.format(Date(selectedTimestamp)),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTimePicker = true },
                    ) {
                        Text(
                            text = timeFormat.format(Date(selectedTimestamp)),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val priceValue = price.toDoubleOrNull()
                    if (brand.isNotBlank() && priceValue != null && priceValue >= 0) {
                        onConfirm(
                            brand.trim(),
                            drinkName.trim().ifBlank { null },
                            priceValue,
                            selectedTimestamp,
                        )
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

    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                // 保留原时间，只更新日期部分
                val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                val pickerCal = Calendar.getInstance().apply { timeInMillis = millis }
                cal.set(Calendar.YEAR, pickerCal.get(Calendar.YEAR))
                cal.set(Calendar.MONTH, pickerCal.get(Calendar.MONTH))
                cal.set(Calendar.DAY_OF_MONTH, pickerCal.get(Calendar.DAY_OF_MONTH))
                selectedTimestamp = cal.timeInMillis
                showDatePicker = false
            },
            initialDateMillis = selectedTimestamp,
        )
    }

    // 时间选择器
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                selectedTimestamp = cal.timeInMillis
                showTimePicker = false
            },
            initialHour = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                .get(Calendar.HOUR_OF_DAY),
            initialMinute = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                .get(Calendar.MINUTE),
        )
    }
}

// ==================== 日期选择器弹窗 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    initialDateMillis: Long,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onConfirm(it) }
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    ) {
        DatePicker(state = state)
    }
}

// ==================== 时间选择器弹窗 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    initialHour: Int,
    initialMinute: Int,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            TimePicker(state = state)
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(state.hour, state.minute)
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
