package com.example.pokedexapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.pokedexapp.data.local.PokemonDatabase
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.data.mappers.toEntity
import com.example.pokedexapp.data.remote.GraphQlQuery
import com.example.pokedexapp.data.remote.PokeApi
import com.example.pokedexapp.data.remote.PokeQueries
import com.example.pokedexapp.data.remote.PokemonRemoteMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class PokemonRepository(
    val db: PokemonDatabase,
    val api: PokeApi
) {
    // The Main Feed (With API Sync)
    fun getPokemonList(): Flow<PagingData<PokemonEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 40),
            remoteMediator = PokemonRemoteMediator(api,db),
            pagingSourceFactory = {
                db.dao.pagingSource()
            }
        ).flow
    }

    // Search (Local Only)
    fun searchPokemon(query: String): Flow<List<PokemonEntity>> {
        return db.dao.searchPokemon(query)
    }
    fun getFullPokemonDetails(id: Int): Flow<PokemonEntity?> {
        return flow {
            val localPokemon: PokemonEntity? = db.dao.getPokemonById(id)
            if (localPokemon != null && localPokemon.hp > 0) {
                emit(localPokemon)
                return@flow
            }
            try {
                val response = api.getPokemonByQuery(
                    GraphQlQuery(
                        query = PokeQueries.POKEMON_DETAILS_QUERY,
                        variables = mapOf("id" to id)
                    )
                )
                response.data?.pokemon?.firstOrNull()?.let { gql ->

                    val entity = gql.toEntity()

                    db.dao.insertPokemon(entity)
                    emit(entity)
                }
            }
            catch(e:Exception){
                if (localPokemon == null) emit(null)
                Timber.e(e, "Failed to fetch details")
            }
        }.flowOn(Dispatchers.IO)
    }

    // One time sync - handled by WorkManager
    suspend fun syncSearchIndex() {
        withContext(Dispatchers.IO) {
            val response =
                api.getPokemonByQuery(GraphQlQuery(PokeQueries.SEARCH_LIST_QUERY, emptyMap()))
            val namesOnly = response.data?.pokemon?.map {
                PokemonEntity(
                    id = it.id, name = it.name,
                )
            } ?: emptyList()
            db.dao.insertSearchNames(namesOnly)
        }
    }
}