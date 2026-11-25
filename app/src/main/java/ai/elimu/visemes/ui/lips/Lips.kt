package ai.elimu.visemes.ui.lips

import ai.elimu.visemes.viseme.LipShape
import ai.elimu.visemes.viseme.Viseme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val LipColor = Color(0xFF9C4DCC)
private val LipHighlightColor = Color(0xFFC792EA)
private val LipOutlineColor = Color(0xFF4A148C)
private val MouthInsideColor = Color(0xFF45122F)
private val TeethColor = Color(0xFFFFFFFF)
private val TeethLineColor = Color(0xFFD9CFE3)
private val TongueColor = Color(0xFFFF7C9C)
private val TongueGrooveColor = Color(0xFFE0506F)

/**
 * A cartoon mouth with purple lips, teeth and a tongue, held in the pose of the given [shape].
 *
 * The mouth is drawn in the middle of the available space, and scales with it. It is designed for a
 * 4:3 area (e.g. `Modifier.aspectRatio(4f / 3f)`), and is drawn without a face behind it, so put it
 * on a background that gives the purple lips some contrast.
 *
 * To see the mouth move from one shape to another, use [AnimatedLips].
 */
@Composable
fun Lips(shape: LipShape, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawLips(shape)
    }
}

/** Measurements (in pixels) that the different parts of the mouth are drawn from. */
private class Geometry(
    val width: Float,
    val height: Float,
    /** Where the middle of the mouth is. */
    val centerX: Float,
    val centerY: Float,
    /** Half of the width of the mouth opening (corner to corner). */
    val halfWidth: Float,
    /** How far the mouth opening reaches up (and down) from the middle, at its tallest. */
    val halfOpen: Float,
    /** How thick the lips are (measured at the middle of the mouth), and at the corners. */
    val upperLip: Float,
    val lowerLip: Float,
    val sideLip: Float,
    val strokeWidth: Float
)

/**
 * Draws the mouth in the pose of [shape].
 *
 * From the back to the front: the lips, the inside of the mouth (dark, with the tongue, the teeth
 * and a lower lip that can be pulled up behind the upper teeth), the edge of the opening, shiny
 * highlights on the lips, and last the tip of the tongue when it is pushed out.
 */
internal fun DrawScope.drawLips(shape: LipShape) {
    val g = Geometry(
        width = size.width,
        height = size.height,
        centerX = size.width / 2f,
        centerY = size.height / 2f,
        halfWidth = size.width * (0.26f + 0.16f * shape.lipSpread - 0.10f * shape.lipRound),
        halfOpen = size.height * (0.012f + 0.30f * shape.jawOpen),
        upperLip = size.height * (0.075f + 0.045f * shape.lipRound),
        lowerLip = size.height * (0.095f + 0.055f * shape.lipRound),
        sideLip = size.height * (0.05f + 0.03f * shape.lipRound),
        strokeWidth = maxOf(2.dp.toPx(), size.height * 0.012f)
    )
    // 0.5 gives pointed mouth corners (a smile), 1 gives smooth, round corners (an "oo")
    val innerCorner = 0.5f + 0.5f * shape.lipRound
    val outerCorner = 0.6f + 0.4f * shape.lipRound
    val outline = Stroke(width = g.strokeWidth, join = StrokeJoin.Round)

    // The lips: the opening, plus the thickness of the lips all around it
    val outer = lensPath(
        g.centerX, g.centerY,
        halfWidth = g.halfWidth + g.sideLip,
        upperReach = g.halfOpen + g.upperLip,
        lowerReach = g.halfOpen + g.lowerLip,
        corner = outerCorner
    )
    drawPath(outer, LipColor)
    drawPath(outer, LipOutlineColor, style = outline)

    // The inside of the mouth. Everything in here is cut off at the edge of the opening.
    val inner = lensPath(
        g.centerX, g.centerY,
        halfWidth = g.halfWidth,
        upperReach = g.halfOpen,
        lowerReach = g.halfOpen,
        corner = innerCorner
    )
    clipPath(inner) {
        drawRect(MouthInsideColor)
        drawTongue(g, shape)
        drawTeeth(g, shape)
        drawTuckedLowerLip(g, shape)
    }
    drawPath(inner, LipOutlineColor, style = outline)

    // Shiny highlights, in the middle of the thickness of the lips
    val highlight = Stroke(width = g.strokeWidth * 1.3f, cap = StrokeCap.Round)
    val middleHalfWidth = g.halfWidth + g.sideLip * 0.5f
    val middleCorner = (innerCorner + outerCorner) / 2f
    drawPath(
        archPath(
            g.centerX, g.centerY, middleHalfWidth, g.halfOpen + g.upperLip * 0.55f,
            middleCorner, direction = -1f, fromT = 0.16f, toT = 0.38f
        ),
        LipHighlightColor,
        style = highlight
    )
    drawPath(
        archPath(
            g.centerX, g.centerY, middleHalfWidth, g.halfOpen + g.lowerLip * 0.5f,
            middleCorner, direction = 1f, fromT = 0.34f, toT = 0.62f
        ),
        LipHighlightColor,
        style = highlight
    )

    drawTongueTip(g, shape)
}

