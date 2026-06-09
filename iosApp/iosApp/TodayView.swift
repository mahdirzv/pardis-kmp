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
                TodayGreeting(activeName: activeName).accessibilityAddTraits(.isHeader)
                TodayStreakStrip(words: wordCount)

                VStack(alignment: .leading, spacing: PardisSpacing.xs) {
                    Text("CONTINUE READING")
                        .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
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

                TonightBedtimeCard(onOpen: onOpenBedtime)

                VStack(alignment: .leading, spacing: PardisSpacing.sm) {
                    PardisSectionHeader(title: "New this week", subtitle: "تازه‌ها", actionLabel: "See all", action: onOpenLibrary)
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: PardisSpacing.sm) {
                            ForEach(model.stories, id: \.slug) { s in
                                TodayShelfCover(titleEn: s.titleEn, titleFa: s.titleFa, cover: coverURL(s)) { onOpenStory(s.slug) }
                            }
                        }
                    }
                }

                WordOfDayCard()

                VStack(alignment: .leading, spacing: PardisSpacing.sm) {
                    PardisSectionHeader(title: "Explore collections", subtitle: "مجموعه‌ها")
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: PardisSpacing.sm) {
                            CollectionCard(name: "Shahnameh Heroes", fa: "پهلوانان", variant: 0, onTap: onOpenLibrary)
                            CollectionCard(name: "Creatures of Myth", fa: "هیولاها", variant: 5, onTap: onOpenLibrary)
                            CollectionCard(name: "Voyages", fa: "سفرها", variant: 4, onTap: onOpenLibrary)
                        }
                    }
                }

                Text("پایانِ امروز · فردا قصه‌ای تازه")
                    .font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.inkFaint)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, PardisSpacing.md)
            }
            .padding(.horizontal, PardisSpacing.lg)
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

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                Text("\(greeting), \(activeName)".uppercased())
                    .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                    .foregroundStyle(PardisColors.saffronDeep)
                Text("Salâm")
                    .font(PardisFonts.display(size: PardisTypography.xxl, weight: .heavy))
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
                Text(label.uppercased()).font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold)).foregroundStyle(fg)
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
                    Text("TONIGHT'S BEDTIME").font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold)).foregroundStyle(PardisColors.inkOnDarkMuted)
                    Text("Laay Laay, Little Star").font(PardisFonts.display(size: PardisTypography.base, weight: .bold)).foregroundStyle(PardisColors.inkOnDark)
                    Text("12 min · sleep timer ready").font(PardisFonts.body(size: PardisTypography.sm, weight: .regular)).foregroundStyle(PardisColors.inkOnDarkMuted)
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
                Text(titleFa).font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular)).foregroundStyle(PardisColors.indigo).lineLimit(1)
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
                Text("WORD OF THE DAY").font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold)).foregroundStyle(PardisColors.lilacDeep)
                Spacer()
                PardisIcon(kind: .volume, size: 18, color: PardisColors.lilacDeep)
            }
            Text("دلیر").font(PardisFonts.persian(size: PardisTypography.xxl, weight: .heavy)).foregroundStyle(PardisColors.lilacDeep)
            Text("delir — \"brave\"").font(PardisFonts.body(size: PardisTypography.base, weight: .regular)).foregroundStyle(PardisColors.lilacDeep)
            Text("A delir heart fears nothing.").font(PardisFonts.body(size: PardisTypography.base, weight: .regular)).italic().foregroundStyle(PardisColors.inkSoft)
        }
        .padding(.horizontal, 20).padding(.vertical, 22)
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
                    Text(fa).font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular)).foregroundStyle(PardisColors.inkOnDarkSoft).lineLimit(1)
                }
                .padding(13)
            }
            .frame(width: todayCollectionWidth, height: todayCollectionHeight)
            .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}
