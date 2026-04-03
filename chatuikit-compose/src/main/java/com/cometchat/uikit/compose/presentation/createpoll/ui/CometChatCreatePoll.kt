package com.cometchat.uikit.compose.presentation.createpoll.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.createpoll.style.CometChatCreatePollStyle
import org.json.JSONArray
import kotlin.math.roundToInt

/**
 * CometChatCreatePoll displays a form for creating polls.
 * Follows the same pattern as the reference Java implementation and chatuikit-kotlin.
 *
 * This composable shows:
 * - Toolbar with back button and title (left-aligned, matching Kotlin)
 * - Question input field
 * - Options list with dynamic add/remove and drag-to-reorder
 * - Submit button enabled only when valid (question + 2+ options)
 *
 * Features:
 * - Auto-add new option when last option is filled
 * - Remove empty options on focus loss (except minimum 2)
 * - Long-press drag to reorder options (same as Kotlin ItemTouchHelper)
 * - Submit button enabled only when valid (question + 2+ options)
 * - Consumer handles poll creation via onSubmitClick callback (same as reference)
 *
 * @param modifier Modifier applied to the container
 * @param style Style configuration for the component
 * @param title Optional custom title (defaults to "Create Poll")
 * @param hideToolbar Whether to hide the toolbar
 * @param isSubmitting Whether the form is currently submitting (shows loading state)
 * @param errorMessage Optional error message to display
 * @param onSubmitClick Callback invoked when submit button is clicked with question and options
 * @param onBackPress Callback invoked when back button is pressed
 */
@Composable
fun CometChatCreatePoll(
    modifier: Modifier = Modifier,
    style: CometChatCreatePollStyle = CometChatCreatePollStyle.default(),
    // Toolbar
    title: String? = null,
    hideToolbar: Boolean = false,
    // State (controlled by consumer)
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    // Callbacks - consumer handles poll creation (same as reference)
    onSubmitClick: ((question: String, options: JSONArray) -> Unit)? = null,
    onBackPress: (() -> Unit)? = null
) {
    // Local state for question and options (no ViewModel)
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    
    // Drag state
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var itemHeight by remember { mutableIntStateOf(0) }
    
    // Derived state for submit button enabled
    val isSubmitEnabled by remember {
        derivedStateOf {
            question.isNotBlank() && 
            options.getOrNull(0)?.isNotBlank() == true &&
            options.getOrNull(1)?.isNotBlank() == true
        }
    }

    val displayTitle = title ?: stringResource(R.string.cometchat_poll)

    // Main container - no corner radius to match Kotlin
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor)
            .statusBarsPadding()
    ) {
        // Toolbar - matching Kotlin layout (icon + title in row, left-aligned)
        if (!hideToolbar) {
            CreatePollToolbar(
                title = displayTitle,
                style = style,
                onBackPress = onBackPress
            )
            HorizontalDivider(
                color = style.separatorColor,
                thickness = 1.dp
            )
        }

        // Content - scrollable column (same as Kotlin RecyclerView with isNestedScrollingEnabled = false)
        val scrollState = rememberScrollState()
        
        // Auto-scroll to bottom when new option is added (same as Java reference)
        LaunchedEffect(Unit) {
            snapshotFlow { options.size }
                .distinctUntilChanged()
                .collect { size ->
                    if (size > 2) {
                        // Scroll to bottom to show newly added option
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Question section
            Text(
                text = stringResource(R.string.cometchat_question),
                style = style.questionTitleTextStyle,
                color = style.questionTitleTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuestionInput(
                value = question,
                onValueChange = { question = it },
                style = style
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Options section
            Text(
                text = stringResource(R.string.cometchat_set_the_answers),
                style = style.optionTitleTextStyle,
                color = style.optionTitleTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Options list with drag-to-reorder
            options.toList().forEachIndexed { index, option ->
                val isDragging = draggedIndex == index
                
                Box(
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = if (isDragging) dragOffsetY.roundToInt() else 0
                            )
                        }
                        .onGloballyPositioned { coordinates ->
                            if (itemHeight == 0) {
                                itemHeight = coordinates.size.height
                            }
                        }
                ) {
                    DraggablePollOptionItem(
                        index = index,
                        value = option,
                        options = options,
                        style = style,
                        isDragging = isDragging,
                        onDragStart = {
                            // Only allow drag if option has content (same as Kotlin)
                            if (option.isNotBlank()) {
                                draggedIndex = index
                                dragOffsetY = 0f
                            }
                        },
                        onDrag = { dragAmount ->
                            if (draggedIndex != -1) {
                                dragOffsetY += dragAmount
                                
                                // Calculate target index based on drag offset
                                if (itemHeight > 0) {
                                    val targetIndex = (draggedIndex + (dragOffsetY / itemHeight).roundToInt())
                                        .coerceIn(0, options.lastIndex)
                                    
                                    // Swap if moved to different position
                                    if (targetIndex != draggedIndex && 
                                        options.getOrNull(targetIndex)?.isNotBlank() == true) {
                                        val item = options.removeAt(draggedIndex)
                                        options.add(targetIndex, item)
                                        draggedIndex = targetIndex
                                        dragOffsetY = 0f
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            draggedIndex = -1
                            dragOffsetY = 0f
                        }
                    )
                }
            }
        }

        // Bottom section with error and submit button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp)
                .navigationBarsPadding()
        ) {
            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = style.errorTextStyle,
                    color = style.errorTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Submit button
            SubmitButton(
                isEnabled = isSubmitEnabled && !isSubmitting,
                isLoading = isSubmitting,
                style = style,
                onClick = {
                    val processedOptions = JSONArray()
                    options.forEachIndexed { idx, opt ->
                        if (opt.isNotBlank()) {
                            processedOptions.put(idx, opt.trim())
                        }
                    }
                    onSubmitClick?.invoke(question.trim(), processedOptions)
                }
            )
        }
    }
}

/**
 * Draggable poll option item with add/remove logic and long-press drag support.
 */
@Composable
private fun DraggablePollOptionItem(
    index: Int,
    value: String,
    options: MutableList<String>,
    style: CometChatCreatePollStyle,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val maxOptions = 12
    val showDragHandle = value.isNotBlank()
    val density = LocalDensity.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(
                if (showDragHandle) {
                    Modifier.pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Option input field
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                // Update the option directly
                if (index < options.size) {
                    options[index] = newValue
                }
                
                // Auto-add logic (same as Kotlin reference)
                if (newValue.isNotBlank()) {
                    // Add 3rd option when both first two are filled
                    if (options.size == 2) {
                        val first = options.getOrNull(0)?.trim() ?: ""
                        val second = options.getOrNull(1)?.trim() ?: ""
                        if (first.isNotEmpty() && second.isNotEmpty()) {
                            options.add("")
                        }
                    } else if (index == options.lastIndex && options.size < maxOptions) {
                        // Add new option when last option is filled
                        options.add("")
                    }
                }
            },
            modifier = Modifier
                .weight(1f)
                .background(
                    color = style.optionBackgroundColor,
                    shape = RoundedCornerShape(style.optionCornerRadius)
                )
                .border(
                    width = style.optionStrokeWidth,
                    color = style.optionStrokeColor,
                    shape = RoundedCornerShape(style.optionCornerRadius)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .onFocusChanged { focusState ->
                    // Remove empty options on focus loss (same as Kotlin reference)
                    if (!focusState.isFocused && index < options.size) {
                        if (options[index].isBlank()) {
                            // Don't remove if only 2 options remain
                            // Don't remove the last option (placeholder for new input)
                            if (options.size > 2 && index != options.lastIndex) {
                                options.removeAt(index)
                            }
                        }
                    }
                }
                .semantics {
                    contentDescription = "Poll option ${index + 1}"
                },
            textStyle = style.optionTextStyle.copy(color = style.optionTextColor),
            cursorBrush = SolidColor(style.optionTextColor),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.cometchat_add),
                            style = style.optionTextStyle,
                            color = style.optionHintColor
                        )
                    }
                    innerTextField()
                }
            }
        )
        
        // Drag handle icon (visible only when option has text) - same as Kotlin
        if (showDragHandle) {
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_menu),
                contentDescription = stringResource(R.string.cometchat_drag_to_reorder),
                tint = style.dragIconTint,
                modifier = Modifier
                    .size(24.dp)
                    .semantics {
                        contentDescription = "Drag handle for option ${index + 1}"
                    }
            )
        }
    }
}


