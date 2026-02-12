package com.example.pokedexapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.pokedexapp.presentation.DetailScreen
import com.example.pokedexapp.presentation.ListScreen
import com.example.pokedexapp.presentation.PokemonViewModel
import com.example.pokedexapp.ui.navigation.Routes
import com.example.pokedexapp.ui.theme.PokeDexAppTheme
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeDexAppTheme {
                SharedTransitionLayout {
                    val navController = rememberNavController()
                    val viewModel: PokemonViewModel = koinViewModel()
                    val pokemonList = viewModel.pokemonPagingFlow.collectAsLazyPagingItems()
                    val searchList by viewModel.searchResults.collectAsStateWithLifecycle(
                        initialValue = emptyList()
                    )
                    NavHost(
                       navController = navController,
                       startDestination = Routes.POKEMON_LIST_SCREEN.route
                    )
                    {
                        composable(Routes.POKEMON_LIST_SCREEN.route) {
                            ListScreen(
                                modifier = Modifier.padding(),
                                animatedVisibilityScope = this,
                                onItemClick = { id, origin ->
                                    viewModel.getPokemonDetails(id)
                                    navController.navigate(
                                        Routes.POKEMON_DETAIL_SCREEN.withArgs(id,origin)
                                    )
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
                                "{id}","{origin}"
                            ),
                            arguments = listOf(
                                navArgument(name = "id"){
                                    type = NavType.IntType
                                },
                                navArgument(name = "origin"){
                                    type = NavType.StringType
                                }
                            )
                        )
                        {  backStackEntry ->
                            val uiState by viewModel.uiState.collectAsState()
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
                            ){
                                navController.popBackStack()
                                viewModel.getPokemonDetails(id)
                            }
                        }
                    }
                }
            }
        }
    }
}
