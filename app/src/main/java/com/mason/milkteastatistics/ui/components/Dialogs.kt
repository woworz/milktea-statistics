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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
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
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showManageBrands by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑记录" else "添加奶茶记录") },
        text = {
            Column {
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
                            .clickable { showDatePicker = true },
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
                            .clickable { showTimePicker = true },
                    ) {
                        Text(
                            text = timeFormat.format(Date(selectedTimestamp)),
                            modifier = Modifier.padding(12.dp),
                            style = MiuixTheme.textStyles.body2,
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
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )

    if (showDatePicker) {
        MilkTeaDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
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

    if (showTimePicker) {
        MilkTeaTimePickerDialog(
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

// ==================== 日期选择器 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MilkTeaDatePickerDialog(
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
            }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    ) {
        DatePicker(state = state)
    }
}

// ==================== 时间选择器 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MilkTeaTimePickerDialog(
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
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(state.hour, state.minute)
            }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
