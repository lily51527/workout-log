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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onAddWorkout: (Workout) -> Unit,
    exerciseToMuscleMap: Map<String, String>
) {
    var addWorkoutDate by remember {
        mutableStateOf(
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).format(Date())
        )
    }
    var exercise by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var repsUnit by remember { mutableStateOf("次數") }
    var muscleFeel by remember { mutableIntStateOf(3) }
    var control by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }

    var exerciseInputExpanded by remember { mutableStateOf(false) }
    var repsUnitExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(exercise) {
        muscleGroup = exerciseToMuscleMap[exercise] ?: ""
    }

    val exerciseToMuscleKeyList = exerciseToMuscleMap.keys.toList()
    val filteredSuggestions = exerciseToMuscleKeyList.filter {
        it.contains(
            exercise,
            ignoreCase = true
        ) && it.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增訓練記錄") },
        text = {
            AddWorkoutDialogContent(
                date = addWorkoutDate,
                exercise = exercise,
                muscleGroup = muscleGroup,
                weight = weight,
                sets = sets,
                reps = reps,
                repsUnit = repsUnit,
                muscleFeel = muscleFeel,
                control = control,
                notes = notes,
                exerciseInputExpanded = exerciseInputExpanded,
                repsUnitExpanded = repsUnitExpanded,
                filteredSuggestions = filteredSuggestions,
                onDateChange = { addWorkoutDate = it },
                onExerciseChange = { exercise = it },
                onExerciseExpandedChange = { exerciseInputExpanded = it },
                onRepsUnitExpandedChange = { repsUnitExpanded = it },
                onMuscleGroupChange = { muscleGroup = it },
                onWeightChange = { weight = it },
                onSetsChange = { sets = it },
                onRepsChange = { reps = it },
                onMuscleFeelChange = { muscleFeel = it },
                onControlChange = { control = it },
                onNotesChange = { notes = it },
                onUnitSelected = { repsUnit = it }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val newWorkout = Workout(
                        date = addWorkoutDate,
                        exercise = exercise,
                        muscleGroup = muscleGroup,
                        weight = weight.toDoubleOrNull() ?: 0.0,
                        sets = sets.toIntOrNull() ?: 0,
                        reps = reps.toIntOrNull() ?: 0,
                        repsUnit = repsUnit,
                        muscleFeel = muscleFeel,
                        control = control,
                        notes = notes
                    )
                    onAddWorkout(newWorkout)
                    onDismiss()
                }
            ) {
                Text("新增")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun AddWorkoutDialogContent(
    date: String,
    exercise: String,
    muscleGroup: String,
    weight: String,
    sets: String,
    reps: String,
    repsUnit: String,
    muscleFeel: Int,
    control: Int,
    notes: String,
    exerciseInputExpanded: Boolean,
    repsUnitExpanded: Boolean,
    filteredSuggestions: List<String>,
    onDateChange: (String) -> Unit,
    onExerciseChange: (String) -> Unit,
    onExerciseExpandedChange: (Boolean) -> Unit,
    onRepsUnitExpandedChange: (Boolean) -> Unit,
    onMuscleGroupChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onSetsChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onMuscleFeelChange: (Int) -> Unit,
    onControlChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onUnitSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = date,
            onValueChange = onDateChange,
            label = { Text("日期 (YYYY-MM-DD)") },
        )
        ExerciseInput(
            exercise = exercise,
            expanded = exerciseInputExpanded,
            onExpandedChange = onExerciseExpandedChange,
            onExerciseChange = onExerciseChange,
            filteredSuggestions = filteredSuggestions
        )
        OutlinedTextField(
            value = muscleGroup,
            onValueChange = onMuscleGroupChange,
            label = { Text("訓練部位") }
        )
        OutlinedTextField(
            value = weight,
            onValueChange = onWeightChange,
            label = { Text("重量 (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = sets,
            onValueChange = onSetsChange,
            label = { Text("組數") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = reps,
                onValueChange = onRepsChange,
                label = { Text("次數/時間") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(4.dp))
            RepsUnitDropDown(
                selectedUnit = repsUnit,
                expanded = repsUnitExpanded,
                onUnitSelected = onUnitSelected,
                onExpandedChange = onRepsUnitExpandedChange
            )
        }
        RatingInput(
            label = "肌肉感覺",
            rating = muscleFeel,
            onRatingChange = onMuscleFeelChange
        )
        RatingInput(
            label = "控制情況",
            rating = control,
            onRatingChange = onControlChange
        )
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
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
            Icon(painter = painterResource(R.drawable.arrow_drop_down_24px), contentDescription = null)
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
        AddWorkoutDialogContent(
            date = "2023-09-03",
            exercise = "引體向上",
            muscleGroup = "三角肌 (前束)",
            sets = "3",
            reps = "8",
            repsUnit = "次",
            weight = "10.0",
            exerciseInputExpanded = false,
            repsUnitExpanded = false,
            muscleFeel = 3,
            control = 3,
            notes = "",
            onDateChange = {},
            onExerciseChange = {},
            onExerciseExpandedChange = {},
            filteredSuggestions = emptyList(),
            onSetsChange = {},
            onWeightChange = {},
            onMuscleFeelChange = {},
            onRepsUnitExpandedChange = {},
            onNotesChange = {},
            onMuscleGroupChange = {},
            onControlChange = {},
            onRepsChange = {},
            onUnitSelected = {}
        )
    }
}