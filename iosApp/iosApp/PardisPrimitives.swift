import SwiftUI
import Shared

private let pardisStoryCoverSize: CGFloat = 68
private let pardisFeaturedStoryCoverHeight: CGFloat = 156

enum PardisIconKind {
    case back
    case book
    case close
    case download
    case home
    case moon
    case play
    case refresh
    case search
    case shield
    case star
    case trash
    case user

    var systemName: String {
        switch self {
        case .back:
            return "chevron.left"
        case .book:
            return "book.closed"
        case .close:
            return "xmark"
        case .download:
            return "arrow.down.to.line"
        case .home:
            return "house"
        case .moon:
            return "moon"
        case .play:
            return "play.fill"
        case .refresh:
            return "arrow.clockwise"
        case .search:
            return "magnifyingglass"
        case .shield:
            return "checkmark.shield"
        case .star:
            return "star"
        case .trash:
            return "trash"
        case .user:
            return "person"
        }
    }
}

struct PardisIcon: View {
    let kind: PardisIconKind
    var size: CGFloat = 18
    var color: Color = PardisColors.ink

    var body: some View {
        Image(systemName: kind.systemName)
            .font(.system(size: size, weight: .semibold))
            .foregroundStyle(color)
            .frame(width: size + PardisSpacing.xs, height: size + PardisSpacing.xs)
            .accessibilityHidden(true)
    }
}

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
                    .font(PardisFonts.display(size: PardisTypography.xxl, weight: .heavy))
                    .foregroundStyle(PardisColors.indigo)
                Text(subtitle)
                    .font(PardisFonts.body(size: PardisTypography.base, weight: .medium))
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
            .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
            .foregroundStyle(foreground)
            .padding(.horizontal, PardisSpacing.sm)
            .padding(.vertical, PardisSpacing.xs)
            .background(background)
            .clipShape(Capsule(style: .continuous))
    }
}

struct PardisSectionHeader: View {
    let title: String
    var subtitle: String?
    var actionLabel: String?
    var action: (() -> Void)?

    var body: some View {
        HStack(alignment: .center, spacing: PardisSpacing.md) {
            VStack(alignment: .leading, spacing: PardisSpacing.xs) {
                Text(title)
                    .font(PardisFonts.display(size: PardisTypography.xl, weight: .bold))
                    .foregroundStyle(PardisColors.ink)
                if let subtitle {
                        Text(subtitle)
                        .font(PardisFonts.body(size: PardisTypography.sm, weight: .medium))
                        .foregroundStyle(PardisColors.inkSoft)
                }
            }
            Spacer(minLength: 0)
            if let actionLabel, let action {
                Button(action: action) {
                    HStack(spacing: PardisSpacing.xs) {
                        PardisIcon(kind: .refresh, size: 14, color: PardisColors.indigo)
                        Text(actionLabel)
                            .font(PardisFonts.body(size: PardisTypography.sm, weight: .semibold))
                    }
                    .foregroundStyle(PardisColors.indigo)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct PardisMetric: Identifiable {
    let id = UUID()
    let value: String
    let label: String
    let tone: PardisMetricTone
}

struct PardisTabItem: Identifiable {
    let id = UUID()
    let label: String
    let icon: PardisIconKind
}

struct PardisBottomTabBar: View {
    let items: [PardisTabItem]
    let selectedIndex: Int
    let onSelect: (Int) -> Void

    var body: some View {
        HStack(spacing: PardisSpacing.xs) {
            ForEach(Array(items.enumerated()), id: \.element.id) { index, item in
                let selected = index == selectedIndex
                Button {
                    onSelect(index)
                } label: {
                    VStack(spacing: PardisSpacing.xxs) {
                        PardisIcon(
                            kind: item.icon,
                            size: 18,
                            color: selected ? PardisColors.saffronDeep : PardisColors.inkMuted
                        )
                        .padding(.horizontal, PardisSpacing.sm)
                        .padding(.vertical, PardisSpacing.xxs)
                        .background(selected ? PardisColors.saffronTint : Color.clear)
                        .clipShape(Capsule(style: .continuous))

                        Text(item.label)
                            .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                            .foregroundStyle(selected ? PardisColors.saffronDeep : PardisColors.inkMuted)
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)
                    }
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, PardisSpacing.sm)
        .padding(.vertical, PardisSpacing.xs)
        .pardisCardSurface(cornerRadius: PardisRadius.lg)
    }
}

enum PardisMetricTone {
    case saffron
    case indigo
    case mint
}

struct PardisMetricStrip: View {
    let metrics: [PardisMetric]

    var body: some View {
        HStack(spacing: PardisSpacing.sm) {
            ForEach(metrics) { metric in
                PardisMetricTile(metric: metric)
            }
        }
    }
}

private struct PardisMetricTile: View {
    let metric: PardisMetric

    private var colors: (background: Color, foreground: Color) {
        switch metric.tone {
        case .saffron:
            return (PardisColors.saffronTint, PardisColors.saffronDeep)
        case .indigo:
            return (PardisColors.indigoTint, PardisColors.indigoDeep)
        case .mint:
            return (PardisColors.mintSoft, PardisColors.mintDeep)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
            Text(metric.value)
                .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                .foregroundStyle(colors.foreground)
                .lineLimit(1)
            Text(metric.label)
                .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                .foregroundStyle(PardisColors.inkSoft)
                .lineLimit(1)
        }
        .padding(PardisSpacing.sm)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(colors.background)
        .overlay(
            RoundedRectangle(cornerRadius: PardisRadius.md, style: .continuous)
                .stroke(PardisComponentColors.cardBorder, lineWidth: PardisSpacing.hairline)
        )
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.md, style: .continuous))
    }
}

struct PardisFilterPill: View {
    let title: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(PardisFonts.body(size: PardisTypography.sm, weight: .semibold))
                .foregroundStyle(selected ? PardisComponentColors.chipSelectedContent : PardisComponentColors.chipContent)
                .padding(.horizontal, PardisSpacing.md)
                .padding(.vertical, PardisSpacing.sm)
                .background(selected ? PardisComponentColors.chipSelectedContainer : PardisComponentColors.chipContainer)
                .overlay(
                    Capsule(style: .continuous)
                        .stroke(
                            selected ? PardisComponentColors.chipSelectedContainer : PardisComponentColors.chipBorder,
                            lineWidth: PardisSpacing.hairline
                        )
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
                    PardisComponentColors.mediaPlaceholderContainer
                }
            } else {
                ZStack {
                    PardisComponentColors.mediaPlaceholderContainer
                    Text(placeholderText)
                        .font(PardisFonts.body(size: PardisTypography.sm, weight: .medium))
                        .foregroundStyle(PardisComponentColors.mediaPlaceholderContent)
                }
            }
        }
        .frame(width: width, height: height)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.md, style: .continuous))
        .accessibilityLabel(accessibilityLabel)
    }
}

