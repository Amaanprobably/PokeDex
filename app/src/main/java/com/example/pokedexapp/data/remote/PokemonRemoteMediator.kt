package com.example.pokedexapp.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.pokedexapp.data.local.PokemonDatabase
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.data.mappers.toEntity
import com.example.pokedexapp.data.remote.PokeQueries.POKEMON_LIST_QUERY
import okio.IOException
import retrofit2.HttpException
import kotlin.collections.emptyList

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    val pokeApi: PokeApi,
    val pokeDb : PokemonDatabase
): RemoteMediator<Int, PokemonEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) return MediatorResult.Success(endOfPaginationReached = true)
                    else state.pages.sumOf { it.data.size }
                }
            }
            val query = POKEMON_LIST_QUERY
            val response = pokeApi.getPokemonByQuery(
                GraphQlQuery(
                    query = query,
                    variables = mapOf("limit" to state.config.pageSize, "offset" to offset)
                )
            )
            val rawData = response.data?.pokemon ?: emptyList()

            val entities = rawData.map { gql ->
                gql.toEntity()
            }
            pokeDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    pokeDb.dao.clearAll()
                }
                pokeDb.dao.insertAllPokemon(entities)
            }
            MediatorResult.Success(endOfPaginationReached = rawData.isEmpty())
        }
        catch (e: HttpException){
            MediatorResult.Error(e)
        }
        catch (e: IOException){
            MediatorResult.Error(e)
        }
    }
}
