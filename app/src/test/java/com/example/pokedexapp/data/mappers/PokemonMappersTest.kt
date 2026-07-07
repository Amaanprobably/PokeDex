package mappers

import com.example.pokedexapp.data.mappers.toEntity
import com.example.pokedexapp.data.remote.responses.PokemonGql
import com.example.pokedexapp.data.remote.responses.StatName
import com.example.pokedexapp.data.remote.responses.StatWrapper
import com.example.pokedexapp.data.remote.responses.TypeName
import com.example.pokedexapp.data.remote.responses.TypeWrapper
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonMappersTest{
    private fun buildGqlPokemon(
        id: Int = 1,
        name: String = "bulbasaur",
        height: Int? = 7,
        weight: Int? = 69,
        sprites: Any? = null,
        types: List<TypeWrapper> = listOf(TypeWrapper(TypeName("grass"))),
        stats: List<StatWrapper> = listOf(
            StatWrapper(45, StatName("hp")),
            StatWrapper(49, StatName("attack")),
            StatWrapper(49, StatName("defense")),
            StatWrapper(45, StatName("speed")),
            StatWrapper(65, StatName("special-attack")),
            StatWrapper(65, StatName("special-defense"))
        )
    ) = PokemonGql(id, name, height, sprites, weight, types, stats)

    @Test
    fun `toEntity falls back to github url when sprites is null`() {
        val entity = buildGqlPokemon(id = 6, sprites = null).toEntity()
        assertEquals(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/6.png",
            entity.imageSprite
        )
    }

    @Test
    fun `toEntity should extract official artwork url when sprites data is present`() {
        val spritesJson = """
        {"other":{"official-artwork":{"front_default":"https://real-artwork.com/6.png"}}}
    """.trimIndent()
        val spritesAsParsedByGson = Gson().fromJson(spritesJson, Any::class.java)

        val entity = buildGqlPokemon(id = 6, sprites = spritesAsParsedByGson).toEntity()
        assertEquals("https://real-artwork.com/6.png", entity.imageSprite)
    }
}