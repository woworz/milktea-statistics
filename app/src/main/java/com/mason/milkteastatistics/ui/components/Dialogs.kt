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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mason.milkteastatistics.data.CommonBrand
import com.mason.milkteastatistics.data.MilkTeaRecord
import com.mason.milkteastatistics.data.PurchaseTemplate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedTimestamp.toPickerDateMillis(),
    )
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
                datePickerState.selectedDateMillis = selectedTimestamp.toPickerDateMillis()
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

                RecordDialogMode.Date -> DatePicker(state = datePickerState)
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
                            datePickerState.selectedDateMillis?.let { dateMillis ->
                                selectedTimestamp = selectedTimestamp.withPickerDate(dateMillis)
                            }
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

private fun Long.toPickerDateMillis(): Long {
    val localCal = Calendar.getInstance().apply { timeInMillis = this@toPickerDateMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(localCal.get(Calendar.YEAR), localCal.get(Calendar.MONTH), localCal.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun Long.withPickerDate(dateMillis: Long): Long {
    val pickerCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateMillis }
    return Calendar.getInstance().apply {
        timeInMillis = this@withPickerDate
        set(Calendar.YEAR, pickerCal.get(Calendar.YEAR))
        set(Calendar.MONTH, pickerCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, pickerCal.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
