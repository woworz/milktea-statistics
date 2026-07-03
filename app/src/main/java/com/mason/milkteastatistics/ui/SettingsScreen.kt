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
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
                    TextField(
                        value = newBrand,
                        onValueChange = { newBrand = it },
                        label = "输入品牌名称",
                        useLabelAsPlaceholder = true,
                        modifier = Modifier.weight(1f),
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
                                        style = MiuixTheme.textStyles.body1,
                                        color = MiuixTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "添加记录时可快速选择",
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                                IconButton(onClick = { viewModel.removeCommonBrand(cb.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "删除 ${cb.name}",
                                        tint = MiuixTheme.colorScheme.error,
                                    )
                                }
                            }
                            if (index < commonBrands.lastIndex) {
                                HorizontalDivider(
                                    color = MiuixTheme.colorScheme.outline,
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
