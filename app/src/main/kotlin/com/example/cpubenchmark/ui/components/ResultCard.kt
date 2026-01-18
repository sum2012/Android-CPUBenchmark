package com.example.cpubenchmark.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cpubenchmark.data.BenchmarkResult
import com.example.cpubenchmark.data.ThreadType
import com.example.cpubenchmark.ui.theme.MultiThreadColor
import com.example.cpubenchmark.ui.theme.SingleThreadColor
import com.example.cpubenchmark.ui.theme.ScoreTextStyle
import com.example.cpubenchmark.ui.theme.TimeTextStyle
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * 結果顯示卡片
 * @param result 基準測試結果
 * @param modifier 修飾符
 */
@Composable
fun ResultCard(
    result: BenchmarkResult?,
    modifier: Modifier = Modifier
) {
    if (result == null) return

    val (title, color) = when (result.threadType) {
        ThreadType.SINGLE_THREAD -> "單線程測試" to SingleThreadColor
        ThreadType.MULTI_THREAD -> "多線程測試" to MultiThreadColor
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(color)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 分數顯示（帶數字滾動動畫）
            AnimatedCounter(
                targetNumber = result.score,
                modifier = Modifier,
                textStyle = ScoreTextStyle.copy(color = color)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 標籤
            Text(
                text = "性能得分",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 詳細信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResultDetailItem(
                    label = "耗時",
                    value = "${result.timeTaken} ms",
                    color = color
                )
                ResultDetailItem(
                    label = "核心數",
                    value = "${result.cpuCoresUsed}",
                    color = color
                )
            }
        }
    }
}

/**
 * 結果詳細信息項目
 */
@Composable
private fun ResultDetailItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TimeTextStyle,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 對比結果卡片
 * 顯示單線程和多線程結果的對比
 */
@Composable
fun ComparisonCard(
    singleThreadResult: BenchmarkResult?,
    multiThreadResult: BenchmarkResult?,
    modifier: Modifier = Modifier
) {
    if (singleThreadResult == null || multiThreadResult == null) return

    val speedup = if (singleThreadResult.timeTaken > 0) {
        String.format(Locale.getDefault(), "%.2fx", multiThreadResult.timeTaken.toDouble() / singleThreadResult.timeTaken)
    } else {
        "N/A"
    }

    val efficiency = if (multiThreadResult.cpuCoresUsed > 0) {
        val rawEfficiency = (multiThreadResult.timeTaken.toDouble() / singleThreadResult.timeTaken / multiThreadResult.cpuCoresUsed) * 100
        String.format(Locale.getDefault(), "%.1f%%", rawEfficiency.coerceIn(0.0, 100.0))
    } else {
        "N/A"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "性能對比",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ComparisonItem(
                    label = "加速比",
                    value = speedup,
                    description = "相對於單線程"
                )
                ComparisonItem(
                    label = "並行效率",
                    value = efficiency,
                    description = "理論性能百分比"
                )
                ComparisonItem(
                    label = "多核利用率",
                    value = "${multiThreadResult.cpuCoresUsed} 核",
                    description = "使用的CPU核心"
                )
            }
        }
    }
}

/**
 * 對比項目組件
 */
@Composable
private fun ComparisonItem(
    label: String,
    value: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 數字滾動動畫組件
 */
@Composable
fun AnimatedCounter(
    targetNumber: Long,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = ScoreTextStyle
) {
    var animatedNumber by remember { mutableStateOf(0L) }

    LaunchedEffect(targetNumber) {
        val startNumber = animatedNumber
        val diff = targetNumber - startNumber

        if (diff != 0L) {
            val steps = 30
            val stepValue = diff.toFloat() / steps
            val stepDuration = 1000 / steps

            repeat(steps) {
                delay(stepDuration.toLong())
                animatedNumber = (startNumber + (stepValue * (it + 1)).toLong()).coerceAtMost(targetNumber)
            }
            animatedNumber = targetNumber
        }
    }

    Text(
        text = animatedNumber.toString(),
        style = textStyle,
        modifier = modifier
    )
}
