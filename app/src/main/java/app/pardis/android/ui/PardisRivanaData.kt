/**
 * Android resolution of the shared [RivanaBadgeIcon] semantic enum to the local [PardisIconKind]
 * icon set. The Rivana content itself now lives in `core/model` (`RivanaContent`), shared with iOS.
 */
package app.pardis.android.ui

import app.pardis.core.model.RivanaBadgeIcon

internal fun RivanaBadgeIcon.toPardisIconKind(): PardisIconKind = when (this) {
    RivanaBadgeIcon.Compass -> PardisIconKind.Compass
    RivanaBadgeIcon.Flame -> PardisIconKind.Flame
    RivanaBadgeIcon.Feather -> PardisIconKind.Feather
    RivanaBadgeIcon.Crown -> PardisIconKind.Crown
    RivanaBadgeIcon.Moon -> PardisIconKind.Moon
    RivanaBadgeIcon.Mic -> PardisIconKind.Mic
}
