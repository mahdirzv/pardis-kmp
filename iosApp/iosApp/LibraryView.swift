import SwiftUI
import Shared

private let libraryGridCoverHeight: CGFloat = 200
private let libraryListCoverW: CGFloat = 60
private let libraryListCoverH: CGFloat = 78

/// "Library" tab (v2), mirroring Android `LibraryScreen`: header + grid/list toggle, search,
/// filter pills, a 2-column cover grid (or list), and by-age tiles.
struct LibraryView: View {
    let model: LibrarySharedViewModel
    let onOpenStory: (String) -> Void

    @State private var grid = true
    private let columns = [GridItem(.flexible(), spacing: 14), GridItem(.flexible(), spacing: 14)]

    private func coverURL(_ s: Story) -> URL? {
        (model.localCoverUrls[s.slug] ?? s.coverUrl).flatMap(URL.init(string:))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PardisSpacing.md) {
                HStack(alignment: .bottom) {
                    VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                        Text("Library").font(PardisFonts.display(size: PardisTypography.xxl, weight: .heavy)).foregroundStyle(PardisColors.ink)
                        Text("کتابخانه‌ی قصه‌ها").font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular)).foregroundStyle(PardisColors.inkMuted)
                    }
                    Spacer()
                    GridListToggle(grid: $grid)
                }
                .accessibilityAddTraits(.isHeader)

                LibrarySearchPill(model: model)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: PardisSpacing.xs) {
                        PardisFilterPill(title: "All ages", selected: model.selectedAgeBand == nil && !model.showOnlyCached) { model.setAgeBand(nil) }
                        ForEach(model.ageBands, id: \.self) { band in
                            PardisFilterPill(title: band, selected: model.selectedAgeBand == band) {
                                model.setAgeBand(model.selectedAgeBand == band ? nil : band)
                            }
                        }
                        PardisFilterPill(title: model.totalCachedLabel.isEmpty ? "Offline" : "Offline · \(model.totalCachedLabel)", selected: model.showOnlyCached) {
                            model.toggleShowOnlyCached()
                        }
                    }
                }

                if model.isLoading && model.stories.isEmpty {
                    ProgressView().tint(PardisColors.saffron).frame(maxWidth: .infinity)
                }
                if let err = model.errorMessage {
                    VStack(alignment: .leading, spacing: PardisSpacing.xs) {
                        Text("Error: \(err)").font(PardisFonts.body(size: PardisTypography.base, weight: .regular)).foregroundStyle(PardisColors.error)
                        Button("Retry") { model.refresh() }
                            .font(PardisFonts.body(size: PardisTypography.sm, weight: .bold))
                            .foregroundStyle(PardisColors.saffronDeep)
                            .padding(.horizontal, 12).padding(.vertical, 6)
                            .background(PardisColors.saffronSoft).clipShape(Capsule())
                    }
                }

                if grid {
                    LazyVGrid(columns: columns, spacing: PardisSpacing.md) {
                        ForEach(model.stories, id: \.slug) { s in
                            LibraryCover(story: s, cover: coverURL(s),
                                         cachedLabel: model.downloadedSizeLabels[s.slug],
                                         progress: model.downloadProgress[s.slug],
                                         failed: model.failedDownloads.contains(s.slug),
                                         onOpen: { onOpenStory(s.slug) },
                                         onDownload: { model.downloadStory(s.slug) })
                        }
                    }
                } else {
                    VStack(spacing: 0) {
                        ForEach(model.stories, id: \.slug) { s in
                            LibraryListRow(story: s, cover: coverURL(s)) { onOpenStory(s.slug) }
                        }
                    }
                }

                if !model.ageBands.isEmpty {
                    VStack(alignment: .leading, spacing: PardisSpacing.sm) {
                        PardisSectionHeader(title: "By age", subtitle: "بر اساس سن")
                        HStack(spacing: 10) {
                            let labels = ["Little ones", "Readers", "Explorers"]
                            let tones = ["mint", "saffron", "lapis"]
                            ForEach(Array(model.ageBands.prefix(3).enumerated()), id: \.offset) { i, band in
                                LibraryAgeTile(band: band, label: labels[i % 3], tone: tones[i % 3]) {
                                    model.setAgeBand(model.selectedAgeBand == band ? nil : band)
                                }
                            }
                        }
                    }
                    .padding(.vertical, PardisSpacing.sm)
                }
            }
            .padding(.horizontal, PardisSpacing.lg)
            .padding(.top, PardisSpacing.xl)
        }
    }
}

