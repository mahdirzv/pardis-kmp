import SwiftUI
import Shared

private let todayAvatarSize: CGFloat = 46
private let todayShelfCoverWidth: CGFloat = 150
private let todayShelfCoverHeight: CGFloat = 132
private let todayCollectionWidth: CGFloat = 168
private let todayCollectionHeight: CGFloat = 110
private let todayTonightArt: CGFloat = 64

/// "Today" tab (v2), mirroring Android `TodayScreen`: greeting, streak strip, continue-reading,
/// tonight's bedtime, new-this-week shelf, word of the day, explore collections.
///
/// Font mapping follows Android's Material typography scheme (`PardisTheme.kt`): eyebrow labels
/// are `labelSmall` → mono + 0.6 tracking; `displayLarge` → display/xxxl; `bodyMedium` → body/sm;
/// `bodySmall` → body/sm/medium. Gutter padding is applied per-section (not to the whole column)
/// so the horizontal shelves scroll full-bleed to the screen edge like Android's gutter-spacer rows.
///
/// Note: omits the Paisley (top), Rosette (bedtime) and Vine (word-of-day) pattern overlays like
/// the other iOS screens — needs asset-catalog motif images (known follow-up).
struct TodayView: View {
    let model: LibrarySharedViewModel
    let activeName: String
    let onOpenStory: (String) -> Void
    let onOpenLibrary: () -> Void
    let onOpenBedtime: () -> Void

    private var featured: Story? { model.stories.first }
    private var wordCount: Int { model.stories.reduce(0) { $0 + Int($1.vocabCount) } }

    private func coverURL(_ s: Story) -> URL? {
        (model.localCoverUrls[s.slug] ?? s.coverUrl).flatMap(URL.init(string:))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PardisSpacing.md) {
                TodayGreeting(activeName: activeName)
                    .accessibilityAddTraits(.isHeader)
                    .padding(.horizontal, PardisSpacing.lg)
                TodayStreakStrip(words: wordCount)
                    .padding(.horizontal, PardisSpacing.lg)

                VStack(alignment: .leading, spacing: PardisSpacing.xs) {
                    Text("CONTINUE READING")
                        .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                        .kerning(0.6)
                        .foregroundStyle(PardisColors.inkMuted)
                    if let f = featured {
                        PardisFeaturedStoryCard(
                            titleEn: f.titleEn, titleFa: f.titleFa, ageBand: f.ageBand,
                            minutes: f.minutes, vocabCount: f.vocabCount, coverUrl: coverURL(f),
                            onOpen: { onOpenStory(f.slug) },
                            eyebrow: "Continue reading", actionLabel: "Start reading"
                        )
                    } else {
                        PardisPanel {
                            Text(model.isLoading ? "Loading today's stories…" : "Refresh Library to load today's reading list.")
                                .font(PardisFonts.body(size: PardisTypography.base, weight: .regular))
                                .foregroundStyle(PardisColors.inkSoft)
                        }
                    }
                }
                .padding(.horizontal, PardisSpacing.lg)

                TonightBedtimeCard(onOpen: onOpenBedtime)
                    .padding(.horizontal, PardisSpacing.lg)

                VStack(alignment: .leading, spacing: PardisSpacing.sm) {
                    PardisSectionHeader(title: "New this week", subtitle: "تازه‌ها", actionLabel: "See all", action: onOpenLibrary)
                        .padding(.horizontal, PardisSpacing.lg)
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: PardisSpacing.sm) {
                            ForEach(model.stories, id: \.slug) { s in
                                TodayShelfCover(titleEn: s.titleEn, titleFa: s.titleFa, cover: coverURL(s)) { onOpenStory(s.slug) }
                            }
                        }
                        .padding(.horizontal, PardisSpacing.lg)
                    }
                }

                WordOfDayCard()
                    .padding(.horizontal, PardisSpacing.lg)

                VStack(alignment: .leading, spacing: PardisSpacing.sm) {
                    PardisSectionHeader(title: "Explore collections", subtitle: "مجموعه‌ها")
                        .padding(.horizontal, PardisSpacing.lg)
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: PardisSpacing.sm) {
                            CollectionCard(name: "Shahnameh Heroes", fa: "پهلوانان", variant: 0, onTap: onOpenLibrary)
                            CollectionCard(name: "Creatures of Myth", fa: "هیولاها", variant: 5, onTap: onOpenLibrary)
                            CollectionCard(name: "Voyages", fa: "سفرها", variant: 4, onTap: onOpenLibrary)
                        }
                        .padding(.horizontal, PardisSpacing.lg)
                    }
                }

                Text("پایانِ امروز · فردا قصه‌ای تازه")
                    .font(PardisFonts.persian(size: PardisTypography.sm, weight: .medium))
                    .foregroundStyle(PardisColors.inkFaint)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, PardisSpacing.md)
            }
            .padding(.top, PardisSpacing.xl)
        }
    }
}

private struct TodayGreeting: View {
    let activeName: String

    private var greeting: String {
        let h = Calendar.current.component(.hour, from: Date())
        switch h { case ..<12: return "Good morning"; case ..<18: return "Good afternoon"; default: return "Good evening" }
    }

    // Weekday name, mirroring the handoff eyebrow `{greeting} · {weekday}`.
    private var weekday: String { Date().formatted(.dateTime.weekday(.wide)) }

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                // Eyebrow is greeting + weekday (no name); the name goes on the Salâm title.
                Text("\(greeting) · \(weekday)".uppercased())
                    .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                    .kerning(0.6)
                    .foregroundStyle(PardisColors.saffronDeep)
                Text("Salâm, \(activeName)")
                    .font(PardisFonts.display(size: PardisTypography.xxxl, weight: .heavy))
                    .foregroundStyle(PardisColors.ink)
                Text("سلام، روزت پر از قصه")
                    .font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.inkMuted)
            }
            Spacer(minLength: 0)
            ZStack {
                Circle().fill(PardisColors.saffron)
                Text(String(activeName.prefix(1)))
                    .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                    .foregroundStyle(PardisColors.inkOnDark)
            }
            .frame(width: todayAvatarSize, height: todayAvatarSize)
        }
    }
}

