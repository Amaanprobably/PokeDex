package com.example.pokedexapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_table")
data class SearchPokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageSprite: String
)