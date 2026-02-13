package com.example.pokedexapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Pokemon(
    val id: Int,
    val name: String,
    val url:String,
    val imageSprite: String,
    val types: List<String>,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int,
    val height: Double,
    val weight: Double,
    val dominantColor: Int
)