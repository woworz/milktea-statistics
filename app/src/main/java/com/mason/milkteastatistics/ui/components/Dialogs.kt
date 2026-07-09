package com.mason.milkteastatistics.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mason.milkteastatistics.data.CommonBrand
import com.mason.milkteastatistics.data.MilkTeaRecord
import com.mason.milkteastatistics.data.PurchaseTemplate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ==================== 添加/编辑弹窗 ====================

private enum class RecordDialogMode {
    Form,
    Date,
    Time,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecordDialog(
    record: MilkTeaRecord?,
    commonBrands: List<CommonBrand> = emptyList(),
    purchaseTemplates: List<PurchaseTemplate> = emptyList(),
    onAddCommonBrand: ((String) -> Unit)? = null,
    onRemoveCommonBrand: ((Long) -> Unit)? = null,
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
    var dialogMode by remember(record) { mutableStateOf(RecordDialogMode.Form) }
    var showManageBrands by remember { mutableStateOf(false) }
    var displayedMonthMillis by remember(record) { mutableStateOf(selectedTimestamp.monthStartMillis()) }
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
            .get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
            .get(Calendar.MINUTE),
        is24Hour = true,
    )

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(dialogMode) {
        when (dialogMode) {
            RecordDialogMode.Date -> {
                displayedMonthMillis = selectedTimestamp.monthStartMillis()
            }

            RecordDialogMode.Time -> {
                val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                timePickerState.hour = cal.get(Calendar.HOUR_OF_DAY)
                timePickerState.minute = cal.get(Calendar.MINUTE)
            }

            RecordDialogMode.Form -> Unit
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (dialogMode == RecordDialogMode.Form) {
                onDismiss()
            } else {
                dialogMode = RecordDialogMode.Form
            }
        },
        title = {
            Text(
                when (dialogMode) {
                    RecordDialogMode.Form -> if (isEdit) "编辑记录" else "添加奶茶记录"
                    RecordDialogMode.Date -> "选择日期"
                    RecordDialogMode.Time -> "选择时间"
                },
            )
        },
        text = {
            when (dialogMode) {
                RecordDialogMode.Form -> Column {
                    if (purchaseTemplates.isNotEmpty()) {
                        Text(
                            text = "快速复购",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium),
                        ) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                purchaseTemplates.forEach { template ->
                                    PurchaseTemplateCard(
                                        template = template,
                                        onClick = {
                                            brand = template.brand
                                            drinkName = template.drinkName.orEmpty()
                                            price = "%.2f".format(Locale.US, template.price)
                                            priceError = false
                                        },
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // 常用品牌快捷标签
                    if (commonBrands.isNotEmpty()) {
                        Text(
                            text = "常用品牌",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp)),
                        ) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                commonBrands.forEach { cb ->
                                    Button(
                                        onClick = { brand = if (brand == cb.name) "" else cb.name },
                                    ) {
                                        Text(cb.name)
                                    }
                                }
                                if (onAddCommonBrand != null) {
                                    Button(
                                        onClick = { showManageBrands = true },
                                    ) {
                                        Text("管理", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("品牌") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = drinkName,
                        onValueChange = { drinkName = it },
                        label = { Text("饮品（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            price = it
                            priceError = it.isNotEmpty() && it.toDoubleOrNull() == null
                        },
                        label = { Text("价格 (¥)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    if (priceError) {
                        Text(
                            text = "请输入有效数字",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "时间",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dialogMode = RecordDialogMode.Date },
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
                                .clickable { dialogMode = RecordDialogMode.Time },
                        ) {
                            Text(
                                text = timeFormat.format(Date(selectedTimestamp)),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                RecordDialogMode.Date -> CompactCalendarPicker(
                    selectedTimestamp = selectedTimestamp,
                    displayedMonthMillis = displayedMonthMillis,
                    onDisplayedMonthChange = { displayedMonthMillis = it },
                    onDateSelected = { dateMillis ->
                        selectedTimestamp = selectedTimestamp.withDateFrom(dateMillis)
                    },
                )
                RecordDialogMode.Time -> TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (dialogMode) {
                        RecordDialogMode.Form -> {
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
                        }

                        RecordDialogMode.Date -> {
                            dialogMode = RecordDialogMode.Form
                        }

                        RecordDialogMode.Time -> {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                            cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cal.set(Calendar.MINUTE, timePickerState.minute)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            selectedTimestamp = cal.timeInMillis
                            dialogMode = RecordDialogMode.Form
                        }
                    }
                },
            ) {
                Text(if (dialogMode == RecordDialogMode.Form) {
                    if (isEdit) "保存" else "添加"
                } else {
                    "确定"
                })
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (dialogMode == RecordDialogMode.Form) {
                        onDismiss()
                    } else {
                        dialogMode = RecordDialogMode.Form
                    }
                },
            ) {
                Text(if (dialogMode == RecordDialogMode.Form) "取消" else "返回")
            }
        },
    )

    if (showManageBrands && onAddCommonBrand != null && onRemoveCommonBrand != null) {
        ManageBrandsDialog(
            commonBrands = commonBrands,
            onAdd = { name ->
                if (name.isNotBlank()) {
                    onAddCommonBrand(name.trim())
                }
            },
            onDelete = { onRemoveCommonBrand(it) },
            onDismiss = { showManageBrands = false },
        )
    }
}

@Composable
private fun PurchaseTemplateCard(
    template: PurchaseTemplate,
    onClick: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .width(148.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = template.brand,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = template.drinkName ?: "未填写饮品",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "¥%.1f · ${template.orderCount} 次".format(template.price),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun CompactCalendarPicker(
    selectedTimestamp: Long,
    displayedMonthMillis: Long,
    onDisplayedMonthChange: (Long) -> Unit,
    onDateSelected: (Long) -> Unit,
) {
    val monthFormat = remember { SimpleDateFormat("yyyy年M月", Locale.getDefault()) }
    val monthCalendar = remember(displayedMonthMillis) {
        Calendar.getInstance().apply {
            timeInMillis = displayedMonthMillis
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val selectedCalendar = remember(selectedTimestamp) {
        Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
    }
    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstWeekdayOffset = (monthCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val totalCells = ((firstWeekdayOffset + daysInMonth + 6) / 7) * 7
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = monthFormat.format(Date(displayedMonthMillis)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
            )
            Row {
                IconButton(
                    onClick = { onDisplayedMonthChange(displayedMonthMillis.addMonths(-1)) },
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "上个月",
                    )
                }
                IconButton(
                    onClick = { onDisplayedMonthChange(displayedMonthMillis.addMonths(1)) },
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "下个月",
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { weekday ->
                Text(
                    text = weekday,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(totalCells / 7) { weekIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { dayIndex ->
                        val cellIndex = weekIndex * 7 + dayIndex
                        val dayOfMonth = cellIndex - firstWeekdayOffset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (dayOfMonth in 1..daysInMonth) {
                                val isSelected = selectedCalendar.get(Calendar.YEAR) == monthCalendar.get(Calendar.YEAR) &&
                                    selectedCalendar.get(Calendar.MONTH) == monthCalendar.get(Calendar.MONTH) &&
                                    selectedCalendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth
                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable {
                                            onDateSelected(displayedMonthMillis.withDayOfMonth(dayOfMonth))
                                        },
                                    shape = CircleShape,
                                    color = if (isSelected) colorScheme.primary else colorScheme.surface,
                                    contentColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = dayOfMonth.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 常用品牌管理弹窗 ====================

@Composable
private fun ManageBrandsDialog(
    commonBrands: List<CommonBrand>,
    onAdd: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var newBrand by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("管理常用品牌") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = newBrand,
                        onValueChange = { newBrand = it },
                        label = { Text("品牌名称") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            onAdd(newBrand)
                            newBrand = ""
                        },
                        enabled = newBrand.isNotBlank(),
                    ) {
                        Text("添加")
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (commonBrands.isEmpty()) {
                    Text(
                        text = "还没有常用品牌\n在输入框中添加",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    commonBrands.forEach { cb ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = cb.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { onDelete(cb.id) }) {
                                Text("删除", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成") }
        },
    )
}

private fun Long.monthStartMillis(): Long =
    Calendar.getInstance().apply {
        timeInMillis = this@monthStartMillis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private fun Long.addMonths(amount: Int): Long =
    Calendar.getInstance().apply {
        timeInMillis = this@addMonths
        add(Calendar.MONTH, amount)
    }.timeInMillis

private fun Long.withDayOfMonth(dayOfMonth: Int): Long =
    Calendar.getInstance().apply {
        timeInMillis = this@withDayOfMonth
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
    }.timeInMillis

private fun Long.withDateFrom(dateMillis: Long): Long {
    val dateCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
    return Calendar.getInstance().apply {
        timeInMillis = this@withDateFrom
        set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
        set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
