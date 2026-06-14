import SwiftUI

extension CustomizableToolbarContent {
    /// On iOS 26 the system gives toolbar items a shared Liquid Glass background. Our toolbar buttons
    /// (HeroCircleButton / ReaderIconButton) already draw their own circular background, so the two
    /// stack into a visible "double background". Hide the system's shared background where it exists;
    /// a no-op on earlier OSes that don't add one.
    @ToolbarContentBuilder
    func hidingSharedBackground() -> some CustomizableToolbarContent {
        if #available(iOS 26.0, *) {
            sharedBackgroundVisibility(.hidden)
        } else {
            self
        }
    }
}
