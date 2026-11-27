package ai.elimu.visemes.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A raised button with an arrow pointing to the right, for going on to the next task.
 *
 * @param description What screen readers say, since the button has no text.
 * @param modifier Must give the button its size.
 */
@Composable
internal fun NextButton(
    description: String,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    RaisedSurface(
        faceColor = MaterialTheme.colorScheme.primary,
        shape = shape,
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = description }
    ) {
        val arrowColor = MaterialTheme.colorScheme.onPrimary
        Canvas(modifier = Modifier.size(28.dp)) {
            val strokeWidth = 3.dp.toPx()
            val tail = Offset(size.width * 0.1f, size.height / 2f)
            val tip = Offset(size.width * 0.9f, size.height / 2f)
            val headSize = size.width * 0.38f

            drawLine(arrowColor, tail, tip, strokeWidth, StrokeCap.Round)
            drawLine(arrowColor, tip, Offset(tip.x - headSize, tip.y - headSize), strokeWidth, StrokeCap.Round)
            drawLine(arrowColor, tip, Offset(tip.x - headSize, tip.y + headSize), strokeWidth, StrokeCap.Round)
        }
    }
}
