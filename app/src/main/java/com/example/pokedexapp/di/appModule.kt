package com.example.pokedexapp.di

import androidx.paging.ExperimentalPagingApi
import androidx.room.Room
import com.example.pokedexapp.data.local.PokemonDatabase
import com.example.pokedexapp.data.remote.PokeApi
import com.example.pokedexapp.data.repository.PokemonRepository
import com.example.pokedexapp.presentation.PokemonViewModel
import org.koin.dsl.module
import retrofit2.Retrofit
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
        PokemonViewModel(get(),get())
    }
}
