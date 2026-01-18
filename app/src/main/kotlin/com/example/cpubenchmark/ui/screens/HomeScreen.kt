package com.example.cpubenchmark.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cpubenchmark.ui.MainViewModel
import com.example.cpubenchmark.ui.components.*
import com.example.cpubenchmark.ui.theme.MultiThreadColor
import com.example.cpubenchmark.ui.theme.SingleThreadColor

/**
 * 主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CPU 基準測試",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.resetResults() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "重置結果"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 設備信息卡片
            DeviceInfoCard(
                deviceName = uiState.deviceName,
                cpuCores = uiState.cpuCores,
                cpuType = uiState.cpuType
            )

            // 測試進度區域
            if (uiState.isRunning) {
                TestProgressSection(
                    progress = uiState.currentProgress,
                    currentCore = uiState.currentCore,
                    totalCores = uiState.cpuCores
                )
            }

            // 測試按鈕區域
            TestButtonsSection(
                isRunning = uiState.isRunning,
                onSingleThreadClick = { viewModel.startSingleThreadTest() },
                onMultiThreadClick = { viewModel.startMultiThreadTest() },
                onStopClick = { viewModel.stopTest() }
            )

            // 結果顯示區域
            if (uiState.hasResults) {
                ResultsSection(
                    singleThreadResult = uiState.singleThreadResult,
                    multiThreadResult = uiState.multiThreadResult,
                    showComparison = uiState.showComparison
                )
            }

            // 底部說明
            InfoSection()
        }
    }
}

/**
 * 測試進度區域
 */
@Composable
private fun TestProgressSection(
    progress: Float,
    currentCore: Int,
    totalCores: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "正在進行性能測試...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator(
                progress = progress,
                label = if (currentCore <= 1) "單線程測試" else "多線程測試",
                coreInfo = "核心 ${currentCore}/${totalCores}",
                size = 180.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CPU核心活動指示器
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CPU活動:",
                    style = MaterialTheme.typography.bodySmall
                )
                CpuCoreActivityIndicator(
                    coreCount = totalCores,
                    activeCores = if (currentCore == 0) 1 else currentCore
                )
            }
        }
    }
}

/**
 * 測試按鈕區域
 */
@Composable
private fun TestButtonsSection(
    isRunning: Boolean,
    onSingleThreadClick: () -> Unit,
    onMultiThreadClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 單線程測試按鈕
        if (isRunning) {
            Button(
                onClick = onStopClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "停止測試",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            BenchmarkButton(
                text = "單線程測試",
                icon = Icons.Default.Memory,
                backgroundColor = SingleThreadColor,
                onClick = onSingleThreadClick
            )

            BenchmarkButton(
                text = "多線程測試",
                icon = Icons.Default.ViewModule,
                backgroundColor = MultiThreadColor,
                onClick = onMultiThreadClick
            )
        }
    }
}

/**
 * 結果顯示區域
 */
@Composable
private fun ResultsSection(
    singleThreadResult: com.example.cpubenchmark.data.BenchmarkResult?,
    multiThreadResult: com.example.cpubenchmark.data.BenchmarkResult?,
    showComparison: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "測試結果",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // 單線程結果
        singleThreadResult?.let { result ->
            ResultCard(result = result)
        }

        // 多線程結果
        multiThreadResult?.let { result ->
            ResultCard(result = result)
        }

        // 對比結果
        if (showComparison) {
            ComparisonCard(
                singleThreadResult = singleThreadResult,
                multiThreadResult = multiThreadResult
            )
        }
    }
}

/**
 * 底部說明區域
 */
@Composable
private fun InfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "關於測試",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "• 單線程測試：使用單一CPU核心進行計算，評估單核性能",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "• 多線程測試：使用所有CPU核心並行計算，評估多核性能",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "• 測試內容：斐波那契數列、矩陣乘法、蒙特卡洛模擬、質數計算",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "• 加速比：多線程相對於單線程的性能提升倍數",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
