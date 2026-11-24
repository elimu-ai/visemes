package ai.elimu.visemes.viseme

/**
 * How the mouth looks at one moment. Every value is between `0f` and `1f`.
 *
 * A [Viseme] is a [LipShape] that is held while a speech sound is made, and [Neutral] is the
 * relaxed mouth before and after the sound. Two shapes can be blended with [interpolate], which is
 * what the lip animation does to move from one shape to the next.
 */
data class LipShape(
    /** How far the jaw is dropped: `0` = lips pressed together, `1` = mouth wide open. */
    val jawOpen: Float,

    /** `0` = corners of the mouth pulled in (puckered), `1` = pulled out (a broad smile). */
    val lipSpread: Float,

    /** `0` = flat lips, `1` = rounded and pushed-out lips (as in "oo"). */
    val lipRound: Float,

    /** How much of the upper teeth can be seen. */
    val upperTeeth: Float,

    /** How much of the lower teeth can be seen. */
    val lowerTeeth: Float,

    /** `1` = the lower lip is pulled up behind the upper teeth (as in "f" and "v"). */
    val lipTuck: Float,

    /** `0` = tongue resting low in the mouth, `1` = raised up to the roof of the mouth. */
    val tongueRaise: Float,

    /** `1` = the tip of the tongue is pushed out between the teeth (as in "th"). */
    val tongueOut: Float
) {

    /**
     * Blends this shape into [to]. `fraction = 0` gives this shape, and `fraction = 1` gives [to].
     */
    fun interpolate(to: LipShape, fraction: Float): LipShape {
        val f = fraction.coerceIn(0f, 1f)
        return LipShape(
            jawOpen = mix(jawOpen, to.jawOpen, f),
            lipSpread = mix(lipSpread, to.lipSpread, f),
            lipRound = mix(lipRound, to.lipRound, f),
            upperTeeth = mix(upperTeeth, to.upperTeeth, f),
            lowerTeeth = mix(lowerTeeth, to.lowerTeeth, f),
            lipTuck = mix(lipTuck, to.lipTuck, f),
            tongueRaise = mix(tongueRaise, to.tongueRaise, f),
            tongueOut = mix(tongueOut, to.tongueOut, f)
        )
    }

    companion object {
        /** The relaxed mouth: lips lightly closed, tongue resting. */
        val Neutral = LipShape(
            jawOpen = 0.04f,
            lipSpread = 0.55f,
            lipRound = 0.05f,
            upperTeeth = 0f,
            lowerTeeth = 0f,
            lipTuck = 0f,
            tongueRaise = 0.3f,
            tongueOut = 0f
        )
    }
}

private fun mix(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction
