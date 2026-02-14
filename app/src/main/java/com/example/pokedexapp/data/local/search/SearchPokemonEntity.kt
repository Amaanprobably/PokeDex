package com.example.pokedexapp.data.local.search

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_table")
data class SearchPokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageSprite: String
)