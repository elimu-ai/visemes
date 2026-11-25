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

/**
 * The letter-sounds to go through, one after the other, and where we are in the list.
 *
 * @param items The letter-sounds, in the order they are shown (see [LetterSoundSelector.byFrequency]).
 * @param index The position of the letter-sound that is shown now.
 */
data class LetterSoundQueue(
    val items: List<LetterSoundCandidate>,
    val index: Int = 0
) {
    /** The letter-sound that is shown now, or `null` if the queue is empty. */
    val current: LetterSoundCandidate? get() = items.getOrNull(index)

    /** Whether there is another letter-sound after the current one. */
    val hasNext: Boolean get() = index < items.lastIndex

    /** The queue moved on to the next letter-sound. Stays as it is after the last one. */
    fun next(): LetterSoundQueue = if (hasNext) copy(index = index + 1) else this
}
