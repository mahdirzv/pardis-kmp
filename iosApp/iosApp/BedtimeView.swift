import SwiftUI
import Shared

// Layout-specific dimensions mirroring Android PardisBedtimeScreen.
private let bedtimeFeaturedHeight: CGFloat = 250
private let bedtimePlayButtonSize: CGFloat = 46
private let bedtimeRowArtSize: CGFloat = 56
private let bedtimeRowPlaySize: CGFloat = 40
private let windDownIconBox: CGFloat = 46

/// "Bedtime" tab — night-themed lullaby shelf, mirroring Android `BedtimeScreen`.
/// `onOpenLullaby` receives the index into `RivanaContent.lullabies`.
struct BedtimeView: View {
    let onOpenLullaby: (Int) -> Void

    private var lullabies: [Lullaby] { RivanaContent.shared.lullabies }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PardisSpacing.md) {
                VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                    Text("SWEET DREAMS")
                        .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                        .foregroundStyle(PardisColors.saffronSoft.opacity(0.75))
                    Text("Bedtime")
                        .font(PardisFonts.display(size: PardisTypography.xxl, weight: .heavy))
                        .foregroundStyle(PardisColors.inkOnDark)
                    Text("وقتِ خواب · لای‌لای")
                        .font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular))
                        .foregroundStyle(PardisColors.inkOnDarkFaint)
                }

                if let first = lullabies.first {
                    BedtimeFeatured(lullaby: first) { onOpenLullaby(0) }
                }

                WindDownCard()

                Text("Lullaby shelf")
                    .font(PardisFonts.display(size: PardisTypography.lg, weight: .semibold))
                    .foregroundStyle(PardisColors.inkOnDark)
                    .padding(.vertical, PardisSpacing.xs)

                ForEach(Array(lullabies.dropFirst().enumerated()), id: \.element.title) { i, l in
                    LullabyRow(lullaby: l) { onOpenLullaby(i + 1) }
                }

                Text("شب بخیر · خواب‌های خوش")
                    .font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.inkOnDarkFaint)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, PardisSpacing.md)
            }
            .padding(.horizontal, PardisSpacing.lg)
            .padding(.top, PardisSpacing.xl)
        }
        .background(PardisGradients.bedtimeNight.ignoresSafeArea())
    }
}

private struct BedtimeFeatured: View {
    let lullaby: Lullaby
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            ZStack(alignment: .bottomLeading) {
                PardisSceneArt(seed: lullaby.title, forcedVariant: Int(lullaby.sceneVariant))
                LinearGradient(colors: [.clear, Color(hex: "#B30F0C1E")], startPoint: .top, endPoint: .bottom)

                VStack(alignment: .leading, spacing: 0) {
                    HStack(spacing: 5) {
                        PardisIcon(kind: .moon, size: 14, color: PardisColors.inkOnDark)
                        Text("Lullaby of the night")
                            .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                            .foregroundStyle(PardisColors.inkOnDark)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, PardisSpacing.xs)
                    .background(PardisColors.surfaceOnDark)
                    .clipShape(Capsule(style: .continuous))

                    Spacer().frame(height: PardisSpacing.sm + PardisSpacing.hairline)

                    Text(lullaby.title)
                        .font(PardisFonts.display(size: PardisTypography.xl, weight: .bold))
                        .foregroundStyle(PardisColors.inkOnDark)
                    Text(lullaby.titleFa)
                        .font(PardisFonts.persian(size: PardisTypography.lg, weight: .regular))
                        .foregroundStyle(PardisColors.inkOnDarkMuted)

                    Spacer().frame(height: PardisSpacing.sm + PardisSpacing.xs)

                    HStack(spacing: PardisSpacing.sm + PardisSpacing.xxs) {
                        ZStack {
                            Circle().fill(PardisColors.inkOnDark)
                            PardisIcon(kind: .play, size: 18, color: PardisColors.indigoDeep)
                        }
                        .frame(width: bedtimePlayButtonSize, height: bedtimePlayButtonSize)
                        Text("\(lullaby.minutes) min · \(lullaby.origin)")
                            .font(PardisFonts.body(size: PardisTypography.sm, weight: .regular))
                            .foregroundStyle(PardisColors.inkOnDarkSoft)
                    }
                }
                .padding(18)
            }
            .frame(height: bedtimeFeaturedHeight)
            .clipShape(RoundedRectangle(cornerRadius: PardisRadius.xl, style: .continuous))
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Play \(lullaby.title)")
    }
}

private struct WindDownCard: View {
    var body: some View {
        HStack(spacing: PardisSpacing.sm + PardisSpacing.xs - PardisSpacing.hairline) {
            ZStack {
                RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous).fill(Color(hex: "#2EF4B53A"))
                PardisIcon(kind: .clock, size: 18, color: PardisColors.sun)
            }
            .frame(width: windDownIconBox, height: windDownIconBox)

            VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                Text("Wind-down routine")
                    .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                    .foregroundStyle(PardisColors.inkOnDark)
                Text("One short story, then a lullaby. ~25 min.")
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.inkOnDarkFaint)
            }
            Spacer(minLength: 0)

            Text("Start")
                .font(PardisFonts.body(size: PardisTypography.sm, weight: .bold))
                .foregroundStyle(PardisColors.inkOnDark)
                .padding(.horizontal, PardisSpacing.md - PardisSpacing.xxs)
                .padding(.vertical, PardisSpacing.sm)
                .background(PardisColors.surfaceOnDarkSoft)
                .clipShape(Capsule(style: .continuous))
        }
        .padding(PardisSpacing.md)
        .background(PardisColors.surfaceOnDarkFaint)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
    }
}

private struct LullabyRow: View {
    let lullaby: Lullaby
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: PardisSpacing.sm + PardisSpacing.xs + PardisSpacing.hairline) {
                PardisSceneArt(seed: lullaby.title, forcedVariant: Int(lullaby.sceneVariant))
                    .frame(width: bedtimeRowArtSize, height: bedtimeRowArtSize)
                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))

                VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                    Text(lullaby.title)
                        .font(PardisFonts.display(size: PardisTypography.base, weight: .semibold))
                        .foregroundStyle(PardisColors.inkOnDark)
                        .lineLimit(1)
                    Text(lullaby.titleFa)
                        .font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular))
                        .foregroundStyle(PardisColors.inkOnDarkFaint)
                        .lineLimit(1)
                    Text("\(lullaby.minutes) min · \(lullaby.plays) plays")
                        .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                        .foregroundStyle(PardisColors.inkOnDarkFaint)
                }
                Spacer(minLength: 0)

                ZStack {
                    Circle().fill(PardisColors.surfaceOnDarkSoft)
                    PardisIcon(kind: .play, size: 16, color: PardisColors.inkOnDark)
                }
                .frame(width: bedtimeRowPlaySize, height: bedtimeRowPlaySize)
            }
            .padding(.vertical, PardisSpacing.xs)
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Play \(lullaby.title)")
    }
}
