@file:Suppress("unused")

package app.pardis.design

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Generated from design-system/tokens.json + web neutral.ts
 * Pardis palette only. Use these in Compose UI.
 */
object PardisColors {
    // ── Scheme-reactive neutrals ──────────────────────────────
    // Read as usual (PardisColors.background, .ink, …). They're backed by Compose snapshot state, so
    // flipping them in [applyScheme] (called by PardisTheme on scheme change — the only writer)
    // recomposes every reader. This lets the brand-token UI, which reads PardisColors.* directly
    // (not MaterialTheme.colorScheme), follow light/dark. Brand accents, on-dark overlays and scrims
    // below stay constant across schemes (their on-chip text contrast is preserved; they pop on dark).
    var background by mutableStateOf(Color(0xFFFAF6EE))
    var backgroundAlt by mutableStateOf(Color(0xFFF3EEDD))
    var backgroundStage by mutableStateOf(Color(0xFFEDE6D6))
    var surface by mutableStateOf(Color(0xFFFFFFFF))
    var surface2 by mutableStateOf(Color(0xFFFDFAF0))
    var surfaceSoft by mutableStateOf(Color(0xFFE8EBFB))
    var surfaceMint by mutableStateOf(Color(0xFFDEF5E9))
    var surfacePeach by mutableStateOf(Color(0xFFFFE9D2))
    var surfaceLilac by mutableStateOf(Color(0xFFECE6FB))
    var ink by mutableStateOf(Color(0xFF14111B))
    var inkSoft by mutableStateOf(Color(0xFF4B4760))
    var inkMuted by mutableStateOf(Color(0xFF8A8499))
    var inkFaint by mutableStateOf(Color(0xFFB6B0C0))
    var border by mutableStateOf(Color(0xFFECE3D0))
    var borderSoft by mutableStateOf(Color(0xFFF2ECDD))
    var borderStrong by mutableStateOf(Color(0xFFDDD2BC))
    var error by mutableStateOf(Color(0xFFEF4444))

    // ── Dark neutral source values (also consumed by PardisDarkColorScheme) ──
    val darkBackground = Color(0xFF141019)
    val darkBackgroundAlt = Color(0xFF1D1825)
    val darkSurface = Color(0xFF211C2B)
    val darkSurface2 = Color(0xFF2A2435)
    val darkBorder = Color(0xFF322C40)
    val darkBorderSoft = Color(0xFF28222F)
    val darkBorderStrong = Color(0xFF443C56)
    val darkInk = Color(0xFFF4F1FA)
    val darkInkSoft = Color(0xFFC3BDD2)
    val darkInkMuted = Color(0xFF8E87A0)
    val darkInkFaint = Color(0xFF5F596F)
    // Canonical handoff dark equivalents (app.css [data-theme="dark"]). Stage bg is app-specific
    // (no handoff token); the surface tints equal their matching accent-soft dark value.
    private val darkBackgroundStage = Color(0xFF241E2D)
    private val darkSurfaceSoft = Color(0xFF1F2547)   // = lapis-soft
    private val darkSurfaceMint = Color(0xFF143027)   // = mint-soft
    private val darkSurfacePeach = Color(0xFF3A2A18)  // = saffron-soft
    private val darkSurfaceLilac = Color(0xFF261F44)  // = lilac-soft

    /** True while the dark scheme is active; lets [PardisGradients.dawn] pick its dark stops. */
    var isDarkScheme by mutableStateOf(false)
        private set

