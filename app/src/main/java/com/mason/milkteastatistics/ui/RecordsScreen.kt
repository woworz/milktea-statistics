package com.mason.milkteastatistics.ui

import com.mason.milkteastatistics.model.DateRange
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme
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

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = "记录"
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 筛选
            item {
                RecordsFilterSection(
                    selectedRange = selectedDateRange,
                    onRangeSelected = { viewModel.setDateRange(it) },
                    selectedBrand = selectedBrand,
                    allBrands = allBrands,
                    onBrandSelected = { viewModel.setBrandFilter(it) },
                )
            }

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
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            } else {
                items(filteredRecords, key = { it.id }) { record ->
                    RecordCard(
                        record = record,
                        onEdit = { viewModel.startEdit(record) },
                        onDelete = { viewModel.deleteRecord(record) },
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
        // 日期范围 - 用 Button 替代 SegmentedButton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DateRange.entries.forEach { range ->
                Button(
                    onClick = { onRangeSelected(range) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(range.label)
                }
            }
        }

        // 品牌筛选 - 用 Button 替代 FilterChip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { onBrandSelected(null) },
            ) {
                Text("全部品牌", style = MiuixTheme.textStyles.body2)
            }
            allBrands.forEach { brand ->
                Button(
                    onClick = { onBrandSelected(brand) },
                ) {
                    Text(brand, style = MiuixTheme.textStyles.body2)
                }
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
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "¥%.2f".format(record.price),
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
                record.drinkName?.let { name ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = name,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(record.timestamp)),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}
