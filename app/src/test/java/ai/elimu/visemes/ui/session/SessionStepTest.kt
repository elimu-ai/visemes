package ai.elimu.visemes.ui.session

import ai.elimu.visemes.content.LetterSoundCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionStepTest {

    private val a = LetterSoundCandidate("a", listOf("æ"), 2)
    private val s = LetterSoundCandidate("s", listOf("s"), 1)

    @Test
    fun showsLearningAndAssessmentBeforeTheNextLetterSound() {
        val steps = (0..3).map { SessionStep.at(listOf(a, s), it) }

        assertEquals(
            listOf(
                SessionStep(a, LetterSoundTask.VISEME_LEARNING),
                SessionStep(a, LetterSoundTask.PHONEME_ISOLATION_ASSESSMENT),
                SessionStep(s, LetterSoundTask.VISEME_LEARNING),
                SessionStep(s, LetterSoundTask.PHONEME_ISOLATION_ASSESSMENT)
            ),
            steps
        )
    }

    @Test
    fun endsAfterTheLastTaskOfTheLastLetterSound() {
        assertNull(SessionStep.at(listOf(a, s), 4))
        assertNull(SessionStep.at(emptyList(), 0))
    }
}
