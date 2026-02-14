package com.example.pokedexapp.data.remote

import com.example.pokedexapp.data.remote.responses.GraphQlQuery
import com.example.pokedexapp.data.remote.responses.GraphQlResponse
import com.example.pokedexapp.data.remote.responses.PokemonListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PokeApi {

    @POST("https://graphql.pokeapi.co/v1beta2")
    suspend fun getPokemonByQuery(@Body query: GraphQlQuery): GraphQlResponse

    // REST endpoint for the search list (as GraphQL has a limit)
    @GET("https://pokeapi.co/api/v2/pokemon")
    suspend fun getAllPokemon(
        @Query("limit") limit: Int = 2000,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse
}