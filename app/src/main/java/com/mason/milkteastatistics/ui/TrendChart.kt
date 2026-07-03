package com.mason.milkteastatistics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mason.milkteastatistics.data.DailySummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil

enum class ChartMetric(val label: String) {
    COUNT("杯数"),
    SPEND("金额"),
}

@Composable
fun TrendChart(
    dailyData: List<DailySummary>,
    metric: ChartMetric,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.secondary,
) {
    if (dailyData.isEmpty()) return

    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val dateFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }
    
    // Gradient colors for bar chart
    val gradientTopColor = MaterialTheme.colorScheme.secondary // Matcha green
    val gradientBottomColor = MaterialTheme.colorScheme.secondaryContainer // Light matcha
    
    // Touch interaction state
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var tooltipPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Fixed chart dimensions (matching Canvas padding)
    val chartLeftPadding = 40.dp
    val chartTopPadding = 16.dp
    val chartBottomPadding = 32.dp
    val chartRightPadding = 16.dp
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(start = chartLeftPadding, end = chartRightPadding, top = chartTopPadding, bottom = chartBottomPadding)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Calculate chart area
                        val chartWidth = size.width
                        val chartHeight = size.height
                        val barCount = dailyData.size
                        val barWidth = chartWidth / barCount * 0.6f
                        val gap = chartWidth / barCount * 0.4f
                        
                        // Find nearest bar
                        var nearestIndex: Int? = null
                        var minDistance = Float.MAX_VALUE
                        
                        dailyData.forEachIndexed { index, _ ->
                            val barX = index * (barWidth + gap) + gap / 2
                            val barCenterX = barX + barWidth / 2
                            val distance = abs(tapOffset.x - barCenterX)
                            
                            if (distance < minDistance && distance < barWidth) {
                                minDistance = distance
                                nearestIndex = index
                            }
                        }
                        
                        if (nearestIndex != null) {
                            selectedPointIndex = nearestIndex
                            tooltipPosition = tapOffset
                        } else {
                            selectedPointIndex = null
                        }
                    }
                },
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
                        color = textColor.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.LEFT
                        isAntiAlias = true
                    },
                )
            }

            // Bars with gradient
            dailyData.forEachIndexed { index, summary ->
                val value = if (metric == ChartMetric.COUNT) {
                    summary.count.toFloat()
                } else {
                    summary.totalSpend.toFloat()
                }
                val barHeight = (value / yMax) * chartHeight
                val x = index * (barWidth + gap) + gap / 2

                // Draw bar with gradient
                val gradient = Brush.verticalGradient(
                    colors = listOf(gradientBottomColor, gradientTopColor),
                    startY = chartHeight,
                    endY = chartHeight - barHeight,
                )
                
                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                )

                // X axis label
                val label = dateFormat.format(Date(summary.dayStart))
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x + barWidth / 2,
                    chartHeight + 16.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.toArgb()
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    },
                )
            }
        }
        
        // Tooltip overlay
        selectedPointIndex?.let { index ->
            val summary = dailyData[index]
            val value = if (metric == ChartMetric.COUNT) {
                "${summary.count}杯"
            } else {
                "¥%.1f".format(summary.totalSpend)
            }
            val dateLabel = dateFormat.format(Date(summary.dayStart))
            
            Surface(
                modifier = Modifier
                    .offset { 
                        // Tooltip position relative to Box (with padding offset)
                        val x = (tooltipPosition.x + chartLeftPadding.toPx()).toInt().coerceIn(0, 800)
                        val y = (tooltipPosition.y + chartTopPadding.toPx() - 60).toInt().coerceAtLeast(0)
                        IntOffset(x - 60, y)
                    },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 4.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }
        }
    }
}

