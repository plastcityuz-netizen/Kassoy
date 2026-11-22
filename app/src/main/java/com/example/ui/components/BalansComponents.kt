package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.data.CashFlowPoint
import com.example.data.ExpenseSlice
import com.example.ui.theme.LocalBalansPalette
import com.example.ui.theme.TabularNumberStyle
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Brand Signature Motif: "Balans chizig'i"
 * Fine horizontal balance line with a glowing emerald center dot.
 * Level when balanced; tilts gently under risk.
 */
@Composable
fun BalansLineBeam(
    modifier: Modifier = Modifier,
    tiltDegrees: Float = 0f,
    animatePulse: Boolean = true
) {
    val palette = LocalBalansPalette.current
    val transition = rememberInfiniteTransition(label = "balans_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val animatedTilt by animateFloatAsState(
        targetValue = tiltDegrees,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "tilt_anim"
    )
    val effectivePulse = if (animatePulse) pulse else 1f

    Canvas(modifier = modifier.height(22.dp)) {
        val midY = size.height / 2f
        val midX = size.width / 2f
        val rad = Math.toRadians(animatedTilt.toDouble())
        val dy = (sin(rad) * (size.width * 0.42f)).toFloat()

        val start = Offset(0f, midY - dy)
        val end = Offset(size.width, midY + dy)

        drawLine(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    palette.cyan.copy(alpha = 0.5f),
                    palette.emerald,
                    palette.cyan.copy(alpha = 0.5f),
                    Color.Transparent
                )
            ),
            start = start,
            end = end,
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(
            color = palette.emerald.copy(alpha = 0.25f),
            radius = 9.dp.toPx() * effectivePulse,
            center = Offset(midX, midY)
        )
        drawCircle(
            color = palette.emerald,
            radius = 4.dp.toPx(),
            center = Offset(midX, midY)
        )
    }
}

/**
 * Minimal "B" logo mark — two balanced debit/credit halves with glowing emerald midpoint.
 */
