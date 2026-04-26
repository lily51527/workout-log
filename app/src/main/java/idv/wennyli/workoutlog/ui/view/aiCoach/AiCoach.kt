package idv.wennyli.workoutlog.ui.view.aiCoach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import idv.wennyli.workoutlog.R
import idv.wennyli.workoutlog.data.model.AiCoachFeedback
import idv.wennyli.workoutlog.data.model.RecommendedExercise
import idv.wennyli.workoutlog.utils.toRelativeTimeString

@Composable
fun AiCoach(
    viewModel: AiCoachViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AiCoachScreen(
        uiState = uiState,
        onRequestFeedback = { viewModel.getFeedback() }
    )
}

@Composable
private fun AiCoachScreen(
    uiState: AiCoachUiState,
    onRequestFeedback: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (uiState) {
            is AiCoachUiState.Idle -> IdleContent(onRequestFeedback)
            is AiCoachUiState.Loading -> LoadingContent()
            is AiCoachUiState.Success -> FeedbackContent(uiState.feedback, onRequestFeedback)
            is AiCoachUiState.Error -> ErrorContent(uiState.message, onRequestFeedback)
        }
    }
}

@Composable
private fun IdleContent(onRequestFeedback: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.ai_coach_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.ai_coach_idle_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestFeedback) {
            Text(stringResource(R.string.ai_coach_button_get_feedback))
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.ai_coach_loading), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FeedbackContent(feedback: AiCoachFeedback, onRequestFeedback: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.ai_coach_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.ai_coach_last_updated, feedback.generatedAt.toRelativeTimeString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            FeedbackCard(title = stringResource(R.string.ai_coach_card_summary)) {
                Text(feedback.summary, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (feedback.reasoning.isNotBlank()) {
            item {
                FeedbackCard(title = stringResource(R.string.ai_coach_card_reasoning)) {
                    Text(feedback.reasoning, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (feedback.warnings.isNotEmpty()) {
            item {
                FeedbackCard(title = stringResource(R.string.ai_coach_card_warnings)) {
                    feedback.warnings.forEach { warning ->
                        Text("• $warning", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        if (feedback.recommendedExercises.isNotEmpty()) {
            item {
                Text(stringResource(R.string.ai_coach_section_recommended), style = MaterialTheme.typography.titleMedium)
            }
            items(feedback.recommendedExercises) { exercise ->
                ExerciseCard(exercise)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRequestFeedback,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.ai_coach_button_reanalyze))
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.ai_coach_error_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.ai_coach_button_retry))
        }
    }
}

@Composable
private fun FeedbackCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ExerciseCard(exercise: RecommendedExercise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(exercise.exercise, style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.ai_coach_muscle_group, exercise.muscleGroup),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (exercise.intensitySuggestion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(exercise.intensitySuggestion, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
