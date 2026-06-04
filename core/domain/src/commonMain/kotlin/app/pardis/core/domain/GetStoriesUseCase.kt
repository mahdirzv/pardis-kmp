package app.pardis.core.domain

import app.pardis.core.model.Story

interface GetStoriesUseCase {
    suspend operator fun invoke(): List<Story>
}