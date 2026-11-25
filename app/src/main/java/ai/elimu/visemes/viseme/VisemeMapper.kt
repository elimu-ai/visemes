package ai.elimu.visemes.viseme

/**
 * Finds the [Viseme] (mouth shape) of a speech sound written in the International Phonetic
 * Alphabet (IPA), e.g. `"æ"` or `"tʃ"`.
 */
object VisemeMapper {

    /** Used for sounds that are not recognised: a relaxed, half-open mouth. */
    val Fallback = Viseme.MID_CENTRAL

    /** Postalveolar sounds. Affricates like /tʃ/ and /dʒ/ are shaped by their release (/ʃ/, /ʒ/). */
    private const val POSTALVEOLAR_SYMBOLS = "ʃʒɕʑʧʤ"

    /** One viseme for each sound, in the same order. */
    fun fromSounds(soundsIpa: List<String>): List<Viseme> = soundsIpa.map { fromIpa(it) }

    fun fromIpa(ipa: String): Viseme {
        // Only the base letters decide the mouth shape. Slashes, brackets, stress marks, length
        // marks (ː), tie bars and other diacritics are left out.
        val symbols = ipa.filter { it.isLetter() && it.category != CharCategory.MODIFIER_LETTER }
        if (symbols.isEmpty()) return Fallback

        if (symbols.any { it in POSTALVEOLAR_SYMBOLS }) return Viseme.POSTALVEOLAR
        if (symbols.startsWith("ts") || symbols.startsWith("dz")) return Viseme.SIBILANT

        return when (symbols.first()) {
            // Consonants
            'p', 'b', 'm', 'ɓ' -> Viseme.BILABIAL
            'f', 'v', 'ʋ', 'ɱ' -> Viseme.LABIODENTAL
            'θ', 'ð' -> Viseme.DENTAL
            't', 'd', 'n', 'l', 'ɫ', 'ɾ' -> Viseme.ALVEOLAR
            's', 'z', 'ʦ', 'ʣ' -> Viseme.SIBILANT
            'j', 'c', 'ɟ', 'ɲ', 'ç', 'ʝ' -> Viseme.PALATAL
            'k', 'g', 'ɡ', 'ŋ', 'x', 'ɣ', 'q', 'χ' -> Viseme.VELAR
            'h', 'ɦ', 'ʔ' -> Viseme.GLOTTAL
            'r', 'ɹ', 'ɻ', 'ɽ' -> Viseme.RHOTIC
            'w', 'ʍ' -> Viseme.LABIAL_VELAR

            // Vowels
            'ɑ', 'a', 'ɒ' -> Viseme.OPEN_BACK
            'æ' -> Viseme.NEAR_OPEN_FRONT
            'ɛ', 'e' -> Viseme.MID_FRONT
            'i', 'ɪ', 'ɨ' -> Viseme.CLOSE_FRONT
            'ə', 'ʌ', 'ɜ', 'ɐ', 'ɘ', 'ɚ', 'ɝ' -> Viseme.MID_CENTRAL
            'ɔ', 'o', 'ø', 'œ', 'ɤ' -> Viseme.MID_BACK_ROUNDED
            'u', 'ʊ', 'ɯ', 'y', 'ʏ' -> Viseme.CLOSE_BACK_ROUNDED

            else -> Fallback
        }
    }
}
