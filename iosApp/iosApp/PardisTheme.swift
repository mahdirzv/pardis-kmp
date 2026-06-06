import SwiftUI

struct PardisFonts {
    static func display(size: CGFloat, weight: Font.Weight) -> Font {
        .system(size: size, weight: weight, design: .rounded)
    }

    static func body(size: CGFloat, weight: Font.Weight) -> Font {
        .system(size: size, weight: weight, design: .rounded)
    }

    static func persian(size: CGFloat, weight: Font.Weight) -> Font {
        .system(size: size, weight: weight, design: .rounded)
    }

    static func mono(size: CGFloat, weight: Font.Weight) -> Font {
        .system(size: size, weight: weight, design: .monospaced)
    }
}

struct PardisComponentColors {
    static let chipSelectedContainer = PardisColors.ink
    static let chipSelectedContent = PardisColors.surface
    static let chipContainer = PardisColors.surface
    static let chipContent = PardisColors.inkSoft
    static let chipBorder = PardisColors.border
    static let cardContainer = PardisColors.surface
    static let cardBorder = PardisColors.borderSoft
    static let primaryActionContainer = PardisColors.saffron
    static let primaryActionContent = PardisColors.inkOnDark
    static let mediaPlaceholderContainer = PardisColors.surfaceLilac
    static let mediaPlaceholderContent = PardisColors.inkSoft
}

struct PardisScreenBackground: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        content
            .background {
                ZStack {
                    LinearGradient(
                        colors: colorScheme == .dark
                            ? [PardisColors.darkBackground, PardisColors.darkBackgroundAlt]
                            : [PardisColors.background, PardisColors.backgroundAlt],
                        startPoint: .top,
                        endPoint: .bottom
                    )

                    RadialGradient(
                        colors: [PardisColors.indigo.opacity(colorScheme == .dark ? 0.20 : 0.10), .clear],
                        center: .topLeading,
                        startRadius: 0,
                        endRadius: 220
                    )

                    RadialGradient(
                        colors: [PardisColors.saffron.opacity(colorScheme == .dark ? 0.08 : 0.10), .clear],
                        center: .topTrailing,
                        startRadius: 0,
                        endRadius: 220
                    )

                    RadialGradient(
                        colors: [PardisColors.lilac.opacity(colorScheme == .dark ? 0.16 : 0.08), .clear],
                        center: .bottomLeading,
                        startRadius: 0,
                        endRadius: 260
                    )
                }
                .ignoresSafeArea()
            }
    }
}

struct PardisCardSurface: ViewModifier {
    let cornerRadius: CGFloat

    func body(content: Content) -> some View {
        content
            .background(PardisComponentColors.cardContainer)
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .stroke(PardisComponentColors.cardBorder, lineWidth: PardisSpacing.hairline)
            )
            .shadow(color: PardisColors.ink.opacity(0.10), radius: PardisShadows.mdRadius, x: 0, y: 8)
    }
}

struct PardisPrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(PardisFonts.body(size: PardisTypography.base, weight: .bold))
            .foregroundStyle(PardisComponentColors.primaryActionContent)
            .padding(.horizontal, PardisSpacing.md)
            .frame(height: 50)
            .frame(maxWidth: .infinity)
            .background(PardisComponentColors.primaryActionContainer)
            .clipShape(Capsule(style: .continuous))
            .shadow(color: PardisColors.saffron.opacity(0.28), radius: PardisShadows.mdRadius, x: 0, y: 8)
            .scaleEffect(configuration.isPressed ? 0.98 : 1)
            .animation(.easeOut(duration: 0.14), value: configuration.isPressed)
    }
}

extension View {
    func pardisScreenBackground() -> some View {
        modifier(PardisScreenBackground())
    }

    func pardisCardSurface(cornerRadius: CGFloat = PardisRadius.md) -> some View {
        modifier(PardisCardSurface(cornerRadius: cornerRadius))
    }
}