struct PardisStoryCard: View {
    let titleEn: String
    let titleFa: String
    let ageBand: String
    let minutes: Int32
    let vocabCount: Int32
    let coverUrl: URL?
    let downloadProgress: String?
    let downloadedSizeLabel: String?
    let isFailed: Bool
    let onSelect: () -> Void
    let onDownload: () -> Void
    let onCancel: () -> Void
    let onRemove: () -> Void
    var noCoverLabel: String = "No cover"
    var cancelLabel: String = "Cancel"
    var removeLabel: String = "Remove"
    var retryLabel: String = "Retry"
    var downloadFailedLabel: String = "Download failed"
    var downloadOfflineLabel: String = "Download offline"

    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.sm) {
            HStack(alignment: .top, spacing: PardisSpacing.sm) {
                PardisAsyncImageFrame(
                    url: coverUrl,
                    accessibilityLabel: "Cover image for \(titleEn)",
                    width: pardisStoryCoverSize,
                    height: pardisStoryCoverSize,
                    placeholderText: noCoverLabel
                )

                VStack(alignment: .leading, spacing: PardisSpacing.xs) {
                    Text(titleEn)
                        .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                        .foregroundStyle(PardisColors.ink)
                        .lineLimit(1)
                    PersianReaderText(
                        text: titleFa,
                        font: PardisFonts.persian(size: PardisTypography.sm, weight: .medium),
                        color: PardisColors.indigo
                    )
                    .lineLimit(1)
                    HStack(spacing: PardisSpacing.xs) {
                        PardisMetaPill(text: "\(ageBand) • \(minutes)m", background: PardisColors.saffronTint, foreground: PardisColors.saffronDeep)
                        PardisMetaPill(text: "\(vocabCount) words", background: PardisColors.indigoTint, foreground: PardisColors.indigoDeep)
                    }
                }
            }

            PardisStoryOfflineControls(
                downloadProgress: downloadProgress,
                downloadedSizeLabel: downloadedSizeLabel,
                isFailed: isFailed,
                onDownload: onDownload,
                onCancel: onCancel,
                onRemove: onRemove,
                cancelLabel: cancelLabel,
                removeLabel: removeLabel,
                retryLabel: retryLabel,
                downloadFailedLabel: downloadFailedLabel,
                downloadOfflineLabel: downloadOfflineLabel
            )
        }
        .padding(PardisSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .pardisCardSurface(cornerRadius: PardisRadius.lg)
        .contentShape(Rectangle())
        .onTapGesture(perform: onSelect)
        .accessibilityAddTraits(.isButton)
    }
}

