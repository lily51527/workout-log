package idv.wennyli.workoutlog.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
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

@Composable
fun Main(
    mainNavController: NavController,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isLoggedOut by viewModel.isLoggedOut.collectAsState()

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            mainNavController.navigate(Screen.Login.route) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
        }
    }

    MainScreen(
        currentRoute = currentDestination?.route,
        onNavigate = { route ->
            bottomNavController.navigate(route) {
                popUpTo(bottomNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onSignOut = { viewModel.signOut() }
    ) { innerPadding ->
        BottomNavGraph(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            navController = bottomNavController
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val items = listOf(
        BottomNavItem.Log,
        BottomNavItem.Timer,
        BottomNavItem.AiCoach,
        BottomNavItem.Settings
    )

    val showMainBar = items.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (showMainBar) {
                MainTopAppBar(
                    currentRoute = currentRoute,
                    items = items,
                    onSignOut = onSignOut
                )
            }
        },
        bottomBar = {
            if (showMainBar) {
                MainBottomNavigationBar(
                    currentRoute = currentRoute,
                    items = items,
                    onNavigate = onNavigate
                )
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    currentRoute: String?,
    items: List<BottomNavItem>,
    onSignOut: () -> Unit
) {
    TopAppBar(
        title = {
            val titleId = items.find { it.route == currentRoute }?.labelId ?: R.string.app_name
            Text(text = stringResource(titleId))
        },
        actions = {
            if (currentRoute == BottomNavItem.Settings.route) {
                IconButton(onClick = onSignOut) {
                    Icon(
                        painter = painterResource(R.drawable.outline_logout_24),
                        contentDescription = stringResource(R.string.main_screen_logout)
                    )
                }
            }
        }
    )
}

@Composable
fun MainBottomNavigationBar(
    currentRoute: String?,
    items: List<BottomNavItem>,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(screen.iconId),
                        contentDescription = stringResource(screen.labelId)
                    )
                },
                label = { Text(stringResource(screen.labelId)) },
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) }
            )
        }
    }
}

@Preview
@Composable
fun MainTopAppBarPreview() {
    WorkoutLogTheme {
        MainTopAppBar(
            currentRoute = BottomNavItem.Settings.route,
            items = listOf(BottomNavItem.Log, BottomNavItem.Timer, BottomNavItem.Settings),
            onSignOut = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WorkoutLogTheme {
        MainScreen(
            currentRoute = BottomNavItem.Log.route, // 您可以隨便改這個字串來預覽不同狀態
            onNavigate = {},
            onSignOut = {}
        ) { innerPadding ->
            // 在 Preview 時，我們只塞入一個帶有文字的空盒子！
            // 這樣就不會觸發任何 Hilt 的呼叫了！
            Box(modifier = Modifier.padding(innerPadding)) {
                Text("這是內容區域預覽")
            }
        }
    }
}