package com.mason.milkteastatistics.ui

import com.mason.milkteastatistics.model.DateRange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mason.milkteastatistics.data.DailyStats
import com.mason.milkteastatistics.ui.components.AppTopBar
import com.mason.milkteastatistics.ui.components.EmptyStateCard
import com.mason.milkteastatistics.ui.components.FilterPill
import com.mason.milkteastatistics.ui.components.MetricCard
import com.mason.milkteastatistics.ui.components.SectionHeader

@Composable
fun StatsScreen(viewModel: MilkTeaViewModel) {
    val selectedDateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
    val allBrands by viewModel.allBrands.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val dailyAggregates by viewModel.dailyAggregates.collectAsStateWithLifecycle()

    var chartMetric by remember { mutableStateOf(ChartMetric.COUNT) }
    var useLineChart by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "消费统计",
                subtitle = "查看花费、杯数和每日趋势",
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(0.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DateRange.entries.forEach { range ->
                    FilterPill(
                        label = range.label,
                        selected = selectedDateRange == range,
                        onClick = { viewModel.setDateRange(range) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterPill(
                        label = "全部",
                        selected = selectedBrand == null,
                        onClick = { viewModel.setBrandFilter(null) },
                    )
                }
                items(allBrands) { brand ->
                    FilterPill(
                        label = brand,
                        selected = selectedBrand == brand,
                        onClick = { viewModel.setBrandFilter(brand) },
                    )
                }
            }

            SectionHeader(title = "概览")
            StatsRow(stats = stats)

            SectionHeader(
                title = "每日趋势",
                trailing = if (dailyAggregates.isEmpty()) null else "${dailyAggregates.size} 天",
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChartMetric.entries.forEach { m ->
                        item {
                            FilterPill(
                                label = m.label,
                                selected = chartMetric == m,
                                onClick = { chartMetric = m },
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterPill(
                        label = "柱状",
                        selected = !useLineChart,
                        onClick = { useLineChart = false },
                    )
                    FilterPill(
                        label = "折线",
                        selected = useLineChart,
                        onClick = { useLineChart = true },
                    )
                }
            }

            if (dailyAggregates.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = if (chartMetric == ChartMetric.COUNT) "每日杯数" else "每日金额",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (useLineChart) {
                            TrendLineChart(
                                dailyData = dailyAggregates,
                                metric = chartMetric,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            TrendChart(
                                dailyData = dailyAggregates,
                                metric = chartMetric,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            } else {
                EmptyStateCard(
                    title = "该时间段暂无数据",
                    message = "切换时间范围或先添加记录后再查看趋势。",
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatsRow(stats: DailyStats) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricCard(
                label = "总花费",
                value = "¥%.1f".format(stats.totalSpend),
                modifier = Modifier.weight(1f),
                valueColor = MaterialTheme.colorScheme.primary,
            )
            MetricCard(
                label = "杯数",
                value = "${stats.totalCount}",
                modifier = Modifier.weight(1f),
            )
        }
        MetricCard(
            label = "平均单价",
            value = "¥%.1f".format(stats.avgPrice),
            modifier = Modifier.fillMaxWidth(),
            valueColor = MaterialTheme.colorScheme.primary,
        )
    }
}
