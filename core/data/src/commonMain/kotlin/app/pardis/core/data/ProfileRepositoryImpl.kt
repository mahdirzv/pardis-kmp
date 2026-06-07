package app.pardis.core.data

import app.pardis.core.database.PardisDatabase
import app.pardis.core.domain.GetProfilesUseCase
import app.pardis.core.domain.GetSelectedProfileUseCase
import app.pardis.core.domain.ProfileRepository
import app.pardis.core.domain.SelectProfileUseCase
import app.pardis.core.model.ChildProfile
import app.pardis.core.model.pardisProfiles
import kotlin.concurrent.Volatile

private const val KEY_SELECTED_PROFILE = "selected_profile_id"

/**
 * Profile repository. Roster is static demo data; the selected id persists via the generic
 * app_setting table when a SQLDelight driver is present, with an in-memory fallback when not
 * (mirrors StoryRepositoryImpl's optional-db pattern).
 */
class ProfileRepositoryImpl(
    private val db: PardisDatabase? = null,
) : ProfileRepository {

    // Repo is a Koin singleton; @Volatile makes the in-memory fallback's writes visible
    // across coroutine dispatch threads (single-writer/single-reader).
    @Volatile
    private var inMemorySelectedId: String? = null

    override fun profiles(): List<ChildProfile> = pardisProfiles

    override suspend fun selectedProfileId(): String? =
        db?.pardisQueries?.getSetting(KEY_SELECTED_PROFILE)?.executeAsOneOrNull()
            ?: inMemorySelectedId

    override suspend fun setSelectedProfile(id: String) {
        inMemorySelectedId = id
        db?.pardisQueries?.setSetting(KEY_SELECTED_PROFILE, id)
    }
}

class GetProfilesUseCaseImpl(
    private val repository: ProfileRepository,
) : GetProfilesUseCase {
    override fun invoke(): List<ChildProfile> = repository.profiles()
}

class GetSelectedProfileUseCaseImpl(
    private val repository: ProfileRepository,
) : GetSelectedProfileUseCase {
    override suspend fun invoke(): ChildProfile? {
        val id = repository.selectedProfileId() ?: return null
        return repository.profiles().firstOrNull { it.id == id }
    }
}

class SelectProfileUseCaseImpl(
    private val repository: ProfileRepository,
) : SelectProfileUseCase {
    override suspend fun invoke(id: String) = repository.setSelectedProfile(id)
}
