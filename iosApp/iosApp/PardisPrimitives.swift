import SwiftUI
import Shared

struct PersianReaderText: View {
    let text: String
    let font: Font
    let color: Color

    var body: some View {
        Text(text)
            .font(font)
            .foregroundStyle(color)
            .frame(maxWidth: .infinity, alignment: .trailing)
            .multilineTextAlignment(.trailing)
            .environment(\.layoutDirection, .rightToLeft)
    }
}

struct PardisScreenHeader<Trailing: View>: View {
    let title: String
    let subtitle: String
    @ViewBuilder var trailing: Trailing

    init(title: String, subtitle: String, @ViewBuilder trailing: () -> Trailing = { EmptyView() }) {
        self.title = title
        self.subtitle = subtitle
        self.trailing = trailing()
    }

    var body: some View {
        HStack(alignment: .top, spacing: PardisSpacing.md) {
            VStack(alignment: .leading, spacing: PardisSpacing.xs) {
                Text(title)
                    .font(.system(size: PardisTypography.xxl, weight: .heavy, design: .rounded))
                    .foregroundStyle(PardisColors.indigo)
                Text(subtitle)
                    .font(.system(size: PardisTypography.base, weight: .medium, design: .rounded))
                    .foregroundStyle(PardisColors.inkSoft)
            }
            Spacer(minLength: 0)
            trailing
        }
    }
}

struct PardisMetaPill: View {
    let text: String
    let background: Color
    let foreground: Color

    var body: some View {
        Text(text)
            .font(.system(size: PardisTypography.xs, weight: .semibold, design: .rounded))
            .foregroundStyle(foreground)
            .padding(.horizontal, PardisSpacing.sm)
            .padding(.vertical, PardisSpacing.xs)
            .background(background)
            .clipShape(Capsule(style: .continuous))
    }
}

struct PardisFilterPill: View {
    let title: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: PardisTypography.sm, weight: .semibold, design: .rounded))
                .foregroundStyle(selected ? PardisColors.inkOnDark : PardisColors.inkSoft)
                .padding(.horizontal, PardisSpacing.md)
                .padding(.vertical, PardisSpacing.sm)
                .background(selected ? PardisColors.indigo : PardisColors.surface)
                .overlay(
                    Capsule(style: .continuous)
                        .stroke(selected ? PardisColors.indigo : PardisColors.border, lineWidth: 1)
                )
                .clipShape(Capsule(style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

struct PardisPanel<Content: View>: View {
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.sm) {
            content
        }
        .padding(PardisSpacing.md)
        .pardisCardSurface(cornerRadius: PardisRadius.lg)
    }
}

struct PardisAsyncImageFrame: View {
    let url: URL?
    let accessibilityLabel: String
    let width: CGFloat?
    let height: CGFloat
    var placeholderText: String = "No illustration"

    var body: some View {
        Group {
            if let url {
                AsyncImage(url: url) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Color(PardisColors.surfaceLilac)
                }
            } else {
                ZStack {
                    Color(PardisColors.surfaceLilac)
                    Text(placeholderText)
                        .font(.system(size: PardisTypography.sm, weight: .medium, design: .rounded))
                        .foregroundStyle(PardisColors.inkSoft)
                }
            }
        }
        .frame(width: width, height: height)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.md, style: .continuous))
        .accessibilityLabel(accessibilityLabel)
    }
}

struct PardisReaderHeaderBar: View {
    let onBack: () -> Void
    let pageLabel: String
    let isOffline: Bool

    var body: some View {
        PardisPanel {
            HStack(spacing: PardisSpacing.sm) {
                Button("← Library", action: onBack)
                    .font(.system(size: PardisTypography.sm, weight: .semibold, design: .rounded))
                    .foregroundStyle(PardisColors.indigo)
                Spacer()
                PardisMetaPill(text: pageLabel, background: PardisColors.backgroundAlt, foreground: PardisColors.inkSoft)
                if isOffline {
                    PardisMetaPill(text: "Offline", background: PardisColors.mintSoft, foreground: PardisColors.mintDeep)
                }
            }
        }
    }
}

struct PardisControlGroup<Content: View>: View {
    let label: String
    let content: Content

    init(label: String, @ViewBuilder content: () -> Content) {
        self.label = label
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.xs) {
            Text(label)
                .font(.system(size: PardisTypography.xs, weight: .semibold, design: .rounded))
                .foregroundStyle(PardisColors.inkMuted)
            content
        }
    }
}

struct PardisVocabChipView: View {
    let vocab: VocabItem
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 2) {
                PersianReaderText(
                    text: vocab.fa,
                    font: .system(size: PardisTypography.sm, weight: .bold, design: .rounded),
                    color: PardisColors.indigoDeep
                )
                Text("\(vocab.translit) — \(vocab.en)")
                    .font(.system(size: PardisTypography.xs, weight: .medium, design: .rounded))
                    .foregroundStyle(PardisColors.inkSoft)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, PardisSpacing.md)
            .padding(.vertical, PardisSpacing.sm)
            .background(PardisColors.mintSoft)
            .overlay(
                Capsule(style: .continuous)
                    .stroke(PardisColors.borderSoft, lineWidth: 1)
            )
            .clipShape(Capsule(style: .continuous))
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Vocabulary: \(vocab.fa) means \(vocab.en), transliteration \(vocab.translit)")
        .accessibilityAddTraits(.isButton)
    }
}

struct PardisVocabSheetContent: View {
    let vocab: VocabItem
    let onPlayPronunciation: (() -> Void)?
    let onClose: () -> Void

    var body: some View {
        PardisPanel {
            Text("Vocab")
                .font(.system(size: PardisTypography.sm, weight: .bold, design: .rounded))
                .foregroundStyle(PardisColors.indigo)
            PersianReaderText(text: vocab.fa, font: .system(size: PardisTypography.xl, weight: .bold, design: .rounded), color: PardisColors.ink)
            Text("(\(vocab.translit))")
                .font(.system(size: PardisTypography.base, weight: .medium, design: .rounded))
                .foregroundStyle(PardisColors.inkSoft)
            Text(vocab.en)
                .font(.system(size: PardisTypography.base, weight: .regular, design: .rounded))
                .foregroundStyle(PardisColors.ink)
            if !vocab.context.isEmpty {
                Text("in: \(vocab.context)")
                    .font(.system(size: PardisTypography.sm, weight: .regular, design: .rounded))
                    .foregroundStyle(PardisColors.inkMuted)
            }
            if let onPlayPronunciation, vocab.audioUrl != nil {
                Button("▶ Play pronunciation", action: onPlayPronunciation)
                    .foregroundStyle(PardisColors.indigo)
            }
            Button("Close", action: onClose)
                .foregroundStyle(PardisColors.saffron)
        }
    }
}

