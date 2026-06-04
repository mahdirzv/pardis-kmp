import SwiftUI
import Shared

struct ContentView: View {
    @StateObject private var libraryModel = LibrarySharedViewModel()

    var body: some View {
        NavigationStack {
            LibraryScreen(model: libraryModel)
        }
        .collect(flow: libraryModel.uiStateFlow) { state in
            libraryModel.apply(state)
        }
    }
}

struct LibraryScreen: View {
    @ObservedObject var model: LibrarySharedViewModel

    var body: some View {
        VStack {
            Text("Pardis Reader (iOS Native SwiftUI)")
                .font(.largeTitle)
            Text("Shared logic via KMP + SKIE")
            List(model.stories, id: \.slug) { story in
                VStack(alignment: .leading) {
                    Text(story.titleEn)
                    Text(story.titleFa)
                        .font(.caption)
                }
            }
            Button("Refresh (shared action)") {
                model.refresh()
            }
        }
        .padding()
    }
}