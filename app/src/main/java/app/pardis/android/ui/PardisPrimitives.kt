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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
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
    Flame,
    Feather,
    Clock,
    ChevRight,
    Pause,
    Volume,
    Sparkle,
    Bookmark,
    Lock,
    Crown,
    Compass,
    Mic,
    Sprout,
    Check,
    Languages,
    Shield,
    Settings,
    Heart,
    Bell,
    Grid,
    ListView,
    Trophy,
}

enum class PardisMotif { Paisley, Vine, Rosette, Star8 }

/** Directional alpha fade for a pattern overlay, mirroring the design's `fade` prop. */
enum class PardisPatternFade { None, Top, TopRight, BottomLeft, Bottom, Edges }

/**
 * Faint Persian motif overlay for backgrounds and gradient cards (non-interactive).
 * Matches the design's CSS `mask-image` treatment: a single small motif tile *repeated*
 * across the surface (not one motif stretched to fill). Tile defaults to each motif's
 * canonical app.css size; override [tileWidth]/[tileHeight] to match a specific placement.
 */
@Composable
fun PardisPatternOverlay(
    motif: PardisMotif,
    color: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 0.06f,
    fade: PardisPatternFade = PardisPatternFade.None,
    tileWidth: Dp = Dp.Unspecified,
    tileHeight: Dp = Dp.Unspecified,
) {
    val (res, defaultW, defaultH) = when (motif) {
        PardisMotif.Paisley -> Triple(R.drawable.pattern_paisley, 200.dp, 200.dp)
        PardisMotif.Vine -> Triple(R.drawable.pattern_vine, 280.dp, 140.dp)
        PardisMotif.Rosette -> Triple(R.drawable.pattern_rosette, 170.dp, 170.dp)
        PardisMotif.Star8 -> Triple(R.drawable.pattern_star8, 120.dp, 120.dp)
    }
    val tileW = if (tileWidth != Dp.Unspecified) tileWidth else defaultW
    val tileH = if (tileHeight != Dp.Unspecified) tileHeight else defaultH
    val context = LocalContext.current
    val density = LocalDensity.current
    val tilePxW = with(density) { tileW.roundToPx() }
    val tilePxH = with(density) { tileH.roundToPx() }
    val tile = remember(res, tilePxW, tilePxH) {
        val d = androidx.core.content.ContextCompat.getDrawable(context, res)!!
        val bmp = android.graphics.Bitmap.createBitmap(tilePxW, tilePxH, android.graphics.Bitmap.Config.ARGB_8888)
        val c = android.graphics.Canvas(bmp)
        d.setBounds(0, 0, tilePxW, tilePxH)
        d.draw(c)
        bmp.asImageBitmap()
    }
    val brush = remember(tile) { ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated)) }
    val canvasModifier = if (fade != PardisPatternFade.None) {
        modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    } else {
        modifier
    }
    Canvas(canvasModifier) {
        drawRect(brush = brush, alpha = alpha, colorFilter = ColorFilter.tint(color))
        val mask = when (fade) {
            PardisPatternFade.None -> null
            PardisPatternFade.Top -> Brush.verticalGradient(listOf(Color.Black, Color.Transparent))
            PardisPatternFade.Bottom -> Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
            PardisPatternFade.TopRight -> Brush.linearGradient(
                listOf(Color.Black, Color.Transparent),
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height),
            )
            PardisPatternFade.BottomLeft -> Brush.linearGradient(
                listOf(Color.Black, Color.Transparent),
                start = Offset(0f, size.height),
                end = Offset(size.width, 0f),
            )
            PardisPatternFade.Edges -> Brush.radialGradient(listOf(Color.Transparent, Color.Black))
        }
        if (mask != null) drawRect(brush = mask, blendMode = BlendMode.DstIn)
    }
}

