package ai.elimu.visemes.content

import ai.elimu.content_provider.utils.ContentProviderUtil.getAllLetterSoundGsons
import ai.elimu.visemes.BuildConfig
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads educational content from the elimu.ai Content Provider
 * (https://github.com/elimu-ai/content-provider).
 */
object LetterSoundRepository {

    private const val TAG = "LetterSoundRepository"

    /**
     * Loads all letter-sound correspondences.
     *
     * @throws Exception if the Content Provider app is not installed, or if it could not be read.
     */
    suspend fun loadAll(context: Context): List<LetterSoundCandidate> = withContext(Dispatchers.IO) {
        val letterSoundGsons = getAllLetterSoundGsons(
            context, BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
        )
        Log.i(TAG, "letterSoundGsons.size: " + letterSoundGsons.size)

        letterSoundGsons.map { letterSoundGson ->
            LetterSoundCandidate(
                letters = letterSoundGson.letters.joinToString("") { it.text },
                soundsIpa = letterSoundGson.sounds.map { it.valueIpa },
                // `usageCount` is nullable on the Java model
                usageCount = letterSoundGson.usageCount ?: 0
            )
        }
    }
}
