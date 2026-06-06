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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.pardis.core.model.VocabItem
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import app.pardis.design.PardisShadows
import app.pardis.design.PardisSpacing
import coil.compose.AsyncImage

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
        color = if (selected) PardisColors.indigo else PardisColors.surface,
        border = BorderStroke(1.dp, if (selected) PardisColors.indigo else PardisColors.border),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = PardisSpacing.md, vertical = PardisSpacing.sm),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) PardisColors.inkOnDark else PardisColors.inkSoft,
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
fun PardisCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(PardisRadius.lg)
    Surface(
        modifier = modifier
            .shadow(PardisShadows.md, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = PardisColors.surface,
        border = BorderStroke(1.dp, PardisColors.borderSoft),
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
        color = PardisColors.surfaceLilac,
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
                    color = PardisColors.inkSoft,
                )
            }
        }
    }
}

@Composable
fun PardisReaderHeaderBar(
    onBack: () -> Unit,
    pageLabel: String,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
) {
    PardisPanel(modifier = modifier, contentPadding = PaddingValues(horizontal = PardisSpacing.md, vertical = PardisSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
        ) {
            TextButton(onClick = onBack) {
                Text("← Library", color = PardisColors.indigo, style = MaterialTheme.typography.labelLarge)
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
                    text = "Offline",
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
        border = BorderStroke(1.dp, PardisColors.borderSoft),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = PardisSpacing.md, vertical = PardisSpacing.sm)
                .semantics {
                    contentDescription = "Vocabulary term: ${vocab.fa} transliterated as ${vocab.translit}, English ${vocab.en}"
                },
            verticalArrangement = Arrangement.spacedBy(2.dp),
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


