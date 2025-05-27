package com.fairshare.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class SlideDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    UP_TO_DOWN,
    DOWN_TO_UP
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationAnimation(
    visible: Boolean,
    direction: SlideDirection = SlideDirection.RIGHT_TO_LEFT,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val duration = 300
    val slideDistance = 30

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = when (direction) {
            SlideDirection.LEFT_TO_RIGHT -> slideInHorizontally(
                initialOffsetX = { -slideDistance },
                animationSpec = tween(duration)
            ) + fadeIn(animationSpec = tween(duration))
            
            SlideDirection.RIGHT_TO_LEFT -> slideInHorizontally(
                initialOffsetX = { slideDistance },
                animationSpec = tween(duration)
            ) + fadeIn(animationSpec = tween(duration))
            
            SlideDirection.UP_TO_DOWN -> slideInVertically(
                initialOffsetY = { -slideDistance },
                animationSpec = tween(duration)
            ) + fadeIn(animationSpec = tween(duration))
            
            SlideDirection.DOWN_TO_UP -> slideInVertically(
                initialOffsetY = { slideDistance },
                animationSpec = tween(duration)
            ) + fadeIn(animationSpec = tween(duration))
        },
        exit = when (direction) {
            SlideDirection.LEFT_TO_RIGHT -> slideOutHorizontally(
                targetOffsetX = { slideDistance },
                animationSpec = tween(duration)
            ) + fadeOut(animationSpec = tween(duration))
            
            SlideDirection.RIGHT_TO_LEFT -> slideOutHorizontally(
                targetOffsetX = { -slideDistance },
                animationSpec = tween(duration)
            ) + fadeOut(animationSpec = tween(duration))
            
            SlideDirection.UP_TO_DOWN -> slideOutVertically(
                targetOffsetY = { slideDistance },
                animationSpec = tween(duration)
            ) + fadeOut(animationSpec = tween(duration))
            
            SlideDirection.DOWN_TO_UP -> slideOutVertically(
                targetOffsetY = { -slideDistance },
                animationSpec = tween(duration)
            ) + fadeOut(animationSpec = tween(duration))
        },
        content = content
    )
} 