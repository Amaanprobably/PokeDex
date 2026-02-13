package com.example.pokedexapp.data.remote

import com.example.pokedexapp.data.remote.responses.GraphQlQuery
import com.example.pokedexapp.data.remote.responses.GraphQlResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PokeApi {

    @POST("https://beta.pokeapi.co/graphql/v1beta/")
    suspend fun getPokemonByQuery(@Body query: GraphQlQuery): GraphQlResponse
}