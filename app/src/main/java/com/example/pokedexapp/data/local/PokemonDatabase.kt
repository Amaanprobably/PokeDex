package com.example.pokedexapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PokemonEntity::class, RemoteKeys::class],
    version = 3,
    exportSchema = true)
abstract class PokemonDatabase: RoomDatabase() {
    abstract val dao: PokemonDao
}