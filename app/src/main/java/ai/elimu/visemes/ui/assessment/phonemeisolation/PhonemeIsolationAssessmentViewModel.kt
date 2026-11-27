package ai.elimu.visemes.ui.assessment.phonemeisolation

import ai.elimu.visemes.audio.SoundPlayer
import ai.elimu.visemes.content.LetterSoundCandidate
import ai.elimu.visemes.content.LetterSoundRepository
import ai.elimu.visemes.content.WordRepository
import ai.elimu.visemes.ui.requireLetterSound
import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface PhonemeIsolationUiState {
    data object Loading : PhonemeIsolationUiState

    /** No question can be made for the letter-sound (or the Content Provider could not be read). */
    data object Unavailable : PhonemeIsolationUiState

    /**
     * @param isWordHeard Whether the learner has listened to the word. Only then can they answer.
     * @param selectedOption `null` until the learner has pressed one of the options.
     */
    data class Question(
        val question: PhonemeIsolationQuestion<Bitmap>,
        val isWordHeard: Boolean = false,
        val selectedOption: LetterSoundCandidate? = null
    ) : PhonemeIsolationUiState {
        val isAnswered get() = selectedOption != null
        val isCorrect get() = selectedOption?.let { question.isCorrect(it) }
    }
}

class PhonemeIsolationAssessmentViewModel(
    application: Application,
    savedState: SavedStateHandle
) : AndroidViewModel(application) {

    private val TAG = "PhonemeIsolationAssessmentViewModel"

    /** The letter-sound being assessed. */
    private val target = savedState.requireLetterSound()

    private val _uiState = MutableStateFlow<PhonemeIsolationUiState>(PhonemeIsolationUiState.Loading)
    val uiState: StateFlow<PhonemeIsolationUiState> = _uiState.asStateFlow()

    private val soundPlayer = SoundPlayer(application)

    init {
        loadQuestion()
    }

    private fun loadQuestion() {
        viewModelScope.launch {
            val question = try {
                val letterSounds = LetterSoundRepository.loadAll(getApplication<Application>())
                withContext(Dispatchers.IO) {
                    PhonemeIsolationQuestionFactory.create(
                        target = target,
                        words = WordRepository.loadAll(getApplication<Application>()),
                        letterSounds = letterSounds
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load a question", e)
                null
            }

            Log.i(TAG, "target: $target, word: ${question?.word}")
            _uiState.value = if (question != null) {
                PhonemeIsolationUiState.Question(question)
            } else {
                PhonemeIsolationUiState.Unavailable
            }
        }
    }

    /** Says the word shown in the picture. */
    fun playWord() {
        val state = _uiState.value as? PhonemeIsolationUiState.Question ?: return
        _uiState.value = state.copy(isWordHeard = true)
        soundPlayer.play(state.question.word)
    }

    /**
     * Says the sound of the option, and takes it as the answer. Only the first answer counts;
     * further presses are ignored.
     */
    fun selectOption(option: LetterSoundCandidate) {
        val state = _uiState.value as? PhonemeIsolationUiState.Question ?: return
        if (!state.isWordHeard || state.isAnswered) return

        _uiState.value = state.copy(selectedOption = option)
        Log.i(TAG, "word: \"${state.question.word}\", isCorrect: ${state.question.isCorrect(option)}")
        soundPlayer.play(option.letters)
    }

    fun stopSound() {
        soundPlayer.stop()
    }

    override fun onCleared() {
        soundPlayer.release()
    }
}
