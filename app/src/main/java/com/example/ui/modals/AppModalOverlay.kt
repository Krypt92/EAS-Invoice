package com.example.ui.modals

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Renders a modal dialog overlay inside the main Activity window hierarchy rather than spawning
 * a separate Android Window/ViewRootImpl. This prevents IME_INSETS_HIDE_ANIMATION timeouts in
 * FrameTracker when a modal or nested dialog is dismissed while a TextField has active IME focus.
 */
@Composable
fun AppModalOverlay(
    onDismissRequest: () -> Unit,
    zIndex: Float = 10f,
    content: @Composable () -> Unit
) {
    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) {
        onDispose {
            focusManager.clearFocus()
        }
    }

    BackHandler {
        focusManager.clearFocus()
        onDismissRequest()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(zIndex)
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                onDismissRequest()
            }
            .systemBarsPadding()
            .imePadding()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Consume clicks inside the modal card and clear any active text field focus when tapping blank card areas
                focusManager.clearFocus()
            },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
