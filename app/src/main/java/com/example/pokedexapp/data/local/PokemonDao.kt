package com.example.pokedexapp.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPokemon(pokemon: List<PokemonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: PokemonEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSearchNames(pokemon: List<PokemonEntity>)

    @Query("Select * from pokemon_table")
    fun pagingSource() : PagingSource<Int, PokemonEntity>

    @Query("Delete from pokemon_table")
    suspend fun clearAll()

    @Query("Select * from pokemon_table where name like '%' || :query || '%'")
    fun searchPokemon(query: String) : Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemon_table WHERE id = :id")
    suspend fun getPokemonById(id : Int): PokemonEntity?

    @Query("SELECT * FROM pokemon_table WHERE name IN (:names)")
    fun getPokemonListByNames(names: List<String>): Flow<List<PokemonEntity>>
}