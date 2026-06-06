package com.mason.milkteastatistics.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mason.milkteastatistics.data.MilkTeaRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ==================== 添加/编辑弹窗 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecordDialog(
    record: MilkTeaRecord?,
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

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑记录" else "添加奶茶记录") },
        text = {
            Column {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("品牌") },
                    placeholder = { Text("例如：喜茶、奈雪") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = drinkName,
                    onValueChange = { drinkName = it },
                    label = { Text("饮品（可选）") },
                    placeholder = { Text("例如：柠檬水、珍珠奶茶") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = it.isNotEmpty() && it.toDoubleOrNull() == null
                    },
                    label = { Text("价格 (¥)") },
                    placeholder = { Text("例如：25") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceError,
                    supportingText = if (priceError) {
                        { Text("请输入有效数字") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "时间",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            style = MaterialTheme.typography.bodyMedium,
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
                            style = MaterialTheme.typography.bodyMedium,
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