/** Thin rounded linear progress bar. value in 0f..1f. */
@Composable
fun PardisProgressBar(value: Float, modifier: Modifier = Modifier, height: Int = 6, color: Color = PardisColors.saffron) {
    Box(
        modifier = modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(PardisRadius.full)).background(Color(0x1F14111B)),
    ) {
        Box(
            Modifier.fillMaxWidth(value.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(PardisRadius.full)).background(color),
        )
    }
}

/** Circular progress ring (track + accent arc from 12 o'clock). Centered [content] overlays it. */
@Composable
fun PardisRing(
    progress: Float,
    ringColor: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0x1F14111B),
    strokeWidthDp: Float = 5f,
    content: @Composable () -> Unit = {},
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = strokeWidthDp.dp.toPx()
            val tl = Offset(sw / 2f, sw / 2f)
            val arc = Size(size.width - sw, size.height - sw)
            drawArc(trackColor, 0f, 360f, false, topLeft = tl, size = arc, style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(ringColor, -90f, progress.coerceIn(0f, 1f) * 360f, false, topLeft = tl, size = arc, style = Stroke(sw, cap = StrokeCap.Round))
        }
        content()
    }
}

/** Flat Lucide vector icons (converted to vector drawables), tinted at draw time. */
@Composable
fun PardisIcon(
    icon: PardisIconKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = PardisColors.ink,
    size: androidx.compose.ui.unit.Dp = PardisIconSize,
) {
    val res = when (icon) {
        PardisIconKind.Back -> R.drawable.ic_back
        PardisIconKind.Book -> R.drawable.ic_book
        PardisIconKind.Close -> R.drawable.ic_close
        PardisIconKind.Download -> R.drawable.ic_download
        PardisIconKind.Home -> R.drawable.ic_home
        PardisIconKind.Moon -> R.drawable.ic_moon
        PardisIconKind.Play -> R.drawable.ic_play
        PardisIconKind.Refresh -> R.drawable.ic_refresh
        PardisIconKind.Search -> R.drawable.ic_search
        PardisIconKind.Star -> R.drawable.ic_star
        PardisIconKind.Trash -> R.drawable.ic_trash
        PardisIconKind.User -> R.drawable.ic_user
        PardisIconKind.Flame -> R.drawable.ic_flame
        PardisIconKind.Feather -> R.drawable.ic_feather
        PardisIconKind.Clock -> R.drawable.ic_clock
        PardisIconKind.ChevRight -> R.drawable.ic_chevright
        PardisIconKind.Pause -> R.drawable.ic_pause
        PardisIconKind.Volume -> R.drawable.ic_volume
        PardisIconKind.Sparkle -> R.drawable.ic_sparkle
        PardisIconKind.Bookmark -> R.drawable.ic_bookmark
        PardisIconKind.Lock -> R.drawable.ic_lock
        PardisIconKind.Crown -> R.drawable.ic_crown
        PardisIconKind.Compass -> R.drawable.ic_compass
        PardisIconKind.Mic -> R.drawable.ic_mic
        PardisIconKind.Sprout -> R.drawable.ic_sprout
        PardisIconKind.Check -> R.drawable.ic_check
        PardisIconKind.Languages -> R.drawable.ic_languages
        PardisIconKind.Shield -> R.drawable.ic_shield
        PardisIconKind.Settings -> R.drawable.ic_settings
        PardisIconKind.Heart -> R.drawable.ic_heart
        PardisIconKind.Bell -> R.drawable.ic_bell
        PardisIconKind.Grid -> R.drawable.ic_grid
        PardisIconKind.ListView -> R.drawable.ic_list
        PardisIconKind.Trophy -> R.drawable.ic_trophy
    }
    Image(
        painter = painterResource(res),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(tint),
    )
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
fun PardisSceneArt(seed: String, modifier: Modifier = Modifier, forcedVariant: Int? = null) {
    val variant = forcedVariant?.let { ((it % 7) + 7) % 7 } ?: (((seed.hashCode() % 7) + 7) % 7)
    val pattern = when (variant % 3) {
        0 -> R.drawable.pattern_paisley
        1 -> R.drawable.pattern_vine
        else -> R.drawable.pattern_rosette
    }
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            fun p(xf: Float, yf: Float) = Offset(xf * w, yf * h)
            fun poly(pts: List<Offset>) = Path().apply {
                pts.forEachIndexed { i, o -> if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y) }
                close()
            }
            fun stars(pts: List<Offset>) = pts.forEach { drawCircle(Color.White, w * 0.006f, it) }
            when (variant) {
            0 -> { // night — lapis gradient, violet+saffron glow, stars, saffron moon, 2 mountain ridges
                drawRect(Brush.verticalGradient(0f to Color(0xFF1A256E), 0.7f to Color(0xFF2436A1), 1f to Color(0xFF4F2EB5)))
                drawRect(Brush.radialGradient(listOf(Color(0x385B47FF), Color(0x005B47FF)), center = p(0.5f, 0.65f), radius = h * 0.65f))
                stars(listOf(p(0.12f, 0.14f), p(0.28f, 0.26f), p(0.70f, 0.16f), p(0.88f, 0.32f), p(0.50f, 0.09f), p(0.22f, 0.44f), p(0.38f, 0.18f)))
                val moon = Offset(w * 0.5f, h * 0.14f + w * 0.2f)
                drawCircle(Brush.radialGradient(listOf(Color(0x55F08A2D), Color(0x00F08A2D)), center = moon, radius = w * 0.34f), w * 0.34f, moon)
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFE9D2), Color(0xFFF08A2D)), center = moon, radius = w * 0.2f), w * 0.2f, moon)
                drawPath(poly(listOf(p(0f, 1f), p(0f, 0.88f), p(0.22f, 0.79f), p(0.38f, 0.85f), p(0.54f, 0.766f), p(0.70f, 0.844f), p(0.88f, 0.784f), p(1f, 0.85f), p(1f, 1f))), Color(0x990F1849))
                drawPath(poly(listOf(p(0f, 1f), p(0f, 0.829f), p(0.14f, 0.704f), p(0.26f, 0.802f), p(0.40f, 0.650f), p(0.56f, 0.780f), p(0.70f, 0.681f), p(0.84f, 0.787f), p(1f, 0.719f), p(1f, 1f))), Color(0xFF0A1234))
            }
            1 -> { // dawn — sunrise gradient, sun, cypress silhouette
                drawRect(Brush.verticalGradient(0f to Color(0xFFFFF4E5), 0.5f to Color(0xFFFFD9A8), 1f to Color(0xFFF4B53A)))
                drawRect(Brush.radialGradient(listOf(Color(0x8CFFE9D2), Color(0x00FFE9D2)), center = p(0.5f, 0.4f), radius = h * 0.5f))
                val sun = Offset(w * 0.5f, h * 0.16f + w * 0.2f)
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFE9D2), Color(0xFFF08A2D)), center = sun, radius = w * 0.2f), w * 0.2f, sun)
                drawOval(Color(0xFF0F1849), topLeft = Offset(w * 0.43f, h * 0.42f), size = Size(w * 0.14f, h * 0.6f))
            }
            2 -> { // vase — lapis gradient, lilac glow, saffron Persian vase + stem
                drawRect(Brush.verticalGradient(0f to Color(0xFF2436A1), 1f to Color(0xFF0F1849)))
                drawRect(Brush.radialGradient(listOf(Color(0x408B6FE6), Color(0x008B6FE6)), center = p(0.5f, 0.4f), radius = h * 0.6f))
                drawRect(Color(0xD9FFE9D2), topLeft = Offset(w * 0.497f, h * 0.24f), size = Size(w * 0.006f, h * 0.26f))
                drawPath(
                    poly(listOf(p(0.455f, 0.34f), p(0.545f, 0.34f), p(0.56f, 0.398f), p(0.53f, 0.456f), p(0.59f, 0.688f), p(0.62f, 0.862f), p(0.59f, 0.92f), p(0.41f, 0.92f), p(0.38f, 0.862f), p(0.41f, 0.688f), p(0.47f, 0.456f), p(0.44f, 0.398f))),
                    Brush.verticalGradient(listOf(Color(0xFFF08A2D), Color(0xFFC46A12))),
                )
            }
            3 -> { // hills — dawn-pink gradient + lapis & lilac rounded hills
                drawRect(Brush.verticalGradient(0f to Color(0xFFFFE9D2), 0.7f to Color(0xFFFCDEE6), 1f to Color(0xFFECE6FB)))
                drawRect(Brush.radialGradient(listOf(Color(0x66FFE9D2), Color(0x00FFE9D2)), center = p(0.5f, 0.3f), radius = h * 0.55f))
                drawOval(Color(0xFF8B6FE6), topLeft = Offset(w * 0.4f, h * 0.65f), size = Size(w * 0.7f, h * 0.7f))
                drawOval(Color(0xFF2436A1), topLeft = Offset(-w * 0.1f, h * 0.5f), size = Size(w * 0.8f, h * 1.0f))
            }
            4 -> { // sea — sky→sea gradient, sailboat
                drawRect(Brush.verticalGradient(0f to Color(0xFFFFE9D2), 0.45f to Color(0xFFFCDEE6), 0.65f to Color(0xFFDEF5E9), 1f to Color(0xFF6AD0AB)))
                drawRect(Color(0xFF14111B), topLeft = Offset(w * 0.465f, h * 0.44f), size = Size(w * 0.012f, h * 0.22f))
                drawPath(poly(listOf(p(0.36f, 0.62f), p(0.58f, 0.46f), p(0.58f, 0.62f))), Color.White)
                drawPath(poly(listOf(p(0.332f, 0.66f), p(0.668f, 0.66f), p(0.7f, 0.74f), p(0.3f, 0.74f))), Color(0xFF14111B))
            }
            5 -> { // flame — lapis night, glowing saffron→rose flame
                drawRect(Brush.verticalGradient(0f to Color(0xFF2436A1), 0.6f to Color(0xFF1A256E), 1f to Color(0xFF0F1849)))
                drawRect(Brush.radialGradient(listOf(Color(0x66F08A2D), Color(0x00F08A2D)), center = p(0.5f, 0.8f), radius = h * 0.5f))
                drawOval(Brush.verticalGradient(listOf(Color(0xFFFCEAB6), Color(0xFFF08A2D), Color(0xFFE1547A))), topLeft = Offset(w * 0.37f, h * 0.46f), size = Size(w * 0.26f, h * 0.4f))
            }
            else -> { // lullaby — soft violet→lapis, white moon, stars
                drawRect(Brush.verticalGradient(0f to Color(0xFFBCC8E8), 0.5f to Color(0xFF8B6FE6), 1f to Color(0xFF1A256E)))
                stars(listOf(p(0.18f, 0.16f), p(0.32f, 0.24f), p(0.70f, 0.18f), p(0.84f, 0.30f), p(0.50f, 0.10f), p(0.24f, 0.40f)))
                val moon = Offset(w * 0.5f, h * 0.2f + w * 0.14f)
                drawCircle(Color.White, w * 0.14f, moon)
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
                PardisIcon(PardisIconKind.Play, contentDescription = actionLabel, tint = PardisColors.inkOnDark)
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
                    color = PardisColors.inkOnDarkSoft,
                )
                Text(
                    text = titleEn,
                    style = MaterialTheme.typography.headlineSmall,
                    color = PardisColors.inkOnDark,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                PersianReaderInline(
                    text = titleFa,
                    style = MaterialTheme.typography.titleMedium,
                    color = PardisColors.inkOnDarkSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(PardisSpacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(PardisSpacing.xs)) {
                    PardisMetaPill(
                        text = "$ageBand • ${minutes}m",
                        containerColor = PardisColors.surfaceOnDark,
                        contentColor = PardisColors.inkOnDark,
                    )
                    PardisMetaPill(
                        text = "$vocabCount words",
                        containerColor = PardisColors.surfaceOnDark,
                        contentColor = PardisColors.inkOnDark,
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
