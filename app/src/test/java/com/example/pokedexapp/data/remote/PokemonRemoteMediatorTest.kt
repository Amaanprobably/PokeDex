package com.example.pokedexapp.data.remote

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator.MediatorResult
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.pokedexapp.data.local.PokemonDatabase
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.data.local.RemoteKeys
import com.example.pokedexapp.data.remote.responses.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
class PokemonRemoteMediatorTest {

    private lateinit var db: PokemonDatabase
    private lateinit var api: PokeApi
    private lateinit var mediator: PokemonRemoteMediator

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PokemonDatabase::class.java
        ).allowMainThreadQueries().build()
        api = mockk()
        mediator = PokemonRemoteMediator(api, db)
    }

    @After
    fun teardown() {
        db.close()
    }
    private fun buildGqlPokemon(id: Int) = PokemonGql(
        id = id,
        name = "pokemon-$id",
        height = 7,
        weight = 69,
        sprites = null,
        types = listOf(TypeWrapper(TypeName("normal"))),
        stats = listOf(StatWrapper(45, StatName("hp")))
    )

    private fun buildGqlResponse(ids: List<Int>) =
        GraphQlResponse(data = GraphQlData(pokemon = ids.map { buildGqlPokemon(it) }))

    private fun createPagingState(
        pages: List<PagingSource.LoadResult.Page<Int, PokemonEntity>> = emptyList(),
        anchorPosition: Int? = null,
        pageSize: Int = 40
    ) = PagingState(
        pages = pages,
        anchorPosition = anchorPosition,
        config = PagingConfig(pageSize = pageSize),
        leadingPlaceholderCount = 0
    )

    @Test
    fun `REFRESH with no anchor position loads from offset 0`() = runTest {
        // creates an empty container MockK can pour a real argument into.
        val querySlot = slot<GraphQlQuery>()
        //capture() saves a reference to whatever GraphQlQuery object the mediator actually constructed and passed in into querySlot.
        coEvery { api.getPokemonByQuery(capture(querySlot)) } returns buildGqlResponse((1..40).toList())

        val result = mediator.load(LoadType.REFRESH, createPagingState())

        // reads the offset key out of the exact map the mediator itself built
        assertEquals(0, querySlot.captured.variables["offset"])
        assertTrue(result is MediatorResult.Success)
        assertFalse((result as MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `REFRESH clears stale pokemon and remote keys before inserting the new page`() = runTest {
        db.dao.insertPokemon(PokemonEntity(id = 999, name = "stale"))
        db.dao.insertAllRemoteKeys(listOf(RemoteKeys(pokemonId = 999, prevKey = null, nextKey = 40)))

        coEvery { api.getPokemonByQuery(any()) } returns buildGqlResponse(listOf(1, 2))

        mediator.load(LoadType.REFRESH, createPagingState())

        assertNull(db.dao.getPokemonById(999))
        assertNull(db.dao.getRemoteKeys(999))
        assertNotNull(db.dao.getPokemonById(1))
        assertNotNull(db.dao.getPokemonById(2))
    }

    @Test
    fun `REFRESH with an empty API response reaches end of pagination without crashing`() = runTest {
        coEvery { api.getPokemonByQuery(any()) } returns GraphQlResponse(data = GraphQlData(pokemon = emptyList()))

        val result = mediator.load(LoadType.REFRESH, createPagingState())

        assertTrue(result is MediatorResult.Success)
        assertTrue((result as MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `REFRESH with a null data field falls back to empty list without crashing`() = runTest {
        coEvery { api.getPokemonByQuery(any()) } returns GraphQlResponse(data = null)

        val result = mediator.load(LoadType.REFRESH, createPagingState())

        assertTrue(result is MediatorResult.Success)
        assertTrue((result as MediatorResult.Success).endOfPaginationReached)
    }
    @Test
    fun `REFRESH with an anchor position resumes from the anchored item's stored offset`() = runTest {
        db.dao.insertAllRemoteKeys(listOf(RemoteKeys(pokemonId = 55, prevKey = 40, nextKey = 80)))

        val querySlot = slot<GraphQlQuery>()
        coEvery { api.getPokemonByQuery(capture(querySlot)) } returns buildGqlResponse((41..80).toList())

        val anchoredPage = PagingSource.LoadResult.Page(
            data = listOf(PokemonEntity(id = 55, name = "pokemon-55")),
            prevKey = 40,
            nextKey = 80
        )
        val state = createPagingState(pages = listOf(anchoredPage), anchorPosition = 0)

        mediator.load(LoadType.REFRESH, state)

        // nextKey(80) - 40 == 40 (expected) -> refresh should resume near the user's scroll position
        assertEquals(40, querySlot.captured.variables["offset"])
    }
    @Test
    fun `APPEND fetches the next page using the last item's stored nextKey`() = runTest {
        db.dao.insertAllRemoteKeys(listOf(RemoteKeys(pokemonId = 40, prevKey = 0, nextKey = 40)))

        val querySlot = slot<GraphQlQuery>()
        coEvery { api.getPokemonByQuery(capture(querySlot)) } returns buildGqlResponse((41..80).toList())

        val lastPage = PagingSource.LoadResult.Page(
            data = listOf(PokemonEntity(id = 40, name = "pokemon-40")),
            prevKey = 0,
            nextKey = 40
        )

        val result = mediator.load(LoadType.APPEND, createPagingState(pages = listOf(lastPage)))

        assertEquals(40, querySlot.captured.variables["offset"])
        assertFalse((result as MediatorResult.Success).endOfPaginationReached)
        assertNotNull(db.dao.getPokemonById(41))
    }

    @Test
    fun `APPEND reaches end of pagination when stored nextKey is null, without calling the API`() = runTest {
        db.dao.insertAllRemoteKeys(listOf(RemoteKeys(pokemonId = 40, prevKey = 0, nextKey = null)))

        val lastPage = PagingSource.LoadResult.Page(
            data = listOf(PokemonEntity(id = 40, name = "pokemon-40")),
            prevKey = 0,
            nextKey = null
        )

        val result = mediator.load(LoadType.APPEND, createPagingState(pages = listOf(lastPage)))

        assertTrue((result as MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { api.getPokemonByQuery(any()) }
    }
    @Test
    fun `PREPEND reaches end of pagination when stored prevKey is null`() = runTest {
        db.dao.insertAllRemoteKeys(listOf(RemoteKeys(pokemonId = 1, prevKey = null, nextKey = 40)))

        val firstPage = PagingSource.LoadResult.Page(
            data = listOf(PokemonEntity(id = 1, name = "pokemon-1")),
            prevKey = null,
            nextKey = 40
        )

        val result = mediator.load(LoadType.PREPEND, createPagingState(pages = listOf(firstPage)))

        assertTrue((result as MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { api.getPokemonByQuery(any()) }
    }
    @Test
    fun `IOException from the API is surfaced as MediatorResult Error`() = runTest {
        val exception = IOException("no internet")
        coEvery { api.getPokemonByQuery(any()) } throws exception

        val result = mediator.load(LoadType.REFRESH, createPagingState())

        assertTrue(result is MediatorResult.Error)
        assertEquals(exception, (result as MediatorResult.Error).throwable)
    }

    @Test
    fun `HttpException from the API is surfaced as MediatorResult Error`() = runTest {
        val exception = mockk<HttpException>(relaxed = true)
        coEvery { api.getPokemonByQuery(any()) } throws exception

        val result = mediator.load(LoadType.REFRESH, createPagingState())

        assertTrue(result is MediatorResult.Error)
    }
}