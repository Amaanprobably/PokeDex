package com.example.pokedexapp.domain.repository

import androidx.paging.PagingData
import com.example.pokedexapp.domain.model.Pokemon
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    fun getPokemonList(): Flow<PagingData<Pokemon>>
    fun searchPokemon(query: String): Flow<List<Pokemon>>
    fun getFullPokemonDetails(id: Int): Flow<Pokemon?>
    suspend fun syncSearchIndex()
}