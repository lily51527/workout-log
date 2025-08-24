package idv.wennyli.workoutlog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import idv.wennyli.workoutlog.R

// 定義底部導覽項目
sealed class BottomNavItem(val route: String, val iconId: Int, val labelId: Int) {
    object Log : BottomNavItem(
        "workout_log_screen",
        R.drawable.outline_edit_document_8,
        R.string.mian_screen_workout_record
    )

    object Timer : BottomNavItem(
        "timer_screen",
        R.drawable.outline_timer_8,
        R.string.mian_screen_timer
    )

    object Settings :
        BottomNavItem(
            "settings_screen",
            R.drawable.baseline_settings_8,
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

        }
        composable(BottomNavItem.Timer.route) {

        }
        composable(BottomNavItem.Settings.route) {

        }
    }
}