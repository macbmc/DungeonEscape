package com.dungeonescape.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dungeonescape.models.Vector2D
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    onMove: (Vector2D) -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Movement joystick" }
            .pointerInput(Unit) {
                val radius = this.size.width / 2f
                detectDragGestures(
                    onDragStart = { offset ->
                        val localOffset = offset - Offset(radius, radius)
                        val dist = sqrt(localOffset.x * localOffset.x + localOffset.y * localOffset.y)
                        val clampedOffset = if (dist > radius) {
                            localOffset * (radius / dist)
                        } else {
                            localOffset
                        }
                        knobOffset = clampedOffset
                        val normX = (clampedOffset.x / radius).coerceIn(-1f, 1f)
                        val normY = (clampedOffset.y / radius).coerceIn(-1f, 1f)
                        onMove(Vector2D(normX, normY))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        val clampedOffset = if (dist > radius) {
                            newOffset * (radius / dist)
                        } else {
                            newOffset
                        }
                        knobOffset = clampedOffset
                        val normX = (clampedOffset.x / radius).coerceIn(-1f, 1f)
                        val normY = (clampedOffset.y / radius).coerceIn(-1f, 1f)
                        onMove(Vector2D(normX, normY))
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(Vector2D.ZERO)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(Vector2D.ZERO)
                    }
                )
            }
    ) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val baseRadius = this.size.width / 2f
        val knobRadius = baseRadius * 0.38f

        // Outer base circle
        drawCircle(
            color = Color(0x33FFFFFF),
            radius = baseRadius,
            center = center
        )
        drawCircle(
            color = Color(0x88FFFFFF),
            radius = baseRadius,
            center = center,
            style = Stroke(width = 3f)
        )

        // Inner directional cross
        val tick = baseRadius * 0.25f
        drawLine(Color(0x44FFFFFF), center - Offset(tick, 0f), center + Offset(tick, 0f), strokeWidth = 2f)
        drawLine(Color(0x44FFFFFF), center - Offset(0f, tick), center + Offset(0f, tick), strokeWidth = 2f)

        // Movable thumbstick knob
        val knobCenter = center + knobOffset
        drawCircle(
            color = Color(0x9900E5FF),
            radius = knobRadius,
            center = knobCenter
        )
        drawCircle(
            color = Color(0xFFE0F7FA),
            radius = knobRadius,
            center = knobCenter,
            style = Stroke(width = 3f)
        )
    }
}
