package com.example.pokedexapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.pokedexapp.presentation.detail.DetailScreen
import com.example.pokedexapp.presentation.detail.DetailViewModel
import com.example.pokedexapp.presentation.list.ListScreen
import com.example.pokedexapp.presentation.list.ListViewModel
import com.example.pokedexapp.presentation.navigation.Routes
import com.example.pokedexapp.presentation.theme.PokeDexAppTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeDexAppTheme {
                SharedTransitionLayout {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Routes.POKEMON_LIST_SCREEN.route
                    )
                    {
                        composable(Routes.POKEMON_LIST_SCREEN.route) {

                            val viewModel: ListViewModel = koinViewModel()
                            val pokemonList = viewModel.pokemonPagingFlow.collectAsLazyPagingItems()
                            val searchList by viewModel.searchResults.collectAsStateWithLifecycle(
                                initialValue = emptyList()
                            )
                            ListScreen(
                                modifier = Modifier.padding(),
                                animatedVisibilityScope = this,
                                onItemClick = { id, origin ->
                                    val currentState =
                                        navController.currentBackStackEntry?.lifecycle?.currentState
                                    if (currentState == Lifecycle.State.RESUMED) {
                                        navController.navigate(
                                            Routes.POKEMON_DETAIL_SCREEN.withArgs(id, origin)
                                        ) {
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                pokemonList = pokemonList,
                                onSearch = { query ->
                                    viewModel.searchPokemon(query)
                                },
                                searchList = searchList
                            )
                        }
                        composable(
                            route = Routes.POKEMON_DETAIL_SCREEN.withArgs(
                                "{id}", "{origin}"
                            ),
                            arguments = listOf(
                                navArgument(name = "id") {
                                    type = NavType.IntType
                                },
                                navArgument(name = "origin") {
                                    type = NavType.StringType
                                }
                            )
                        )
                        { backStackEntry ->
                            val viewModel: DetailViewModel = koinViewModel()
                            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                            val id = remember {
                                backStackEntry.arguments?.getInt("id") ?: 0
                            }
                            val origin = remember {
                                backStackEntry.arguments?.getString("origin") ?: "list"
                            }
                            DetailScreen(
                                pokemonId = id,
                                animatedVisibilityScope = this,
                                state = uiState,
                                origin = origin,
                                onBack = { navController.popBackStack() }
                            ) {
                                viewModel.retry()
                            }
                        }
                    }
                }
            }
        }
    }
}