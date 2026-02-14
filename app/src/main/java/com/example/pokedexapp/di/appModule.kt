package com.example.pokedexapp.di

import androidx.paging.ExperimentalPagingApi
import androidx.room.Room
import com.example.pokedexapp.data.local.PokemonDatabase
import com.example.pokedexapp.data.local.search.SearchDatabase
import com.example.pokedexapp.data.remote.PokeApi
import com.example.pokedexapp.data.repository.PokemonRepositoryImpl
import com.example.pokedexapp.domain.repository.PokemonRepository
import com.example.pokedexapp.presentation.detail.DetailViewModel
import com.example.pokedexapp.presentation.list.ListViewModel
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
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

        Retrofit.Builder()
            .baseUrl("https://graphql.pokeapi.co/v1beta2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApi::class.java)
    }
    single {
        Room.databaseBuilder(context = get(), klass =  PokemonDatabase::class.java, name = "pokemon.db").fallbackToDestructiveMigration(true).build()
    }
    single {
        Room.databaseBuilder(context = get(), klass =  SearchDatabase::class.java, name = "search.db").fallbackToDestructiveMigration(true).build()
    }
    single {
        get<PokemonDatabase>().dao
    }
    single {
        get<SearchDatabase>().dao
    }
    single<PokemonRepository> {
        PokemonRepositoryImpl(get(),get(),get())
    }
    viewModel {
        ListViewModel(get())
    }
    viewModel {
        DetailViewModel(get(),get())
    }
}