/**
 * Toolbar with back button and title - matching Kotlin layout (horizontal row).
 */
@Composable
private fun CreatePollToolbar(
    title: String,
    style: CometChatCreatePollStyle,
    onBackPress: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button (back arrow icon, matching Kotlin)
        Icon(
            painter = painterResource(id = R.drawable.cometchat_ic_back),
            contentDescription = stringResource(R.string.cometchat_go_back),
            tint = style.backIconTint,
            modifier = Modifier
                .size(24.dp)
                .clickable(enabled = onBackPress != null) { onBackPress?.invoke() }
                .semantics {
                    role = Role.Button
                    contentDescription = "Go back from create poll"
                }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Title - left aligned next to icon (matching Kotlin layout)
        Text(
            text = title,
            style = style.titleTextStyle,
            color = style.titleTextColor
        )
    }
}

/**
 * Question input field.
 */
@Composable
private fun QuestionInput(
    value: String,
    onValueChange: (String) -> Unit,
    style: CometChatCreatePollStyle
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = style.questionBackgroundColor,
                shape = RoundedCornerShape(style.questionCornerRadius)
            )
            .border(
                width = style.questionStrokeWidth,
                color = style.questionStrokeColor,
                shape = RoundedCornerShape(style.questionCornerRadius)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = "Poll question input"
            },
        textStyle = style.questionTextStyle.copy(color = style.questionTextColor),
        cursorBrush = SolidColor(style.questionTextColor),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.cometchat_ask_question),
                        style = style.questionTextStyle,
                        color = style.questionHintColor
                    )
                }
                innerTextField()
            }
        }
    )
}

/**
 * Submit button with loading state.
 */
@Composable
private fun SubmitButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    style: CometChatCreatePollStyle,
    onClick: () -> Unit
) {
    val backgroundColor = if (isEnabled) {
        style.submitButtonBackgroundColor
    } else {
        style.submitButtonDisabledBackgroundColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(style.submitButtonCornerRadius))
            .background(backgroundColor)
            .border(
                width = style.submitButtonStrokeWidth,
                color = style.submitButtonStrokeColor,
                shape = RoundedCornerShape(style.submitButtonCornerRadius)
            )
            .clickable(enabled = isEnabled && !isLoading) { onClick() }
            .padding(vertical = 12.dp)
            .semantics {
                role = Role.Button
                contentDescription = if (isLoading) {
                    "Creating poll, please wait"
                } else if (isEnabled) {
                    "Send poll"
                } else {
                    "Send poll button disabled"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = style.progressIndicatorColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = stringResource(R.string.cometchat_send),
                style = style.submitButtonTextStyle,
                color = style.submitButtonTextColor
            )
        }
    }
}
