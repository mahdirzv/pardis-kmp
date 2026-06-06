# Rivana Reskin — Slice 1 (Foundation + Reskin, Android) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restyle the existing Android Library + Reader screens to the Rivana visual language (expanded tokens, bundled fonts, a small reusable Compose component layer), in light theme, with zero functional regressions.

**Architecture:** Token-first then screen-by-screen. Expand `design-system/tokens.json` and the app's `PardisTokens.kt` additively; bundle Bricolage Grotesque / Plus Jakarta Sans / Vazirmatn / JetBrains Mono and expose named `TextStyle`s via a new `PardisText`; extract a small `ui/components/` layer (Buttons, Chips, Tag, Pattern, Progress, SectionHead, Cover); split the 792-line `PardisApp.kt` into `theme/ components/ library/ reader/`. Existing shared ViewModels/UiState/Actions and all media logic are untouched — only the Compose presentation changes.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), Coil, Media3/ExoPlayer, Koin, Android Gradle (minSdk 24, compileSdk 37).

**Spec:** `docs/superpowers/specs/2026-06-06-rivana-reskin-foundation-design.md`

---

## Conventions for this plan

- **No test harness exists** in this repo (no `test/` source sets) and this slice is presentational. So most tasks use **build + `@Preview` + on-device** verification rather than failing-test-first. Where a task introduces genuine pure logic, a JVM unit test is added (and the harness is stood up in that task).
- **On-device** = Galaxy A32, serial `RFCR11CB9JM`. Gradle needs `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- **Build gate** for every task: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug` → `BUILD SUCCESSFUL`.
- **Token duplication:** edit `tokens.json` (source of truth), `app/src/main/java/app/pardis/design/PardisTokens.kt` (compiled), and `design-system/generated/android/PardisTokens.kt` (reference copy) together. Only the middle one affects the build.
- All new files use package `app.pardis.android.ui.theme` or `app.pardis.android.ui.components` (or `.library` / `.reader`).
- Commit after each task with the message shown.

---

## File Structure (target)

```
design-system/tokens.json                         # MODIFY: + fonts, radii, gradients
app/src/main/java/app/pardis/design/PardisTokens.kt  # MODIFY: + PardisRadius.xl2/xl3
design-system/generated/android/PardisTokens.kt   # MODIFY: keep in sync (reference)
app/src/main/res/font/                             # NEW: 4 variable .ttf
app/src/main/java/app/pardis/android/ui/
  PardisApp.kt                                     # MODIFY → slim (NavHost + RTL only)
  theme/
    PardisFonts.kt                                 # NEW: FontFamily defs
    PardisText.kt                                  # NEW: named TextStyles
    PardisBrushes.kt                               # NEW: gradient Brush helpers
  components/
    Buttons.kt                                     # NEW: PardisButton
    Chips.kt                                        # NEW: PardisChip, PardisTag
    Pattern.kt                                      # NEW: paisley overlay
    Progress.kt                                     # NEW: PardisProgressBar
    SectionHead.kt                                  # NEW: SectionHead
    Cards.kt                                        # NEW: PardisCard (moved), Cover
  library/
    LibraryScreen.kt                                # NEW: extracted + reskinned (incl. StoryCard)
  reader/
    ReaderScreen.kt                                 # NEW: extracted + reskinned
    PageDots.kt                                     # NEW
    NarrationDock.kt                                # NEW
    VocabSheet.kt                                   # NEW
app/src/main/res/drawable/ic_paisley.xml           # NEW: vector for Pattern
```

---

## Phase 1 — Tokens & Type foundation

### Task 1: Expand radius tokens

**Files:**
- Modify: `design-system/tokens.json` (radius block)
- Modify: `app/src/main/java/app/pardis/design/PardisTokens.kt:54-60` (`PardisRadius`)
- Modify: `design-system/generated/android/PardisTokens.kt:54-60` (reference copy)

- [ ] **Step 1: Add the two larger radii to `tokens.json`**

In `design-system/tokens.json`, replace the `"radius"` block:

```json
  "radius": {
    "sm": 8,
    "md": 12,
    "lg": 18,
    "xl": 24,
    "xl2": 28,
    "xl3": 34,
    "full": 999
  },
```

- [ ] **Step 2: Add `xl2`/`xl3` to the compiled `PardisRadius`**

In `app/src/main/java/app/pardis/design/PardisTokens.kt`, replace the `PardisRadius` object:

```kotlin
object PardisRadius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 18.dp
    val xl = 24.dp
    val xl2 = 28.dp
    val xl3 = 34.dp
    val full = 999.dp
}
```

- [ ] **Step 3: Mirror the same change into the reference copy**

Apply the identical `PardisRadius` replacement to `design-system/generated/android/PardisTokens.kt`.

- [ ] **Step 4: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add design-system/tokens.json design-system/generated/android/PardisTokens.kt app/src/main/java/app/pardis/design/PardisTokens.kt
git commit -m "feat(tokens): add xl2/xl3 radii for Rivana reskin"
```

---

### Task 2: Bundle fonts

**Files:**
- Create: `app/src/main/res/font/bricolage_grotesque.ttf`, `plus_jakarta_sans.ttf`, `vazirmatn.ttf`, `jetbrains_mono.ttf`
- Create: `app/src/main/java/app/pardis/android/ui/theme/PardisFonts.kt`

> minSdk is 24. We bundle the **variable** TTFs (one file per family) and request weights via `FontVariation`. On API < 26 the weight axis is ignored and the font renders at its default instance — acceptable graceful degradation.

- [ ] **Step 1: Download the four variable fonts (OFL) into `res/font/`**

```bash
mkdir -p app/src/main/res/font
BASE="https://raw.githubusercontent.com/google/fonts/main/ofl"
curl -fsSL "$BASE/bricolagegrotesque/BricolageGrotesque%5Bopsz,wdth,wght%5D.ttf" -o app/src/main/res/font/bricolage_grotesque.ttf
curl -fsSL "$BASE/plusjakartasans/PlusJakartaSans%5Bwght%5D.ttf" -o app/src/main/res/font/plus_jakarta_sans.ttf
curl -fsSL "$BASE/vazirmatn/Vazirmatn%5Bwght%5D.ttf" -o app/src/main/res/font/vazirmatn.ttf
curl -fsSL "$BASE/jetbrainsmono/JetBrainsMono%5Bwght%5D.ttf" -o app/src/main/res/font/jetbrains_mono.ttf
ls -la app/src/main/res/font/
```
Expected: four `.ttf` files, each > 50 KB. (Resource font filenames must be lowercase/underscore only.)

- [ ] **Step 2: Record the font roles in `tokens.json` (source-of-truth manifest)**

Add a `"fonts"` block to `design-system/tokens.json` (after `"radius"`), and mirror it into `design-system/generated/android/PardisTokens.kt` only as a doc comment (no compiled object — families live in `PardisFonts.kt`):

```json
  "fonts": {
    "display": "Bricolage Grotesque",
    "body": "Plus Jakarta Sans",
    "fa": "Vazirmatn",
    "mono": "JetBrains Mono"
  },