private struct TodayStreakStrip: View {
    let words: Int
    var body: some View {
        HStack(spacing: PardisSpacing.sm) {
            StreakTile(icon: .flame, value: "7 nights", label: "reading streak", bg: PardisColors.saffronTint, border: PardisColors.saffronSoft, fg: PardisColors.saffronDeep)
            StreakTile(icon: .feather, value: "\(words) words", label: "collected", bg: PardisColors.indigoTint, border: PardisColors.indigoSoft, fg: PardisColors.indigoDeep)
        }
    }
}

private struct StreakTile: View {
    let icon: PardisIconKind
    let value: String
    let label: String
    let bg: Color
    let border: Color
    let fg: Color
    var body: some View {
        HStack(spacing: PardisSpacing.sm) {
            PardisIcon(kind: icon, size: 18, color: fg)
            VStack(alignment: .leading, spacing: 0) {
                Text(value).font(PardisFonts.display(size: PardisTypography.base, weight: .heavy)).foregroundStyle(fg)
                Text(label.uppercased())
                    .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                    .kerning(0.6)
                    .foregroundStyle(fg)
            }
            Spacer(minLength: 0)
        }
        .padding(14)
        .background(bg)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous).stroke(border, lineWidth: PardisSpacing.hairline))
    }
}

private struct TonightBedtimeCard: View {
    let onOpen: () -> Void
    var body: some View {
        Button(action: onOpen) {
            HStack(spacing: 14) {
                PardisSceneArt(seed: "tonight", forcedVariant: 6)
                    .frame(width: todayTonightArt, height: todayTonightArt)
                    .clipShape(RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous))
                VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                    Text("TONIGHT'S BEDTIME")
                        .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                        .kerning(0.6)
                        .foregroundStyle(PardisColors.inkOnDarkMuted)
                    Text("Laay Laay, Little Star").font(PardisFonts.display(size: PardisTypography.base, weight: .bold)).foregroundStyle(PardisColors.inkOnDark)
                    Text("12 min · sleep timer ready").font(PardisFonts.body(size: PardisTypography.sm, weight: .medium)).foregroundStyle(PardisColors.inkOnDarkMuted)
                }
                Spacer(minLength: 0)
                ZStack {
                    Circle().fill(PardisColors.surfaceOnDark)
                    PardisIcon(kind: .moon, size: 18, color: PardisColors.inkOnDark)
                }.frame(width: 44, height: 44)
            }
            .padding(16)
            .background(PardisGradients.night)
            .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

private struct TodayShelfCover: View {
    let titleEn: String
    let titleFa: String
    let cover: URL?
    let onTap: () -> Void
    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: PardisSpacing.xs) {
                PardisAsyncImageFrame(url: cover, accessibilityLabel: titleEn, width: todayShelfCoverWidth, height: todayShelfCoverHeight)
                Text(titleEn).font(PardisFonts.display(size: PardisTypography.base, weight: .semibold)).foregroundStyle(PardisColors.ink).lineLimit(1)
                Text(titleFa).font(PardisFonts.persian(size: PardisTypography.sm, weight: .medium)).foregroundStyle(PardisColors.inkMuted).lineLimit(1)
            }
            .frame(width: todayShelfCoverWidth)
        }
        .buttonStyle(.plain)
    }
}

private struct WordOfDayCard: View {
    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.xs) {
            HStack {
                Text("WORD OF THE DAY")
                    .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                    .kerning(0.6)
                    .foregroundStyle(PardisColors.lilacDeep)
                Spacer()
                PardisIcon(kind: .volume, size: 18, color: PardisColors.lilacDeep)
            }
            // The hero word is the handoff's literal 40pt (no nearer type token); translit is mono.
            Text("دلیر").font(PardisFonts.persian(size: 40, weight: .bold)).foregroundStyle(PardisColors.lilacDeep)
            Text("delir — \"brave\"").font(PardisFonts.mono(size: PardisTypography.sm, weight: .medium)).foregroundStyle(PardisColors.lilacDeep)
            Text("A delir heart fears nothing.").font(PardisFonts.body(size: PardisTypography.sm, weight: .regular)).italic().foregroundStyle(PardisColors.inkSoft)
        }
        .padding(.horizontal, 18).padding(.top, 18).padding(.bottom, 16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(PardisColors.lilacSoft)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous).stroke(PardisColors.lilac.opacity(0.22), lineWidth: PardisSpacing.hairline))
    }
}

private struct CollectionCard: View {
    let name: String
    let fa: String
    let variant: Int
    let onTap: () -> Void
    var body: some View {
        Button(action: onTap) {
            ZStack(alignment: .bottomLeading) {
                PardisSceneArt(seed: name, forcedVariant: variant)
                LinearGradient(colors: [.clear, PardisColors.scrim], startPoint: .top, endPoint: .bottom)
                VStack(alignment: .leading, spacing: 0) {
                    Text(name).font(PardisFonts.display(size: PardisTypography.base, weight: .bold)).foregroundStyle(PardisColors.inkOnDark).lineLimit(1)
                    Text(fa).font(PardisFonts.persian(size: PardisTypography.sm, weight: .medium)).foregroundStyle(PardisColors.inkOnDarkSoft).lineLimit(1)
                }
                .padding(13)
            }
            .frame(width: todayCollectionWidth, height: todayCollectionHeight)
            .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}
