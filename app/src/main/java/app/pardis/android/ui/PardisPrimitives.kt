package app.pardis.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.pardis.android.R
import app.pardis.core.model.VocabItem
import app.pardis.design.PardisComponentColors
import app.pardis.design.PardisColors
import app.pardis.design.PardisFonts
import app.pardis.design.PardisRadius
import app.pardis.design.PardisSpacing
import app.pardis.design.PardisThemedSurface
import coil.compose.AsyncImage

private val PardisIconSize = 20.dp
private val PardisStoryCoverSize = 68.dp
private val PardisFeaturedStoryCoverHeight = 156.dp
private val PardisFeaturedStoryHeroHeight = 248.dp

enum class PardisIconKind {
    Back,
    Book,
    Close,
    Download,
    Home,
    Moon,
    Play,
    Refresh,
    Search,
    Star,
    Trash,
    User,
}

@Composable
fun PardisIcon(
    icon: PardisIconKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = PardisColors.ink,
) {
    val iconModifier = if (contentDescription == null) {
        modifier
    } else {
        modifier.semantics { this.contentDescription = contentDescription }
    }

    Canvas(modifier = iconModifier.size(PardisIconSize)) {
        val stroke = Stroke(width = size.minDimension * 0.11f, cap = StrokeCap.Round)
        val thinStroke = Stroke(width = size.minDimension * 0.08f, cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        when (icon) {
            PardisIconKind.Back -> {
                drawLine(tint, start = Offset(w * 0.68f, h * 0.18f), end = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = Offset(w * 0.28f, h * 0.50f), end = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PardisIconKind.Book -> {
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.14f, h * 0.20f), end = androidx.compose.ui.geometry.Offset(w * 0.14f, h * 0.82f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.25f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.86f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.86f, h * 0.20f), end = androidx.compose.ui.geometry.Offset(w * 0.86f, h * 0.82f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.14f, h * 0.20f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.25f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.86f, h * 0.20f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.25f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
            }
            PardisIconKind.Close -> {
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.24f, h * 0.24f), end = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.24f), end = androidx.compose.ui.geometry.Offset(w * 0.24f, h * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PardisIconKind.Download -> {
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.16f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.44f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.66f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.44f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.66f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.84f), end = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PardisIconKind.Home -> {
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.48f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.20f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.20f), end = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.48f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.46f), end = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.82f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.70f, h * 0.46f), end = androidx.compose.ui.geometry.Offset(w * 0.70f, h * 0.82f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.82f), end = androidx.compose.ui.geometry.Offset(w * 0.70f, h * 0.82f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
            }
            PardisIconKind.Moon -> {
                drawArc(tint, startAngle = 92f, sweepAngle = 245f, useCenter = false, topLeft = androidx.compose.ui.geometry.Offset(w * 0.24f, h * 0.16f), size = androidx.compose.ui.geometry.Size(w * 0.58f, h * 0.70f), style = stroke)
                drawArc(tint, startAngle = 95f, sweepAngle = 190f, useCenter = false, topLeft = androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.20f), size = androidx.compose.ui.geometry.Size(w * 0.42f, h * 0.58f), style = thinStroke)
            }
            PardisIconKind.Play -> {
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.22f), end = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.34f, h * 0.22f), end = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.50f), end = androidx.compose.ui.geometry.Offset(w * 0.34f, h * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PardisIconKind.Refresh -> {
                drawArc(tint, startAngle = 25f, sweepAngle = 285f, useCenter = false, topLeft = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.18f), size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.64f), style = stroke)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.22f), end = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.48f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.22f), end = androidx.compose.ui.geometry.Offset(w * 0.52f, h * 0.26f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PardisIconKind.Search -> {
                drawCircle(tint, radius = w * 0.24f, center = androidx.compose.ui.geometry.Offset(w * 0.43f, h * 0.42f), style = stroke)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.62f), end = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PardisIconKind.Star -> {
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.16f), end = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.42f), end = androidx.compose.ui.geometry.Offset(w * 0.86f, h * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.86f, h * 0.42f), end = androidx.compose.ui.geometry.Offset(w * 0.66f, h * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.66f, h * 0.58f), end = androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.84f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.68f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.68f), end = androidx.compose.ui.geometry.Offset(w * 0.26f, h * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.26f, h * 0.84f), end = androidx.compose.ui.geometry.Offset(w * 0.34f, h * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.34f, h * 0.58f), end = androidx.compose.ui.geometry.Offset(w * 0.14f, h * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.14f, h * 0.42f), end = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.42f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.16f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            PardisIconKind.Trash -> {
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.36f), end = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.36f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.36f), end = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.82f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.64f, h * 0.36f), end = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.82f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.22f), end = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.22f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
            }
            PardisIconKind.User -> {
                drawCircle(tint, radius = w * 0.17f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.32f), style = stroke)
                drawArc(tint, startAngle = 205f, sweepAngle = 130f, useCenter = false, topLeft = androidx.compose.ui.geometry.Offset(w * 0.24f, h * 0.48f), size = androidx.compose.ui.geometry.Size(w * 0.52f, h * 0.42f), style = stroke)
            }
        }
    }
}

