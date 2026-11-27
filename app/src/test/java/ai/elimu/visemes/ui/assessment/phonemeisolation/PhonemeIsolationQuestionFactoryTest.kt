package ai.elimu.visemes.ui.assessment.phonemeisolation

import ai.elimu.visemes.content.LetterSoundCandidate
import ai.elimu.visemes.content.WordCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    private fun create(vararg words: WordCandidate<String>, target: LetterSoundCandidate = c) =
        PhonemeIsolationQuestionFactory.create(target, words.asSequence(), all)

    @Test
    fun asksForTheFirstSoundOfTheWord() {
        val question = create(word("cat", c, a, t))!!

        assertEquals("cat", question.word)
        assertEquals("cat.png", question.image)
        assertEquals(c, question.target)
        assertEquals(2, question.options.size)
        assertEquals(1, question.options.count { question.isCorrect(it) })
    }

    @Test
    fun theDistractorIsNotHeardInTheWord() {
        repeat(50) {
            val question = create(word("cat", c, a, t))!!
            // Not /k/ (also spelled "k"), /æ/ or /t/, and not "x" which has two sounds
            assertEquals(o, question.options.single { !question.isCorrect(it) })
        }
    }

    @Test
    fun usesTheFirstWordThatStartsWithTheLetterSound() {
        val question = create(word("top", t, o), word("cat", c, a, t), word("cot", c, o, t))

        assertEquals("cat", question?.word)
    }

    @Test
    fun skipsWordsThatStartWithAnotherSoundOfTheSameLetters() {
        val softC = LetterSoundCandidate("c", listOf("s"), 10)

        val question = create(word("cell", softC), word("cat", c, a, t))

        assertEquals("cat", question?.word)
    }

    @Test
    fun doesNotReadTheLetterSoundsOfWordsWithOtherFirstLetters() {
        val unread = WordCandidate<String>("top", { error("should not be read") }, { null })

        assertEquals("cat", create(unread, word("cat", c, a, t))?.word)
    }

    @Test
    fun skipsWordsWithoutAPicture() {
        val question = create(word("cat", c, a, t, image = null), word("cot", c, o, t))

        assertEquals("cot", question?.word)
    }

    @Test
    fun hasNoQuestionForALetterSoundWithSeveralSounds() {
        assertNull(create(word("xa", x, a), target = x))
    }

    @Test
    fun hasNoQuestionIfNoWordStartsWithTheLetterSound() {
        assertNull(create(word("top", t, o)))
    }

    @Test
    fun hasNoQuestionIfThereIsNoDistractor() {
        val words = sequenceOf(word("cat", c, a, t))

        assertNull(PhonemeIsolationQuestionFactory.create(c, words, listOf(c, k, a, t)))
    }
}
