import SwiftUI
import Shared

// Layout-specific dimensions, mirroring Android PardisYouScreen. Named constants per the
// existing `pardisStoryCoverSize` precedent; colors/spacing/radius come from design tokens.
private let youAvatarSize: CGFloat = 72
private let youSettingsIconBox: CGFloat = 34
private let youToggleWidth: CGFloat = 46
private let youToggleHeight: CGFloat = 28
private let youToggleKnob: CGFloat = 22
private let youDividerInset: CGFloat = 63

private struct SettingsItem: Identifiable {
    let icon: PardisIconKind
    let tone: String
    let label: String
    var detail: String? = nil
    var id: String { label }
}

/// Maps a string tone to its (soft, deep) color pair using design tokens. Mirrors Android
/// `toneColors` exactly — including "rose"/unknown falling through to lilac.
func pardisToneColors(_ tone: String) -> (soft: Color, deep: Color) {
    switch tone {
    case "mint": return (PardisColors.mintSoft, PardisColors.mintDeep)
    case "saffron": return (PardisColors.saffronSoft, PardisColors.saffronDeep)
    case "lapis": return (PardisColors.indigoSoft, PardisColors.indigoDeep)
    default: return (PardisColors.lilacSoft, PardisColors.lilacDeep)
    }
}

/// "You" tab — profile card + settings groups, mirroring Android `YouScreen`. The decorative
/// Android paisley/rosette pattern overlays are omitted (no iOS primitive); the dawn gradient
/// and screen background provide the equivalent richness. Dark-mode toggle is decorative
/// (not wired), matching Android.
struct YouView: View {
    let activeProfile: ChildProfile
    let downloadCount: Int
    let onSwitchProfile: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PardisSpacing.md) {
                Text("You")
                    .font(PardisFonts.display(size: PardisTypography.xxl, weight: .heavy))
                    .foregroundStyle(PardisColors.ink)

                YouProfileCard(activeProfile: activeProfile, onSwitchProfile: onSwitchProfile)

                AppearanceGroup()

                SettingsGroup(label: "Reading", items: [
                    SettingsItem(icon: .languages, tone: "lapis", label: "Story language", detail: "English & فارسی"),
                    SettingsItem(icon: .volume, tone: "saffron", label: "Narration speed", detail: "Normal"),
                    SettingsItem(icon: .download, tone: "mint", label: "Downloads", detail: "\(downloadCount) stories"),
                ])

                SettingsGroup(label: "Family", items: [
                    SettingsItem(icon: .shield, tone: "lapis", label: "Parents' corner", detail: "Locked"),
                    SettingsItem(icon: .bell, tone: "saffron", label: "Bedtime reminder", detail: "8:00 PM"),
                    SettingsItem(icon: .star, tone: "lilac", label: "Rivana Plus", detail: "Active"),
                ])

                SettingsGroup(label: "About", items: [
                    SettingsItem(icon: .settings, tone: "lapis", label: "Settings"),
                    SettingsItem(icon: .heart, tone: "rose", label: "Rate Rivana"),
                ])

                VStack(spacing: PardisSpacing.xs) {
                    Text("ریوانا · قصه‌های پارسی برای کودکان")
                        .font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular))
                        .foregroundStyle(PardisColors.inkFaint)
                    Text("PARDIS · v1.0")
                        .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                        .foregroundStyle(PardisColors.inkFaint)
                }
                .frame(maxWidth: .infinity)
                .padding(.top, PardisSpacing.md)
            }
            .padding(.horizontal, PardisSpacing.lg)
            .padding(.top, PardisSpacing.xl)
        }
    }
}

private struct YouProfileCard: View {
    let activeProfile: ChildProfile
    let onSwitchProfile: () -> Void