    // Base accents stay constant; their *Soft/*Tint backgrounds darken and *Deep text colors brighten
    // in dark (scheme-reactive, set in applyScheme to the canonical handoff values). indigoDeep also
    // anchors the night/lapis gradients — those are pinned to a literal hex in PardisGradients so the
    // gradients don't follow the text brightening.
    val saffron = Color(0xFFF08A2D)
    var saffronDeep by mutableStateOf(Color(0xFFC46A12))
    var saffronSoft by mutableStateOf(Color(0xFFFFE9D2))
    var saffronTint by mutableStateOf(Color(0xFFFFF4E5))
    val indigo = Color(0xFF2436A1)
    var indigoDeep by mutableStateOf(Color(0xFF1A256E))
    val indigoDarker = Color(0xFF0F1849)
    var indigoSoft by mutableStateOf(Color(0xFFE8EBFB))
    var indigoTint by mutableStateOf(Color(0xFFF0F2FC))
    val mint = Color(0xFF2FA876)
    var mintDeep by mutableStateOf(Color(0xFF1F7A52))
    var mintSoft by mutableStateOf(Color(0xFFDEF5E9))
    val lilac = Color(0xFF8B6FE6)
    var lilacDeep by mutableStateOf(Color(0xFF5235B6))
    var lilacSoft by mutableStateOf(Color(0xFFECE6FB))
    val rose = Color(0xFFE1547A)
    var roseDeep by mutableStateOf(Color(0xFFB83A5E))
    var roseSoft by mutableStateOf(Color(0xFFFCDEE6))
    val sun = Color(0xFFF4B53A)
    var sunDeep by mutableStateOf(Color(0xFF9A6B12))
    var sunSoft by mutableStateOf(Color(0xFFFCEAB6))
    val sunPale = Color(0xFFFFD08A) // grad-saffron end stop
    val violet = Color(0xFF4F2EB5) // grad-night / grad-lapis mid stop
    val errorDark = Color(0xFFFF8A80)
    val errorContainerDark = Color(0xFF6D2C2C)
    val onErrorContainerDark = Color(0xFFFFDAD4)
    val success = Color(0xFF2FA876)
    val warning = Color(0xFFF4B53A)

    // ── On-dark overlays (white/black with alpha) ─────────────
    // Text & icons drawn over dark gradient cards and scene art.
    val inkOnDark = Color(0xFFFFFFFF) // primary text on dark
    val inkOnDarkSoft = Color(0xD9FFFFFF) // 85% — secondary text
    val inkOnDarkMuted = Color(0xB3FFFFFF) // 70% — labels / eyebrows
    val inkOnDarkFaint = Color(0x80FFFFFF) // 50% — faint text / tracks
    // White surface fills layered on dark (chips, circular icon buttons).
    val surfaceOnDark = Color(0x29FFFFFF) // 16%
    val surfaceOnDarkSoft = Color(0x1FFFFFFF) // 12%
    val surfaceOnDarkFaint = Color(0x14FFFFFF) // 8% — progress tracks
    // Black scrims for legibility under text over imagery/scene art.
    val scrim = Color(0x99000000) // 60%
    val scrimSoft = Color(0x66000000) // 40%

    /**
     * Flip the scheme-reactive neutrals. Called by [PardisTheme] on scheme change (the only writer).
     * Deterministic + idempotent given the same [dark], so it converges on recomposition. Brand /
     * on-dark / scrim tokens are scheme-agnostic and untouched.
     */
    fun applyScheme(dark: Boolean) {
        background = if (dark) darkBackground else Color(0xFFFAF6EE)
        backgroundAlt = if (dark) darkBackgroundAlt else Color(0xFFF3EEDD)
        backgroundStage = if (dark) darkBackgroundStage else Color(0xFFEDE6D6)
        surface = if (dark) darkSurface else Color(0xFFFFFFFF)
        surface2 = if (dark) darkSurface2 else Color(0xFFFDFAF0)
        surfaceSoft = if (dark) darkSurfaceSoft else Color(0xFFE8EBFB)
        surfaceMint = if (dark) darkSurfaceMint else Color(0xFFDEF5E9)
        surfacePeach = if (dark) darkSurfacePeach else Color(0xFFFFE9D2)
        surfaceLilac = if (dark) darkSurfaceLilac else Color(0xFFECE6FB)
        ink = if (dark) darkInk else Color(0xFF14111B)
        inkSoft = if (dark) darkInkSoft else Color(0xFF4B4760)
        inkMuted = if (dark) darkInkMuted else Color(0xFF8A8499)
        inkFaint = if (dark) darkInkFaint else Color(0xFFB6B0C0)
        border = if (dark) darkBorder else Color(0xFFECE3D0)
        borderSoft = if (dark) darkBorderSoft else Color(0xFFF2ECDD)
        borderStrong = if (dark) darkBorderStrong else Color(0xFFDDD2BC)
        error = if (dark) errorDark else Color(0xFFEF4444)

        // Accent soft/tint backgrounds darken; accent deep text colors brighten (canonical handoff).
        saffronSoft = if (dark) Color(0xFF3A2A18) else Color(0xFFFFE9D2)
        saffronTint = if (dark) Color(0xFF241B12) else Color(0xFFFFF4E5)
        saffronDeep = if (dark) Color(0xFFF2B074) else Color(0xFFC46A12)
        indigoSoft = if (dark) Color(0xFF1F2547) else Color(0xFFE8EBFB)
        indigoTint = if (dark) Color(0xFF181C36) else Color(0xFFF0F2FC)
        indigoDeep = if (dark) Color(0xFFAEB9F4) else Color(0xFF1A256E)
        mintSoft = if (dark) Color(0xFF143027) else Color(0xFFDEF5E9)
        mintDeep = if (dark) Color(0xFF7FD9B0) else Color(0xFF1F7A52)
        lilacSoft = if (dark) Color(0xFF261F44) else Color(0xFFECE6FB)
        lilacDeep = if (dark) Color(0xFFC5B4F4) else Color(0xFF5235B6)
        roseSoft = if (dark) Color(0xFF3A1E28) else Color(0xFFFCDEE6)
        roseDeep = if (dark) Color(0xFFF18AA6) else Color(0xFFB83A5E)
        sunSoft = if (dark) Color(0xFF34280F) else Color(0xFFFCEAB6)
        sunDeep = if (dark) Color(0xFFF2C14E) else Color(0xFF9A6B12)

        isDarkScheme = dark
    }
}

