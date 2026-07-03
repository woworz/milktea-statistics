package com.mason.milkteastatistics.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.DateRange
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
import com.mason.milkteastatistics.ui.components.MetricCard
import com.mason.milkteastatistics.ui.components.SectionHeader
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: MilkTeaViewModel) {
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle()
    val todayRecords by viewModel.todayRecords.collectAsStateWithLifecycle()
    val commonBrands by viewModel.commonBrands.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "今日奶茶",
                subtitle = "快速记录今天喝了什么、花了多少",
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加记录"
                )
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
                val todayPrice = todayRecords.sumOf { it.price }
                TodayOverview(
                    todaySpend = todayPrice,
                    todayCount = todayCount,
                )
            }

            if (todayRecords.isNotEmpty()) {
                val mostRecent = todayRecords.sortedByDescending { it.timestamp }.first()
                item {
                    SectionHeader(
                        title = "今天的记录",
                        trailing = "最近 ${recentSummary(record = mostRecent)}",
                    )
                }
            }

            if (todayRecords.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "今天还没有记录",
                        message = "喝完顺手记一下，统计会更准确。",
                        actionLabel = "添加记录",
                        onAction = { showAddDialog = true },
                    )
                }
            } else {
                items(todayRecords.sortedByDescending { it.timestamp }, key = { it.id }) { record ->
                    TodayRecordCard(record = record)
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
}

@Composable
private fun TodayOverview(
    todaySpend: Double,
    todayCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MetricCard(
            label = "今日花费",
            value = "¥%.1f".format(todaySpend),
            modifier = Modifier.weight(1f),
            valueColor = MiuixTheme.colorScheme.primary,
        )
        MetricCard(
            label = "今日杯数",
            value = "$todayCount",
            modifier = Modifier.weight(1f),
            icon = Icons.Default.DateRange,
        )
    }
}

private fun recentSummary(record: MilkTeaRecord): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = timeFormat.format(Date(record.timestamp))
    return buildString {
        append(record.brand)
        append(" · ")
        append(time)
    }
}

@Composable
private fun TodayRecordCard(record: MilkTeaRecord) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = record.brand,
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Text(
                    text = "¥%.1f".format(record.price),
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.primary,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = record.drinkName ?: "未填写饮品",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = timeFormat.format(Date(record.timestamp)),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}
