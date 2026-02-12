package com.example.pokedexapp.data.remote

object PokeQueries {
    val POKEMON_LIST_QUERY = """
        query GetPokemonList(${"$"}limit: Int, ${"$"}offset: Int) {
          pokemon: pokemon_v2_pokemon(limit: ${"$"}limit, offset: ${"$"}offset) { 
            id
            name
            height
            weight
            sprites: pokemon_v2_pokemonsprites {
                sprites
            }
            types: pokemon_v2_pokemontypes { 
              type: pokemon_v2_type { name }
            }
            stats: pokemon_v2_pokemonstats { 
              base_stat
              stat: pokemon_v2_stat { name }
            }
          }
        }
    """.trimIndent()

    // 2. SEARCH QUERY
    val SEARCH_LIST_QUERY = """
        query GetSearchNames {
          pokemon: pokemon_v2_pokemon (where:{is_default: {_eq: true}}){
            id
            name
          }
        }
    """.trimIndent()

    // 3. DETAIL QUERY
    val POKEMON_DETAILS_QUERY = """
        query GetPokemonDetails(${"$"}id: Int!) {
          pokemon: pokemon_v2_pokemon(where: {id: {_eq: ${"$"}id}}) {
            id
            name
            height
            weight
            sprites: pokemon_v2_pokemonsprites {
                sprites
            }
            types: pokemon_v2_pokemontypes {
              type: pokemon_v2_type { name }
            }
            stats: pokemon_v2_pokemonstats {
              base_stat
              stat: pokemon_v2_stat { name }
            }
          }
        }
    """.trimIndent()
}