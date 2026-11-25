package com.clipboardmanager.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppColors {
    val TealPrimary = Color(0xFF147D6A)
    val TealPrimaryDark = Color(0xFF0E5E4F)
    val TealSoftTint = Color(0xFFEDF5FA)
    val WarmPaperBg = Color(0xFFF6F2E9)
    val PureWhite = Color(0xFFFFFFFF)
    val WarmDivider = Color(0xFFE6E0D4)
    val ChipBorder = Color(0xFFDFD9CE)
    val TextPrimary = Color(0xFF1A1D21)
    val TextSecondary = Color(0xFF6E7680)
    val TextStrikethrough = Color(0xFF7B828C)
    val LockBlue = Color(0xFF4B89F5)
    val StarGold = Color(0xFFF2C037)
    val PinRed = Color(0xFFD93228)
    val PinRedHighlight = Color(0xFFFF7A70)
    val PinRedDark = Color(0xFFA01C14)
    val DangerRed = Color(0xFFC93B3B)
}

private val LightColors = lightColorScheme(
    primary = AppColors.TealPrimary,
    onPrimary = Color.White,
    primaryContainer = AppColors.TealSoftTint,
    onPrimaryContainer = AppColors.TealPrimaryDark,
    secondary = AppColors.LockBlue,
    onSecondary = Color.White,
    background = AppColors.WarmPaperBg,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.PureWhite,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.WarmPaperBg,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.ChipBorder,
    error = AppColors.DangerRed
)

private val AppTypography = Typography(
    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.2).sp,
        color = AppColors.TealPrimary
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 21.sp,
        color = AppColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        color = AppColors.TextPrimary
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        color = AppColors.TextSecondary
    ),
    labelMedium = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
)

@Composable
fun ClipboardManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
