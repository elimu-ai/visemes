package ai.elimu.visemes.ui.lips

import ai.elimu.visemes.audio.PlaybackState
import ai.elimu.visemes.viseme.LipShape
import ai.elimu.visemes.viseme.Viseme
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/** How long the mouth takes to move from one shape to the next. */
private const val TRANSITION_MS = 220

/** How long each mouth shape is held, so that it can be seen even when the sound is very short. */
private const val HOLD_MS = 280L

/**
 * The [Lips], animated to go with a sound.
 *
 * Every time a sound is started ([PlaybackState.playId] changes), the mouth moves from its current
 * shape to the shape of the sound (the [visemes]; one after the other if the sound is made up of
 * several), and holds that shape for as long as the sound is playing ([PlaybackState.isPlaying]).
 * When the sound is over, the mouth moves back to the neutral shape.
 */
@Composable
fun AnimatedLips(
    visemes: List<Viseme>,
    playback: PlaybackState,
    modifier: Modifier = Modifier
) {
    val animator = remember { LipAnimator() }
    val latestPlayback by rememberUpdatedState(playback)

    LaunchedEffect(playback.playId, visemes) {
        // Nothing to animate until a sound is started. (A sound that has already finished, e.g.
        // when the screen is rotated, is not played again.) The mouth only needs to settle back to
        // neutral, in case this effect cut a movement short (e.g. when going to the next sound).
        if (playback.playId == 0 || !playback.isPlaying) {
            animator.moveTo(LipShape.Neutral, TRANSITION_MS)
            return@LaunchedEffect
        }

        // Neutral -> the sound (one mouth shape at a time) ...
        for (viseme in visemes) {
            animator.moveTo(viseme.shape, TRANSITION_MS)
            delay(HOLD_MS)
        }
        // ... held until the sound is over ...
        snapshotFlow { latestPlayback.isPlaying }.first { isPlaying -> !isPlaying }
        // ... and back to neutral.
        animator.moveTo(LipShape.Neutral, TRANSITION_MS)
    }

    Canvas(modifier = modifier) {
        drawLips(animator.shape)
    }
}

/**
 * Keeps track of the shape of the mouth while it moves from one shape to another.
 */
@Stable
private class LipAnimator {

    private var from by mutableStateOf(LipShape.Neutral)
    private var to by mutableStateOf(LipShape.Neutral)
    private val progress = Animatable(1f)

    /** The mouth as it should be drawn right now. */
    val shape: LipShape
        get() = from.interpolate(to, progress.value)

    /**
     * Moves the mouth to [target]. It starts from wherever the mouth is right now, so that a
     * movement that is interrupted by a new one does not make the mouth jump.
     */
    suspend fun moveTo(target: LipShape, durationMillis: Int) {
        from = shape
        to = target
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis, easing = FastOutSlowInEasing))
    }
}
