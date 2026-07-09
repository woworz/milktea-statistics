package com.mason.milkteastatistics.ui

import com.mason.milkteastatistics.model.DateRange
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mason.milkteastatistics.data.MilkTeaRecord
import com.mason.milkteastatistics.ui.components.AddEditRecordDialog
import com.mason.milkteastatistics.ui.components.AppTopBar
import com.mason.milkteastatistics.ui.components.EmptyStateCard
import com.mason.milkteastatistics.ui.components.FilterPill
import com.mason.milkteastatistics.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordsScreen(viewModel: MilkTeaViewModel) {
    val selectedDateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
    val allBrands by viewModel.allBrands.collectAsStateWithLifecycle()
    val filteredRecords by viewModel.filteredRecords.collectAsStateWithLifecycle()
    val editingRecord by viewModel.editingRecord.collectAsStateWithLifecycle()
    val commonBrands by viewModel.commonBrands.collectAsStateWithLifecycle()
    val purchaseTemplates by viewModel.purchaseTemplates.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var recordPendingDelete by remember { mutableStateOf<MilkTeaRecord?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val searchedRecords = remember(filteredRecords, searchQuery) {
        filteredRecords.filterBySearch(searchQuery)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "全部记录",
                subtitle = "按时间和品牌筛选，点击卡片可编辑",
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记录")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                RecordsFilterSection(
                    selectedRange = selectedDateRange,
                    onRangeSelected = { viewModel.setDateRange(it) },
                    selectedBrand = selectedBrand,
                    allBrands = allBrands,
                    onBrandSelected = { viewModel.setBrandFilter(it) },
                )
            }

            item {
                RecordsSearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                )
            }

            if (filteredRecords.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "记录列表",
                        trailing = if (searchQuery.isBlank()) {
                            "${filteredRecords.size} 条"
                        } else {
                            "${searchedRecords.size}/${filteredRecords.size} 条"
                        },
                    )
                }
            }

            if (searchedRecords.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = if (filteredRecords.isEmpty()) "暂无匹配记录" else "没有搜索结果",
                        message = if (filteredRecords.isEmpty()) {
                            "可以切换筛选条件，或添加一条新的奶茶记录。"
                        } else {
                            "换个关键词试试，品牌、饮品名和价格都可以搜索。"
                        },
                        actionLabel = if (filteredRecords.isEmpty()) "添加记录" else null,
                        onAction = if (filteredRecords.isEmpty()) {
                            { showAddDialog = true }
                        } else {
                            null
                        },
                    )
                }
            } else {
                items(searchedRecords, key = { it.id }) { record ->
                    RecordCard(
                        record = record,
                        onEdit = { viewModel.startEdit(record) },
                        onDelete = { recordPendingDelete = record },
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddEditRecordDialog(
            record = null,
            commonBrands = commonBrands,
            purchaseTemplates = purchaseTemplates,
            onAddCommonBrand = { viewModel.addCommonBrand(it) },
            onRemoveCommonBrand = { viewModel.removeCommonBrand(it) },
            onDismiss = { showAddDialog = false },
            onConfirm = { brand, drinkName, price, timestamp ->
                viewModel.addRecord(brand, drinkName, price, timestamp)
                showAddDialog = false
            },
        )
    }

    editingRecord?.let { record ->
        AddEditRecordDialog(
            record = record,
            commonBrands = commonBrands,
            purchaseTemplates = purchaseTemplates,
            onAddCommonBrand = { viewModel.addCommonBrand(it) },
            onRemoveCommonBrand = { viewModel.removeCommonBrand(it) },
            onDismiss = { viewModel.cancelEdit() },
            onConfirm = { brand, drinkName, price, timestamp ->
                viewModel.updateRecord(
                    record.copy(brand = brand, drinkName = drinkName, price = price, timestamp = timestamp),
                )
                viewModel.cancelEdit()
            },
        )
    }

    recordPendingDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordPendingDelete = null },
            title = { Text("删除这条记录？") },
            text = { Text("删除后无法恢复：${record.brand} ¥%.1f".format(record.price)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(record)
                        recordPendingDelete = null
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordPendingDelete = null }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun RecordsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("搜索记录") },
        placeholder = { Text("品牌、饮品或价格") },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "清空搜索",
                    )
                }
            }
        } else {
            null
        },
    )
}

// ==================== 筛选栏 ====================

@Composable
private fun RecordsFilterSection(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit,
    selectedBrand: String?,
    allBrands: List<String>,
    onBrandSelected: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DateRange.entries.forEach { range ->
                val selected = selectedRange == range
                FilterPill(
                    label = range.label,
                    selected = selected,
                    onClick = { onRangeSelected(range) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterPill(
                    label = "全部品牌",
                    selected = selectedBrand == null,
                    onClick = { onBrandSelected(null) },
                )
            }
            items(allBrands) { brand ->
                FilterPill(
                    label = brand,
                    selected = selectedBrand == brand,
                    onClick = { onBrandSelected(brand) },
                )
            }
        }
    }
}

// ==================== 记录卡片 ====================

@Composable
private fun RecordCard(
    record: MilkTeaRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = record.brand,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "¥%.2f".format(record.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                record.drinkName?.let { name ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(record.timestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "点击编辑",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun List<MilkTeaRecord>.filterBySearch(query: String): List<MilkTeaRecord> {
    val normalizedQuery = query.trim().lowercase(Locale.getDefault())
    if (normalizedQuery.isBlank()) {
        return this
    }

    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return filter { record ->
        listOf(
            record.brand,
            record.drinkName.orEmpty(),
            "¥%.2f".format(record.price),
            "¥%.1f".format(record.price),
            "%.2f".format(record.price),
            "%.1f".format(record.price),
            dateFormat.format(Date(record.timestamp)),
        ).any { value ->
            value.lowercase(Locale.getDefault()).contains(normalizedQuery)
        }
    }
}
