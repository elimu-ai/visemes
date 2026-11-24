package ai.elimu.visemes.ui

import ai.elimu.visemes.R
import ai.elimu.visemes.audio.PlaybackState
import ai.elimu.visemes.ui.lips.AnimatedLips
import ai.elimu.visemes.viseme.Viseme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Purple = Color(0xFF7B3FA0)

/** The "skin" behind the lips. It makes the purple lips easy to see. */
private val FaceColor = Color(0xFFFFE0C7)

/** Flat purple-on-off-white look. */
internal val VisemeColorScheme = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    background = Color(0xFFFAF7FC),
    surface = Color(0xFFFAF7FC)
)

@Composable
fun LetterSoundVisemeScreen(
    uiState: LetterSoundVisemeUiState,
    playback: PlaybackState,
    onPlaySound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (uiState) {
            LetterSoundVisemeUiState.Loading -> Centered {
                CircularProgressIndicator()
            }

            LetterSoundVisemeUiState.Unavailable -> Centered {
                Text(
                    text = stringResource(R.string.letter_sound_unavailable),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }

            is LetterSoundVisemeUiState.Loaded -> LoadedContent(
                state = uiState,
                playback = playback,
                onPlaySound = onPlaySound
            )
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun LoadedContent(
    state: LetterSoundVisemeUiState.Loaded,
    playback: PlaybackState,
    onPlaySound: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        if (maxWidth > maxHeight) {
            // Landscape: the sound and the button on the left, the mouth on the right
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SoundLabel(state)
                    Spacer(Modifier.height(24.dp))
                    PlaySoundButton(onPlaySound)
                }
                MouthPanel(
                    state = state,
                    playback = playback,
                    onPlaySound = onPlaySound,
                    modifier = Modifier.weight(1.2f)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SoundLabel(state)
                Spacer(Modifier.height(32.dp))
                MouthPanel(
                    state = state,
                    playback = playback,
                    onPlaySound = onPlaySound,
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(32.dp))
                PlaySoundButton(onPlaySound)
            }
        }
    }
}

/** The sound(s) in IPA (e.g. /æ/), and the letter(s) that make the sound (e.g. "a"). */
@Composable
private fun SoundLabel(state: LetterSoundVisemeUiState.Loaded) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = state.soundsIpa.joinToString(" ") { "/$it/" },
            color = MaterialTheme.colorScheme.primary,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "\"${state.letters}\"",
            fontSize = 40.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** The animated mouth. Pressing it plays the sound. */
@Composable
private fun MouthPanel(
    state: LetterSoundVisemeUiState.Loaded,
    playback: PlaybackState,
    onPlaySound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.lips_description)
    Box(
        modifier = modifier
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(32.dp))
            .background(FaceColor)
            .clickable(role = Role.Button, onClick = onPlaySound)
            .semantics { contentDescription = description }
    ) {
        AnimatedLips(
            visemes = state.visemes,
            playback = playback,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PlaySoundButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(56.dp)
            .widthIn(min = 200.dp)
    ) {
        Text(
            text = stringResource(R.string.play_sound),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun LoadedPreview() {
    MaterialTheme(colorScheme = VisemeColorScheme) {
        LetterSoundVisemeScreen(
            uiState = LetterSoundVisemeUiState.Loaded(
                letters = "a",
                soundsIpa = listOf("æ"),
                visemes = listOf(Viseme.NEAR_OPEN_FRONT)
            ),
            playback = PlaybackState(),
            onPlaySound = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 700, heightDp = 360)
@Composable
private fun LoadedLandscapePreview() {
    MaterialTheme(colorScheme = VisemeColorScheme) {
        LetterSoundVisemeScreen(
            uiState = LetterSoundVisemeUiState.Loaded(
                letters = "x",
                soundsIpa = listOf("k", "s"),
                visemes = listOf(Viseme.VELAR, Viseme.SIBILANT)
            ),
            playback = PlaybackState(),
            onPlaySound = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun UnavailablePreview() {
    MaterialTheme(colorScheme = VisemeColorScheme) {
        LetterSoundVisemeScreen(
            uiState = LetterSoundVisemeUiState.Unavailable,
            playback = PlaybackState(),
            onPlaySound = {}
        )
    }
}
