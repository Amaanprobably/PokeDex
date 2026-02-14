package com.example.pokedexapp.data.mappers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.data.local.search.SearchPokemonEntity
import com.example.pokedexapp.data.remote.responses.PokemonGql
import com.example.pokedexapp.data.remote.responses.Sprites
import com.example.pokedexapp.domain.model.Pokemon
import com.example.pokedexapp.presentation.theme.Bug
import com.example.pokedexapp.presentation.theme.Dark
import com.example.pokedexapp.presentation.theme.Dragon
import com.example.pokedexapp.presentation.theme.Electric
import com.example.pokedexapp.presentation.theme.Fairy
import com.example.pokedexapp.presentation.theme.Fire
import com.example.pokedexapp.presentation.theme.Flying
import com.example.pokedexapp.presentation.theme.Ghost
import com.example.pokedexapp.presentation.theme.Grass
import com.example.pokedexapp.presentation.theme.Ground
import com.example.pokedexapp.presentation.theme.Ice
import com.example.pokedexapp.presentation.theme.Normal
import com.example.pokedexapp.presentation.theme.Poison
import com.example.pokedexapp.presentation.theme.Psychic
import com.example.pokedexapp.presentation.theme.Rock
import com.example.pokedexapp.presentation.theme.Steel
import com.example.pokedexapp.presentation.theme.Water
import com.google.gson.Gson
import timber.log.Timber
fun PokemonGql.toEntity(): PokemonEntity {
    val typeList = this.types.map { it.type.name }
    val primaryType = typeList.firstOrNull() ?: "normal"
    val typeColor = getPokemonColor(primaryType).toArgb()
    val spritesDto = try {
        Gson().fromJson(this.sprites.toString(), Sprites::class.java)
    } catch (e: Exception){
        Timber.tag("SpriteFetching").d(e.localizedMessage)
        null
    }
    val highResUrl = spritesDto?.other?.officialArtwork?.front_default ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${this.id}.png"
    return PokemonEntity(
        id = this.id,
        name = this.name,
        //remove the url in future update
        url = "https://pokeapi.co/api/v2/pokemon/${this.id}/",
        imageSprite = highResUrl,
        dominantColor = typeColor,
        types = typeList.joinToString(","),
        hp = this.getStat("hp"),
        attack = this.getStat("attack"),
        defense = this.getStat("defense"),
        speed = this.getStat("speed"),
        specialAttack = this.getStat("special-attack"),
        specialDefense = this.getStat("special-defense"),
        height = this.height ?: 0,
        weight = this.weight ?: 0
    )
}
fun PokemonEntity.toDomain(): Pokemon {
    return Pokemon(
        id = id,
        name = name,
        url = url,
        imageSprite = imageSprite,
        types = types.split(",").map { it.trim() },
        hp = hp,
        attack = attack,
        defense = defense,
        speed = speed,
        height = height.toDouble() / 10,
        weight = weight.toDouble() / 10,
        specialAttack = specialAttack,
        specialDefense = specialDefense,
        dominantColor = dominantColor
    )
}
fun SearchPokemonEntity.toDomain(): Pokemon {
    return Pokemon(
        id = this.id,
        name = this.name,
        imageSprite = this.imageSprite,
        url = "",
        types = emptyList(),
        hp = 0,
        attack = 0,
        defense = 0,
        specialAttack = 0,
        specialDefense = 0,
        speed = 0,
        height = 0.0,
        weight = 0.0,
        dominantColor = 0
    )
}
fun PokemonGql.getStat(statName: String): Int {
    return stats.find { it.stat.name == statName }?.base_stat ?: 0
}
fun getPokemonColor(type: String): Color {
    return when (type.lowercase()) {
        "fire" -> Fire
        "water" -> Water
        "grass" -> Grass
        "electric" -> Electric
        "poison" -> Poison
        "psychic" -> Psychic
        "ice" -> Ice
        "ground" -> Ground
        "flying" -> Flying
        "bug" -> Bug
        "rock" -> Rock
        "ghost" -> Ghost
        "dragon" -> Dragon
        "dark" -> Dark
        "steel" -> Steel
        "fairy" -> Fairy
        else -> Normal
    }
}