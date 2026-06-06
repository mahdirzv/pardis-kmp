package app.pardis.core.data

import app.pardis.core.domain.GetProgressUseCase
import app.pardis.core.domain.SaveProgressUseCase
import app.pardis.core.domain.StoryRepository

class SaveProgressUseCaseImpl(
    private val repository: StoryRepository
) : SaveProgressUseCase {
    override suspend fun invoke(slug: String, page: Int) {
        repository.saveProgress(slug, page)
    }
}

class GetProgressUseCaseImpl(
    private val repository: StoryRepository
) : GetProgressUseCase {
    override suspend fun invoke(slug: String): Int? {
        return repository.getProgress(slug)
    }
}