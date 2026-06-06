package com.mason.milkteastatistics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mason.milkteastatistics.data.DailySummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

enum class ChartMetric(val label: String) {
    COUNT("杯数"),
    SPEND("金额"),
}

@Composable
fun TrendChart(
    dailyData: List<DailySummary>,
    metric: ChartMetric,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
) {
    if (dailyData.isEmpty()) return

    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val dateFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(start = 40.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
    ) {
        val chartWidth = size.width
        val chartHeight = size.height
        val maxValue = dailyData.maxOf {
            if (metric == ChartMetric.COUNT) it.count.toFloat() else it.totalSpend.toFloat()
        }
        val yMax = if (maxValue <= 0f) 1f else ceil(maxValue * 1.2f)
        val barCount = dailyData.size
        val barWidth = chartWidth / barCount * 0.6f
        val gap = chartWidth / barCount * 0.4f

        // Y axis grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = chartHeight * (1f - i.toFloat() / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 1f,
            )
            // Y axis label
            val labelValue = (yMax * i / gridLines).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                "$labelValue",
                -36.dp.toPx(),
                y + 4.sp.toPx(),
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.LEFT
                },
            )
        }

        // Bars
        dailyData.forEachIndexed { index, summary ->
            val value = if (metric == ChartMetric.COUNT) {
                summary.count.toFloat()
            } else {
                summary.totalSpend.toFloat()
            }
            val barHeight = (value / yMax) * chartHeight
            val x = index * (barWidth + gap) + gap / 2

            // Bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, chartHeight - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            )

            // X axis label
            val label = dateFormat.format(Date(summary.dayStart))
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x + barWidth / 2,
                chartHeight + 16.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 9.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                },
            )
        }
    }
}

@Composable
fun TrendLineChart(
    dailyData: List<DailySummary>,
    metric: ChartMetric,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    dotColor: Color = MaterialTheme.colorScheme.primary,
) {
    if (dailyData.isEmpty()) return

    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val dateFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(start = 40.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
    ) {
        val chartWidth = size.width
        val chartHeight = size.height
        val maxValue = dailyData.maxOf {
            if (metric == ChartMetric.COUNT) it.count.toFloat() else it.totalSpend.toFloat()
        }
        val yMax = if (maxValue <= 0f) 1f else ceil(maxValue * 1.2f)
        val count = dailyData.size
        val stepX = if (count > 1) chartWidth / (count - 1) else chartWidth

        // Grid
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = chartHeight * (1f - i.toFloat() / gridLines)
            drawLine(gridColor, Offset(0f, y), Offset(chartWidth, y), 1f)
            val labelValue = (yMax * i / gridLines).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                "$labelValue",
                -36.dp.toPx(),
                y + 4.sp.toPx(),
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.LEFT
                },
            )
        }

        // Build path & draw dots
        val path = Path()
        val points = dailyData.mapIndexed { index, summary ->
            val value = if (metric == ChartMetric.COUNT) {
                summary.count.toFloat()
            } else {
                summary.totalSpend.toFloat()
            }
            val x = if (count > 1) index * stepX.toFloat() else chartWidth / 2f
            val y = chartHeight - (value / yMax) * chartHeight
            Offset(x, y)
        }

        if (points.isNotEmpty()) {
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            drawPath(path, color = lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))

            points.forEachIndexed { index, point ->
                drawCircle(dotColor, radius = 4.dp.toPx(), center = point)
                val label = dateFormat.format(Date(dailyData[index].dayStart))
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    point.x,
                    chartHeight + 16.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.hashCode()
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    },
                )
            }
        }
    }
}