/** The tongue: a rounded pink shape lying in the bottom of the mouth, raised for some sounds. */
private fun DrawScope.drawTongue(g: Geometry, shape: LipShape) {
    val tongueWidth = g.halfWidth * 1.24f
    val top = mix(g.centerY + g.halfOpen * 0.45f, g.centerY - g.halfOpen * 0.35f, shape.tongueRaise)
    // Goes down past the bottom of the opening, so that the rounded corners are cut off
    val bottom = g.centerY + g.halfOpen * 1.6f

    drawRoundRect(
        color = TongueColor,
        topLeft = Offset(g.centerX - tongueWidth / 2f, top),
        size = Size(tongueWidth, bottom - top),
        cornerRadius = CornerRadius(tongueWidth * 0.28f)
    )
    // The groove down the middle of the tongue
    drawLine(
        color = TongueGrooveColor,
        start = Offset(g.centerX, top + g.halfOpen * 0.12f),
        end = Offset(g.centerX, top + g.halfOpen * 0.92f),
        strokeWidth = g.strokeWidth * 0.7f,
        cap = StrokeCap.Round
    )
}

/** The upper and lower teeth. Each one reaches into the opening from the top and the bottom. */
private fun DrawScope.drawTeeth(g: Geometry, shape: LipShape) {
    val left = g.centerX - g.halfWidth * 0.8f
    val right = g.centerX + g.halfWidth * 0.8f
    val opening = 2f * g.halfOpen

    if (shape.upperTeeth > 0f) {
        drawTeethRow(
            g, left, right,
            top = g.centerY - g.halfOpen - g.height * 0.2f,
            bottom = g.centerY - g.halfOpen + opening * 0.5f * shape.upperTeeth
        )
    }
    if (shape.lowerTeeth > 0f) {
        drawTeethRow(
            g, left, right,
            top = g.centerY + g.halfOpen - opening * 0.5f * shape.lowerTeeth,
            bottom = g.centerY + g.halfOpen + g.height * 0.2f
        )
    }
}

/** A row of four teeth, as one white block with thin lines between the teeth. */
private fun DrawScope.drawTeethRow(g: Geometry, left: Float, right: Float, top: Float, bottom: Float) {
    val rowWidth = right - left
    val rowHeight = bottom - top
    drawRoundRect(
        color = TeethColor,
        topLeft = Offset(left, top),
        size = Size(rowWidth, rowHeight),
        cornerRadius = CornerRadius(minOf(rowWidth * 0.07f, rowHeight * 0.5f))
    )
    for (i in 1..3) {
        val x = left + rowWidth * i / 4f
        drawLine(
            color = TeethLineColor,
            start = Offset(x, top),
            end = Offset(x, bottom),
            strokeWidth = g.strokeWidth * 0.5f
        )
    }
}

/**
 * The lower lip pulled up inside the opening, in front of the upper teeth (for "f" and "v"). It
 * is drawn on top of the teeth, so that the teeth look like they rest on the lip.
 */
private fun DrawScope.drawTuckedLowerLip(g: Geometry, shape: LipShape) {
    if (shape.lipTuck <= 0f) return

    // The top edge of the lip, in the middle: from the bottom of the opening up to just above
    // the middle of it
    val edge = g.centerY + g.halfOpen - 2f * g.halfOpen * 0.65f * shape.lipTuck
    val reach = g.halfWidth * 1.2f
    val lip = Path().apply {
        moveTo(g.centerX - reach, edge + g.halfOpen * 0.35f)
        cubicTo(
            g.centerX - g.halfWidth * 0.4f, edge - g.halfOpen * 0.25f,
            g.centerX + g.halfWidth * 0.4f, edge - g.halfOpen * 0.25f,
            g.centerX + reach, edge + g.halfOpen * 0.35f
        )
        lineTo(g.centerX + reach, g.centerY + g.halfOpen * 3f)
        lineTo(g.centerX - reach, g.centerY + g.halfOpen * 3f)
        close()
    }
    drawPath(lip, LipColor)
    drawPath(lip, LipOutlineColor, style = Stroke(width = g.strokeWidth, join = StrokeJoin.Round))
}

