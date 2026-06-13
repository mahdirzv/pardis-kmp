import SwiftUI
import Shared

/// End-of-story celebration, mirroring Android `PardisFinishScreen`: night gradient, radial
/// glow, confetti burst, HeroSealMedallion, StarRow, stats, garden chips, Next/Done CTA bar.
/// Note: omits the nightsky/rosette pattern overlays like other iOS screens (needs
/// asset-catalog motif images - known follow-up).
struct FinishScreen: View {
    let slug: String
    let onNextStory: (String) -> Void
    let onDone: () -> Void

    @State private var model = FinishSharedViewModel()

    var body: some View {
        ZStack {
            PardisGradients.night.ignoresSafeArea()
            // radial glow behind the medallion
            RadialGradient(
                stops: [
                    .init(color: PardisColors.sun.opacity(0.20), location: 0.0),
                    .init(color: PardisColors.lilac.opacity(0.10), location: 0.38),
                    .init(color: .clear, location: 0.66),
                ],
                center: .center,
                startRadius: 0,
                endRadius: 230
            )
            .frame(width: 460, height: 460)
            .frame(maxHeight: .infinity, alignment: .top)
            .offset(y: -40)

            ConfettiBurst()

            ScrollView {
                VStack(spacing: 0) {
                    Spacer().frame(height: 28)
                    HeroSealMedallion(sceneSeed: model.title.isEmpty ? slug : model.title)
                    Spacer().frame(height: 18)
                    StarRow()
                    Spacer().frame(height: 18)
                    Text("CHAPTER COMPLETE")
                        .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                        .kerning(0.6)
                        .foregroundStyle(PardisColors.inkOnDarkMuted)
                    Spacer().frame(height: 7)
                    Text("Âfarin, you did it!")
                        .font(PardisFonts.display(size: PardisTypography.xxxl, weight: .heavy))
                        .foregroundStyle(PardisColors.inkOnDark)
                        .multilineTextAlignment(.center)
                    Text("آفرین! یک قصه‌ی دیگر تمام شد")
                        .font(PardisFonts.persian(size: PardisTypography.base, weight: .semibold))
                        .foregroundStyle(PardisColors.inkOnDarkMuted)
                    if !model.title.isEmpty {
                        Spacer().frame(height: 6)
                        Text("You finished \(model.title)")
                            .font(PardisFonts.body(size: PardisTypography.sm, weight: .regular))
                            .foregroundStyle(PardisColors.inkOnDarkFaint)
                    }

                    Spacer().frame(height: 24)
                    HStack(spacing: PardisSpacing.sm) {
                        FinishStat(icon: .flame, value: "+1", label: "night streak", iconTint: PardisColors.saffron)
                        FinishStat(icon: .feather, value: "+\(model.words.count)", label: "new words", iconTint: PardisColors.lilac)
                        FinishStat(icon: .starFill, value: "+20", label: "stars", iconTint: PardisColors.sun)
                    }

                    if !model.words.isEmpty {
                        Spacer().frame(height: 26)
                        HStack(spacing: 7) {
                            PardisIcon(kind: .sprout, size: 16, color: PardisColors.mint)
                            Text("NEW WORDS IN YOUR GARDEN")
                                .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                                .kerning(0.6)
                                .foregroundStyle(PardisColors.inkOnDarkMuted)
                        }
                        Spacer().frame(height: 11)
                        GardenChips(words: Array(model.words.prefix(6)))
                    }
                    Spacer().frame(height: 110)
                }
                .padding(.horizontal, PardisSpacing.lg)
            }

            // CTA bar
            VStack {
                Spacer()
                HStack(spacing: PardisSpacing.sm) {
                    Button(action: {
                        if let next = model.nextSlug { onNextStory(next) } else { onDone() }
                    }) {
                        HStack(spacing: 8) {
                            Text("Next story")
                                .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                                .foregroundStyle(PardisColors.indigoDeep)
                            PardisIcon(kind: .chevRight, size: 18, color: PardisColors.indigoDeep)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 15)
                        .background(PardisColors.inkOnDark)
                        .clipShape(Capsule(style: .continuous))
                    }
                    .buttonStyle(.plain)

                    Button(action: onDone) {
                        Text("Done")
                            .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                            .foregroundStyle(PardisColors.inkOnDark)
                            .frame(width: 116)
                            .padding(.vertical, 15)
                            .overlay(
                                Capsule(style: .continuous)
                                    .stroke(PardisColors.inkOnDarkFaint, lineWidth: 1.5)
                            )
                    }
                    .buttonStyle(.plain)
                }
                .padding(.horizontal, PardisSpacing.lg)
                .padding(.vertical, PardisSpacing.md)
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .task { await model.activate() }
        .onAppear { model.load(slug: slug) }
    }
}

private struct HeroSealMedallion: View {
    let sceneSeed: String

    @State private var spinning = false

    var body: some View {
        ZStack {
            // rotating dashed gold ring
            Circle()
                .stroke(
                    PardisColors.sun.opacity(0.45),
                    style: StrokeStyle(lineWidth: 2, dash: [3, 3.7])
                )
                .frame(width: 186, height: 186)
                .rotationEffect(.degrees(spinning ? 360 : 0))
                .animation(.linear(duration: 26).repeatForever(autoreverses: false), value: spinning)
            // static white ring
            Circle()
                .stroke(PardisColors.surfaceOnDark, lineWidth: 1)
                .frame(width: 162, height: 162)
            // scene art seal with a vignette to the night background
            ZStack {
                PardisSceneArt(seed: sceneSeed)
                RadialGradient(
                    stops: [
                        .init(color: .clear, location: 0.3),
                        .init(color: PardisColors.indigoDeep.opacity(0.45), location: 1.0),
                    ],
                    center: .center,
                    startRadius: 0,
                    endRadius: 71
                )
            }
            .frame(width: 142, height: 142)
            .clipShape(Circle())
            // gold check seal
            ZStack {
                Circle()
                    .fill(LinearGradient(colors: [PardisColors.sun, PardisColors.saffronDeep], startPoint: .topLeading, endPoint: .bottomTrailing))
                Circle().stroke(PardisColors.inkOnDark, lineWidth: 2)
                PardisIcon(kind: .check, size: 26, color: PardisColors.inkOnDark)
            }
            .frame(width: 50, height: 50)
            .offset(y: 94 - 25 - 2)
        }
        .frame(width: 188, height: 188)
        .onAppear { spinning = true }
    }
}

private struct StarRow: View {
    var body: some View {
        HStack(alignment: .bottom, spacing: 6) {
            PardisIcon(kind: .starFill, size: 32, color: PardisColors.sun)
            PardisIcon(kind: .starFill, size: 40, color: PardisColors.sun)
                .offset(y: -6)
            PardisIcon(kind: .starFill, size: 32, color: PardisColors.sun)
        }
    }
}

private struct FinishStat: View {
    let icon: PardisIconKind
    let value: String
    let label: String
    let iconTint: Color

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .fill(PardisColors.surfaceOnDarkSoft)
                    .frame(width: 38, height: 38)
                PardisIcon(kind: icon, size: 21, color: iconTint)
            }
            Spacer().frame(height: 8)
            Text(value)
                .font(PardisFonts.display(size: PardisTypography.xl, weight: .heavy))
                .foregroundStyle(PardisColors.inkOnDark)
            Text(label)
                .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                .foregroundStyle(PardisColors.inkOnDarkMuted)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 15)
        .padding(.horizontal, 6)
        .background(PardisColors.surfaceOnDark)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

private struct GardenChips: View {
    let words: [VocabItem]

