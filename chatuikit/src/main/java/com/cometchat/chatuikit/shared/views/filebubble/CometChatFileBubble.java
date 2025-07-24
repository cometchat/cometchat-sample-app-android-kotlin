package com.cometchat.chatuikit.shared.views.filebubble;

import static com.cometchat.chatuikit.shared.resources.utils.Utils.getMimeTypeFromFile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;

import com.cometchat.chat.models.Attachment;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.permission.CometChatPermissionHandler;
import com.cometchat.chatuikit.shared.permission.builder.PermissionHandlerBuilder;
import com.cometchat.chatuikit.shared.permission.listener.PermissionResultListener;
import com.cometchat.chatuikit.shared.resources.utils.FontUtils;
import com.cometchat.chatuikit.shared.resources.utils.MediaUtils;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * CometChatFileBubble is a custom view that extends MaterialCardView. This view
 * represents a file bubble in a chat application, providing a file icon, title,
 * and subtitle. It also handles click events for downloading the file.
 */
public class CometChatFileBubble extends MaterialCardView {
    private static final String TAG = CometChatFileBubble.class.getSimpleName();
    private LinearLayout layout;
    private ImageView downloadIcon;
    private TextView title, subtitle;
    private ImageView imageViewFileIcon;

    // Data fields
    private String titleText;
    private String subTitleText;
    private String fileUrl;
    private OnClick onClick;
    private @StyleRes int style;
    private MediaMessage message;
    private PermissionResultListener permissionResultListener;
    private PermissionHandlerBuilder permissionHandlerBuilder;

    /**
     * Constructor to create CometChatFileBubble with just the context.
     *
     * @param context The context of the view.
     */
    public CometChatFileBubble(Context context) {
        this(context, null);
    }

