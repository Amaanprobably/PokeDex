package com.example.pokedexapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val pokemonId: Int,
    val prevKey:Int?,
    val nextKey:Int?
)
