package idv.wennyli.workoutlog.ui.view.workoutLog

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutLog(
    viewModel: WorkoutLogViewModel = hiltViewModel(),
    onNavigateToAddWorkout: () -> Unit,
    onNavigateToEditWorkout: (String) -> Unit
) {
    val workoutList by viewModel.workouts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()

    WorkoutLogScreen(
        workoutList = workoutList,
        loading = loading,
        error = error,
        currentDate = currentDate,
        onDateSelected = { viewModel.setCurrentDate(it) },
        onDeleteWorkout = { viewModel.deleteWorkout(it) },
        onEditWorkout = onNavigateToEditWorkout,
        onNavigateToAddWorkout = onNavigateToAddWorkout,
        onErrorClear = { viewModel.clearError() }
    )
}

@Composable
private fun WorkoutLogScreen(
    workoutList: List<Workout>,
    loading: Boolean,
    error: String?,
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDeleteWorkout: (String) -> Unit,
    onEditWorkout: (String) -> Unit,
    onNavigateToAddWorkout: () -> Unit,
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
                    workoutList = workoutList.filter { it.date == currentDate },
                    onDeleteWorkout = onDeleteWorkout,
                    onEditWorkout = onEditWorkout
                )
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAddWorkout,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(painter = painterResource(R.drawable.add_24px), contentDescription = "新增訓練")
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
            painter = painterResource(R.drawable.calendar_month_24px),
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
    onDeleteWorkout: (String) -> Unit,
    onEditWorkout: (String) -> Unit
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
                    onDeleteWorkout = onDeleteWorkout,
                    onEditWorkout = onEditWorkout
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun WorkoutItem(
    workout: Workout,
    onDeleteWorkout: (String) -> Unit,
    onEditWorkout: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable { onEditWorkout(workout.id) },
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
                Text(
                    text = workout.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                    painter = painterResource(R.drawable.delete_24px),
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
            onDeleteWorkout = {},
            onEditWorkout = {}
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
            onDeleteWorkout = {},
            onEditWorkout = {}
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
            error = null,
            currentDate = "2023-08-01",
            onDateSelected = {},
            onDeleteWorkout = {},
            onEditWorkout = {},
            onNavigateToAddWorkout = {},
            onErrorClear = {}
        )
    }
}