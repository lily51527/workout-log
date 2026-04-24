package idv.wennyli.workoutlog.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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

// 新增：將新增/編輯畫面的路由與參數萃取為靜態常數與輔助函式
object WorkoutDestinations {
    const val ADD_WORKOUT_BASE = "add_workout_screen"
    const val WORKOUT_ID_ARG = "workoutId"
    // 用於 NavGraph 定義的路由格式
    const val ADD_WORKOUT_ROUTE = "$ADD_WORKOUT_BASE?$WORKOUT_ID_ARG={$WORKOUT_ID_ARG}"

    // 輔助函式：根據是否有傳入 ID 來動態產生要跳轉的路由字串
    fun getAddWorkoutRoute(workoutId: String? = null): String {
        return if (workoutId == null) {
            ADD_WORKOUT_BASE
        } else {
            "$ADD_WORKOUT_BASE?$WORKOUT_ID_ARG=$workoutId"
        }
    }
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
            WorkoutLog(
                onNavigateToAddWorkout = {
                    // 導覽至新增頁面 (使用靜態輔助函式，不帶參數)
                    navController.navigate(WorkoutDestinations.getAddWorkoutRoute())
                },
                onNavigateToEditWorkout = { workoutId ->
                    // 導覽至編輯頁面 (使用靜態輔助函式，並帶入參數)
                    navController.navigate(WorkoutDestinations.getAddWorkoutRoute(workoutId))
                }
            )
        }

        composable(
            route = WorkoutDestinations.ADD_WORKOUT_ROUTE,
            arguments = listOf(navArgument(WorkoutDestinations.WORKOUT_ID_ARG) {
                type = NavType.StringType
                nullable = true
            }),
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
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString(WorkoutDestinations.WORKOUT_ID_ARG)
            AddWorkoutRoute(
                workoutId = workoutId,
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