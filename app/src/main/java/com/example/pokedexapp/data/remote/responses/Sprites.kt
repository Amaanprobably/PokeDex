package com.example.pokedexapp.data.remote.responses

import com.google.gson.annotations.SerializedName

data class Sprites(

    val other: Other

)


data class Other(

    @SerializedName("official-artwork")

    val officialArtwork: OfficialArtwork

)


data class OfficialArtwork(

    val front_default: String,

    val front_shiny: String?

)