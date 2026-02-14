package com.example.pokedexapp.data.local.search

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SearchPokemonEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SearchDatabase : RoomDatabase() {
    abstract val dao: SearchDao
}