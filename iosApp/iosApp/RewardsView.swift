import SwiftUI
import Shared

// Layout-specific dimensions mirroring Android PardisRewardsScreen.
private let rewardLevelRingSize: CGFloat = 88
private let rewardBadgeRingSize: CGFloat = 60
private let rewardWordRingSize: CGFloat = 46
private let rewardStreakDaySize: CGFloat = 32
private let rewardHeroTileSize: CGFloat = 84
private let rewardWordHeaderIcon: CGFloat = 38

/// Maps the shared semantic badge icon to the iOS icon set. Equality (not `switch`) so it
/// compiles under both SKIE's generated enum and plain Kotlin/Native export. Mirrors Android
/// `RivanaBadgeIcon.toPardisIconKind`.
private func badgeIconKind(_ icon: RivanaBadgeIcon) -> PardisIconKind {
    if icon == RivanaBadgeIcon.flame { return .flame }
    if icon == RivanaBadgeIcon.feather { return .feather }
    if icon == RivanaBadgeIcon.crown { return .crown }
    if icon == RivanaBadgeIcon.moon { return .moon }
    if icon == RivanaBadgeIcon.mic { return .mic }
    return .compass
}

/// The accent (deep) tone color, mirroring Android `toneBase`.
private func toneBase(_ tone: String) -> Color {
    switch tone {
    case "mint": return PardisColors.mint
    case "saffron": return PardisColors.saffron
    case "lapis": return PardisColors.indigo
    default: return PardisColors.lilac
    }
}

/// "Rewards" tab, mirroring Android `RewardsScreen`. `onOpenCharacter` receives the index into
/// `RivanaContent.characters`.
struct RewardsView: View {
    let storyCount: Int
    let onOpenCharacter: (Int) -> Void

    private var words: [RivanaWord] { RivanaContent.shared.words }
    private var badges: [RivanaBadge] { RivanaContent.shared.badges }
    private var characters: [RivanaCharacter] { RivanaContent.shared.characters }
    private var mastered: Int { words.filter { $0.mastery >= 1 }.count }
    private var growing: Int { words.count - mastered }
    private var nextBadge: RivanaBadge? {
        badges.filter { !$0.earned }.max(by: { $0.progress < $1.progress })
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PardisSpacing.md) {
                HStack(alignment: .bottom) {
                    VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                        Text("Rewards")
                            .font(PardisFonts.display(size: PardisTypography.xxl, weight: .heavy))
                            .foregroundStyle(PardisColors.ink)
                        Text("جایزه‌ها و دستاوردها")
                            .font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular))
                            .foregroundStyle(PardisColors.inkMuted)
                    }
                    Spacer()
                    HStack(spacing: PardisSpacing.sm - PardisSpacing.xxs) {
                        PardisIcon(kind: .star, size: 16, color: PardisColors.saffronDeep)
                        Text("320")
                            .font(PardisFonts.display(size: PardisTypography.base, weight: .heavy))
                            .foregroundStyle(PardisColors.saffronDeep)
                    }
                    .padding(.horizontal, 13)
                    .padding(.vertical, 7)
                    .background(PardisColors.saffronSoft)
                    .clipShape(Capsule(style: .continuous))
                }

                RewardLevelHero()
                RewardStatsStrip(words: mastered, stories: storyCount)
                StreakCalendar()

                VStack(alignment: .leading, spacing: PardisSpacing.sm) {
                    Text("ALMOST THERE")
                        .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                        .foregroundStyle(PardisColors.inkMuted)
                    if let b = nextBadge { NextBadgeCard(badge: b) }
                }

                WordGarden(mastered: mastered, growing: growing, total: words.count, words: words)

                VStack(alignment: .leading, spacing: PardisSpacing.sm) {
                    PardisSectionHeader(title: "Heroes met", subtitle: "پهلوانانِ آشنا")
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: PardisSpacing.md) {
                            ForEach(Array(characters.enumerated()), id: \.element.name) { i, c in
                                HeroTile(character: c) { onOpenCharacter(i) }
                            }
                        }
                    }
                }

                VStack(alignment: .leading, spacing: PardisSpacing.sm) {
                    PardisSectionHeader(title: "Badges", subtitle: "نشان‌ها")
                    BadgesGrid(badges: badges)
                }
            }
            .padding(.horizontal, PardisSpacing.lg)
            .padding(.top, PardisSpacing.xl)
        }
        .background(PardisColors.background.ignoresSafeArea())
    }
}