/**
 * The tip of the tongue pushed out between the teeth, over the lower lip (for "th"). It is
 * drawn last, on top of the lips and the teeth.
 */
private fun DrawScope.drawTongueTip(g: Geometry, shape: LipShape) {
    if (shape.tongueOut <= 0f) return

    val tipWidth = g.halfWidth * 0.6f
    val top = g.centerY - g.halfOpen * 0.3f
    // Sticks out past the lower lip
    val bottom = g.centerY + g.halfOpen + g.lowerLip * 1.35f * shape.tongueOut
    val topLeft = Offset(g.centerX - tipWidth / 2f, top)
    val tipSize = Size(tipWidth, bottom - top)

    drawRoundRect(
        color = TongueColor,
        topLeft = topLeft,
        size = tipSize,
        cornerRadius = CornerRadius(tipWidth / 2f)
    )
    drawRoundRect(
        color = TongueGrooveColor,
        topLeft = topLeft,
        size = tipSize,
        cornerRadius = CornerRadius(tipWidth / 2f),
        style = Stroke(width = g.strokeWidth * 0.6f)
    )
    drawLine(
        color = TongueGrooveColor,
        start = Offset(g.centerX, top + (bottom - top) * 0.25f),
        end = Offset(g.centerX, bottom - tipWidth * 0.35f),
        strokeWidth = g.strokeWidth * 0.7f,
        cap = StrokeCap.Round
    )
}

private const val FOUR_THIRDS = 4f / 3f

/**
 * A closed shape that is wide at the middle and comes together at the left and right corners,
 * like a lens. The top half goes [upperReach] up from the middle, and the bottom half goes
 * [lowerReach] down.
 *
 * @param corner `1` gives smooth, round corners, and lower values give pointier corners.
 */
private fun lensPath(
    centerX: Float,
    centerY: Float,
    halfWidth: Float,
    upperReach: Float,
    lowerReach: Float,
    corner: Float
): Path = Path().apply {
    // A curve with its control points at 4/3 of the height reaches exactly that height at its
    // middle point
    moveTo(centerX - halfWidth, centerY)
    cubicTo(
        centerX - halfWidth * corner, centerY - upperReach * FOUR_THIRDS,
        centerX + halfWidth * corner, centerY - upperReach * FOUR_THIRDS,
        centerX + halfWidth, centerY
    )
    cubicTo(
        centerX + halfWidth * corner, centerY + lowerReach * FOUR_THIRDS,
        centerX - halfWidth * corner, centerY + lowerReach * FOUR_THIRDS,
        centerX - halfWidth, centerY
    )
    close()
}

/**
 * A piece of one side of a [lensPath]: the part between [fromT] and [toT], where `0` is the left
 * corner, `0.5` is the middle and `1` is the right corner. [direction] is `-1` for the upper side
 * and `1` for the lower side.
 */
private fun archPath(
    centerX: Float,
    centerY: Float,
    halfWidth: Float,
    reach: Float,
    corner: Float,
    direction: Float,
    fromT: Float,
    toT: Float
): Path = Path().apply {
    val steps = 8
    for (i in 0..steps) {
        val t = fromT + (toT - fromT) * i / steps
        val x = cubic(
            centerX - halfWidth, centerX - halfWidth * corner,
            centerX + halfWidth * corner, centerX + halfWidth, t
        )
        val y = centerY + direction * cubic(0f, reach * FOUR_THIRDS, reach * FOUR_THIRDS, 0f, t)
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
}

/** A point on a cubic Bézier curve. [t] goes from `0` (the start) to `1` (the end). */
private fun cubic(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
    val u = 1f - t
    return u * u * u * p0 + 3f * u * u * t * p1 + 3f * u * t * t * p2 + t * t * t * p3
}

private fun mix(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction

@Preview(showBackground = true, widthDp = 420, heightDp = 800)
@Composable
private fun LipsPreview() {
    val poses = listOf(
        "neutral" to LipShape.Neutral,
        "m" to Viseme.BILABIAL.shape,
        "f" to Viseme.LABIODENTAL.shape,
        "th" to Viseme.DENTAL.shape,
        "s" to Viseme.SIBILANT.shape,
        "sh" to Viseme.POSTALVEOLAR.shape,
        "ae" to Viseme.NEAR_OPEN_FRONT.shape,
        "ah" to Viseme.OPEN_BACK.shape,
        "ee" to Viseme.CLOSE_FRONT.shape,
        "oo" to Viseme.CLOSE_BACK_ROUNDED.shape
    )
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        poses.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (_, shape) ->
                    Lips(
                        shape = shape,
                        modifier = Modifier
                            .size(width = 196.dp, height = 147.dp)
                            .background(Color(0xFFFFE0C7), RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}
