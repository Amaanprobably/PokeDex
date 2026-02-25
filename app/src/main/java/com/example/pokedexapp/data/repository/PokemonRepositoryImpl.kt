package com.example.pokedexapp.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.pokedexapp.data.local.PokemonDatabase
import com.example.pokedexapp.domain.model.Pokemon
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.data.local.SearchPokemonEntity
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
@OptIn(ExperimentalPagingApi::class)
class PokemonRepositoryImpl(
    val db: PokemonDatabase,
    val api: PokeApi
) : PokemonRepository {
    // The Main Feed (With API Sync)
    override fun getPokemonList(): Flow<PagingData<Pokemon>> {
        return Pager(
            config = PagingConfig(pageSize = 40, initialLoadSize = 40, prefetchDistance = 20),
            remoteMediator = PokemonRemoteMediator(api,db),
            pagingSourceFactory = {
                db.dao.pagingSource()
            }
        ).flow.map{ pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }

    // Search (Local Only - SearchDatabase)
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
                Log.e("PokemonDetails","Unknown error occured",e)
                emit(null)
            }
        }.flowOn(Dispatchers.IO)
    }

    //Sync to SearchDatabase
    override suspend fun syncSearchIndex() {
        withContext(Dispatchers.IO) {
            try {
                val response = api.getAllPokemon(limit = 2000, offset = 0)

                val allPokemonEntities = response.results.map { result ->

                    val id = result.url.trimEnd('/').substringAfterLast('/').toInt()

                    SearchPokemonEntity(
                        id = id,
                        name = result.name,
                        imageSprite = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${id}.png"
                    )
                }
                db.dao.insertSearchNames(allPokemonEntities)
            } catch (e: Exception) {
                Log.e("PokedexSync","Crash in Sync Worker",e)
            }
        }
    }
}