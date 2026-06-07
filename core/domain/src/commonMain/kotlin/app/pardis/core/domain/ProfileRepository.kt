package app.pardis.core.domain

import app.pardis.core.model.ChildProfile

interface ProfileRepository {
    fun profiles(): List<ChildProfile>
    suspend fun selectedProfileId(): String?
    suspend fun setSelectedProfile(id: String)
}

/** Returns the full demo roster. */
interface GetProfilesUseCase {
    operator fun invoke(): List<ChildProfile>
}

/** Resolves the persisted selected id against the roster; null if none / unknown. */
interface GetSelectedProfileUseCase {
    suspend operator fun invoke(): ChildProfile?
}

/** Persists the selected profile id. */
interface SelectProfileUseCase {
    suspend operator fun invoke(id: String)
}
