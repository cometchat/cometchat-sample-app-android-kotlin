package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicator
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.compose.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// Preview: Individual Status States
// ============================================================================

/**
 * Preview showing the ONLINE status indicator.
 */
@Preview(showBackground = true, name = "StatusIndicator - Online")
@Composable
fun PreviewStatusIndicatorOnline() {
    CometChatTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CometChatStatusIndicator(
                status = StatusIndicator.ONLINE
            )
        }
    }
}


/**
 * Preview showing the OFFLINE status indicator (should be hidden).
 */
@Preview(showBackground = true, name = "StatusIndicator - Offline (Hidden)")
@Composable
fun PreviewStatusIndicatorOffline() {
    CometChatTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // This should not render anything
            CometChatStatusIndicator(
                status = StatusIndicator.OFFLINE
            )
            // Show placeholder text to indicate nothing is rendered
            Text(
                text = "(Hidden - OFFLINE)",
                color = CometChatTheme.colorScheme.textColorTertiary
            )
        }
    }
}

/**
 * Preview showing the PUBLIC_GROUP status indicator (should be hidden).
 */
@Preview(showBackground = true, name = "StatusIndicator - Public Group (Hidden)")
@Composable
fun PreviewStatusIndicatorPublicGroup() {
    CometChatTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // This should not render anything
            CometChatStatusIndicator(
                status = StatusIndicator.PUBLIC_GROUP
            )
            // Show placeholder text to indicate nothing is rendered
            Text(
                text = "(Hidden - PUBLIC_GROUP)",
                color = CometChatTheme.colorScheme.textColorTertiary
            )
        }
    }
}

/**
 * Preview showing the PRIVATE_GROUP status indicator.
 */
@Preview(showBackground = true, name = "StatusIndicator - Private Group")
@Composable
fun PreviewStatusIndicatorPrivateGroup() {
    CometChatTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CometChatStatusIndicator(
                status = StatusIndicator.PRIVATE_GROUP
            )
        }
    }
}

/**
 * Preview showing the PROTECTED_GROUP status indicator.
 */
@Preview(showBackground = true, name = "StatusIndicator - Protected Group")
@Composable
fun PreviewStatusIndicatorProtectedGroup() {
    CometChatTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CometChatStatusIndicator(
                status = StatusIndicator.PROTECTED_GROUP
            )
        }
    }
}

// ============================================================================
// Preview: All Status States
// ============================================================================

/**
 * Preview showing all status indicator states in a row.
 */
@Preview(showBackground = true, name = "StatusIndicator - All States")
@Composable
fun PreviewStatusIndicatorAllStates() {
    CometChatTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusIndicator.values().forEach { status ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CometChatStatusIndicator(status = status)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = status.name,
                        color = CometChatTheme.colorScheme.textColorPrimary
                    )
                }
            }
        }
    }
}

// ============================================================================
// Preview: With Avatar Context
// ============================================================================

/**
 * Preview showing status indicator overlaid on a mock avatar (simulated).
 */
@Preview(showBackground = true, name = "StatusIndicator - On Avatar")
@Composable
fun PreviewStatusIndicatorOnAvatar() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Online user
            Box(
                modifier = Modifier.size(48.dp)
            ) {
                // Mock avatar background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = CometChatTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        color = Color.White
                    )
                }
                // Status indicator at bottom-end
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(15.dp)
                ) {
                    CometChatStatusIndicator(
                        status = StatusIndicator.ONLINE
                    )
                }
            }
            
            // Private group
            Box(
                modifier = Modifier.size(48.dp)
            ) {
                // Mock avatar background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = CometChatTheme.colorScheme.infoColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PG",
                        color = Color.White
                    )
                }
                // Status indicator at bottom-end
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(15.dp)
                ) {
                    CometChatStatusIndicator(
                        status = StatusIndicator.PRIVATE_GROUP
                    )
                }
            }
            
            // Protected group
            Box(
                modifier = Modifier.size(48.dp)
            ) {
                // Mock avatar background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = CometChatTheme.colorScheme.warningColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PW",
                        color = Color.White
                    )
                }
                // Status indicator at bottom-end
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(15.dp)
                ) {
                    CometChatStatusIndicator(
                        status = StatusIndicator.PROTECTED_GROUP
                    )
                }
            }
        }
    }
}

// ============================================================================
// Preview: Custom Styling
// ============================================================================

/**
 * Preview showing status indicator with custom styling.
 */
@Preview(showBackground = true, name = "StatusIndicator - Custom Style")
@Composable
fun PreviewStatusIndicatorCustomStyle() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Default size (15dp)
            CometChatStatusIndicator(
                status = StatusIndicator.ONLINE
            )
            
            // Larger size
            CometChatStatusIndicator(
                status = StatusIndicator.ONLINE,
                style = CometChatStatusIndicatorStyle.default(
                    size = 24.dp,
                    strokeWidth = 3.dp
                )
            )
            
            // Custom stroke color
            CometChatStatusIndicator(
                status = StatusIndicator.PRIVATE_GROUP,
                style = CometChatStatusIndicatorStyle.default(
                    strokeColor = CometChatTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            )
        }
    }
}
