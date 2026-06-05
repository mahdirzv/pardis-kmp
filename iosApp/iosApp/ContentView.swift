import SwiftUI
import Shared

struct ContentView: View {
    @State private var selectedSlug: String? = nil

    var body: some View {
        NavigationStack {
            LibraryScreen(onSelect: { slug in selectedSlug = slug })
                .navigationDestination(item: $selectedSlug) { slug in
                    ReaderScreen(slug: slug)
                }
        }
        .environment(\.layoutDirection, .rightToLeft) // RTL for Farsi/Persian content (bilingual handled per text)
    }
}

struct LibraryScreen: View {
    @State private var model = LibrarySharedViewModel()
    var onSelect: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Pardis")
                .font(.largeTitle)
                .foregroundStyle(PardisColors.indigo)
                .accessibilityAddTraits(.isHeader)
            Text("Persian heritage stories")
                .foregroundStyle(PardisColors.inkSoft)

            if model.isLoading && model.stories.isEmpty {
                ProgressView().tint(PardisColors.saffron)
            }

            List(model.stories, id: \.slug) { story in
                HStack {
                    if let cover = story.coverUrl, let url = URL(string: cover) {
                        AsyncImage(url: url) { image in
                            image.resizable().scaledToFill()
                        } placeholder: {
                            Color(PardisColors.surfaceLilac)
                        }
                        .frame(width: 50, height: 50)
                        .cornerRadius(PardisRadius.sm)
                        .padding(.trailing, PardisSpacing.sm)
                        .accessibilityLabel("Cover image for \(story.titleEn)")
                    }
                    VStack(alignment: .leading, spacing: 4) {
                        Text(story.titleEn).font(.headline)
                        Text(story.titleFa).font(.subheadline).foregroundStyle(PardisColors.indigo)
                        Text("\(story.ageBand) • \(story.minutes)m • \(story.vocabCount) words")
                            .font(.caption)
                            .foregroundStyle(PardisColors.inkMuted)
                    }
                }
                .padding(PardisSpacing.md)
                .background(PardisColors.surface2)
                .cornerRadius(PardisRadius.md)
                .overlay(
                    RoundedRectangle(cornerRadius: PardisRadius.md)
                        .stroke(PardisColors.border, lineWidth: 1)
                )
                .contentShape(Rectangle())
                .onTapGesture { onSelect(story.slug) }
                // TODO: Extract to PardisCard view modifier / struct per Phase 3 design system plan
            }

            Button("Refresh") {
                model.refresh()
            }
            .buttonStyle(.borderedProminent)
            .tint(PardisColors.saffron)
        }
        .padding()
        .background(PardisColors.background.ignoresSafeArea())
        .task {
            await model.activate()
        }
    }
}

struct ReaderScreen: View {
    let slug: String
    @State private var model = ReaderSharedViewModel()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Button("← Back") { dismiss() }
                    .foregroundStyle(PardisColors.indigo)
                Spacer()
                if !model.pages.isEmpty {
                    Text("\(model.currentPage + 1) / \(model.pages.count)")
                        .foregroundStyle(PardisColors.inkSoft)
                }
            }

            if model.isLoading && model.pages.isEmpty {
                ProgressView().tint(PardisColors.saffron)
            } else if let err = model.errorMessage {
                Text("Error: \(err)").foregroundStyle(.red)
            } else if let page = model.pages[safe: model.currentPage] {
                Text("Page \(page.page)")
                    .font(.caption)
                    .foregroundStyle(PardisColors.inkMuted)

                // Illustration with AsyncImage
                if let urlStr = page.illustrationUrl, let url = URL(string: urlStr) {
                    AsyncImage(url: url) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Color(PardisColors.surfaceLilac)
                    }
                    .frame(height: 220)
                    .cornerRadius(12)
                    .accessibilityLabel("Illustration for page \(page.page)")
                } else {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(PardisColors.surfaceLilac)
                        .frame(height: 220)
                        .overlay(Text("No illustration").foregroundStyle(PardisColors.inkSoft))
                }

                Text(page.paragraphsFa.joined(separator: "\n\n"))
                    .font(.body)

                Text(page.paragraphsEn.joined(separator: "\n\n"))
                    .font(.callout)
                    .foregroundStyle(PardisColors.inkSoft)

                if !page.vocabulary.isEmpty {
                    Text("Vocab").font(.headline)
                    ForEach(page.vocabulary.prefix(3), id: \.fa) { v in
                        Text("\(v.fa) (\(v.translit)) — \(v.en)")
                            .font(.caption)
                            .padding(4)
                            .background(PardisColors.mintSoft)
                            .cornerRadius(PardisRadius.sm)
                            .accessibilityLabel("Vocabulary: \(v.fa) means \(v.en), transliteration \(v.translit)")
                    }
                }
            } else {
                Text("Loading story \(slug)...")
            }

            HStack {
                Button("Prev") { model.prevPage() }.disabled(model.currentPage == 0)
                Button("Next") { model.nextPage() }
                    .buttonStyle(.borderedProminent)
                    .tint(PardisColors.saffron)
                Spacer()
                Button(model.isVideoMode ? "Text" : "Video") { model.toggleVideo() }
            }
        }
        .padding()
        .background(PardisColors.background.ignoresSafeArea())
        .task {
            await model.activate()
        }
        .onAppear {
            model.load(slug: slug)
        }
    }
}

extension Collection {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}