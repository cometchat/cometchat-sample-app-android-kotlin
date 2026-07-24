package com.cometchat.uikit.compose.presentation.messagecomposer.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for
 * [com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatAttachmentTile].
 *
 * A tile renders one staged attachment in the composer tray. There are three tile shapes:
 * a square **media** tile (image/video), an **audio** card (picker-selected `audio/…` files —
 * play glyph + name + duration), and a **document** card (everything else). Recorder voice notes
 * are never staged here; they are sent standalone.
 *
 * Status is communicated per the design system's mobile states:
 * - a **corner badge** straddles the top-end of every tile — a ✕ that cancels while uploading and
 *   removes otherwise;
 * - the tile's **leading visual** (media thumbnail / file-type icon / audio play button) dims
 *   under a dark overlay while uploading (white progress ring) and on failure (a red circle with
 *   a retry / error glyph);
 * - a failed / rejected **card** additionally swaps its border to the error color and replaces
 *   its meta line with "Tap to retry" / "Upload failed" in the error color.
 *
 * Use [default] to create an instance backed by CometChat theme tokens.
 *
 * @property mediaTileSize Side length of a square image/video tile.
 * @property mediaCornerRadius Corner radius of a media tile.
 * @property mediaStrokeColor Border color of a media tile.
 * @property mediaStrokeWidth Border width of a media tile.
 * @property cardCornerRadius Corner radius of a document / audio card tile.
 * @property cardWidth Fixed width of a document card tile — every card renders at this width
 *                     regardless of how short or long its file name is.
 * @property cardBackgroundColor Background of a document / audio card tile.
 * @property cardStrokeColor Border color of a document / audio card tile.
 * @property cardStrokeWidth Border width of a document / audio card tile.
 * @property placeholderColor Background of a media tile before its upload succeeds.
 * @property fileIconSize Side length of the leading icon **container** on a document card (the
 *                        rounded white box holding the file-type glyph).
 * @property fileIconBackgroundColor Background of the leading icon container.
 * @property fileIconCornerRadius Corner radius of the leading icon container.
 * @property fileIconStrokeColor Border color of the leading icon container.
 * @property fileIconStrokeWidth Border width of the leading icon container.
 * @property fileTypeIconSize Size of the file-type glyph inside the icon container.
 * @property audioCardWidth Fixed width of an audio card tile.
 * @property audioCardHeight Fixed height of an audio card tile (name + seek bar + time).
 * @property audioPlayButtonSize Diameter of the round play/pause button on an audio card.
 * @property audioPlayButtonColor Background of the audio play button.
 * @property audioPlayIconTint Tint of the play/pause glyph on the audio play button.
 * @property audioSeekTrackColor Track color of the audio seek bar.
 * @property audioSeekFillColor Fill color of the played portion of the seek bar.
 * @property audioSeekKnobColor Fill of the seek bar knob.
 * @property audioSeekKnobStrokeColor Border color of the seek bar knob.
 * @property audioTimeTextColor Color of the "00:00/00:32" time label on an audio card.
 * @property audioTimeTextStyle Text style of the audio time label.
 * @property fileNameTextColor Color of the file name label.
 * @property fileNameTextStyle Text style of the file name label.
 * @property fileMetaTextColor Color of the "EXT" / size meta label.
 * @property fileMetaTextStyle Text style of the meta label.
 * @property videoPlayBadgeColor Background of the play badge on a successfully-uploaded video tile.
 * @property videoPlayIconTint Tint of the video play glyph.
 * @property durationLabelColor Background pill of the duration chip on a video tile.
 * @property durationTextColor Text color of the duration chip.
 * @property durationTextStyle Text style of the duration chip.
 * @property loadingIndicatorColor Color of the progress ring shown while uploading (drawn over
 *                                 the dark state overlay, so white by default).
 * @property stateOverlayColor Dark scrim drawn over the leading slot (file icon / play button)
 *                             while uploading or failed — the visual stays visible, dimmed.
 * @property mediaDimColor Dark tint drawn over a media thumbnail while uploading or failed.
 * @property errorBadgeColor Fill of the round error / retry badge shown on a failed tile.
 * @property errorBadgeIconTint Tint of the glyph inside the error / retry badge.
 * @property cardErrorStrokeColor Border color of a failed / rejected card (and media tile).
 * @property errorTextColor Color of the "Upload failed" / "Tap to retry" meta line.
 * @property cornerBadgeSize Diameter of the top-end corner badge (cross / spinner).
 * @property cornerBadgeOverhang How far the corner badge sticks out beyond the tile body's
 *                               top-end corner.
 * @property cornerBadgeBackgroundColor Background of the top-end corner badge.
 * @property cornerBadgeBorderColor Ring color around the corner badge (helps it read on a thumbnail).
 * @property cornerBadgeBorderWidth Ring width around the corner badge.
 * @property cornerBadgeIconTint Tint of the corner badge glyph (✕ / spinner).
 */
