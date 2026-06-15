import SwiftUI
import Shared

private let characterHeroHeight: CGFloat = 400

private func toneAccent(_ tone: String) -> Color {
    switch tone {
    case "saffron": return PardisColors.saffronDeep
    case "lilac": return PardisColors.lilacDeep
    case "lapis": return PardisColors.indigo
    case "rose": return PardisColors.roseDeep
    case "sun": return PardisColors.sunDeep
    case "mint": return PardisColors.mintDeep
    default: return PardisColors.indigo
    }
}

/// Character detail, mirroring Android `CharacterScreen`. Pushed from Rewards' "Heroes met".
struct CharacterView: View {
    let character: RivanaCharacter
    let onBack: () -> Void

    private var accent: Color { toneAccent(character.tone) }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                CharacterHero(character: character)

                Text(character.bio)
                    .font(PardisFonts.body(size: PardisTypography.base, weight: .regular))
                    .foregroundStyle(PardisColors.ink)
                    .padding(.horizontal, PardisSpacing.lg)
                    .padding(.vertical, PardisSpacing.sm)

                CharacterStats(character: character, accent: accent)
                    .padding(.horizontal, PardisSpacing.lg)
                    .padding(.vertical, PardisSpacing.sm)

                Spacer().frame(height: PardisSpacing.xl)
            }
        }
        .ignoresSafeArea(edges: .top)
        .background(PardisColors.background.ignoresSafeArea())
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbarBackground(.hidden, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button(action: onBack) { Image(systemName: "chevron.left") }
                    .accessibilityLabel("Back")
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: {}) { Image(systemName: "heart") }
                    .accessibilityLabel("Save")
            }
        }
    }
}

private struct CharacterHero: View {
    let character: RivanaCharacter

    var body: some View {
        PardisSceneArt(seed: character.name, forcedVariant: Int(character.sceneVariant))
            .frame(height: characterHeroHeight)
            .overlay {
                LinearGradient(
                    stops: [
                        .init(color: PardisColors.scrimSoft, location: 0.0),
                        .init(color: .clear, location: 0.30),
                        .init(color: .clear, location: 0.55),
                        .init(color: PardisColors.background, location: 1.0),
                    ],
                    startPoint: .top, endPoint: .bottom
                )
            }
            .overlay(alignment: .bottomLeading) {
                VStack(alignment: .leading, spacing: 0) {
                    CollectedTag(collected: character.collected)
                    Spacer().frame(height: PardisSpacing.sm + PardisSpacing.xxs)
                    Text(character.name)
                        .font(PardisFonts.display(size: PardisTypography.xxl, weight: .heavy))
                        .foregroundStyle(PardisColors.ink)
                    Text(character.nameFa)
                        .font(PardisFonts.persian(size: PardisTypography.xl, weight: .regular))
                        .foregroundStyle(PardisColors.inkSoft)
                    Spacer().frame(height: PardisSpacing.xs + PardisSpacing.xxs)
                    Text(character.role.uppercased())
                        .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                        .foregroundStyle(PardisColors.inkMuted)
                }
                .padding(.horizontal, PardisSpacing.lg)
                .padding(.vertical, PardisSpacing.md)
            }
            .ignoresSafeArea(edges: .top)
    }
}

private struct CollectedTag: View {
    let collected: Bool

    var body: some View {
        HStack(spacing: 5) {
            PardisIcon(kind: collected ? .check : .lock, size: 12, color: collected ? PardisColors.mintDeep : PardisColors.inkOnDark)
            Text(collected ? "Collected" : "Not yet met")
                .font(PardisFonts.body(size: PardisTypography.xs, weight: .bold))
                .foregroundStyle(collected ? PardisColors.mintDeep : PardisColors.inkOnDark)
        }
        .padding(.horizontal, 11)
        .padding(.vertical, PardisSpacing.sm - PardisSpacing.xxs)
        .background(collected ? PardisColors.mintSoft : PardisColors.scrim)
        .clipShape(Capsule(style: .continuous))
    }
}

private struct CharacterStats: View {
    let character: RivanaCharacter
    let accent: Color

    private var roleWord: String {
        character.role.split(separator: " ").first.map(String.init) ?? character.role
    }

    var body: some View {
        HStack(spacing: PardisSpacing.sm) {
            CharacterStat(icon: .book, value: "\(character.appearances)", label: "stories", accent: accent)
            CharacterStat(icon: .feather, value: roleWord, label: "role", accent: accent)
            CharacterStat(
                icon: character.collected ? .starFill : .star,
                value: character.collected ? "★" : "–",
                label: "badge",
                accent: accent
            )
        }
    }
}

private struct CharacterStat: View {
    let icon: PardisIconKind
    let value: String
    let label: String
    let accent: Color

    var body: some View {
        VStack(spacing: PardisSpacing.xs) {
            PardisIcon(kind: icon, size: 20, color: accent)
            Text(value)
                .font(PardisFonts.display(size: PardisTypography.base, weight: .heavy))
                .foregroundStyle(PardisColors.ink)
                .lineLimit(1)
            Text(label.uppercased())
                .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                .foregroundStyle(PardisColors.inkMuted)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .padding(.horizontal, PardisSpacing.sm)
        .background(PardisColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous)
                .stroke(PardisColors.border, lineWidth: PardisSpacing.hairline)
        )
    }
}
