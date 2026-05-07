package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.uikit.compose.presentation.calllogs.style.CometChatCallLogsStyle
import com.cometchat.uikit.compose.presentation.calllogs.ui.CometChatCallLogs
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.core.factory.CometChatCallLogsViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatCallLogsViewModel
import com.cometchat.uikit.compose.preview.data.repository.PreviewCallLogsRepository
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Helper to create a preview ViewModel with mock data using the factory pattern.
 */
@Composable
private fun rememberPreviewCallLogsViewModel(
    simulateError: Boolean = false,
    simulateEmpty: Boolean = true
): CometChatCallLogsViewModel {
    val factory = remember(simulateError, simulateEmpty) {
        CometChatCallLogsViewModelFactory(
            repository = PreviewCallLogsRepository(
                simulateError = simulateError,
                simulateEmpty = simulateEmpty
            ),
            enableListeners = false
        )
    }
    return viewModel(factory = factory)
}

// ============================================================================
// SECTION 1: UI STATE PREVIEWS
// ============================================================================

/**
 * Preview showing the loading state.
 */
@Preview(showBackground = true, name = "CallLogs - State - Loading")
@Composable
fun PreviewCallLogsLoading() {
    CometChatTheme {
        val style = CometChatCallLogsStyle.default()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            CometChatToolbar(
                title = "Calls",
                style = CometChatToolbarStyle.default(
                    backgroundColor = style.backgroundColor,
                    titleTextColor = style.titleTextColor,
                    titleTextStyle = style.titleTextStyle
                ),
                hideBackIcon = true
            )
            CometChatLoadingState(style = style.loadingStateStyle)
        }
    }
}

/**
 * Preview showing the content state.
 * Note: Without a mock CallLog repository, this uses the default ViewModel
 * which will show loading then empty/error since SDK is not initialized.
 */
@Preview(showBackground = true, name = "CallLogs - Default (Empty)")
@Composable
fun PreviewCallLogsDefault() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            title = "Calls",
            hideBackButton = true
        )
    }
}

/**
 * Preview showing the error state.
 */
@Preview(showBackground = true, name = "CallLogs - State - Error")
@Composable
fun PreviewCallLogsError() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel(simulateError = true)
        CometChatCallLogs(
            viewModel = vm,
            title = "Calls",
            hideBackButton = true
        )
    }
}

// ============================================================================
// SECTION 2: VISIBILITY PREVIEWS
// ============================================================================

/**
 * Preview without toolbar.
 */
@Preview(showBackground = true, name = "CallLogs - Visibility - No Toolbar")
@Composable
fun PreviewCallLogsNoToolbar() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            hideToolbar = true
        )
    }
}

/**
 * Preview with back button visible.
 */
@Preview(showBackground = true, name = "CallLogs - Visibility - With Back Button")
@Composable
fun PreviewCallLogsWithBackButton() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            title = "Calls",
            hideBackButton = false,
            onBackPress = { }
        )
    }
}

/**
 * Preview with hidden title.
 */
@Preview(showBackground = true, name = "CallLogs - Visibility - No Title")
@Composable
fun PreviewCallLogsNoTitle() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            hideTitle = true,
            hideBackButton = true
        )
    }
}

/**
 * Preview without separators.
 */
@Preview(showBackground = true, name = "CallLogs - Visibility - No Separators")
@Composable
fun PreviewCallLogsNoSeparators() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            title = "Calls",
            hideBackButton = true,
            hideSeparator = true
        )
    }
}

// ============================================================================
// SECTION 3: CUSTOM VIEW OVERRIDES
// ============================================================================

/**
 * Preview with custom empty view.
 */
@Preview(showBackground = true, name = "CallLogs - Custom View - Empty")
@Composable
fun PreviewCallLogsCustomEmptyView() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            title = "Calls",
            hideBackButton = true,
            emptyView = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📞",
                            style = CometChatTheme.typography.heading1Bold
                        )
                        Text(
                            text = "No call history",
                            style = CometChatTheme.typography.heading3Medium,
                            color = CometChatTheme.colorScheme.textColorPrimary,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = "Your call logs will appear here",
                            style = CometChatTheme.typography.bodyRegular,
                            color = CometChatTheme.colorScheme.textColorSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        )
    }
}

// ============================================================================
// SECTION 4: TOOLBAR CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom title.
 */
@Preview(showBackground = true, name = "CallLogs - Toolbar - Custom Title")
@Composable
fun PreviewCallLogsCustomTitle() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            title = "Call History",
            hideBackButton = true
        )
    }
}

/**
 * Preview with overflow menu.
 */
@Preview(showBackground = true, name = "CallLogs - Toolbar - Overflow Menu")
@Composable
fun PreviewCallLogsOverflowMenu() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            title = "Calls",
            hideBackButton = true,
            overflowMenu = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = CometChatTheme.colorScheme.iconTintPrimary
                    )
                }
            }
        )
    }
}

// ============================================================================
// SECTION 5: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "CallLogs - Style - Custom Background")
@Composable
fun PreviewCallLogsCustomBackground() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            title = "Calls",
            hideBackButton = true,
            style = CometChatCallLogsStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            )
        )
    }
}

/**
 * Preview with custom title color.
 */
@Preview(showBackground = true, name = "CallLogs - Style - Custom Title Color")
@Composable
fun PreviewCallLogsCustomTitleColor() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            title = "Calls",
            hideBackButton = true,
            style = CometChatCallLogsStyle.default(
                titleTextColor = CometChatTheme.colorScheme.primary
            )
        )
    }
}

// ============================================================================
// SECTION 6: COMPREHENSIVE PREVIEWS
// ============================================================================

/**
 * Preview showing all features combined.
 */
@Preview(showBackground = true, name = "CallLogs - Comprehensive - All Features")
@Composable
fun PreviewCallLogsComprehensive() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            title = "Call History",
            hideBackButton = false,
            overflowMenu = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = CometChatTheme.colorScheme.iconTintPrimary
                    )
                }
            },
            onBackPress = { },
            onItemClick = { }
        )
    }
}

/**
 * Preview showing minimal configuration.
 */
@Preview(showBackground = true, name = "CallLogs - Comprehensive - Minimal")
@Composable
fun PreviewCallLogsMinimal() {
    CometChatTheme {
        val vm = rememberPreviewCallLogsViewModel()
        CometChatCallLogs(
            viewModel = vm,
            hideToolbar = true,
            hideSeparator = true
        )
    }
}
