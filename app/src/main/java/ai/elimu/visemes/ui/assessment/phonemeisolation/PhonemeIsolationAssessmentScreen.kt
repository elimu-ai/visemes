package ai.elimu.visemes.ui.assessment.phonemeisolation

import ai.elimu.visemes.R
import ai.elimu.visemes.content.LetterSoundCandidate
import ai.elimu.visemes.ui.NextButton
import ai.elimu.visemes.ui.RaisedEdgeHeight
import ai.elimu.visemes.ui.RaisedSurface
import ai.elimu.visemes.ui.VisemeColorScheme
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** How long it takes to slide from one question to the next. */
private const val QUESTION_TRANSITION_MS = 450

/** The frame around the picture. */
private val PictureFrameColor = Color(0xFFD9C2EA)

/** The option the learner picked turns deep purple (correct) or dull plum-grey (incorrect). */
private val SelectedCorrectColor = Color(0xFF4A148C)
private val SelectedIncorrectColor = Color(0xFF6F6475)

/** An option that can't be pressed (yet, or anymore). */
private val InactiveOptionColor = Color(0xFFE4DAEB)
private val InactiveLabelColor = Color(0xFF8E7D9A)

/** The whole screen turns lilac after a correct answer, and grey after an incorrect one. */
private val CorrectBackgroundColor = Color(0xFFE9D5F7)
private val IncorrectBackgroundColor = Color(0xFFE8E4EA)

private val OptionShape = RoundedCornerShape(16.dp)
private val NextButtonHeight = 56.dp

/**
 * From top to bottom: a picture of the word (press it to hear the word), the two sounds to choose
 * between, and (in the bottom right corner, after answering) the arrow for going on.
 */
@Composable
fun PhonemeIsolationAssessmentScreen(
    uiState: PhonemeIsolationUiState,
    onPlayWord: () -> Unit,
    onOptionSelected: (LetterSoundCandidate) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val question = uiState as? PhonemeIsolationUiState.Question
    val background by animateColorAsState(
        targetValue = when (question?.isCorrect) {
            true -> CorrectBackgroundColor
            false -> IncorrectBackgroundColor
            null -> MaterialTheme.colorScheme.background // not answered yet
        },
        animationSpec = tween(durationMillis = 300),
        label = "background"
    )

    Surface(modifier = modifier.fillMaxSize(), color = background) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = uiState,
                // Only a different question is new content. (Not e.g. an option being picked.)
                contentKey = { state ->
                    if (state is PhonemeIsolationUiState.Question) state.index else state
                },
                transitionSpec = {
                    if (initialState is PhonemeIsolationUiState.Question &&
                        targetState is PhonemeIsolationUiState.Question
                    ) {
                        (slideInHorizontally(tween(QUESTION_TRANSITION_MS)) { fullWidth -> fullWidth } +
                            fadeIn(tween(QUESTION_TRANSITION_MS))) togetherWith
                            (slideOutHorizontally(tween(QUESTION_TRANSITION_MS)) { fullWidth -> -fullWidth } +
                                fadeOut(tween(QUESTION_TRANSITION_MS)))
                    } else {
                        fadeIn() togetherWith fadeOut()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                label = "questionTransition"
            ) { state ->
                when (state) {
                    PhonemeIsolationUiState.Loading -> Centered {
                        CircularProgressIndicator()
                    }

                    PhonemeIsolationUiState.Unavailable -> Centered {
                        Text(
                            text = stringResource(R.string.phoneme_isolation_unavailable),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }

                    is PhonemeIsolationUiState.Question -> QuestionContent(
                        state = state,
                        onPlayWord = onPlayWord,
                        onOptionSelected = onOptionSelected
                    )
                }
            }

            // In the bottom right corner of the screen
            AnimatedVisibility(
                visible = question?.isAnswered == true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .safeDrawingPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp - RaisedEdgeHeight)
            ) {
                // The edge of the button has to be inside the content, or it is cut off while fading
                Box(modifier = Modifier.padding(bottom = RaisedEdgeHeight)) {
                    NextButton(
                        description = stringResource(R.string.next_question),
                        onClick = onNext,
                        shape = OptionShape,
                        modifier = Modifier.size(width = 120.dp, height = NextButtonHeight)
                    )
                }
            }
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun QuestionContent(
    state: PhonemeIsolationUiState.Question,
    onPlayWord: () -> Unit,
    onOptionSelected: (LetterSoundCandidate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            // Extra room at the bottom for the arrow, so that nothing moves when it appears
            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 24.dp + NextButtonHeight + 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The picture gets the height that the options leave free
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = RaisedEdgeHeight),
            contentAlignment = Alignment.Center
        ) {
            PictureCard(image = state.question.image, onClick = onPlayWord)
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.question.options.forEach { option ->
                OptionButton(
                    option = option,
                    state = state,
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp)
                )
            }
        }
    }
}

