package com.example.pokedexapp.data.remote

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.pokedexapp.data.local.PokemonDatabase
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.data.local.RemoteKeys
import com.example.pokedexapp.data.mappers.toEntity
import com.example.pokedexapp.data.remote.PokeQueries.POKEMON_LIST_QUERY
import com.example.pokedexapp.data.remote.responses.GraphQlQuery
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
        Log.d("PokedexSync","MEDIATOR FIRED: $loadType")
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextKey?.minus(40) ?: 0
                }
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevKey = remoteKeys?.prevKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    prevKey
                }
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextKey
                }
            }
            val query = POKEMON_LIST_QUERY
            val response = pokeApi.getPokemonByQuery(
                GraphQlQuery(
                    query = query,
                    variables = mapOf("limit" to state.config.pageSize, "offset" to page)
                )
            )
            val rawData = response.data?.pokemon ?: emptyList()
            val endOfPaginationReached = rawData.isEmpty()

            val entities = rawData.map { gql ->
                gql.toEntity()
            }
            pokeDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    pokeDb.dao.clearRemoteKeys()
                    pokeDb.dao.clearAllPokemon()
                }
                val prevKey = if (page == 0) null else page - 40
                val nextKey = if (endOfPaginationReached) null else page + 40

                val keys = rawData.map {
                    RemoteKeys(pokemonId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                pokeDb.dao.insertAllRemoteKeys(keys)
                pokeDb.dao.insertAllPokemon(entities)
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        }
        catch (e: HttpException){
            MediatorResult.Error(e)
        }
        catch (e: IOException){
            MediatorResult.Error(e)
        }
    }
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PokemonEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { pokemon ->
                pokeDb.dao.getRemoteKeys(pokemon.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, PokemonEntity>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { pokemon ->
                pokeDb.dao.getRemoteKeys(pokemon.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, PokemonEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                pokeDb.dao.getRemoteKeys(repoId)
            }
        }
    }
}