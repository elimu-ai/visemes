package ai.elimu.visemes.ui.learning.lettersoundviseme

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * Shows the sound(s) of the most frequent letter-sound, together with an animated mouth (visemes)
 * which shows how the lips and tongue move to make the sound.
 *
 * The letter-sounds are loaded from the elimu.ai Content Provider.
 */
class LetterSoundVisemeActivity : ComponentActivity() {

    private val TAG = "LetterSoundVisemeActivity"

    private val viewModel: LetterSoundVisemeViewModel by viewModels()

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
            val playback by viewModel.playback.collectAsState()
            val showNext by viewModel.showNext.collectAsState()

            MaterialTheme(colorScheme = VisemeColorScheme) {
                LetterSoundVisemeScreen(
                    uiState = uiState,
                    playback = playback,
                    showNext = showNext,
                    onPlaySound = viewModel::playSound,
                    onNextSound = viewModel::nextSound
                )
            }
        }
    }

    override fun onStop() {
        Log.i(TAG, "onStop")
        // Don't keep talking when the app is no longer visible
        viewModel.stopSound()
        super.onStop()
    }
}
