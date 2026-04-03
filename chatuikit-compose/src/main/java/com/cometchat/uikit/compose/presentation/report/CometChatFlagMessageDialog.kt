package com.cometchat.uikit.compose.presentation.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cometchat.chat.models.FlagDetail
import com.cometchat.chat.models.FlagReason
import com.cometchat.uikit.compose.R

/**
 * A dialog component for reporting/flagging messages.
 *
 * This composable provides full parity with the Kotlin XML `CometChatFlagMessageDialog` implementation,
 * matching the visual layout and behavior:
 * - Title with close button
 * - Description text
 * - Selectable flag reason chips
 * - Optional remark input field
 * - Cancel and Report buttons with progress indicator support
 *
 * Layout structure:
 * ```
 * ┌─────────────────────────────────┐
 * │  Report                    [X]  │
 * │                                 │
 * │  Why are you reporting...       │
 * │                                 │
 * │  [Spam] [Sexual] [Harassment]   │
 * │                                 │
 * │  Reason (Optional)              │
 * │  ┌─────────────────────────┐    │
 * │  │ Add remark...           │    │
 * │  └─────────────────────────┘    │
 * │                                 │
 * │   [Cancel]      [Report]        │
 * └─────────────────────────────────┘
 * ```
 *
 * @param flagReasons List of flag reasons to display as selectable chips
 * @param style Visual styling configuration via [CometChatFlagMessageDialogStyle]
 * @param localizationMap Optional map of flag reason IDs to string resource IDs for localization
 * @param title Custom title text, or null to use default string resource
 * @param description Custom description text, or null to use default string resource
 * @param remarkHint Custom hint text for the remark input field, or null to use default string resource
 * @param cancelButtonText Custom text for the cancel button, or null to use default string resource
 * @param reportButtonText Custom text for the report button, or null to use default string resource
 * @param showRemarkField Whether to show the remark input field (default true)
 * @param showError Whether to show the error message
 * @param showProgress Whether to show progress indicator on report button
 * @param dismissOnBackPress Whether dialog dismisses on back press (default true)
 * @param dismissOnClickOutside Whether dialog dismisses on outside click (default false)
 * @param onReportClick Callback invoked when report button is clicked with FlagDetail
 * @param onCancelClick Callback invoked when cancel button is clicked
 * @param onCloseClick Callback invoked when close button is clicked
 * @param onDismiss Callback invoked when dialog is dismissed
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CometChatFlagMessageDialog(
    flagReasons: List<FlagReason>,
    style: CometChatFlagMessageDialogStyle = CometChatFlagMessageDialogStyle.default(),
    localizationMap: Map<String, Int> = emptyMap(),
    title: String? = null,
    description: String? = null,
    remarkHint: String? = null,
    cancelButtonText: String? = null,
    reportButtonText: String? = null,
    showRemarkField: Boolean = true,
    showError: Boolean = false,
    showProgress: Boolean = false,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = false,
    onReportClick: (FlagDetail) -> Unit,
    onCancelClick: () -> Unit,
    onCloseClick: () -> Unit = onCancelClick,
    onDismiss: () -> Unit = onCancelClick
) {
    val context = LocalContext.current
    
    // State
    var selectedChipIndex by remember { mutableIntStateOf(-1) }
    var remarkText by remember { mutableStateOf("") }
    
    // Default localization map
    val defaultLocalizationMap = remember {
        mapOf(
            "spam" to R.string.cometchat_flag_reason_spam,
            "sexual" to R.string.cometchat_flag_reason_sexual,
            "harassment" to R.string.cometchat_flag_reason_harassment
        )
    }
    val effectiveLocalizationMap = remember(localizationMap) {
        defaultLocalizationMap + localizationMap
    }
    
    // Helper function to get localized flag reason name
    fun getLocalizedReasonName(flagReason: FlagReason): String {
        val reasonId = flagReason.id?.lowercase() ?: return flagReason.name ?: ""
        val resourceId = effectiveLocalizationMap[reasonId]
        return if (resourceId != null) {
            try {
                context.getString(resourceId)
            } catch (e: Exception) {
                flagReason.name ?: ""
            }
        } else {
            flagReason.name ?: ""
        }
    }
    
    val isReportEnabled = selectedChipIndex >= 0
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .semantics { contentDescription = "Flag Message Dialog" },
            shape = RoundedCornerShape(style.borderRadius),
            colors = CardDefaults.cardColors(containerColor = style.backgroundColor),
            border = if (style.strokeWidth > 0.dp) {
                BorderStroke(style.strokeWidth, style.strokeColor)
            } else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title ?: stringResource(R.string.cometchat_report),
                        style = style.titleTextStyle,
                        color = style.titleColor
                    )
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cometchat_ic_close),
                            contentDescription = "Close",
                            tint = style.closeIconColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description
                Text(
                    text = description ?: stringResource(R.string.cometchat_report_message_description),
                    style = style.subtitleTextStyle,
                    color = style.subtitleColor
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Separator line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(style.separatorColor)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Flag reason chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = Int.MAX_VALUE
                ) {
                    flagReasons.forEachIndexed { index, reason ->
                        val isSelected = selectedChipIndex == index
                        FlagReasonChip(
                            text = getLocalizedReasonName(reason),
                            isSelected = isSelected,
                            style = style,
                            onClick = { selectedChipIndex = index }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Remark field section
                if (showRemarkField) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.cometchat_report_reason_label),
                            style = style.remarkFieldTitleTextStyle,
                            color = style.remarkFieldTitleTextColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.cometchat_report_optional_label),
                            style = style.remarkFieldTitleTextStyle,
                            color = style.remarkFieldHintTextColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Remark input field
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = style.remarkFieldBackgroundColor,
                        border = BorderStroke(1.dp, style.remarkFieldStrokeColor)
                    ) {
                        BasicTextField(
                            value = remarkText,
                            onValueChange = { remarkText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textStyle = style.remarkFieldTextStyle.copy(color = style.remarkFieldTextColor),
                            cursorBrush = SolidColor(style.remarkFieldTextColor),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (remarkText.isEmpty()) {
                                        Text(
                                            text = remarkHint ?: stringResource(R.string.cometchat_report_remark_hint),
                                            style = style.remarkFieldTextStyle,
                                            color = style.remarkFieldHintTextColor
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Error message
                if (showError) {
                    Text(
                        text = stringResource(R.string.cometchat_report_error_message),
                        style = style.errorTextStyle,
                        color = style.errorTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    FlagDialogButton(
                        modifier = Modifier.weight(1f),
                        text = cancelButtonText ?: stringResource(R.string.cometchat_cancel),
                        textColor = style.cancelButtonEnabledTextColor,
                        textStyle = style.cancelButtonTextStyle,
                        backgroundColor = style.cancelButtonEnabledBackgroundColor,
                        strokeColor = style.buttonStrokeColor,
                        strokeWidth = style.buttonStrokeWidth,
                        cornerRadius = style.buttonCornerRadius,
                        showProgress = false,
                        onClick = onCancelClick
                    )
                    
                    // Report button
                    FlagDialogButton(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (isReportEnabled) 1f else 0.6f),
                        text = reportButtonText ?: stringResource(R.string.cometchat_report),
                        textColor = if (isReportEnabled) {
                            style.reportButtonEnabledTextColor
                        } else {
                            style.reportButtonDisabledTextColor
                        },
                        textStyle = style.reportButtonTextStyle,
                        backgroundColor = if (isReportEnabled) {
                            style.reportButtonEnabledBackgroundColor
                        } else {
                            style.reportButtonDisabledBackgroundColor
                        },
                        strokeColor = Color.Transparent,
                        strokeWidth = 0.dp,
                        cornerRadius = style.buttonCornerRadius,
                        showProgress = showProgress,
                        progressColor = style.progressIndicatorColor,
                        enabled = isReportEnabled,
                        onClick = {
                            if (isReportEnabled && selectedChipIndex >= 0 && selectedChipIndex < flagReasons.size) {
                                val selectedReason = flagReasons[selectedChipIndex]
                                val flagDetail = FlagDetail().apply {
                                    reasonId = selectedReason.id
                                    remark = remarkText
                                }
                                onReportClick(flagDetail)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Internal composable for flag reason chips.
 */
@Composable
private fun FlagReasonChip(
    text: String,
    isSelected: Boolean,
    style: CometChatFlagMessageDialogStyle,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        style.chipActiveBackgroundColor
    } else {
        style.chipInactiveBackgroundColor
    }
    val textColor = if (isSelected) {
        style.chipActiveTextColor
    } else {
        style.chipInactiveTextColor
    }
    val borderColor = if (isSelected) {
        style.chipActiveBorderColor
    } else {
        style.chipInactiveBorderColor
    }
    
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(style.chipCornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(style.chipStrokeWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = style.chipTextStyle,
            color = textColor
        )
    }
}

/**
 * Internal composable for dialog buttons.
 */
@Composable
private fun FlagDialogButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
    textStyle: androidx.compose.ui.text.TextStyle,
    backgroundColor: Color,
    strokeColor: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp,
    showProgress: Boolean,
    progressColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (strokeWidth > 0.dp) {
            BorderStroke(strokeWidth, strokeColor)
        } else null,
        onClick = { if (enabled) onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(19.dp),
                    color = progressColor,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    style = textStyle,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
