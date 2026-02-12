package com.example.pokedexapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.data.repository.PokemonRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModel(
    repository: PokemonRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _pokemonId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<UiState> = _pokemonId
        .filterNotNull()
        .flatMapLatest { id ->
            repository.getFullPokemonDetails(id)
        }
        .map { pokemon ->
            when {
                pokemon != null && pokemon.hp > 0 -> UiState.Success(pokemon)
                pokemon == null -> UiState.Error("Failed to load details") // Handle null as Error
                else -> UiState.Loading
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
    val pokemonPagingFlow = repository.getPokemonList()
        .cachedIn(viewModelScope)

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<PokemonEntity>> = searchQuery
        .debounce(100L)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchPokemon(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getPokemonDetails(id: Int) {
        _pokemonId.value = id
    }
    fun searchPokemon(newQuery: String) {
        _searchQuery.value = newQuery
    }
}