import SwiftUI
import Shared

/// Story detail page, mirroring Android `PardisDetailScreen` (hero, meta chips, synopsis,
/// narrator note, vocab preview, sticky CTA). Note: omits the paisley pattern overlay on the
/// hero like the other iOS screens (needs asset-catalog motif images — known follow-up).
struct DetailScreen: View {
    let slug: String
    let onRead: (String) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var model = DetailSharedViewModel()

    var body: some View {
        ZStack {
            PardisColors.background.ignoresSafeArea()
            if let story = model.story {
                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        DetailHero(story: story)
                        DetailMetaChips(story: story)
                            .padding(.horizontal, PardisSpacing.lg)
                            .padding(.vertical, PardisSpacing.sm)
                        DetailSynopsis(story: story)
                            .padding(.horizontal, PardisSpacing.lg)
                        DetailNarratorNote()
                            .padding(PardisSpacing.lg)
                        if !model.words.isEmpty {
                            DetailVocabPreview(words: model.words)
                                .padding(.horizontal, PardisSpacing.lg)
                        }
                        Spacer().frame(height: 96)
                    }
                }
                .ignoresSafeArea(edges: .top)
                .overlay(alignment: .bottom) {
                    DetailCtaBar(
                        label: model.canResume ? "Continue reading" : "Start reading",
                        onRead: { onRead(story.slug) }
                    )
                }
            } else if model.isLoading {
                ProgressView().tint(PardisColors.saffron)
            } else {
                VStack(spacing: PardisSpacing.sm) {
                    Text(model.errorMessage ?? "Couldn't load this story.")
                        .font(PardisFonts.body(size: PardisTypography.base, weight: .regular))
                        .foregroundStyle(PardisColors.inkSoft)
                    Button(action: { model.retry() }) {
                        Text("Retry")
                            .font(PardisFonts.body(size: PardisTypography.sm, weight: .semibold))
                            .foregroundStyle(PardisColors.saffronDeep)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 9)
                            .background(PardisColors.saffronSoft)
                            .clipShape(Capsule(style: .continuous))
                    }
                    .buttonStyle(.plain)
                }
                .padding(PardisSpacing.lg)
            }
        }
        // Native navigation bar: kept transparent so the hero still bleeds up behind it, while the
        // system positions the Back/Save controls inside the safe area (below the Dynamic Island)
        // for us — no manual insets, overlays, or GeometryReaders. The hero's own top scrim gives
        // the buttons contrast over the image.
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbarBackground(.hidden, for: .navigationBar)
        .toolbar {
            ToolbarItem(id: "back", placement: .topBarLeading) {
                HeroCircleButton(icon: .back, label: "Back", action: { dismiss() })
            }
            .hidingSharedBackground()
            ToolbarItem(id: "save", placement: .topBarTrailing) {
                HeroCircleButton(icon: .heart, label: "Save", action: {})
            }
            .hidingSharedBackground()
        }
        .toolbar(.hidden, for: .tabBar)
        .task { await model.activate() }
        .onAppear { model.load(slug: slug) }
    }
}

private struct DetailHero: View {
    let story: Story

    var body: some View {
        ZStack {
            PardisAsyncImageFrame(
                url: story.coverUrl.flatMap(URL.init(string:)),
                accessibilityLabel: story.titleEn,
                width: nil,
                height: 360,
                placeholderText: "No cover",
                cornerRadius: 0
            )
            // Scrim: subtle dark at top (for the controls), strong fade to page bg at bottom (title).
            LinearGradient(
                stops: [
                    .init(color: PardisColors.scrimSoft, location: 0.0),
                    .init(color: .clear, location: 0.28),
                    .init(color: .clear, location: 0.62),
                    .init(color: PardisColors.background, location: 1.0),
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        }
        .frame(height: 360)
        .overlay(alignment: .bottomLeading) {
            VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                Text(story.titleEn)
                    .font(PardisFonts.display(size: PardisTypography.xxxl, weight: .heavy))
                    .foregroundStyle(PardisColors.ink)
                    .lineLimit(2)
                Text(story.titleFa)
                    .font(PardisFonts.persian(size: PardisTypography.base, weight: .semibold))
                    .foregroundStyle(PardisColors.inkSoft)
            }
            .padding(.horizontal, PardisSpacing.lg)
            .padding(.vertical, PardisSpacing.md)
        }
    }
}

private struct HeroCircleButton: View {
    let icon: PardisIconKind
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                Circle().fill(PardisColors.surface).frame(width: 44, height: 44)
                PardisIcon(kind: icon, size: 19, color: PardisColors.ink)
            }
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}

