package com.example.pokedexapp.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.pokedexapp.domain.model.Pokemon
import com.example.pokedexapp.domain.repository.PokemonRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModel(
    repository: PokemonRepository
) : ViewModel() {
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val pokemonPagingFlow = repository.getPokemonList()
        .cachedIn(viewModelScope)

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<Pokemon>> =
        combine(
            searchQuery.debounce(300L),
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

    fun searchPokemon(newQuery: String) {
        _searchQuery.value = newQuery
    }
    fun refresh(){
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }
}