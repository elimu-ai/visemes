package ai.elimu.visemes.ui.assessment.phonemeisolation

import ai.elimu.visemes.content.LetterSoundCandidate
import ai.elimu.visemes.content.WordCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhonemeIsolationQuestionFactoryTest {

    private val c = LetterSoundCandidate("c", listOf("k"), 50)
    private val k = LetterSoundCandidate("k", listOf("k"), 20)
    private val a = LetterSoundCandidate("a", listOf("æ"), 90)
    private val t = LetterSoundCandidate("t", listOf("t"), 80)
    private val o = LetterSoundCandidate("o", listOf("ɒ"), 40)
    private val x = LetterSoundCandidate("x", listOf("k", "s"), 100)
    private val all = listOf(c, k, a, t, o, x)

    private fun word(
        text: String,
        vararg letterSounds: LetterSoundCandidate,
        image: String? = "$text.png"
    ) = WordCandidate(text, { letterSounds.toList() }, { image })

    private fun create(vararg words: WordCandidate<String>, max: Int = 10) =
        PhonemeIsolationQuestionFactory.create(words.asSequence(), all, max)

    @Test
    fun asksForTheFirstSoundOfTheWord() {
        val question = create(word("cat", c, a, t)).single()

        assertEquals("cat", question.word)
        assertEquals("cat.png", question.image)
        assertEquals(c, question.target)
        assertEquals(2, question.options.size)
        assertEquals(1, question.options.count { question.isCorrect(it) })
    }

    @Test
    fun theDistractorIsNotHeardInTheWord() {
        repeat(50) {
            val question = create(word("cat", c, a, t)).single()
            // Not /k/ (also spelled "k"), /æ/ or /t/, and not "x" which has two sounds
            assertEquals(o, question.options.single { !question.isCorrect(it) })
        }
    }

    @Test
    fun usesTheFirstWordOfEachLetterSound() {
        val questions = create(word("cat", c, a, t), word("cot", c, o, t), word("top", t, o))

        assertEquals(listOf("top", "cat"), questions.map { it.word })
    }

    @Test
    fun skipsWordsWithoutAPicture() {
        val questions = create(word("cat", c, a, t, image = null), word("cot", c, o, t))

        assertEquals(listOf("cot"), questions.map { it.word })
    }

    @Test
    fun skipsWordsThatDoNotStartWithASinglePhoneme() {
        assertTrue(create(word("xa", x, a), word("empty")).isEmpty())
    }

    @Test
    fun skipsAWordIfThereIsNoDistractor() {
        val words = sequenceOf(word("cat", c, a, t))

        assertTrue(PhonemeIsolationQuestionFactory.create(words, listOf(c, k, a, t)).isEmpty())
    }

    @Test
    fun stopsReadingWordsWhenThereAreEnoughQuestions() {
        val unread = WordCandidate<String>("top", { error("should not be read") }, { null })

        assertEquals(1, create(word("cat", c, a, t), unread, max = 1).size)
    }
}
