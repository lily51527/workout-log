package idv.wennyli.workoutlog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.ui.view.settings.Settings
import idv.wennyli.workoutlog.ui.view.Timer
import idv.wennyli.workoutlog.ui.view.WorkoutLog

// 定義底部導覽項目
sealed class BottomNavItem(val route: String, val iconId: Int, val labelId: Int) {
    object Log : BottomNavItem(
        "workout_log_screen",
        R.drawable.outline_edit_document_24,
        R.string.mian_screen_workout_record
    )

    object Timer : BottomNavItem(
        "timer_screen",
        R.drawable.outline_timer_24,
        R.string.mian_screen_timer
    )

    object Settings :
        BottomNavItem(
            "settings_screen",
            R.drawable.outline_settings_24,
            R.string.mian_screen_settings
        )
}

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Log.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Log.route) {
            WorkoutLog()
        }
        composable(BottomNavItem.Timer.route) {
            Timer()
        }
        composable(BottomNavItem.Settings.route) {
            Settings()
        }
    }
}