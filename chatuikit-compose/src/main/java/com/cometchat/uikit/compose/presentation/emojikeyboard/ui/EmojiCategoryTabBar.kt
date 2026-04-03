package com.cometchat.uikit.compose.presentation.emojikeyboard.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.emojikeyboard.model.EmojiCategory
import com.cometchat.uikit.compose.presentation.emojikeyboard.style.CometChatEmojiKeyboardStyle

/**
 * Resolves the drawable resource ID for a given emoji category ID.
 *
 * Maps known category IDs to their corresponding drawable resources,
 * falling back to the category's [fallbackSymbol] for unknown IDs.
 *
 * @param categoryId The category identifier (e.g., "people", "flags")
 * @param fallbackSymbol The drawable resource to use if the category ID is not recognized
 * @return The resolved drawable resource ID
 */
@DrawableRes
internal fun getCategoryIconResId(categoryId: String, @DrawableRes fallbackSymbol: Int): Int {
    return when (categoryId.lowercase()) {
        "people" -> R.drawable.cometchat_smileys
        "animals_and_nature" -> R.drawable.cometchat_animals
        "food_and_drink" -> R.drawable.cometchat_food
        "activity" -> R.drawable.cometchat_activity
        "travel_and_places" -> R.drawable.cometchat_travel
        "objects" -> R.drawable.cometchat_objects
        "symbols" -> R.drawable.cometchat_symbols
        "flags" -> R.drawable.cometchat_flags
        else -> fallbackSymbol
    }
}


/**
 * A bottom tab bar displaying category icons for the emoji keyboard.
 *
 * Renders a horizontally scrollable row of icon-only tabs, one per [EmojiCategory].
 * The selected tab is highlighted with an oval background and distinct icon tint.
 * Ripple effect is disabled to match the existing View-based implementation.
 *
 * @param categories List of emoji categories to display as tabs
 * @param selectedIndex Index of the currently selected tab
 * @param modifier Modifier applied to the tab bar container
 * @param style Style configuration controlling icon sizes, tints, and background colors
 * @param onTabSelected Callback invoked with the tab index when a tab is tapped
 */
@Composable
internal fun EmojiCategoryTabBar(
    categories: List<EmojiCategory>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    style: CometChatEmojiKeyboardStyle,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = style.categoryIconTint,
        edgePadding = 8.dp,
        indicator = {},
        divider = {}
    ) {
        categories.forEachIndexed { index, category ->
            val isSelected = index == selectedIndex
            val iconResId = remember(category.id, category.symbol) {
                getCategoryIconResId(category.id, category.symbol)
            }

            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                interactionSource = remember { MutableInteractionSource() },
                modifier = Modifier.semantics {
                    contentDescription = category.name
                    role = Role.Tab
                    selected = isSelected
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(style.tabBackgroundSize)
                        .background(
                            color = if (isSelected) style.selectedCategoryBackgroundColor
                            else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(style.tabIconSize),
                        tint = if (isSelected) style.selectedCategoryIconTint
                        else style.categoryIconTint
                    )
                }
            }
        }
    }
}