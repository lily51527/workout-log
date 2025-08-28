package idv.wennyli.workoutlog.ui.view.settings

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.data.model.BodyMeasurement
import idv.wennyli.workoutlog.data.model.UserProfile
import idv.wennyli.workoutlog.ui.theme.WorkoutLogTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun Settings(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val measurementList by viewModel.measurements.collectAsState()
    val error by viewModel.error.collectAsState()

    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }

    SettingsScreen(
        userProfile = userProfile,
        measurementList = measurementList,
        error = error,
        height = height,
        weight = weight,
        bodyFat = bodyFat,
        onGenderChange = { viewModel.updateUserProfile(gender = it) },
        onBirthDateChange = { viewModel.updateUserProfile(birthDate = it) },
        onHeightChange = { height = it },
        onWeightChange = { weight = it },
        onBodyFatChange = { bodyFat = it },
        onSave = {
            viewModel.addBodyMeasurement(height, weight, bodyFat)
            height = ""
            weight = ""
            bodyFat = ""
        },
        onMeasurementDelete = { viewModel.deleteBodyMeasurement(it.id) },
        onClearError = { viewModel.clearError() }
    )
}

@Composable
private fun SettingsScreen(
    userProfile: UserProfile?,
    measurementList: List<BodyMeasurement>,
    error: String?,
    height: String,
    weight: String,
    bodyFat: String,
    onGenderChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onBodyFatChange: (String) -> Unit,
    onSave: () -> Unit,
    onMeasurementDelete: (BodyMeasurement) -> Unit,
    onClearError: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // 為主要項目之間新增間距
    ) {
        // 個人資料區塊
        item {
            ProfileCard(
                gender = userProfile?.gender ?: "",
                birthDate = userProfile?.birthDate ?: "",
                onGenderSelected = onGenderChange,
                onBirthDateSelected = onBirthDateChange
            )
        }

        // 輸入區塊
        item {
            MeasurementInputCard(
                height = height,
                weight = weight,
                bodyFat = bodyFat,
                onHeightChange = onHeightChange,
                onWeightChange = onWeightChange,
                onBodyFatChange = onBodyFatChange,
                onSave = onSave
            )
        }

        // 歷史記錄列表
        item {
            Text("歷史記錄", style = MaterialTheme.typography.titleLarge)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        if (measurementList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("尚未有任何記錄。")
                }
            }
        } else {
            items(measurementList, key = { it.id }) { measurement ->
                MeasurementItem(
                    measurement = measurement,
                    onDelete = onMeasurementDelete
                )
            }
        }
    }

    // 錯誤提示
    error?.let {
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("錯誤") },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = onClearError) {
                    Text("確定")
                }
            }
        )
    }
}

@Composable
private fun ProfileCard(
    gender: String,
    birthDate: String,
    onGenderSelected: (String) -> Unit,
    onBirthDateSelected: (String) -> Unit,
) {
    val genderOptions = listOf(
        "male" to R.string.settings_gender_male,
        "female" to R.string.settings_gender_female,
        "other" to R.string.settings_gender_other
    )
    val context = LocalContext.current
    val calender = Calendar.getInstance()

    // Date Picker Dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calender.set(year, month, dayOfMonth)
            onBirthDateSelected(
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(calender.time)
            )
        },
        calender.get(Calendar.YEAR),
        calender.get(Calendar.MONTH),
        calender.get(Calendar.DAY_OF_MONTH)
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                stringResource(R.string.settings_profile_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.padding(16.dp))

            // Gender Selection
            Text(
                stringResource(R.string.settings_gender_title),
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                genderOptions.forEach { (key, stringResId) ->
                    Row(
                        modifier =
                            Modifier
                                .selectable(
                                    selected = key == gender,
                                    onClick = { onGenderSelected(key) }
                                )
                                .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (key == gender),
                            onClick = { onGenderSelected(key) }
                        )
                        Text(
                            text = stringResource(stringResId),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                stringResource(R.string.settings_birthdate_title),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { datePickerDialog.show() }
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "選擇日期",
                    modifier = Modifier.size(
                        ButtonDefaults.IconSize
                    )
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSize))
                Text(birthDate.ifBlank { "請選擇日期" })
            }
        }
    }
}

@Composable
private fun MeasurementInputCard(
    height: String,
    weight: String,
    bodyFat: String,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onBodyFatChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("記錄目前身體數據", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = height,
                onValueChange = onHeightChange,
                label = { Text("身高 (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text("體重 (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bodyFat,
                onValueChange = onBodyFatChange,
                label = { Text("體脂肪率 (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("儲存")
            }
        }
    }
}

@Composable
private fun MeasurementItem(
    measurement: BodyMeasurement,
    onDelete: (BodyMeasurement) -> Unit
) {
    val formattedDate = measurement.timestamp?.let {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
    } ?: "N/A"

    val bodyFatText = measurement.bodyFat?.let { ", 體脂: $it%" } ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "身高: ${measurement.height} cm, 體重: ${measurement.weight} kg$bodyFatText",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            IconButton(onClick = { onDelete(measurement) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "刪除紀錄",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileCardPreview() {
    WorkoutLogTheme {
        ProfileCard(
            gender = "male",
            birthDate = "1990-01-01",
            onGenderSelected = {},
            onBirthDateSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementInputCardPreview() {
    WorkoutLogTheme {
        MeasurementInputCard(
            height = "170",
            weight = "70",
            bodyFat = "15",
            onHeightChange = {},
            onWeightChange = {},
            onBodyFatChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementItemPreview() {
    WorkoutLogTheme {
        MeasurementItem(
            measurement = BodyMeasurement(
                height = 170.0,
                weight = 70.0,
                bodyFat = 15.0,
                timestamp = Date()
            ),
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    WorkoutLogTheme {
        SettingsScreen(
            userProfile = UserProfile(),
            measurementList = emptyList(),
            error = null,
            onGenderChange = {},
            onBirthDateChange = {},
            height = "170",
            weight = "70",
            bodyFat = "15",
            onHeightChange = {},
            onWeightChange = {},
            onBodyFatChange = {},
            onSave = {},
            onMeasurementDelete = {},
            onClearError = {}
        )
    }
}