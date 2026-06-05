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
    val indigoSoft = Color(0xFFE8EBFB)
    val indigoTint = Color(0xFFE8EBFB)
    val mint = Color(0xFF34B57F)
    val mintDeep = Color(0xFF1F7A52)
    val mintSoft = Color(0xFFDEF5E9)
    val lilac = Color(0xFF8B6FE6)
    val lilacDeep = Color(0xFF5235B6)
    val lilacSoft = Color(0xFFECE6FB)
    val ink = Color(0xFF14111B)
    val inkSoft = Color(0xFF4B4760)
    val inkMuted = Color(0xFF8A8499)
    val inkFaint = Color(0xFFB8B2C5)
    val inkOnDark = Color(0xFFFFFFFF)
    val border = Color(0xFFECE3D0)
    val borderStrong = Color(0xFFD9CDB1)
    val error = Color(0xFFEF4444)
    val success = Color(0xFF34B57F)
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
    val sm = 8.dp
    val md = 12.dp
    val lg = 18.dp
    val xl = 24.dp
    val full = 999.dp
}

object PardisMotion {
    const val fast = 150
    const val normal = 250
    const val slow = 400
}

object PardisTypography {
    // sizes in sp (use .sp when applying to Text)
    val xs = 12
    val sm = 14
    val base = 16
    val lg = 18
    val xl = 20
    val xxl = 24
    val xxxl = 30
    // weights (use FontWeight)
    const val weightNormal = 400
    const val weightMedium = 500
    const val weightSemiBold = 600
    const val weightBold = 700
    // line heights (multipliers)
    const val lineHeightTight = 1.05f
    const val lineHeightNormal = 1.55f
    const val lineHeightRelaxed = 1.7f
}

object PardisShadows {
    // For Compose, use shadow() modifier or custom; values are reference
    const val smBlur = 2
    const val mdBlur = 8
    const val lgBlur = 20
}