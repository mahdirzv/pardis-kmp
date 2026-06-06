package app.pardis.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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

enum class PardisIconKind {
    Back,
    Book,
    Close,
    Download,
    Play,
    Refresh,
    Search,
    Trash,
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
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.18f), end = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.50f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.50f), end = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
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
            PardisIconKind.Trash -> {
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.36f), end = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.36f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.36f), end = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.82f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.64f, h * 0.36f), end = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.82f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
                drawLine(tint, start = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.22f), end = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.22f), strokeWidth = thinStroke.width, cap = StrokeCap.Round)
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
    placeholderText: String = "No illustration",
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = placeholderText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PardisComponentColors.mediaPlaceholderContent,
                )
            }
        }
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
    PardisCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(PardisSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
        ) {
            PardisRemoteImageFrame(
                imageUrl = coverUrl,
                contentDescription = "Cover image for featured story: $titleEn",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PardisFeaturedStoryCoverHeight),
                placeholderText = noCoverLabel,
            )
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelSmall,
                color = PardisColors.inkMuted,
            )
            Text(
                text = titleEn,
                style = MaterialTheme.typography.headlineSmall,
                color = PardisColors.ink,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            PersianReaderInline(
                text = titleFa,
                style = MaterialTheme.typography.titleMedium,
                color = PardisColors.indigo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!blurb.isNullOrBlank()) {
                Text(
                    text = blurb,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PardisColors.inkSoft,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
            Button(
            onClick = onOpen,
            colors = ButtonDefaults.buttonColors(
                containerColor = PardisComponentColors.primaryActionContainer,
                contentColor = PardisComponentColors.primaryActionContent,
            ),
        ) {
            PardisIcon(PardisIconKind.Book, contentDescription = null, tint = PardisComponentColors.primaryActionContent)
            Spacer(Modifier.size(PardisSpacing.xs))
            Text(actionLabel, style = MaterialTheme.typography.labelLarge)
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
                    colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffron),
                ) {
                    PardisIcon(PardisIconKind.Download, contentDescription = null, tint = PardisComponentColors.primaryActionContent)
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