```

- [ ] **Step 3: Create `PardisFonts.kt`**

Create `app/src/main/java/app/pardis/android/ui/theme/PardisFonts.kt`:

```kotlin
package app.pardis.android.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import app.pardis.android.R

/**
 * Rivana type families. We ship variable TTFs and request each weight via FontVariation
 * (effective on API 26+; older devices fall back to the default instance).
 */
private fun variableFont(resId: Int, weight: FontWeight) =
    Font(resId, weight = weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

val DisplayFamily = FontFamily(
    variableFont(R.font.bricolage_grotesque, FontWeight.Medium),
    variableFont(R.font.bricolage_grotesque, FontWeight.SemiBold),
    variableFont(R.font.bricolage_grotesque, FontWeight.Bold),
    variableFont(R.font.bricolage_grotesque, FontWeight.ExtraBold),
)

val BodyFamily = FontFamily(
    variableFont(R.font.plus_jakarta_sans, FontWeight.Normal),
    variableFont(R.font.plus_jakarta_sans, FontWeight.Medium),
    variableFont(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    variableFont(R.font.plus_jakarta_sans, FontWeight.Bold),
)

val FaFamily = FontFamily(
    variableFont(R.font.vazirmatn, FontWeight.Normal),
    variableFont(R.font.vazirmatn, FontWeight.Medium),
    variableFont(R.font.vazirmatn, FontWeight.SemiBold),
)

val MonoFamily = FontFamily(
    variableFont(R.font.jetbrains_mono, FontWeight.Medium),
    variableFont(R.font.jetbrains_mono, FontWeight.SemiBold),
)
```

- [ ] **Step 4: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL` (resolves `R.font.*`).

- [ ] **Step 5: Commit**

```bash
git add design-system/tokens.json design-system/generated/android/PardisTokens.kt app/src/main/res/font app/src/main/java/app/pardis/android/ui/theme/PardisFonts.kt
git commit -m "feat(theme): bundle Rivana fonts (Bricolage, Plus Jakarta, Vazirmatn, JetBrains Mono)"
```

---

### Task 3: Named text styles (`PardisText`)

**Files:**
- Create: `app/src/main/java/app/pardis/android/ui/theme/PardisText.kt`

> Named after the prototype's type primitives. Avoid the name `PardisTypography` — that token object already exists.

- [ ] **Step 1: Create `PardisText.kt`**

Create `app/src/main/java/app/pardis/android/ui/theme/PardisText.kt`:

```kotlin
package app.pardis.android.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import app.pardis.design.PardisColors

/** Rivana named text styles, mirroring the prototype's .display/.h1/.h2/.h3/.lead/.body/.small/.micro/.eyebrow/.fa */
object PardisText {
    val display = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 36.sp, letterSpacing = (-0.038).em, color = PardisColors.ink)
    val h1 = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 31.sp, letterSpacing = (-0.034).em, color = PardisColors.ink)
    val h2 = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 25.sp, letterSpacing = (-0.028).em, color = PardisColors.ink)
    val h3 = TextStyle(fontFamily = DisplayFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 21.sp, letterSpacing = (-0.02).em, color = PardisColors.ink)
    val lead = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp, color = PardisColors.inkSoft)
    val body = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp, color = PardisColors.inkSoft)
    val small = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp, color = PardisColors.inkSoft)
    val micro = TextStyle(fontFamily = MonoFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.14.em, color = PardisColors.inkMuted)
    val eyebrow = TextStyle(fontFamily = MonoFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.16.em, color = PardisColors.saffronDeep)
    val fa = TextStyle(fontFamily = FaFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 27.sp, textDirection = TextDirection.Rtl, color = PardisColors.ink)
}
```

- [ ] **Step 2: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/theme/PardisText.kt
git commit -m "feat(theme): add PardisText named text styles"
```

---

### Task 4: Gradient brushes (`PardisBrushes`)

**Files:**
- Modify: `design-system/tokens.json` (+ `gradients` block)
- Create: `app/src/main/java/app/pardis/android/ui/theme/PardisBrushes.kt`

- [ ] **Step 1: Record gradients in `tokens.json`**

Add a `"gradients"` block to `design-system/tokens.json` (after `"shadows"`):

```json
  "gradients": {
    "night": "linear 180deg #1A256E #2436A1 #4F2EB5",
    "lapis": "linear 135deg #2436A1 #4F2EB5 #1A256E",
    "saffron": "linear 135deg #F08A2D #F4B53A #FFD08A",
    "dawn": "linear 135deg #FFE9D2 #ECE6FB #E8EBFB"
  }
```

- [ ] **Step 2: Create `PardisBrushes.kt`**

Create `app/src/main/java/app/pardis/android/ui/theme/PardisBrushes.kt`:

```kotlin
package app.pardis.android.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Gradient brushes from the Rivana token set (used for scrims / future bedtime). */
object PardisBrushes {
    val night = Brush.verticalGradient(listOf(Color(0xFF1A256E), Color(0xFF2436A1), Color(0xFF4F2EB5)))
    val lapis = Brush.linearGradient(listOf(Color(0xFF2436A1), Color(0xFF4F2EB5), Color(0xFF1A256E)))
    val saffron = Brush.linearGradient(listOf(Color(0xFFF08A2D), Color(0xFFF4B53A), Color(0xFFFFD08A)))
    val dawn = Brush.linearGradient(listOf(Color(0xFFFFE9D2), Color(0xFFECE6FB), Color(0xFFE8EBFB)))
    /** Bottom-up scrim for image overlays (transparent → dark). */
    val imageScrim = Brush.verticalGradient(listOf(Color.Transparent, Color(0x66000000)))
}
```

- [ ] **Step 3: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add design-system/tokens.json app/src/main/java/app/pardis/android/ui/theme/PardisBrushes.kt
git commit -m "feat(theme): add PardisBrushes gradient helpers"
```

---

## Phase 2 — Component layer

### Task 5: Buttons

**Files:**
- Create: `app/src/main/java/app/pardis/android/ui/components/Buttons.kt`

- [ ] **Step 1: Create `Buttons.kt`**

Create `app/src/main/java/app/pardis/android/ui/components/Buttons.kt`:

```kotlin
package app.pardis.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.pardis.android.ui.theme.PardisText
import app.pardis.design.PardisColors

enum class PardisButtonVariant { Accent, Ink, Lapis, Soft, Ghost }
enum class PardisButtonSize { Sm, Md, Lg }

/** Rivana pill button. Variants map to .btn-accent/.btn-ink/.btn-lapis/.btn-soft/.btn-ghost. */
@Composable
fun PardisButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PardisButtonVariant = PardisButtonVariant.Accent,
    size: PardisButtonSize = PardisButtonSize.Md,
    enabled: Boolean = true,
) {
    val (bg, fg) = when (variant) {
        PardisButtonVariant.Accent -> PardisColors.saffron to Color.White
        PardisButtonVariant.Ink -> PardisColors.ink to Color.White
        PardisButtonVariant.Lapis -> PardisColors.indigo to Color.White
        PardisButtonVariant.Soft -> PardisColors.saffronSoft to PardisColors.saffronDeep
        PardisButtonVariant.Ghost -> Color.Transparent to PardisColors.inkSoft
    }
    val h = when (size) { PardisButtonSize.Sm -> 40.dp; PardisButtonSize.Md -> 50.dp; PardisButtonSize.Lg -> 56.dp }
    val pad = when (size) { PardisButtonSize.Sm -> 15.dp; PardisButtonSize.Md -> 20.dp; PardisButtonSize.Lg -> 26.dp }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(h),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        border = if (variant == PardisButtonVariant.Ghost) BorderStroke(1.5.dp, PardisColors.border) else null,
        contentPadding = PaddingValues(horizontal = pad),
    ) {
        Text(text, style = PardisText.body.copy(color = fg, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF6EE)
@Composable
private fun PardisButtonPreview() {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(320.dp)
    ) {
        PardisButtonVariant.entries.forEach { v ->
            PardisButton(text = v.name, onClick = {}, variant = v)
        }
    }
}
```

- [ ] **Step 2: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/components/Buttons.kt
git commit -m "feat(components): add PardisButton with Rivana variants"
```

---

### Task 6: Chips and Tags

**Files:**
- Create: `app/src/main/java/app/pardis/android/ui/components/Chips.kt`

- [ ] **Step 1: Create `Chips.kt`**

Create `app/src/main/java/app/pardis/android/ui/components/Chips.kt`:

```kotlin
package app.pardis.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pardis.android.ui.theme.PardisText
import app.pardis.design.PardisColors

/** Filter chip (.chip / .chip.is-active). Active = ink fill, white text.
 *  NOTE: `onClick` is the LAST parameter so trailing-lambda call sites compile. */
@Composable
fun PardisChip(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (active) PardisColors.ink else PardisColors.surface
    val fg = if (active) PardisColors.surface else PardisColors.inkSoft
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 38.dp)
            .background(bg, RoundedCornerShape(999.dp))
            .border(1.dp, if (active) PardisColors.ink else PardisColors.border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = PardisText.small.copy(color = fg, fontWeight = FontWeight.SemiBold))
    }
}

