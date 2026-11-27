package ai.elimu.visemes.ui.session

import ai.elimu.visemes.content.LetterSoundCandidate
import ai.elimu.visemes.content.LetterSoundRepository
import ai.elimu.visemes.content.LetterSoundSelector
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LetterSoundSessionUiState {
    data object Loading : LetterSoundSessionUiState

    /** The Content Provider could not be read, or it has no letter-sounds to show. */
    data object Unavailable : LetterSoundSessionUiState

    /** The activity of [step] is to be shown. */
    data class Active(val position: Int, val step: SessionStep) : LetterSoundSessionUiState

    /** The student has been through all letter-sounds. */
    data object Finished : LetterSoundSessionUiState
}

class LetterSoundSessionViewModel(
    application: Application,
    private val savedState: SavedStateHandle
) : AndroidViewModel(application) {

    private val TAG = "LetterSoundSessionViewModel"

    private val _uiState = MutableStateFlow<LetterSoundSessionUiState>(
        LetterSoundSessionUiState.Loading
    )
    val uiState: StateFlow<LetterSoundSessionUiState> = _uiState.asStateFlow()

    /** The letter-sounds to go through, the most frequent one first. `null` while loading. */
    private var letterSounds: List<LetterSoundCandidate>? = null

    /**
     * How far the student has come (see [SessionStep.at]). Saved, since the system may remove
     * this app from memory while the activity of a step is open.
     */
    private var position: Int
        get() = savedState[KEY_POSITION] ?: 0
        set(value) {
            savedState[KEY_POSITION] = value
        }

    /** Whether the activity of the current step has been opened, and has not finished yet. */
    var isStepOpen: Boolean
        get() = savedState[KEY_IS_STEP_OPEN] ?: false
        set(value) {
            savedState[KEY_IS_STEP_OPEN] = value
        }

    init {
        loadLetterSounds()
    }

    private fun loadLetterSounds() {
        viewModelScope.launch {
            letterSounds = try {
                val candidates = LetterSoundRepository.loadAll(getApplication<Application>())
                LetterSoundSelector.byFrequency(candidates)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load letter-sounds", e)
                emptyList()
            }
            Log.i(TAG, "letterSounds.size: ${letterSounds?.size}")
            showCurrentStep()
        }
    }

    /** Goes on to the next step. */
    fun completeStep() {
        isStepOpen = false
        position++
        showCurrentStep()
    }

    private fun showCurrentStep() {
        // If still loading, the current step is shown once that is done
        val items = letterSounds ?: return
        val step = SessionStep.at(items, position)
        _uiState.value = when {
            items.isEmpty() -> LetterSoundSessionUiState.Unavailable
            step == null -> LetterSoundSessionUiState.Finished
            else -> LetterSoundSessionUiState.Active(position, step)
        }
    }

    private companion object {
        const val KEY_POSITION = "position"
        const val KEY_IS_STEP_OPEN = "isStepOpen"
    }
}
