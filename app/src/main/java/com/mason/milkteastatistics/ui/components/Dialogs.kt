package com.mason.milkteastatistics.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.mason.milkteastatistics.data.CommonBrand
import com.mason.milkteastatistics.data.MilkTeaRecord
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
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
            RecordDialogMode.Time -> {
                val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                timePickerState.hour = cal.get(Calendar.HOUR_OF_DAY)
                timePickerState.minute = cal.get(Calendar.MINUTE)
            }

            RecordDialogMode.Form,
            RecordDialogMode.Date -> Unit
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
                    // 常用品牌快捷标签
                    if (commonBrands.isNotEmpty()) {
                        Text(
                            text = "常用品牌",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Spacer(Modifier.height(6.dp))
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
                                    Text("管理", style = MiuixTheme.textStyles.body2)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    TextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = "品牌",
                        useLabelAsPlaceholder = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = drinkName,
                        onValueChange = { drinkName = it },
                        label = "饮品（可选）",
                        useLabelAsPlaceholder = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = price,
                        onValueChange = {
                            price = it
                            priceError = it.isNotEmpty() && it.toDoubleOrNull() == null
                        },
                        label = "价格 (¥)",
                        useLabelAsPlaceholder = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (priceError) {
                        Text(
                            text = "请输入有效数字",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.error,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "时间",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
                                style = MiuixTheme.textStyles.body2,
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
                                style = MiuixTheme.textStyles.body2,
                            )
                        }
                    }
                }

                RecordDialogMode.Date -> DateAdjuster(
                    timestamp = selectedTimestamp,
                    dateFormat = dateFormat,
                    onTimestampChange = { selectedTimestamp = it },
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
private fun DateAdjuster(
    timestamp: Long,
    dateFormat: SimpleDateFormat,
    onTimestampChange: (Long) -> Unit,
) {
    val cal = remember(timestamp) { Calendar.getInstance().apply { timeInMillis = timestamp } }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = dateFormat.format(Date(timestamp)),
            style = MiuixTheme.textStyles.title3,
            color = MiuixTheme.colorScheme.onSurface,
        )
        DateAdjustRow(
            label = "年份",
            value = cal.get(Calendar.YEAR).toString(),
            onDecrease = { onTimestampChange(timestamp.addDateField(Calendar.YEAR, -1)) },
            onIncrease = { onTimestampChange(timestamp.addDateField(Calendar.YEAR, 1)) },
        )
        DateAdjustRow(
            label = "月份",
            value = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0'),
            onDecrease = { onTimestampChange(timestamp.addDateField(Calendar.MONTH, -1)) },
            onIncrease = { onTimestampChange(timestamp.addDateField(Calendar.MONTH, 1)) },
        )
        DateAdjustRow(
            label = "日期",
            value = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0'),
            onDecrease = { onTimestampChange(timestamp.addDateField(Calendar.DAY_OF_MONTH, -1)) },
            onIncrease = { onTimestampChange(timestamp.addDateField(Calendar.DAY_OF_MONTH, 1)) },
        )
    }
}

@Composable
private fun DateAdjustRow(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDecrease) { Text("-") }
            Text(
                text = value,
                style = MiuixTheme.textStyles.title3,
                color = MiuixTheme.colorScheme.onSurface,
            )
            TextButton(onClick = onIncrease) { Text("+") }
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
                    TextField(
                        value = newBrand,
                        onValueChange = { newBrand = it },
                        label = "品牌名称",
                        useLabelAsPlaceholder = true,
                        modifier = Modifier.weight(1f),
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
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
                                style = MiuixTheme.textStyles.body2,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { onDelete(cb.id) }) {
                                Text("删除", color = MiuixTheme.colorScheme.error)
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

private fun Long.addDateField(field: Int, amount: Int): Long =
    Calendar.getInstance().apply {
        timeInMillis = this@addDateField
        add(field, amount)
    }.timeInMillis
