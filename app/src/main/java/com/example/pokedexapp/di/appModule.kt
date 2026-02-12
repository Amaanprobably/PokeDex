package com.example.pokedexapp.di

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.Room
import com.example.pokedexapp.data.local.PokemonDao
import com.example.pokedexapp.data.local.PokemonDatabase
import com.example.pokedexapp.data.remote.PokeApi
import com.example.pokedexapp.data.remote.PokemonRemoteMediator
import com.example.pokedexapp.data.repository.PokemonRepository
import com.example.pokedexapp.presentation.PokemonViewModel
import com.example.pokedexapp.ui.navigation.Routes
import org.koin.dsl.module
import retrofit2.Retrofit
import kotlinx.serialization.json.Json
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalPagingApi::class)
val appModule = module {
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Shows full JSON response
        }
        val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

        Retrofit.Builder()
            .baseUrl("https://beta.pokeapi.co/v1beta2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApi::class.java)
    }
    single {
        Room.databaseBuilder(context = get(), klass =  PokemonDatabase::class.java, name = "pokemon.db").build()
    }
    single {
        get<PokemonDatabase>().dao
    }
    single {
        PokemonRepository(get(),get())
    }
    viewModel {
        PokemonViewModel(get())
    }
}
