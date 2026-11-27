package ai.elimu.visemes.content

import ai.elimu.content_provider.utils.ContentProviderUtil.getAllWordGsons
import ai.elimu.content_provider.utils.ContentProviderUtil.getImageGsons
import ai.elimu.content_provider.utils.converter.CursorToLetterSoundGsonConverter
import ai.elimu.visemes.BuildConfig
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log

/**
 * Reads words, and their pictures, from the elimu.ai Content Provider.
 *
 * Every function blocks while it reads, so call them off the main thread.
 */
object WordRepository {

    private const val TAG = "WordRepository"

    /** Pictures are scaled down to roughly this size (in pixels) to save memory. */
    private const val MAX_IMAGE_SIZE = 512

    private const val APPLICATION_ID = BuildConfig.CONTENT_PROVIDER_APPLICATION_ID

    /**
     * All words, the most frequent one first.
     *
     * The letter-sounds and the picture of a word are only read when they are asked for, since
     * that takes several queries per word.
     *
     * @throws Exception if the Content Provider app is not installed, or if it could not be read.
     */
    fun loadAll(context: Context): Sequence<WordCandidate<Bitmap>> {
        val wordGsons = getAllWordGsons(context, APPLICATION_ID)
        Log.i(TAG, "wordGsons.size: ${wordGsons.size}")

        return wordGsons
            .sortedByDescending { it.usageCount ?: 0 }
            .asSequence()
            .mapNotNull { wordGson ->
                val wordId = wordGson.id ?: return@mapNotNull null
                WordCandidate(
                    text = wordGson.text,
                    loadLetterSounds = { loadLetterSounds(context, wordId) },
                    loadImage = { loadImage(context, wordId) }
                )
            }
    }

    /** The letter-sounds of a word, in the order they appear in the word. */
    private fun loadLetterSounds(context: Context, wordId: Long): List<LetterSoundCandidate> {
        val uri = Uri.parse(
            "content://$APPLICATION_ID.provider.letter_sound_provider/letter_sounds/by-word-id/$wordId"
        )
        val cursor = context.contentResolver.query(uri, null, null, null, null)
            ?: return emptyList()
        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(
                        CursorToLetterSoundGsonConverter
                            .getLetterSoundGson(it, context, APPLICATION_ID)
                            .toCandidate()
                    )
                }
            }
        }
    }

    /** The first picture of the word that can be shown, or `null` if it has none. */
    private fun loadImage(context: Context, wordId: Long): Bitmap? {
        return getImageGsons(wordId, context, APPLICATION_ID).firstNotNullOfOrNull { imageGson ->
            val uri = Uri.parse(
                "content://$APPLICATION_ID.provider.image_provider/images/${imageGson.id}"
            )
            try {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val largestSide = maxOf(info.size.width, info.size.height)
                    decoder.setTargetSampleSize(maxOf(1, largestSide / MAX_IMAGE_SIZE))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load image ${imageGson.id}", e)
                null
            }
        }
    }
}
