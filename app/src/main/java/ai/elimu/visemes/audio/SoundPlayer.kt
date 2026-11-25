package ai.elimu.visemes.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

/**
 * What the sound player is doing.
 *
 * @param playId Starts at `0` and goes up by one every time a sound is started. A change tells the
 * UI that a new sound has been started (even if the previous one was still playing).
 * @param isPlaying Whether the sound is playing right now.
 */
data class PlaybackState(
    val playId: Int = 0,
    val isPlaying: Boolean = false
)

/**
 * Plays a sound, and tells when it starts and finishes, so that the lips can follow along.
 *
 * The sound is spoken by the device's text-to-speech engine. Just like in
 * [Herufi](https://github.com/elimu-ai/herufi), the sound is made by saying the letter(s).
 *
 * If there is no text-to-speech engine (or it fails), nothing is heard, but [state] still reports
 * "playing" for a moment so that the mouth animation can be seen.
 *
 * Create it on the main thread, and call [release] when it is no longer needed.
 */
class SoundPlayer(context: Context) {

    private val TAG = "SoundPlayer"

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var textToSpeech: TextToSpeech? = null

    @Volatile
    private var isReady = false

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status -> onInit(status) }
    }

    private fun onInit(status: Int) {
        val engine = textToSpeech ?: return
        if (status != TextToSpeech.SUCCESS) {
            Log.w(TAG, "Text-to-speech could not be initialized. status: $status")
            return
        }

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // Already marked as playing when it was requested
            }

            override fun onDone(utteranceId: String?) {
                markFinished(utteranceId)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                markFinished(utteranceId)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.w(TAG, "onError. errorCode: $errorCode")
                markFinished(utteranceId)
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                markFinished(utteranceId)
            }
        })
        isReady = true
    }

    /**
     * Says [text], replacing any sound that is already playing.
     */
    fun play(text: String) {
        // Marked as playing right away (and not when the engine reports that it has started), so
        // that the UI never sees a sound that was requested but is not "playing".
        val playId = _state.updateAndGet {
            PlaybackState(playId = it.playId + 1, isPlaying = true)
        }.playId
        mainHandler.removeCallbacksAndMessages(null)

        val engine = textToSpeech
        val result = if (isReady && engine != null) {
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceIdOf(playId))
        } else {
            TextToSpeech.ERROR
        }
        if (result != TextToSpeech.SUCCESS) {
            Log.w(TAG, "Could not speak. Showing the animation without sound.")
            mainHandler.postDelayed({ markFinished(utteranceIdOf(playId)) }, SILENT_DURATION_MS)
        }
    }

    /** Stops the sound that is playing (if any). */
    fun stop() {
        mainHandler.removeCallbacksAndMessages(null)
        textToSpeech?.stop()
        _state.update { it.copy(isPlaying = false) }
    }

    fun release() {
        stop()
        isReady = false
        textToSpeech?.shutdown()
        textToSpeech = null
    }

    /**
     * Only the latest sound counts: a sound that was replaced by a new one is ignored when it
     * reports that it has been stopped.
     */
    private fun markFinished(utteranceId: String?) {
        _state.update {
            if (utteranceId == utteranceIdOf(it.playId)) it.copy(isPlaying = false) else it
        }
    }

    private fun utteranceIdOf(playId: Int) = "viseme_$playId"

    private companion object {
        /** About as long as it takes to say a letter-sound. */
        const val SILENT_DURATION_MS = 1200L
    }
}
