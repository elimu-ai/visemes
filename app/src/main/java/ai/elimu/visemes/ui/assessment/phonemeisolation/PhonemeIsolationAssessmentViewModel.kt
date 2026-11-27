package ai.elimu.visemes.ui.assessment.phonemeisolation

import ai.elimu.visemes.audio.SoundPlayer
import ai.elimu.visemes.content.LetterSoundCandidate
import ai.elimu.visemes.content.LetterSoundRepository
import ai.elimu.visemes.content.WordRepository
import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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

    /** The Content Provider could not be read, or no question can be made from its content. */
    data object Unavailable : PhonemeIsolationUiState

    /**
     * @param index The position of the question in the session.
     * @param isWordHeard Whether the learner has listened to the word. Only then can they answer.
     * @param selectedOption `null` until the learner has pressed one of the options.
     * @param hasNext Whether there is another question after this one.
     */
    data class Question(
        val question: PhonemeIsolationQuestion<Bitmap>,
        val index: Int = 0,
        val isWordHeard: Boolean = false,
        val selectedOption: LetterSoundCandidate? = null,
        val hasNext: Boolean = false
    ) : PhonemeIsolationUiState {
        val isAnswered get() = selectedOption != null
        val isCorrect get() = selectedOption?.let { question.isCorrect(it) }
    }
}

class PhonemeIsolationAssessmentViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "PhonemeIsolationAssessmentViewModel"

    private val _uiState = MutableStateFlow<PhonemeIsolationUiState>(PhonemeIsolationUiState.Loading)
    val uiState: StateFlow<PhonemeIsolationUiState> = _uiState.asStateFlow()

    private val soundPlayer = SoundPlayer(application)

    private var questions: List<PhonemeIsolationQuestion<Bitmap>> = emptyList()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            questions = try {
                val letterSounds = LetterSoundRepository.loadAll(getApplication<Application>())
                withContext(Dispatchers.IO) {
                    PhonemeIsolationQuestionFactory.create(
                        words = WordRepository.loadAll(getApplication<Application>()),
                        letterSounds = letterSounds
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load questions", e)
                emptyList()
            }

            Log.i(TAG, "questions.size: ${questions.size}")
            showQuestion(0)
        }
    }

    private fun showQuestion(index: Int) {
        val question = questions.getOrNull(index)
        _uiState.value = if (question != null) {
            PhonemeIsolationUiState.Question(
                question = question,
                index = index,
                hasNext = index < questions.lastIndex
            )
        } else {
            PhonemeIsolationUiState.Unavailable
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

    /**
     * Goes on to the next question, once the current one has been answered.
     *
     * @return `true` if the last question has been answered, so that the session is over.
     */
    fun next(): Boolean {
        val state = _uiState.value as? PhonemeIsolationUiState.Question ?: return false
        if (!state.isAnswered) return false
        if (!state.hasNext) return true

        soundPlayer.stop()
        showQuestion(state.index + 1)
        return false
    }

    fun stopSound() {
        soundPlayer.stop()
    }

    override fun onCleared() {
        soundPlayer.release()
    }
}
