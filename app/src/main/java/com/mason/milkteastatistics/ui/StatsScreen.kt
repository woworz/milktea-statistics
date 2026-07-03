package com.mason.milkteastatistics.ui

import com.mason.milkteastatistics.model.DateRange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.mason.milkteastatistics.data.DailyStats
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "统计",
                    style = MiuixTheme.textStyles.title3,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(0.dp))

            // 日期范围 - 用 Button 替代 SegmentedButton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DateRange.entries.forEach { range ->
                    val selected = selectedDateRange == range
                    Button(
                        onClick = { viewModel.setDateRange(range) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = if (selected) "✓ ${range.label}" else range.label,
                            style = MiuixTheme.textStyles.body2,
                            color = if (selected) {
                                MiuixTheme.colorScheme.primary
                            } else {
                                MiuixTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }

            // 品牌筛选 - 用 Button 替代 FilterChip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    val selected = selectedBrand == null
                    Button(
                        onClick = { viewModel.setBrandFilter(null) },
                    ) {
                        Text(
                            text = if (selected) "✓ 全部" else "全部",
                            color = if (selected) {
                                MiuixTheme.colorScheme.primary
                            } else {
                                MiuixTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
                items(allBrands) { brand ->
                    val selected = selectedBrand == brand
                    Button(
                        onClick = { viewModel.setBrandFilter(brand) },
                    ) {
                        Text(
                            text = if (selected) "✓ $brand" else brand,
                            color = if (selected) {
                                MiuixTheme.colorScheme.primary
                            } else {
                                MiuixTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }

            // 统计卡片
            StatsRow(stats = stats)

            // 图表类型切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ChartMetric.entries.forEach { m ->
                        val selected = chartMetric == m
                        Button(
                            onClick = { chartMetric = m },
                        ) {
                            Text(
                                text = if (selected) "✓ ${m.label}" else m.label,
                                style = MiuixTheme.textStyles.body2,
                                color = if (selected) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { useLineChart = false },
                    ) {
                        Text(
                            text = if (!useLineChart) "✓ 柱状" else "柱状",
                            style = MiuixTheme.textStyles.body2,
                            color = if (!useLineChart) {
                                MiuixTheme.colorScheme.primary
                            } else {
                                MiuixTheme.colorScheme.onSurface
                            },
                        )
                    }
                    Button(
                        onClick = { useLineChart = true },
                    ) {
                        Text(
                            text = if (useLineChart) "✓ 折线" else "折线",
                            style = MiuixTheme.textStyles.body2,
                            color = if (useLineChart) {
                                MiuixTheme.colorScheme.primary
                            } else {
                                MiuixTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }

            // 图表
            if (dailyAggregates.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "该时间段暂无数据",
                        modifier = Modifier.padding(24.dp),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatsRow(stats: DailyStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatItem(
            label = "总花费",
            value = "¥%.1f".format(stats.totalSpend),
            modifier = Modifier.weight(1f),
        )
        StatItem(
            label = "杯数",
            value = "${stats.totalCount}",
            modifier = Modifier.weight(1f),
        )
        StatItem(
            label = "均价",
            value = "¥%.1f".format(stats.avgPrice),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}
