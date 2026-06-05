package app.pardis.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pardis.design.PardisColors
import app.pardis.design.PardisRadius
import app.pardis.design.PardisShadows
import app.pardis.design.PardisSpacing
import app.pardis.shared.library.LibraryAction
import app.pardis.shared.library.LibraryUiState
import app.pardis.shared.library.LibraryViewModel
import app.pardis.shared.reader.ReaderAction
import app.pardis.shared.reader.ReaderUiState
import app.pardis.shared.reader.ReaderViewModel
import app.pardis.core.model.VocabItem
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PardisApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = PardisColors.background
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "library") {
                    composable("library") {
                        LibraryRoute(
                            onOpenStory = { slug ->
                                navController.navigate("reader/$slug")
                            }
                        )
                    }
                    composable(
                        "reader/{slug}",
                        arguments = listOf(navArgument("slug") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val slug = backStackEntry.arguments?.getString("slug") ?: ""
                        ReaderRoute(
                            slug = slug,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryRoute(
    onOpenStory: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenStory = onOpenStory,
    )
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onAction: (LibraryAction) -> Unit,
    onOpenStory: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PardisColors.background)
            .padding(PardisSpacing.md)
    ) {
        Text(
            "Pardis",
            style = MaterialTheme.typography.headlineMedium,
            color = PardisColors.indigo,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            "Persian heritage stories",
            style = MaterialTheme.typography.bodyMedium,
            color = PardisColors.inkSoft
        )
        Spacer(Modifier.height(PardisSpacing.md))

        if (state.isLoading && state.stories.isEmpty()) {
            CircularProgressIndicator(color = PardisColors.saffron)
        }

        state.errorMessage?.let { err ->
            Text("Error: $err", color = MaterialTheme.colorScheme.error)
            Button(onClick = { onAction(LibraryAction.Refresh) }) { Text("Retry") }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(PardisSpacing.sm)
        ) {
            items(state.stories, key = { it.slug }) { story ->
                StoryCard(
                    titleEn = story.titleEn,
                    titleFa = story.titleFa,
                    ageBand = story.ageBand,
                    minutes = story.minutes,
                    vocabCount = story.vocabCount,
                    coverUrl = story.coverUrl,
                    onClick = { onOpenStory(story.slug) }
                )
            }
        }

        // Floating refresh for demo
        Spacer(Modifier.height(PardisSpacing.md))
        Button(
            onClick = { onAction(LibraryAction.Refresh) },
            colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffron)
        ) {
            Text("Refresh library")
        }
    }
}

@Composable
private fun StoryCard(
    titleEn: String,
    titleFa: String,
    ageBand: String,
    minutes: Int,
    vocabCount: Int,
    coverUrl: String?,
    onClick: () -> Unit
) {
    PardisCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        onClick = onClick
    ) {
        Row(Modifier.padding(PardisSpacing.md)) {
            if (coverUrl != null) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "Cover image for story: $titleEn in $ageBand age band",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = PardisSpacing.sm)
                )
            }
            Column {
                Text(
                    titleEn,
                    style = MaterialTheme.typography.titleMedium,
                    color = PardisColors.ink,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    titleFa,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PardisColors.indigo
                )
                Spacer(Modifier.height(PardisSpacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm)) {
                    Text("$ageBand • ${minutes}m", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkSoft)
                    Text("• $vocabCount words", style = MaterialTheme.typography.labelSmall, color = PardisColors.inkMuted)
                }
            }
        }
    }
}

/**
 * Basic PardisCard component using design tokens (colors, radius, spacing, shadows).
 * Follows kmpSkill + Phase 3 plan: tokenised, native only.
 */
@Composable
fun PardisCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(PardisRadius.md)
    Surface(
        modifier = modifier
            .shadow(PardisShadows.md, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = PardisColors.surface2,
        border = androidx.compose.foundation.BorderStroke(1.dp, PardisColors.border)
    ) {
        content()
    }
}

