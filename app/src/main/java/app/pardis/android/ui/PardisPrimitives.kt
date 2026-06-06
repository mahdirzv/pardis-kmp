package app.pardis.android.ui

import androidx.compose.foundation.BorderStroke
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
import app.pardis.design.PardisRadius
import app.pardis.design.PardisSpacing
import app.pardis.design.PardisThemedSurface
import coil.compose.AsyncImage

private val PardisStoryCoverSize = 68.dp
private val PardisFeaturedStoryCoverHeight = 156.dp

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
                    Text(cancelLabel, style = MaterialTheme.typography.labelSmall)
                }
            }
            downloadedSizeLabel != null -> {
                PardisMetaPill("Offline • $downloadedSizeLabel", PardisColors.mintSoft, PardisColors.mintDeep)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onRemove) {
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
                    Text(retryLabel, style = MaterialTheme.typography.labelSmall)
                }
            }
            else -> {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onDownload,
                    colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffron),
                ) {
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
            style = style,
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
            style = style,
            color = color,
            textAlign = TextAlign.Start,
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}