@Immutable
data class CometChatAttachmentTileStyle(
    val mediaTileSize: Dp,
    val mediaCornerRadius: Dp,
    val mediaStrokeColor: Color,
    val mediaStrokeWidth: Dp,
    val cardCornerRadius: Dp,
    val cardWidth: Dp,
    val cardBackgroundColor: Color,
    val cardStrokeColor: Color,
    val cardStrokeWidth: Dp,
    val placeholderColor: Color,
    val fileIconSize: Dp,
    val fileIconBackgroundColor: Color,
    val fileIconCornerRadius: Dp,
    val fileIconStrokeColor: Color,
    val fileIconStrokeWidth: Dp,
    val fileTypeIconSize: Dp,
    val audioCardWidth: Dp,
    val audioCardHeight: Dp,
    val audioPlayButtonSize: Dp,
    val audioPlayButtonColor: Color,
    val audioPlayIconTint: Color,
    val audioSeekTrackColor: Color,
    val audioSeekFillColor: Color,
    val audioSeekKnobColor: Color,
    val audioSeekKnobStrokeColor: Color,
    val audioTimeTextColor: Color,
    val audioTimeTextStyle: TextStyle,
    val fileNameTextColor: Color,
    val fileNameTextStyle: TextStyle,
    val fileMetaTextColor: Color,
    val fileMetaTextStyle: TextStyle,
    val videoPlayBadgeColor: Color,
    val videoPlayIconTint: Color,
    val durationLabelColor: Color,
    val durationTextColor: Color,
    val durationTextStyle: TextStyle,
    val loadingIndicatorColor: Color,
    val stateOverlayColor: Color,
    val mediaDimColor: Color,
    val errorBadgeColor: Color,
    val errorBadgeIconTint: Color,
    val cardErrorStrokeColor: Color,
    val errorTextColor: Color,
    val cornerBadgeSize: Dp,
    val cornerBadgeOverhang: Dp,
    val cornerBadgeBackgroundColor: Color,
    val cornerBadgeBorderColor: Color,
    val cornerBadgeBorderWidth: Dp,
    val cornerBadgeIconTint: Color
) {
    companion object {

        /** Creates a default tile style using CometChat theme tokens. */
        @Composable
        fun default(
            mediaTileSize: Dp = 60.dp,
            mediaCornerRadius: Dp = 10.dp,
            mediaStrokeColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            mediaStrokeWidth: Dp = 1.dp,
            cardCornerRadius: Dp = 10.dp,
            cardWidth: Dp = 200.dp,
            cardBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            cardStrokeColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            cardStrokeWidth: Dp = 1.dp,
            placeholderColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            fileIconSize: Dp = 40.dp,
            fileIconBackgroundColor: Color = CometChatTheme.colorScheme.colorWhite,
            fileIconCornerRadius: Dp = 12.dp,
            fileIconStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            fileIconStrokeWidth: Dp = 1.dp,
            fileTypeIconSize: Dp = 26.dp,
            audioCardWidth: Dp = 200.dp,
            audioCardHeight: Dp = 60.dp,
            audioPlayButtonSize: Dp = 40.dp,
            audioPlayButtonColor: Color = CometChatTheme.colorScheme.primary,
            audioPlayIconTint: Color = CometChatTheme.colorScheme.iconTintWhite,
            audioSeekTrackColor: Color = CometChatTheme.colorScheme.neutralColor300,
            audioSeekFillColor: Color = CometChatTheme.colorScheme.primary,
            audioSeekKnobColor: Color = CometChatTheme.colorScheme.colorWhite,
            audioSeekKnobStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            audioTimeTextColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            audioTimeTextStyle: TextStyle = CometChatTheme.typography.caption1Regular.copy(fontSize = 11.sp),
            fileNameTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            fileNameTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            fileMetaTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            fileMetaTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            videoPlayBadgeColor: Color = Color.Black.copy(alpha = 0.5f),
            videoPlayIconTint: Color = CometChatTheme.colorScheme.iconTintWhite,
            durationLabelColor: Color = Color.Black.copy(alpha = 0.6f),
            durationTextColor: Color = CometChatTheme.colorScheme.textColorWhite,
            // The 60dp composer tile is ~60% the size of the reference card, so the chip drops
            // below the 10sp caption2 token to keep the reference's proportions.
            durationTextStyle: TextStyle = CometChatTheme.typography.caption2Medium.copy(fontSize = 8.sp),
            loadingIndicatorColor: Color = CometChatTheme.colorScheme.colorWhite,
            stateOverlayColor: Color = Color.Black.copy(alpha = 0.62f),
            mediaDimColor: Color = Color.Black.copy(alpha = 0.35f),
            errorBadgeColor: Color = CometChatTheme.colorScheme.errorColor,
            errorBadgeIconTint: Color = CometChatTheme.colorScheme.colorWhite,
            cardErrorStrokeColor: Color = CometChatTheme.colorScheme.errorColor,
            errorTextColor: Color = CometChatTheme.colorScheme.errorColor,
            cornerBadgeSize: Dp = 22.dp,
            cornerBadgeOverhang: Dp = 6.dp,
            cornerBadgeBackgroundColor: Color = CometChatTheme.colorScheme.neutralColor600,
            cornerBadgeBorderColor: Color = CometChatTheme.colorScheme.colorWhite,
            cornerBadgeBorderWidth: Dp = 2.dp,
            cornerBadgeIconTint: Color = CometChatTheme.colorScheme.iconTintWhite
        ): CometChatAttachmentTileStyle = CometChatAttachmentTileStyle(
            mediaTileSize = mediaTileSize,
            mediaCornerRadius = mediaCornerRadius,
            mediaStrokeColor = mediaStrokeColor,
            mediaStrokeWidth = mediaStrokeWidth,
            cardCornerRadius = cardCornerRadius,
            cardWidth = cardWidth,
            cardBackgroundColor = cardBackgroundColor,
            cardStrokeColor = cardStrokeColor,
            cardStrokeWidth = cardStrokeWidth,
            placeholderColor = placeholderColor,
            fileIconSize = fileIconSize,
            fileIconBackgroundColor = fileIconBackgroundColor,
            fileIconCornerRadius = fileIconCornerRadius,
            fileIconStrokeColor = fileIconStrokeColor,
            fileIconStrokeWidth = fileIconStrokeWidth,
            fileTypeIconSize = fileTypeIconSize,
            audioCardWidth = audioCardWidth,
            audioCardHeight = audioCardHeight,
            audioPlayButtonSize = audioPlayButtonSize,
            audioPlayButtonColor = audioPlayButtonColor,
            audioPlayIconTint = audioPlayIconTint,
            audioSeekTrackColor = audioSeekTrackColor,
            audioSeekFillColor = audioSeekFillColor,
            audioSeekKnobColor = audioSeekKnobColor,
            audioSeekKnobStrokeColor = audioSeekKnobStrokeColor,
            audioTimeTextColor = audioTimeTextColor,
            audioTimeTextStyle = audioTimeTextStyle,
            fileNameTextColor = fileNameTextColor,
            fileNameTextStyle = fileNameTextStyle,
            fileMetaTextColor = fileMetaTextColor,
            fileMetaTextStyle = fileMetaTextStyle,
            videoPlayBadgeColor = videoPlayBadgeColor,
            videoPlayIconTint = videoPlayIconTint,
            durationLabelColor = durationLabelColor,
            durationTextColor = durationTextColor,
            durationTextStyle = durationTextStyle,
            loadingIndicatorColor = loadingIndicatorColor,
            stateOverlayColor = stateOverlayColor,
            mediaDimColor = mediaDimColor,
            errorBadgeColor = errorBadgeColor,
            errorBadgeIconTint = errorBadgeIconTint,
            cardErrorStrokeColor = cardErrorStrokeColor,
            errorTextColor = errorTextColor,
            cornerBadgeSize = cornerBadgeSize,
            cornerBadgeOverhang = cornerBadgeOverhang,
            cornerBadgeBackgroundColor = cornerBadgeBackgroundColor,
            cornerBadgeBorderColor = cornerBadgeBorderColor,
            cornerBadgeBorderWidth = cornerBadgeBorderWidth,
            cornerBadgeIconTint = cornerBadgeIconTint
        )
    }
}
