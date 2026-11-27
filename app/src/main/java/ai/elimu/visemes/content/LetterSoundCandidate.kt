package ai.elimu.visemes.content

/**
 * A letter-sound correspondence reduced to what this app needs.
 *
 * @param letters The letter(s), e.g. `"a"` or `"ch"`.
 * @param soundsIpa The sound(s) that the letter(s) make, in IPA, e.g. `["æ"]`. Some correspondences
 * have more than one sound (e.g. the letter "x" makes /k/ followed by /s/).
 * @param usageCount How often the letter-sound is used in the content. The most frequent one is
 * the one that gets shown.
 */
data class LetterSoundCandidate(
    val letters: String,
    val soundsIpa: List<String>,
    val usageCount: Int
)

object LetterSoundSelector {

    /**
     * The usable letter-sounds, with the most frequent ([LetterSoundCandidate.usageCount]) first.
     * Equally frequent letter-sounds keep their original order.
     *
     * Entries without letters, or without sounds, are left out since there is nothing to show for
     * them.
     */
    fun byFrequency(candidates: List<LetterSoundCandidate>): List<LetterSoundCandidate> {
        return candidates
            .filter { it.letters.isNotBlank() }
            .filter { it.soundsIpa.isNotEmpty() && it.soundsIpa.all { sound -> sound.isNotBlank() } }
            .sortedByDescending { it.usageCount }
    }

    /**
     * The letter-sound with the highest [LetterSoundCandidate.usageCount]. If several are equally
     * frequent, the first one wins.
     *
     * @return `null` if there is no usable letter-sound.
     */
    fun mostFrequent(candidates: List<LetterSoundCandidate>): LetterSoundCandidate? {
        return byFrequency(candidates).firstOrNull()
    }
}
