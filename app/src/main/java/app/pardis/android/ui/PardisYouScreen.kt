package app.pardis.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisRadius
import app.pardis.design.PardisColors
import app.pardis.design.PardisGradients
import app.pardis.design.PardisSpacing
import app.pardis.core.model.ChildProfile
import androidx.compose.runtime.getValue

private data class SettingsItem(val icon: PardisIconKind, val tone: String, val label: String, val detail: String? = null)

@Composable
internal fun YouScreen(activeProfile: ChildProfile, onSwitchProfile: () -> Unit, downloadCount: Int, bottomContentPadding: androidx.compose.ui.unit.Dp) {
    val gutter = PardisSpacing.lg
    Box(Modifier.fillMaxSize().background(PardisColors.background)) {
    PardisPatternOverlay(
        motif = PardisMotif.Rosette,
        color = PardisColors.indigo,
        alpha = 0.05f,
        fade = PardisPatternFade.Top,
        modifier = Modifier.fillMaxWidth().height(220.dp).align(Alignment.TopCenter),
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = PardisSpacing.xl, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(PardisSpacing.md),
    ) {
        item {
            Text("You", style = MaterialTheme.typography.displayLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = gutter))
        }
        item { YouProfileCard(activeProfile = activeProfile, onSwitchProfile = onSwitchProfile, modifier = Modifier.padding(horizontal = gutter)) }
        item { AppearanceGroup(Modifier.padding(horizontal = gutter)) }
        item {
            SettingsGroup(
                "Reading",
                listOf(
                    SettingsItem(PardisIconKind.Languages, "lapis", "Story language", "English & فارسی"),
                    SettingsItem(PardisIconKind.Volume, "saffron", "Narration speed", "Normal"),
                    SettingsItem(PardisIconKind.Download, "mint", "Downloads", "$downloadCount stories"),
                ),
                Modifier.padding(horizontal = gutter),
            )
        }
        item {
            SettingsGroup(
                "Family",
                listOf(
                    SettingsItem(PardisIconKind.Shield, "lapis", "Parents' corner", "Locked"),
                    SettingsItem(PardisIconKind.Bell, "saffron", "Bedtime reminder", "8:00 PM"),
                    SettingsItem(PardisIconKind.Star, "lilac", "Rivana Plus", "Active"),
                ),
                Modifier.padding(horizontal = gutter),
            )
        }
        item {
            SettingsGroup(
                "About",
                listOf(
                    SettingsItem(PardisIconKind.Settings, "lapis", "Settings"),
                    SettingsItem(PardisIconKind.Heart, "rose", "Rate Rivana"),
                ),
                Modifier.padding(horizontal = gutter),
            )
        }
        item {
            Column(Modifier.fillMaxWidth().padding(top = PardisSpacing.md), horizontalAlignment = Alignment.CenterHorizontally) {
                PersianReaderInline(
                    "ریوانا · قصه‌های پارسی برای کودکان",
                    style = MaterialTheme.typography.bodySmall,
                    color = PardisColors.inkFaint,
                )
                Text(
                    "PARDIS · v1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = PardisColors.inkFaint,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        }
    }
    }
}

@Composable
private fun YouProfileCard(activeProfile: ChildProfile, onSwitchProfile: () -> Unit, modifier: Modifier) {
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.xl))
            .background(PardisGradients.dawn)
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.xl)),
    ) {
    PardisPatternOverlay(PardisMotif.Paisley, PardisColors.indigo, alpha = 0.07f, fade = PardisPatternFade.TopRight, modifier = Modifier.matchParentSize())
    Row(
        Modifier.fillMaxWidth().padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier.size(72.dp).clip(RoundedCornerShape(PardisRadius.full))
                .background(toneGradient(activeProfile.tone)),
            contentAlignment = Alignment.Center,
        ) {
            Text(activeProfile.name.take(1), style = MaterialTheme.typography.displayLarge, color = PardisColors.inkOnDark, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(activeProfile.name, style = MaterialTheme.typography.headlineSmall, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
            Text("Age ${activeProfile.age} · ${activeProfile.streak}-night streak", style = MaterialTheme.typography.bodySmall, color = PardisColors.inkSoft)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surface).clickable(onClick = onSwitchProfile).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PardisIcon(PardisIconKind.User, contentDescription = null, tint = PardisColors.ink, size = 15.dp)
                Text("Switch reader", style = MaterialTheme.typography.labelLarge, color = PardisColors.ink)
            }
        }
    }
    }
}

@Composable
private fun AppearanceGroup(modifier: Modifier) {
    Column(modifier) {
        Text("APPEARANCE", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted, modifier = Modifier.padding(bottom = PardisSpacing.sm))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.lg)).background(PardisColors.surface).padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(PardisRadius.sm)).background(PardisColors.lilacSoft),
                contentAlignment = Alignment.Center,
            ) {
                PardisIcon(PardisIconKind.Moon, contentDescription = null, tint = PardisColors.lilacDeep, size = 18.dp)
            }
            Text("Dark mode", style = MaterialTheme.typography.bodyLarge, color = PardisColors.ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            // Decorative off toggle (dark mode not wired yet)
            Box(
                Modifier.size(width = 46.dp, height = 28.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.border),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(Modifier.padding(start = 3.dp).size(22.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.inkOnDark))
            }
        }
    }
}

@Composable
private fun SettingsGroup(label: String, items: List<SettingsItem>, modifier: Modifier) {
    Column(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted, modifier = Modifier.padding(bottom = PardisSpacing.sm))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(PardisRadius.lg)).border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.lg)).background(PardisColors.surface),
        ) {
            items.forEachIndexed { i, item ->
                if (i > 0) Box(Modifier.padding(start = 63.dp).fillMaxWidth().height(1.dp).background(PardisColors.border))
                SettingsRow(item)
            }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsItem) {
    val (soft, deep) = toneColors(item.tone)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(PardisRadius.sm)).background(soft),
            contentAlignment = Alignment.Center,
        ) {
            PardisIcon(item.icon, contentDescription = null, tint = deep, size = 18.dp)
        }
        Text(item.label, style = MaterialTheme.typography.bodyLarge, color = PardisColors.ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (item.detail != null) {
            Text(item.detail, style = MaterialTheme.typography.bodySmall, color = PardisColors.inkMuted)
        }
        PardisIcon(PardisIconKind.ChevRight, contentDescription = null, tint = PardisColors.inkFaint, size = 17.dp)
    }
}