enum class PardisTagTone { Lapis, Saffron, Mint, Lilac, Neutral }

/** Small colored tag (.tag / .tag-lapis etc.). */
@Composable
fun PardisTag(text: String, tone: PardisTagTone = PardisTagTone.Neutral, modifier: Modifier = Modifier) {
    val (bg, fg) = when (tone) {
        PardisTagTone.Lapis -> PardisColors.indigoSoft to PardisColors.indigoDeep
        PardisTagTone.Saffron -> PardisColors.saffronSoft to PardisColors.saffronDeep
        PardisTagTone.Mint -> PardisColors.mintSoft to PardisColors.mintDeep
        PardisTagTone.Lilac -> PardisColors.lilacSoft to PardisColors.lilacDeep
        PardisTagTone.Neutral -> PardisColors.backgroundAlt to PardisColors.inkSoft
    }
    Row(
        modifier = modifier.background(bg, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = PardisText.small.copy(color = fg, fontWeight = FontWeight.Bold, fontSize = 11.sp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF6EE)
@Composable
private fun ChipsPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PardisChip("All ages", active = true, onClick = {})
        PardisChip("4–7", active = false, onClick = {})
        PardisTag("7m", PardisTagTone.Saffron)
        PardisTag("offline", PardisTagTone.Mint)
    }
}
```

- [ ] **Step 2: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/components/Chips.kt
git commit -m "feat(components): add PardisChip and PardisTag"
```

---

### Task 7: Paisley pattern overlay

**Files:**
- Create: `app/src/main/res/drawable/ic_paisley.xml`
- Create: `app/src/main/java/app/pardis/android/ui/components/Pattern.kt`

- [ ] **Step 1: Create the paisley vector drawable**

Create `app/src/main/res/drawable/ic_paisley.xml` (a simple repeatable teardrop/boteh motif used tinted at low opacity):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="64dp" android:height="64dp"
    android:viewportWidth="64" android:viewportHeight="64">
    <path android:fillColor="#FFFFFFFF"
        android:pathData="M32,8c-9,0 -16,7 -16,16c0,11 12,14 12,22c0,5 -4,7 -8,7c6,4 20,2 20,-11c0,-9 -10,-12 -10,-19c0,-6 4,-10 10,-10c5,0 8,3 8,7c2,-9 -4,-19 -16,-19z"/>
    <path android:fillColor="#FFFFFFFF"
        android:pathData="M30,24m-3,0a3,3 0,1 1,6 0a3,3 0,1 1,-6 0"/>
</vector>
```

- [ ] **Step 2: Create `Pattern.kt`**

Create `app/src/main/java/app/pardis/android/ui/components/Pattern.kt`:

```kotlin
package app.pardis.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import app.pardis.android.R
import app.pardis.design.PardisColors

/**
 * Decorative Persian paisley overlay, tinted + low opacity. Non-interactive; place inside a clipped Box.
 */
@Composable
fun PaisleyOverlay(
    modifier: Modifier = Modifier,
    color: Color = PardisColors.ink,
    opacity: Float = 0.06f,
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.ic_paisley),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(opacity),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(color),
        )
    }
}
```

- [ ] **Step 3: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/drawable/ic_paisley.xml app/src/main/java/app/pardis/android/ui/components/Pattern.kt
git commit -m "feat(components): add paisley pattern overlay"
```

---

### Task 8: Progress bar + SectionHead

**Files:**
- Create: `app/src/main/java/app/pardis/android/ui/components/Progress.kt`
- Create: `app/src/main/java/app/pardis/android/ui/components/SectionHead.kt`

