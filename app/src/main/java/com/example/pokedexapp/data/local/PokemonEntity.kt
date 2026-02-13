package com.example.pokedexapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_table")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val url: String = "",
    val imageSprite: String = "",
    val types: String = "normal",
    val hp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val specialAttack: Int = 0,
    val specialDefense: Int = 0,
    val speed: Int = 0,
    val height: Int = 0,
    val weight: Int = 0,
    val dominantColor: Int = 0xFFA8A878.toInt()
)