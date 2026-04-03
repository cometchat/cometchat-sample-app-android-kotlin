package com.cometchat.uikit.compose.presentation.stickerkeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.domain.model.StickerSet

/**
 * A composable that displays a horizontal scrollable tab bar for sticker sets.
 *
 * Each tab shows the icon of the sticker set (first sticker's URL).
 * The selected tab has a visual indicator below it.
 *
 * @param stickerSets List of sticker sets to display as tabs
 * @param selectedIndex Index of the currently selected tab
 * @param modifier Modifier for the tab bar container
 * @param tabIconSize Size of each tab icon
 * @param activeIndicatorColor Color of the active tab indicator
 * @param onTabSelected Callback when a tab is selected, provides the index
 */
@Composable
internal fun StickerTabBar(
    stickerSets: List<StickerSet>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    tabIconSize: Dp = 36.dp,
    activeIndicatorColor: Color = CometChatTheme.colorScheme.primary,
    onTabSelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density

    // Auto-scroll to selected tab
    LaunchedEffect(selectedIndex) {
        val tabWidth = (tabIconSize.value + 16).toInt() // icon + padding
        val scrollPosition = (selectedIndex * tabWidth * density).toInt()
        scrollState.animateScrollTo(scrollPosition)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stickerSets.forEachIndexed { index, stickerSet ->
            StickerTabItem(
                stickerSet = stickerSet,
                isSelected = index == selectedIndex,
                tabIconSize = tabIconSize,
                activeIndicatorColor = activeIndicatorColor,
                onClick = { onTabSelected(index) }
            )
        }
    }
}


/**
 * A single tab item in the sticker tab bar.
 *
 * Displays the sticker set icon with a selection indicator below when selected.
 *
 * @param stickerSet The sticker set to display
 * @param isSelected Whether this tab is currently selected
 * @param tabIconSize Size of the tab icon
 * @param activeIndicatorColor Color of the active indicator
 * @param onClick Callback when the tab is clicked
 */
@Composable
private fun StickerTabItem(
    stickerSet: StickerSet,
    isSelected: Boolean,
    tabIconSize: Dp,
    activeIndicatorColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
            .semantics {
                contentDescription = "${stickerSet.name} sticker set"
                role = Role.Tab
                selected = isSelected
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tab icon
        Box(
            modifier = Modifier
                .size(tabIconSize)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(stickerSet.iconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stickerSet.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(tabIconSize),
                loading = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = CometChatTheme.colorScheme.iconTintSecondary,
                        strokeWidth = 2.dp
                    )
                },
                error = {
                    Box(
                        modifier = Modifier
                            .size(tabIconSize)
                            .background(
                                CometChatTheme.colorScheme.backgroundColor3,
                                RoundedCornerShape(8.dp)
                            )
                    )
                }
            )
        }

        // Selection indicator
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(width = tabIconSize, height = 3.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) activeIndicatorColor else Color.Transparent
                )
        )
    }
}
