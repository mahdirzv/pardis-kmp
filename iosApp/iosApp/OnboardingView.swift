import SwiftUI
import Shared

// Layout-specific dimensions for the onboarding picker, mirroring the Android
// PardisOnboardingScreen values. Named constants (per the existing `pardisStoryCoverSize`
// precedent) instead of inline magic numbers; colors/spacing/radius come from design tokens.
private let onboardingAvatarSize: CGFloat = 80
private let onboardingAddChildIconSize: CGFloat = 56
private let onboardingAddChildMinHeight: CGFloat = 168
private let onboardingBackButtonSize: CGFloat = 40
private let onboardingGridSpacing: CGFloat = 16
private let onboardingWordmarkSpacing: CGFloat = 9

/// Maps a profile tone to its avatar gradient using design tokens (no raw colors).
/// Mirrors Android `toneGradient`.
func profileToneGradient(_ tone: ProfileTone) -> LinearGradient {
    // Equality against the bridged enum entries compiles under both SKIE's generated Swift enum
    // and plain Kotlin/Native ObjC export (static accessors), unlike a `switch` with case labels.
    let colors: [Color]
    if tone == ProfileTone.lapis {
        colors = [PardisColors.indigo, PardisColors.indigoDeep]
    } else if tone == ProfileTone.lilac {
        colors = [PardisColors.lilac, PardisColors.lilacDeep]
    } else { // .saffron (and safe fallback)
        colors = [PardisColors.saffron, PardisColors.saffronDeep]
    }
    return LinearGradient(colors: colors, startPoint: .topLeading, endPoint: .bottomTrailing)
}

/// "Who's reading tonight?" profile picker. First-launch gate, and — with `isSwitch == true` —
/// the switch-profile screen reached from the You tab (shows a back chevron).
/// Mirrors Android `PardisOnboardingScreen`.
struct OnboardingView: View {
    let profiles: [ChildProfile]
    var isSwitch: Bool = false
    let onSelect: (ChildProfile) -> Void
    var onBack: () -> Void = {}
    var onComingSoon: () -> Void = {}

    private let columns = [
        GridItem(.flexible(), spacing: onboardingGridSpacing),
        GridItem(.flexible(), spacing: onboardingGridSpacing),
    ]

    var body: some View {
        VStack(spacing: 0) {
            if isSwitch {
                HStack {
                    Button(action: onBack) {
                        PardisIcon(kind: .back, size: 20, color: PardisColors.ink)
                            .frame(width: onboardingBackButtonSize, height: onboardingBackButtonSize)
                            .background(PardisColors.surface)
                            .clipShape(Circle())
                    }
                    .accessibilityLabel("Back")
                    Spacer()
                }
                .padding(.leading, PardisSpacing.lg)
                .padding(.top, PardisSpacing.sm)
            }

            VStack(spacing: PardisSpacing.xs) {
                HStack(spacing: onboardingWordmarkSpacing) {
                    Text("Rivana")
                        .font(PardisFonts.display(size: PardisTypography.xl, weight: .heavy))
                        .foregroundStyle(PardisColors.ink)
                    Text("ریوانا")
                        .font(PardisFonts.persian(size: PardisTypography.lg, weight: .medium))
                        .foregroundStyle(PardisColors.inkSoft)
                }

                Spacer().frame(height: PardisSpacing.lg)

                Text("Who's reading tonight?")
                    .font(PardisFonts.display(size: PardisTypography.xxl, weight: .heavy))
                    .foregroundStyle(PardisColors.ink)
                    .multilineTextAlignment(.center)
                Text("امشب کی قصه می‌خواند؟")
                    .font(PardisFonts.persian(size: PardisTypography.lg, weight: .regular))
                    .foregroundStyle(PardisColors.inkSoft)
            }
            .frame(maxWidth: .infinity)
            .padding(.horizontal, PardisSpacing.lg)
            .padding(.top, isSwitch ? PardisSpacing.xs : PardisSpacing.xl)
            .accessibilityAddTraits(.isHeader)

            Spacer().frame(height: PardisSpacing.lg)

            ScrollView {
                LazyVGrid(columns: columns, spacing: onboardingGridSpacing) {
                    ForEach(profiles, id: \.id) { profile in
                        ProfilePickCard(profile: profile) { onSelect(profile) }
                    }
                    AddChildCard(onTap: onComingSoon)
                }
                .padding(.horizontal, PardisSpacing.lg)
            }

            Button(action: onComingSoon) {
                HStack(spacing: 7) {
                    PardisIcon(kind: .shield, size: 17, color: PardisColors.indigo)
                    Text("I'm a parent")
                        .font(PardisFonts.body(size: PardisTypography.sm, weight: .bold))
                        .foregroundStyle(PardisColors.inkSoft)
                }
                .padding(PardisSpacing.sm)
            }
            .accessibilityLabel("Open parent area")
            .padding(.vertical, PardisSpacing.lg)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(PardisColors.background.ignoresSafeArea())
    }
}

private struct ProfilePickCard: View {
    let profile: ChildProfile
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: PardisSpacing.sm + PardisSpacing.xs) {
                ZStack {
                    Circle().fill(profileToneGradient(profile.tone))
                    Text(String(profile.name.prefix(1)))
                        .font(PardisFonts.display(size: PardisTypography.xxxl, weight: .bold))
                        .foregroundStyle(PardisColors.inkOnDark)
                }
                .frame(width: onboardingAvatarSize, height: onboardingAvatarSize)

                VStack(spacing: PardisSpacing.xxs) {
                    Text(profile.name)
                        .font(PardisFonts.display(size: PardisTypography.lg, weight: .heavy))
                        .foregroundStyle(PardisColors.ink)
                    Text("AGE \(profile.age)")
                        .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                        .foregroundStyle(PardisColors.inkSoft)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, PardisSpacing.lg)
            .padding(.horizontal, PardisSpacing.md)
            .background(PardisColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: PardisRadius.xl, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: PardisRadius.xl, style: .continuous)
                    .stroke(PardisColors.border, lineWidth: PardisSpacing.hairline)
            )
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Read as \(profile.name)")
    }
}

private struct AddChildCard: View {
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: PardisSpacing.sm + PardisSpacing.xxs) {
                ZStack {
                    Circle().fill(PardisColors.backgroundAlt)
                    Text("+")
                        .font(PardisFonts.display(size: PardisTypography.xl, weight: .bold))
                        .foregroundStyle(PardisColors.inkSoft)
                }
                .frame(width: onboardingAddChildIconSize, height: onboardingAddChildIconSize)

                Text("Add child")
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .bold))
                    .foregroundStyle(PardisColors.inkSoft)
            }
            .frame(maxWidth: .infinity, minHeight: onboardingAddChildMinHeight)
            .overlay(
                RoundedRectangle(cornerRadius: PardisRadius.xl, style: .continuous)
                    .stroke(PardisColors.border, lineWidth: 2)
            )
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Add child")
    }
}
