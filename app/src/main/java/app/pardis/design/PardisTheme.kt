package app.pardis.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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
    background = PardisColors.darkBackground,
    onBackground = PardisColors.darkInk,
    surface = PardisColors.darkSurface,
    onSurface = PardisColors.darkInk,
    surfaceVariant = PardisColors.darkSurface2,
    onSurfaceVariant = PardisColors.darkInkSoft,
    outline = PardisColors.darkBorderStrong,
    outlineVariant = PardisColors.darkBorder,
    error = PardisColors.errorDark,
    errorContainer = PardisColors.errorContainerDark,
    onErrorContainer = PardisColors.onErrorContainerDark,
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

object PardisComponentColors {
    val chipSelectedContainer = PardisColors.ink
    val chipSelectedContent = PardisColors.surface
    val chipContainer = PardisColors.surface
    val chipContent = PardisColors.inkSoft
    val chipBorder = PardisColors.border
    val cardContainer = PardisColors.surface
    val cardBorder = PardisColors.borderSoft
    val primaryActionContainer = PardisColors.saffron
    val primaryActionContent = PardisColors.inkOnDark
    val mediaPlaceholderContainer = PardisColors.surfaceLilac
    val mediaPlaceholderContent = PardisColors.inkSoft
}

fun Modifier.pardisScreenBackground(): Modifier = background(
    Brush.verticalGradient(
        colors = listOf(
            PardisColors.background,
            PardisColors.backgroundAlt,
        ),
    ),
)

fun Modifier.pardisCardSurface(): Modifier {
    val shape = RoundedCornerShape(PardisRadius.lg)
    return shadow(PardisShadows.md, shape)
        .background(PardisComponentColors.cardContainer, shape)
}

@Composable
fun PardisThemedSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = PardisComponentColors.cardContainer,
        shape = RoundedCornerShape(PardisRadius.lg),
        border = BorderStroke(PardisSpacing.hairline, PardisComponentColors.cardBorder),
        tonalElevation = PardisSpacing.none,
        shadowElevation = PardisShadows.sm,
        content = content,
    )
}

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