    var body: some View {
        HStack(spacing: PardisSpacing.md) {
            ZStack {
                Circle().fill(profileToneGradient(activeProfile.tone))
                Text(String(activeProfile.name.prefix(1)))
                    .font(PardisFonts.display(size: PardisTypography.xxxl, weight: .bold))
                    .foregroundStyle(PardisColors.inkOnDark)
            }
            .frame(width: youAvatarSize, height: youAvatarSize)

            VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                Text(activeProfile.name)
                    .font(PardisFonts.display(size: PardisTypography.xl, weight: .heavy))
                    .foregroundStyle(PardisColors.ink)
                Text("Age \(activeProfile.age) · \(activeProfile.streak)-night streak")
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.inkSoft)

                Button(action: onSwitchProfile) {
                    HStack(spacing: PardisSpacing.sm - PardisSpacing.xxs) {
                        PardisIcon(kind: .user, size: 15, color: PardisColors.ink)
                        Text("Switch reader")
                            .font(PardisFonts.body(size: PardisTypography.sm, weight: .bold))
                            .foregroundStyle(PardisColors.ink)
                    }
                    .padding(.horizontal, PardisSpacing.md - PardisSpacing.xxs)
                    .padding(.vertical, PardisSpacing.sm)
                    .background(PardisColors.surface)
                    .clipShape(Capsule(style: .continuous))
                }
                .buttonStyle(.plain)
                .padding(.top, PardisSpacing.sm)
            }
            Spacer(minLength: 0)
        }
        .padding(PardisSpacing.lg - PardisSpacing.xs)
        .background(PardisGradients.dawn)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.xl, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: PardisRadius.xl, style: .continuous)
                .stroke(PardisColors.border, lineWidth: PardisSpacing.hairline)
        )
    }
}

private struct AppearanceGroup: View {
    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.sm) {
            Text("APPEARANCE")
                .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                .foregroundStyle(PardisColors.inkMuted)

            HStack(spacing: PardisSpacing.sm + PardisSpacing.xs + PardisSpacing.hairline) {
                ZStack {
                    RoundedRectangle(cornerRadius: PardisRadius.sm, style: .continuous)
                        .fill(PardisColors.lilacSoft)
                    PardisIcon(kind: .moon, size: 18, color: PardisColors.lilacDeep)
                }
                .frame(width: youSettingsIconBox, height: youSettingsIconBox)

                Text("Dark mode")
                    .font(PardisFonts.body(size: PardisTypography.lg, weight: .semibold))
                    .foregroundStyle(PardisColors.ink)
                    .frame(maxWidth: .infinity, alignment: .leading)

                // Decorative off toggle (dark mode not wired yet) — matches Android.
                ZStack(alignment: .leading) {
                    Capsule(style: .continuous).fill(PardisColors.border)
                    Circle().fill(PardisColors.inkOnDark)
                        .frame(width: youToggleKnob, height: youToggleKnob)
                        .padding(.leading, 3)
                }
                .frame(width: youToggleWidth, height: youToggleHeight)
            }
            .padding(.horizontal, PardisSpacing.md)
            .padding(.vertical, PardisSpacing.sm + PardisSpacing.xs + PardisSpacing.hairline)
            .background(PardisColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous)
                    .stroke(PardisColors.border, lineWidth: PardisSpacing.hairline)
            )
        }
    }
}

private struct SettingsGroup: View {
    let label: String
    let items: [SettingsItem]

    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.sm) {
            Text(label.uppercased())
                .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                .foregroundStyle(PardisColors.inkMuted)

            VStack(spacing: 0) {
                ForEach(Array(items.enumerated()), id: \.element.id) { index, item in
                    if index > 0 {
                        Rectangle()
                            .fill(PardisColors.border)
                            .frame(height: PardisSpacing.hairline)
                            .padding(.leading, youDividerInset)
                    }
                    SettingsRow(item: item)
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
}

private struct SettingsRow: View {
    let item: SettingsItem

    var body: some View {
        let tone = pardisToneColors(item.tone)
        HStack(spacing: PardisSpacing.sm + PardisSpacing.xs + PardisSpacing.hairline) {
            ZStack {
                RoundedRectangle(cornerRadius: PardisRadius.sm, style: .continuous).fill(tone.soft)
                PardisIcon(kind: item.icon, size: 18, color: tone.deep)
            }
            .frame(width: youSettingsIconBox, height: youSettingsIconBox)

            Text(item.label)
                .font(PardisFonts.body(size: PardisTypography.lg, weight: .semibold))
                .foregroundStyle(PardisColors.ink)
                .frame(maxWidth: .infinity, alignment: .leading)

            if let detail = item.detail {
                Text(detail)
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.inkMuted)
            }

            PardisIcon(kind: .chevRight, size: 17, color: PardisColors.inkFaint)
        }
        .padding(.horizontal, PardisSpacing.md)
        .padding(.vertical, PardisSpacing.sm + PardisSpacing.xs + PardisSpacing.hairline)
    }
}
