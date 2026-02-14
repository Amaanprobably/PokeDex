package com.example.pokedexapp.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPokemon(pokemon: List<PokemonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: PokemonEntity)

    @Query("Select * from pokemon_table")
    fun pagingSource() : PagingSource<Int, PokemonEntity>

    @Query("Delete from pokemon_table")
    suspend fun clearAllPokemon()

    @Query("SELECT * FROM pokemon_table WHERE id = :id")
    suspend fun getPokemonById(id : Int): PokemonEntity?

    @Query("SELECT * FROM remote_keys WHERE pokemonId = :pokemonId")
    suspend fun getRemoteKeys(pokemonId: Int): RemoteKeys?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>)

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}