    var body: some View {
        PardisFlowLayout(spacing: PardisSpacing.sm) {
            ForEach(words, id: \.fa) { w in
                HStack(spacing: 8) {
                    Text(w.fa)
                        .font(PardisFonts.persian(size: PardisTypography.base, weight: .semibold))
                        .foregroundStyle(PardisColors.inkOnDark)
                    Text(w.translit)
                        .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                        .foregroundStyle(PardisColors.inkOnDarkFaint)
                }
                .padding(.horizontal, 13)
                .padding(.vertical, 8)
                .background(PardisColors.surfaceOnDark)
                .clipShape(Capsule(style: .continuous))
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

/// One-shot confetti burst: mixed sparkles, discs and bars falling once on entry,
/// mirroring Android's Animatable(0→1 over 3.2s) particle field.
private struct ConfettiBurst: View {
    @State private var startDate: Date? = nil

    private let colors: [Color] = [
        PardisColors.sun, PardisColors.saffron, PardisColors.lilac, PardisColors.mint, PardisColors.inkOnDark,
    ]

    var body: some View {
        TimelineView(.animation) { timeline in
            let t: Double = {
                guard let startDate else { return 0 }
                return min(timeline.date.timeIntervalSince(startDate) / 3.2, 1.0)
            }()
            GeometryReader { geo in
                let w = geo.size.width
                let h = geo.size.height
                ForEach(0..<24, id: \.self) { i in
                    let leftFrac = Double((i * 38 + 6) % 100) / 100.0
                    let delayFrac = Double(i % 9) * 0.05
                    let durFrac = 0.55 + Double(i % 5) * 0.1
                    let local = min(max((t - delayFrac) / durFrac, 0), 1)
                    let drift = Double((i % 5) - 2) * 26
                    let size = CGFloat(6 + (i % 4) * 2)
                    let yFrac = -0.06 + (1.18 - (-0.06)) * local
                    let alpha: Double = (local <= 0 || local >= 1) ? 0 : (local < 0.12 ? local / 0.12 : 1)
                    let x = w / 2 + w * (leftFrac - 0.5) + drift * local
                    confettiPiece(kind: i % 3, color: colors[i % colors.count], size: size)
                        .rotationEffect(.degrees(540 * local))
                        .opacity(alpha)
                        .position(x: x, y: h * yFrac)
                }
            }
        }
        .allowsHitTesting(false)
        .onAppear { startDate = Date() }
    }

    @ViewBuilder
    private func confettiPiece(kind: Int, color: Color, size: CGFloat) -> some View {
        switch kind {
        case 0:
            PardisIcon(kind: .sparkle, size: size + 8, color: color)
        case 1:
            Rectangle().fill(color).frame(width: size, height: size / 2)
        default:
            Circle().fill(color).frame(width: size, height: size)
        }
    }
}