@Composable
fun BalansLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val palette = LocalBalansPalette.current
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(palette.surface, palette.elevated)
                )
            )
            .border(1.dp, palette.border, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val w = this.size.width
            val h = this.size.height
            val stroke = 2.2.dp.toPx()
            // Left Debit pillar
            drawLine(
                color = palette.textPrimary,
                start = Offset(w * 0.22f, h * 0.14f),
                end = Offset(w * 0.22f, h * 0.86f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            // Right Credit arcs
            drawArc(
                color = palette.cyan,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(w * 0.25f, h * 0.14f),
                size = Size(w * 0.55f, h * 0.36f),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = palette.cyan,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(w * 0.25f, h * 0.50f),
                size = Size(w * 0.58f, h * 0.36f),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // Horizontal Balans Line + Glowing Dot
            drawLine(
                color = palette.emerald,
                start = Offset(0f, h * 0.5f),
                end = Offset(w, h * 0.5f),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = palette.emerald,
                radius = 3.2.dp.toPx(),
                center = Offset(w * 0.5f, h * 0.5f)
            )
        }
    }
}

/**
 * Subtle geometric girih/islimi-inspired vector backdrop (4% opacity) for hero/footer zones.
 */
@Composable
fun GirihPatternBackdrop(modifier: Modifier = Modifier) {
    val palette = LocalBalansPalette.current
    Canvas(modifier = modifier) {
        val step = 64.dp.toPx()
        val lineColor = palette.emerald.copy(alpha = if (palette.isDark) 0.045f else 0.055f)
        var x = 0f
        while (x < size.width + step) {
            var y = 0f
            while (y < size.height + step) {
                val r = step * 0.38f
                val path = Path().apply {
                    moveTo(x, y - r)
                    lineTo(x + r, y)
                    lineTo(x, y + r)
                    lineTo(x - r, y)
                    close()
                }
                drawPath(path, color = lineColor, style = Stroke(width = 1.dp.toPx()))
                y += step
            }
            x += step
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accentBorder: Color? = null,
    cornerRadius: Dp = 16.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalBalansPalette.current
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(palette.glassStart, palette.glassEnd)
                )
            )
            .background(palette.surface.copy(alpha = if (palette.isDark) 0.82f else 0.88f))
            .border(
                width = 1.dp,
                color = accentBorder ?: palette.border,
                shape = shape
            )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun SolidCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalBalansPalette.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun MetricDeltaBadge(
    deltaPercent: Double,
    modifier: Modifier = Modifier
) {
    val palette = LocalBalansPalette.current
    val isPositive = deltaPercent >= 0.0
    val color = if (isPositive) palette.success else palette.danger
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text = Formatters.formatDeltaPercent(deltaPercent),
            style = MaterialTheme.typography.labelSmall.merge(TabularNumberStyle),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BalansScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val palette = LocalBalansPalette.current
    val clamped = score.coerceIn(0, 100)
    val animatedSweep by animateFloatAsState(
        targetValue = (clamped / 100f) * 180f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "gauge_sweep"
    )
    val gaugeColor = when {
        clamped >= 75 -> palette.emerald
        clamped >= 55 -> palette.warning
        else -> palette.danger
    }

    Box(
        modifier = modifier.size(width = 196.dp, height = 112.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            val strokeW = 12.dp.toPx()
            val arcSize = Size(size.width, size.height * 2f - strokeW)
            val topLeft = Offset(0f, strokeW / 2f)

            drawArc(
                color = palette.border,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.horizontalGradient(listOf(palette.cyan, gaugeColor)),
                startAngle = 180f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Glowing tip indicator
            val angleRad = Math.toRadians((180f + animatedSweep).toDouble())
            val rx = arcSize.width / 2f
            val ry = arcSize.height / 2f
            val cx = topLeft.x + rx + (cos(angleRad) * rx).toFloat()
            val cy = topLeft.y + ry + (sin(angleRad) * ry).toFloat()

            drawCircle(
                color = gaugeColor.copy(alpha = 0.28f),
                radius = 12.dp.toPx(),
                center = Offset(cx, cy)
            )
            drawCircle(
                color = gaugeColor,
                radius = 5.5.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$clamped",
                style = MaterialTheme.typography.displayLarge.merge(TabularNumberStyle),
                color = palette.textPrimary
            )
            Text(
                text = "/ 100",
                style = MaterialTheme.typography.labelMedium,
                color = palette.textMuted
            )
        }
    }
}

@Composable
fun CashFlowAreaChart(
    points: List<CashFlowPoint>,
    modifier: Modifier = Modifier
) {
    val palette = LocalBalansPalette.current
    var selectedIdx by remember(points) { mutableIntStateOf((points.size - 1).coerceAtLeast(0)) }

    if (points.isEmpty()) return

    val maxVal = points.maxOf { maxOf(it.inflowUzs, it.outflowUzs) }.coerceAtLeast(1L).toFloat()
    val selected = points.getOrNull(selectedIdx)

    Column(modifier = modifier) {
        if (selected != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textMuted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "+${Formatters.formatUzs(selected.inflowUzs, compact = true)}",
                        style = MaterialTheme.typography.labelMedium.merge(TabularNumberStyle),
                        color = palette.emeraldText
                    )
                    Text(
                        text = "-${Formatters.formatUzs(selected.outflowUzs, compact = true)}",
                        style = MaterialTheme.typography.labelMedium.merge(TabularNumberStyle),
                        color = palette.danger
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        if (points.size > 1) {
                            val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                            selectedIdx = (ratio * (points.size - 1)).roundToInt().coerceIn(0, points.lastIndex)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val stepX = if (points.size > 1) w / (points.size - 1) else w

            // Subtle grid lines
            for (g in 1..3) {
                val gy = h * (g / 4f)
                drawLine(
                    color = palette.border,
                    start = Offset(0f, gy),
                    end = Offset(w, gy),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val inflowPath = Path()
            val areaPath = Path()
            val outflowPath = Path()

            points.forEachIndexed { i, pt ->
                val x = i * stepX
                val yIn = h - (pt.inflowUzs / maxVal) * (h * 0.82f) - h * 0.08f
                val yOut = h - (pt.outflowUzs / maxVal) * (h * 0.82f) - h * 0.08f
                if (i == 0) {
                    inflowPath.moveTo(x, yIn)
                    areaPath.moveTo(x, h)
                    areaPath.lineTo(x, yIn)
                    outflowPath.moveTo(x, yOut)
                } else {
                    val prevX = (i - 1) * stepX
                    val midX = (prevX + x) / 2f
                    val prevYIn = h - (points[i - 1].inflowUzs / maxVal) * (h * 0.82f) - h * 0.08f
                    val prevYOut = h - (points[i - 1].outflowUzs / maxVal) * (h * 0.82f) - h * 0.08f
                    inflowPath.cubicTo(midX, prevYIn, midX, yIn, x, yIn)
                    areaPath.cubicTo(midX, prevYIn, midX, yIn, x, yIn)
                    outflowPath.cubicTo(midX, prevYOut, midX, yOut, x, yOut)
                }
                if (i == points.lastIndex) {
                    areaPath.lineTo(x, h)
                    areaPath.close()
                }
            }

            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    listOf(palette.emerald.copy(alpha = 0.28f), Color.Transparent)
                )
            )
            drawPath(
                path = inflowPath,
                color = palette.emerald,
                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = outflowPath,
                color = palette.cyan.copy(alpha = 0.8f),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Active point crosshair
            val selPt = points.getOrNull(selectedIdx)
            if (selPt != null) {
                val sx = selectedIdx * stepX
                val sy = h - (selPt.inflowUzs / maxVal) * (h * 0.82f) - h * 0.08f
                drawLine(
                    color = palette.textMuted.copy(alpha = 0.4f),
                    start = Offset(sx, 0f),
                    end = Offset(sx, h),
                    strokeWidth = 1.dp.toPx()
                )
                drawCircle(color = palette.emerald.copy(alpha = 0.3f), radius = 8.dp.toPx(), center = Offset(sx, sy))
                drawCircle(color = palette.emerald, radius = 4.dp.toPx(), center = Offset(sx, sy))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted
                )
            }
        }
    }
}

@Composable
fun ExpenseDonutChart(
    slices: List<ExpenseSlice>,
    modifier: Modifier = Modifier
) {
    val palette = LocalBalansPalette.current
    val sliceColors = listOf(
        palette.emerald,
        palette.cyan,
        palette.violet,
        palette.warning,
        palette.danger
    )
    if (slices.isEmpty()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(116.dp)) {
            val stroke = 18.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            var startAngle = -90f
            val total = slices.sumOf { it.amountUzs }.coerceAtLeast(1L).toFloat()
            slices.forEachIndexed { idx, slice ->
                val sweep = (slice.amountUzs / total) * 360f
                drawArc(
                    color = sliceColors[idx % sliceColors.size],
                    startAngle = startAngle,
                    sweepAngle = (sweep - 3f).coerceAtLeast(2f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                startAngle += sweep
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEachIndexed { idx, slice ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(sliceColors[idx % sliceColors.size])
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = slice.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = Formatters.formatPercent(slice.sharePercent),
                        style = MaterialTheme.typography.labelMedium.merge(TabularNumberStyle),
                        color = palette.textMuted
                    )
                }
            }
        }
    }
}

@Composable
fun SkeletonLoadingState(
    label: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalBalansPalette.current
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skeleton_alpha"
    )
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BalansLineBeam(modifier = Modifier.fillMaxWidth())
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textMuted
        )
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.elevated.copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalBalansPalette.current
    SolidCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Inbox,
                contentDescription = title,
                tint = palette.emeraldText,
                modifier = Modifier.size(36.dp)
            )
            BalansLineBeam(modifier = Modifier.width(140.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted
            )
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.emerald,
                        contentColor = palette.onEmerald
                    ),
                    modifier = Modifier.testTag("empty_state_action")
                ) {
                    Text(actionLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ErrorStateCard(
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalBalansPalette.current
    SolidCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = message,
                tint = palette.danger
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary
                )
            }
            TextButton(onClick = onRetry) {
                Text(retryLabel, color = palette.emeraldText)
            }
        }
    }
}
