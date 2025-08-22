package idv.wennyli.workoutlog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import idv.wennyli.workoutlog.ui.view.Login

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object WorkoutLog : Screen("workout_log_screen")
    object Timer : Screen("timer_screen")
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
        composable(Screen.WorkoutLog.route) {

        }
        composable(Screen.Timer.route) {

        }
    }
}