package ai.elimu.visemes.viseme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LipShapeTest {

    private val delta = 1e-5f

    @Test
    fun interpolateStartsAtTheCurrentShapeAndEndsAtTheTargetShape() {
        val target = Viseme.OPEN_BACK.shape

        val start = LipShape.Neutral.interpolate(target, 0f)
        val end = LipShape.Neutral.interpolate(target, 1f)

        assertEquals(LipShape.Neutral.jawOpen, start.jawOpen, delta)
        assertEquals(LipShape.Neutral.tongueRaise, start.tongueRaise, delta)
        assertEquals(target.jawOpen, end.jawOpen, delta)
        assertEquals(target.lipSpread, end.lipSpread, delta)
        assertEquals(target.tongueRaise, end.tongueRaise, delta)
    }

    @Test
    fun interpolateGoesHalfWayAtOneHalf() {
        val target = Viseme.OPEN_BACK.shape

        val halfWay = LipShape.Neutral.interpolate(target, 0.5f)

        assertEquals((LipShape.Neutral.jawOpen + target.jawOpen) / 2f, halfWay.jawOpen, delta)
        assertEquals((LipShape.Neutral.lipSpread + target.lipSpread) / 2f, halfWay.lipSpread, delta)
    }

    @Test
    fun interpolateStaysBetweenTheTwoShapes() {
        val target = Viseme.OPEN_BACK.shape

        assertEquals(LipShape.Neutral.jawOpen, LipShape.Neutral.interpolate(target, -1f).jawOpen, delta)
        assertEquals(target.jawOpen, LipShape.Neutral.interpolate(target, 2f).jawOpen, delta)
    }

    @Test
    fun allMouthShapesAreWithinTheValidRange() {
        val shapes = Viseme.entries.map { it.shape } + LipShape.Neutral
        for (shape in shapes) {
            listOf(
                shape.jawOpen, shape.lipSpread, shape.lipRound, shape.upperTeeth,
                shape.lowerTeeth, shape.lipTuck, shape.tongueRaise, shape.tongueOut
            ).forEach { value ->
                assertTrue("$shape has a value outside of 0..1", value in 0f..1f)
            }
        }
    }

    @Test
    fun theMouthIsClosedForSoundsMadeWithThePressedTogetherLips() {
        assertEquals(0f, Viseme.BILABIAL.shape.jawOpen, 0f)
    }
}
