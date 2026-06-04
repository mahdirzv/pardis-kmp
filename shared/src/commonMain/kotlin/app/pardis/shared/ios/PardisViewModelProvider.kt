package app.pardis.shared.ios

import app.pardis.shared.library.LibraryViewModel
import org.koin.mp.KoinPlatform

/**
 * iOS entry point to resolve shared ViewModels from Koin (called from Swift adapters).
 * Keep thin; actual logic stays in the ViewModels.
 */
object PardisViewModelProvider {
    fun libraryViewModel(): LibraryViewModel =
        KoinPlatform.getKoin().get()
}