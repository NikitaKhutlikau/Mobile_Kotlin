package com.example.mobile_kotlin.ui.navigation

import LoginScreen
import RegisterScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.mobile_kotlin.ui.main.ActorDetailScreen
import com.example.mobile_kotlin.ui.main.ActorsScreen
import com.example.mobile_kotlin.ui.main.FavoritesScreen
import com.example.mobile_kotlin.ui.main.ProfileScreen
import com.example.mobile_kotlin.viewmodels.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Навигация при изменении статуса авторизации
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(Destinations.MAIN) {
                popUpTo(0) // Очищаем back stack полностью
                launchSingleTop = true
            }
        } else {
            navController.navigate(Destinations.AUTH) {
                popUpTo(0) // Очищаем back stack полностью
                launchSingleTop = true
            }
        }
    }


    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Destinations.MAIN else Destinations.AUTH
    ) {
        // Группа авторизации
        navigation(startDestination = Destinations.LOGIN, route = Destinations.AUTH) {
            composable(Destinations.LOGIN) { backStackEntry ->
                LoginScreen(
                    onLoginSuccess = {
                        //navController.navigate(Destinations.MAIN)
                    },
                    onRegisterClick = {
                        navController.popBackStack()
                        navController.navigate(Destinations.REGISTER)
                    },
                    authViewModel
                )
            }
            composable(Destinations.REGISTER) { backStackEntry ->

                RegisterScreen(
                    onRegisterSuccess = {
                        /*navController.navigate(Destinations.MAIN) {
                            popUpTo(Destinations.REGISTER) { inclusive = true }
                            launchSingleTop = true
                        }*/
                    },
                    onLoginClick = {
                        navController.popBackStack()
                        navController.navigate(Destinations.LOGIN)
                    },
                    authViewModel
                )
            }
        }

        navigation(startDestination = Destinations.ACTORS, route = Destinations.MAIN) {
            composable(Destinations.ACTORS) {
                AuthCheck(
                    navController = navController,
                    content = { ActorsScreen(navController) },
                    authViewModel = authViewModel
                )
            }
            composable(Destinations.FAVORITES) {
                AuthCheck(
                    navController = navController, // Добавляем navController
                    content = { FavoritesScreen(navController) },
                    authViewModel = authViewModel
                )
            }
            composable(Destinations.PROFILE) { // Добавляем профиль
                AuthCheck(
                    navController = navController,
                    content = { ProfileScreen(navController) },
                    authViewModel = authViewModel
                )
            }
            composable(
                route = "${Destinations.ACTOR_DETAIL}/{actorId}",
                arguments = listOf(navArgument("actorId") { type = NavType.StringType })
            ) {
                AuthCheck(
                    navController = navController, // Добавляем navController
                    content = {
                        ActorDetailScreen(
                            actorId = it.arguments?.getString("actorId"),
                            userId = authViewModel.currentUser.collectAsState().value?.id,
                            navController = navController
                        )
                    },
                    authViewModel = authViewModel
                )
            }
        }
    }
}


@Composable
private fun AuthCheck(
    navController: NavHostController,
    content: @Composable () -> Unit,
    authViewModel: AuthViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    var handled by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && !handled) {
            if (navController.currentDestination?.route != Destinations.AUTH) {
                navController.navigate(Destinations.AUTH) {
                    launchSingleTop = true
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
                handled = true
            }
        }
    }

    if (isLoggedIn) content()
}