- [ ] **Step 1: Create `Progress.kt`**

```kotlin
package app.pardis.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisColors

/** Thin rounded progress bar (.progress). value in 0f..1f. */
@Composable
fun PardisProgressBar(value: Float, modifier: Modifier = Modifier, height: Int = 6, color: Color = PardisColors.saffron) {
    Box(
        modifier = modifier.fillMaxWidth().height(height.dp)
            .background(Color(0x1A14111B), RoundedCornerShape(999.dp))
    ) {
        Box(
            Modifier.fillMaxWidth(value.coerceIn(0f, 1f)).fillMaxHeight()
                .background(color, RoundedCornerShape(999.dp))
        )
    }
}
```

- [ ] **Step 2: Create `SectionHead.kt`**

```kotlin
package app.pardis.android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pardis.android.ui.theme.PardisText
import app.pardis.design.PardisColors

/** Section header: EN title (display) + optional Farsi subtitle. */
@Composable
fun SectionHead(title: String, fa: String? = null, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(title, style = PardisText.h3)
        if (fa != null) Text(fa, style = PardisText.fa.copy(fontSize = 14.sp, color = PardisColors.inkMuted), modifier = Modifier.padding(top = 2.dp))
    }
}
```

- [ ] **Step 3: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/components/Progress.kt app/src/main/java/app/pardis/android/ui/components/SectionHead.kt
git commit -m "feat(components): add PardisProgressBar and SectionHead"
```

---

### Task 9: Cards — move `PardisCard`, add `Cover`

**Files:**
- Create: `app/src/main/java/app/pardis/android/ui/components/Cards.kt`
- Modify: `app/src/main/java/app/pardis/android/ui/PardisApp.kt` (remove the old `PardisCard` definition at lines 301-322 once the new one exists; done in Task 13's split — for now just add the new file; the old one stays until the split, both compile because they're in different packages? No — same package. See step note.)

> **Important:** `PardisCard` currently lives in `PardisApp.kt` in package `app.pardis.android.ui`. The new `Cards.kt` is in package `app.pardis.android.ui.components`, so there is **no name clash** — both can exist during the transition. Screens will switch to the components version in Tasks 11–12, and the old one is deleted in Task 13.

- [ ] **Step 1: Create `Cards.kt` with `PardisCard` (Rivana surface) + `Cover`**

```kotlin
package app.pardis.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pardis.android.ui.theme.PardisText
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import coil.compose.AsyncImage

/** Rivana card surface (.card): rounded-lg, soft shadow, hairline border. */
@Composable
fun PardisCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(PardisRadius.xl2)
    Surface(
        modifier = modifier
            .shadow(8.dp, shape, clip = false)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = PardisColors.surface,
        border = BorderStroke(1.dp, PardisColors.border),
    ) { content() }
}

/**
 * Story cover card for the Library grid: cover image (or lilac placeholder) with a paisley overlay,
 * EN + FA titles, and a row of tags. Tapping opens the story.
 */
