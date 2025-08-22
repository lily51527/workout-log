package idv.wennyli.workoutlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import idv.wennyli.workoutlog.ui.navigation.MainNavHost
import idv.wennyli.workoutlog.ui.navigation.Screen
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // 判斷起始畫面：如果使用者已登入，直接進入主畫面，否則顯示登入畫面
//                    val startDestination = if (auth.currentUser != null) {
//                        Screen.WorkoutLog.route
//                    } else {
//                        Screen.Login.route
//                    }

                    MainNavHost(navController = navController, startDestination = Screen.Login.route)
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    WorkoutLogTheme {
//
//    }
//}