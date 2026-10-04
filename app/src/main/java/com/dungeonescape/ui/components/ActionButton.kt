package com.dungeonescape.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionButton(
    label: String,
    subLabel: String? = null,
    baseColor: Color,
    cooldownProgress: Float = 0f, // 0.0f = ready, 1.0f = full cooldown
    size: Dp = 72.dp,
    modifier: Modifier = Modifier,
    onPress: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .semantics { contentDescription = label }
            .pointerInput(cooldownProgress) {
                detectTapGestures(
                    onPress = {
                        if (cooldownProgress <= 0f) {
                            isPressed = true
                            onPress()
                            tryAwaitRelease()
                            isPressed = false
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = this.size.width / 2f
            val center = Offset(radius, radius)
            val currentScale = if (isPressed) 0.9f else 1.0f
            val isReady = cooldownProgress <= 0.01f

            val buttonColor = if (isReady) {
                baseColor.copy(alpha = if (isPressed) 0.95f else 0.75f)
            } else {
                Color(0xFF37474F).copy(alpha = 0.6f)
            }

            // Fill
            drawCircle(
                color = buttonColor,
                radius = radius * currentScale,
                center = center
            )

            // Outer ring
            drawCircle(
                color = if (isReady) Color.White.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.5f),
                radius = radius * currentScale,
                center = center,
                style = Stroke(width = 3.5f)
            )

            // Cooldown Arc overlay if in cooldown
            if (cooldownProgress > 0f) {
                val sweep = cooldownProgress * 360f
                drawArc(
                    color = Color(0xAA000000),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                drawArc(
                    color = Color(0xFFFF5252),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius + 2f, center.y - radius + 2f),
                    size = Size((radius - 2f) * 2, (radius - 2f) * 2),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
        }

        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (cooldownProgress > 0f && subLabel != null) subLabel else label,
                color = Color.White,
                fontSize = if (label.length > 5) 12.sp else 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
