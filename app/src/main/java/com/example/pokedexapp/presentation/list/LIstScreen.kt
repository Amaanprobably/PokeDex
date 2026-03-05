package com.example.pokedexapp.presentation.list

import android.content.Context
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pokedexapp.R
import com.example.pokedexapp.domain.model.Pokemon
import com.example.pokedexapp.presentation.components.ErrorScreen
import com.example.pokedexapp.presentation.components.LoadingScreen
import com.example.pokedexapp.presentation.components.toUserFriendlyMessage
import com.example.pokedexapp.presentation.theme.RobotoCondensed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.ListScreen(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    pokemonList: LazyPagingItems<Pokemon>,
    searchList: List<Pokemon>,
    onSearch: (String) -> Unit,
    onItemClick: (Int, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(R.drawable.ic_international_pok_mon_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                DockedSearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                onSearch(it)
                                active = (it.isNotBlank())
                            },
                            onSearch = {
                                active = false
                            },
                            expanded = active,
                            onExpandedChange = { },
                            placeholder = { Text("Search...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (active && searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            onSearch("")
                                            active = false
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear Search"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.padding(4.dp)
                        )
                    },
                    expanded = active,
                    onExpandedChange = { active = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    if (searchList.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.animateContentSize()
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            items(searchList, key = {
                                it.id
                            }) { pokemon ->
                                ListItem(
                                    headlineContent = { Text(pokemon.name) },
                                    leadingContent = {
                                        AsyncImage(
                                            model = pokemon.imageSprite,
                                            contentDescription = null,
                                            modifier = Modifier.size(50.dp).sharedElement(
                                                sharedContentState = rememberSharedContentState(key = "search-image-${pokemon.id}"),
                                                animatedVisibilityScope = animatedVisibilityScope,
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .clickable {
                                            onItemClick(
                                                pokemon.id,
                                                "search"
                                            )
                                        }
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                        .clip(shape = RoundedCornerShape(10.dp))

                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val refreshState = pokemonList.loadState.refresh
        val appendState = pokemonList.loadState.append
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LaunchedEffect(refreshState) {
                if (refreshState is LoadState.Error && pokemonList.itemCount > 0) {
                    val result = snackbarHostState.showSnackbar(
                        message = refreshState.error.toUserFriendlyMessage(),
                        actionLabel = "Retry",
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        pokemonList.retry()
                    }
                }
            }
            when (refreshState) {
                is LoadState.Loading if pokemonList.itemCount == 0 -> {
                    LoadingScreen(padding = PaddingValues(0.dp))
                }
                is LoadState.Error if pokemonList.itemCount == 0 -> {
                    ErrorScreen(
                        modifier = Modifier.background(color = Color.White),
                        message = refreshState.error.toUserFriendlyMessage()
                    ) {
                        pokemonList.retry()
                    }
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = refreshState is LoadState.Loading,
                        onRefresh = { pokemonList.refresh() }
                    ) {
                        LazyVerticalGrid(
                            modifier = modifier.fillMaxSize().background(Color.White),
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(
                                pokemonList.itemCount,
                                key = pokemonList.itemKey { it.id })
                            { index ->
                                val pokemon = pokemonList[index]
                                if (pokemon != null) {
                                    var isLoading by remember(pokemon.id) { mutableStateOf(true) }
                                    val animatedColor by animateColorAsState(
                                        targetValue = Color(pokemon.dominantColor),
                                        animationSpec = tween(durationMillis = 500),
                                        label = "color"
                                    )
                                    val imageRequest = ImageRequest.Builder(LocalContext.current)
                                        .data(pokemon.imageSprite)
                                        .memoryCacheKey("list-image-${pokemon.id}")
                                        .placeholderMemoryCacheKey("list-image-${pokemon.id}")
                                        .build()
                                    Card(
                                        modifier = Modifier
                                            .clip(
                                                shape = RoundedCornerShape(
                                                    16.dp
                                                )
                                            )
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        animatedColor,
                                                        MaterialTheme.colorScheme.surface
                                                    )
                                                )
                                            ).clickable {
                                                onItemClick(pokemon.id, "list")
                                            }.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .sharedBounds(
                                                    sharedContentState = rememberSharedContentState(
                                                        key = "bounds-${pokemon.id}"
                                                    ),
                                                    animatedVisibilityScope = animatedVisibilityScope,
                                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                                )
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isLoading) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.scale(0.5f),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                AsyncImage(
                                                    model = imageRequest,
                                                    contentDescription = pokemon.name,
                                                    //filterQuality = FilterQuality.Medium,
                                                    modifier = Modifier
                                                        .size(150.dp)
                                                        .sharedElement(
                                                            sharedContentState = rememberSharedContentState(
                                                                key = "list-image-${pokemon.id}"
                                                            ),
                                                            animatedVisibilityScope = animatedVisibilityScope,
                                                        ),
                                                    onSuccess = {
                                                        isLoading = false
                                                    }
                                                )
                                            }
                                            Text(
                                                text = pokemon.name.replaceFirstChar { it.uppercaseChar() },
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = RobotoCondensed,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                            when (appendState) {
                                is LoadState.Loading -> {
                                    item(span = { GridItemSpan(2) }) {
                                        Box(
                                            Modifier.fillMaxWidth().padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }

                                is LoadState.Error -> {
                                    item(span = { GridItemSpan(2) }) {
                                        ErrorScreen(message = appendState.error.toUserFriendlyMessage()) {
                                            pokemonList.retry()
                                        }
                                    }
                                }

                                is LoadState.NotLoading -> {
                                    if (appendState.endOfPaginationReached && pokemonList.itemCount > 0) {
                                        item(span = { GridItemSpan(2) }) {
                                            Text(
                                                "You've caught them all!!!",
                                                Modifier.padding(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}