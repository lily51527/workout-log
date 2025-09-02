package idv.wennyli.workoutlog.ui.view.workoutLog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme

data class AddWorkoutDialogState(
    val date: String,
    val exercise: String,
    val muscleGroup: String,
    val weight: String,
    val sets: String,
    val reps: String,
    val repsUnit: String,
    val muscleFeel: Int,
    val control: Int,
    val notes: String,
    val exerciseInputExpanded: Boolean,
    val repsUnitExpanded: Boolean,
    val filteredSuggestions: List<String>
)

data class AddWorkoutDialogEvents(
    val onDateChange: (String) -> Unit,
    val onExerciseChange: (String) -> Unit,
    val onExerciseExpandedChange: (Boolean) -> Unit,
    val onRepsUnitExpandedChange: (Boolean) -> Unit,
    val onMuscleGroupChange: (String) -> Unit,
    val onWeightChange: (String) -> Unit,
    val onSetsChange: (String) -> Unit,
    val onRepsChange: (String) -> Unit,
    val onMuscleFeelChange: (Int) -> Unit,
    val onControlChange: (Int) -> Unit,
    val onNotesChange: (String) -> Unit,
    val onUnitSelected: (String) -> Unit,
    val onDismiss: () -> Unit,
    val onAddWorkout: (Workout) -> Unit
)

@Composable
fun AddWorkoutDialog(
    state: AddWorkoutDialogState,
    events: AddWorkoutDialogEvents
) {
    AlertDialog(
        onDismissRequest = events.onDismiss,
        title = { Text("新增訓練記錄") },
        text = {
            AddWorkoutDialogContent(
                state = state,
                events = events
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val newWorkout = Workout(
                        date = state.date,
                        exercise = state.exercise,
                        muscleGroup = state.muscleGroup,
                        weight = state.weight.toDoubleOrNull() ?: 0.0,
                        sets = state.sets.toIntOrNull() ?: 0,
                        reps = state.reps.toIntOrNull() ?: 0,
                        repsUnit = state.repsUnit,
                        muscleFeel = state.muscleFeel,
                        control = state.control,
                        notes = state.notes
                    )
                    events.onAddWorkout(newWorkout)
                    events.onDismiss()
                }
            ) {
                Text("新增")
            }
        },
        dismissButton = {
            Button(onClick = events.onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun AddWorkoutDialogContent(
    state: AddWorkoutDialogState,
    events: AddWorkoutDialogEvents
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = state.date,
            onValueChange = events.onDateChange,
            label = { Text("日期 (YYYY-MM-DD)") },
        )
        ExerciseInput(
            exercise = state.exercise,
            expanded = state.exerciseInputExpanded,
            onExpandedChange = events.onExerciseExpandedChange,
            onExerciseChange = events.onExerciseChange,
            filteredSuggestions = state.filteredSuggestions
        )
        OutlinedTextField(
            value = state.muscleGroup,
            onValueChange = events.onMuscleGroupChange,
            label = { Text("訓練部位") }
        )
        OutlinedTextField(
            value = state.weight,
            onValueChange = events.onWeightChange,
            label = { Text("重量 (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = state.sets,
            onValueChange = events.onSetsChange,
            label = { Text("組數") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.reps,
                onValueChange = events.onRepsChange,
                label = { Text("次數/時間") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(4.dp))
            RepsUnitDropDown(
                selectedUnit = state.repsUnit,
                expanded = state.repsUnitExpanded,
                onUnitSelected = events.onUnitSelected,
                onExpandedChange = events.onRepsUnitExpandedChange
            )
        }
        RatingInput(
            label = "肌肉感覺",
            rating = state.muscleFeel,
            onRatingChange = events.onMuscleFeelChange
        )
        RatingInput(
            label = "控制情況",
            rating = state.control,
            onRatingChange = events.onControlChange
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = events.onNotesChange,
            label = { Text("備註") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseInput(
    exercise: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onExerciseChange: (String) -> Unit,
    filteredSuggestions: List<String>
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) }
    ) {
        OutlinedTextField(
            value = exercise,
            onValueChange = onExerciseChange,
            label = { Text("訓練動作") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        if (filteredSuggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                filteredSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onExerciseChange(suggestion)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RepsUnitDropDown(
    selectedUnit: String,
    expanded: Boolean,
    onUnitSelected: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit
) {
    Box {
        OutlinedButton(
            onClick = { onExpandedChange(true) }
        ) {
            Text(selectedUnit)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            DropdownMenuItem(
                text = { Text("次數") },
                onClick = {
                    onUnitSelected("次數")
                    onExpandedChange(false)
                }
            )
            DropdownMenuItem(
                text = { Text("秒數") },
                onClick = {
                    onUnitSelected("秒數")
                    onExpandedChange(false)
                }
            )
        }
    }
}

@Composable
private fun RatingInput(
    label: String,
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row {
            (1..5).forEach { value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = rating == value,
                        onClick = { onRatingChange(value) }
                    )
                    Text(text = value.toString())
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseInputPreview() {
    WorkoutLogTheme {
        ExerciseInput(
            exercise = "引體向上",
            expanded = false,
            onExpandedChange = {},
            onExerciseChange = {},
            filteredSuggestions = listOf("引體向上", "深蹲")
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RepsUnitDropDownPreview() {
    WorkoutLogTheme {
        RepsUnitDropDown(
            selectedUnit = "次數",
            expanded = false,
            onUnitSelected = {},
            onExpandedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingInputPreview() {
    WorkoutLogTheme {
        RatingInput(
            label = "感覺",
            rating = 3,
            onRatingChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddWorkoutDialogPreview() {
    WorkoutLogTheme {
        val state = AddWorkoutDialogState(
            date = "2023-08-01",
            exercise = "引體向上",
            muscleGroup = "三角肌 (前束)",
            weight = "10.0",
            sets = "3",
            reps = "8",
            repsUnit = "次",
            muscleFeel = 3,
            control = 3,
            notes = "remark",
            exerciseInputExpanded = false,
            repsUnitExpanded = false,
            filteredSuggestions = listOf("引體向上", "深蹲")
        )

        val events = AddWorkoutDialogEvents(
            onDateChange = {},
            onExerciseChange = {},
            onExerciseExpandedChange = {},
            onRepsUnitExpandedChange = {},
            onMuscleGroupChange = {},
            onWeightChange = {},
            onSetsChange = {},
            onRepsChange = {},
            onUnitSelected = {},
            onMuscleFeelChange = {},
            onControlChange = {},
            onNotesChange = {},
            onDismiss = {},
            onAddWorkout = {}
        )

        AddWorkoutDialogContent(
            state = state,
            events = events
        )
    }
}