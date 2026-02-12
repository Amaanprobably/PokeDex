package com.example.pokedexapp.data.mappers

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.data.remote.PokemonGql
import com.example.pokedexapp.data.remote.Sprites
import com.example.pokedexapp.data.remote.responses.Pokemon
import com.example.pokedexapp.ui.theme.Bug
import com.example.pokedexapp.ui.theme.Dark
import com.example.pokedexapp.ui.theme.Dragon
import com.example.pokedexapp.ui.theme.Electric
import com.example.pokedexapp.ui.theme.Fairy
import com.example.pokedexapp.ui.theme.Fire
import com.example.pokedexapp.ui.theme.Flying
import com.example.pokedexapp.ui.theme.Ghost
import com.example.pokedexapp.ui.theme.Grass
import com.example.pokedexapp.ui.theme.Ground
import com.example.pokedexapp.ui.theme.Ice
import com.example.pokedexapp.ui.theme.Normal
import com.example.pokedexapp.ui.theme.Poison
import com.example.pokedexapp.ui.theme.Psychic
import com.example.pokedexapp.ui.theme.Rock
import com.example.pokedexapp.ui.theme.Steel
import com.example.pokedexapp.ui.theme.Water
import com.google.gson.Gson
import timber.log.Timber

/* Previous Retrofit implementation
fun Pokemon.toPokemonEntity(url: String): PokemonEntity {
    return PokemonEntity(
        // Simple fields
        id = this.id,
        name = this.name,
        url = url,
        // Flattening the Image
        imageSprite = this.sprites.front_default, // Fallback if null
        height = this.height,
        weight = this.weight,
        // Flattening the Types (List -> String)
        // transforms ["fire", "flying"] into "fire,flying"
        types = this.types.joinToString(",") { it.type.name },
        hp = this.stats.find { it.stat.name == "hp" }?.base_stat ?: 0,
        attack = this.stats.find { it.stat.name == "attack" }?.base_stat ?: 0,
        defense = this.stats.find { it.stat.name == "defense" }?.base_stat ?: 0,
        speed = this.stats.find { it.stat.name == "speed" }?.base_stat ?: 0
    )
}
*/
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
    // Grab the high-res official artwork URL or use a default (remove after testing)
    val highResUrl = spritesDto?.other?.officialArtwork?.front_default ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${this.id}.png"
    return PokemonEntity(
        id = this.id,
        name = this.name,
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