package com.mason.milkteastatistics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mason.milkteastatistics.ui.components.AppTopBar
import com.mason.milkteastatistics.ui.components.EmptyStateCard
import com.mason.milkteastatistics.ui.components.SectionHeader

@Composable
fun SettingsScreen(viewModel: MilkTeaViewModel) {
    val commonBrands by viewModel.commonBrands.collectAsStateWithLifecycle()
    var newBrand by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "设置",
                subtitle = "维护常用品牌，让添加记录更快",
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
            SectionHeader(
                title = "常用品牌",
                trailing = "${commonBrands.size} 个",
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                ) {
                    OutlinedTextField(
                        value = newBrand,
                        onValueChange = { newBrand = it },
                        label = { Text("输入品牌名称") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            if (newBrand.isNotBlank()) {
                                viewModel.addCommonBrand(newBrand.trim())
                                newBrand = ""
                            }
                        },
                        enabled = newBrand.isNotBlank(),
                    ) {
                        Text("添加")
                    }
                }
            }

            if (commonBrands.isEmpty()) {
                EmptyStateCard(
                    title = "还没有常用品牌",
                    message = "添加后会出现在记录弹窗里，常喝品牌可以一键选择。",
                )
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        commonBrands.forEachIndexed { index, cb ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cb.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "添加记录时可快速选择",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { viewModel.removeCommonBrand(cb.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "删除 ${cb.name}",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                            if (index < commonBrands.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
