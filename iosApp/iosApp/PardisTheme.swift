import SwiftUI

struct PardisScreenBackground: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background {
                ZStack {
                    LinearGradient(
                        colors: [PardisColors.background, PardisColors.backgroundAlt],
                        startPoint: .top,
                        endPoint: .bottom
                    )

                    RadialGradient(
                        colors: [PardisColors.indigo.opacity(0.10), .clear],
                        center: .topLeading,
                        startRadius: 0,
                        endRadius: 220
                    )

                    RadialGradient(
                        colors: [PardisColors.saffron.opacity(0.10), .clear],
                        center: .topTrailing,
                        startRadius: 0,
                        endRadius: 220
                    )

                    RadialGradient(
                        colors: [PardisColors.lilac.opacity(0.08), .clear],
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
            .background(PardisColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .stroke(PardisColors.borderSoft, lineWidth: 1)
            )
            .shadow(color: PardisColors.ink.opacity(0.10), radius: PardisShadows.mdRadius, x: 0, y: 8)
    }
}

struct PardisPrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: PardisTypography.base, weight: .bold, design: .rounded))
            .foregroundStyle(PardisColors.inkOnDark)
            .padding(.horizontal, PardisSpacing.md)
            .frame(height: 50)
            .frame(maxWidth: .infinity)
            .background(PardisColors.saffron)
            .clipShape(Capsule(style: .continuous))
            .shadow(color: PardisColors.saffron.opacity(0.28), radius: 12, x: 0, y: 8)
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

