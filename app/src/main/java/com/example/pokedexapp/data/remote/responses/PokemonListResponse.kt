package com.example.pokedexapp.data.remote.responses

data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonListResult>
)

data class PokemonListResult(
    val name: String,
    val url: String
)