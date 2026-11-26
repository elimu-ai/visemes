package ai.elimu.visemes.ui

import ai.elimu.visemes.R
import ai.elimu.visemes.audio.PlaybackState
import ai.elimu.visemes.ui.lips.AnimatedLips
import ai.elimu.visemes.viseme.Viseme
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** How long it takes to slide from one letter-sound to the next. */
private const val TASK_TRANSITION_MS = 450

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
    modifier: Modifier = Modifier,
    /** Whether to show the arrow for going to the next sound (after the sound has been played). */
    showNext: Boolean = false,
    onNextSound: () -> Unit = {}
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = uiState,
                // Only a different letter-sound is a new "task". (Not e.g. `hasNext` changing.)
                contentKey = { state ->
                    if (state is LetterSoundVisemeUiState.Loaded) {
                        "loaded:${state.letters}:${state.soundsIpa}"
                    } else {
                        state.toString()
                    }
                },
                transitionSpec = {
                    if (initialState is LetterSoundVisemeUiState.Loaded &&
                        targetState is LetterSoundVisemeUiState.Loaded
                    ) {
                        // From one task to the next: the current one slides away to the left, and
                        // the next one comes in from the right
                        (slideInHorizontally(tween(TASK_TRANSITION_MS)) { fullWidth -> fullWidth } +
                            fadeIn(tween(TASK_TRANSITION_MS))) togetherWith
                            (slideOutHorizontally(tween(TASK_TRANSITION_MS)) { fullWidth -> -fullWidth } +
                                fadeOut(tween(TASK_TRANSITION_MS)))
                    } else {
                        fadeIn() togetherWith fadeOut()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                label = "letterSoundTransition"
            ) { state ->
                when (state) {
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
                        state = state,
                        playback = playback,
                        onPlaySound = onPlaySound
                    )
                }
            }

            // In the bottom right corner of the screen
            AnimatedVisibility(
                visible = showNext,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .safeDrawingPadding()
                    // The edge below the button is part of the button (see below), so it takes up
                    // that much of the bottom margin. The button itself stays where it is.
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 24.dp - RaisedEdgeHeight)
            ) {
                // The darker edge sticks out below the button. While this fades in or out, it is
                // drawn in a layer that is cut off at the edge of the content, so the edge has to
                // be inside it. Otherwise the bottom of the button is cut off.
                Box(modifier = Modifier.padding(bottom = RaisedEdgeHeight)) {
                    NextSoundButton(onClick = onNextSound)
                }
            }
        }
    }
}

/** A round button with an arrow pointing to the right, for going on to the next sound. */
@Composable
private fun NextSoundButton(onClick: () -> Unit) {
    val description = stringResource(R.string.next_sound)
    RaisedSurface(
        faceColor = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .semantics { contentDescription = description }
    ) {
        val arrowColor = MaterialTheme.colorScheme.onPrimary
        Canvas(modifier = Modifier.size(28.dp)) {
            val strokeWidth = 3.dp.toPx()
            val tail = Offset(size.width * 0.1f, size.height / 2f)
            val tip = Offset(size.width * 0.9f, size.height / 2f)
            val headSize = size.width * 0.38f

            drawLine(arrowColor, tail, tip, strokeWidth, StrokeCap.Round)
            drawLine(arrowColor, tip, Offset(tip.x - headSize, tip.y - headSize), strokeWidth, StrokeCap.Round)
            drawLine(arrowColor, tip, Offset(tip.x - headSize, tip.y + headSize), strokeWidth, StrokeCap.Round)
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
            // Extra room at the bottom, so that the arrow for the next sound (in the corner) never
            // covers the mouth or the button
            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 24.dp + 56.dp)
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
    RaisedSurface(
        faceColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        modifier = Modifier
            .height(56.dp)
            .widthIn(min = 200.dp)
    ) {
        Text(
            text = stringResource(R.string.play_sound),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
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

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun NextSoundPreview() {
    MaterialTheme(colorScheme = VisemeColorScheme) {
        LetterSoundVisemeScreen(
            uiState = LetterSoundVisemeUiState.Loaded(
                letters = "a",
                soundsIpa = listOf("æ"),
                visemes = listOf(Viseme.NEAR_OPEN_FRONT),
                hasNext = true
            ),
            playback = PlaybackState(playId = 1, isPlaying = false),
            onPlaySound = {},
            showNext = true,
            onNextSound = {}
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
