package com.example.pokedexapp.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.data.repository.PokemonRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModel(
    repository: PokemonRepository,
    context : Context
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _pokemonId = MutableStateFlow<Int?>(null)

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    init {
        viewModelScope.launch {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow("PokemonSearchSync")
                .map { it.firstOrNull()?.state }
                .filter { it == WorkInfo.State.SUCCEEDED }
                .collect {
                    refreshTrigger.emit(Unit)
                }
        }
    }

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
        .onStart { emit(UiState.Loading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
    val pokemonPagingFlow = repository.getPokemonList()
        .cachedIn(viewModelScope)

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<PokemonEntity>> =
        combine(
            searchQuery.debounce(100L),
            refreshTrigger.onStart { emit(Unit) }
        ) { query, _ -> query }
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    flowOf(emptyList())
                } else {
                    repository.searchPokemon(query)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun getPokemonDetails(id: Int) {
        _pokemonId.value = id
    }
    fun searchPokemon(newQuery: String) {
        _searchQuery.value = newQuery
    }
}