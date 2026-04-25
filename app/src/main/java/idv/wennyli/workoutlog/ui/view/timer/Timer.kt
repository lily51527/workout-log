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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme
import kotlinx.coroutines.delay

@Composable
fun Timer(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val exerciseName by viewModel.exerciseName.collectAsState()
    val totalSets by viewModel.totalSets.collectAsState()
    val restTime by viewModel.restTime.collectAsState()
    val currentSet by viewModel.currentSet.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val showSaveConfirmation by viewModel.showSaveConfirmation.collectAsState()

    LaunchedEffect(key1 = timeLeft, key2 = timerState) {
        if (timerState == TimerState.RESTING && timeLeft > 0) {
            delay(1000L)
            viewModel.setTimeLeft(timeLeft - 1)
        } else if (timerState == TimerState.RESTING && timeLeft == 0) {
            val nextState = if (currentSet < totalSets) TimerState.WORKING else TimerState.FINISHED
            viewModel.setTimerState(nextState)
        }
    }

    TimerScreen(
        exerciseName = exerciseName,
        totalSets = totalSets,
        restTime = restTime,
        currentSet = currentSet,
        timeLeft = timeLeft,
        timerState = timerState,
        isShowSaveConfirmation = showSaveConfirmation,
        onExerciseNameChange = { viewModel.onExerciseNameChange(it) },
        onTotalSetsChange = { viewModel.onTotalSetsChange(it) },
        onRestTimeChange = { viewModel.onRestTimeChange(it) },
        onStartSet = { viewModel.onStartSet() },
        onStartRest = { viewModel.onStartRest() },
        onSkipRest = { viewModel.onSkipRest() },
        onReset = { viewModel.onReset() },
        onSaveWorkout = { viewModel.saveWorkout() },
        onSaveConfirmationDismissRequest = { viewModel.onReset() }
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
    isShowSaveConfirmation: Boolean,
    onExerciseNameChange: (String) -> Unit,
    onTotalSetsChange: (Int) -> Unit,
    onRestTimeChange: (Int) -> Unit,
    onStartSet: () -> Unit,
    onStartRest: () -> Unit,
    onSkipRest: () -> Unit,
    onReset: () -> Unit,
    onSaveWorkout: () -> Unit,
    onSaveConfirmationDismissRequest: () -> Unit
) {
    if (isShowSaveConfirmation) {
        AlertDialog(
            onDismissRequest = onSaveConfirmationDismissRequest,
            title = { Text(stringResource(R.string.timer_save_success_title)) },
            text = { Text(stringResource(R.string.timer_save_success_message)) },
            confirmButton = {
                Button(onClick = onSaveConfirmationDismissRequest) {
                    Text(stringResource(R.string.common_confirm))
                }
            }
        )
    }

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
            onReset = onReset,
            onSaveWorkout = onSaveWorkout,
            isSaveEnabled = exerciseName.isNotBlank() && currentSet > 0
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
            label = { Text(stringResource(R.string.timer_label_exercise_name)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = totalSets.toString(),
                onValueChange = { onTotalSetsChange(it.toIntOrNull() ?: 1) },
                label = { Text(stringResource(R.string.timer_label_total_sets)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                enabled = isEnabled
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = restTime.toString(),
                onValueChange = { onRestTimeChange(it.toIntOrNull() ?: 0) },
                label = { Text(stringResource(R.string.timer_label_rest_time)) },
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
            text = exerciseName.ifBlank { stringResource(R.string.timer_no_exercise) },
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(R.string.timer_current_set, currentSet, totalSets),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (timerState) {
                TimerState.IDLE -> stringResource(R.string.timer_state_idle)
                TimerState.WORKING -> stringResource(R.string.timer_state_working, currentSet)
                TimerState.RESTING -> stringResource(R.string.timer_state_resting, formatTime(timeLeft))
                TimerState.FINISHED -> stringResource(R.string.timer_state_finished)
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
    onReset: () -> Unit,
    onSaveWorkout: () -> Unit,
    isSaveEnabled: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 當計時器完成時，顯示儲存按鈕
        if (timerState == TimerState.FINISHED) {
            Button(
                onClick = onSaveWorkout,
                enabled = isSaveEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(stringResource(R.string.timer_button_save))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
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
                Text(stringResource(R.string.timer_button_start_set))
            }
            Button(
                onClick = onStartRest,
                enabled = timerState == TimerState.WORKING
            ) {
                Text(stringResource(R.string.timer_button_start_rest))
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
                Text(stringResource(R.string.timer_button_skip_rest))
            }
            Button(
                onClick = onReset
            ) {
                Text(stringResource(R.string.timer_button_reset))
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
            onReset = {},
            onSaveWorkout = {},
            isSaveEnabled = true
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
            onReset = {},
            onSaveWorkout = {},
            isShowSaveConfirmation = false,
            onSaveConfirmationDismissRequest = {}
        )
    }
}