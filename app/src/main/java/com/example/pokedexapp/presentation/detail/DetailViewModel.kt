package com.example.pokedexapp.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedexapp.domain.model.Pokemon
import com.example.pokedexapp.domain.repository.PokemonRepository
import com.example.pokedexapp.presentation.components.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModel(
    repository: PokemonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pokemonId: Int = checkNotNull(savedStateHandle["id"])
    private val _retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<UiState<Pokemon>> = _retryTrigger
        .flatMapLatest {
            repository.getFullPokemonDetails(pokemonId)
        }
        .map { pokemon ->
            when {
                pokemon != null && pokemon.hp > 0 -> UiState.Success(pokemon)
                pokemon == null -> UiState.Error("Failed to load details")
                else -> UiState.Loading
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun retry() {
        _retryTrigger.value += 1
    }
}