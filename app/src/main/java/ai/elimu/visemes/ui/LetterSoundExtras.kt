package ai.elimu.visemes.ui

import ai.elimu.visemes.content.LetterSoundCandidate
import android.content.Intent
import androidx.lifecycle.SavedStateHandle

private const val EXTRA_LETTERS = "ai.elimu.visemes.extra.LETTERS"
private const val EXTRA_SOUNDS_IPA = "ai.elimu.visemes.extra.SOUNDS_IPA"
private const val EXTRA_USAGE_COUNT = "ai.elimu.visemes.extra.USAGE_COUNT"

/** Tells a learning or assessment activity which letter-sound it is for. */
internal fun Intent.putLetterSound(letterSound: LetterSoundCandidate): Intent {
    return putExtra(EXTRA_LETTERS, letterSound.letters)
        .putExtra(EXTRA_SOUNDS_IPA, letterSound.soundsIpa.toTypedArray())
        .putExtra(EXTRA_USAGE_COUNT, letterSound.usageCount)
}

/**
 * The letter-sound that was given to the activity with [putLetterSound]. (The extras of the
 * activity are available to its view model through its [SavedStateHandle].)
 */
internal fun SavedStateHandle.requireLetterSound(): LetterSoundCandidate {
    return LetterSoundCandidate(
        letters = requireNotNull(get<String>(EXTRA_LETTERS)),
        soundsIpa = requireNotNull(get<Array<String>>(EXTRA_SOUNDS_IPA)).toList(),
        usageCount = requireNotNull(get<Int>(EXTRA_USAGE_COUNT))
    )
}
