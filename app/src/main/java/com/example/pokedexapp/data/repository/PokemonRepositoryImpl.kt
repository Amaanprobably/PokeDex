package com.example.pokedexapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.pokedexapp.data.local.PokemonDatabase
import com.example.pokedexapp.domain.model.Pokemon
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.data.mappers.toDomain
import com.example.pokedexapp.data.mappers.toEntity
import com.example.pokedexapp.data.remote.responses.GraphQlQuery
import com.example.pokedexapp.data.remote.PokeApi
import com.example.pokedexapp.data.remote.PokeQueries
import com.example.pokedexapp.data.remote.PokemonRemoteMediator
import com.example.pokedexapp.domain.repository.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalPagingApi::class)
class PokemonRepositoryImpl(
    val db: PokemonDatabase,
    val api: PokeApi
) : PokemonRepository {
    // The Main Feed (With API Sync)
    override fun getPokemonList(): Flow<PagingData<Pokemon>> {
        return Pager(
            config = PagingConfig(pageSize = 40),
            remoteMediator = PokemonRemoteMediator(api,db),
            pagingSourceFactory = {
                db.dao.pagingSource()
            }
        ).flow.map{ pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }

    // Search (Local Only)
    override fun searchPokemon(query: String): Flow<List<Pokemon>> {
        return db.dao.searchPokemon(query).map { list ->
            list.map { entity -> entity.toDomain() }
        }
    }
    override fun getFullPokemonDetails(id: Int): Flow<Pokemon?> {
        return flow {
            val localPokemon: PokemonEntity? = db.dao.getPokemonById(id)
            if (localPokemon != null && localPokemon.hp > 0) {
                emit(localPokemon.toDomain())
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
                    emit(entity.toDomain())
                } ?: emit(null)
            }
            catch(e:Exception){
                Timber.e(e, "Failed to fetch details")
                emit(null)
            }
        }.flowOn(Dispatchers.IO)
    }

    // One time sync - handled by WorkManager
    override suspend fun syncSearchIndex() {
        withContext(Dispatchers.IO) {
            val response =
                api.getPokemonByQuery(GraphQlQuery(PokeQueries.SEARCH_LIST_QUERY, emptyMap()))
            val namesOnly = response.data?.pokemon?.map {
                PokemonEntity(
                    id = it.id,
                    name = it.name,
                    imageSprite = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${it.id}.png"
                )
            } ?: emptyList()
            db.dao.insertSearchNames(namesOnly)
        }
    }
}