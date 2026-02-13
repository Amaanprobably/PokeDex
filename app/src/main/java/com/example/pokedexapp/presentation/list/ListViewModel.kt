package com.example.pokedexapp.presentation.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.work.WorkInfo
import androidx.work.WorkManager
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModel(
    repository: PokemonRepository,
    context : Context
) : ViewModel() {
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
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val pokemonPagingFlow = repository.getPokemonList()
        .cachedIn(viewModelScope)

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<Pokemon>> =
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

    fun searchPokemon(newQuery: String) {
        _searchQuery.value = newQuery
    }
}