@Composable
fun PardisScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(PardisSpacing.xs)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = PardisColors.indigo,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PardisColors.inkSoft,
                )
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun PardisFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(PardisRadius.full),
        color = if (selected) PardisComponentColors.chipSelectedContainer else PardisComponentColors.chipContainer,
        border = BorderStroke(
            PardisSpacing.hairline,
            if (selected) PardisComponentColors.chipSelectedContainer else PardisComponentColors.chipBorder,
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = PardisSpacing.md, vertical = PardisSpacing.sm),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) PardisComponentColors.chipSelectedContent else PardisComponentColors.chipContent,
        )
    }
}

@Composable
fun PardisMetaPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(PardisRadius.full),
        color = containerColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = PardisSpacing.sm, vertical = PardisSpacing.xs),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

@Composable
fun PardisSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = PardisColors.ink,
                fontWeight = FontWeight.SemiBold,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = PardisColors.inkSoft,
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                PardisIcon(PardisIconKind.Refresh, contentDescription = null, tint = PardisColors.indigo)
                Spacer(Modifier.size(PardisSpacing.xs))
                Text(actionLabel, color = PardisColors.indigo, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun PardisMetricStrip(
    metrics: List<PardisMetric>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
    ) {
        metrics.forEach { metric ->
            PardisMetricTile(
                metric = metric,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

data class PardisTabItem(
    val label: String,
    val icon: PardisIconKind,
)

@Composable
fun PardisBottomTabBar(
    items: List<PardisTabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PardisThemedSurface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PardisSpacing.sm, vertical = PardisSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(index) }
                        .padding(vertical = PardisSpacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PardisSpacing.xxs),
                ) {
                    Surface(
                        shape = RoundedCornerShape(PardisRadius.full),
                        color = if (selected) PardisColors.saffronTint else Color.Transparent,
                    ) {
                        PardisIcon(
                            icon = item.icon,
                            contentDescription = null,
                            tint = if (selected) PardisColors.saffronDeep else PardisColors.inkMuted,
                            modifier = Modifier.padding(horizontal = PardisSpacing.sm, vertical = PardisSpacing.xxs),
                        )
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) PardisColors.saffronDeep else PardisColors.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

data class PardisMetric(
    val value: String,
    val label: String,
    val tone: PardisMetricTone,
)

enum class PardisMetricTone {
    Saffron,
    Indigo,
    Mint,
}

@Composable
private fun PardisMetricTile(
    metric: PardisMetric,
    modifier: Modifier = Modifier,
) {
    val colors = when (metric.tone) {
        PardisMetricTone.Saffron -> PardisColors.saffronTint to PardisColors.saffronDeep
        PardisMetricTone.Indigo -> PardisColors.indigoTint to PardisColors.indigoDeep
        PardisMetricTone.Mint -> PardisColors.mintSoft to PardisColors.mintDeep
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(PardisRadius.md),
        color = colors.first,
        border = BorderStroke(PardisSpacing.hairline, PardisComponentColors.cardBorder),
    ) {
        Column(
            modifier = Modifier.padding(PardisSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.xxs),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleMedium,
                color = colors.second,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelSmall,
                color = PardisColors.inkSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun PardisCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    PardisThemedSurface(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        content()
    }
}

@Composable
fun PardisPanel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(PardisSpacing.md),
    content: @Composable ColumnScope.() -> Unit,
) {
    PardisCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
            content = content,
        )
    }
}

@Composable
fun PardisRemoteImageFrame(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") placeholderText: String = "No illustration",
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(PardisRadius.md),
        color = PardisComponentColors.mediaPlaceholderContainer,
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // No real cover/illustration: render a generated Persian "scene" backdrop
            // (mirrors the Rivana prototype's CSS scenes) chosen deterministically per story.
            PardisSceneArt(seed = contentDescription, modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Generated gradient "scene" art used wherever a real cover/illustration is missing. Mirrors the
 * Rivana prototype's CSS scenes (night / dawn / sea / hills / flame). The variant is picked
 * deterministically from [seed] so a given story always shows the same scene. Colors are art
 * constants (like PardisBrushes), intentionally not tokenized.
 */
@Composable
fun PardisSceneArt(seed: String, modifier: Modifier = Modifier) {
    val variant = ((seed.hashCode() % 5) + 5) % 5
    val pattern = when (variant % 3) {
        0 -> R.drawable.pattern_paisley
        1 -> R.drawable.pattern_vine
        else -> R.drawable.pattern_rosette
    }
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            when (variant) {
            0 -> { // Night: lapis sky, saffron moon, dark hills
                drawRect(Brush.verticalGradient(listOf(Color(0xFF1A256E), Color(0xFF2436A1), Color(0xFF4F2EB5))))
                val moon = Offset(w * 0.5f, h * 0.34f)
                drawCircle(Brush.radialGradient(listOf(Color(0x66FFE9D2), Color(0x00F08A2D)), center = moon, radius = w * 0.3f), radius = w * 0.3f, center = moon)
                drawCircle(Color(0xFFF08A2D), radius = w * 0.11f, center = moon)
                val hills = Path().apply {
                    moveTo(0f, h); lineTo(0f, h * 0.72f); lineTo(w * 0.28f, h * 0.52f); lineTo(w * 0.52f, h * 0.7f)
                    lineTo(w * 0.78f, h * 0.5f); lineTo(w, h * 0.64f); lineTo(w, h); close()
                }
                drawPath(hills, Color(0xFF0F1849))
            }
            1 -> { // Dawn: warm sky, rising sun
                drawRect(Brush.verticalGradient(listOf(Color(0xFFFFF4E5), Color(0xFFFFD9A8), Color(0xFFF4B53A))))
                drawCircle(Color(0xFFF08A2D), radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.36f))
            }
            2 -> { // Sea: peach-to-teal with a white sail
                drawRect(Brush.verticalGradient(listOf(Color(0xFFFFE9D2), Color(0xFFDEF5E9), Color(0xFF6AD0AB))))
                val sail = Path().apply { moveTo(w * 0.48f, h * 0.32f); lineTo(w * 0.48f, h * 0.64f); lineTo(w * 0.66f, h * 0.64f); close() }
                drawPath(sail, Color.White)
                drawRect(Color(0xFF14111B), topLeft = Offset(w * 0.36f, h * 0.64f), size = androidx.compose.ui.geometry.Size(w * 0.34f, h * 0.05f))
            }
            3 -> { // Hills: dawn pastel with rounded indigo + lilac hills
                drawRect(Brush.verticalGradient(listOf(Color(0xFFFFE9D2), Color(0xFFFCDEE6), Color(0xFFECE6FB))))
                drawCircle(Color(0xFF2436A1), radius = w * 0.42f, center = Offset(w * 0.18f, h * 1.08f))
                drawCircle(Color(0xFF8B6FE6), radius = w * 0.34f, center = Offset(w * 0.86f, h * 1.12f))
            }
            else -> { // Flame: deep lapis with a warm glow rising from the base
                drawRect(Brush.verticalGradient(listOf(Color(0xFF2436A1), Color(0xFF1A256E), Color(0xFF0F1849))))
                val glow = Offset(w * 0.5f, h * 0.82f)
                drawCircle(Brush.radialGradient(listOf(Color(0x88F08A2D), Color(0x00F08A2D)), center = glow, radius = w * 0.45f), radius = w * 0.45f, center = glow)
            }
            }
        }
        // Authentic Persian motif overlay (paisley / vine / rosette), tinted + faint.
        Image(
            painter = painterResource(pattern),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(Color.White),
            alpha = 0.14f,
        )
    }
}

@Composable
fun PardisStoryCard(
    titleEn: String,
    titleFa: String,
    ageBand: String,
    minutes: Int,
    vocabCount: Int,
    coverUrl: String?,
    downloadProgress: String?,
    downloadedSizeLabel: String?,
    isFailed: Boolean,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    noCoverLabel: String = "No cover",
    cancelLabel: String = "Cancel",
    removeLabel: String = "Remove",
    retryLabel: String = "Retry",
    downloadFailedLabel: String = "Download failed",
    downloadOfflineLabel: String = "Download offline",
) {
    PardisCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(Modifier.padding(PardisSpacing.md)) {
            Row(horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
                PardisRemoteImageFrame(
                    imageUrl = coverUrl,
                    contentDescription = "Cover image for story: $titleEn in $ageBand age band",
                    modifier = Modifier.size(PardisStoryCoverSize),
                    placeholderText = noCoverLabel,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        color = PardisColors.ink,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    PersianReaderInline(
                        text = titleFa,
                        style = MaterialTheme.typography.bodyLarge,
                        color = PardisColors.indigo,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(PardisSpacing.xs))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PardisMetaPill(
                            text = "$ageBand • ${minutes}m",
                            containerColor = PardisColors.saffronTint,
                            contentColor = PardisColors.saffronDeep,
                        )
                        PardisMetaPill(
                            text = "$vocabCount words",
                            containerColor = PardisColors.indigoTint,
                            contentColor = PardisColors.indigoDeep,
                        )
                    }
                }
            }
            Spacer(Modifier.height(PardisSpacing.sm))
            PardisStoryOfflineControls(
                downloadProgress = downloadProgress,
                downloadedSizeLabel = downloadedSizeLabel,
                isFailed = isFailed,
                onDownload = onDownload,
                onCancel = onCancel,
                onRemove = onRemove,
                cancelLabel = cancelLabel,
                removeLabel = removeLabel,
                retryLabel = retryLabel,
                downloadFailedLabel = downloadFailedLabel,
                downloadOfflineLabel = downloadOfflineLabel,
            )
        }
    }
}

