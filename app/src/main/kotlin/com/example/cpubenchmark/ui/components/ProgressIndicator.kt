package com.example.cpubenchmark.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cpubenchmark.ui.theme.ProgressColor
import com.example.cpubenchmark.ui.theme.SingleThreadColor

/**
 * 圓形進度指示器
 * @param progress 進度（0-1）
 * @param label 標籤文字
 * @param coreInfo 核心信息
 * @param size 圓形大小
 * @param strokeWidth 線寬
 * @param progressColor 進度顏色
 * @param trackColor 軌道顏色
 */
@Composable
fun CircularProgressIndicator(
    progress: Float,
    label: String,
    coreInfo: String = "",
    size: Dp = 200.dp,
    strokeWidth: Dp = 12.dp,
    progressColor: Color = ProgressColor,
    trackColor: Color = progressColor.copy(alpha = 0.2f),
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 100),
        label = "progress"
    )

    val animatedColor by animateColorAsState(
        targetValue = if (progress >= 1f) SingleThreadColor else progressColor,
        animationSpec = tween(durationMillis = 300),
        label = "color"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = Size(
                width = this.size.width - strokeWidthPx,
                height = this.size.height - strokeWidthPx
            )
            val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)

            // 繪製軌道
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // 繪製進度
            drawArc(
                color = animatedColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (coreInfo.isNotEmpty()) {
                Text(
                    text = coreInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 帶動畫效果的進度條
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = ProgressColor,
    trackColor: Color = color.copy(alpha = 0.2f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 100),
        label = "progress"
    )

    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        color = color,
        trackColor = trackColor
    )
}

/**
 * CPU核心活動指示器
 * 顯示各核心的使用情況
 */
@Composable
fun CpuCoreActivityIndicator(
    coreCount: Int,
    activeCores: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(coreCount) { index ->
            val isActive = index < activeCores
            val infiniteTransition = rememberInfiniteTransition(label = "coreActivity")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "coreAlpha"
            )

            Canvas(
                modifier = Modifier
                    .size(12.dp)
            ) {
                drawCircle(
                    color = if (isActive) {
                        ProgressColor.copy(alpha = alpha)
                    } else {
                        ProgressColor.copy(alpha = 0.2f)
                    }
                )
            }
        }
    }
}
