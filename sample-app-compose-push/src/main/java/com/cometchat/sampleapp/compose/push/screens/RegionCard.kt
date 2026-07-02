package com.cometchat.sampleapp.compose.push.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.R

/**
 * Composable for displaying a region selection card.
 * Matches the XML layout in master-app-kotlin activity_app_credentials.xml exactly.
 * Uses horizontal layout with flag icon + text (not vertical with emoji).
 */
@Composable
fun RegionCard(
    region: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography
    
    val borderColor = if (isSelected) {
        colorScheme.strokeColorHighlight
    } else {
        colorScheme.strokeColorDefault
    }
    
    val backgroundColor = if (isSelected) {
        colorScheme.extendedPrimaryColor50
    } else {
        colorScheme.backgroundColor1
    }
    
    // Get flag drawable based on region
    val flagDrawable = when (region) {
        "us" -> R.drawable.ic_flag_us
        "eu" -> R.drawable.ic_flag_eu
        "in" -> R.drawable.ic_flag_india
        else -> R.drawable.ic_flag_us
    }
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), // cometchat_radius_2 = 8dp
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor), // cometchat_1dp = 1dp
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp), // cometchat_margin_3 = 12dp, cometchat_margin_2 = 8dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag icon
            Image(
                painter = painterResource(id = flagDrawable),
                contentDescription = "$label flag",
                modifier = Modifier.size(20.dp, 15.dp)
            )
            
            // Region label - marginStart: 8dp (cometchat_padding_2)
            Text(
                text = label,
                style = typography.buttonMedium,
                color = colorScheme.textColorSecondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