    /**
     * Constructor to create CometChatFileBubble with context and attributes.
     *
     * @param context The context of the view.
     * @param attrs   The attribute set to customize the view.
     */
    public CometChatFileBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatFileBubbleStyle);
    }

    /**
     * Constructor to create CometChatFileBubble with context, attributes, and a
     * default style.
     *
     * @param context      The context of the view.
     * @param attrs        The attribute set to customize the view.
     * @param defStyleAttr The default style attribute for the view.
     */
    public CometChatFileBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Inflate the view layout and initialize UI components.
     *
     * @param attrs        The attributes for customizing the view.
     * @param defStyleAttr The default style attribute.
     */
    private void inflateAndInitializeView(AttributeSet attrs, @StyleRes int defStyleAttr) {
        // Initialize MaterialCard and inflate the custom view layout
        Utils.initMaterialCard(this);
        // UI elements
        View view = View.inflate(getContext(), R.layout.cometchat_file_bubble, null);

        // Bind UI elements to their corresponding IDs
        layout = view.findViewById(R.id.parent);
        title = view.findViewById(R.id.tv_toolbar_title);
        subtitle = view.findViewById(R.id.tv_subtitle);
        downloadIcon = view.findViewById(R.id.iv_download);
        imageViewFileIcon = view.findViewById(R.id.cometchat_file_icon);
        permissionHandlerBuilder = CometChatPermissionHandler.withContext(getContext());
        permissionResultListener = (grantedPermission, deniedPermission) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || !grantedPermission.isEmpty()) {
                String defaultTitle = (titleText != null && !titleText.isEmpty()) ? titleText : String.valueOf(System.currentTimeMillis());
                Utils.downloadFile(getContext(), fileUrl, defaultTitle);
            }
        };
        // Add the view to the layout
        addView(view);

        // Set default visibility
        downloadIcon.setVisibility(GONE);
        title.setVisibility(GONE);
        subtitle.setVisibility(GONE);

        // Set the download icon click event
        downloadIcon.setOnClickListener(clickView -> handleDownloadClick());
        view.setOnLongClickListener(v -> {
            Utils.performAdapterClick(v);
            return true;
        });

        view.setOnClickListener(v -> openFile());

        // Apply custom attributes and styles
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Handle the click event for the download icon.
     */
    private void handleDownloadClick() {
        if (onClick == null) {
            permissionHandlerBuilder
                .withPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .withListener(permissionResultListener)
                .check();
        } else {
            // Trigger the custom click listener
            onClick.onClick();
        }
    }

    private void openFile() {
        if (message != null) {
            File file = Utils.getFileFromLocalPath(message);
            if (file != null && file.exists()) {
                MediaUtils.openFile(getContext(), file);
                return;
            }
            if (message.getAttachment() != null)
                MediaUtils.openMediaInPlayer(getContext(), message.getAttachment().getFileUrl(), message.getAttachment().getFileMimeType());
        }
    }

    /**
     * Apply custom style attributes to the view.
     *
     * @param attrs        The attribute set to apply.
     * @param defStyleAttr The default style attribute.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        // Obtain styled attributes
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatFileBubble, defStyleAttr, 0);
        int styleResId = typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleStyle, 0);
        // Apply default style if defined
        TypedArray finalTypedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatFileBubble, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(finalTypedArray);
    }

    /**
     * Extract attributes from the typed array and apply them to the view.
     *
     * @param typedArray The array of attributes to extract and apply.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            // Apply text styles and colors for title and subtitle
            setTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleTitleTextAppearance, 0));
            setTitleTextColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleTitleColor,
                                                  CometChatTheme.getNeutralColor900(getContext())));
            setSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatFileBubble_cometchatFileBubbleSubTitleTextAppearance, 0));
            setSubtitleTextColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleSubTitleColor,
                                                     CometChatTheme.getNeutralColor600(getContext())));

            // Apply tint color for the download icon
            setDownloadIconTintColor(typedArray.getColor(R.styleable.CometChatFileBubble_cometchatFileBubbleFileDownloadIconTint, 0));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Set the text appearance for the title.
     *
     * @param appearance The style resource ID to apply.
     */
    public void setTitleTextAppearance(@StyleRes int appearance) {
        if (appearance != 0) {
            title.setTextAppearance(appearance);
        }
    }

    /**
     * Set the title text color.
     *
     * @param color The color for the title text.
     */
    public void setTitleTextColor(@ColorInt int color) {
        if (color != 0) {
            title.setTextColor(color);
        }
    }

    /**
     * Set the text appearance for the subtitle.
     *
     * @param appearance The style resource ID to apply.
     */
    public void setSubtitleTextAppearance(@StyleRes int appearance) {
        if (appearance != 0) {
            subtitle.setTextAppearance(appearance);
        }
    }

    /**
     * Set the subtitle text color.
     *
     * @param color The color for the subtitle text.
     */
    public void setSubtitleTextColor(@ColorInt int color) {
        if (color != 0) {
            subtitle.setTextColor(color);
        }
    }

    /**
     * Set the tint color for the download icon.
     *
     * @param color The color for the download icon tint.
     */
    public void setDownloadIconTintColor(@ColorInt int color) {
        if (color != 0) {
            downloadIcon.setImageTintList(ColorStateList.valueOf(color));
        }
    }

    /**
     * Set the font for the title text.
     *
     * @param font The font name for the title.
     */
    public void setTitleTextFont(String font) {
        if (font != null && !font.isEmpty()) {
            title.setTypeface(FontUtils.getInstance().getTypeFace(font, getContext()));
        }
    }

    /**
     * Set the font for the subtitle text.
     *
     * @param font The font name for the subtitle.
     */
    public void setSubtitleTextFont(String font) {
        if (font != null && !font.isEmpty()) {
            subtitle.setTypeface(FontUtils.getInstance().getTypeFace(font, getContext()));
        }
    }

    /**
     * Set the download icon image resource.
     *
     * @param image The resource ID of the download icon image.
     */
    public void setDownloadIcon(@DrawableRes int image) {
        if (image != 0) {
            downloadIcon.setImageResource(image);
        }
    }

    /**
     * Sets the media message for the current view. This method takes a
     * {@link MediaMessage} and updates the UI to reflect the properties of the
     * message, including its attachment (if available). It determines the
     * appropriate file icon based on the MIME type or file extension of the
     * attachment or file, formats the subtitle text to display the sent time, file
     * size, and file extension, and sets the file URL accordingly. If the
     * attachment is not null, the method will check its MIME type and file
     * extension to determine which file icon to display. If the attachment's file
     * URL ends with certain extensions, it assigns the corresponding icon resource.
     * If the attachment is null, it retrieves the MIME type from the provided file
     * and sets the appropriate icon. The visibility of the download icon is also
     * adjusted based on whether the file exists at the specified path in the
     * metadata. The subtitle text is formatted to show the date and time when the
     * message was sent, the size of the file, and its extension. This information
     * is derived from the `MediaMessage` and its attachment.
     *
     * @param mediaMessage The {@link MediaMessage} to be set. This message contains the file
     *                     attachment and associated metadata. If the message does not
     *                     contain an attachment, the file information will be derived from
     *                     the file associated with the message.
     */
    public void setMessage(MediaMessage mediaMessage) {
        this.message = mediaMessage;
        Attachment attachment = mediaMessage.getAttachment();
        String dateFormatterPattern = "d MMM, yyyy";
        if (attachment != null) {
            int size = attachment.getFileSize();
            if (attachment.getFileExtension() != null) {
                if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.VIDEO)) {
                    setFileIcon(R.drawable.cometchat_video_file_icon);
                } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.OCTET_STREAM)) {
                    if (attachment.getFileUrl().endsWith(UIKitConstants.MimeType.DOC)) {
                        setFileIcon(R.drawable.cometchat_word_file_icon);
                    } else if (attachment.getFileUrl().endsWith(UIKitConstants.MimeType.PPT)) {
                        setFileIcon(R.drawable.cometchat_ppt_file_icon);
                    } else if (attachment.getFileUrl().endsWith(UIKitConstants.MimeType.XLS)) {
                        setFileIcon(R.drawable.cometchat_xlsx_file_icon);
                    }
                } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.PDF)) {
                    setFileIcon(R.drawable.cometchat_pdf_file_icon);
                } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.ZIP)) {
                    setFileIcon(R.drawable.cometchat_zip_file_icon);
                } else if (attachment.getFileUrl().contains(UIKitConstants.MimeType.CSV)) {
                    setFileIcon(R.drawable.cometchat_text_file_icon);
                } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.AUDIO)) {
                    setFileIcon(R.drawable.cometchat_audio_file_icon);
                } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.IMAGE)) {
                    setFileIcon(R.drawable.cometchat_image_file_icon);
                } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.TEXT)) {
                    setFileIcon(R.drawable.cometchat_text_file_icon);
                } else if (attachment.getFileMimeType().contains(UIKitConstants.MimeType.LINK)) {
                    setFileIcon(R.drawable.cometchat_link_file_icon);
                } else {
                    setFileIcon(R.drawable.cometchat_unknown_file_icon);
                }
            }
            @SuppressLint("SimpleDateFormat") String subTitleText = new SimpleDateFormat(dateFormatterPattern).format(mediaMessage.getSentAt() * 1000) + " • " + Utils.getFileSize(
                size) + " • " + attachment.getFileUrl().substring(attachment.getFileUrl().lastIndexOf(".") + 1);
            setFileUrl(attachment.getFileUrl(), attachment.getFileName(), subTitleText);
            if (mediaMessage.getMetadata() != null && mediaMessage.getMetadata().has(UIKitConstants.IntentStrings.PATH)) {
                try {
                    File file = new File(mediaMessage.getMetadata().getString(UIKitConstants.IntentStrings.PATH));
                    if (file.exists()) {
                        downloadIcon.setVisibility(GONE);
                    } else {
                        downloadIcon.setVisibility(VISIBLE);
                    }
                } catch (Exception e) {
                    downloadIcon.setVisibility(VISIBLE);
                }
            }
        } else {
            @SuppressLint("SimpleDateFormat") String subTitleText = new SimpleDateFormat(dateFormatterPattern).format(mediaMessage.getSentAt() * 1000) + " • " + Utils.getFileSize(
                (int) mediaMessage.getFile().length()) + " • " + mediaMessage
                .getFile()
                .getPath()
                .substring(mediaMessage.getFile().getPath().lastIndexOf(".") + 1);
            switch (getMimeTypeFromFile(getContext(), mediaMessage.getFile())) {
                case UIKitConstants.MimeType.MIME_CSV:
                case UIKitConstants.MimeType.MIME_RTF:
                    setFileIcon(R.drawable.cometchat_text_file_icon);
                    break;
                case UIKitConstants.MimeType.MIME_DOC:
                    setFileIcon(R.drawable.cometchat_word_file_icon);
                    break;
                case UIKitConstants.MimeType.MIME_XLS:
                    setFileIcon(R.drawable.cometchat_xlsx_file_icon);
                    break;
                case UIKitConstants.MimeType.MIME_PPT:
                    setFileIcon(R.drawable.cometchat_ppt_file_icon);
                    break;
                case UIKitConstants.MimeType.MIME_PDF:
                    setFileIcon(R.drawable.cometchat_pdf_file_icon);
                    break;
                case UIKitConstants.MimeType.MIME_ZIP:
                case UIKitConstants.MimeType.MIME_ODP:
                case UIKitConstants.MimeType.MIME_ODS:
                case UIKitConstants.MimeType.MIME_ODT:
                    setFileIcon(R.drawable.cometchat_zip_file_icon);
                    break;
                case UIKitConstants.MimeType.MIME_MP4_VIDEO:
                    setFileIcon(R.drawable.cometchat_video_file_icon);
                    break;
                case UIKitConstants.MimeType.MIME_MPEG_AUDIO:
                case UIKitConstants.MimeType.MIME_MP3_AUDIO:
                    setFileIcon(R.drawable.cometchat_audio_file_icon);
                    break;
                case UIKitConstants.MimeType.MIME_JPEG_IMAGE:
                case UIKitConstants.MimeType.MIME_PNG_IMAGE:
                    setFileIcon(R.drawable.cometchat_image_file_icon);
                    break;
                case UIKitConstants.MimeType.LINK:
                    setFileIcon(R.drawable.cometchat_link_file_icon);
                    break;
                default:
                    setFileIcon(R.drawable.cometchat_unknown_file_icon);
                    break;
            }
            downloadIcon.setVisibility(GONE);
            setFileUrl("", mediaMessage.getFile().getName(), subTitleText);
        }
    }

    /**
     * Set the file URL, title, and subtitle for the file bubble.
     *
     * @param fileUrl      The file URL.
     * @param titleText    The title text.
     * @param subtitleText The subtitle text.
     */
    public void setFileUrl(String fileUrl, String titleText, String subtitleText) {
        if (fileUrl != null && !fileUrl.isEmpty()) {
            this.fileUrl = fileUrl;
            downloadIcon.setVisibility(VISIBLE);
        }
        setTitleText(titleText);
        setSubtitleText(subtitleText);
    }

    /**
     * Set the subtitle text for the file bubble.
     *
     * @param text The subtitle text to display.
     */
    public void setSubtitleText(String text) {
        if (text != null && !text.isEmpty()) {
            subTitleText = text;
            subtitle.setVisibility(VISIBLE);
            subtitle.setText(text);
        }
    }

    // Getters for testing or direct access if needed
    public LinearLayout getView() {
        return layout;
    }

    public TextView getTitle() {
        return title;
    }

    public TextView getSubtitle() {
        return subtitle;
    }

    public ImageView getDownloadImageView() {
        return downloadIcon;
    }

    public ImageView getFileIcon() {
        return imageViewFileIcon;
    }

    /**
     * Set the file icon for the file bubble.
     *
     * @param image The resource ID of the image to set.
     */
    public void setFileIcon(@DrawableRes int image) {
        if (image != 0) {
            imageViewFileIcon.setImageResource(image);
        }
    }

    public String getTitleText() {
        return titleText;
    }

    /**
     * Set the title text for the file bubble.
     *
     * @param text The title text to display.
     */
    public void setTitleText(String text) {
        if (text != null && !text.isEmpty()) {
            title.setVisibility(VISIBLE);
            titleText = text;
            title.setText(text);
        }
    }

    public String getSubTitleText() {
        return subTitleText;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public OnClick getOnClick() {
        return onClick;
    }

    /**
     * Set a custom click listener for the file bubble.
     *
     * @param onClick The custom OnClick listener.
     */
    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    public @StyleRes int getStyle() {
        return style;
    }

    /**
     * Set the style for the file bubble.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        this.style = style;
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatFileBubble);
        extractAttributesAndApplyDefaults(typedArray);
    }
}
