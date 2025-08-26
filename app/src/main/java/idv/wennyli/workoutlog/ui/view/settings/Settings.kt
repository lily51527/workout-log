package idv.wennyli.workoutlog.ui.view.settings

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun Settings(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val error by viewModel.error.collectAsState()

    SettingsScreen(
        userProfile = userProfile,
        error = error,
        onGenderChange = { viewModel.updateUserProfile(gender = it) },
        onBirthDateChange = { viewModel.updateUserProfile(birthDate = it) },
    )
}

@Composable
private fun SettingsScreen(
    userProfile: UserProfile?,
    error: String?,
    onGenderChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // 個人資料區塊
        ProfileCard(
            gender = userProfile?.gender ?: "",
            birthDate = userProfile?.birthDate ?: "",
            onGenderSelected = onGenderChange,
            onBirthDateSelected = onBirthDateChange
        )
        Spacer(modifier = Modifier.height(16.dp))
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


@Preview(showBackground = true)
@Composable
private fun ProfileCardPreview() {
    ProfileCard(
        gender = "male",
        birthDate = "1990-01-01",
        onGenderSelected = {},
        onBirthDateSelected = {}
    )
}