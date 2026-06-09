package app.pardis.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import app.pardis.design.PardisSpacing
import app.pardis.core.model.RivanaCharacter

private fun toneAccent(tone: String): Color = when (tone) {
    "saffron" -> PardisColors.saffronDeep
    "lilac" -> PardisColors.lilacDeep
    "lapis" -> PardisColors.indigo
    "rose" -> PardisColors.roseDeep
    "sun" -> PardisColors.sunDeep
    "mint" -> PardisColors.mintDeep
    else -> PardisColors.indigo
}

@Composable
internal fun CharacterScreen(character: RivanaCharacter, onBack: () -> Unit) {
    val accent = toneAccent(character.tone)
    Box(Modifier.fillMaxSize().background(PardisColors.background)) {
        PardisPatternOverlay(
            motif = PardisMotif.Vine,
            color = accent,
            alpha = 0.05f,
            fade = PardisPatternFade.Bottom,
            modifier = Modifier.fillMaxWidth().height(260.dp).align(Alignment.BottomCenter),
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            CharacterHero(character, onBack)
            Text(
                character.bio,
                style = MaterialTheme.typography.titleMedium,
                color = PardisColors.ink,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = PardisSpacing.lg, vertical = PardisSpacing.sm),
            )
            CharacterStats(character, accent, Modifier.padding(horizontal = PardisSpacing.lg, vertical = PardisSpacing.sm))
            Spacer(Modifier.height(PardisSpacing.xl))
        }
    }
}

@Composable
private fun CharacterHero(c: RivanaCharacter, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(400.dp)) {
        PardisSceneArt(seed = c.name, forcedVariant = c.sceneVariant, modifier = Modifier.matchParentSize())
        PardisPatternOverlay(PardisMotif.Rosette, PardisColors.inkOnDark, alpha = 0.14f, fade = PardisPatternFade.BottomLeft, modifier = Modifier.matchParentSize())
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0.0f to PardisColors.scrimSoft,
                    0.30f to Color.Transparent,
                    0.55f to Color.Transparent,
                    1.0f to PardisColors.background,
                ),
            ),
        )
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = PardisSpacing.md, vertical = PardisSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CharacterCircleButton(PardisIconKind.Back, "Back", onBack)
            CharacterCircleButton(PardisIconKind.Heart, "Save", {})
        }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = PardisSpacing.lg, vertical = PardisSpacing.md)) {
            CollectedTag(c.collected)
            Spacer(Modifier.height(10.dp))
            Text(c.name, style = MaterialTheme.typography.displayLarge, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold)
            PersianReaderInline(c.nameFa, style = MaterialTheme.typography.headlineSmall, color = PardisColors.inkSoft)
            Spacer(Modifier.height(6.dp))
            Text(c.role.uppercase(), style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
        }
    }
}

@Composable
private fun CollectedTag(collected: Boolean) {
    Row(
        Modifier.clip(RoundedCornerShape(PardisRadius.full))
            .background(if (collected) PardisColors.mintSoft else PardisColors.scrim)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        PardisIcon(
            if (collected) PardisIconKind.Check else PardisIconKind.Lock,
            contentDescription = null,
            tint = if (collected) PardisColors.mintDeep else PardisColors.inkOnDark,
            size = 12.dp,
        )
        Text(
            if (collected) "Collected" else "Not yet met",
            style = MaterialTheme.typography.labelSmall,
            color = if (collected) PardisColors.mintDeep else PardisColors.inkOnDark,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CharacterCircleButton(icon: PardisIconKind, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(PardisRadius.full)).background(PardisColors.surface).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PardisIcon(icon, contentDescription = label, tint = PardisColors.ink, size = 19.dp)
    }
}

@Composable
private fun CharacterStats(c: RivanaCharacter, accent: Color, modifier: Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
        CharacterStat(PardisIconKind.Book, "${c.appearances}", "stories", accent, Modifier.weight(1f))
        CharacterStat(PardisIconKind.Feather, c.role.substringBefore(' '), "role", accent, Modifier.weight(1f))
        CharacterStat(if (c.collected) PardisIconKind.StarFill else PardisIconKind.Star, if (c.collected) "★" else "–", "badge", accent, Modifier.weight(1f))
    }
}

@Composable
private fun CharacterStat(icon: PardisIconKind, value: String, label: String, accent: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(PardisRadius.base)).background(PardisColors.surface)
            .border(1.dp, PardisColors.border, RoundedCornerShape(PardisRadius.base)).padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PardisIcon(icon, contentDescription = null, tint = accent, size = 20.dp)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = PardisColors.ink, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
    }
}
