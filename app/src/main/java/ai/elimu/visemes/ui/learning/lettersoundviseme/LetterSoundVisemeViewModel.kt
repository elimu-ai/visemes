package ai.elimu.visemes.ui.learning.lettersoundviseme

import ai.elimu.visemes.audio.PlaybackState
import ai.elimu.visemes.audio.SoundPlayer
import ai.elimu.visemes.ui.requireLetterSound
import ai.elimu.visemes.viseme.Viseme
import ai.elimu.visemes.viseme.VisemeMapper
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * @param letters The letter(s) of the letter-sound, e.g. `"a"`.
 * @param soundsIpa The sound(s) of the letter(s), in IPA, e.g. `["æ"]`.
 * @param visemes The mouth shape of each sound, in the same order.
 */
data class LetterSoundVisemeUiState(
    val letters: String,
    val soundsIpa: List<String>,
    val visemes: List<Viseme>
)

class LetterSoundVisemeViewModel(
    application: Application,
    savedState: SavedStateHandle
) : AndroidViewModel(application) {

    val uiState: LetterSoundVisemeUiState = savedState.requireLetterSound().let {
        LetterSoundVisemeUiState(
            letters = it.letters,
            soundsIpa = it.soundsIpa,
            visemes = VisemeMapper.fromSounds(it.soundsIpa)
        )
    }

    private val soundPlayer = SoundPlayer(application)
    val playback: StateFlow<PlaybackState> = soundPlayer.state

    /**
     * Whether to show the arrow for going on: after the sound has been played (and is not playing
     * anymore).
     */
    val showNext: StateFlow<Boolean> = playback
        .map { it.playId > 0 && !it.isPlaying }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun playSound() {
        soundPlayer.play(uiState.letters)
    }

    fun stopSound() {
        soundPlayer.stop()
    }

    override fun onCleared() {
        soundPlayer.release()
    }
}
