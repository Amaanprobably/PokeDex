package com.example.pokedexapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PokemonEntity::class, SearchPokemonEntity::class, RemoteKeys::class],
    version = 4,
    exportSchema = true)
abstract class PokemonDatabase: RoomDatabase() {
    abstract val dao: PokemonDao
}