@Composable
fun PardisFeaturedStoryCard(
    titleEn: String,
    titleFa: String,
    ageBand: String,
    minutes: Int,
    vocabCount: Int,
    coverUrl: String?,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    eyebrow: String = "Featured story",
    blurb: String? = null,
    actionLabel: String = "Start reading",
    noCoverLabel: String = "No cover",
) {
    PardisCard(modifier = modifier.fillMaxWidth(), onClick = onOpen) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PardisFeaturedStoryHeroHeight),
        ) {
            // Scene / cover fills the whole hero.
            PardisRemoteImageFrame(
                imageUrl = coverUrl,
                contentDescription = "Cover image for featured story: $titleEn",
                modifier = Modifier.fillMaxSize(),
                placeholderText = noCoverLabel,
            )
            // Legibility scrim so white text reads over any scene (light or dark).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x00000000), Color(0x40000000), Color(0xCC0F0C14)),
                        ),
                    ),
            )
            // Play affordance, top-right.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(PardisSpacing.md)
                    .size(48.dp)
                    .background(PardisColors.saffron, RoundedCornerShape(PardisRadius.full)),
                contentAlignment = Alignment.Center,
            ) {
                PardisIcon(PardisIconKind.Play, contentDescription = actionLabel, tint = Color.White)
            }
            // Overlaid text block, bottom.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(PardisSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PardisSpacing.xxs),
            ) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xCCFFFFFF),
                )
                Text(
                    text = titleEn,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                PersianReaderInline(
                    text = titleFa,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xE6FFFFFF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(PardisSpacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs)) {
                    PardisMetaPill(
                        text = "$ageBand • ${minutes}m",
                        containerColor = Color(0x33FFFFFF),
                        contentColor = Color.White,
                    )
                    PardisMetaPill(
                        text = "$vocabCount words",
                        containerColor = Color(0x33FFFFFF),
                        contentColor = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun PardisStoryOfflineControls(
    downloadProgress: String?,
    downloadedSizeLabel: String?,
    isFailed: Boolean,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
    cancelLabel: String,
    removeLabel: String,
    retryLabel: String,
    downloadFailedLabel: String,
    downloadOfflineLabel: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        when {
            downloadProgress != null -> {
                PardisMetaPill(downloadProgress, PardisColors.backgroundAlt, PardisColors.inkSoft)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onCancel) {
                    PardisIcon(PardisIconKind.Close, contentDescription = null, tint = PardisColors.inkSoft)
                    Spacer(Modifier.size(PardisSpacing.xs))
                    Text(cancelLabel, style = MaterialTheme.typography.labelSmall)
                }
            }
            downloadedSizeLabel != null -> {
                PardisMetaPill("Offline • $downloadedSizeLabel", PardisColors.mintSoft, PardisColors.mintDeep)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onRemove) {
                    PardisIcon(PardisIconKind.Trash, contentDescription = null, tint = PardisColors.inkSoft)
                    Spacer(Modifier.size(PardisSpacing.xs))
                    Text(removeLabel, style = MaterialTheme.typography.labelSmall)
                }
            }
            isFailed -> {
                Text(
                    text = downloadFailedLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.weight(1f))
                Button(onClick = onDownload) {
                    PardisIcon(PardisIconKind.Refresh, contentDescription = null, tint = PardisComponentColors.primaryActionContent)
                    Spacer(Modifier.size(PardisSpacing.xs))
                    Text(retryLabel, style = MaterialTheme.typography.labelSmall)
                }
            }
            else -> {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onDownload,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PardisColors.saffronSoft,
                        contentColor = PardisColors.saffronDeep,
                    ),
                ) {
                    PardisIcon(PardisIconKind.Download, contentDescription = null, tint = PardisColors.saffronDeep)
                    Spacer(Modifier.size(PardisSpacing.xs))
                    Text(downloadOfflineLabel, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun PardisReaderHeaderBar(
    onBack: () -> Unit,
    pageLabel: String,
    isOffline: Boolean,
    backLabel: String,
    offlineLabel: String,
    modifier: Modifier = Modifier,
) {
    PardisPanel(modifier = modifier, contentPadding = PaddingValues(horizontal = PardisSpacing.md, vertical = PardisSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
        ) {
            TextButton(onClick = onBack) {
                PardisIcon(PardisIconKind.Back, contentDescription = null, tint = PardisColors.indigo)
                Spacer(Modifier.size(PardisSpacing.xs))
                Text(backLabel, color = PardisColors.indigo, style = MaterialTheme.typography.labelLarge)
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PardisMetaPill(
                    text = pageLabel,
                    containerColor = PardisColors.backgroundAlt,
                    contentColor = PardisColors.inkSoft,
                )
            }
            if (isOffline) {
                PardisMetaPill(
                    text = offlineLabel,
                    containerColor = PardisColors.mintSoft,
                    contentColor = PardisColors.mintDeep,
                )
            }
        }
    }
}

@Composable
fun PardisControlGroup(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(PardisSpacing.xs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PardisColors.inkMuted,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun PardisVocabChip(vocab: VocabItem, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = PardisSpacing.xs),
        shape = RoundedCornerShape(PardisRadius.full),
        color = PardisColors.mintSoft,
        border = BorderStroke(PardisSpacing.hairline, PardisColors.borderSoft),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = PardisSpacing.md, vertical = PardisSpacing.sm)
                .semantics {
                    contentDescription = "Vocabulary term: ${vocab.fa} transliterated as ${vocab.translit}, English ${vocab.en}"
                },
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.xxs),
        ) {
            PersianReaderInline(
                text = vocab.fa,
                style = MaterialTheme.typography.bodySmall,
                color = PardisColors.indigoDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${vocab.translit} — ${vocab.en}",
                style = MaterialTheme.typography.labelSmall,
                color = PardisColors.inkSoft,
            )
        }
    }
}

@Composable
fun PardisVocabSheet(
    vocab: VocabItem,
    modifier: Modifier = Modifier,
    onPlayPronunciation: (() -> Unit)? = null,
    onClose: () -> Unit,
) {
    PardisPanel(modifier = modifier) {
        Text("Vocab", style = MaterialTheme.typography.labelMedium, color = PardisColors.indigo)
        PersianReaderInline(
            text = vocab.fa,
            style = MaterialTheme.typography.titleLarge,
            color = PardisColors.ink,
        )
        Text("(${vocab.translit})", style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkSoft)
        Text(vocab.en, style = MaterialTheme.typography.bodyLarge, color = PardisColors.inkSoft)
        if (vocab.context.isNotBlank()) {
            Text("in: ${vocab.context}", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkMuted)
        }
        if (onPlayPronunciation != null && vocab.audioUrl != null) {
            TextButton(onClick = onPlayPronunciation) {
                Text("▶ Play pronunciation", color = PardisColors.indigo)
            }
        }
        TextButton(onClick = onClose) {
            Text("Close", color = PardisColors.saffron)
        }
    }
}

@Composable
fun PersianReaderParagraph(
    text: String,
    style: TextStyle,
    color: Color,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = style.copy(fontFamily = PardisFonts.persian),
            color = color,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
fun PersianReaderInline(
    text: String,
    style: TextStyle,
    color: Color,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = text,
            style = style.copy(fontFamily = PardisFonts.persian),
            color = color,
            textAlign = TextAlign.Start,
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}