private struct RewardLevelHero: View {
    var body: some View {
        HStack(spacing: 18) {
            PardisRing(progress: 320.0 / 500.0, ringColor: PardisColors.sun, trackColor: PardisColors.surfaceOnDark, strokeWidth: 6) {
                VStack(spacing: 0) {
                    Text("LVL")
                        .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                        .foregroundStyle(PardisColors.inkOnDarkMuted)
                    Text("3")
                        .font(PardisFonts.display(size: PardisTypography.xxl, weight: .heavy))
                        .foregroundStyle(PardisColors.inkOnDark)
                }
            }
            .frame(width: rewardLevelRingSize, height: rewardLevelRingSize)

            VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                Text("YOUR RANK")
                    .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                    .foregroundStyle(PardisColors.inkOnDarkMuted)
                Text("Story Keeper")
                    .font(PardisFonts.display(size: PardisTypography.xl, weight: .heavy))
                    .foregroundStyle(PardisColors.inkOnDark)
                Spacer().frame(height: PardisSpacing.sm)
                HStack {
                    Text("320 / 500 XP")
                    Spacer()
                    Text("180 to Lvl 4")
                }
                .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                .foregroundStyle(PardisColors.inkOnDarkSoft)
                Spacer().frame(height: PardisSpacing.xs + PardisSpacing.hairline)
                PardisProgressBar(value: 320.0 / 500.0, height: 6, color: PardisColors.sun)
            }
        }
        .padding(20)
        .background(PardisGradients.lapis)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.xl, style: .continuous))
    }
}

private struct RewardStatsStrip: View {
    let words: Int
    let stories: Int

    var body: some View {
        HStack(spacing: PardisSpacing.sm) {
            RewardStat(icon: .flame, value: "7", label: "streak", deep: PardisColors.saffronDeep)
            RewardStat(icon: .feather, value: "\(words)", label: "words", deep: PardisColors.mintDeep)
            RewardStat(icon: .crown, value: "4", label: "heroes", deep: PardisColors.lilacDeep)
            RewardStat(icon: .book, value: "\(stories)", label: "stories", deep: PardisColors.indigoDeep)
        }
    }
}

private struct RewardStat: View {
    let icon: PardisIconKind
    let value: String
    let label: String
    let deep: Color

    var body: some View {
        VStack(spacing: PardisSpacing.xxs) {
            PardisIcon(kind: icon, size: 18, color: deep)
            Text(value)
                .font(PardisFonts.display(size: PardisTypography.lg, weight: .heavy))
                .foregroundStyle(PardisColors.ink)
            Text(label.uppercased())
                .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                .foregroundStyle(PardisColors.inkMuted)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 13)
        .background(PardisColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous)
                .stroke(PardisColors.border, lineWidth: PardisSpacing.hairline)
        )
    }
}

private struct StreakCalendar: View {
    private let days = ["M", "T", "W", "T", "F", "S", "S"]

    var body: some View {
        VStack(spacing: 14) {
            HStack {
                HStack(spacing: 9) {
                    PardisIcon(kind: .flame, size: 18, color: PardisColors.inkOnDark)
                    Text("7-night streak")
                        .font(PardisFonts.display(size: PardisTypography.base, weight: .heavy))
                        .foregroundStyle(PardisColors.inkOnDark)
                }
                Spacer()
                Text("Keep it lit!")
                    .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                    .foregroundStyle(PardisColors.inkOnDarkSoft)
            }
            HStack {
                ForEach(Array(days.enumerated()), id: \.offset) { _, d in
                    VStack(spacing: 5) {
                        ZStack {
                            Circle().fill(PardisColors.inkOnDark)
                            PardisIcon(kind: .flame, size: 16, color: PardisColors.saffronDeep)
                        }
                        .frame(width: rewardStreakDaySize, height: rewardStreakDaySize)
                        Text(d)
                            .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                            .foregroundStyle(PardisColors.inkOnDarkMuted)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
        .padding(.horizontal, 18)
        .padding(.vertical, PardisSpacing.md)
        .background(PardisGradients.saffron)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
    }
}

private struct NextBadgeCard: View {
    let badge: RivanaBadge

    var body: some View {
        HStack(spacing: 15) {
            PardisRing(progress: Double(badge.progress), ringColor: toneBase(badge.tone), trackColor: PardisColors.backgroundAlt, strokeWidth: 5) {
                PardisIcon(kind: badgeIconKind(badge.icon), size: 18, color: pardisToneColors(badge.tone).deep)
            }
            .frame(width: rewardBadgeRingSize, height: rewardBadgeRingSize)

            VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                Text(badge.label)
                    .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                    .foregroundStyle(PardisColors.ink)
                Text(badge.desc)
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.inkMuted)
                Text("\(Int(badge.progress * 100))% complete")
                    .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                    .foregroundStyle(pardisToneColors(badge.tone).deep)
            }
            Spacer(minLength: 0)
        }
        .padding(16)
        .background(PardisColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous)
                .stroke(PardisColors.border, lineWidth: PardisSpacing.hairline)
        )
    }
}

private struct WordGarden: View {
    let mastered: Int
    let growing: Int
    let total: Int
    let words: [RivanaWord]

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 11) {
                ZStack {
                    RoundedRectangle(cornerRadius: PardisRadius.sm, style: .continuous).fill(PardisColors.mint)
                    PardisIcon(kind: .sprout, size: 18, color: PardisColors.inkOnDark)
                }
                .frame(width: rewardWordHeaderIcon, height: rewardWordHeaderIcon)
                VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                    Text("\(total) words growing")
                        .font(PardisFonts.display(size: PardisTypography.base, weight: .heavy))
                        .foregroundStyle(PardisColors.mintDeep)
                    Text("\(mastered) mastered · \(growing) sprouting")
                        .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                        .foregroundStyle(PardisColors.mintDeep)
                }
                Spacer(minLength: 0)
            }
            .padding(16)
            .frame(maxWidth: .infinity)
            .background(PardisColors.mintSoft)

            ForEach(Array(stride(from: 0, to: words.count, by: 3)), id: \.self) { start in
                HStack(spacing: 0) {
                    ForEach(start..<min(start + 3, words.count), id: \.self) { idx in
                        WordCell(word: words[idx]).frame(maxWidth: .infinity)
                    }
                    // pad incomplete final row to keep cells left-aligned
                    if min(start + 3, words.count) - start < 3 {
                        ForEach(0..<(3 - (min(start + 3, words.count) - start)), id: \.self) { _ in
                            Color.clear.frame(maxWidth: .infinity)
                        }
                    }
                }
            }
        }
        .background(PardisColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous)
                .stroke(PardisColors.border, lineWidth: PardisSpacing.hairline)
        )
    }
}

