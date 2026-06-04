package app.pardis.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.shared.library.LibraryAction
import app.pardis.shared.library.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PardisApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LibraryScreen()
        }
    }
}

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pardis Reader (Android Native Compose)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Shared logic loaded via Koin + ViewModel")
        Text("Stories: ${state.stories.size}")
        Button(onClick = { viewModel.onAction(LibraryAction.Refresh) }) {
            Text("Refresh (calls shared)")
        }
        // TODO: List the stories, tap to navigate to native Reader screen with MP4 or page audio
    }
}