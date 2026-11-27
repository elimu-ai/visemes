package ai.elimu.visemes.ui.assessment.phonemeisolation

import ai.elimu.visemes.ui.VisemeColorScheme
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * Assesses phoneme isolation: the learner sees a picture of a word, and picks which of two sounds
 * the word starts with.
 *
 * Which letter-sound to assess is given by the caller (see `Intent.putLetterSound`). The words,
 * pictures and letter-sounds are loaded from the elimu.ai Content Provider.
 *
 * The activity finishes with [RESULT_OK] when the learner presses the arrow after answering, or
 * right away if no question can be made for the letter-sound.
 */
class PhonemeIsolationAssessmentActivity : ComponentActivity() {

    private val TAG = "PhonemeIsolationAssessmentActivity"

    private val viewModel: PhonemeIsolationAssessmentViewModel by viewModels()

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

            // Nothing to ask about this letter-sound, so move on
            LaunchedEffect(uiState) {
                if (uiState == PhonemeIsolationUiState.Unavailable) complete()
            }

            MaterialTheme(colorScheme = VisemeColorScheme) {
                PhonemeIsolationAssessmentScreen(
                    uiState = uiState,
                    onPlayWord = viewModel::playWord,
                    onOptionSelected = viewModel::selectOption,
                    onNext = ::complete
                )
            }
        }
    }

    private fun complete() {
        setResult(RESULT_OK)
        finish()
    }

    override fun onStop() {
        Log.i(TAG, "onStop")
        // Don't keep talking when the activity is no longer visible
        viewModel.stopSound()
        super.onStop()
    }
}
