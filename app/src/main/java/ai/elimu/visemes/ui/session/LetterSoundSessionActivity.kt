package ai.elimu.visemes.ui.session

import ai.elimu.visemes.ui.VisemeColorScheme
import ai.elimu.visemes.ui.assessment.phonemeisolation.PhonemeIsolationAssessmentActivity
import ai.elimu.visemes.ui.learning.lettersoundviseme.LetterSoundVisemeActivity
import ai.elimu.visemes.ui.putLetterSound
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * Takes the student through the letter-sounds, the most frequent one first. For each of them, every
 * [LetterSoundTask] is shown (first learning, then assessment) before going on to the next one.
 *
 * Each task is its own activity, which finishes with [RESULT_OK] when the student is done with it.
 * If the student leaves it in another way (e.g. by going back), the session ends.
 */
class LetterSoundSessionActivity : ComponentActivity() {

    private val TAG = "LetterSoundSessionActivity"

    private val viewModel: LetterSoundSessionViewModel by viewModels()

    private val taskLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.i(TAG, "resultCode: ${result.resultCode}")
            if (result.resultCode == RESULT_OK) {
                viewModel.completeStep()
            } else {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        // Draw behind the status and navigation bars. The background is always light, so the
        // bars always get dark icons (also when the device is in dark mode).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState) {
                when (val state = uiState) {
                    is LetterSoundSessionUiState.Active -> open(state.step)
                    LetterSoundSessionUiState.Finished -> finish()
                    else -> Unit
                }
            }

            MaterialTheme(colorScheme = VisemeColorScheme) {
                LetterSoundSessionScreen(uiState = uiState)
            }
        }
    }

    private fun open(step: SessionStep) {
        // E.g. after the screen has been rotated
        if (viewModel.isStepOpen) return
        viewModel.isStepOpen = true

        val activityClass = when (step.task) {
            LetterSoundTask.VISEME_LEARNING -> LetterSoundVisemeActivity::class.java
            LetterSoundTask.PHONEME_ISOLATION_ASSESSMENT ->
                PhonemeIsolationAssessmentActivity::class.java
        }
        Log.i(TAG, "Opening ${activityClass.simpleName} for ${step.letterSound}")
        taskLauncher.launch(Intent(this, activityClass).putLetterSound(step.letterSound))
    }
}
