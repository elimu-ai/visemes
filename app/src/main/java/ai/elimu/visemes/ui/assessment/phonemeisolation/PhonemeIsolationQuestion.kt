package ai.elimu.visemes.ui.assessment.phonemeisolation

import ai.elimu.visemes.content.LetterSoundCandidate
import ai.elimu.visemes.content.WordCandidate
import kotlin.random.Random

/**
 * "Which sound does this word start with?" with a picture of the word, and exactly two options: one
 * correct and one incorrect.
 *
 * @param word The word shown in the picture, e.g. `"cat"`.
 * @param image The picture of the word.
 * @param target The letter-sound being assessed: the first one of the word, e.g. "c" /k/.
 * @param options The [target] and a distractor, in the order they are shown.
 */
data class PhonemeIsolationQuestion<I : Any>(
    val word: String,
    val image: I,
    val target: LetterSoundCandidate,
    val options: List<LetterSoundCandidate>
) {
    fun isCorrect(option: LetterSoundCandidate) = option == target
}

object PhonemeIsolationQuestionFactory {

    /** The distractor is picked from this many of the most frequent sounds that qualify. */
    private const val DISTRACTOR_POOL_SIZE = 5

    /**
     * Builds a question about the [target] letter-sound, using the first word in [words] which
     * starts with it and has a picture.
     *
     * Only letter-sounds with a single sound can be assessed or offered, since the learner has to
     * isolate one phoneme. (So not e.g. "x", which makes /k/ followed by /s/.)
     *
     * The distractor is a sound that is not heard anywhere in the word, with different letters
     * than the correct answer.
     *
     * @param words The words to choose from, the preferred (most frequent) ones first.
     * @param letterSounds All letter-sounds, which the distractor is picked from.
     * @return `null` if there is no word that a question can be made from.
     */
    fun <I : Any> create(
        target: LetterSoundCandidate,
        words: Sequence<WordCandidate<I>>,
        letterSounds: List<LetterSoundCandidate>,
        random: Random = Random.Default
    ): PhonemeIsolationQuestion<I>? {
        if (!target.isSinglePhoneme()) return null

        val distractors = letterSounds
            .filter { it.isSinglePhoneme() }
            .sortedByDescending { it.usageCount }
            // One per sound, so that a sound with several spellings isn't picked more often
            .distinctBy { it.soundsIpa }

        return words
            // A cheap check first, since reading the letter-sounds of a word takes several queries
            .filter { it.text.startsWith(target.letters, ignoreCase = true) }
            .firstNotNullOfOrNull { word ->
                val wordLetterSounds = word.loadLetterSounds()
                val first = wordLetterSounds.firstOrNull() ?: return@firstNotNullOfOrNull null
                if (!first.letters.equals(target.letters, ignoreCase = true) ||
                    first.soundsIpa != target.soundsIpa
                ) {
                    return@firstNotNullOfOrNull null
                }

                val soundsInWord = wordLetterSounds.flatMap { it.soundsIpa }.toSet()
                val distractor = distractors
                    .filter { it.soundsIpa.single() !in soundsInWord }
                    .filter { !it.letters.equals(target.letters, ignoreCase = true) }
                    .take(DISTRACTOR_POOL_SIZE)
                    .randomOrNull(random)
                    ?: return@firstNotNullOfOrNull null

                // Read last, since it is the most expensive part
                val image = word.loadImage() ?: return@firstNotNullOfOrNull null

                PhonemeIsolationQuestion(
                    word = word.text,
                    image = image,
                    target = target,
                    options = listOf(target, distractor).shuffled(random)
                )
            }
    }

    private fun LetterSoundCandidate.isSinglePhoneme(): Boolean {
        return letters.isNotBlank() && soundsIpa.size == 1 && soundsIpa.single().isNotBlank()
    }
}