@Composable
fun TrendLineChart(
    dailyData: List<DailySummary>,
    metric: ChartMetric,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.secondary,
    dotColor: Color = MaterialTheme.colorScheme.secondary,
) {
    if (dailyData.isEmpty()) return

    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val dateFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }
    
    // Touch interaction state
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var tooltipPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Fixed chart dimensions (matching Canvas padding)
    val chartLeftPadding = 40.dp
    val chartTopPadding = 16.dp
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(start = chartLeftPadding, end = 16.dp, top = chartTopPadding, bottom = 32.dp)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val chartWidth = size.width
                        val chartHeight = size.height
                        val count = dailyData.size
                        val stepX = if (count > 1) chartWidth / (count - 1) else chartWidth
                        
                        // Find nearest point
                        var nearestIndex: Int? = null
                        var minDistance = Float.MAX_VALUE
                        
                        dailyData.forEachIndexed { index, summary ->
                            val value = if (metric == ChartMetric.COUNT) {
                                summary.count.toFloat()
                            } else {
                                summary.totalSpend.toFloat()
                            }
                            val maxValue = dailyData.maxOf {
                                if (metric == ChartMetric.COUNT) it.count.toFloat() else it.totalSpend.toFloat()
                            }
                            val yMax = if (maxValue <= 0f) 1f else ceil(maxValue * 1.2f)
                            
                            val x = if (count > 1) index * stepX.toFloat() else chartWidth / 2f
                            val y = chartHeight - (value / yMax) * chartHeight
                            
                            val distance = abs(tapOffset.x - x) + abs(tapOffset.y - y)
                            
                            if (distance < minDistance && distance < 50) {
                                minDistance = distance
                                nearestIndex = index
                            }
                        }
                        
                        if (nearestIndex != null) {
                            selectedPointIndex = nearestIndex
                            tooltipPosition = tapOffset
                        } else {
                            selectedPointIndex = null
                        }
                    }
                },
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
                        color = textColor.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.LEFT
                        isAntiAlias = true
                    },
                )
            }

            // Build points
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
                // Create smooth curve path using cubic Bézier
                val curvePath = Path()
                curvePath.moveTo(points[0].x, points[0].y)
                
                if (points.size > 1) {
                    for (i in 1 until points.size) {
                        val prev = points[maxOf(0, i - 1)]
                        val curr = points[i]
                        val next = points[minOf(points.size - 1, i + 1)]
                        val prevPrev = points[maxOf(0, i - 2)]
                        
                        val tension = 0.3f
                        val cp1x = prev.x + (curr.x - prevPrev.x) * tension
                        val cp1y = prev.y + (curr.y - prevPrev.y) * tension
                        val cp2x = curr.x - (next.x - prev.x) * tension
                        val cp2y = curr.y - (next.y - prev.y) * tension
                        
                        curvePath.cubicTo(cp1x, cp1y, cp2x, cp2y, curr.x, curr.y)
                    }
                }
                
                // Create fill path (curve + bottom edge)
                val fillPath = Path()
                fillPath.moveTo(points[0].x, chartHeight)
                fillPath.lineTo(points[0].x, points[0].y)
                
                if (points.size > 1) {
                    for (i in 1 until points.size) {
                        val prev = points[maxOf(0, i - 1)]
                        val curr = points[i]
                        val next = points[minOf(points.size - 1, i + 1)]
                        val prevPrev = points[maxOf(0, i - 2)]
                        
                        val tension = 0.3f
                        val cp1x = prev.x + (curr.x - prevPrev.x) * tension
                        val cp1y = prev.y + (curr.y - prevPrev.y) * tension
                        val cp2x = curr.x - (next.x - prev.x) * tension
                        val cp2y = curr.y - (next.y - prev.y) * tension
                        
                        fillPath.cubicTo(cp1x, cp1y, cp2x, cp2y, curr.x, curr.y)
                    }
                }
                
                fillPath.lineTo(points.last().x, chartHeight)
                fillPath.close()
                
                // Draw gradient fill under curve
                val fillGradient = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.3f),
                        lineColor.copy(alpha = 0.05f),
                    ),
                    startY = 0f,
                    endY = chartHeight,
                )
                drawPath(fillPath, brush = fillGradient)
                
                // Draw curve stroke
                drawPath(
                    path = curvePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx()),
                )

                // Draw dots with white border
                points.forEachIndexed { index, point ->
                    // White border
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = point,
                    )
                    // Inner dot
                    drawCircle(
                        color = dotColor,
                        radius = 5.dp.toPx(),
                        center = point,
                    )
                    
                    // X axis label
                    val label = dateFormat.format(Date(dailyData[index].dayStart))
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        point.x,
                        chartHeight + 16.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = textColor.toArgb()
                            textSize = 9.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        },
                    )
                }
            }
        }
        
        // Tooltip overlay
        selectedPointIndex?.let { index ->
            val summary = dailyData[index]
            val value = if (metric == ChartMetric.COUNT) {
                "${summary.count}杯"
            } else {
                "¥%.1f".format(summary.totalSpend)
            }
            val dateLabel = dateFormat.format(Date(summary.dayStart))
            
            Surface(
                modifier = Modifier
                    .offset { 
                        // Tooltip position relative to Box (with padding offset)
                        val x = (tooltipPosition.x + chartLeftPadding.toPx()).toInt().coerceIn(0, 800)
                        val y = (tooltipPosition.y + chartTopPadding.toPx() - 60).toInt().coerceAtLeast(0)
                        IntOffset(x - 60, y)
                    },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 4.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }
        }
    }
}
