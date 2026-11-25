package com.clipboardmanager.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.clipboardmanager.app.ui.theme.AppColors

@Composable
fun PushPinGraphic(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Soft cast shadow onto the sticky note
        drawOval(
            color = Color.Black.copy(alpha = 0.16f),
            topLeft = Offset(w * 0.18f, h * 0.64f),
            size = androidx.compose.ui.geometry.Size(w * 0.48f, h * 0.24f)
        )

        rotate(degrees = 26f, pivot = Offset(w * 0.56f, h * 0.44f)) {
            // Metallic pin needle
            val needlePath = Path().apply {
                moveTo(w * 0.47f, h * 0.58f)
                lineTo(w * 0.53f, h * 0.58f)
                lineTo(w * 0.50f, h * 0.94f)
                close()
            }
            drawPath(
                path = needlePath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE6ECF2), Color(0xFF7D8792)),
                    start = Offset(w * 0.46f, h * 0.58f),
                    end = Offset(w * 0.54f, h * 0.94f)
                )
            )

            // Bottom red collar
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.PinRed, AppColors.PinRedDark),
                    center = Offset(w * 0.48f, h * 0.52f),
                    radius = w * 0.34f
                ),
                topLeft = Offset(w * 0.22f, h * 0.43f),
                size = androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.20f)
            )

            // Center stem
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(AppColors.PinRedDark, AppColors.PinRed, Color(0xFFF2554B))
                ),
                topLeft = Offset(w * 0.36f, h * 0.27f),
                size = androidx.compose.ui.geometry.Size(w * 0.28f, h * 0.24f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f)
            )

            // Top glossy cap
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppColors.PinRedHighlight,
                        AppColors.PinRed,
                        AppColors.PinRedDark
                    ),
                    center = Offset(w * 0.43f, h * 0.22f),
                    radius = w * 0.36f
                ),
                topLeft = Offset(w * 0.19f, h * 0.12f),
                size = androidx.compose.ui.geometry.Size(w * 0.62f, h * 0.23f)
            )

            // Specular highlight dot
            drawOval(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(w * 0.31f, h * 0.16f),
                size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.07f)
            )
        }
    }
}
