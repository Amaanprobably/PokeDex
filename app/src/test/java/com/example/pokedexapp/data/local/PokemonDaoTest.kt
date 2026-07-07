package com.example.pokedexapp.data.local

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asSnapshot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PokemonDaoTest {
    private lateinit var db: PokemonDatabase
    private lateinit var dao: PokemonDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PokemonDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.dao
    }

    @After
    fun closeDB() {
        db.close()
    }

    private fun buildPokemonEntity(id: Int = 1, name: String = "bulbasaur") =
        PokemonEntity(id = id, name = name)

    @Test
    fun `insertPokemon returns the same entity`() = runTest {
        val pokemon = buildPokemonEntity(id = 1, name = "bulbasaur")
        dao.insertPokemon(pokemon)

        val result = dao.getPokemonById(1)

        assertEquals(pokemon, result)
    }
    @Test
    fun `getPokemonById returns null when id does not exist`() = runTest {
        assertNull(dao.getPokemonById(999))
    }
    @Test
    fun `insertAllPokemon with REPLACE overwrites existing row on same id`() = runTest {
        dao.insertPokemon(buildPokemonEntity(id = 1, name = "bulbasaur"))

        dao.insertAllPokemon(listOf(buildPokemonEntity(id = 1, name = "ivysaur")))

        assertEquals("ivysaur", dao.getPokemonById(1)?.name)
    }
    @Test
    fun `clearAllPokemon removes every row`() = runTest {
        dao.insertAllPokemon(listOf(buildPokemonEntity(1), buildPokemonEntity(2)))

        dao.clearAllPokemon()

        assertNull(dao.getPokemonById(1))
        assertNull(dao.getPokemonById(2))
    }

    @Test
    fun `pagingSource returns all inserted pokemon in insertion order`() = runTest {
        dao.insertAllPokemon(
            listOf(
                buildPokemonEntity(1, "bulbasaur"),
                buildPokemonEntity(2, "ivysaur"),
                buildPokemonEntity(3, "venusaur")
            )
        )

        val pager = Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { dao.pagingSource() }
        )

        val items = pager.flow.asSnapshot()

        assertEquals(listOf("bulbasaur", "ivysaur", "venusaur"), items.map { it.name })
    }
    @Test
    fun `searchPokemon returns entries whose name contains the query`() = runTest {
        dao.insertSearchNames(
            listOf(
                SearchPokemonEntity(1, "bulbasaur", ""),
                SearchPokemonEntity(2, "ivysaur", ""),
                SearchPokemonEntity(3, "charmander", "")
            )
        )

        dao.searchPokemon("saur").test {
            val result = awaitItem()
            assertEquals(listOf("bulbasaur", "ivysaur"), result.map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchPokemon is case-insensitive`() = runTest {
        dao.insertSearchNames(listOf(SearchPokemonEntity(1, "Bulbasaur", "")))

        dao.searchPokemon("bulba").test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchPokemon Flow emits a new list when matching data is inserted later`() = runTest {
        dao.searchPokemon("char").test {
            assertEquals(emptyList<SearchPokemonEntity>(), awaitItem())

            dao.insertSearchNames(listOf(SearchPokemonEntity(1, "charmander", "")))

            assertEquals(listOf("charmander"), awaitItem().map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test
    fun `insertAllRemoteKeys then getRemoteKeys returns the stored keys`() = runTest {
        val keys = RemoteKeys(pokemonId = 1, prevKey = null, nextKey = 2)
        dao.insertAllRemoteKeys(listOf(keys))

        assertEquals(keys, dao.getRemoteKeys(1))
    }

    @Test
    fun `getRemoteKeys returns null for an id with no stored keys`() = runTest {
        assertNull(dao.getRemoteKeys(999))
    }

    @Test
    fun `clearRemoteKeys removes all stored keys`() = runTest {
        dao.insertAllRemoteKeys(listOf(RemoteKeys(1, null, 2)))

        dao.clearRemoteKeys()

        assertNull(dao.getRemoteKeys(1))
    }
}