struct PardisFeaturedStoryCard: View {
    let titleEn: String
    let titleFa: String
    let ageBand: String
    let minutes: Int32
    let vocabCount: Int32
    let coverUrl: URL?
    let onOpen: () -> Void
    var eyebrow: String = "Featured story"
    var blurb: String?
    var actionLabel: String = "Start reading"
    var noCoverLabel: String = "No cover"

    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.sm) {
            PardisAsyncImageFrame(
                url: coverUrl,
                accessibilityLabel: "Cover image for featured story \(titleEn)",
                width: nil,
                height: pardisFeaturedStoryCoverHeight,
                placeholderText: noCoverLabel
            )

            Text(eyebrow)
                .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                .foregroundStyle(PardisColors.inkMuted)

            Text(titleEn)
                .font(PardisFonts.display(size: PardisTypography.xl, weight: .bold))
                .foregroundStyle(PardisColors.ink)
                .lineLimit(2)

            PersianReaderText(
                text: titleFa,
                font: PardisFonts.persian(size: PardisTypography.lg, weight: .semibold),
                color: PardisColors.indigo
            )
            .lineLimit(1)

            if let blurb, !blurb.isEmpty {
                Text(blurb)
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.inkSoft)
                    .lineLimit(2)
            }

            HStack(spacing: PardisSpacing.xs) {
                PardisMetaPill(text: "\(ageBand) • \(minutes)m", background: PardisColors.saffronTint, foreground: PardisColors.saffronDeep)
                PardisMetaPill(text: "\(vocabCount) words", background: PardisColors.indigoTint, foreground: PardisColors.indigoDeep)
            }

            Button(action: onOpen) {
                HStack(spacing: PardisSpacing.xs) {
                    PardisIcon(kind: .book, color: PardisComponentColors.primaryActionContent)
                    Text(actionLabel)
                }
            }
                .buttonStyle(PardisPrimaryButtonStyle())
        }
        .padding(PardisSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .pardisCardSurface(cornerRadius: PardisRadius.lg)
    }
}

private struct PardisStoryOfflineControls: View {
    let downloadProgress: String?
    let downloadedSizeLabel: String?
    let isFailed: Bool
    let onDownload: () -> Void
    let onCancel: () -> Void
    let onRemove: () -> Void
    let cancelLabel: String
    let removeLabel: String
    let retryLabel: String
    let downloadFailedLabel: String
    let downloadOfflineLabel: String

    var body: some View {
        HStack(spacing: PardisSpacing.sm) {
            if let progress = downloadProgress {
                PardisMetaPill(text: progress, background: PardisColors.backgroundAlt, foreground: PardisColors.inkSoft)
                Spacer(minLength: 0)
                Button(action: onCancel) {
                    HStack(spacing: PardisSpacing.xs) {
                        PardisIcon(kind: .close, size: 13, color: PardisColors.inkSoft)
                        Text(cancelLabel)
                    }
                }
                    .buttonStyle(.bordered)
                    .controlSize(.small)
            } else if let size = downloadedSizeLabel {
                PardisMetaPill(text: "Offline • \(size)", background: PardisColors.mintSoft, foreground: PardisColors.mintDeep)
                Spacer(minLength: 0)
                Button(action: onRemove) {
                    HStack(spacing: PardisSpacing.xs) {
                        PardisIcon(kind: .trash, size: 13, color: PardisColors.inkSoft)
                        Text(removeLabel)
                    }
                }
                    .buttonStyle(.bordered)
                    .controlSize(.small)
            } else if isFailed {
                Text(downloadFailedLabel)
                    .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                    .foregroundStyle(PardisColors.error)
                Spacer(minLength: 0)
                Button(action: onDownload) {
                    HStack(spacing: PardisSpacing.xs) {
                        PardisIcon(kind: .refresh, size: 13, color: PardisComponentColors.primaryActionContent)
                        Text(retryLabel)
                    }
                }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.small)
                    .tint(PardisColors.saffron)
            } else {
                Spacer(minLength: 0)
                Button(action: onDownload) {
                    HStack(spacing: PardisSpacing.xs) {
                        PardisIcon(kind: .download, size: 13, color: PardisComponentColors.primaryActionContent)
                        Text(downloadOfflineLabel)
                    }
                }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.small)
                    .tint(PardisColors.saffron)
            }
        }
    }
}

struct PardisReaderHeaderBar: View {
    let onBack: () -> Void
    let pageLabel: String
    let isOffline: Bool
    let backLabel: String
    let offlineLabel: String

    var body: some View {
        PardisPanel {
            HStack(spacing: PardisSpacing.sm) {
                Button(action: onBack) {
                    HStack(spacing: PardisSpacing.xs) {
                        PardisIcon(kind: .back, size: 14, color: PardisColors.indigo)
                        Text(backLabel)
                    }
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .semibold))
                    .foregroundStyle(PardisColors.indigo)
                }
                Spacer()
                PardisMetaPill(text: pageLabel, background: PardisColors.backgroundAlt, foreground: PardisColors.inkSoft)
                if isOffline {
                    PardisMetaPill(text: offlineLabel, background: PardisColors.mintSoft, foreground: PardisColors.mintDeep)
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
                .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
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
                    .stroke(PardisColors.borderSoft, lineWidth: PardisSpacing.hairline)
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
