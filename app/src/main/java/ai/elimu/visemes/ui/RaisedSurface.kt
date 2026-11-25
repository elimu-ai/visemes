package ai.elimu.visemes.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

internal val RaisedEdgeHeight = 6.dp

/**
 * A 2D button that looks raised: its face sits on a solid, darker edge of the same shape (the same
 * look as the buttons in [Herufi](https://github.com/elimu-ai/herufi)).
 *
 * While pressed, the face drops onto the edge, so the button looks flat. When released, it
 * animates back up.
 *
 * The size that the caller gives is the size of the face. The edge sticks out below it, so leave
 * [edgeHeight] of room under the button.
 */
@Composable
internal fun RaisedSurface(
    faceColor: Color,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    edgeHeight: Dp = RaisedEdgeHeight,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val drop by animateDpAsState(
        targetValue = if (isPressed) edgeHeight else 0.dp,
        // Down quickly, back up a little slower so that the release can be seen
        animationSpec = tween(durationMillis = if (isPressed) 60 else 140),
        label = "raised-surface-drop"
    )

    Box(
        // The caller's size applies to the face; the edge sticks out below it
        propagateMinConstraints = true,
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null, // the face dropping is the feedback
            enabled = enabled,
            role = Role.Button,
            onClick = onClick
        )
    ) {
        // The darker edge, as a copy of the face that is moved down
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = edgeHeight)
                .background(lerp(faceColor, Color.Black, 0.25f), shape)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset { IntOffset(0, drop.roundToPx()) }
                .clip(shape)
                .background(faceColor),
            content = content
        )
    }
}
