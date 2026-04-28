package com.cometchat.uikit.compose.presentation.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.SearchFilter

/**
 * Chip dimension constants matching Kotlin implementation visual appearance.
 * 
 * Kotlin source values:
 * - cometchat_14dp = 14dp (icon size)
 * - cometchat_margin_1 = 4dp (icon margin end)
 * - cometchat_padding_3 = 12dp (horizontal padding)
 * - cometchat_padding_2 = 8dp (vertical padding)
 * - cometchat_margin_2 = 8dp (chip spacing)
 * - cometchat_margin_4 = 16dp (chip group horizontal margin)
 * - cometchat_margin_1 = 4dp (chip group vertical margin)
 * 
 * Note: Compose values are slightly adjusted to match the visual appearance
 * of the Kotlin/View-based implementation on device.
 */
private object ChipDimens {
    val iconSize = 14.dp           // cometchat_14dp
    val iconMarginEnd = 4.dp       // cometchat_margin_1
    val horizontalPadding = 12.dp  // cometchat_padding_3
    val verticalPadding = 8.dp     // cometchat_padding_2
    val chipSpacing = 8.dp         // cometchat_margin_2
    val chipGroupHorizontalMargin = 16.dp  // cometchat_margin_4
    val chipGroupVerticalMargin = 4.dp     // cometchat_margin_1
}

/**
 * A composable that displays a wrapping flow of filter chips with icons.
 * Chips wrap to the next line when they don't fit in the available width.
 * 
 * Structure mirrors Kotlin implementation:
 * - FlowRow (chip_group / FlexboxLayout)
 *   - FilterChipCard (chipCard / MaterialCardView)
 *     - ChipContainer (chipContainer / LinearLayout)
 *       - ChipIconView (chipIconView / ImageView)
 *       - ChipTextView (chipTextView / TextView)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchFilterChips(
    filters: List<SearchFilter>,
    selectedFilters: Set<SearchFilter>,
    onFilterToggle: (SearchFilter) -> Unit,
    modifier: Modifier = Modifier,
    style: CometChatSearchStyle = CometChatSearchStyle.default()
) {
    // Matching Kotlin chip_group (FlexboxLayout) constraints:
    // - marginHorizontal = 16dp (cometchat_margin_4)
    // - marginVertical = 4dp (cometchat_margin_1)
    // - flexWrap = wrap
    // - justifyContent = flex_start
    FlowRow(
        modifier = modifier
            .padding(
                horizontal = ChipDimens.chipGroupHorizontalMargin,
                vertical = ChipDimens.chipGroupVerticalMargin
            ),
        horizontalArrangement = Arrangement.spacedBy(ChipDimens.chipSpacing),
        verticalArrangement = Arrangement.spacedBy(ChipDimens.chipSpacing)
    ) {
        filters.forEach { filter ->
            val isSelected = filter in selectedFilters
            FilterChipCard(
                filter = filter,
                isSelected = isSelected,
                onClick = { onFilterToggle(filter) },
                style = style
            )
        }
    }
}

/**
 * FilterChipCard - Equivalent to Kotlin's chipCard (MaterialCardView)
 * Wraps the ChipContainer and handles click, background, border styling.
 */
