package com.mason.milkteastatistics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
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
            SmallTopAppBar(
                title = "设置"
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "常用品牌",
                style = MiuixTheme.textStyles.title2,
            )

            Spacer(Modifier.height(16.dp))

            // 添加
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(Modifier.height(16.dp))

            // 列表
            if (commonBrands.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "还没有常用品牌",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            } else {
                commonBrands.forEachIndexed { index, cb ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = cb.name,
                            style = MiuixTheme.textStyles.body1,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.removeCommonBrand(cb.id) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除",
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

            Spacer(Modifier.height(16.dp))
        }
    }
}