private struct WordCell: View {
    let word: RivanaWord

    var body: some View {
        VStack(spacing: 0) {
            PardisRing(
                progress: Double(word.mastery),
                ringColor: word.mastery >= 1 ? PardisColors.mint : PardisColors.saffron,
                trackColor: PardisColors.backgroundAlt,
                strokeWidth: 4
            ) {
                Text(word.fa)
                    .font(PardisFonts.persian(size: PardisTypography.base, weight: .bold))
                    .foregroundStyle(PardisColors.ink)
            }
            .frame(width: rewardWordRingSize, height: rewardWordRingSize)
            Text(word.tr)
                .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                .foregroundStyle(PardisColors.inkMuted)
                .padding(.top, PardisSpacing.sm - PardisSpacing.xxs)
            Text(word.en)
                .font(PardisFonts.body(size: PardisTypography.sm, weight: .semibold))
                .foregroundStyle(PardisColors.inkSoft)
        }
        .padding(.vertical, 14)
    }
}

private struct HeroTile: View {
    let character: RivanaCharacter
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 7) {
                ZStack {
                    PardisSceneArt(seed: character.name, forcedVariant: Int(character.sceneVariant))
                    if !character.collected {
                        PardisColors.scrimSoft
                        PardisIcon(kind: .lock, size: 20, color: PardisColors.inkOnDark)
                    }
                }
                .frame(width: rewardHeroTileSize, height: rewardHeroTileSize)
                .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))

                Text(character.name)
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .bold))
                    .foregroundStyle(character.collected ? PardisColors.ink : PardisColors.inkMuted)
                    .lineLimit(1)
                Text(character.nameFa)
                    .font(PardisFonts.persian(size: PardisTypography.xs, weight: .regular))
                    .foregroundStyle(PardisColors.inkMuted)
                    .lineLimit(1)
            }
            .frame(width: rewardHeroTileSize)
        }
        .buttonStyle(.plain)
    }
}

private struct BadgesGrid: View {
    let badges: [RivanaBadge]

    var body: some View {
        VStack(spacing: PardisSpacing.sm) {
            ForEach(Array(stride(from: 0, to: badges.count, by: 3)), id: \.self) { start in
                HStack(spacing: PardisSpacing.sm) {
                    ForEach(start..<min(start + 3, badges.count), id: \.self) { idx in
                        BadgeCell(badge: badges[idx]).frame(maxWidth: .infinity)
                    }
                    if min(start + 3, badges.count) - start < 3 {
                        ForEach(0..<(3 - (min(start + 3, badges.count) - start)), id: \.self) { _ in
                            Color.clear.frame(maxWidth: .infinity)
                        }
                    }
                }
            }
        }
    }
}

private struct BadgeCell: View {
    let badge: RivanaBadge

    var body: some View {
        let tone = pardisToneColors(badge.tone)
        VStack(spacing: 0) {
            ZStack {
                RoundedRectangle(cornerRadius: PardisRadius.md, style: .continuous)
                    .fill(badge.earned ? tone.soft : PardisColors.backgroundAlt)
                if badge.earned {
                    PardisIcon(kind: badgeIconKind(badge.icon), size: 32, color: tone.deep)
                } else {
                    PardisIcon(kind: .lock, size: 24, color: PardisColors.inkFaint)
                    if badge.progress > 0 {
                        VStack {
                            Spacer()
                            PardisProgressBar(value: Double(badge.progress), height: 4, color: toneBase(badge.tone))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 11)
                        }
                    }
                }
            }
            .aspectRatio(1, contentMode: .fit)

            Text(badge.label)
                .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                .foregroundStyle(badge.earned ? PardisColors.ink : PardisColors.inkMuted)
                .lineLimit(1)
                .padding(.top, PardisSpacing.sm)
        }
    }
}
