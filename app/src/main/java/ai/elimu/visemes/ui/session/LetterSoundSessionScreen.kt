package ai.elimu.visemes.ui.session

import ai.elimu.visemes.R
import ai.elimu.visemes.ui.Centered
import ai.elimu.visemes.ui.VisemeColorScheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * What is seen before the first task is opened. In between tasks, it is only a plain background.
 */
@Composable
fun LetterSoundSessionScreen(uiState: LetterSoundSessionUiState) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (uiState) {
            LetterSoundSessionUiState.Loading -> Centered {
                CircularProgressIndicator()
            }

            LetterSoundSessionUiState.Unavailable -> Centered {
                Text(
                    text = stringResource(R.string.letter_sound_unavailable),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }

            is LetterSoundSessionUiState.Active, LetterSoundSessionUiState.Finished -> Unit
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun UnavailablePreview() {
    MaterialTheme(colorScheme = VisemeColorScheme) {
        LetterSoundSessionScreen(uiState = LetterSoundSessionUiState.Unavailable)
    }
}
