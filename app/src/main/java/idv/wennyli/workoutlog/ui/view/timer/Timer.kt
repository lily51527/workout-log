package idv.wennyli.workoutlog.ui.view.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme
import kotlinx.coroutines.delay

private enum class TimerState {
    IDLE, WORKING, RESTING, FINISHED
}

@Composable
fun Timer() {
    var exerciseName by remember { mutableStateOf("") }
    var totalSets by remember { mutableIntStateOf(3) }
    var restTime by remember { mutableIntStateOf(60) }
    var currentSet by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(0) }
    var timerState by remember { mutableStateOf(TimerState.IDLE) }

    LaunchedEffect(key1 = timeLeft, key2 = timerState) {
        if (timerState == TimerState.RESTING && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else if (timerState == TimerState.RESTING && timeLeft == 0) {
            timerState = if (currentSet < totalSets) TimerState.WORKING else TimerState.FINISHED
        }
    }

    TimerScreen(
        exerciseName = exerciseName,
        totalSets = totalSets,
        restTime = restTime,
        currentSet = currentSet,
        timeLeft = timeLeft,
        timerState = timerState,
        onExerciseNameChange = { exerciseName = it },
        onTotalSetsChange = { totalSets = it },
        onRestTimeChange = { restTime = it },
        onStartSet = {
            if (currentSet < totalSets) {
                currentSet++
                timerState = TimerState.WORKING
            }
        },
        onStartRest = {
            if (timerState == TimerState.WORKING) {
                timeLeft = restTime
                timerState = TimerState.RESTING
            }
        },
        onSkipRest = {
            timeLeft = 0
            timerState = if (currentSet < totalSets) TimerState.WORKING else TimerState.FINISHED
        },
        onReset = {
            exerciseName = ""
            totalSets = 3
            restTime = 60
            currentSet = 0
            timeLeft = 0
            timerState = TimerState.IDLE
        }
    )
}

@Composable
private fun TimerScreen(
    exerciseName: String,
    totalSets: Int,
    restTime: Int,
    currentSet: Int,
    timeLeft: Int,
    timerState: TimerState,
    onExerciseNameChange: (String) -> Unit,
    onTotalSetsChange: (Int) -> Unit,
    onRestTimeChange: (Int) -> Unit,
    onStartSet: () -> Unit,
    onStartRest: () -> Unit,
    onSkipRest: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TimerSetup(
            exerciseName = exerciseName,
            onExerciseNameChange = onExerciseNameChange,
            totalSets = totalSets,
            onTotalSetsChange = onTotalSetsChange,
            restTime = restTime,
            onRestTimeChange = onRestTimeChange,
            isEnabled = timerState == TimerState.IDLE
        )

        TimerDisplay(
            exerciseName = exerciseName,
            currentSet = currentSet,
            totalSets = totalSets,
            timeLeft = timeLeft,
            timerState = timerState
        )

        TimerControls(
            timerState = timerState,
            onStartSet = onStartSet,
            onStartRest = onStartRest,
            onSkipRest = onSkipRest,
            onReset = onReset
        )
    }
}

@Composable
private fun TimerSetup(
    exerciseName: String,
    onExerciseNameChange: (String) -> Unit,
    totalSets: Int,
    onTotalSetsChange: (Int) -> Unit,
    restTime: Int,
    onRestTimeChange: (Int) -> Unit,
    isEnabled: Boolean
) {
    Column {
        OutlinedTextField(
            value = exerciseName,
            onValueChange = onExerciseNameChange,
            label = { Text("運動名稱") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = totalSets.toString(),
                onValueChange = { onTotalSetsChange(it.toIntOrNull() ?: 1) },
                label = { Text("總組數") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                enabled = isEnabled
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = restTime.toString(),
                onValueChange = { onRestTimeChange(it.toIntOrNull() ?: 0) },
                label = { Text("休息時間(秒)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                enabled = isEnabled
            )
        }
    }
}

@Composable
private fun TimerDisplay(
    exerciseName: String,
    currentSet: Int,
    totalSets: Int,
    timeLeft: Int,
    timerState: TimerState
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = exerciseName.ifBlank { "未設定動作" },
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "目前組數: $currentSet / $totalSets",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (timerState) {
                TimerState.IDLE -> "準備開始"
                TimerState.WORKING -> "進行中 (第 $currentSet 組)"
                TimerState.RESTING -> "休息中: ${formatTime(timeLeft)}"
                TimerState.FINISHED -> "訓練完成！"
            }
        )
    }
}

@Composable
private fun TimerControls(
    timerState: TimerState,
    onStartSet: () -> Unit,
    onStartRest: () -> Unit,
    onSkipRest: () -> Unit,
    onReset: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onStartSet,
                enabled = timerState == TimerState.IDLE ||
                        timerState == TimerState.WORKING ||
                        timerState == TimerState.FINISHED
            ) {
                Text("開始一組")
            }
            Button(
                onClick = onStartRest,
                enabled = timerState == TimerState.WORKING
            ) {
                Text("開始休息")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onSkipRest,
                enabled = timerState == TimerState.RESTING
            ) {
                Text("跳過休息")
            }
            Button(
                onClick = onReset
            ) {
                Text("重設")
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@Preview(showBackground = true)
@Composable
fun TimerSetupPreview() {
    WorkoutLogTheme {
        TimerSetup(
            exerciseName = "深蹲",
            onExerciseNameChange = {},
            totalSets = 3,
            onTotalSetsChange = {},
            restTime = 60,
            onRestTimeChange = {},
            isEnabled = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimerDisplayPreview() {
    WorkoutLogTheme {
        TimerDisplay(
            exerciseName = "深蹲",
            currentSet = 1,
            totalSets = 3,
            timeLeft = 30,
            timerState = TimerState.RESTING
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerControlsPreview() {
    WorkoutLogTheme {
        TimerControls(
            timerState = TimerState.IDLE,
            onStartSet = {},
            onStartRest = {},
            onSkipRest = {},
            onReset = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview() {
    WorkoutLogTheme {
        TimerScreen(
            exerciseName = "深蹲",
            totalSets = 3,
            restTime = 60,
            currentSet = 1,
            timeLeft = 30,
            timerState = TimerState.WORKING,
            onExerciseNameChange = {},
            onTotalSetsChange = {},
            onRestTimeChange = {},
            onStartSet = {},
            onStartRest = {},
            onSkipRest = {},
            onReset = {}
        )
    }
}