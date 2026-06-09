package app.pardis.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisRadius
import app.pardis.design.PardisColors
import app.pardis.design.PardisSpacing
import androidx.compose.runtime.getValue

@Composable
internal fun BedtimeScreen(onOpenLullaby: (Int) -> Unit, bottomContentPadding: androidx.compose.ui.unit.Dp) {
    val gutter = PardisSpacing.lg
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PardisColors.indigoDeep, Color(0xFF0F1330), Color(0xFF0A0E22)))),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = PardisSpacing.xl, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
        ) {
            item {
                Column(Modifier.padding(horizontal = gutter)) {
                    Text("SWEET DREAMS", style = MaterialTheme.typography.labelSmall, color = Color(0xBFFFE9D2))
                    Text("Bedtime", style = MaterialTheme.typography.displayLarge, color = PardisColors.inkOnDark, fontWeight = FontWeight.ExtraBold)
                    PersianReaderInline("وقتِ خواب · لای‌لای", style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkOnDarkFaint)
                }
            }
            item { BedtimeFeatured(rivanaLullabies[0], onClick = { onOpenLullaby(0) }, Modifier.padding(horizontal = gutter)) }
            item { WindDownCard(Modifier.padding(horizontal = gutter)) }
            item {
                Text(
                    "Lullaby shelf",
                    style = MaterialTheme.typography.titleLarge,
                    color = PardisColors.inkOnDark,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = gutter, vertical = PardisSpacing.xs),
                )
            }
            itemsIndexed(rivanaLullabies.drop(1)) { i, l -> LullabyRow(l, onClick = { onOpenLullaby(i + 1) }, Modifier.padding(horizontal = gutter)) }
            item {
                Text(
                    "شب بخیر · خواب‌های خوش",
                    style = MaterialTheme.typography.bodySmall,
                    color = PardisColors.inkOnDarkFaint,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = PardisSpacing.md),
                )
            }
        }
    }
}

@Composable
private fun BedtimeFeatured(l: Lullaby, onClick: () -> Unit, modifier: Modifier) {
    Box(modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(PardisRadius.xl)).clickable(onClick = onClick)) {
        PardisSceneArt(seed = l.title, forcedVariant = l.sceneVariant, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB30F0C1E)))))
        Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
            Row(
                Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDark).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                PardisIcon(PardisIconKind.Moon, contentDescription = null, tint = PardisColors.inkOnDark, size = 14.dp)
                Text("Lullaby of the night", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDark)
            }
            Spacer(Modifier.height(9.dp))
            Text(l.title, style = MaterialTheme.typography.headlineSmall, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
            PersianReaderInline(l.titleFa, style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDarkMuted)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.inkOnDark),
                    contentAlignment = Alignment.Center,
                ) {
                    PardisIcon(PardisIconKind.Play, contentDescription = "Play ${l.title}", tint = PardisColors.indigoDeep)
                }
                Text("${l.minutes} min · ${l.origin}", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkOnDarkSoft)
            }
        }
    }
}

@Composable
private fun WindDownCard(modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).background(PardisColors.surfaceOnDarkFaint).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(PardisRadius.base)).background(Color(0x2EF4B53A)),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(PardisIconKind.Clock, contentDescription = null, tint = PardisColors.sun)
        }
        Column(Modifier.weight(1f)) {
            Text("Wind-down routine", style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
            Text("One short story, then a lullaby. ~25 min.", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkOnDarkFaint)
        }
        Box(
            Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDarkSoft).padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text("Start", style = MaterialTheme.typography.labelLarge, color = PardisColors.inkOnDark)
        }
    }
}

@Composable
private fun LullabyRow(l: Lullaby, onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = PardisSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        PardisSceneArt(seed = l.title, forcedVariant = l.sceneVariant, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)))
        Column(Modifier.weight(1f)) {
            Text(l.title, style = MaterialTheme.typography.titleMedium, color = PardisColors.inkOnDark, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            PersianReaderInline(l.titleFa, style = MaterialTheme.typography.bodySmall, color = PardisColors.inkOnDarkFaint, maxLines = 1)
            Text("${l.minutes} min · ${l.plays} plays", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkOnDarkFaint)
        }
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surfaceOnDarkSoft),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(PardisIconKind.Play, contentDescription = "Play ${l.title}", tint = PardisColors.inkOnDark)
        }
    }
}