/**
 * Simple vocab chip component using tokens.
 */
@Composable
fun PardisVocabChip(vocab: VocabItem, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = PardisSpacing.xs),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(PardisRadius.sm),
        color = PardisColors.mintSoft
    ) {
        Text(
            "${vocab.fa} (${vocab.translit}) — ${vocab.en}",
            modifier = Modifier.padding(horizontal = PardisSpacing.sm, vertical = PardisSpacing.xs / 2)
                .semantics { contentDescription = "Vocabulary term: ${vocab.fa} transliterated as ${vocab.translit}, English ${vocab.en}" },
            style = MaterialTheme.typography.bodySmall,
            color = PardisColors.ink
        )
    }
}

@Composable
fun ReaderRoute(
    slug: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Load when slug changes (Route owns the VM and triggers load)
    LaunchedEffect(slug) {
        viewModel.onAction(ReaderAction.LoadStory(slug))
    }

    // Basic prefetch for next illustration (performance, using Coil)
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(state.currentPage, state.pages) {
        val nextPage = state.pages.getOrNull(state.currentPage + 1)
        nextPage?.illustrationUrl?.let { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    ReaderScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
fun ReaderScreen(
    state: ReaderUiState,
    onAction: (ReaderAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PardisColors.background)
            .padding(PardisSpacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Library", color = PardisColors.indigo) }
            Spacer(Modifier.weight(1f))
            if (state.pages.isNotEmpty()) {
                Text("${state.currentPage + 1} / ${state.pages.size}", color = PardisColors.inkSoft)
            }
        }

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PardisColors.saffron)
                }
            }
            state.errorMessage != null -> {
                Text("Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
                Button(onClick = { onAction(ReaderAction.LoadStory(state.storySlug)) }) { Text("Retry") }
            }
            state.pages.isEmpty() -> {
                Text("No pages loaded for ${state.storySlug}")
            }
            else -> {
                val page = state.pages.getOrNull(state.currentPage) ?: state.pages.first()

                Text(
                    "Page ${page.page}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PardisColors.inkMuted
                )
                Spacer(Modifier.height(PardisSpacing.sm))

                // Scrollable content for the story page
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Illustration with Coil
                    if (page.illustrationUrl != null) {
                        AsyncImage(
                            model = page.illustrationUrl,
                            contentDescription = "Illustration for page ${page.page} of story",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            color = PardisColors.surfaceLilac
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "No illustration",
                                    color = PardisColors.inkSoft
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(PardisSpacing.md))

                    // Bilingual text
                    Text(page.paragraphsFa.joinToString("\n\n"), style = MaterialTheme.typography.bodyLarge, color = PardisColors.ink)
                    Spacer(Modifier.height(PardisSpacing.sm))
                    Text(page.paragraphsEn.joinToString("\n\n"), style = MaterialTheme.typography.bodyMedium, color = PardisColors.inkSoft)

                    Spacer(Modifier.height(PardisSpacing.lg))

                    if (page.vocabulary.isNotEmpty()) {
                        Text("Vocab on this page:", style = MaterialTheme.typography.labelMedium)
                        Column {
                            page.vocabulary.take(3).forEach { v ->
                                PardisVocabChip(vocab = v)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(PardisSpacing.md))

                // Transport (fixed at bottom)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PardisSpacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = { onAction(ReaderAction.PrevPage) }, enabled = state.currentPage > 0) {
                        Text("Prev")
                    }
                    Button(
                        onClick = { onAction(ReaderAction.NextPage) },
                        enabled = state.currentPage < state.pages.lastIndex,
                        colors = ButtonDefaults.buttonColors(containerColor = PardisColors.saffron)
                    ) {
                        Text(if (state.currentPage == state.pages.lastIndex) "Finish" else "Next")
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { onAction(ReaderAction.ToggleVideo) }) {
                        Text(if (state.isVideoMode) "Text mode" else "Video mode")
                    }
                }
            }
        }
    }
}