private struct GridListToggle: View {
    @Binding var grid: Bool
    var body: some View {
        HStack(spacing: 2) {
            seg(.grid, active: grid) { grid = true }
            seg(.listView, active: !grid) { grid = false }
        }
        .padding(3)
        .background(PardisColors.backgroundAlt)
        .clipShape(Capsule(style: .continuous))
    }
    private func seg(_ icon: PardisIconKind, active: Bool, _ tap: @escaping () -> Void) -> some View {
        Button(action: tap) {
            PardisIcon(kind: icon, size: 18, color: active ? PardisColors.ink : PardisColors.inkMuted)
                .padding(.horizontal, 12).padding(.vertical, 6)
                .background(active ? PardisColors.surface : Color.clear)
                .clipShape(Capsule(style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

private struct LibrarySearchPill: View {
    let model: LibrarySharedViewModel
    var body: some View {
        HStack(spacing: 10) {
            PardisIcon(kind: .search, size: 19, color: PardisColors.inkMuted)
            TextField("Search heroes, words, voyages…", text: Binding(
                get: { model.searchQuery },
                set: { model.searchQuery = $0; model.search(query: $0) }
            ))
            .textFieldStyle(.plain)
            .font(PardisFonts.body(size: PardisTypography.base, weight: .regular))
            PardisIcon(kind: .mic, size: 18, color: PardisColors.saffron)
        }
        .padding(.horizontal, 16)
        .frame(height: 48)
        .background(PardisColors.surface)
        .clipShape(Capsule(style: .continuous))
        .overlay(Capsule(style: .continuous).stroke(PardisColors.border, lineWidth: PardisSpacing.hairline))
    }
}

private struct LibraryCover: View {
    let story: Story
    let cover: URL?
    let cachedLabel: String?
    let progress: String?
    let failed: Bool
    let onOpen: () -> Void
    let onDownload: () -> Void

    private var cached: Bool { cachedLabel != nil }

    var body: some View {
        Button(action: onOpen) {
            VStack(alignment: .leading, spacing: PardisSpacing.xs) {
                coverArt
                    .frame(height: libraryGridCoverHeight)
                    .frame(maxWidth: .infinity)
                    .clipShape(RoundedRectangle(cornerRadius: PardisRadius.md, style: .continuous))
                    // Time badge sits on the art, top-right (design).
                    .overlay(alignment: .topTrailing) {
                        Text("\(story.minutes)m")
                            .font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold))
                            .foregroundStyle(PardisColors.inkOnDark)
                            .padding(.horizontal, 8).padding(.vertical, 3)
                            .background(PardisColors.scrimSoft).clipShape(Capsule())
                            .padding(8)
                    }
                    // Download control on the art, top-left: icon-only download, a tick when offline,
                    // a spinner while downloading.
                    .overlay(alignment: .topLeading) { downloadControl.padding(8) }
                Text(story.titleEn).font(PardisFonts.display(size: PardisTypography.base, weight: .semibold)).foregroundStyle(PardisColors.ink).lineLimit(1)
                Text(story.titleFa).font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular)).foregroundStyle(PardisColors.inkMuted).lineLimit(1)
            }
        }
        .buttonStyle(.plain)
    }

    // Scene art as the cover base (design uses procedural art — no blank "missing" placeholder),
    // with the remote cover drawn on top when it loads.
    private var coverArt: some View {
        PardisSceneArt(seed: story.slug)
            .overlay {
                if let cover {
                    AsyncImage(url: cover) { $0.resizable().scaledToFill() } placeholder: { Color.clear }
                }
            }
            .clipped()
    }

    @ViewBuilder
    private var downloadControl: some View {
        if cached {
            badge(fill: PardisColors.mint) { PardisIcon(kind: .check, size: 14, color: PardisColors.inkOnDark) }
        } else if progress != nil {
            badge(fill: PardisColors.scrim) { ProgressView().tint(PardisColors.inkOnDark).scaleEffect(0.6) }
        } else {
            badge(fill: PardisColors.scrim) { PardisIcon(kind: failed ? .refresh : .download, size: 14, color: PardisColors.inkOnDark) }
                .highPriorityGesture(TapGesture().onEnded { onDownload() })
        }
    }

    private func badge<Content: View>(fill: Color, @ViewBuilder _ content: () -> Content) -> some View {
        ZStack { Circle().fill(fill); content() }.frame(width: 28, height: 28)
    }
}

private struct LibraryListRow: View {
    let story: Story
    let cover: URL?
    let onOpen: () -> Void
    var body: some View {
        Button(action: onOpen) {
            HStack(spacing: 13) {
                PardisAsyncImageFrame(url: cover, accessibilityLabel: story.titleEn, width: libraryListCoverW, height: libraryListCoverH)
                    .clipShape(RoundedRectangle(cornerRadius: PardisRadius.sm, style: .continuous))
                VStack(alignment: .leading, spacing: PardisSpacing.xxs) {
                    Text(story.titleEn).font(PardisFonts.display(size: PardisTypography.base, weight: .semibold)).foregroundStyle(PardisColors.ink).lineLimit(1)
                    Text(story.titleFa).font(PardisFonts.persian(size: PardisTypography.sm, weight: .regular)).foregroundStyle(PardisColors.inkMuted).lineLimit(1)
                    Text("\(story.ageBand) · \(story.minutes)m · \(story.vocabCount) words").font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold)).foregroundStyle(PardisColors.inkMuted)
                }
                Spacer(minLength: 0)
                PardisIcon(kind: .chevRight, size: 18, color: PardisColors.inkFaint)
            }
            .padding(.vertical, PardisSpacing.xs)
        }
        .buttonStyle(.plain)
    }
}

private struct LibraryAgeTile: View {
    let band: String
    let label: String
    let tone: String
    let onTap: () -> Void
    var body: some View {
        let c = pardisToneColors(tone)
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 0) {
                Text(band).font(PardisFonts.display(size: PardisTypography.lg, weight: .heavy)).foregroundStyle(c.deep)
                Text(label).font(PardisFonts.body(size: PardisTypography.xs, weight: .semibold)).foregroundStyle(c.deep)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 12).padding(.vertical, 16)
            .background(c.soft)
            .clipShape(RoundedRectangle(cornerRadius: PardisRadius.base, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}
