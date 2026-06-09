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

/// On-dark color tokens + scrim, mirroring the hand-authored extensions in Android's
/// `app/.../design/PardisTokens.kt` (the generated token file carries only `inkOnDark`).
/// Added here in the hand-authored iOS theme layer rather than the generated file.
extension PardisColors {
    static let inkOnDarkSoft = Color.white.opacity(0.85)   // secondary text
    static let inkOnDarkMuted = Color.white.opacity(0.70)  // labels / eyebrows
    static let inkOnDarkFaint = Color.white.opacity(0.50)  // faint text / tracks
    static let surfaceOnDark = Color.white.opacity(0.16)
    static let surfaceOnDarkSoft = Color.white.opacity(0.12)
    static let surfaceOnDarkFaint = Color.white.opacity(0.08) // progress tracks
    static let scrimSoft = Color.black.opacity(0.45)
    static let scrim = Color.black.opacity(0.55)
    // Deep-night anchors used by the bedtime/lullaby gradients (mirror Android's inline hexes).
    static let nightMid = Color(hex: "#0F1330")
    static let nightDeep = Color(hex: "#0A0E22")
    // Gradient mid/end stops present in Android's hand-authored tokens but not the generated file.
    static let violet = Color(hex: "#4F2EB5")    // grad-lapis mid stop
    static let sunPale = Color(hex: "#FFD08A")   // grad-saffron end stop
}

/// Semantic gradient tokens, mirroring Android `PardisGradients`. Lives in the hand-authored iOS
/// theme layer (not the generated token file). 135° ≈ topLeading → bottomTrailing.
struct PardisGradients {
    static let dawn = LinearGradient(
        colors: [PardisColors.surfacePeach, PardisColors.surfaceLilac, PardisColors.indigoSoft],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    static let bedtimeNight = LinearGradient(
        colors: [PardisColors.indigoDeep, PardisColors.nightMid, PardisColors.nightDeep],
        startPoint: .top,
        endPoint: .bottom
    )
    static let lullabyNight = LinearGradient(
        stops: [
            .init(color: PardisColors.indigoDeep, location: 0.0),
            .init(color: PardisColors.indigoDarker, location: 0.6),
            .init(color: PardisColors.nightDeep, location: 1.0),
        ],
        startPoint: .top,
        endPoint: .bottom
    )
    static let lapis = LinearGradient(
        colors: [PardisColors.indigo, PardisColors.violet, PardisColors.indigoDeep],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    static let saffron = LinearGradient(
        colors: [PardisColors.saffron, PardisColors.sun, PardisColors.sunPale],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
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
