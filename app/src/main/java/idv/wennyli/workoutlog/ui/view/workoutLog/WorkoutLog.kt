package idv.wennyli.workoutlog.ui.view.workoutLog

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutLog(
    viewModel: WorkoutLogViewModel = hiltViewModel()
) {
    val workoutList by viewModel.workouts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()

    var showAddWorkoutDialog by remember { mutableStateOf(false) }

    WorkoutLogScreen(
        workoutList = workoutList,
        loading = loading,
        showAddWorkoutDialog = showAddWorkoutDialog,
        onAddWorkout = { viewModel.addWorkout(it) },
        exerciseToMuscleMap = viewModel.exerciseToMuscleMap,
        error = error,
        currentDate = currentDate,
        onDateSelected = { viewModel.setCurrentDate(it) },
        onDeleteWorkout = { viewModel.deleteWorkout(it) },
        onShowAddWorkoutDialog = { showAddWorkoutDialog = it },
        onErrorClear = { viewModel.clearError() }
    )
}

@Composable
private fun WorkoutLogScreen(
    workoutList: List<Workout>,
    loading: Boolean,
    showAddWorkoutDialog: Boolean,
    onAddWorkout: (Workout) -> Unit,
    exerciseToMuscleMap: Map<String, String>,
    error: String?,
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDeleteWorkout: (String) -> Unit,
    onShowAddWorkoutDialog: (Boolean) -> Unit,
    onErrorClear: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                DateSelector(
                    currentDate = currentDate,
                    onDateSelected = onDateSelected
                )
                Spacer(modifier = Modifier.padding(8.dp))
                WorkoutList(
                    workoutList = workoutList,
                    onDeleteWorkout = onDeleteWorkout
                )
            }
        }

        FloatingActionButton(
            onClick = { onShowAddWorkoutDialog(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "新增訓練")
        }

        if (showAddWorkoutDialog) {
            AddWorkoutDialog(
                onDismiss = { onShowAddWorkoutDialog(false) },
                onAddWorkout = onAddWorkout,
                exerciseToMuscleMap = exerciseToMuscleMap
            )
        }

        error?.let {
            AlertDialog(
                onDismissRequest = onErrorClear,
                title = { Text("錯誤") },
                text = { Text(it) },
                confirmButton = {
                    Button(onClick = onErrorClear) {
                        Text("確定")
                    }
                }
            )
        }
    }
}

@Composable
private fun DateSelector(
    currentDate: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    try {
        calendar.time = sdf.parse(currentDate) ?: Date()
    } catch (e: Exception) {
        Log.e("DateSelector", "Error parsing date: ${e.message}")
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            onDateSelected(sdf.format(calendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedButton(
        onClick = { datePickerDialog.show() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "選擇日期",
            modifier = Modifier.size(
                ButtonDefaults.IconSize
            )
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text("目前日期: $currentDate")
    }
}

@Composable
private fun WorkoutList(
    workoutList: List<Workout>,
    onDeleteWorkout: (String) -> Unit
) {
    if (workoutList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("此日期沒有訓練記錄。")
        }
    } else {
        LazyColumn {
            items(workoutList, key = { it.id }) { workout ->
                WorkoutItem(
                    workout = workout,
                    onDeleteWorkout = onDeleteWorkout
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun WorkoutItem(
    workout: Workout,
    onDeleteWorkout: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(workout.exercise, style = MaterialTheme.typography.titleMedium)
                Text(workout.muscleGroup, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "重量: ${workout.weight}kg, 組數: ${workout.sets}, 次數/時間: ${workout.reps} ${workout.repsUnit}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "感覺: ${workout.muscleFeel}/5, 控制: ${workout.control}/5",
                    style = MaterialTheme.typography.bodySmall
                )
                if (workout.notes.isNotBlank()) {
                    Text("備註: ${workout.notes}", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = { onDeleteWorkout(workout.id) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "刪除訓練",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutItemPreview() {
    WorkoutLogTheme {
        WorkoutItem(
            workout = Workout(
                id = "1",
                exercise = "引體向上",
                muscleGroup = "三角肌 (前束)",
                sets = 3,
                reps = 8,
                repsUnit = "次",
                weight = 10.0,
                date = "2023-08-01",
                muscleFeel = 3,
                control = 3,
                notes = "remark"
            ),
            onDeleteWorkout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutListPreview() {
    WorkoutLogTheme {
        WorkoutList(
            workoutList = listOf(
                Workout(
                    id = "1",
                    exercise = "引體向上",
                    muscleGroup = "闊背肌",
                    sets = 3,
                    reps = 8,
                    repsUnit = "次",
                    weight = 10.0,
                    date = "2023-08-01",
                    muscleFeel = 3,
                    control = 3,
                    notes = "remark"
                ),
                Workout(
                    id = "2",
                    exercise = "深蹲",
                    muscleGroup = "股四頭肌",
                    sets = 3,
                    reps = 8,
                    repsUnit = "次",
                    weight = 10.0,
                    date = "2023-08-01",
                    muscleFeel = 3,
                    control = 3,
                    notes = "remark"
                )
            ),
            onDeleteWorkout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutLogScreenPreview() {
    WorkoutLogTheme {
        WorkoutLogScreen(
            workoutList = emptyList(),
            loading = false,
            showAddWorkoutDialog = false,
            onAddWorkout = {},
            exerciseToMuscleMap = emptyMap(),
            error = null,
            currentDate = "2023-08-01",
            onDateSelected = {},
            onDeleteWorkout = {},
            onShowAddWorkoutDialog = {},
            onErrorClear = {}
        )
    }
}