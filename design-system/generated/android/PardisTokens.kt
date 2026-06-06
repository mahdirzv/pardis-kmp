package app.pardis.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Generated from design-system/tokens.json + web neutral.ts
 * Pardis palette only. Use these in Compose UI.
 */
object PardisColors {
    val background = Color(0xFFFAF6EE)
    val backgroundAlt = Color(0xFFF3EEDD)
    val backgroundStage = Color(0xFFEDE6D6)
    val surface = Color(0xFFFFFFFF)
    val surface2 = Color(0xFFFDFAF0)
    val surfaceSoft = Color(0xFFE8EBFB)
    val surfaceMint = Color(0xFFDEF5E9)
    val surfacePeach = Color(0xFFFFE9D2)
    val surfaceLilac = Color(0xFFECE6FB)
    val saffron = Color(0xFFF08A2D)
    val saffronDeep = Color(0xFFC46A12)
    val saffronSoft = Color(0xFFFFE9D2)
    val saffronTint = Color(0xFFFFF4E5)
    val indigo = Color(0xFF2436A1)
    val indigoDeep = Color(0xFF1A256E)
    val indigoDarker = Color(0xFF0F1849)
    val indigoSoft = Color(0xFFE8EBFB)
    val indigoTint = Color(0xFFF0F2FC)
    val mint = Color(0xFF2FA876)
    val mintDeep = Color(0xFF1F7A52)
    val mintSoft = Color(0xFFDEF5E9)
    val lilac = Color(0xFF8B6FE6)
    val lilacDeep = Color(0xFF5235B6)
    val lilacSoft = Color(0xFFECE6FB)
    val rose = Color(0xFFE1547A)
    val roseDeep = Color(0xFFB83A5E)
    val roseSoft = Color(0xFFFCDEE6)
    val sun = Color(0xFFF4B53A)
    val sunDeep = Color(0xFF9A6B12)
    val sunSoft = Color(0xFFFCEAB6)
    val ink = Color(0xFF14111B)
    val inkSoft = Color(0xFF4B4760)
    val inkMuted = Color(0xFF8A8499)
    val inkFaint = Color(0xFFB6B0C0)
    val inkOnDark = Color(0xFFFFFFFF)
    val border = Color(0xFFECE3D0)
    val borderSoft = Color(0xFFF2ECDD)
    val borderStrong = Color(0xFFDDD2BC)
    val error = Color(0xFFEF4444)
    val success = Color(0xFF2FA876)
    val warning = Color(0xFFF4B53A)
}

object PardisSpacing {
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
    val md = 16.dp
    val lg = 20.dp
    val xl = 26.dp
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