@Composable
private fun FilterChipCard(
    filter: SearchFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
    style: CometChatSearchStyle
) {
    val context = LocalContext.current
    val (iconRes, label) = filter.getIconAndLabel(context)
    
    val backgroundColor = if (isSelected) {
        style.filterChipSelectedBackgroundColor
    } else {
        style.filterChipBackgroundColor
    }
    val strokeColor = if (isSelected) {
        style.filterChipSelectedStrokeColor
    } else {
        style.filterChipStrokeColor
    }
    val textColor = if (isSelected) {
        style.filterChipSelectedTextColor
    } else {
        style.filterChipTextColor
    }
    
    val shape = RoundedCornerShape(style.filterChipCornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    // ChipCard styling (MaterialCardView equivalent)
    // Apply minimum height to ensure consistent sizing across devices
    ChipContainer(
        modifier = Modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .border(
                width = style.filterChipStrokeWidth,
                color = strokeColor,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Checkbox
                selected = isSelected
                contentDescription = "$label filter${if (isSelected) ", selected" else ""}"
            },
        iconRes = iconRes,
        label = label,
        textColor = textColor,
        textStyle = style.filterChipTextStyle
    )
}

/**
 * ChipContainer - Equivalent to Kotlin's chipContainer (LinearLayout)
 * Contains the icon and text with proper padding and alignment.
 * 
 * Kotlin constraints:
 * - orientation = HORIZONTAL
 * - gravity = CENTER
 * - padding: left/right = 12dp (cometchat_padding_3), top/bottom = 8dp (cometchat_padding_2)
 */
@Composable
private fun ChipContainer(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    textColor: Color,
    textStyle: TextStyle
) {
    Row(
        modifier = modifier
            .padding(
                horizontal = ChipDimens.horizontalPadding,
                vertical = ChipDimens.verticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        ChipIconView(
            iconRes = iconRes,
            tint = textColor
        )
        Spacer(modifier = Modifier.width(ChipDimens.iconMarginEnd))
        ChipTextView(
            text = label,
            color = textColor,
            style = textStyle
        )
    }
}

/**
 * ChipIconView - Equivalent to Kotlin's chipIconView (ImageView)
 * 
 * Kotlin constraints:
 * - Size: 14dp x 14dp (cometchat_14dp)
 * - marginEnd = 4dp (cometchat_margin_1) - handled by Spacer in ChipContainer
 * - scaleType = FIT_CENTER
 */
@Composable
private fun ChipIconView(
    iconRes: Int,
    tint: Color
) {
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(ChipDimens.iconSize)
    )
}

/**
 * ChipTextView - Equivalent to Kotlin's chipTextView (TextView)
 * 
 * Kotlin constraints:
 * - gravity = CENTER
 * - maxLines = 1
 * - ellipsize = END
 */
@Composable
private fun ChipTextView(
    text: String,
    color: Color,
    style: TextStyle
) {
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = 1
    )
}

/**
 * Returns the icon resource and display label for a SearchFilter.
 */
private fun SearchFilter.getIconAndLabel(context: android.content.Context): Pair<Int, String> {
    return when (this) {
        SearchFilter.UNREAD -> Pair(
            R.drawable.cometchat_ic_unread_outlined,
            context.getString(R.string.cometchat_unread)
        )
        SearchFilter.GROUPS -> Pair(
            R.drawable.cometchat_ic_group_outlined,
            context.getString(R.string.cometchat_groups)
        )
        SearchFilter.PHOTOS -> Pair(
            R.drawable.cometchat_ic_photo_outlined,
            context.getString(R.string.cometchat_photos)
        )
        SearchFilter.VIDEOS -> Pair(
            R.drawable.cometchat_ic_video_outlined,
            context.getString(R.string.cometchat_videos)
        )
        SearchFilter.LINKS -> Pair(
            R.drawable.cometchat_ic_link_outlined,
            context.getString(R.string.cometchat_links)
        )
        SearchFilter.DOCUMENTS -> Pair(
            R.drawable.cometchat_ic_document_outlined,
            context.getString(R.string.cometchat_documents)
        )
        SearchFilter.AUDIO -> Pair(
            R.drawable.cometchat_ic_audio_outlined,
            context.getString(R.string.cometchat_audio)
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, name = "SearchFilterChips - None Selected")
@Composable
private fun SearchFilterChipsPreview() {
    CometChatTheme {
        SearchFilterChips(
            filters = listOf(
                SearchFilter.UNREAD,
                SearchFilter.GROUPS,
                SearchFilter.PHOTOS,
                SearchFilter.VIDEOS,
                SearchFilter.LINKS,
                SearchFilter.DOCUMENTS,
                SearchFilter.AUDIO
            ),
            selectedFilters = emptySet(),
            onFilterToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "SearchFilterChips - With Selection")
@Composable
private fun SearchFilterChipsWithSelectionPreview() {
    CometChatTheme {
        SearchFilterChips(
            filters = listOf(
                SearchFilter.UNREAD,
                SearchFilter.GROUPS,
                SearchFilter.PHOTOS,
                SearchFilter.VIDEOS,
                SearchFilter.LINKS,
                SearchFilter.DOCUMENTS,
                SearchFilter.AUDIO
            ),
            selectedFilters = setOf(SearchFilter.UNREAD, SearchFilter.GROUPS),
            onFilterToggle = {}
        )
    }
}
