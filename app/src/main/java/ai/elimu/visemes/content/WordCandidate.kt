package ai.elimu.visemes.content

/**
 * A word that may be used in a task.
 *
 * @param text The word, e.g. `"cat"`.
 * @param loadLetterSounds Reads the letter-sounds of the word, in the order they appear in it.
 * @param loadImage Reads a picture of the word. Gives `null` if there is none.
 * @param I The type of the picture.
 */
class WordCandidate<I : Any>(
    val text: String,
    val loadLetterSounds: () -> List<LetterSoundCandidate>,
    val loadImage: () -> I?
)
