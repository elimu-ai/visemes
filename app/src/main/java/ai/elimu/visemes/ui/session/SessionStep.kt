package ai.elimu.visemes.ui.session

import ai.elimu.visemes.content.LetterSoundCandidate

/**
 * The activities that the student goes through for each letter-sound, in this order.
 *
 * To add another learning or assessment activity, add it here, and in
 * [LetterSoundSessionActivity] say which activity it is.
 */
enum class LetterSoundTask {
    VISEME_LEARNING,
    PHONEME_ISOLATION_ASSESSMENT
}

/** One activity to show: a [task] for a [letterSound]. */
data class SessionStep(
    val letterSound: LetterSoundCandidate,
    val task: LetterSoundTask
) {
    companion object {
        /**
         * The step at a position in the session: all tasks of the first letter-sound, then all
         * tasks of the second one, and so on.
         *
         * @param letterSounds The letter-sounds, in the order they are shown.
         * @param position `0` for the first step.
         * @return `null` if the position is past the last step.
         */
        fun at(letterSounds: List<LetterSoundCandidate>, position: Int): SessionStep? {
            val tasks = LetterSoundTask.entries
            val letterSound = letterSounds.getOrNull(position / tasks.size) ?: return null
            return SessionStep(letterSound, tasks[position % tasks.size])
        }
    }
}
