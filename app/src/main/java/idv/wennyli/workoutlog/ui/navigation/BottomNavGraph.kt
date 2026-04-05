package idv.wennyli.workoutlog.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.ui.view.settings.Settings
import idv.wennyli.workoutlog.ui.view.timer.Timer
import idv.wennyli.workoutlog.ui.view.workoutLog.AddWorkoutRoute
import idv.wennyli.workoutlog.ui.view.workoutLog.WorkoutLog

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

private const val addWorkoutRoute = "add_workout_screen"

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
            WorkoutLog(
                onNavigateToAddWorkout = {
                    // 導覽至新增頁面
                    navController.navigate(addWorkoutRoute)
                }
            )
        }

        composable(
            route = addWorkoutRoute,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(durationMillis = 300) // 動畫持續 300 毫秒
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) {
            AddWorkoutRoute(
                onNavigateUp = {
                    navController.popBackStack()
                }
            )
        }
        composable(BottomNavItem.Timer.route) {
            Timer()
        }
        composable(BottomNavItem.Settings.route) {
            Settings()
        }
    }
}