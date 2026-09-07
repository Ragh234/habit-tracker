package com.example.habittracker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habittracker.ui.detail.DetailScreen
import com.example.habittracker.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val HABIT_DETAIL = "habit/{habitId}"

    fun habitDetail(habitId: Long): String = "habit/$habitId"
}

@Composable
fun HabitTrackerNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onHabitClick = { habitId ->
                    navController.navigate(Routes.habitDetail(habitId))
                }
            )
        }
        composable(
            route = Routes.HABIT_DETAIL,
            // Declaring the type means the argument arrives in SavedStateHandle as a Long
            // rather than as a String the ViewModel has to parse and validate.
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) {
            DetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
