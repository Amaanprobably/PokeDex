package com.example.pokedexapp.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pokedexapp.data.local.PokemonEntity
import com.example.pokedexapp.ui.theme.RobotoCondensed
import com.example.pokedexapp.R.drawable
@Composable
fun SharedTransitionScope.DetailScreen(
    pokemonId: Int,
    animatedVisibilityScope: AnimatedVisibilityScope,
    state: UiState,
    origin: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    var pokemon: PokemonEntity? by remember { mutableStateOf(null) }

    when (state) {
        is UiState.Success -> pokemon = state.pokemon
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(state.message) { onRetry() }
    }

    if (pokemon != null) {
        val animatedColor by animateColorAsState(
            targetValue = Color(pokemon!!.dominantColor),
            animationSpec = tween(durationMillis = 500),
            label = "color"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "bounds-${pokemonId}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            animatedColor,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 60.dp, start = 16.dp)
                    .size(36.dp)
                    .clickable {
                        onBack()
                    }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp, top = 90.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 24.dp)
                ) {
                    AsyncImage(
                        model = pokemon?.imageSprite,
                        contentDescription = pokemon?.name,
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.CenterHorizontally)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "${origin}-image-${pokemonId}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .offset(y = (-90).dp)
                            .renderInSharedTransitionScopeOverlay { animatedVisibilityScope.transition.targetState == EnterExitState.Visible }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-90).dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "#${pokemonId} ${pokemon!!.name.replaceFirstChar { it.uppercaseChar() }}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val typesList = pokemon!!.types.split(",").map { it.trim() }
                            if (typesList.size > 1) {
                                TypeBadge(
                                    color = Color(pokemon!!.dominantColor),
                                    text = typesList[0]
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                TypeBadge(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    text = typesList[1]
                                )
                            } else {
                                TypeBadge(
                                    color = Color(pokemon!!.dominantColor),
                                    text = "Normal"
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                TypeBadge(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    text = typesList[0]
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        AttributeIcons(height = pokemon!!.height, weight = pokemon!!.weight)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Base Stats",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        StatBar(name = "HP", value = pokemon!!.hp, color = Color.Green)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatBar(name = "ATK", value = pokemon!!.attack, color = Color.Red)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatBar(name = "DEF", value = pokemon!!.defense, color = Color.Yellow)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatBar(name = "SPD", value = pokemon!!.speed, color = Color.Blue)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatBar(name = "SP-ATK", value = pokemon!!.specialAttack, color = Color.Magenta)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatBar(name = "SP-DEF", value = pokemon!!.specialDefense, color = Color(0xFF673AB7))

                    }
                }
            }
        }
    }
}
@Composable
fun TypeBadge(color: Color, text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text.replaceFirstChar { it.uppercaseChar() },
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
    }
}
@Composable
fun AttributeIcons(height: Int, weight: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(id = drawable.ic_weight),
                contentDescription = "weight",
                modifier = Modifier.size(36.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${weight.toDouble() / 10} kg",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = RobotoCondensed,
                color = Color.Black
            )
            Text(
                text = "Weight",
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = RobotoCondensed
            )
        }

        VerticalDivider(
            modifier = Modifier
                .height(50.dp)
                .alpha(0.5f),
            thickness = 2.dp,
            color = Color.LightGray
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(id = drawable.ic_height),
                contentDescription = "height",
                modifier = Modifier.size(36.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${height.toDouble() / 10} m",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = RobotoCondensed,
                color = Color.Black
            )
            Text(
                text = "Height",
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = RobotoCondensed
            )
        }
    }
}
@Composable
fun StatBar(name: String, value: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            modifier = Modifier.width(60.dp),
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 14.sp
        )
        LinearProgressIndicator(
            progress = { (value / 190f).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = color.copy(alpha = 0.6f),
            trackColor = Color.LightGray.copy(alpha = 0.2f),
        )
        Text(
            text = "$value",
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            fontSize = 14.sp
        )
    }
}