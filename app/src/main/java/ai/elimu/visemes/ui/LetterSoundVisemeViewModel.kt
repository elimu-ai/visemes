package ai.elimu.visemes.ui

import ai.elimu.visemes.audio.PlaybackState
import ai.elimu.visemes.audio.SoundPlayer
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val visemes: List<Viseme>
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

    init {
        loadMostFrequentLetterSound()
    }

    private fun loadMostFrequentLetterSound() {
        viewModelScope.launch {
            val mostFrequent = try {
                val candidates = LetterSoundRepository.loadAll(getApplication<Application>())
                LetterSoundSelector.mostFrequent(candidates)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load letter-sounds", e)
                null
            }

            _uiState.value = if (mostFrequent != null) {
                Log.i(TAG, "mostFrequent: $mostFrequent")
                LetterSoundVisemeUiState.Loaded(
                    letters = mostFrequent.letters,
                    soundsIpa = mostFrequent.soundsIpa,
                    visemes = VisemeMapper.fromSounds(mostFrequent.soundsIpa)
                )
            } else {
                LetterSoundVisemeUiState.Unavailable
            }
        }
    }

    fun playSound() {
        val state = _uiState.value
        if (state is LetterSoundVisemeUiState.Loaded) {
            soundPlayer.play(state.letters)
        }
    }

    fun stopSound() {
        soundPlayer.stop()
    }

    override fun onCleared() {
        soundPlayer.release()
    }
}