private struct DetailMetaChips: View {
    let story: Story

    var body: some View {
        PardisFlowLayout(spacing: PardisSpacing.sm) {
            MetaChip(icon: .clock, label: "\(story.minutes) min")
            MetaChip(icon: .book, label: "\(story.pageCount) pages")
            MetaChip(icon: .user, label: "Age \(story.ageBand)")
            MetaChip(icon: .feather, label: "\(story.vocabCount) words")
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct MetaChip: View {
    let icon: PardisIconKind
    let label: String

    var body: some View {
        HStack(spacing: 6) {
            PardisIcon(kind: icon, size: 13, color: PardisColors.inkMuted)
            Text(label)
                .font(PardisFonts.body(size: PardisTypography.sm, weight: .semibold))
                .foregroundStyle(PardisColors.inkSoft)
        }
        .padding(.horizontal, 11)
        .padding(.vertical, 7)
        .background(PardisColors.surface)
        .clipShape(Capsule(style: .continuous))
    }
}

private struct DetailSynopsis: View {
    let story: Story

    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.sm) {
            Text(story.blurbEn ?? "A new Persian tale to read together.")
                .font(PardisFonts.display(size: PardisTypography.base, weight: .regular))
                .foregroundStyle(PardisColors.ink)
            if let blurbFa = story.blurbFa {
                Text(blurbFa)
                    .font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular))
                    .foregroundStyle(PardisColors.inkMuted)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct DetailVocabPreview: View {
    let words: [VocabItem]

    var body: some View {
        VStack(alignment: .leading, spacing: PardisSpacing.sm) {
            Text("WORDS YOU'LL LEARN")
                .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                .kerning(0.6)
                .foregroundStyle(PardisColors.inkMuted)
            PardisFlowLayout(spacing: PardisSpacing.sm) {
                ForEach(words.prefix(12), id: \.fa) { w in
                    HStack(spacing: 7) {
                        Text(w.fa)
                            .font(PardisFonts.persian(size: PardisTypography.base, weight: .semibold))
                            .foregroundStyle(PardisColors.indigoDeep)
                        Text(w.translit)
                            .font(PardisFonts.mono(size: PardisTypography.xs, weight: .semibold))
                            .foregroundStyle(PardisColors.indigo)
                    }
                    .padding(.horizontal, 13)
                    .padding(.vertical, 8)
                    .background(PardisColors.indigoSoft)
                    .clipShape(Capsule(style: .continuous))
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

private struct DetailNarratorNote: View {
    var body: some View {
        HStack(spacing: 11) {
            ZStack {
                Circle().fill(PardisColors.saffron).frame(width: 40, height: 40)
                PardisIcon(kind: .mic, size: 18, color: PardisColors.inkOnDark)
            }
            VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                Text("Narrated in English & Persian")
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .bold))
                    .foregroundStyle(PardisColors.saffronDeep)
                Text("Word-by-word read-along · tap any Persian word")
                    .font(PardisFonts.body(size: PardisTypography.sm, weight: .medium))
                    .foregroundStyle(PardisColors.saffronDeep)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            PardisIcon(kind: .languages, size: 20, color: PardisColors.saffronDeep)
        }
        .padding(14)
        .background(PardisColors.saffronTint)
        .clipShape(RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: PardisRadius.lg, style: .continuous)
                .stroke(PardisColors.saffronSoft, lineWidth: 1)
        )
    }
}

private struct DetailCtaBar: View {
    let label: String
    let onRead: () -> Void

    var body: some View {
        Button(action: onRead) {
            HStack(spacing: 8) {
                PardisIcon(kind: .play, size: 18, color: PardisColors.inkOnDark)
                Text(label)
                    .font(PardisFonts.display(size: PardisTypography.base, weight: .bold))
                    .foregroundStyle(PardisColors.inkOnDark)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 15)
            .background(PardisColors.saffron)
            .clipShape(Capsule(style: .continuous))
        }
        .buttonStyle(.plain)
        .padding(.horizontal, PardisSpacing.lg)
        .padding(.vertical, PardisSpacing.md)
        .background(
            LinearGradient(
                stops: [
                    .init(color: .clear, location: 0.0),
                    .init(color: PardisColors.background, location: 0.3),
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea(edges: .bottom)
        )
    }
}
