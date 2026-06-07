package app.pardis.shared.profile

import app.pardis.core.data.ProfileRepositoryImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileRepositoryImplTest {

    @Test
    fun profiles_returnsRoster() {
        val repo = ProfileRepositoryImpl(db = null)
        assertEquals(3, repo.profiles().size)
        assertEquals("roya", repo.profiles().first().id)
    }

    @Test
    fun selectedProfileId_isNull_beforeAnySelection() = runTest {
        val repo = ProfileRepositoryImpl(db = null)
        assertNull(repo.selectedProfileId())
    }

    @Test
    fun setSelectedProfile_thenSelectedProfileId_roundTrips() = runTest {
        val repo = ProfileRepositoryImpl(db = null)
        repo.setSelectedProfile("darius")
        assertEquals("darius", repo.selectedProfileId())
    }
}