/** The picture of the word. Pressing it says the word. */
@Composable
private fun PictureCard(image: Bitmap, onClick: () -> Unit) {
    val description = stringResource(R.string.play_word)
    RaisedSurface(
        faceColor = PictureFrameColor,
        shape = RoundedCornerShape(24.dp),
        onClick = onClick,
        modifier = Modifier
            .widthIn(max = 400.dp)
            .aspectRatio(4f / 3f)
            .semantics { contentDescription = description }
    ) {
        Image(
            bitmap = remember(image) { image.asImageBitmap() },
            contentDescription = null, // described by the parent
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(8.dp)
        )
    }
}

/** One of the two sounds, in IPA (e.g. /k/). */
@Composable
private fun OptionButton(
    option: LetterSoundCandidate,
    state: PhonemeIsolationUiState.Question,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = state.selectedOption == option
    val isCorrectOption = state.question.isCorrect(option)

    // Whether the option can be pressed: after listening to the word, and before answering
    val enabled = state.isWordHeard && !state.isAnswered

    // After answering, only the picked option is marked as correct or incorrect. The right answer
    // is not revealed after a wrong pick, and a wrong option that wasn't picked fades.
    val looksActive = enabled || (state.isAnswered && isCorrectOption)
    val faceColor = when {
        isSelected && isCorrectOption -> SelectedCorrectColor
        isSelected -> SelectedIncorrectColor
        looksActive -> MaterialTheme.colorScheme.primary
        else -> InactiveOptionColor
    }
    val labelColor = if (isSelected || looksActive) Color.White else InactiveLabelColor
    // A mark, so that the colour is not the only signal
    val mark = when {
        !isSelected -> null
        isCorrectOption -> "✓"
        else -> "✗"
    }
    val resultDescription = when {
        !isSelected -> null
        isCorrectOption -> stringResource(R.string.answer_correct)
        else -> stringResource(R.string.answer_incorrect)
    }

    // The button pops when it is the correct pick, and shakes when it is a wrong one
    val scale = remember { Animatable(1f) }
    val shift = remember { Animatable(0f) }
    LaunchedEffect(isSelected) {
        if (!isSelected) return@LaunchedEffect
        if (isCorrectOption) {
            scale.animateTo(1.08f, tween(160))
            scale.animateTo(1f, tween(160))
        } else {
            listOf(-10f, 10f, -6f, 6f, 0f).forEach { shift.animateTo(it, tween(50)) }
        }
    }

    RaisedSurface(
        faceColor = faceColor,
        shape = OptionShape,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .graphicsLayer {
                translationX = shift.value.dp.toPx()
                scaleX = scale.value
                scaleY = scale.value
            }
            .semantics { resultDescription?.let { stateDescription = it } }
    ) {
        Text(
            text = listOfNotNull("/${option.soundsIpa.single()}/", mark).joinToString(" "),
            color = labelColor,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun PreviewScreen(selectCorrect: Boolean? = null) {
    val target = LetterSoundCandidate("c", listOf("k"), 2)
    val distractor = LetterSoundCandidate("o", listOf("ɒ"), 1)
    // A plain square stands in for the picture
    val image = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        .apply { eraseColor(android.graphics.Color.LTGRAY) }

    MaterialTheme(colorScheme = VisemeColorScheme) {
        PhonemeIsolationAssessmentScreen(
            uiState = PhonemeIsolationUiState.Question(
                question = PhonemeIsolationQuestion("cat", image, target, listOf(target, distractor)),
                isWordHeard = true,
                selectedOption = selectCorrect?.let { if (it) target else distractor },
                hasNext = true
            ),
            onPlayWord = {},
            onOptionSelected = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun QuestionPreview() = PreviewScreen()

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun AnsweredCorrectPreview() = PreviewScreen(selectCorrect = true)

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun AnsweredIncorrectPreview() = PreviewScreen(selectCorrect = false)

@Preview(showBackground = true, widthDp = 700, heightDp = 360)
@Composable
private fun QuestionLandscapePreview() = PreviewScreen()