/**
 * Named multi-stop gradients mirroring app.css `--grad-*`. Brushes resolve to the
 * drawn area at paint time, so a single instance adapts to any card size.
 */
object PardisGradients {
    // night/lapis are pinned to literal hexes (handoff --grad-night / --grad-lapis), NOT the
    // PardisColors.indigoDeep token — that brightens for *text* in dark mode, but these gradients
    // must stay constant across schemes. (A literal also avoids depending on object-init order.)
    private val gradIndigoDeep = Color(0xFF1A256E)

    // --grad-night: 180deg indigoDeep → indigo → violet
    val night = Brush.verticalGradient(
        listOf(gradIndigoDeep, PardisColors.indigo, PardisColors.violet),
    )
    // --grad-lapis: 135deg indigo → violet → indigoDeep
    val lapis = Brush.linearGradient(
        listOf(PardisColors.indigo, PardisColors.violet, gradIndigoDeep),
    )
    // --grad-saffron: 135deg saffron → sun → sunPale
    val saffron = Brush.linearGradient(
        listOf(PardisColors.saffron, PardisColors.sun, PardisColors.sunPale),
    )
    // --grad-dawn: light surfacePeach→surfaceLilac→indigoSoft; dark uses the handoff's explicit
    // dark dawn stops. A getter so it re-reads the reactive isDarkScheme flag and re-resolves when
    // the scheme flips (read inside a composable, it subscribes).
    val dawn: Brush
        get() = if (PardisColors.isDarkScheme) {
            Brush.linearGradient(listOf(Color(0xFF221A2E), Color(0xFF1A2140), Color(0xFF16223E)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFFFE9D2), Color(0xFFECE6FB), Color(0xFFE8EBFB)))
        }
}

object PardisSpacing {
    val none = 0.dp
    val hairline = 1.dp
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 40.dp
}

object PardisRadius {
    val xs = 8.dp
    val sm = 12.dp
    val base = 16.dp
    val md = 20.dp
    val lg = 26.dp
    val xl = 34.dp
    val xxl = 34.dp
    val full = 999.dp
}

object PardisMotion {
    const val fast = 150
    const val normal = 250
    const val slow = 400
}

object PardisTypography {
    // sizes in sp (use .sp when applying to Text)
    val xs = 11
    val sm = 13
    val base = 15
    val lg = 18
    val xl = 23
    val xxl = 30
    val xxxl = 34
    // weights (use FontWeight)
    const val weightNormal = 400
    const val weightMedium = 500
    const val weightSemiBold = 700
    const val weightBold = 800
    // line heights (multipliers)
    const val lineHeightDisplay = 0.98f
    const val lineHeightHeading = 1.08f
    const val lineHeightTight = 1.35f
    const val lineHeightNormal = 1.55f
    const val lineHeightRelaxed = 1.7f
}

object PardisShadows {
    // For Compose, use .shadow(elevation) or custom; values are reference (in dp)
    val xs = 1.dp
    val sm = 4.dp
    val md = 12.dp
    val lg = 24.dp
}
