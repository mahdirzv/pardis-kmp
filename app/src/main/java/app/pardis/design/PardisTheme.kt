package app.pardis.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

private val PardisLightColorScheme = lightColorScheme(
    primary = PardisColors.indigo,
    onPrimary = PardisColors.inkOnDark,
    primaryContainer = PardisColors.indigoTint,
    onPrimaryContainer = PardisColors.indigoDeep,
    secondary = PardisColors.saffron,
    onSecondary = PardisColors.inkOnDark,
    secondaryContainer = PardisColors.saffronTint,
    onSecondaryContainer = PardisColors.saffronDeep,
    tertiary = PardisColors.lilac,
    onTertiary = PardisColors.inkOnDark,
    tertiaryContainer = PardisColors.lilacSoft,
    onTertiaryContainer = PardisColors.lilacDeep,
    background = PardisColors.background,
    onBackground = PardisColors.ink,
    surface = PardisColors.surface,
    onSurface = PardisColors.ink,
    surfaceVariant = PardisColors.surface2,
    onSurfaceVariant = PardisColors.inkSoft,
    outline = PardisColors.border,
    outlineVariant = PardisColors.borderSoft,
    error = PardisColors.error,
    errorContainer = PardisColors.surfacePeach,
    onErrorContainer = PardisColors.saffronDeep,
)

private val PardisDarkColorScheme = darkColorScheme(
    primary = PardisColors.saffron,
    onPrimary = PardisColors.indigoDarker,
    primaryContainer = PardisColors.indigo,
    onPrimaryContainer = PardisColors.inkOnDark,
    secondary = PardisColors.mint,
    onSecondary = PardisColors.indigoDarker,
    secondaryContainer = PardisColors.indigoDeep,
    onSecondaryContainer = PardisColors.inkOnDark,
    tertiary = PardisColors.lilac,
    onTertiary = PardisColors.indigoDarker,
    background = PardisColors.indigoDarker,
    onBackground = PardisColors.inkOnDark,
    surface = Color(0xFF1B1B27),
    onSurface = PardisColors.inkOnDark,
    surfaceVariant = Color(0xFF272436),
    onSurfaceVariant = Color(0xFFD3CBDD),
    outline = Color(0xFF484256),
    outlineVariant = Color(0xFF322C40),
    error = Color(0xFFFF8A80),
    errorContainer = Color(0xFF6D2C2C),
    onErrorContainer = Color(0xFFFFDAD4),
)

private val PardisTypographyScheme = Typography(
    displayLarge = TextStyle(
        fontSize = PardisTypography.xxxl.sp,
        lineHeight = (PardisTypography.xxxl * 1.02f).sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-1.2).sp,
    ),
    headlineLarge = TextStyle(
        fontSize = PardisTypography.xxl.sp,
        lineHeight = (PardisTypography.xxl * 1.08f).sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.9).sp,
    ),
    headlineMedium = TextStyle(
        fontSize = PardisTypography.xl.sp,
        lineHeight = (PardisTypography.xl * 1.08f).sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    titleLarge = TextStyle(
        fontSize = PardisTypography.lg.sp,
        lineHeight = (PardisTypography.lg * 1.18f).sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontSize = PardisTypography.base.sp,
        lineHeight = (PardisTypography.base * 1.2f).sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.1).sp,
    ),
    bodyLarge = TextStyle(
        fontSize = PardisTypography.base.sp,
        lineHeight = (PardisTypography.base * PardisTypography.lineHeightNormal).sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = PardisTypography.sm.sp,
        lineHeight = (PardisTypography.sm * 1.6f).sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontSize = PardisTypography.sm.sp,
        lineHeight = (PardisTypography.sm * 1.5f).sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontSize = PardisTypography.sm.sp,
        lineHeight = (PardisTypography.sm * 1.25f).sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontSize = PardisTypography.sm.sp,
        lineHeight = (PardisTypography.sm * 1.2f).sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.15.sp,
    ),
    labelSmall = TextStyle(
        fontSize = PardisTypography.xs.sp,
        lineHeight = (PardisTypography.xs * PardisTypography.lineHeightTight).sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.6.sp,
    ),
)

private val PardisShapes = Shapes(
    small = RoundedCornerShape(PardisRadius.sm),
    medium = RoundedCornerShape(PardisRadius.md),
    large = RoundedCornerShape(PardisRadius.lg),
)

@Composable
fun PardisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PardisDarkColorScheme else PardisLightColorScheme,
        typography = PardisTypographyScheme,
        shapes = PardisShapes,
        content = content,
    )
}