@Composable
fun Cover(
    titleEn: String,
    titleFa: String,
    coverUrl: String?,
    tags: List<Pair<String, PardisTagTone>>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    PardisCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column {
            Box(
                Modifier.fillMaxWidth().aspectRatio(1.1f)
                    .clip(RoundedCornerShape(topStart = PardisRadius.xl2, topEnd = PardisRadius.xl2))
            ) {
                if (coverUrl != null) {
                    AsyncImage(model = coverUrl, contentDescription = "Cover for $titleEn", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)), contentAlignment = Alignment.Center) {
                        Surface(color = PardisColors.surfaceLilac, modifier = Modifier.fillMaxSize()) {}
                    }
                }
                PaisleyOverlay(modifier = Modifier.fillMaxSize(), color = Color.White, opacity = 0.10f)
            }
            Column(Modifier.padding(12.dp)) {
                Text(titleFa, style = PardisText.fa.copy(color = PardisColors.indigo, fontSize = 15.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(titleEn, style = PardisText.h3, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (tags.isNotEmpty()) {
                    Row(Modifier.padding(top = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        tags.forEach { (t, tone) -> PardisTag(t, tone) }
                    }
                }
                if (footer != null) {
                    Box(Modifier.padding(top = 8.dp)) { footer() }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL` (both `PardisCard`s coexist; no screen references the new one yet).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/components/Cards.kt
git commit -m "feat(components): add Rivana PardisCard surface and Cover"
```

---

## Phase 3 — Library reskin

### Task 10: Extract + reskin Library

**Files:**
- Create: `app/src/main/java/app/pardis/android/ui/library/LibraryScreen.kt` (with `LibraryRoute`, `LibraryScreen`, `RivanaStoryCard`)
- Modify: `app/src/main/java/app/pardis/android/ui/PardisApp.kt` — remove the old `LibraryRoute` (92-102), `LibraryScreen` (104-218), `StoryCard` (220-299); keep `PardisApp` importing the new `LibraryRoute`.

> Behavior preserved exactly: search, age-band chips (incl. "All ages" clear), cached-only toggle, refresh, error/retry, per-card download/cancel/remove/retry, cover caching, `onOpenStory`. Added: header with FA subtitle, search styled as a pill, chips via `PardisChip`, a grid/list segmented toggle (default grid, local state), a "By age" helper section, paisley page background. The mic icon and collection filters are intentionally omitted.

- [ ] **Step 1: Create `LibraryScreen.kt`**

Create `app/src/main/java/app/pardis/android/ui/library/LibraryScreen.kt`:

```kotlin
package app.pardis.android.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.android.ui.components.*
import app.pardis.android.ui.theme.PardisText
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import app.pardis.shared.library.LibraryAction
import app.pardis.shared.library.LibraryUiState
import app.pardis.shared.library.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryRoute(onOpenStory: (String) -> Unit, viewModel: LibraryViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryScreen(state = state, onAction = viewModel::onAction, onOpenStory = onOpenStory)
}

@Composable
fun LibraryScreen(state: LibraryUiState, onAction: (LibraryAction) -> Unit, onOpenStory: (String) -> Unit) {
    var view by remember { mutableStateOf("grid") } // "grid" | "list" — local UI state, grid default
    Box(Modifier.fillMaxSize().background(PardisColors.background)) {
        PaisleyOverlay(modifier = Modifier.fillMaxWidth().height(180.dp).align(Alignment.TopCenter), color = PardisColors.indigo, opacity = 0.05f)
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (view == "grid") 2 else 1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // ── Header (spans the full row, whatever the column count) ──
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Library", style = PardisText.h1)
                            Text("کتابخانه‌ی قصه‌ها", style = PardisText.fa.copy(color = PardisColors.inkMuted))
                        }
                        // grid/list segmented toggle (two chips)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PardisChip("▦", active = view == "grid") { view = "grid" }
                            PardisChip("☰", active = view == "list") { view = "list" }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    SearchPill(query = state.searchQuery, onQuery = { onAction(LibraryAction.Search(query = it)) })
                    Spacer(Modifier.height(12.dp))
                    FilterRow(state, onAction)
                    if (state.totalCachedLabel.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text("Cached offline: ${state.totalCachedLabel}", style = PardisText.small, color = PardisColors.inkMuted)
                    }
                    state.errorMessage?.let { err ->
                        Spacer(Modifier.height(8.dp))
                        Text("Error: $err", style = PardisText.small, color = PardisColors.error)
                        PardisButton("Retry", onClick = { onAction(LibraryAction.Refresh) }, variant = PardisButtonVariant.Soft, size = PardisButtonSize.Sm)
                    }
                    if (state.isLoading && state.stories.isEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        CircularProgressIndicator(color = PardisColors.saffron)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }

            // ── Story cards ──
            items(state.stories, key = { it.slug }, span = { GridItemSpan(1) }) { story ->
                RivanaStoryCard(
                    titleEn = story.titleEn,
                    titleFa = story.titleFa,
                    ageBand = story.ageBand,
                    minutes = story.minutes,
                    vocabCount = story.vocabCount,
                    coverUrl = state.localCoverUrls[story.slug] ?: story.coverUrl,
                    downloadProgress = state.downloadProgress[story.slug],
                    downloadedSizeLabel = state.downloadedSizeLabels[story.slug],
                    isFailed = state.failedDownloads.contains(story.slug),
                    onClick = { onOpenStory(story.slug) },
                    onDownload = { onAction(LibraryAction.DownloadStory(story.slug)) },
                    onCancel = { onAction(LibraryAction.CancelDownload(story.slug)) },
                    onRemove = { onAction(LibraryAction.RemoveDownload(story.slug)) },
                )
            }

            // ── "By age" helper (spans the full row) ──
            if (state.ageBands.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.padding(top = 10.dp)) {
                        SectionHead("By age", "بر اساس سن")
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.ageBands.take(3).forEachIndexed { i, band ->
                                val tone = listOf(PardisTagTone.Mint, PardisTagTone.Saffron, PardisTagTone.Lapis)[i % 3]
                                AgeTile(band = band, tone = tone, modifier = Modifier.weight(1f),
                                    onClick = { onAction(LibraryAction.SetAgeBand(if (state.selectedAgeBand == band) null else band)) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPill(query: String, onQuery: (String) -> Unit) {
    var tfv by remember(query) { mutableStateOf(TextFieldValue(query)) }
    Row(
        Modifier.fillMaxWidth().height(48.dp)
            .background(PardisColors.surface, RoundedCornerShape(999.dp))
            .border(1.dp, PardisColors.border, RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("🔍", style = PardisText.body)
        androidx.compose.foundation.text.BasicTextField(
            value = tfv,
            onValueChange = { tfv = it; onQuery(it.text) },
            singleLine = true,
            textStyle = PardisText.body.copy(color = PardisColors.ink),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (tfv.text.isEmpty()) Text("Search heroes, words, voyages…", style = PardisText.body, color = PardisColors.inkFaint)
                inner()
            },
        )
    }
}

@Composable
private fun FilterRow(state: LibraryUiState, onAction: (LibraryAction) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        PardisChip("All ages", active = state.selectedAgeBand == null && !state.showOnlyCached, onClick = { onAction(LibraryAction.SetAgeBand(null)) })
        state.ageBands.forEach { band ->
            PardisChip(band, active = state.selectedAgeBand == band, onClick = {
                onAction(LibraryAction.SetAgeBand(if (state.selectedAgeBand == band) null else band))
            })
        }
        PardisChip(if (state.showOnlyCached) "Cached ✓" else "Cached", active = state.showOnlyCached, onClick = { onAction(LibraryAction.ToggleShowOnlyCached) })
    }
}

@Composable
private fun AgeTile(band: String, tone: PardisTagTone, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (bg, fg) = when (tone) {
        PardisTagTone.Mint -> PardisColors.mintSoft to PardisColors.mintDeep
        PardisTagTone.Saffron -> PardisColors.saffronSoft to PardisColors.saffronDeep
        else -> PardisColors.indigoSoft to PardisColors.indigoDeep
    }
    Column(
        modifier.background(bg, RoundedCornerShape(PardisRadius.lg)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Text(band, style = PardisText.h3.copy(color = fg))
        Text("ages", style = PardisText.small.copy(color = fg, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun RivanaStoryCard(
    titleEn: String, titleFa: String, ageBand: String, minutes: Int, vocabCount: Int,
    coverUrl: String?, downloadProgress: String?, downloadedSizeLabel: String?, isFailed: Boolean,
    onClick: () -> Unit, onDownload: () -> Unit, onCancel: () -> Unit, onRemove: () -> Unit,
) {
    val tags = buildList {
        add(ageBand to PardisTagTone.Lapis)
        add("${minutes}m" to PardisTagTone.Saffron)
        if (downloadedSizeLabel != null) add("offline" to PardisTagTone.Mint)
    }
    Cover(titleEn = titleEn, titleFa = titleFa, coverUrl = coverUrl, tags = tags, onClick = onClick, footer = {
        when {
            downloadProgress != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text(downloadProgress, style = PardisText.small); Spacer(Modifier.weight(1f))
                PardisButton("Cancel", onCancel, variant = PardisButtonVariant.Ghost, size = PardisButtonSize.Sm)
            }
            downloadedSizeLabel != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✓ $downloadedSizeLabel", style = PardisText.small.copy(color = PardisColors.mintDeep)); Spacer(Modifier.weight(1f))
                PardisButton("Remove", onRemove, variant = PardisButtonVariant.Ghost, size = PardisButtonSize.Sm)
            }
            isFailed -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Failed", style = PardisText.small.copy(color = PardisColors.error)); Spacer(Modifier.weight(1f))
                PardisButton("Retry", onDownload, variant = PardisButtonVariant.Soft, size = PardisButtonSize.Sm)
            }
            else -> PardisButton("Download", onDownload, variant = PardisButtonVariant.Soft, size = PardisButtonSize.Sm, modifier = Modifier.fillMaxWidth())
        }
    })
}
```

- [ ] **Step 2: Remove the old Library code from `PardisApp.kt`**

Delete these spans from `app/src/main/java/app/pardis/android/ui/PardisApp.kt`:
- `LibraryRoute` (current lines 91-102)
- `LibraryScreen` (current lines 104-218)
- `StoryCard` (current lines 220-299)

Add the import to `PardisApp.kt` so `NavHost` still resolves `LibraryRoute`:
```kotlin
import app.pardis.android.ui.library.LibraryRoute
```
Remove now-unused imports flagged by the compiler (e.g. `LazyColumn`, `items`, `FilterChip`, `horizontalScroll` if no longer used in this file).

- [ ] **Step 3: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Verify on device**

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:installDebug
adb -s RFCR11CB9JM shell monkey -p app.pardis.android -c android.intent.category.LAUNCHER 1
```
Confirm: 2-column grid of restyled cards; header + FA subtitle; search filters; age chips + Cached chip toggle; "By age" tiles set the filter; tapping a card opens the Reader; download/cancel/remove still work.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/library/LibraryScreen.kt app/src/main/java/app/pardis/android/ui/PardisApp.kt
git commit -m "feat(library): extract + reskin Library to Rivana look"
```

---

## Phase 4 — Reader reskin

### Task 11: Reader sub-components (PageDots, NarrationDock, VocabSheet)

**Files:**
- Create: `app/src/main/java/app/pardis/android/ui/reader/PageDots.kt`
- Create: `app/src/main/java/app/pardis/android/ui/reader/NarrationDock.kt`
- Create: `app/src/main/java/app/pardis/android/ui/reader/VocabSheet.kt`

> These are stateless presentational pieces; all callbacks are passed in. The Reader still owns playback logic.

- [ ] **Step 1: Create `PageDots.kt`**

```kotlin
package app.pardis.android.ui.reader

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.pardis.design.PardisColors

/** Animated page indicator: active dot elongates; any dot is tappable to jump. */
@Composable
fun PageDots(count: Int, current: Int, onJump: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)) {
        for (i in 0 until count) {
            val w by animateDpAsState(if (i == current) 22.dp else 5.dp, label = "dotW")
            val color = when { i == current -> PardisColors.saffron; i < current -> PardisColors.saffronSoft; else -> PardisColors.border }
            Box(Modifier.height(5.dp).width(w).background(color, RoundedCornerShape(999.dp)).clickable { onJump(i) })
        }
    }
}
```

- [ ] **Step 2: Create `NarrationDock.kt`**

```kotlin
package app.pardis.android.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.pardis.android.ui.components.PardisButton
import app.pardis.android.ui.components.PardisButtonSize
import app.pardis.android.ui.components.PardisButtonVariant
import app.pardis.android.ui.components.PardisProgressBar
import app.pardis.android.ui.theme.PardisText
import app.pardis.design.PardisColors

/**
 * Bottom narration dock (text mode): page-progress bar + transport + lang + rate.
 * All behavior is delegated via callbacks; this composable holds no playback state.
 */
@Composable
fun NarrationDock(
    progress: Float,
    isFirst: Boolean,
    isLast: Boolean,
    narrationLang: String,
    onPrev: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit,
    onLang: (String) -> Unit,
    onRate: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(PardisColors.surface, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        PardisProgressBar(value = progress, height = 5)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PardisButton("‹", onPrev, variant = PardisButtonVariant.Ghost, size = PardisButtonSize.Sm, enabled = !isFirst)
            PardisButton("▶ Play", onPlay, variant = PardisButtonVariant.Accent, size = PardisButtonSize.Sm)
            PardisButton("›", onNext, variant = PardisButtonVariant.Ghost, size = PardisButtonSize.Sm, enabled = !isLast)
            Spacer(Modifier.weight(1f))
            PardisButton(if (narrationLang == "fa") "FA" else "EN", { onLang(if (narrationLang == "fa") "en" else "fa") }, variant = PardisButtonVariant.Soft, size = PardisButtonSize.Sm)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Rate", style = PardisText.micro, modifier = Modifier.align(Alignment.CenterVertically))
            listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { r ->
                PardisButton("${r}x", { onRate(r) }, variant = PardisButtonVariant.Ghost, size = PardisButtonSize.Sm)
            }
        }
    }
}
```

- [ ] **Step 3: Create `VocabSheet.kt`**

```kotlin
package app.pardis.android.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.pardis.android.ui.components.PardisButton
import app.pardis.android.ui.components.PardisButtonSize
import app.pardis.android.ui.components.PardisButtonVariant
import app.pardis.android.ui.theme.PardisText
import app.pardis.core.model.VocabItem
import app.pardis.design.PardisColors

/** Bottom sheet content for a selected vocab word (grip + FA term + translit/EN/context + actions). */
@Composable
fun VocabSheetContent(v: VocabItem, onPlay: () -> Unit, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(PardisColors.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.width(38.dp).height(5.dp).background(PardisColors.border, RoundedCornerShape(999.dp)))
        Spacer(Modifier.height(12.dp))
        Text("${v.fa}  (${v.translit})", style = PardisText.h2)
        Text(v.en, style = PardisText.lead)
        if (v.context.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text("in: ${v.context}", style = PardisText.small, color = PardisColors.inkMuted)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (v.audioUrl != null) PardisButton("▶ Pronounce", onPlay, variant = PardisButtonVariant.Soft, size = PardisButtonSize.Sm)
            PardisButton("Close", onClose, variant = PardisButtonVariant.Ghost, size = PardisButtonSize.Sm)
        }
    }
}
```

- [ ] **Step 4: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/reader/PageDots.kt app/src/main/java/app/pardis/android/ui/reader/NarrationDock.kt app/src/main/java/app/pardis/android/ui/reader/VocabSheet.kt
git commit -m "feat(reader): add PageDots, NarrationDock, VocabSheet components"
```

---

### Task 12: Extract + reskin Reader

**Files:**
- Create: `app/src/main/java/app/pardis/android/ui/reader/ReaderScreen.kt` (move `ReaderRoute` + `ReaderScreen`, reskinned)
- Modify: `app/src/main/java/app/pardis/android/ui/PardisApp.kt` — remove `ReaderRoute` (346-377) and `ReaderScreen` (379-792) and `PardisVocabChip` (327-344); add `import app.pardis.android.ui.reader.ReaderRoute`.

> **CRITICAL — preserve verbatim:** copy the entire media/logic core from the current `ReaderScreen` (current `PardisApp.kt` lines 417-506: the `videoUrl` resolution, the stable `ExoPlayer` `remember`+listener, all six `LaunchedEffect` blocks, both `DisposableEffect`s, and the `narrationPlayer` state) into the new file **unchanged**. Also copy the full narration `MediaPlayer` build block (current lines 673-720) and the vocab pronunciation block (current lines 770-783) **unchanged** — only their surrounding Compose chrome changes. Do not rewrite playback logic.

- [ ] **Step 1: Create `ReaderScreen.kt` — route + scaffold**

Create `app/src/main/java/app/pardis/android/ui/reader/ReaderScreen.kt`. Start with the route (moved verbatim from current lines 346-377, with imports) and the screen scaffold:

```kotlin
package app.pardis.android.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import android.media.MediaPlayer
import app.pardis.android.ui.components.*
import app.pardis.android.ui.theme.PardisText
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import app.pardis.shared.reader.ReaderAction
import app.pardis.shared.reader.ReaderUiState
import app.pardis.shared.reader.ReaderViewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReaderRoute(slug: String, onBack: () -> Unit, viewModel: ReaderViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(slug) { viewModel.onAction(ReaderAction.LoadStory(slug)) }
    val context = LocalContext.current
    LaunchedEffect(state.currentPage, state.pages, state.localIllustrationUrls) {
        val nextPage = state.pages.getOrNull(state.currentPage + 1)
        val url = nextPage?.let { state.localIllustrationUrls[it.page] ?: it.illustrationUrl }
        url?.let { context.imageLoader.enqueue(ImageRequest.Builder(context).data(it).build()) }
    }
    ReaderScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

/** Text-display toggle local to the screen (does not affect narration language). */
private enum class TextLang { Both, En, Fa }
```

- [ ] **Step 2: Add the `ReaderScreen` composable — top bar + page dots + body, preserving media logic**

Append to `ReaderScreen.kt`. The `else ->` branch keeps the **verbatim** media block from current lines 417-506 (shown abbreviated here with `/* … */` markers pointing at the exact source spans you must paste unchanged), and wraps the presentation in the new chrome:

```kotlin
@Composable
fun ReaderScreen(state: ReaderUiState, onAction: (ReaderAction) -> Unit, onBack: () -> Unit) {
    var textLang by remember { mutableStateOf(TextLang.Both) }

    Column(Modifier.fillMaxSize().background(PardisColors.background)) {
        // ── Top bar ──
        Row(
            Modifier.fillMaxWidth().padding(top = 50.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PardisButton("‹", onBack, variant = PardisButtonVariant.Ghost, size = PardisButtonSize.Sm)
            Column(Modifier.weight(1f).padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.storySlug, style = PardisText.h3, maxLines = 1)
                if (state.pages.isNotEmpty()) Text("Page ${state.currentPage + 1} of ${state.pages.size}", style = PardisText.micro)
            }
            if (state.videoUrlFa != null || state.videoUrlEn != null) {
                PardisButton(if (state.isVideoMode) "Text" else "Video", { onAction(ReaderAction.ToggleVideo) }, variant = PardisButtonVariant.Ghost, size = PardisButtonSize.Sm)
            } else {
                Spacer(Modifier.width(40.dp))
            }
        }
        if (state.pages.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            PageDots(count = state.pages.size, current = state.currentPage, onJump = { onAction(ReaderAction.GoToPage(it)) }, modifier = Modifier.padding(horizontal = 16.dp))
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PardisColors.saffron) }
            state.errorMessage != null -> Column(Modifier.padding(16.dp)) {
                Text("Error: ${state.errorMessage}", style = PardisText.body, color = PardisColors.error)
                PardisButton("Retry", { onAction(ReaderAction.LoadStory(state.storySlug)) }, variant = PardisButtonVariant.Soft, size = PardisButtonSize.Sm)
            }
            state.pages.isEmpty() -> Text("No pages loaded for ${state.storySlug}", style = PardisText.body, modifier = Modifier.padding(16.dp))
            else -> {
                /* ===== PASTE VERBATIM from current PardisApp.kt lines 417-506 =====
                   val page = ...; val videoUrl = ...; val context = LocalContext.current
                   val exoPlayer = remember { ... }
                   LaunchedEffect(videoUrl) { ... }
                   LaunchedEffect(state.isDownloadingVideo, exoPlayer) { ... }
                   val narrationPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
                   LaunchedEffect(exoPlayer, state.cues, state.isVideoMode) { ... }   // cue ticker
                   LaunchedEffect(state.currentPage, exoPlayer, state.isVideoMode) { ... } // seek-on-jump
                   DisposableEffect(exoPlayer) { onDispose { exoPlayer.release() } }
                   DisposableEffect(narrationPlayer.value) { onDispose { ... } }
                   ===== end verbatim paste ===== */
                // `page`, `videoUrl`, `context`, `exoPlayer`, `narrationPlayer` all come from the verbatim
                // paste above — do NOT redeclare them here.

                if (videoUrl != null) {
                    // Video mode: player PINNED above a scrollable captions area (preserves current UX).
                    Box(Modifier.fillMaxWidth().height(380.dp).padding(horizontal = 16.dp).clip(RoundedCornerShape(PardisRadius.xl2))) {
                        AndroidView(factory = { PlayerView(it).apply { player = exoPlayer; useController = true } }, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                        ProseBlock(page = page, textLang = TextLang.Both)
                        VocabChips(page, onAction)
                        Spacer(Modifier.height(24.dp))
                    }
                } else {
                    // Text mode: single scroll body (illustration + toggle + prose + vocab) with swipe-to-turn.
                    Box(
                        Modifier.weight(1f).fillMaxWidth().pointerInput(state.currentPage, state.pages.size) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (dragAmount < -45 && state.currentPage < state.pages.lastIndex) onAction(ReaderAction.NextPage)
                                else if (dragAmount > 45 && state.currentPage > 0) onAction(ReaderAction.PrevPage)
                            }
                        }
                    ) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                            val illoUrl = state.localIllustrationUrls[page.page] ?: page.illustrationUrl
                            Spacer(Modifier.height(8.dp))
                            Box(Modifier.fillMaxWidth().height(290.dp).clip(RoundedCornerShape(PardisRadius.xl3))) {
                                if (illoUrl != null) AsyncImage(model = illoUrl, contentDescription = "Illustration for page ${page.page}", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                else Box(Modifier.fillMaxSize().background(PardisColors.surfaceLilac))
                                PaisleyOverlay(modifier = Modifier.fillMaxSize(), color = androidx.compose.ui.graphics.Color.White, opacity = 0.10f)
                            }
                            Spacer(Modifier.height(16.dp))
                            TextLangToggle(textLang) { textLang = it }
                            Spacer(Modifier.height(12.dp))
                            ProseBlock(page = page, textLang = textLang)
                            VocabChips(page, onAction)
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }

                // ── Narration dock (text mode only) ──
                if (!state.isVideoMode) {
                    NarrationDock(
                        progress = if (state.pages.isNotEmpty()) (state.currentPage + 1f) / state.pages.size else 0f,
                        isFirst = state.currentPage == 0,
                        isLast = state.currentPage >= state.pages.lastIndex,
                        narrationLang = state.preferredNarrationLang,
                        onPrev = { onAction(ReaderAction.PrevPage) },
                        onNext = { onAction(ReaderAction.NextPage) },
                        onLang = { onAction(ReaderAction.SetNarrationLang(it)) },
                        onRate = { onAction(ReaderAction.SetPlaybackRate(it)) },
                        onPlay = {
                            onAction(ReaderAction.PlayNarration)
                            /* ===== PASTE VERBATIM the MediaPlayer build block from current lines 676-720 =====
                               (try { narrationPlayer.value?.release(); ... prepareAsync() ... } catch ...)
                               It references narrationPlayer, state, onAction — all in scope here. ===== */
                        },
                    )
                }

                // ── Vocab sheet ──
                state.selectedVocab?.let { v ->
                    VocabSheetContent(
                        v = v,
                        onClose = { onAction(ReaderAction.DismissVocab) },
                        onPlay = {
                            /* ===== PASTE VERBATIM the pronunciation block from current lines 771-782 ===== */
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TextLangToggle(value: TextLang, onChange: (TextLang) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PardisChip("Both", value == TextLang.Both) { onChange(TextLang.Both) }
        PardisChip("EN", value == TextLang.En) { onChange(TextLang.En) }
        PardisChip("FA", value == TextLang.Fa) { onChange(TextLang.Fa) }
    }
}

@Composable
private fun ProseBlock(page: app.pardis.core.model.StoryPage, textLang: TextLang) {
    if (textLang == TextLang.Both || textLang == TextLang.En) {
        Text(page.paragraphsEn.joinToString("\n\n"), style = PardisText.lead.copy(fontSize = 20.sp, color = PardisColors.ink))
    }
    if (textLang == TextLang.Both || textLang == TextLang.Fa) {
        if (textLang == TextLang.Both) Spacer(Modifier.height(16.dp))
        Text(page.paragraphsFa.joinToString("\n\n"), style = PardisText.fa.copy(fontSize = 20.sp, color = PardisColors.inkSoft))
    }
}

@Composable
private fun VocabChips(page: app.pardis.core.model.StoryPage, onAction: (ReaderAction) -> Unit) {
    if (page.vocabulary.isNotEmpty()) {
        Spacer(Modifier.height(20.dp))
        Text("Tap a word to learn it", style = PardisText.small, color = PardisColors.inkFaint)
        Spacer(Modifier.height(8.dp))
        page.vocabulary.take(5).forEach { v ->
            androidx.compose.foundation.layout.Box(Modifier.padding(vertical = 3.dp)) {
                PardisChip("${v.fa} — ${v.en}", active = false) { onAction(ReaderAction.ShowVocab(v)) }
            }
        }
    }
}
```

> Add `import androidx.compose.ui.unit.sp` for `20.sp`. The `videoUrl`, `exoPlayer`, `narrationPlayer`, and `page` referenced in the chrome are all produced by the verbatim paste in Step 2's marked block; ensure that paste sits **before** the `Box`.

- [ ] **Step 3: Remove old Reader code from `PardisApp.kt`**

Delete from `app/src/main/java/app/pardis/android/ui/PardisApp.kt`: `PardisVocabChip` (current 327-344), `ReaderRoute` (346-377), `ReaderScreen` (379-792). Add:
```kotlin
import app.pardis.android.ui.reader.ReaderRoute
```
Remove now-unused imports the compiler flags.

- [ ] **Step 4: Build**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. If unresolved refs appear in the pasted block, they indicate the verbatim paste is incomplete — re-copy the full span.

- [ ] **Step 5: Verify on device**

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:installDebug
adb -s RFCR11CB9JM shell monkey -p app.pardis.android -c android.intent.category.LAUNCHER 1
```
Open a story. Confirm: restyled top bar + page-dots; swipe left/right turns pages; Both/EN/FA toggle switches prose; illustration card with paisley; narration dock Play/rate/lang works and auto-advances; vocab chip opens restyled sheet with pronunciation; video toggle + ExoPlayer + cue page-sync still work; "Cache video + assets" still downloads and switches to local. **No regressions.**

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/reader/ReaderScreen.kt app/src/main/java/app/pardis/android/ui/PardisApp.kt
git commit -m "feat(reader): extract + reskin Reader to Rivana look (logic preserved)"
```

---

## Phase 5 — Finalize

### Task 13: Slim `PardisApp.kt` + remove leftovers + final verification

**Files:**
- Modify: `app/src/main/java/app/pardis/android/ui/PardisApp.kt`

- [ ] **Step 1: Confirm `PardisApp.kt` is now only the shell**

After Tasks 10 & 12, `PardisApp.kt` should contain only `PardisApp()` (the `MaterialTheme`/`Surface`/RTL provider/`NavHost`) plus the two route imports. Remove any remaining unused imports (e.g. `app.pardis.core.model.VocabItem`, media imports, `FontWeight`, `shadow`) so the file is just:

```kotlin
package app.pardis.android.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.pardis.android.ui.library.LibraryRoute
import app.pardis.android.ui.reader.ReaderRoute
import app.pardis.design.PardisColors

@Composable
fun PardisApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = PardisColors.background) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "library") {
                    composable("library") {
                        LibraryRoute(onOpenStory = { slug -> navController.navigate("reader/$slug") })
                    }
                    composable("reader/{slug}", arguments = listOf(navArgument("slug") { type = NavType.StringType })) { backStackEntry ->
                        val slug = backStackEntry.arguments?.getString("slug") ?: ""
                        ReaderRoute(slug = slug, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Full build + lint for unused symbols**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL` with no unresolved references.

- [ ] **Step 3: Regression walkthrough on device**

Reinstall and walk the full app one more time (Library: search, all chips, Cached toggle, By-age tiles, grid; download a story end-to-end; Reader: dots, swipe, toggle, narration, vocab, video + cache). Confirm parity with pre-reskin behavior.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/app/pardis/android/ui/PardisApp.kt
git commit -m "refactor(ui): slim PardisApp.kt to shell after screen extraction"
```

---

## Final review

After all tasks: dispatch a code-review of the whole branch (`git diff main...HEAD`), then use **superpowers:finishing-a-development-branch** to merge/PR. Confirm CI stays green (Android assemble + iOS framework + iOS app compile — iOS is untouched by this slice).
