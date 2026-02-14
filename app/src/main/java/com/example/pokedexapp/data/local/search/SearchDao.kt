package com.example.pokedexapp.data.local.search

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchNames(searchNames: List<SearchPokemonEntity>)

    @Query("SELECT * FROM search_table WHERE name LIKE '%' || :query || '%'")
    fun searchPokemon(query: String): Flow<List<SearchPokemonEntity>>
}