package idv.wennyli.workoutlog.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.ui.navigation.BottomNavGraph
import idv.wennyli.workoutlog.ui.navigation.BottomNavItem
import idv.wennyli.workoutlog.ui.navigation.Screen
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(mainNavController: NavController) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem.Log,
        BottomNavItem.Timer,
        BottomNavItem.Settings
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleId = items.find { it.route == currentDestination?.route }?.labelId
                        ?: R.string.app_name
                    Text(text = stringResource(titleId))
                },
                actions = {
                    // 只在設定頁面顯示登出按鈕
                    if (currentDestination?.route == BottomNavItem.Settings.route) {
                        IconButton(onClick = {
                            Firebase.auth.signOut()
                            mainNavController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Main.route) { inclusive = true }
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.outline_logout_8),
                                contentDescription = stringResource(R.string.main_screen_logout)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { painterResource(screen.iconId) },
                        label = { Text(stringResource(screen.labelId)) },
                        selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        BottomNavGraph(
            modifier = Modifier.padding(innerPadding),
            navController = bottomNavController
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WorkoutLogTheme {
        // 預覽功能需要一個 NavController，我們可以在這裡建立一個假的實例
        val navController = rememberNavController()
        Main(mainNavController = navController)
    }
}