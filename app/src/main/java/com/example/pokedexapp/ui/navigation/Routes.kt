package com.example.pokedexapp.ui.navigation

enum class Routes(val route: String) {
    POKEMON_LIST_SCREEN("pokemon_list_screen"),
    POKEMON_DETAIL_SCREEN("pokemon_detail_screen");

    fun withArgs(vararg args: Any): String {
        return buildString {
            append(route)
            args.forEach { arg -> append("/$arg") }
        }
    }
}