package com.example.pokedexapp.data.remote

object PokeQueries {
    val POKEMON_LIST_QUERY = """
        query GetPokemonList(${"$"}limit: Int, ${"$"}offset: Int) {
          pokemon: pokemon(limit: ${"$"}limit, offset: ${"$"}offset) { 
            id
            name
            height
            weight
            sprites: pokemonsprites {
                sprites
            }
            types: pokemontypes { 
              type: type { name }
            }
            stats: pokemonstats { 
              base_stat
              stat: stat { name }
            }
          }
        }
    """.trimIndent()
    /* - Moved to the REST Endpoint for names instead
    // 2. SEARCH QUERY
    val SEARCH_LIST_QUERY = """
        query GetSearchNames {
          pokemon: pokemon(
            where: {is_default: {_eq: true}}, 
            limit: 2000
          ) {
            id
            name
          }
        }
    """.trimIndent()
    */
    // 3. DETAIL QUERY
    val POKEMON_DETAILS_QUERY = """
        query GetPokemonDetails(${"$"}id: Int!) {
          pokemon: pokemon(where: {id: {_eq: ${"$"}id}}) {
            id
            name
            height
            weight
            sprites: pokemonsprites {
                sprites
            }
            types: pokemontypes {
              type: type { name }
            }
            stats: pokemonstats {
              base_stat
              stat: stat { name }
            }
          }
        }
    """.trimIndent()
}