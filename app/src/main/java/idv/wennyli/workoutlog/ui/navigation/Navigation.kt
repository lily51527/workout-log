package idv.wennyli.workoutlog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import idv.wennyli.workoutlog.ui.view.Login
import idv.wennyli.workoutlog.ui.view.Main

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Main : Screen("main_screen")
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            Login(navController)
        }
        composable(Screen.Main.route) {
            Main(navController)
        }
    }
}