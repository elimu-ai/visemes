package ai.elimu.visemes.viseme

/**
 * A viseme is the mouth shape that goes with a group of speech sounds. Sounds that look the same
 * when spoken (e.g. /p/, /b/ and /m/, which all start with the lips pressed together) share one
 * viseme.
 *
 * The [shape] of each viseme is a simplified, cartoon-style version of the real mouth position.
 * Use [VisemeMapper] to find the viseme of an IPA sound.
 */
enum class Viseme(val shape: LipShape) {

    // Consonants

    /** /p/ /b/ /m/ — lips pressed together. */
    BILABIAL(pose(0.00f, 0.50f, 0.00f, 0.0f, 0.00f, 0f, 0.30f, 0f)),

    /** /f/ /v/ — upper teeth resting on the lower lip. */
    LABIODENTAL(pose(0.22f, 0.65f, 0.00f, 1.0f, 0.00f, 1f, 0.30f, 0f)),

    /** /θ/ /ð/ — tongue tip pushed out between the teeth. */
    DENTAL(pose(0.22f, 0.60f, 0.00f, 1.0f, 0.80f, 0f, 0.70f, 1f)),

    /** /t/ /d/ /n/ /l/ — tongue tip raised behind the upper teeth. */
    ALVEOLAR(pose(0.20f, 0.60f, 0.00f, 0.9f, 0.70f, 0f, 0.95f, 0f)),

    /** /s/ /z/ — teeth almost closed, lips spread. */
    SIBILANT(pose(0.10f, 0.80f, 0.00f, 1.0f, 1.00f, 0f, 0.80f, 0f)),

    /** /ʃ/ /ʒ/ /tʃ/ /dʒ/ — lips rounded and pushed forward, teeth close together. */
    POSTALVEOLAR(pose(0.22f, 0.38f, 0.90f, 1.0f, 0.60f, 0f, 0.75f, 0f)),

    /** /j/ — lips spread, middle of the tongue raised. */
    PALATAL(pose(0.15f, 0.80f, 0.00f, 0.8f, 0.40f, 0f, 0.90f, 0f)),

    /** /k/ /g/ /ŋ/ — mouth a little open, back of the tongue raised. */
    VELAR(pose(0.35f, 0.58f, 0.05f, 0.5f, 0.25f, 0f, 0.65f, 0f)),

    /** /h/ — relaxed, slightly open mouth. */
    GLOTTAL(pose(0.40f, 0.58f, 0.05f, 0.4f, 0.10f, 0f, 0.30f, 0f)),

    /** /r/ — lips slightly rounded, tongue pulled up and back. */
    RHOTIC(pose(0.30f, 0.40f, 0.55f, 0.5f, 0.20f, 0f, 0.80f, 0f)),

    /** /w/ — small, rounded lips. */
    LABIAL_VELAR(pose(0.18f, 0.20f, 1.00f, 0.0f, 0.00f, 0f, 0.60f, 0f)),

    // Vowels

    /** /ɑ/ /a/ /ɒ/ — jaw dropped, mouth wide open. */
    OPEN_BACK(pose(0.95f, 0.60f, 0.10f, 0.5f, 0.15f, 0f, 0.00f, 0f)),

    /** /æ/ — mouth open and spread. */
    NEAR_OPEN_FRONT(pose(0.75f, 0.90f, 0.00f, 0.7f, 0.30f, 0f, 0.20f, 0f)),

    /** /ɛ/ /e/ — mouth half open and spread. */
    MID_FRONT(pose(0.50f, 0.78f, 0.00f, 0.6f, 0.30f, 0f, 0.40f, 0f)),

    /** /i/ /ɪ/ — lips spread into a smile, teeth showing, tongue high. */
    CLOSE_FRONT(pose(0.18f, 0.95f, 0.00f, 0.9f, 0.70f, 0f, 0.80f, 0f)),

    /** /ə/ /ʌ/ /ɜ/ — relaxed, half-open mouth. */
    MID_CENTRAL(pose(0.40f, 0.55f, 0.10f, 0.4f, 0.15f, 0f, 0.30f, 0f)),

    /** /ɔ/ /o/ — jaw half open, lips rounded. */
    MID_BACK_ROUNDED(pose(0.60f, 0.35f, 0.85f, 0.3f, 0.00f, 0f, 0.10f, 0f)),

    /** /u/ /ʊ/ — small, tightly rounded lips. */
    CLOSE_BACK_ROUNDED(pose(0.20f, 0.20f, 1.00f, 0.0f, 0.00f, 0f, 0.50f, 0f))
}

/**
 * Compact way of writing a [LipShape], in the same order as its properties:
 * jaw open, lip spread, lip round, upper teeth, lower teeth, lip tuck, tongue raise, tongue out.
 */
private fun pose(
    jawOpen: Float,
    lipSpread: Float,
    lipRound: Float,
    upperTeeth: Float,
    lowerTeeth: Float,
    lipTuck: Float,
    tongueRaise: Float,
    tongueOut: Float
) = LipShape(
    jawOpen = jawOpen,
    lipSpread = lipSpread,
    lipRound = lipRound,
    upperTeeth = upperTeeth,
    lowerTeeth = lowerTeeth,
    lipTuck = lipTuck,
    tongueRaise = tongueRaise,
    tongueOut = tongueOut
)
