package com.example.pokedexapp.data.remote.responses

data class GraphQlQuery(val query: String, val variables: Map<String, Any>)

data class GraphQlResponse(val data: GraphQlData?)

data class GraphQlData(

    val pokemon: List<PokemonGql>

)


data class PokemonGql(

    val id: Int,

    val name: String,

    val height: Int?,

    val sprites: Any?,

    val weight: Int?,

    val types: List<TypeWrapper>,

    val stats: List<StatWrapper>

)


data class TypeWrapper(val type: TypeName)

data class TypeName(val name: String)


data class StatWrapper(val base_stat: Int, val stat: StatName)

data class StatName(val name: String)