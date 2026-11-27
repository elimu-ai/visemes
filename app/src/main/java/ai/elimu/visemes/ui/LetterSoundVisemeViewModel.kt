package ai.elimu.visemes.ui

import ai.elimu.analytics.utils.LearningEventUtil
import ai.elimu.model.v2.enums.content.LiteracySkill
import ai.elimu.visemes.BuildConfig
import ai.elimu.visemes.audio.PlaybackState
import ai.elimu.visemes.audio.SoundPlayer
import ai.elimu.visemes.content.LetterSoundQueue
import ai.elimu.visemes.content.LetterSoundRepository
import ai.elimu.visemes.content.LetterSoundSelector
import ai.elimu.visemes.viseme.Viseme
import ai.elimu.visemes.viseme.VisemeMapper
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LetterSoundVisemeUiState {
    data object Loading : LetterSoundVisemeUiState

    /** The Content Provider could not be read, or it has no letter-sounds to show. */
    data object Unavailable : LetterSoundVisemeUiState

    /**
     * @param letters The letter(s) of the most frequent letter-sound, e.g. `"a"`.
     * @param soundsIpa The sound(s) of the letter(s), in IPA, e.g. `["æ"]`.
     * @param visemes The mouth shape of each sound, in the same order.
     */
    data class Loaded(
        val letters: String,
        val soundsIpa: List<String>,
        val visemes: List<Viseme>,
        /** Whether there is another letter-sound after this one. */
        val hasNext: Boolean = false
    ) : LetterSoundVisemeUiState
}

class LetterSoundVisemeViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "LetterSoundVisemeViewModel"

    private val _uiState = MutableStateFlow<LetterSoundVisemeUiState>(
        LetterSoundVisemeUiState.Loading
    )
    val uiState: StateFlow<LetterSoundVisemeUiState> = _uiState.asStateFlow()

    private val soundPlayer = SoundPlayer(application)
    val playback: StateFlow<PlaybackState> = soundPlayer.state

    /** The letter-sounds to go through, the most frequent one first. */
    private var queue = LetterSoundQueue(emptyList())

    /**
     * The [PlaybackState.playId] when the current letter-sound was shown. The sound of the current
     * letter-sound has been played once the play id has gone past this one.
     */
    private val playIdAtStart = MutableStateFlow(0)

    /**
     * Whether to show the arrow for going to the next sound: after the sound of the current
     * letter-sound has been played (and is not playing anymore), if there is a next one.
     */
    val showNext: StateFlow<Boolean> = combine(_uiState, playback, playIdAtStart) { ui, pb, startId ->
        ui is LetterSoundVisemeUiState.Loaded &&
            ui.hasNext &&
            pb.playId > startId &&
            !pb.isPlaying
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        loadLetterSounds()
    }

    private fun loadLetterSounds() {
        viewModelScope.launch {
            val items = try {
                val candidates = LetterSoundRepository.loadAll(getApplication<Application>())
                LetterSoundSelector.byFrequency(candidates)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load letter-sounds", e)
                emptyList()
            }

            queue = LetterSoundQueue(items)
            Log.i(TAG, "items.size: ${items.size}, mostFrequent: ${queue.current}")
            showCurrent()
        }
    }

    private fun showCurrent() {
        val current = queue.current
        _uiState.value = if (current != null) {
            LetterSoundVisemeUiState.Loaded(
                letters = current.letters,
                soundsIpa = current.soundsIpa,
                visemes = VisemeMapper.fromSounds(current.soundsIpa),
                hasNext = queue.hasNext
            )
        } else {
            LetterSoundVisemeUiState.Unavailable
        }
    }

    /** Goes on to the next letter-sound (the next most frequent one). */
    fun nextSound() {
        if (!queue.hasNext) return
        // Only after the sound has been played. This also stops a second press of the arrow, while
        // it is fading out, from skipping a letter-sound.
        val playbackNow = playback.value
        if (playbackNow.playId <= playIdAtStart.value || playbackNow.isPlaying) return

        soundPlayer.stop()
        // Counts as "not played yet" for the new letter-sound. Set before the new letter-sound is
        // shown, so that the arrow is never shown for it.
        playIdAtStart.value = playback.value.playId
        queue = queue.next()
        showCurrent()
    }

    fun playSound() {
        val state = _uiState.value
        if (state is LetterSoundVisemeUiState.Loaded) {
            if (soundPlayer.play(state.letters)) {
                reportLearningEvent()
            }
        }
    }

    /** Reports to the Analytics application (https://github.com/elimu-ai/analytics). */
    private fun reportLearningEvent() {
        LearningEventUtil.reportLiteracyLearningEvent(
            literacySkill = LiteracySkill.PHONEMIC_AWARENESS,
            context = getApplication<Application>(),
            analyticsApplicationId = BuildConfig.ANALYTICS_APPLICATION_ID
        )
    }

    fun stopSound() {
        soundPlayer.stop()
    }

    override fun onCleared() {
        soundPlayer.release()
    }
}
