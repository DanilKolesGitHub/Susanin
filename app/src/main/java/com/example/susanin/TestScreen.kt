package com.example.susanin

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.sp
import com.example.navigation.TestScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

//https://issuetracker.google.com/issues/192993290
class TestScreen(context: ScreenContext, type: TestScreenParams): ViewScreen<TestScreenParams>(context, type) {

    val CONTENT_ANIMATION_DURATION = 1000
    val colors = listOf(Color.Magenta, Color.Blue, Color.Cyan, Color.Green, Color.Yellow, Color.Red)
    private fun colorAt(num: Int) = colors[num % colors.size]

    private val screen = MutableStateFlow<Int>(0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        val layout = ComposeView(container.context)
        layout.setContent {
            HavBox()
        }
        return layout
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun HavBox() {
        val screenNumber by screen.collectAsState()
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = screenNumber,
                transitionSpec = {
                    // Compare the incoming number with the previous number.
                    Log.d("ACD", "targetState $targetState initialState $initialState")
                    val anim = if (targetState > initialState) {
                        // If the target number is larger, it slides up and fades in
                        // while the initial (smaller) number slides up and fades out.
                        slideIntoContainer(
                            animationSpec = tween(CONTENT_ANIMATION_DURATION),
                            towards = AnimatedContentTransitionScope.SlideDirection.Left) togetherWith
                                ExitTransition.KeepUntilTransitionsFinished
                    } else {
                        // If the target number is smaller, it slides down and fades in
                        // while the initial number slides down and fades out.
                        fadeIn(animationSpec = tween(CONTENT_ANIMATION_DURATION), initialAlpha = 1f) togetherWith
                        slideOutOfContainer(
                            animationSpec = tween(CONTENT_ANIMATION_DURATION),
                            towards = AnimatedContentTransitionScope.SlideDirection.Right)
                    }
                    anim.targetContentZIndex = targetState.toFloat()
                    anim
                }, label = "anim"
            ) { targetState ->
                for (i in 0..targetState) {

                    Content(
                        i,
                        { screen.update { it + 2 } },
                        { screen.update { it - 1 } },
                    )
                }
            }
        }
    }

    @Composable
    fun Content(
        num: Int,
        forward: () -> Unit,
        backward: () -> Unit,
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .alpha(0.3f)
            .background(colorAt(num))
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = num.toString(),
                fontSize = 48.sp)
            Button(
                modifier = Modifier.align(Alignment.BottomStart),
                onClick = backward) {
                Text(text = "<")
            }
            Button(
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = forward) {
                Text(text = ">")
            }
        }
    }

}