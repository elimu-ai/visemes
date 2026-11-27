package ai.elimu.visemes.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LetterSoundSelectorTest {

    private fun candidate(letters: String, usageCount: Int, vararg soundsIpa: String) =
        LetterSoundCandidate(letters, soundsIpa.toList(), usageCount)

    @Test
    fun picksTheMostFrequentLetterSound() {
        val mostFrequent = LetterSoundSelector.mostFrequent(
            listOf(
                candidate("m", 10, "m"),
                candidate("a", 99, "æ"),
                candidate("s", 50, "s")
            )
        )

        assertEquals("a", mostFrequent?.letters)
        assertEquals(listOf("æ"), mostFrequent?.soundsIpa)
    }

    @Test
    fun theFirstOneWinsWhenSeveralAreEquallyFrequent() {
        val mostFrequent = LetterSoundSelector.mostFrequent(
            listOf(
                candidate("s", 50, "s"),
                candidate("a", 50, "æ")
            )
        )

        assertEquals("s", mostFrequent?.letters)
    }

    @Test
    fun keepsAllSoundsOfALetterSound() {
        val mostFrequent = LetterSoundSelector.mostFrequent(
            listOf(
                candidate("x", 99, "k", "s"),
                candidate("a", 10, "æ")
            )
        )

        assertEquals(listOf("k", "s"), mostFrequent?.soundsIpa)
    }

    @Test
    fun ignoresLetterSoundsWithoutLettersOrSounds() {
        val mostFrequent = LetterSoundSelector.mostFrequent(
            listOf(
                candidate("", 999, "x"), // no letters
                candidate("  ", 998, "x"), // blank letters
                candidate("x", 997), // no sounds
                candidate("y", 996, " "), // blank sound
                candidate("a", 5, "æ")
            )
        )

        assertEquals("a", mostFrequent?.letters)
    }

    @Test
    fun byFrequencyPutsTheMostFrequentFirstAndKeepsTheOrderOfEqualOnes() {
        val ordered = LetterSoundSelector.byFrequency(
            listOf(
                candidate("m", 10, "m"),
                candidate("s", 50, "s"),
                candidate("a", 99, "æ"),
                candidate("t", 50, "t"),
                candidate("", 1000, "x") // not usable
            )
        )

        assertEquals(listOf("a", "s", "t", "m"), ordered.map { it.letters })
    }

    @Test
    fun returnsNullWhenThereIsNothingToShow() {
        assertNull(LetterSoundSelector.mostFrequent(emptyList()))
        assertNull(LetterSoundSelector.mostFrequent(listOf(candidate("", 1, "x"))))
    }
}
