package idv.wennyli.workoutlog.ui.view.workoutLog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.data.model.RepsUnit
import idv.wennyli.workoutlog.data.model.Workout
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddWorkoutRoute(
    viewModel: AddWorkoutViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val workoutToEdit by viewModel.workoutToEdit.collectAsState()

    // 編輯模式且資料尚未載入完成時，顯示 loading 畫面
    if (viewModel.isEditMode && isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    AddWorkoutScreen(
        isEditMode = viewModel.isEditMode,
        initialWorkout = workoutToEdit,
        onNavigateUp = onNavigateUp,
        onSaveWorkout = { workout ->
            viewModel.saveWorkout(workout)
            onNavigateUp()
        },
        exerciseToMuscleMap = viewModel.exerciseToMuscleMap
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    isEditMode: Boolean = false,
    initialWorkout: Workout? = null,
    onNavigateUp: () -> Unit,
    onSaveWorkout: (Workout) -> Unit,
    exerciseToMuscleMap: Map<String, String>
) {
    var addWorkoutDate by remember {
        mutableStateOf(
            initialWorkout?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        )
    }
    var exercise by remember { mutableStateOf(initialWorkout?.exercise ?: "") }
    var muscleGroup by remember { mutableStateOf(initialWorkout?.muscleGroup ?: "") }
    var weight by remember {
        mutableStateOf(initialWorkout?.weight?.let { if (it == 0.0) "" else it.toString() } ?: "")
    }
    var sets by remember {
        mutableStateOf(initialWorkout?.sets?.let { if (it == 0) "" else it.toString() } ?: "")
    }
    var reps by remember {
        mutableStateOf(initialWorkout?.reps?.let { if (it == 0) "" else it.toString() } ?: "")
    }
    var repsUnit by remember { mutableStateOf(RepsUnit.fromDisplayName(initialWorkout?.repsUnit)) }
    var muscleFeel by remember { mutableIntStateOf(initialWorkout?.muscleFeel ?: 3) }
    var control by remember { mutableIntStateOf(initialWorkout?.control ?: 3) }
    var notes by remember { mutableStateOf(initialWorkout?.notes ?: "") }

    val topBarTitle = if (isEditMode) stringResource(R.string.add_workout_title_edit) else stringResource(R.string.add_workout_title)
    val buttonText = if (isEditMode) stringResource(R.string.common_save) else stringResource(R.string.add_workout_button_add)

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
    Scaffold(
        topBar = {
            AddWorkoutTopAppBar(title = topBarTitle, onNavigateUp = onNavigateUp)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                AddWorkoutContent(
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

                Spacer(modifier = Modifier.height(16.dp))

            }

            // 確認按鈕放在頁面底部
            AddWorkoutButton(
                buttonText = buttonText,
                newWorkout = Workout(
                    date = addWorkoutDate,
                    exercise = exercise,
                    muscleGroup = muscleGroup,
                    weight = weight.toDoubleOrNull() ?: 0.0,
                    sets = sets.toIntOrNull() ?: 0,
                    reps = reps.toIntOrNull() ?: 0,
                    repsUnit = repsUnit.displayName,
                    muscleFeel = muscleFeel,
                    control = control,
                    notes = notes
                ),
                onSaveWorkout = onSaveWorkout
            )
        }
    }
}

@Composable
private fun AddWorkoutContent(
    date: String,
    exercise: String,
    muscleGroup: String,
    weight: String,
    sets: String,
    reps: String,
    repsUnit: RepsUnit,
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
    onUnitSelected: (RepsUnit) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = date,
            onValueChange = onDateChange,
            label = { Text(stringResource(R.string.add_workout_label_date)) },
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
            label = { Text(stringResource(R.string.add_workout_label_muscle_group)) }
        )
        OutlinedTextField(
            value = weight,
            onValueChange = onWeightChange,
            label = { Text(stringResource(R.string.add_workout_label_weight)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = sets,
            onValueChange = onSetsChange,
            label = { Text(stringResource(R.string.add_workout_label_sets)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = reps,
                onValueChange = onRepsChange,
                label = { Text(stringResource(R.string.add_workout_label_reps)) },
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
            label = stringResource(R.string.add_workout_label_muscle_feel),
            rating = muscleFeel,
            onRatingChange = onMuscleFeelChange
        )
        RatingInput(
            label = stringResource(R.string.add_workout_label_control),
            rating = control,
            onRatingChange = onControlChange
        )
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.add_workout_label_notes)) }
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
            label = { Text(stringResource(R.string.add_workout_label_exercise)) },
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
    selectedUnit: RepsUnit,
    expanded: Boolean,
    onUnitSelected: (RepsUnit) -> Unit,
    onExpandedChange: (Boolean) -> Unit
) {
    Box {
        OutlinedButton(
            onClick = { onExpandedChange(true) }
        ) {
            Text(selectedUnit.displayName)
            Icon(
                painter = painterResource(R.drawable.arrow_drop_down_24px),
                contentDescription = null
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            RepsUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.displayName) },
                    onClick = {
                        onUnitSelected(unit)
                        onExpandedChange(false)
                    }
                )
            }
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

@Composable
private fun AddWorkoutButton(
    buttonText: String,
    newWorkout: Workout,
    onSaveWorkout: (Workout) -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, top = 8.dp),
        onClick = {
            onSaveWorkout(newWorkout)
        }
    ) {
        Text(buttonText)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutTopAppBar(
    title: String,
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_24px),
                    contentDescription = stringResource(R.string.common_back_cd)
                )
            }
        }
    )
}

@Preview
@Composable
fun AddWorkoutTopAppBarPreview() {
    WorkoutLogTheme {
        AddWorkoutTopAppBar(title = "新增訓練記錄", onNavigateUp = {})
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
            selectedUnit = RepsUnit.COUNT,
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
private fun AddWorkoutContentPreview() {
    WorkoutLogTheme {
        AddWorkoutContent(
            date = "2023-09-03",
            exercise = "引體向上",
            muscleGroup = "三角肌 (前束)",
            sets = "3",
            reps = "8",
            repsUnit = RepsUnit.COUNT,
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

@Preview(showBackground = true)
@Composable
private fun AddWorkoutButtonPreview() {
    WorkoutLogTheme {
        AddWorkoutButton(
            buttonText = "新增",
            newWorkout = Workout(),
            onSaveWorkout = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddWorkoutScreenPreview() {
    WorkoutLogTheme {
        AddWorkoutScreen(
            onNavigateUp = {},
            onSaveWorkout = {},
            exerciseToMuscleMap = mapOf( // 提供一點假資料讓下拉選單有東西顯示
                "臥推" to "胸大肌, 三頭肌",
                "深蹲" to "股四頭肌, 臀大肌",
                "引體向上" to "闊背肌, 二頭肌"
            )
        )
    }
}