package com.cometchat.chatuikit.compactmessagecomposer;

import static com.cometchat.chatuikit.shared.resources.utils.AnimationUtils.animateVisibilityGone;
import static com.cometchat.chatuikit.shared.resources.utils.AnimationUtils.animateVisibilityVisible;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spannable;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StyleRes;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatSingleLineComposerBinding;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.constants.UIKitUtilityConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatMentionsFormatter;
import com.cometchat.chatuikit.shared.formatters.CometChatRichTextFormatter;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.formatters.style.PromptTextStyle;
import com.cometchat.chatuikit.shared.resources.utils.MediaUtils;
import com.cometchat.chatuikit.shared.resources.utils.itemclicklistener.OnItemClickListener;
import com.cometchat.chatuikit.shared.models.CometChatMessageComposerAction;
import com.cometchat.chatuikit.shared.permission.CometChatPermissionHandler;
import com.cometchat.chatuikit.shared.permission.builder.ActivityResultHandlerBuilder;
import com.cometchat.chatuikit.shared.permission.builder.PermissionHandlerBuilder;
import com.cometchat.chatuikit.shared.permission.listener.PermissionResultListener;
import com.cometchat.chatuikit.shared.resources.soundmanager.CometChatSoundManager;
import com.cometchat.chatuikit.shared.resources.soundmanager.Sound;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.spans.LinkFormatSpan;
import com.cometchat.chatuikit.shared.spans.NonEditableSpan;
import com.cometchat.chatuikit.shared.views.mediarecorder.CometChatMediaRecorder;
import com.cometchat.chatuikit.shared.views.messageinput.CometChatTextWatcher;
import com.cometchat.chatuikit.shared.views.optionsheet.OptionSheetMenuItem;
import com.cometchat.chatuikit.shared.views.optionsheet.attachmentoptionsheet.CometChatAttachmentOptionSheet;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKitHelper;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.interfaces.Function1;
import com.cometchat.chatuikit.shared.interfaces.OnError;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;
import com.cometchat.chatuikit.shared.spans.FormatSpanWatcher;
import com.cometchat.chatuikit.shared.spans.ListContinuationHandler;
import com.cometchat.chatuikit.shared.spans.MarkdownConverter;
import com.cometchat.chatuikit.shared.spans.NumberedListFormatSpan;
import com.cometchat.chatuikit.shared.spans.BulletListFormatSpan;
import com.cometchat.chatuikit.shared.spans.BlockquoteFormatSpan;
import com.cometchat.chatuikit.shared.spans.CodeBlockFormatSpan;
import com.cometchat.chatuikit.shared.spans.ConsumedMentionSpan;
import com.cometchat.chatuikit.shared.spans.InlineCodeFormatSpan;
import com.cometchat.chatuikit.shared.spans.RichTextFormatSpan;
import com.cometchat.chatuikit.shared.spans.RichTextSpanManager;
import com.cometchat.chatuikit.shared.views.suggestionlist.CometChatSuggestionList;
import com.cometchat.chatuikit.shared.views.suggestionlist.SuggestionItem;
import com.cometchat.chatuikit.shared.views.suggestionlist.SuggestionListViewHolderListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kotlin.jvm.functions.Function4;

/**
 * CometChatSingleLineComposer is a compact message composer component with
 * built-in rich text formatting support.
 * <p>
 * This component extends MaterialCardView and provides:
 * <ul>
 *     <li>Single-line input with expandable multi-line support</li>
 *     <li>Rich text formatting toolbar</li>
 *     <li>Attachment button</li>
 *     <li>Voice recording button</li>
 *     <li>Send button with active/inactive states</li>
 *     <li>Edit/Reply mode support</li>
 *     <li>Mentions support</li>
 * </ul>
 * </p>
 */
public class CometChatCompactMessageComposer extends MaterialCardView {
    private static final String TAG = CometChatCompactMessageComposer.class.getSimpleName();

    // Binding
    private CometchatSingleLineComposerBinding binding;

    // ViewModel
    private CompactMessageComposerViewModel viewModel;
    private LifecycleOwner lifecycleOwner;

    // Message Context
    private User user;
    private Group group;
    private BaseMessage quoteMessage;

    // Rich Text Formatting Configuration
    private boolean enableRichTextFormatting = true;  // Master switch for all rich text formatting
    private int richTextFormattingOptionsVisibility = View.VISIBLE;  // Controls toolbar visibility
    private boolean isToolbarVisible = false;  // Current toolbar visibility state
    private boolean showTextSelectionMenuItems = true;  // Show formatting options in text selection menu

    // Sticker Keyboard state
    private boolean isStickerKeyboardVisible = false;

    // Input Configuration
    private EnterKeyBehavior enterKeyBehavior = EnterKeyBehavior.NEW_LINE;

    // Feature Toggles
    private int attachmentButtonVisibility = View.VISIBLE;
    private int voiceRecordingButtonVisibility = View.VISIBLE;
    private int stickerButtonVisibility = View.VISIBLE;  // Sticker button visible by default
    private int aiButtonVisibility = View.GONE;
    private int sendButtonVisibility = View.VISIBLE;

    // Mentions Configuration
    private boolean disableMentions = false;
    private boolean disableMentionAll = false;

    // Typing Indicators
    private boolean disableTypingEvents = false;

    // Sound Configuration
    private boolean disableSoundForMessages = false;
    private @RawRes int customSoundForMessages;

    // Callbacks
    private SendButtonClickListener onSendClick;
    private OnTextChangeListener onTextChange;
    private OnAttachmentClickListener onAttachmentClick;
    private OnErrorListener onError;
    private OnVoiceRecordingListener onVoiceRecording;
    private OnEditCancelListener onEditCancel;
    private OnToolbarVisibilityChangeListener onToolbarVisibilityChange;

    // Custom Views
    private View headerView;
    private View footerView;
    private View sendButtonView;

    // Style Properties - Container
    private @ColorInt int backgroundColor;
    private @ColorInt int strokeColor;
    private float strokeWidth;
    private float cornerRadius;
    private Drawable backgroundDrawable;

    // Style Properties - Compose Box
    private @ColorInt int composeBoxBackgroundColor;
    private @ColorInt int composeBoxStrokeColor;
    private float composeBoxStrokeWidth;
    private float composeBoxCornerRadius;
    private Drawable composeBoxBackgroundDrawable;

    // Style Properties - Input Field
    private @ColorInt int inputTextColor;
    private @ColorInt int inputHintColor;
    private @StyleRes int inputTextAppearance;
    private @ColorInt int inputBackgroundColor;

    // Style Properties - Send Button
    private Drawable activeSendButtonDrawable;
    private Drawable inactiveSendButtonDrawable;
    private @ColorInt int sendButtonBackgroundColor;

    // Style Properties - Attachment Button
    private Drawable attachmentIcon;
    private @ColorInt int attachmentIconTint;

    // Style Properties - Voice Recording Button
    private Drawable voiceRecordingIcon;
    private @ColorInt int voiceRecordingIconTint;

    // Style Properties - AI Button
    private Drawable aiIcon;
    private @ColorInt int aiIconTint;

    // Style Properties - Sticker Button
    private Drawable stickerIcon;
    private @ColorInt int stickerIconTint;

    // Style Properties - Mention Banner
    private @ColorInt int mentionBannerBackgroundColor;
    private @ColorInt int mentionBannerTextColor;
    private @StyleRes int mentionBannerTextAppearance;
    private Drawable mentionBannerIcon;
    private @ColorInt int mentionBannerIconTint;
    private Drawable mentionBannerCloseIcon;
    private @ColorInt int mentionBannerCloseIconTint;

    // Style Properties - Component Styles
    private @StyleRes int richTextToolbarStyle;
    private @StyleRes int suggestionListStyle;
    private @StyleRes int messagePreviewStyle;

    // Sound Manager
    private CometChatSoundManager soundManager;

    // Addition Parameter for attachment options
    private AdditionParameter additionParameter;

    // Lifecycle state tracking
    private boolean isDetachedFromWindow = false;

    // Text Formatters for mentions
    private List<CometChatTextFormatter> cometchatTextFormatters;
    private HashMap<Character, CometChatTextFormatter> cometchatTextFormatterHashMap;
    private CometChatTextFormatter tempTextFormatter;
    private CometChatMentionsFormatter cometchatMentionsFormatter;
    private int lastTextFormatterOpenedIndex = -1;
    private HashMap<Character, HashMap<String, SuggestionItem>> selectedSuggestionItemHashMap;
    private OnItemClickListener<SuggestionItem> onItemClickListener;
    private Timer queryTimer;
    private Timer operationTimer;
    /**
     * Flag to suppress mention detection during programmatic text insertion
     * (e.g., when inserting a mention as plain text in inline code mode).
     * Prevents the afterTextChanged scanning loop from re-triggering the
     * suggestion list when it encounters the '@' character in the inserted text.
     */
    private boolean suppressMentionDetection;

    // WYSIWYG Rich Text Formatting
    private FormatSpanWatcher formatSpanWatcher;
    private ListContinuationHandler listContinuationHandler;

    // Attachment Sheet and Voice Recording
    private BottomSheetDialog bottomSheetDialog;
    private PermissionHandlerBuilder permissionHandlerBuilder;
    private PermissionResultListener permissionResultListener;
    private final List<OptionSheetMenuItem> attachmentOptionSheetMenuItems = new ArrayList<>();
    private final HashMap<String, CometChatMessageComposerAction> actionHashMap = new HashMap<>();
    private List<CometChatMessageComposerAction> messageComposerActions;
    private @StyleRes int attachmentOptionSheetStyle;
    private @StyleRes int mediaRecorderStyle;
    private @StyleRes int aiOptionSheetStyle;
    private Function4<Context, User, Group, HashMap<String, String>, List<CometChatMessageComposerAction>> aiOptions;
    private final List<OptionSheetMenuItem> aiOptionSheetMenuItems = new ArrayList<>();
    private String mentionAllLabelId;
    private String mentionAllLabel;
    private String[] microPhonePermissions;
    private String[] storagePermissions;
    private String[] cameraPermissions;
    private String RESULT_TO_BE_OPEN = "";
    private ActivityResultHandlerBuilder activityResultHandlerBuilder;

    // Inline Audio Recorder Configuration
    private @StyleRes int inlineAudioRecorderStyle;
    private Drawable recorderDeleteButtonIcon;
    private Drawable recorderSendButtonIcon;

    // Attachment Option Visibility (matching CometChatMessageComposer)
    private int imageAttachmentOptionVisibility = View.VISIBLE;
    private int cameraAttachmentOptionVisibility = View.VISIBLE;
    private int videoAttachmentOptionVisibility = View.VISIBLE;
    private int audioAttachmentOptionVisibility = View.VISIBLE;
    private int fileAttachmentOptionVisibility = View.VISIBLE;
    private int pollAttachmentOptionVisibility = View.VISIBLE;
    private int collaborativeDocumentOptionVisibility = View.VISIBLE;
    private int collaborativeWhiteboardOptionVisibility = View.VISIBLE;

    // Flag to suppress info visibility updates during edit mode initialization
    private boolean suppressInfoVisibility = false;

    // AI Agent Chat state
    private boolean isAgentChat = false;
    private boolean isAIAssistantGenerating = false;

    /**
     * Constructs a new CometChatSingleLineComposer with the given context.
     *
     * @param context The context of the view.
     */
    public CometChatCompactMessageComposer(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatSingleLineComposer with the given context and attribute set.
     *
     * @param context The context of the view.
     * @param attrs   The attribute set for the view.
     */
    public CometChatCompactMessageComposer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatSingleLineComposerStyle);
    }

    /**
     * Constructs a new CometChatSingleLineComposer with the given context, attribute set,
     * and default style attribute.
     *
     * @param context      The context of the view.
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style attribute.
     */
    public CometChatCompactMessageComposer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates and initializes the view.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        binding = CometchatSingleLineComposerBinding.inflate(
                LayoutInflater.from(getContext()), this, true);

        initializeViewModel();
        initializeComponents();
        setupClickListeners();
        setupTextChangeListener();
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Initializes the ViewModel.
     */
    private void initializeViewModel() {
        if (getContext() instanceof ViewModelStoreOwner) {
            viewModel = new ViewModelProvider((ViewModelStoreOwner) getContext())
                    .get(CompactMessageComposerViewModel.class);
        }
        if (getContext() instanceof LifecycleOwner) {
            lifecycleOwner = (LifecycleOwner) getContext();
            observeViewModel();
        }
    }

    /**
     * Observes ViewModel LiveData.
     * Following the same pattern as CometChatMessageComposer.attachObservers()
     */
    private void observeViewModel() {
        if (viewModel == null || lifecycleOwner == null) return;

        // UI state observers
        viewModel.getSendButtonActive().observe(lifecycleOwner, this::updateSendButtonState);
        viewModel.getToolbarVisible().observe(lifecycleOwner, this::updateToolbarVisibility);
        viewModel.getActiveFormats().observe(lifecycleOwner, this::updateActiveFormats);
        viewModel.getEditingMessage().observe(lifecycleOwner, this::updateEditMode);
        
        // Message operation observers (following MessageComposerViewModel pattern)
        viewModel.sentMessage().observe(lifecycleOwner, this::onMessageSentSuccess);
        viewModel.getException().observe(lifecycleOwner, this::onMessageSendException);
        viewModel.processEdit().observe(lifecycleOwner, this::showEditMessagePreview);
        viewModel.successEdit().observe(lifecycleOwner, this::onMessageEditSuccess);
        viewModel.processQuote().observe(lifecycleOwner, this::showQuoteMessagePreview);
        viewModel.successQuote().observe(lifecycleOwner, this::onMessageQuoteSuccess);
        
        // Panel observers (for sticker keyboard and other extensions)
        viewModel.showBottomPanel().observe(lifecycleOwner, this::showInternalBottomPanel);
        viewModel.closeBottomPanel().observe(lifecycleOwner, this::closeInternalBottomPanel);
        viewModel.showTopPanel().observe(lifecycleOwner, this::showInternalTopPanel);
        viewModel.closeTopPanel().observe(lifecycleOwner, this::closeInternalTopPanel);
        viewModel.getComposeText().observe(lifecycleOwner, this::setInitialComposerText);
        viewModel.getIsAIAssistantGenerating().observe(lifecycleOwner, this::updateComposerState);
        
        // Add event listeners
        viewModel.addListeners();
    }
    
    /**
     * Called when a message is sent successfully.
     */
    private void onMessageSentSuccess(BaseMessage message) {
        // Play sound if enabled
        if (!disableSoundForMessages) {
            if (customSoundForMessages != 0) {
                soundManager.play(Sound.outgoingMessage, customSoundForMessages);
            } else {
                soundManager.play(Sound.outgoingMessage);
            }
        }
        
        // Notify callback
        if (onSendClick != null) {
            onSendClick.onSendClick(getContext(), message);
        }
    }
    
    /**
     * Called when a message send operation fails.
     */
    private void onMessageSendException(CometChatException e) {
        if (e != null && onError != null) {
            onError.onError(e);
        }
    }
    
    /**
     * Shows the edit message preview.
     */
    private void showEditMessagePreview(BaseMessage message) {
        if (message instanceof TextMessage) {
            setEditMessage((TextMessage) message);
        }
    }
    
    /**
     * Called when message edit is successful.
     */
    private void onMessageEditSuccess(BaseMessage message) {
        clearText();
        cancelEdit();
        
        // Notify callback
        if (onSendClick != null) {
            onSendClick.onSendClick(getContext(), message);
        }
    }
    
    /**
     * Shows the quote/reply message preview.
     * Following the same pattern as CometChatMessageComposer.showQuoteMessagePreview()
     */
    private void showQuoteMessagePreview(BaseMessage baseMessage) {
        this.quoteMessage = baseMessage;
        if (baseMessage != null) {
            binding.messagePreview.setMessage(getContext(), baseMessage, binding.messagePreview, cometchatTextFormatters, UIKitConstants.FormattingType.CONVERSATIONS, null);
            binding.messagePreview.getSubtitleView().setMaxLines(1);
            // Set up close listener
            binding.messagePreview.setOnCloseClickListener(() -> {
                quoteMessage = null;
                animateVisibilityGone(binding.messagePreview);
            });
            animateVisibilityVisible(binding.messagePreview);

            binding.inputField.post(() -> {
                binding.inputField.requestFocus();
                Utils.showKeyBoard(getContext(), binding.inputField);
            });
        }
    }
    
    /**
     * Called when message quote/reply is successful.
     */
    private void onMessageQuoteSuccess(BaseMessage message) {
        quoteMessage = null;
        animateVisibilityGone(binding.messagePreview);
    }
    
    /**
     * Shows the internal bottom panel (e.g., sticker keyboard).
     */
    private void showInternalBottomPanel(Function1<Context, View> viewFunction) {
        if (viewFunction != null) {
            View panelView = viewFunction.apply(getContext());
            if (panelView != null) {
                binding.stickerKeyboardContainer.removeAllViews();
                binding.stickerKeyboardContainer.addView(panelView);
                binding.stickerKeyboardContainer.setVisibility(View.VISIBLE);
                isStickerKeyboardVisible = true;
                // Update sticker button to show active state (filled icon with highlight tint)
                binding.btnSticker.setImageResource(R.drawable.cometchat_ic_filled_sticker);
                binding.btnSticker.setColorFilter(CometChatTheme.getIconTintHighlight(getContext()));
            }
        }
    }
    
    /**
     * Closes the internal bottom panel.
     */
    private void closeInternalBottomPanel(Void aVoid) {
        binding.stickerKeyboardContainer.removeAllViews();
        binding.stickerKeyboardContainer.setVisibility(View.GONE);
        isStickerKeyboardVisible = false;
        // Reset sticker button to inactive state (outline icon with secondary tint)
        binding.btnSticker.setImageResource(R.drawable.cometchat_ic_sticker);
        binding.btnSticker.setColorFilter(stickerIconTint);
    }
    
    /**
     * Shows the internal top panel.
     */
    private void showInternalTopPanel(Function1<Context, View> viewFunction) {
        if (viewFunction != null) {
            View panelView = viewFunction.apply(getContext());
            if (panelView != null) {
                binding.headerViewContainer.removeAllViews();
                binding.headerViewContainer.addView(panelView);
                binding.headerViewContainer.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * Closes the internal top panel.
     */
    private void closeInternalTopPanel(Void aVoid) {
        binding.headerViewContainer.removeAllViews();
        binding.headerViewContainer.setVisibility(View.GONE);
    }
    
    /**
     * Sets the initial composer text from external events.
     */
    private void setInitialComposerText(String text) {
        if (text != null) {
            binding.inputField.setText(text);
            binding.inputField.setSelection(text.length());
        }
    }

    /**
     * Initializes components.
     */
    private void initializeComponents() {
        soundManager = new CometChatSoundManager(getContext());
        bottomSheetDialog = new BottomSheetDialog(getContext());
        additionParameter = new AdditionParameter();
        
        // Initialize text formatter collections
        cometchatTextFormatterHashMap = new HashMap<>();
        selectedSuggestionItemHashMap = new HashMap<>();
        cometchatTextFormatters = new ArrayList<>();
        queryTimer = new Timer();
        operationTimer = new Timer();
        
        // Disable autocorrect, autocomplete, and suggestions for rich text formatting
        // This prevents the keyboard from modifying text and breaking formatting spans
        configureInputFieldForRichText();
        
        // Initialize microphone permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            microPhonePermissions = new String[]{Manifest.permission.RECORD_AUDIO};
        } else {
            microPhonePermissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        
        // Initialize storage permissions
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        
        // Initialize camera permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA};
        
        // Setup permission handling
        setupPermissionResultListener();
        setupPermissionHandlerBuilder();
        setupActivityResultHandler();
        
        // Setup suggestion list
        setupSuggestionListScrollListener();
        setupSuggestionListClickListener();
        binding.suggestionList.setMaxHeightLimit(getResources().getDimensionPixelSize(R.dimen.cometchat_250dp));
    }

    /**
     * Configures the input field to disable autocorrect, autocomplete, and suggestions.
     * <p>
     * This is essential for rich text formatting because:
     * <ul>
     *   <li>Autocorrect can modify text and break formatting spans</li>
     *   <li>Autocomplete suggestions can replace formatted text</li>
     *   <li>Spell check underlines interfere with visual formatting</li>
     *   <li>Auto-capitalization can modify text unexpectedly</li>
     * </ul>
     * </p>
     */
    private void configureInputFieldForRichText() {
        // Set input type to disable suggestions and auto-capitalization while keeping multiline
        // Removed TYPE_TEXT_FLAG_CAP_SENTENCES to prevent auto-capitalization
        int inputType = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        binding.inputField.setInputType(inputType);
        
        // Additional IME options to prevent autocorrect behavior
        binding.inputField.setImeOptions(
                binding.inputField.getImeOptions() | EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        );
    }

    /**
     * Sets up the permission result listener for handling microphone, camera, and storage permissions.
     */
    private void setupPermissionResultListener() {
        permissionResultListener = (grantedPermission, deniedPermission) -> {
            if (!deniedPermission.isEmpty()) {
                handleDeniedPermission(deniedPermission);
            } else {
                if (grantedPermission.contains(Manifest.permission.RECORD_AUDIO)) {
                    openMediaRecorderSheet();
                } else {
                    handleGrantedPermission();
                }
            }
        };
    }
    
    /**
     * Handles the case when permissions are denied.
     */
    private void handleDeniedPermission(List<String> deniedPermissions) {
        if (UIKitConstants.ComposerAction.CAMERA.equals(RESULT_TO_BE_OPEN)) {
            showPermissionDialog(deniedPermissions);
        } else if (
            UIKitConstants.ComposerAction.DOCUMENT.equals(RESULT_TO_BE_OPEN) ||
                UIKitConstants.ComposerAction.IMAGE.equals(RESULT_TO_BE_OPEN) ||
                UIKitConstants.ComposerAction.VIDEO.equals(RESULT_TO_BE_OPEN) ||
                UIKitConstants.ComposerAction.AUDIO.equals(RESULT_TO_BE_OPEN)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                openStorage();
            } else {
                showPermissionDialog(deniedPermissions);
            }
        } else {
            // Microphone permission denied
            if (onError != null) {
                onError.onError(new SecurityException("Permission denied"));
            }
        }
    }
    
    /**
     * Handles the case when permissions are granted.
     */
    private void handleGrantedPermission() {
        if (UIKitConstants.ComposerAction.CAMERA.equals(RESULT_TO_BE_OPEN)) {
            launchCamera();
        } else {
            openStorage();
        }
    }
    
    /**
     * Shows a permission dialog explaining why the permission is needed.
     */
    private void showPermissionDialog(List<String> deniedPermissions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.cometchat_permission_required);
        if (deniedPermissions.get(0).equals(Manifest.permission.RECORD_AUDIO))
            builder.setMessage(R.string.cometchat_microphone_permission_warning);
        else if (deniedPermissions.get(0).equals(Manifest.permission.CAMERA))
            builder.setMessage(R.string.cometchat_camera_permission_warning);
        else
            builder.setMessage(R.string.cometchat_storage_permission_warning);

        builder.setNegativeButton(R.string.cometchat_cancel_button, (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton(R.string.cometchat_settings_button, (dialog, which) -> Utils.openAppSettings(getContext()));

        builder.create().show();
    }

    /**
     * Sets up the permission handler builder with the current context and listener.
     */
    private void setupPermissionHandlerBuilder() {
        permissionHandlerBuilder = CometChatPermissionHandler.withContext(getContext()).withListener(permissionResultListener);
    }
    
    /**
     * Sets up the activity result handler for handling camera, file, and other media actions.
     */
    private void setupActivityResultHandler() {
        activityResultHandlerBuilder = CometChatPermissionHandler.withContext(getContext()).registerListener(result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                handleActivityResult(result);
            }
            RESULT_TO_BE_OPEN = "";
        });
    }
    
    /**
     * Handles the result from the activity based on the action performed.
     */
    private void handleActivityResult(ActivityResult result) {
        File file;
        String contentType = null;

        if (UIKitConstants.ComposerAction.CAMERA.equals(RESULT_TO_BE_OPEN)) {
            file = handleCameraResult();
            if (file != null && file.exists()) {
                contentType = CometChatConstants.MESSAGE_TYPE_IMAGE;
            } else {
                Toast.makeText(getContext(), R.string.cometchat_file_not_exist, Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (UIKitConstants.ComposerAction.DOCUMENT.equals(RESULT_TO_BE_OPEN)) {
            file = handleFileResult(result);
            contentType = CometChatConstants.MESSAGE_TYPE_FILE;
        } else {
            file = handleOtherMediaResult(result);
            if (result.getData() != null) {
                contentType = getContentType(result.getData().getData());
            }
        }
        
        if (file != null && file.exists()) {
            sendMediaMessage(file, contentType);
        }
    }
    
    /**
     * Handles the result when the action was to open the camera.
     */
    private File handleCameraResult() {
        if (Build.VERSION.SDK_INT >= 29) {
            return MediaUtils.getRealPath(getContext(),
                    MediaUtils.uri, false);
        } else {
            return new File(MediaUtils.pictureImagePath);
        }
    }
    
    /**
     * Handles the result when the action was to select a file.
     */
    private File handleFileResult(ActivityResult result) {
        if (result.getData() != null && result.getData().getData() != null) {
            return MediaUtils.getRealPath(getContext(),
                    result.getData().getData(), false);
        }
        return null;
    }
    
    /**
     * Handles the result for other types of media actions.
     */
    private File handleOtherMediaResult(ActivityResult result) {
        if (result.getData() != null && result.getData().getData() != null) {
            return MediaUtils.getRealPath(getContext(),
                    result.getData().getData(), false);
        }
        return null;
    }
    
    /**
     * Determines the content type of a given URI.
     */
    private String getContentType(Uri uri) {
        if (uri != null) {
            ContentResolver cr = getContext().getContentResolver();
            String mimeType = cr.getType(uri);
            if (mimeType != null) {
                if (mimeType.contains("image")) return CometChatConstants.MESSAGE_TYPE_IMAGE;
                else if (mimeType.contains("video")) return CometChatConstants.MESSAGE_TYPE_VIDEO;
                else if (mimeType.contains("audio")) return CometChatConstants.MESSAGE_TYPE_AUDIO;
                else if (Arrays.asList(UIKitConstants.IntentStrings.EXTRA_MIME_DOC).contains(mimeType))
                    return CometChatConstants.MESSAGE_TYPE_FILE;
                else return mimeType;
            }
        }
        return null;
    }
    
    /**
     * Opens the appropriate storage picker based on the action type.
     */
    private void openStorage() {
        if (UIKitConstants.ComposerAction.DOCUMENT.equals(RESULT_TO_BE_OPEN)) openDocument();
        else if (UIKitConstants.ComposerAction.IMAGE.equals(RESULT_TO_BE_OPEN)) openImage();
        else if (UIKitConstants.ComposerAction.VIDEO.equals(RESULT_TO_BE_OPEN)) openVideo();
        else if (UIKitConstants.ComposerAction.AUDIO.equals(RESULT_TO_BE_OPEN)) openAudio();
    }
    
    /**
     * Opens the image picker.
     */
    public void openImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        activityResultHandlerBuilder.withIntent(intent).launch();
    }
    
    /**
     * Opens the video picker.
     */
    public void openVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        activityResultHandlerBuilder.withIntent(intent).launch();
    }
    
    /**
     * Opens the document picker.
     */
    public void openDocument() {
        activityResultHandlerBuilder.withIntent(
                MediaUtils.getFileIntent()).launch();
    }
    
    /**
     * Opens the audio picker.
     */
    public void openAudio() {
        activityResultHandlerBuilder.withIntent(
                MediaUtils.openAudio(getContext())).launch();
    }
    
    /**
     * Launches the camera for capturing photos.
     */
    public void launchCamera() {
        activityResultHandlerBuilder.withIntent(
                MediaUtils.openCamera(getContext())).launch();
    }
    
    /**
     * Requests camera permission from the user.
     */
    public void requestCameraPermission() {
        permissionHandlerBuilder.withPermissions(cameraPermissions).check();
    }
    
    /**
     * Requests storage permission from the user.
     */
    public void requestStoragePermission() {
        permissionHandlerBuilder.withPermissions(storagePermissions).check();
    }

    /**
     * Sets up click listeners.
     */
    private void setupClickListeners() {
        // Attachment button
        binding.btnAttachment.setOnClickListener(v -> {
            if (onAttachmentClick != null) {
                onAttachmentClick.onAttachmentClick();
            } else {
                openAttachmentOptionSheet();
            }
        });
        // Remove pressed state visual feedback (icon vanishing on press)
        binding.btnAttachment.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        });

        // Voice recording button
        binding.btnVoiceRecording.setOnClickListener(v -> onVoiceRecordingButtonClick());
        // Remove pressed state visual feedback (icon vanishing on press)
        binding.btnVoiceRecording.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        });

        // Send button
        binding.btnSend.setOnClickListener(v -> handleSendClick());

        // Sticker button - shows sticker keyboard
        binding.btnSticker.setOnClickListener(v -> toggleStickerKeyboard());
        // Remove pressed state visual feedback (icon vanishing on press)
        binding.btnSticker.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return true;
        });

        // Mention banner close button
        binding.mentionBannerClose.setOnClickListener(v -> hideMentionLimitBanner());

        // Rich text toolbar format click listener
        binding.richTextToolbar.setOnFormatClickListener(this::applyFormat);

        // Input field focus change listener - hide sticker keyboard when input gets focus
        binding.inputField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && isStickerKeyboardVisible) {
                hideStickerKeyboard();
            }
        });
        
        // Setup Enter key behavior for sending messages or inserting new lines
        setupEnterKeyBehavior();
        
        // Setup link click detection for editing existing links
        setupLinkClickDetection();
        
        // Setup text selection menu for formatting options
        setupTextSelectionMenu();
        
        // Setup paste-link-on-selection: auto-embed URL when pasted over selected text
        setupPasteLinkDetection();
        
        // Setup rich text paste: parse markdown from clipboard and insert with formatting spans
        setupRichTextPaste();
    }

    /**
     * Sets up touch listener on the input field to detect clicks on LinkFormatSpan.
     * <p>
     * When a user clicks on an existing link in the input field, this listener
     * detects the click and shows the Edit Link dialog.
     * </p>
     * <p>
     * Validates: Requirements 5b.1
     * </p>
     */
    private void setupLinkClickDetection() {
        binding.inputField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Only process if rich text formatting is enabled
                if (!enableRichTextFormatting) {
                    return false;
                }
                
                Editable editable = binding.inputField.getText();
                if (editable == null) {
                    return false;
                }
                
                // Get the offset at the touch position
                int offset = getOffsetForPosition(event.getX(), event.getY());
                if (offset < 0 || offset >= editable.length()) {
                    return false;
                }
                
                // Check if there's a LinkFormatSpan at this position
                LinkFormatSpan[] linkSpans =
                        editable.getSpans(offset, offset + 1, 
                                LinkFormatSpan.class);
                
                if (linkSpans != null && linkSpans.length > 0) {
                    // Found a link span - get its boundaries and show edit dialog
                    LinkFormatSpan linkSpan = linkSpans[0];
                    int spanStart = editable.getSpanStart(linkSpan);
                    int spanEnd = editable.getSpanEnd(linkSpan);
                    
                    if (spanStart >= 0 && spanEnd > spanStart) {
                        String linkText = editable.subSequence(spanStart, spanEnd).toString();
                        String linkUrl = linkSpan.getUrl();
                        
                        // Show the edit link dialog
                        showEditLinkDialog(editable, spanStart, spanEnd, linkText, linkUrl);
                        return true;
                    }
                }
            }
            return false;
        });
    }

    /**
     * Gets the character offset at the given touch position in the input field.
     *
     * @param x The x coordinate of the touch.
     * @param y The y coordinate of the touch.
     * @return The character offset at the touch position, or -1 if invalid.
     */
    private int getOffsetForPosition(float x, float y) {
        if (binding.inputField.getLayout() == null) {
            return -1;
        }
        
        // Adjust for padding
        int paddingLeft = binding.inputField.getTotalPaddingLeft();
        int paddingTop = binding.inputField.getTotalPaddingTop();
        
        // Get the line at the y position
        int line = binding.inputField.getLayout().getLineForVertical((int) (y - paddingTop));
        
        // Get the offset at the x position on that line
        int offset = binding.inputField.getLayout().getOffsetForHorizontal(line, x - paddingLeft);

        return offset;
    }

    /**
     * Sets up the text selection menu with formatting options.
     * When showTextSelectionMenuItems is true and enableRichTextFormatting is true,
     * formatting options (Bold, Italic, Strikethrough, Code) will be added to the
     * system text selection context menu that appears when text is long-pressed and selected.
     */
    private void setupTextSelectionMenu() {
        if (!enableRichTextFormatting || !showTextSelectionMenuItems) {
            // Remove custom action mode callback if formatting is disabled
            binding.inputField.setCustomSelectionActionModeCallback(null);
            return;
        }

        binding.inputField.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            // Menu item IDs for formatting options
            private static final int MENU_ID_BOLD = 100;
            private static final int MENU_ID_ITALIC = 101;
            private static final int MENU_ID_STRIKETHROUGH = 102;
            private static final int MENU_ID_CODE = 103;

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Add formatting options to the menu
                menu.add(Menu.NONE, MENU_ID_BOLD, 1, R.string.cometchat_bold);
                menu.add(Menu.NONE, MENU_ID_ITALIC, 2, R.string.cometchat_italic);
                menu.add(Menu.NONE, MENU_ID_STRIKETHROUGH, 3, R.string.cometchat_strikethrough);
                menu.add(Menu.NONE, MENU_ID_CODE, 4, R.string.cometchat_inline_code);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == MENU_ID_BOLD) {
                    applyFormat(FormatType.BOLD);
                    return true;
                } else if (itemId == MENU_ID_ITALIC) {
                    applyFormat(FormatType.ITALIC);
                    return true;
                } else if (itemId == MENU_ID_STRIKETHROUGH) {
                    applyFormat(FormatType.STRIKETHROUGH);
                    return true;
                } else if (itemId == MENU_ID_CODE) {
                    applyFormat(FormatType.INLINE_CODE);
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // No cleanup needed
            }
        });
    }

    /**
     * Sets up paste-link detection on the input field.
     * <p>
     * When the user has text selected and pastes a URL from the clipboard,
     * the selected text is automatically converted into a markdown-style link
     * instead of being replaced by the URL.
     * </p>
     */
    private void setupPasteLinkDetection() {
        binding.inputField.setOnPasteLinkListener((selectedText, url, selStart, selEnd) -> {
            if (!enableRichTextFormatting) {
                return false;
            }
            Editable editable = binding.inputField.getText();
            if (editable == null) {
                return false;
            }
            // Apply link format span to the selected text with the pasted URL
            RichTextSpanManager.applyLinkFormat(editable, selStart, selEnd, url);
            // Place cursor at the end of the linked text
            binding.inputField.setSelection(selEnd);
            detectActiveFormats();
            return true;
        });
    }

    /**
     * Sets up rich text paste detection on the input field.
     * <p>
     * When the user pastes text that contains markdown formatting (e.g., **bold**,
     * _italic_, ~~strikethrough~~, `inline code`, ```code blocks```, lists, blockquotes,
     * links, etc.), the text is parsed and inserted with the appropriate formatting
     * spans. The toolbar buttons are updated to reflect the formats present in the
     * pasted content.
     * </p>
     */
    private void setupRichTextPaste() {
        binding.inputField.setOnRichTextPasteListener((pasteStart, pasteEnd) -> {
            if (!enableRichTextFormatting) {
                return;
            }
            // Update toolbar to reflect formats in the pasted content
            detectActiveFormats();
        });
    }

    /**
     * Sets up the Enter key behavior for the input field.
     * <p>
     * Based on the configured {@link EnterKeyBehavior}:
     * <ul>
     *     <li>{@link EnterKeyBehavior#SEND_MESSAGE}: Pressing Enter sends the message</li>
     *     <li>{@link EnterKeyBehavior#NEW_LINE}: Pressing Enter inserts a new line</li>
     * </ul>
     * </p>
     * <p>
     * Validates: Requirements 4.6-4.9
     * </p>
     */
    private void setupEnterKeyBehavior() {
        binding.inputField.setOnEditorActionListener((textView, actionId, event) -> {
            // Handle IME action (software keyboard "Send" or "Done" action)
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (enterKeyBehavior == EnterKeyBehavior.SEND_MESSAGE) {
                    handleSendClick();
                    return true;
                }
            }
            return false;
        });

        // Handle hardware keyboard Enter key
        binding.inputField.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (enterKeyBehavior == EnterKeyBehavior.SEND_MESSAGE) {
                    // Check if Shift is pressed - if so, insert new line instead of sending
                    if (event.isShiftPressed()) {
                        // Let the system handle it (insert new line)
                        return false;
                    }
                    // Send the message
                    handleSendClick();
                    return true;
                }
                // EnterKeyBehavior.NEW_LINE - let the system handle it (insert new line)
                return false;
            }
            return false;
        });
    }

    /**
     * Applies the specified format to the input field text.
     * Only applies formatting if enableRichTextFormatting is true.
     * <p>
     * Uses span-based formatting (RichTextSpanManager) for WYSIWYG editing.
     * No markdown markers are inserted - formatting is purely visual via spans.
     * </p>
     * <p>
     * When no text is selected, the format is toggled as a "pending format" -
     * any text typed after will have that format applied automatically.
     * This provides Slack-like behavior where clicking Bold with no selection
     * makes the button appear selected and subsequent typing is bold.
     * </p>
     * <p>
     * For link formatting, a dialog is shown to prompt for the URL when text is selected.
     * If no text is selected, a toast message is shown.
     * </p>
     *
     * @param formatType The format type to apply.
    */
    private void applyFormat(FormatType formatType) {
        // Don't apply formatting if rich text formatting is disabled
        if (!enableRichTextFormatting) {
            return;
        }
        
        Editable editable = binding.inputField.getText();
        if (editable == null) return;

        int selectionStart = binding.inputField.getSelectionStart();
        int selectionEnd = binding.inputField.getSelectionEnd();

        // Special handling for link format - show dialog for URL input
        if (formatType == FormatType.LINK) {
            handleLinkFormat(editable, selectionStart, selectionEnd);
            return;
        }

        // Special handling for list formats - insert initial bullet/number when no selection
        if (formatType == FormatType.BULLET_LIST || formatType == FormatType.ORDERED_LIST) {
            handleListFormat(editable, selectionStart, selectionEnd, formatType);
            return;
        }

        // Check if there's a selection
        if (selectionStart == selectionEnd) {
            // No selection - special handling for CODE_BLOCK and INLINE_CODE
            // to show visual immediately by inserting a placeholder
            if (formatType == FormatType.CODE_BLOCK || formatType == FormatType.INLINE_CODE) {
                handleCodeFormatNoSelection(editable, selectionStart, formatType);
                return;
            }
            
            // Special handling for BLOCKQUOTE: line-scoped deselect + code block combination
            if (formatType == FormatType.BLOCKQUOTE) {
                handleBlockquoteFormatNoSelection(editable, selectionStart);
                return;
            }
            
            // For other formats, toggle pending format for future typing
            if (formatSpanWatcher != null) {
                // Check if format is currently active from span at cursor position
                boolean isActiveFromSpan = RichTextSpanManager.hasFormatInRange(
                        editable, selectionStart, selectionStart, formatType);
                boolean isPending = formatSpanWatcher.isPendingFormat(formatType);
                boolean isDisabled = formatSpanWatcher.isExplicitlyDisabled(formatType);
                
                // Determine current effective state
                boolean isCurrentlyActive = (isActiveFromSpan || isPending) && !isDisabled;
                
                if (isCurrentlyActive) {
                    // Currently active - disable it
                    // Use the new method that also updates span flags to prevent auto-extension
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, formatType, selectionStart);
                } else {
                    // Currently inactive - enable it
                    // Use the new method that also updates span flags to allow auto-extension
                    formatSpanWatcher.enableFormatWithSpanUpdate(editable, formatType, selectionStart);
                }
                
                // Update toolbar to reflect new state
                detectActiveFormats();
            }
            return;
        }

        // Use span-based formatter instead of markdown-based formatter
        // This applies visual formatting without inserting markdown markers
        
        // If activating CODE_BLOCK with selection, remove list and inline formats first
        if (formatType == FormatType.CODE_BLOCK) {
            boolean isCodeBlockActive = RichTextSpanManager.hasFormatInRange(
                    editable, selectionStart, selectionEnd, FormatType.CODE_BLOCK);
            if (!isCodeBlockActive) {
                // Activating code block — remove list and blockquote formats in the range
                RichTextSpanManager.removeFormat(editable, selectionStart, selectionEnd, FormatType.BULLET_LIST);
                RichTextSpanManager.removeFormat(editable, selectionStart, selectionEnd, FormatType.ORDERED_LIST);
                RichTextSpanManager.removeFormat(editable, selectionStart, selectionEnd, FormatType.BLOCKQUOTE, getContext());
                if (formatSpanWatcher != null) {
                    formatSpanWatcher.disableFormat(FormatType.BULLET_LIST);
                    formatSpanWatcher.disableFormat(FormatType.ORDERED_LIST);
                    formatSpanWatcher.disableFormat(FormatType.BLOCKQUOTE);
                    // Deselect inline formats so they don't apply inside code blocks.
                    // Use disableFormatWithSpanUpdate to also change span flags to
                    // EXCLUSIVE_EXCLUSIVE, preventing Android from auto-extending
                    // these spans onto newly inserted text (e.g., mentions).
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.BOLD, selectionStart);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.ITALIC, selectionStart);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.UNDERLINE, selectionStart);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.STRIKETHROUGH, selectionStart);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.INLINE_CODE, selectionStart);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.LINK, selectionStart);
                }
                // Remove existing inline code and link spans from the selection.
                // removeTextStylesFromRange (called inside applyFormat) only strips
                // bold/italic/underline/strikethrough — inline code and link must be
                // removed explicitly so code blocks contain plain text only.
                RichTextSpanManager.removeFormat(editable, selectionStart, selectionEnd, FormatType.INLINE_CODE);
                RichTextSpanManager.removeFormat(editable, selectionStart, selectionEnd, FormatType.LINK);
            } else {
                // Deactivating code block — restore mention formatting before removing the span
                restoreMentionFormattingFromCodeBlock(editable, selectionStart, selectionEnd);
            }
        }

        // If toggling INLINE_CODE with selection, handle mention formatting
        if (formatType == FormatType.INLINE_CODE) {
            boolean isInlineCodeActive = RichTextSpanManager.hasFormatInRange(
                    editable, selectionStart, selectionEnd, FormatType.INLINE_CODE);
            if (isInlineCodeActive) {
                // Deactivating inline code — restore mention formatting before removing the span
                restoreMentionFormattingFromCodeBlock(editable, selectionStart, selectionEnd);
            }
        }

        // If activating blockquote with selection, remove code block first
        if (formatType == FormatType.BLOCKQUOTE) {
            boolean isBlockquoteActive = RichTextSpanManager.hasFormatInRange(
                    editable, selectionStart, selectionEnd, FormatType.BLOCKQUOTE);
            if (!isBlockquoteActive) {
                boolean hasCodeBlock = RichTextSpanManager.hasFormatInRange(
                        editable, selectionStart, selectionEnd, FormatType.CODE_BLOCK);
                if (hasCodeBlock) {
                    RichTextSpanManager.removeFormat(editable, selectionStart, selectionEnd, FormatType.CODE_BLOCK, getContext());
                    if (formatSpanWatcher != null) {
                        formatSpanWatcher.disableFormat(FormatType.CODE_BLOCK);
                    }
                }
            }
        }
        
        RichTextSpanManager.toggleFormat(editable, selectionStart, selectionEnd, formatType, getContext());

        // When code block was just activated, strip mention formatting so mentions
        // appear as plain text inside the code block.
        if (formatType == FormatType.CODE_BLOCK) {
            boolean isNowCodeBlock = RichTextSpanManager.hasFormatInRange(
                    editable, selectionStart, selectionEnd, FormatType.CODE_BLOCK);
            if (isNowCodeBlock) {
                stripMentionFormattingInCodeBlock(editable, selectionStart, selectionEnd);
            }
            // Force DynamicLayout to recompute line heights by re-setting the text.
            // This ensures chooseHeight is called fresh for all remaining code block spans.
            binding.inputField.post(() -> {
                Editable e = binding.inputField.getText();
                if (e != null) {
                    int sel = binding.inputField.getSelectionStart();
                    // Append and remove a space to force full layout rebuild
                    int len = e.length();
                    e.append(" ");
                    e.delete(len, len + 1);
                    if (sel >= 0 && sel <= e.length()) {
                        binding.inputField.setSelection(sel);
                    }
                }
            });
        }

        // When inline code was just activated, strip mention formatting so mentions
        // appear as plain text inside the inline code.
        if (formatType == FormatType.INLINE_CODE) {
            boolean isNowInlineCode = RichTextSpanManager.hasFormatInRange(
                    editable, selectionStart, selectionEnd, FormatType.INLINE_CODE);
            if (isNowInlineCode) {
                stripMentionFormattingInCodeBlock(editable, selectionStart, selectionEnd);
            }
        }

        // Update mention spans: remove format spans from mention ranges
        removeFormatsFromMentions(editable, selectionStart, selectionEnd);

        // Update active formats display
        detectActiveFormats();
    }

    /**
     * Removes RichTextFormatSpan instances that overlap with NonEditableSpan (mention)
     * ranges. If a format span partially overlaps a mention, it is split so that
     * the portions outside the mention are preserved while the mention range is left
     * unformatted.
     *
     * @param editable The editable text.
     * @param start    The start of the range to check.
     * @param end      The end of the range to check.
     */
    private void removeFormatsFromMentions(Editable editable, int start, int end) {
        if (editable == null) return;

        NonEditableSpan[] mentionSpans = editable.getSpans(start, end, NonEditableSpan.class);
        if (mentionSpans.length == 0) return;

        for (NonEditableSpan mentionSpan : mentionSpans) {
            int mentionStart = editable.getSpanStart(mentionSpan);
            int mentionEnd = editable.getSpanEnd(mentionSpan);
            if (mentionStart < 0 || mentionEnd < 0) continue;

            // Get all format spans overlapping this mention
            RichTextFormatSpan[] formatSpans = editable.getSpans(
                    mentionStart, mentionEnd, RichTextFormatSpan.class);

            for (RichTextFormatSpan formatSpan : formatSpans) {
                int fStart = editable.getSpanStart(formatSpan);
                int fEnd = editable.getSpanEnd(formatSpan);
                int fFlags = editable.getSpanFlags(formatSpan);
                FormatType formatType = formatSpan.getFormatType();

                // Skip line-based formats — they must cover the entire line to render
                // correctly (bullet symbol, number, blockquote bar, code block background).
                // Splitting them around a mention would break toMarkdown detection and
                // visual rendering.
                // Also skip INLINE_CODE — it should visually override mentions (plain text),
                // and mention formatting is restored when inline code is removed.
                if (formatType == FormatType.BULLET_LIST
                        || formatType == FormatType.ORDERED_LIST
                        || formatType == FormatType.BLOCKQUOTE
                        || formatType == FormatType.CODE_BLOCK
                        || formatType == FormatType.INLINE_CODE) {
                    continue;
                }

                // Remove the original span
                editable.removeSpan(formatSpan);

                // Re-apply to the portion before the mention (if any)
                if (fStart < mentionStart) {
                    RichTextFormatSpan beforeSpan = RichTextSpanManager.createNewSpanForFormat(formatType);
                    if (beforeSpan != null) {
                        editable.setSpan(beforeSpan, fStart, mentionStart, fFlags);
                    }
                }

                // Re-apply to the portion after the mention (if any)
                if (fEnd > mentionEnd) {
                    RichTextFormatSpan afterSpan = RichTextSpanManager.createNewSpanForFormat(formatType);
                    if (afterSpan != null) {
                        editable.setSpan(afterSpan, mentionEnd, fEnd, fFlags);
                    }
                }
            }
        }
    }

    /**
     * Strips mention formatting (NonEditableSpan) from mentions inside a code block range,
     * replacing each with a {@link ConsumedMentionSpan} that stores the original data.
     * This makes mentions appear as plain text inside the code block while preserving
     * enough information to restore them when the code block is removed.
     *
     * @param editable The editable text.
     * @param start    The start of the code block range.
     * @param end      The end of the code block range.
     */
    private void stripMentionFormattingInCodeBlock(Editable editable, int start, int end) {
        if (editable == null) return;

        NonEditableSpan[] mentionSpans = editable.getSpans(start, end, NonEditableSpan.class);
        for (NonEditableSpan span : mentionSpans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            if (spanStart < 0 || spanEnd < 0) continue;

            // Store original data in a restorable ConsumedMentionSpan
            ConsumedMentionSpan consumed = new ConsumedMentionSpan(
                    span.getId(),
                    span.getText(),
                    span.getSuggestionItem(),
                    span.getTextAppearance()
            );

            editable.removeSpan(span);
            editable.setSpan(consumed, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Restores mention formatting (NonEditableSpan) from {@link ConsumedMentionSpan}
     * markers that were created by {@link #stripMentionFormattingInCodeBlock}.
     * Only spans that carry restoration data ({@link ConsumedMentionSpan#canRestore()})
     * are converted back; plain markers (created during inline-code insertion) are left
     * untouched.
     *
     * @param editable The editable text.
     * @param start    The start of the range to restore.
     * @param end      The end of the range to restore.
     */
    private void restoreMentionFormattingFromCodeBlock(Editable editable, int start, int end) {
        if (editable == null) return;

        ConsumedMentionSpan[] consumed = editable.getSpans(start, end, ConsumedMentionSpan.class);
        for (ConsumedMentionSpan cs : consumed) {
            if (!cs.canRestore()) {
                continue;
            }

            int spanStart = editable.getSpanStart(cs);
            int spanEnd = editable.getSpanEnd(cs);
            if (spanStart < 0 || spanEnd < 0) {
                continue;
            }

            // Rebuild the NonEditableSpan. When the original textAppearance is
            // available (span was stripped from a live code block toggle), use it
            // directly. When textAppearance is null (span was reconstructed from
            // display-name detection in setEditMessage), fall back to the
            // SuggestionItem constructor which derives styling from the item.
            NonEditableSpan restored;
            if (cs.getTextAppearance() != null) {
                restored = new NonEditableSpan(
                        cs.getId(), cs.getText(), cs.getTextAppearance());
                if (cs.getSuggestionItem() != null) {
                    restored.setSuggestionItem(cs.getSuggestionItem());
                }
            } else if (cs.getSuggestionItem() != null) {
                restored = new NonEditableSpan(
                        cs.getId(), cs.getText(), cs.getSuggestionItem());
            } else {
                continue;
            }

            editable.removeSpan(cs);
            editable.setSpan(restored, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Force the EditText to re-render so the restored NonEditableSpan
        // visual styling (color, typeface, background) takes effect immediately.
        if (consumed.length > 0) {
            binding.inputField.invalidate();
        }
    }

    /**
     * Handles CODE_BLOCK and INLINE_CODE format when there's no text selection.
     * Removes a block-level format (CODE_BLOCK, BLOCKQUOTE, BULLET_LIST, ORDERED_LIST)
     * from only the current line where the cursor is positioned.
     * <p>
     * If the span covers only the current line (or is a placeholder), it is removed entirely.
     * If the span extends beyond the current line, it is split: the portion covering the
     * current line is removed, and the remaining parts are preserved as separate spans.
     * Other format types on the same line are left untouched.
     * </p>
     *
     * @param editable       The editable text.
     * @param cursorPosition The current cursor position.
     * @param formatType     The format type to remove from the current line.
     */
    private void removeBlockFormatFromCurrentLine(Editable editable, int cursorPosition, FormatType formatType) {
        if (editable == null) return;

        int lineStart = findLineStart(editable, cursorPosition);
        int lineEnd = findLineEnd(editable, cursorPosition);

        // Build the removal range. We need to cover at least [lineStart, lineEnd].
        // Include the trailing newline so the span doesn't linger on the boundary.
        int removalEnd = Math.min(lineEnd + 1, editable.length());

        // If the line is empty (lineStart == lineEnd), the range would be zero-width
        // and removeFormat wouldn't find any overlapping spans. In that case, use
        // cursorPosition ± 1 to ensure we catch spans that include this position.
        if (lineStart >= removalEnd) {
            // Zero-width range — expand to catch spans at cursor
            lineStart = Math.max(0, cursorPosition - 1);
            removalEnd = Math.min(cursorPosition + 1, editable.length());
            // Still zero? Nothing to remove.
            if (lineStart >= removalEnd) {
                if (formatSpanWatcher != null) {
                    formatSpanWatcher.disableFormat(formatType);
                }
                return;
            }
        }

        // Remove the format from the current line range only.
        // RichTextSpanManager.removeFormat handles span splitting automatically:
        // it removes the original span and re-creates spans for portions before/after the range.
        RichTextSpanManager.removeFormat(editable, lineStart, removalEnd, formatType, getContext());

        // Disable the format so new typing won't get this format
        if (formatSpanWatcher != null) {
            formatSpanWatcher.disableFormat(formatType);
        }
    }

    /**
     * Handles BLOCKQUOTE format when there's no text selection.
     * <p>
     * When enabling: if the cursor is mid-line inside a code block, inserts a newline
     * so the blockquote starts on its own line (mirrors the code-block-inside-blockquote logic).
     * Then enables the blockquote as a pending format.
     * </p>
     * <p>
     * When disabling: removes blockquote from only the current line, preserving other formats.
     * </p>
     *
     * @param editable       The editable text.
     * @param cursorPosition The current cursor position.
     */
    private void handleBlockquoteFormatNoSelection(Editable editable, int cursorPosition) {
        if (editable == null || formatSpanWatcher == null) {
            return;
        }

        boolean isActiveFromSpan = RichTextSpanManager.hasFormatInRange(
                editable, cursorPosition, cursorPosition, FormatType.BLOCKQUOTE);
        boolean isPending = formatSpanWatcher.isPendingFormat(FormatType.BLOCKQUOTE);
        boolean isDisabled = formatSpanWatcher.isExplicitlyDisabled(FormatType.BLOCKQUOTE);
        boolean isCurrentlyActive = (isActiveFromSpan || isPending) && !isDisabled;

        if (isCurrentlyActive) {
            // Deselect: remove blockquote from the current line only
            removeBlockFormatFromCurrentLine(editable, cursorPosition, FormatType.BLOCKQUOTE);
        } else {
            // Enable blockquote.
            int lineStart = findLineStart(editable, cursorPosition);
            int lineEnd = findLineEnd(editable, cursorPosition);

            // If code block is active on this line, remove it first
            int spanCheckEnd = Math.max(lineEnd, lineStart + 1);
            boolean hasCodeBlock = RichTextSpanManager.hasFormatInRange(
                    editable, lineStart, spanCheckEnd, FormatType.CODE_BLOCK);
            if (hasCodeBlock) {
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.CODE_BLOCK, getContext());
                if (formatSpanWatcher != null) {
                    formatSpanWatcher.disableFormat(FormatType.CODE_BLOCK);
                }
            }
            
            // Check if the current line already has content (e.g. list text, code block).
            // If so, apply the blockquote span immediately so the vertical bar draws
            // without waiting for the user to type more text.
            boolean lineHasContent = lineEnd > lineStart;
            if (lineHasContent) {
                int spanEnd = Math.max(lineEnd, lineStart + 1);
                
                // To ensure correct leading margin order (blockquote outermost),
                // temporarily remove any list spans on this line, apply blockquote,
                // then re-apply the list spans. This guarantees blockquote margin
                // is processed first, matching the order when blockquote is selected first.
                BulletListFormatSpan[] bulletSpans = editable.getSpans(lineStart, spanEnd, BulletListFormatSpan.class);
                NumberedListFormatSpan[] numberedSpans = editable.getSpans(lineStart, spanEnd, NumberedListFormatSpan.class);
                
                // Store and remove list spans
                for (BulletListFormatSpan bs : bulletSpans) {
                    editable.removeSpan(bs);
                }
                for (NumberedListFormatSpan ns : numberedSpans) {
                    editable.removeSpan(ns);
                }
                
                // Apply blockquote first (outermost layer)
                BlockquoteFormatSpan bqSpan = new BlockquoteFormatSpan(getContext());
                editable.setSpan(bqSpan, lineStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                
                // Re-apply list spans (innermost layer, after blockquote)
                for (BulletListFormatSpan bs : bulletSpans) {
                    editable.setSpan(bs, lineStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                for (NumberedListFormatSpan ns : numberedSpans) {
                    editable.setSpan(ns, lineStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                
                // Also set as pending so it continues on new lines
                formatSpanWatcher.enableFormat(FormatType.BLOCKQUOTE);
            } else {
                // Empty line — insert a placeholder and apply blockquote span
                // for immediate visual feedback (vertical bar draws right away)
                editable.insert(lineStart, "\u200B"); // zero-width space
                int spanEnd = lineStart + 1;
                BlockquoteFormatSpan bqSpan = new BlockquoteFormatSpan(getContext());
                editable.setSpan(bqSpan, lineStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                formatSpanWatcher.enableFormat(FormatType.BLOCKQUOTE);
                binding.inputField.setSelection(spanEnd);
            }
        }

        detectActiveFormats();
    }

    /**
     * Handles CODE_BLOCK and INLINE_CODE format when there's no text selection.
     * <p>
     * This method provides immediate visual feedback by:
     * <ul>
     *   <li>If format is not active: Inserts a zero-width space with the format span applied,
     *       showing the visual styling immediately. The cursor is placed inside the span.</li>
     *   <li>If format is active: Removes the format from the current line only,
     *       preserving other formats and the span on other lines.</li>
     * </ul>
     * </p>
     * <p>
     * For INLINE_CODE, uses the same styling as message bubbles (background + text color).
     * </p>
     *
     * @param editable       The editable text.
     * @param cursorPosition The current cursor position.
     * @param formatType     The format type (CODE_BLOCK or INLINE_CODE).
     */
    private void handleCodeFormatNoSelection(Editable editable, int cursorPosition, FormatType formatType) {
        if (editable == null || formatSpanWatcher == null) {
            return;
        }
        
        // Check if format is currently active
        boolean isActiveFromSpan = RichTextSpanManager.hasFormatInRange(
                editable, cursorPosition, cursorPosition, formatType);
        boolean isPending = formatSpanWatcher.isPendingFormat(formatType);
        boolean isDisabled = formatSpanWatcher.isExplicitlyDisabled(formatType);
        boolean isCurrentlyActive = (isActiveFromSpan || isPending) && !isDisabled;
        
        if (isCurrentlyActive) {
            if (formatType == FormatType.INLINE_CODE) {
                // For inline code, deselecting should only stop future typing
                // from being formatted — the existing span stays intact.
                formatSpanWatcher.disableFormatWithSpanUpdate(editable, formatType, cursorPosition);
            } else {
                // For CODE_BLOCK, restore mention formatting before removing the format
                int lineStart = findLineStart(editable, cursorPosition);
                int lineEnd = findLineEnd(editable, cursorPosition);
                restoreMentionFormattingFromCodeBlock(editable, lineStart, lineEnd);
                // Remove the format from the current line
                removeBlockFormatFromCurrentLine(editable, cursorPosition, formatType);
                // Force DynamicLayout to recompute line heights
                binding.inputField.post(() -> {
                    Editable e = binding.inputField.getText();
                    if (e != null) {
                        int sel = binding.inputField.getSelectionStart();
                        int len = e.length();
                        e.append(" ");
                        e.delete(len, len + 1);
                        if (sel >= 0 && sel <= e.length()) {
                            binding.inputField.setSelection(sel);
                        }
                    }
                });
            }
        } else {
            // Currently inactive - enable it with a placeholder for immediate visual
            
            // If activating CODE_BLOCK, deselect any active list and inline formats first
            if (formatType == FormatType.CODE_BLOCK) {
                int lineStart = findLineStart(editable, cursorPosition);
                int lineEnd = findLineEnd(editable, cursorPosition);
                int spanCheckEnd = Math.max(lineEnd, lineStart + 1);
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.BULLET_LIST);
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.ORDERED_LIST);
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.BLOCKQUOTE, getContext());
                if (formatSpanWatcher != null) {
                    formatSpanWatcher.disableFormat(FormatType.BULLET_LIST);
                    formatSpanWatcher.disableFormat(FormatType.ORDERED_LIST);
                    formatSpanWatcher.disableFormat(FormatType.BLOCKQUOTE);
                    // Deselect inline formats so they don't apply inside code blocks.
                    // Use disableFormatWithSpanUpdate to also change span flags to
                    // EXCLUSIVE_EXCLUSIVE, preventing Android from auto-extending
                    // these spans onto newly inserted text (e.g., mentions).
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.BOLD, cursorPosition);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.ITALIC, cursorPosition);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.UNDERLINE, cursorPosition);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.STRIKETHROUGH, cursorPosition);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.INLINE_CODE, cursorPosition);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.LINK, cursorPosition);
                }
                // Remove existing inline format spans from the line so that
                // no bold/italic/underline/strikethrough formatting remains
                // inside the code block. Code blocks must contain plain text only.
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.BOLD);
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.ITALIC);
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.UNDERLINE);
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.STRIKETHROUGH);
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.INLINE_CODE);
                RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.LINK);
                // Strip mention formatting so mentions appear as plain text inside code block
                stripMentionFormattingInCodeBlock(editable, lineStart, lineEnd);
            }
            
            int insertPos = cursorPosition;
            
            // For CODE_BLOCK: if the cursor is mid-line inside a blockquote,
            // insert a newline first so the code block starts on its own line.
            // This produces the correct "> ```\n> code\n> ```" markdown output.
            if (formatType == FormatType.CODE_BLOCK && insertPos > 0) {
                boolean isAtLineStart = (editable.charAt(insertPos - 1) == '\n');
                if (!isAtLineStart) {
                    // Check if cursor is inside a blockquote span
                    BlockquoteFormatSpan[] bqSpans = editable.getSpans(
                            insertPos, insertPos, BlockquoteFormatSpan.class);
                    if (bqSpans != null && bqSpans.length > 0) {
                        editable.insert(insertPos, "\n");
                        insertPos++;
                    }
                }
            }
            
            // Insert a zero-width space as placeholder
            String placeholder = "\u200B"; // Zero-width space
            editable.insert(insertPos, placeholder);
            
            // Apply the format span to the placeholder
            int spanStart = insertPos;
            int spanEnd = insertPos + placeholder.length();
            
            if (formatType == FormatType.CODE_BLOCK) {
                // Apply code block span
                CodeBlockFormatSpan span = new CodeBlockFormatSpan(getContext());
                editable.setSpan(span, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                // Apply monospace font
                editable.setSpan(new android.text.style.TypefaceSpan("monospace"),
                        spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else if (formatType == FormatType.INLINE_CODE) {
                // Apply inline code span with composer styling (same as bubble)
                InlineCodeFormatSpan span = new InlineCodeFormatSpan(getContext());
                editable.setSpan(span, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            
            // Enable the pending format so subsequent typing continues the format
            formatSpanWatcher.enableFormat(formatType);
            
            // Set cursor inside the span (after the placeholder)
            binding.inputField.setSelection(spanEnd);
        }
        
        // Update toolbar to reflect new state
        detectActiveFormats();
    }

    /**
     * Handles list format application (bullet list or numbered list).
     * <p>
     * When a list button is clicked:
     * <ul>
     *   <li>If the same list format already exists at cursor, removes it (toggle off)</li>
     *   <li>If a different list format exists, removes it first then applies the new one</li>
     *   <li>If text is selected, applies list format to the selected lines</li>
     *   <li>If no text is selected, inserts initial bullet/number at cursor position</li>
     * </ul>
     * </p>
     * <p>
     * Validates: Requirements 6.1, 6.3, 7.1, 7.3
     * </p>
     *
     * @param editable       The editable text.
     * @param selectionStart The start of the selection.
     * @param selectionEnd   The end of the selection.
     * @param formatType     The list format type (BULLET_LIST or ORDERED_LIST).
     */
    private void handleListFormat(Editable editable, int selectionStart, int selectionEnd, FormatType formatType) {
        // Determine the range to check for existing spans
        // If there's a selection, check the entire selection range
        // Otherwise, check the current line
        int checkStart;
        int checkEnd;
        
        if (selectionStart != selectionEnd) {
            // There's a selection - find the full line range that covers the selection
            checkStart = findLineStart(editable, selectionStart);
            checkEnd = findLineEnd(editable, selectionEnd);
        } else {
            // No selection - check the current line
            checkStart = findLineStart(editable, selectionStart);
            checkEnd = findLineEnd(editable, selectionStart);
        }
        
        // Ensure we have a valid range for span detection
        int spanCheckEnd = Math.max(checkEnd, checkStart + 1);
        
        // Check if the SAME list format already exists in the range OR is pending
        boolean hasSameListFormat = false;
        boolean hasOtherListFormat = false;
        
        // Check for bullet list spans in the range
        BulletListFormatSpan[] bulletSpans = editable.getSpans(checkStart, spanCheckEnd, BulletListFormatSpan.class);
        boolean hasBulletFormat = bulletSpans != null && bulletSpans.length > 0;
        
        // Check for numbered list spans in the range
        NumberedListFormatSpan[] numberedSpans = editable.getSpans(checkStart, spanCheckEnd, NumberedListFormatSpan.class);
        boolean hasNumberedFormat = numberedSpans != null && numberedSpans.length > 0;
        
        // Also check if the format is pending (set by list continuation on Enter)
        boolean isBulletPending = formatSpanWatcher != null && formatSpanWatcher.isPendingFormat(FormatType.BULLET_LIST);
        boolean isNumberedPending = formatSpanWatcher != null && formatSpanWatcher.isPendingFormat(FormatType.ORDERED_LIST);
        
        if (formatType == FormatType.BULLET_LIST) {
            hasSameListFormat = hasBulletFormat || isBulletPending;
            hasOtherListFormat = hasNumberedFormat || isNumberedPending;
        } else if (formatType == FormatType.ORDERED_LIST) {
            hasSameListFormat = hasNumberedFormat || isNumberedPending;
            hasOtherListFormat = hasBulletFormat || isBulletPending;
        }
        
        // If the same format exists (as span or pending), toggle it off
        if (hasSameListFormat) {
            RichTextSpanManager.removeFormat(editable, checkStart, spanCheckEnd, formatType);
            // Also clear any pending list format
            if (formatSpanWatcher != null) {
                formatSpanWatcher.disableFormat(formatType);
            }
            detectActiveFormats();
            return;
        }
        
        // If a different list format exists, remove it first from the entire range
        if (hasOtherListFormat) {
            FormatType otherFormat = (formatType == FormatType.BULLET_LIST) ? FormatType.ORDERED_LIST : FormatType.BULLET_LIST;
            RichTextSpanManager.removeFormat(editable, checkStart, spanCheckEnd, otherFormat);
            // Also clear the other pending format
            if (formatSpanWatcher != null) {
                formatSpanWatcher.disableFormat(otherFormat);
            }
        }
        
        // Remove code block format if active — lists and code block can't coexist
        // Restore mention formatting first since mentions were stripped when code block was applied
        restoreMentionFormattingFromCodeBlock(editable, checkStart, spanCheckEnd);
        RichTextSpanManager.removeFormat(editable, checkStart, spanCheckEnd, FormatType.CODE_BLOCK, getContext());
        if (formatSpanWatcher != null) {
            formatSpanWatcher.disableFormat(FormatType.CODE_BLOCK);
        }
        
        // If there's a selection, apply list format to selected lines
        if (selectionStart != selectionEnd) {
            RichTextSpanManager.applyFormat(editable, selectionStart, selectionEnd, formatType, getContext());
            // Clear explicitly disabled state since user is re-enabling the format
            if (formatSpanWatcher != null) {
                formatSpanWatcher.clearExplicitlyDisabled(formatType);
            }
            detectActiveFormats();
            return;
        }
        
        // No selection - insert initial bullet/number at cursor position
        // Use checkStart and checkEnd which represent the current line boundaries
        int lineStart = checkStart;
        int lineEnd = checkEnd;
        
        // Check if current line is empty
        String lineContent = lineStart < lineEnd ? editable.subSequence(lineStart, lineEnd).toString() : "";
        
        if (lineContent.trim().isEmpty()) {
            // Empty line - insert a placeholder space if line is completely empty
            if (lineStart == lineEnd) {
                editable.insert(lineStart, " ");
                lineEnd = lineStart + 1;
            }
        }
        
        // Apply the list format span to the current line
        if (formatType == FormatType.ORDERED_LIST) {
            int nextNumber = listContinuationHandler != null ? 
                    listContinuationHandler.getNextListNumber(editable, selectionStart) : 1;
            NumberedListFormatSpan span = new NumberedListFormatSpan(nextNumber, getContext());
            editable.setSpan(span, lineStart, Math.max(lineEnd, lineStart + 1), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            BulletListFormatSpan span = new BulletListFormatSpan(getContext());
            editable.setSpan(span, lineStart, Math.max(lineEnd, lineStart + 1), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        
        // Clear explicitly disabled state since user is re-enabling the format
        if (formatSpanWatcher != null) {
            formatSpanWatcher.clearExplicitlyDisabled(formatType);
        }
        
        // Move cursor to end of line for typing
        binding.inputField.setSelection(Math.max(lineEnd, lineStart + 1));
        
        // Update toolbar to reflect new state
        detectActiveFormats();
    }

    /**
     * Finds the start of the line containing the given position.
     *
     * @param editable The editable text.
     * @param position The position within the line.
     * @return The start index of the line.
     */
    private int findLineStart(Editable editable, int position) {
        if (position <= 0) {
            return 0;
        }
        
        int lineStart = position - 1;
        while (lineStart > 0 && editable.charAt(lineStart) != '\n') {
            lineStart--;
        }
        
        // If we found a newline, the line starts after it
        if (lineStart > 0 || (lineStart == 0 && editable.length() > 0 && editable.charAt(0) == '\n')) {
            lineStart++;
        }
        
        return Math.max(0, lineStart);
    }

    /**
     * Finds the end of the line containing the given position.
     *
     * @param editable The editable text.
     * @param position The position within the line.
     * @return The end index of the line (exclusive, points to newline or end of text).
     */
    private int findLineEnd(Editable editable, int position) {
        int length = editable.length();
        if (position >= length) {
            return length;
        }
        
        int lineEnd = position;
        while (lineEnd < length && editable.charAt(lineEnd) != '\n') {
            lineEnd++;
        }
        
        return lineEnd;
    }

    /**
     * Handles Enter key press for list continuation.
     * <p>
     * When Enter is pressed within a list:
     * <ul>
     *   <li>If current line has content (more than just whitespace), creates a new list item on the next line</li>
     *   <li>If current line is empty or only whitespace (double Enter), exits list mode and deselects toolbar button</li>
     * </ul>
     * </p>
     * <p>
     * Validates: Requirements 6.3, 6.4, 7.3, 7.4
     * </p>
     *
     * @param editable      The editable text.
     * @param newlinePosition The position where the newline was inserted.
     */
    private void handleListEnterKey(Editable editable, int newlinePosition) {
        if (listContinuationHandler == null || editable == null) {
            return;
        }
        
        // Check for double Enter: the line between the previous newline and the
        // newline just inserted should contain only whitespace / zero-width spaces.
        // This handles the case where the first Enter created a placeholder (e.g. " ")
        // for a list item — the second Enter should exit the format.
        if (newlinePosition > 0) {
            boolean isDoubleEnter = false;
            
            if (editable.charAt(newlinePosition - 1) == '\n') {
                // Trivial case: two consecutive newlines with nothing between them
                isDoubleEnter = true;
            } else {
                // Check if the line before the newline we just inserted is "empty"
                // (contains only whitespace and zero-width spaces — i.e. a placeholder line)
                int prevNewline = -1;
                for (int i = newlinePosition - 1; i >= 0; i--) {
                    if (editable.charAt(i) == '\n') {
                        prevNewline = i;
                        break;
                    }
                }
                if (prevNewline >= 0) {
                    // There is a previous newline — check if everything between
                    // prevNewline+1 and newlinePosition is only whitespace/ZWS
                    boolean lineIsEmpty = true;
                    for (int i = prevNewline + 1; i < newlinePosition; i++) {
                        char c = editable.charAt(i);
                        if (c != ' ' && c != '\u200B' && c != '\t') {
                            lineIsEmpty = false;
                            break;
                        }
                    }
                    isDoubleEnter = lineIsEmpty;
                } else {
                    // No previous newline exists — this is the first line.
                    // Check if the ENTIRE content before the newline is only whitespace/ZWS
                    // (empty block case: user selected format, didn't type anything, pressed Enter).
                    // This should only trigger exit if the block has NO real content at all.
                    boolean entireContentIsEmpty = true;
                    for (int i = 0; i < newlinePosition; i++) {
                        char c = editable.charAt(i);
                        if (c != ' ' && c != '\u200B' && c != '\t') {
                            entireContentIsEmpty = false;
                            break;
                        }
                    }
                    if (entireContentIsEmpty) {
                        // Check if there's an active block format (code block, blockquote, or list)
                        // that should be exited
                        boolean hasBlockFormat = false;
                        if (formatSpanWatcher != null) {
                            hasBlockFormat = formatSpanWatcher.isPendingFormat(FormatType.CODE_BLOCK)
                                    || formatSpanWatcher.isPendingFormat(FormatType.BLOCKQUOTE)
                                    || formatSpanWatcher.isPendingFormat(FormatType.BULLET_LIST)
                                    || formatSpanWatcher.isPendingFormat(FormatType.ORDERED_LIST);
                        }
                        // Also check for existing spans
                        if (!hasBlockFormat) {
                            hasBlockFormat = editable.getSpans(0, newlinePosition, CodeBlockFormatSpan.class).length > 0
                                    || editable.getSpans(0, newlinePosition, BlockquoteFormatSpan.class).length > 0
                                    || editable.getSpans(0, newlinePosition, BulletListFormatSpan.class).length > 0
                                    || editable.getSpans(0, newlinePosition, NumberedListFormatSpan.class).length > 0;
                        }
                        isDoubleEnter = hasBlockFormat;
                    }
                    // If content is not empty (has real characters), this is NOT a double-enter.
                    // The user typed content on the first line and pressed Enter - just continue the format.
                }
            }
            
            if (isDoubleEnter) {
                // Double Enter detected — exit the block format.
                // For lists: remove the empty placeholder line and its formatting spans.
                // For code blocks/blockquotes: just disable the format without deleting content
                // to avoid triggering recursive text change events.
                
                // Determine the range of the empty line (for lists)
                int emptyLineStart;
                if (editable.charAt(newlinePosition - 1) == '\n') {
                    emptyLineStart = newlinePosition; // nothing to delete before the newline
                } else {
                    // Find the previous newline
                    emptyLineStart = newlinePosition - 1;
                    while (emptyLineStart > 0 && editable.charAt(emptyLineStart) != '\n') {
                        emptyLineStart--;
                    }
                    if (emptyLineStart > 0 && editable.charAt(emptyLineStart) == '\n') {
                        emptyLineStart++; // start after the newline
                    } else if (emptyLineStart == 0 && editable.charAt(0) != '\n') {
                        // No previous newline found - start from the beginning
                        emptyLineStart = 0;
                    }
                }
                
                int emptyLineEnd = newlinePosition; // the newline char itself
                
                // Check pending formats first (for formats applied via toolbar button).
                // IMPORTANT: Lists are checked BEFORE code block/blockquote so that
                // when a list is nested inside a code block or blockquote, double-Enter
                // exits only the list while preserving the outer block format.
                if (formatSpanWatcher != null) {
                    // Handle BULLET_LIST - remove placeholder and disable format
                    if (formatSpanWatcher.isPendingFormat(FormatType.BULLET_LIST)) {
                        // Remove spans on the empty line
                        if (emptyLineStart < emptyLineEnd) {
                            for (BulletListFormatSpan s : editable.getSpans(emptyLineStart, emptyLineEnd, BulletListFormatSpan.class)) {
                                editable.removeSpan(s);
                            }
                            editable.delete(emptyLineStart, emptyLineEnd);
                        }
                        formatSpanWatcher.disableFormat(FormatType.BULLET_LIST);
                        detectActiveFormats();
                        return;
                    }
                    
                    // Handle ORDERED_LIST - remove placeholder and disable format
                    if (formatSpanWatcher.isPendingFormat(FormatType.ORDERED_LIST)) {
                        // Remove spans on the empty line
                        if (emptyLineStart < emptyLineEnd) {
                            for (NumberedListFormatSpan s : editable.getSpans(emptyLineStart, emptyLineEnd, NumberedListFormatSpan.class)) {
                                editable.removeSpan(s);
                            }
                            editable.delete(emptyLineStart, emptyLineEnd);
                        }
                        formatSpanWatcher.disableFormat(FormatType.ORDERED_LIST);
                        detectActiveFormats();
                        return;
                    }
                    
                    // Handle CODE_BLOCK - just disable format, don't delete content
                    if (formatSpanWatcher.isPendingFormat(FormatType.CODE_BLOCK)) {
                        truncateBlockSpanBeforeDoubleNewline(editable, newlinePosition, CodeBlockFormatSpan.class);
                        formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.CODE_BLOCK, newlinePosition);
                        detectActiveFormats();
                        return;
                    }
                    
                    // Handle BLOCKQUOTE - just disable format, don't delete content
                    if (formatSpanWatcher.isPendingFormat(FormatType.BLOCKQUOTE)) {
                        truncateBlockSpanBeforeDoubleNewline(editable, newlinePosition, BlockquoteFormatSpan.class);
                        formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.BLOCKQUOTE, newlinePosition);
                        detectActiveFormats();
                        return;
                    }
                }
                
                // Also check for existing spans (for formats applied to selected text).
                // IMPORTANT: Lists are checked BEFORE code block/blockquote so that
                // when a list is nested inside a code block or blockquote, double-Enter
                // exits only the list while preserving the outer block format.
                int checkPos = Math.max(0, emptyLineStart - 1);
                int prevLineStart = findLineStart(editable, checkPos > 0 ? checkPos - 1 : 0);
                int prevLineEnd = checkPos;
                
                // Check for bullet list span
                BulletListFormatSpan[] bulletSpans = editable.getSpans(prevLineStart, Math.max(prevLineEnd, prevLineStart + 1), BulletListFormatSpan.class);
                if (bulletSpans != null && bulletSpans.length > 0) {
                    // Remove spans on the empty line
                    if (emptyLineStart < emptyLineEnd) {
                        for (BulletListFormatSpan s : editable.getSpans(emptyLineStart, emptyLineEnd, BulletListFormatSpan.class)) {
                            editable.removeSpan(s);
                        }
                        editable.delete(emptyLineStart, emptyLineEnd);
                    }
                    if (formatSpanWatcher != null) {
                        formatSpanWatcher.disableFormat(FormatType.BULLET_LIST);
                    }
                    detectActiveFormats();
                    return;
                }
                
                // Check for numbered list span
                NumberedListFormatSpan[] numberedSpans = editable.getSpans(prevLineStart, Math.max(prevLineEnd, prevLineStart + 1), NumberedListFormatSpan.class);
                if (numberedSpans != null && numberedSpans.length > 0) {
                    // Remove spans on the empty line
                    if (emptyLineStart < emptyLineEnd) {
                        for (NumberedListFormatSpan s : editable.getSpans(emptyLineStart, emptyLineEnd, NumberedListFormatSpan.class)) {
                            editable.removeSpan(s);
                        }
                        editable.delete(emptyLineStart, emptyLineEnd);
                    }
                    if (formatSpanWatcher != null) {
                        formatSpanWatcher.disableFormat(FormatType.ORDERED_LIST);
                    }
                    detectActiveFormats();
                    return;
                }
                
                // Check for code block span (existing span, not pending)
                CodeBlockFormatSpan[] codeBlockSpans =
                        editable.getSpans(0, editable.length(), CodeBlockFormatSpan.class);
                if (codeBlockSpans != null && codeBlockSpans.length > 0) {
                    for (CodeBlockFormatSpan span : codeBlockSpans) {
                        int spanEnd = editable.getSpanEnd(span);
                        if (emptyLineStart <= spanEnd + 1) {
                            truncateBlockSpanBeforeDoubleNewline(editable, newlinePosition, CodeBlockFormatSpan.class);
                            if (formatSpanWatcher != null) {
                                formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.CODE_BLOCK, newlinePosition);
                            }
                            detectActiveFormats();
                            return;
                        }
                    }
                }
                
                // Check for blockquote span (existing span, not pending)
                BlockquoteFormatSpan[] blockquoteSpans =
                        editable.getSpans(0, editable.length(), BlockquoteFormatSpan.class);
                if (blockquoteSpans != null && blockquoteSpans.length > 0) {
                    for (BlockquoteFormatSpan span : blockquoteSpans) {
                        int spanEnd = editable.getSpanEnd(span);
                        if (emptyLineStart <= spanEnd + 1) {
                            truncateBlockSpanBeforeDoubleNewline(editable, newlinePosition, BlockquoteFormatSpan.class);
                            if (formatSpanWatcher != null) {
                                formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.BLOCKQUOTE, newlinePosition);
                            }
                            detectActiveFormats();
                            return;
                        }
                    }
                }
                
                // If we got here, we detected a double-enter but couldn't match a specific
                // format. This shouldn't normally happen, but return to avoid continuing
                // into the list continuation logic below.
                detectActiveFormats();
                return;
            }
        }
        
        // The newline was just inserted at newlinePosition
        // We need to check if the line BEFORE the newline had a list format
        int prevLineEnd = newlinePosition;
        int prevLineStart = findLineStart(editable, prevLineEnd > 0 ? prevLineEnd - 1 : 0);
        
        // Check for bullet list on previous line
        BulletListFormatSpan[] bulletSpans = editable.getSpans(prevLineStart, prevLineEnd, BulletListFormatSpan.class);
        if (bulletSpans != null && bulletSpans.length > 0) {
            // First, ensure the original span doesn't extend past the newline
            // This prevents the old span from covering the new line
            for (BulletListFormatSpan span : bulletSpans) {
                int spanEnd = editable.getSpanEnd(span);
                if (spanEnd > newlinePosition) {
                    // The span extends past the newline - trim it back
                    int spanStart = editable.getSpanStart(span);
                    int spanFlags = editable.getSpanFlags(span);
                    editable.removeSpan(span);
                    // Re-apply with end at the newline position (not including newline)
                    if (spanStart < newlinePosition) {
                        editable.setSpan(span, spanStart, newlinePosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            
            // Create new bullet item on the new line immediately
            // Insert a placeholder space with the bullet span so the bullet is visible right away
            int newLineStart = newlinePosition + 1;
            
            // Insert placeholder space at the new line start
            editable.insert(newLineStart, " ");
            
            // Apply bullet span to the placeholder
            BulletListFormatSpan newBulletSpan = new BulletListFormatSpan(getContext());
            editable.setSpan(newBulletSpan, newLineStart, newLineStart + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            
            // Also set pending format so additional characters continue the format
            if (formatSpanWatcher != null) {
                formatSpanWatcher.enableFormat(FormatType.BULLET_LIST);
            }
            
            // Move cursor after the placeholder space
            binding.inputField.setSelection(newLineStart + 1);
            return;
        }
        
        // Check for numbered list on previous line
        NumberedListFormatSpan[] numberedSpans = editable.getSpans(prevLineStart, prevLineEnd, NumberedListFormatSpan.class);
        if (numberedSpans != null && numberedSpans.length > 0) {
            // First, ensure the original span doesn't extend past the newline
            // This prevents the old span from covering the new line
            for (NumberedListFormatSpan span : numberedSpans) {
                int spanEnd = editable.getSpanEnd(span);
                if (spanEnd > newlinePosition) {
                    // The span extends past the newline - trim it back
                    int spanStart = editable.getSpanStart(span);
                    int spanFlags = editable.getSpanFlags(span);
                    editable.removeSpan(span);
                    // Re-apply with end at the newline position (not including newline)
                    if (spanStart < newlinePosition) {
                        editable.setSpan(span, spanStart, newlinePosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            
            // Create new numbered item on the new line immediately
            // Insert a placeholder space with the numbered span so the number is visible right away
            int newLineStart = newlinePosition + 1;
            
            // Calculate the next number BEFORE inserting the placeholder
            // (so we count existing spans correctly)
            int nextNumber = listContinuationHandler != null ? 
                    listContinuationHandler.getNextListNumber(editable, newLineStart) : 1;
            
            // Insert placeholder space at the new line start
            editable.insert(newLineStart, " ");
            
            // Apply numbered span to the placeholder
            NumberedListFormatSpan newNumberedSpan = new NumberedListFormatSpan(nextNumber, getContext());
            editable.setSpan(newNumberedSpan, newLineStart, newLineStart + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            
            // Also set pending format so additional characters continue the format
            if (formatSpanWatcher != null) {
                formatSpanWatcher.enableFormat(FormatType.ORDERED_LIST);
            }
            
            // Move cursor after the placeholder space
            binding.inputField.setSelection(newLineStart + 1);
            return;
        }
        
        // Check for code block on previous line - continue code block on new line
        CodeBlockFormatSpan[] codeBlockSpans =
                editable.getSpans(prevLineStart, prevLineEnd, CodeBlockFormatSpan.class);
        if (codeBlockSpans != null && codeBlockSpans.length > 0) {
            // Enable pending code block format for continuation
            if (formatSpanWatcher != null) {
                formatSpanWatcher.enableFormat(FormatType.CODE_BLOCK);
            }
        }
        
        // Check for blockquote on previous line - continue blockquote on new line
        BlockquoteFormatSpan[] blockquoteSpans =
                editable.getSpans(prevLineStart, prevLineEnd, BlockquoteFormatSpan.class);
        if (blockquoteSpans != null && blockquoteSpans.length > 0) {
            // Enable pending blockquote format for continuation
            if (formatSpanWatcher != null) {
                formatSpanWatcher.enableFormat(FormatType.BLOCKQUOTE);
            }
        }
    }

    /**
     * Truncates block-level format spans (code block, blockquote) so they end
     * before the trailing newlines created by a double-Enter.
     * <p>
     * When the user presses Enter twice inside a code block or blockquote, the
     * span may have been auto-extended by Android (INCLUSIVE_INCLUSIVE flags) to
     * cover the new newline characters. This method trims the span back to end
     * at the last line of real content and changes its flags to EXCLUSIVE_EXCLUSIVE
     * so that subsequent typing is not formatted.
     * </p>
     * <p>
     * IMPORTANT: This method does NOT delete any text content to avoid triggering
     * recursive text change events. It only adjusts span boundaries.
     * </p>
     *
     * @param editable        The editable text.
     * @param newlinePosition The position of the second newline (just inserted).
     * @param spanClass       The class of the span to truncate.
     */
    private <T extends RichTextFormatSpan> void truncateBlockSpanBeforeDoubleNewline(
            Editable editable, int newlinePosition, Class<T> spanClass) {
        if (editable == null) return;

        T[] spans = editable.getSpans(0, editable.length(), spanClass);
        if (spans == null) return;

        for (T span : spans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);

            // Only process spans that cover the newline area
            if (spanEnd < newlinePosition - 1) continue;

            // Find the end of real content — skip back over trailing newlines and whitespace
            int trimEnd = newlinePosition;
            while (trimEnd > spanStart) {
                char c = editable.charAt(trimEnd - 1);
                if (c != '\n' && c != ' ' && c != '\u200B' && c != '\t') {
                    break;
                }
                trimEnd--;
            }
            // trimEnd now points just past the last non-whitespace character.

            if (trimEnd <= spanStart) {
                // The span only contained whitespace/newlines — remove it entirely
                editable.removeSpan(span);
            } else {
                // Re-apply with trimmed range and EXCLUSIVE_EXCLUSIVE flags
                // This prevents the span from auto-extending when user types after it
                editable.removeSpan(span);
                editable.setSpan(span, spanStart, trimEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * Handles backtick auto-trigger for code formatting.
     * <p>
     * This method is called when a backtick (`) is typed and enables automatic
     * code formatting based on the following patterns:
     * <ul>
     *   <li>Triple backticks (```): Enables CODE_BLOCK format for multi-line code</li>
     *   <li>Single backtick followed by text and closing backtick (`text`): 
     *       Applies INLINE_CODE format to the text between backticks</li>
     * </ul>
     * </p>
     * <p>
     * For triple backticks:
     * <ul>
     *   <li>Removes the ``` markers from the text</li>
     *   <li>Enables CODE_BLOCK as a pending format</li>
     *   <li>Subsequent typed text will have code block formatting</li>
     * </ul>
     * </p>
     * <p>
     * For inline code:
     * <ul>
     *   <li>Detects pattern `text` where text is non-empty</li>
     *   <li>Removes the backtick markers</li>
     *   <li>Applies INLINE_CODE span to the text content</li>
     * </ul>
     * </p>
     *
     * @param editable        The editable text.
     * @param backtickPosition The position where the backtick was inserted.
     */
    private void handleBacktickAutoTrigger(Editable editable, int backtickPosition) {
        if (editable == null || formatSpanWatcher == null) {
            return;
        }
        
        String text = editable.toString();
        int textLength = text.length();
        
        // Check for triple backticks (```) - triggers CODE_BLOCK format
        // The backtick at backtickPosition is the one just typed
        // We need at least 3 characters ending at backtickPosition
        if (backtickPosition >= 2) {
            // Check if we have ``` pattern
            if (text.charAt(backtickPosition - 2) == '`' && 
                text.charAt(backtickPosition - 1) == '`' && 
                text.charAt(backtickPosition) == '`') {
                
                // Verify this is at the start of a line or preceded by whitespace/newline
                boolean isValidStart = (backtickPosition == 2) || 
                        (backtickPosition >= 3 && (text.charAt(backtickPosition - 3) == '\n' || 
                                                   text.charAt(backtickPosition - 3) == ' '));
                
                if (isValidStart) {
                    // Remove the ``` markers
                    int markerStart = backtickPosition - 2;
                    editable.delete(markerStart, backtickPosition + 1);
                    
                    // Clean up the current line before applying code block
                    // (same cleanup as the toolbar path in handleCodeFormatNoSelection)
                    int lineStart = findLineStart(editable, markerStart);
                    int lineEnd = findLineEnd(editable, markerStart);
                    int spanCheckEnd = Math.max(lineEnd, lineStart + 1);
                    // Remove list formats
                    RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.BULLET_LIST);
                    RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.ORDERED_LIST);
                    // Disable inline and list pending formats
                    formatSpanWatcher.disableFormat(FormatType.BULLET_LIST);
                    formatSpanWatcher.disableFormat(FormatType.ORDERED_LIST);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.BOLD, markerStart);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.ITALIC, markerStart);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.UNDERLINE, markerStart);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.STRIKETHROUGH, markerStart);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.INLINE_CODE, markerStart);
                    formatSpanWatcher.disableFormatWithSpanUpdate(editable, FormatType.LINK, markerStart);
                    // Remove inline format spans from the line
                    RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.BOLD);
                    RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.ITALIC);
                    RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.UNDERLINE);
                    RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.STRIKETHROUGH);
                    RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.INLINE_CODE);
                    RichTextSpanManager.removeFormat(editable, lineStart, spanCheckEnd, FormatType.LINK);
                    // Strip mention formatting so mentions appear as plain text
                    stripMentionFormattingInCodeBlock(editable, lineStart, lineEnd);
                    
                    // Recalculate insertPos after cleanup (lineEnd may have shifted)
                    int insertPos = markerStart;
                    String placeholder = "\u200B"; // Zero-width space
                    editable.insert(insertPos, placeholder);
                    
                    // Apply code block span to the placeholder
                    CodeBlockFormatSpan span = new CodeBlockFormatSpan(getContext());
                    editable.setSpan(span, insertPos, insertPos + placeholder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    // Apply monospace font
                    editable.setSpan(new android.text.style.TypefaceSpan("monospace"),
                            insertPos, insertPos + placeholder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    
                    // Enable CODE_BLOCK as pending format
                    formatSpanWatcher.enableFormat(FormatType.CODE_BLOCK);
                    
                    // Set cursor inside the code block
                    binding.inputField.setSelection(insertPos + placeholder.length());
                    
                    // Update toolbar to show code block as active
                    detectActiveFormats();
                    return;
                }
            }
        }
        
        // Check for inline code pattern `text` - the closing backtick was just typed
        // Find the opening backtick before this closing one
        if (backtickPosition >= 2) {
            int openingBacktickPos = -1;
            
            // Search backwards for an opening backtick
            // Stop at newlines or if we find another closing pattern
            for (int i = backtickPosition - 1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '`') {
                    // Found a potential opening backtick
                    // Make sure there's content between them
                    if (i < backtickPosition - 1) {
                        openingBacktickPos = i;
                    }
                    break;
                } else if (c == '\n') {
                    // Don't cross newlines for inline code
                    break;
                }
            }
            
            if (openingBacktickPos >= 0) {
                // We have a valid `text` pattern
                int contentStart = openingBacktickPos + 1;
                int contentEnd = backtickPosition;
                
                // Verify there's actual content (not just whitespace)
                String content = text.substring(contentStart, contentEnd);
                if (!content.trim().isEmpty()) {
                    // Extract the content before removing backticks
                    // Remove closing backtick first (higher index)
                    editable.delete(backtickPosition, backtickPosition + 1);
                    // Remove opening backtick
                    editable.delete(openingBacktickPos, openingBacktickPos + 1);
                    
                    // Apply INLINE_CODE span to the content with context for proper styling
                    // After deletion, content is at openingBacktickPos to openingBacktickPos + content.length()
                    int spanStart = openingBacktickPos;
                    int spanEnd = openingBacktickPos + content.length();
                    
                    // Strip mention formatting so mentions appear as plain text inside inline code
                    stripMentionFormattingInCodeBlock(editable, spanStart, spanEnd);
                    
                    // Use InlineCodeFormatSpan with context for proper bubble-like styling
                    InlineCodeFormatSpan span = new InlineCodeFormatSpan(getContext());
                    editable.setSpan(span, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    
                    // Move cursor to end of the formatted text
                    binding.inputField.setSelection(spanEnd);
                    
                    // Update toolbar
                    detectActiveFormats();
                }
            }
        }
    }

    /**
     * Handles auto-triggering of list formats when the user types list syntax.
     * <p>
     * Detects the following patterns at the start of a line:
     * <ul>
     *   <li>"- " → activates bullet list</li>
     *   <li>"N. " (where N is one or more digits) → activates numbered list</li>
     * </ul>
     * The syntax markers are removed and replaced with the visual list span.
     * </p>
     *
     * @param editable      The editable text.
     * @param spacePosition The position where the space was inserted.
     */
    private void handleListSyntaxAutoTrigger(Editable editable, int spacePosition) {
        if (editable == null || formatSpanWatcher == null) {
            return;
        }

        // Find the start of the current line
        int lineStart = findLineStart(editable, spacePosition);
        // Text before the space on this line
        String prefix = editable.subSequence(lineStart, spacePosition).toString();

        // Check for bullet list: exactly "-"
        if (prefix.equals("-")) {
            // Already has a bullet list span? Skip.
            BulletListFormatSpan[] existing = editable.getSpans(lineStart, spacePosition + 1, BulletListFormatSpan.class);
            if (existing != null && existing.length > 0) return;

            // Remove the "- " marker (dash + space)
            editable.delete(lineStart, spacePosition + 1);

            // Insert a zero-width space as placeholder if line is now empty
            int lineEnd = findLineEnd(editable, lineStart);
            if (lineEnd == lineStart) {
                editable.insert(lineStart, "\u200B");
                lineEnd = lineStart + 1;
            }

            // Apply bullet list span
            BulletListFormatSpan span = new BulletListFormatSpan(getContext());
            editable.setSpan(span, lineStart, Math.max(lineEnd, lineStart + 1), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // Enable as pending format for continuation
            if (formatSpanWatcher != null) {
                formatSpanWatcher.clearExplicitlyDisabled(FormatType.BULLET_LIST);
            }

            binding.inputField.setSelection(Math.max(lineEnd, lineStart + 1));
            detectActiveFormats();
            return;
        }

        // Check for numbered list: one or more digits followed by "."
        if (prefix.matches("\\d+\\.")) {
            // Already has a numbered list span? Skip.
            NumberedListFormatSpan[] existing = editable.getSpans(lineStart, spacePosition + 1, NumberedListFormatSpan.class);
            if (existing != null && existing.length > 0) return;

            int number;
            try {
                number = Integer.parseInt(prefix.substring(0, prefix.length() - 1));
            } catch (NumberFormatException e) {
                number = 1;
            }

            // Remove the "N. " marker
            editable.delete(lineStart, spacePosition + 1);

            // Insert a zero-width space as placeholder if line is now empty
            int lineEnd = findLineEnd(editable, lineStart);
            if (lineEnd == lineStart) {
                editable.insert(lineStart, "\u200B");
                lineEnd = lineStart + 1;
            }

            // Apply numbered list span
            NumberedListFormatSpan span = new NumberedListFormatSpan(number, getContext());
            editable.setSpan(span, lineStart, Math.max(lineEnd, lineStart + 1), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // Enable as pending format for continuation
            if (formatSpanWatcher != null) {
                formatSpanWatcher.clearExplicitlyDisabled(FormatType.ORDERED_LIST);
            }

            binding.inputField.setSelection(Math.max(lineEnd, lineStart + 1));
            detectActiveFormats();
        }

        // Check for blockquote: exactly ">"
        if (prefix.equals(">")) {
            // Already has a blockquote span? Skip.
            BlockquoteFormatSpan[] existing = editable.getSpans(lineStart, spacePosition + 1, BlockquoteFormatSpan.class);
            if (existing != null && existing.length > 0) return;

            // Remove the "> " marker
            editable.delete(lineStart, spacePosition + 1);

            // Insert a zero-width space as placeholder if line is now empty
            int lineEnd = findLineEnd(editable, lineStart);
            if (lineEnd == lineStart) {
                editable.insert(lineStart, "\u200B");
                lineEnd = lineStart + 1;
            }

            // Apply blockquote span
            BlockquoteFormatSpan span = new BlockquoteFormatSpan(getContext());
            editable.setSpan(span, lineStart, Math.max(lineEnd, lineStart + 1), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // Enable as pending format for continuation
            if (formatSpanWatcher != null) {
                formatSpanWatcher.enableFormat(FormatType.BLOCKQUOTE);
            }

            binding.inputField.setSelection(Math.max(lineEnd, lineStart + 1));
            detectActiveFormats();
        }
    }

    /**
     * Handles auto-triggering of inline formats when the user types closing markers.
     * <p>
     * Detects the following patterns:
     * <ul>
     *   <li>**text** → Bold (when second closing * is typed)</li>
     *   <li>_text_ → Italic (when closing _ is typed)</li>
     *   <li>~~text~~ → Strikethrough (when second closing ~ is typed)</li>
     * </ul>
     * The syntax markers are removed and the content is wrapped with the visual span.
     * </p>
     *
     * @param editable       The editable text.
     * @param typedPosition  The position where the character was typed.
     */
    private void handleInlineFormatAutoTrigger(Editable editable, int typedPosition) {
        if (editable == null || formatSpanWatcher == null) {
            return;
        }

        String text = editable.toString();
        char typedChar = text.charAt(typedPosition);

        // Bold: **text** — need at least **x** (5 chars)
        if (typedChar == '*' && typedPosition >= 4) {
            // Check if the just-typed char and the one before it form the closing **
            if (text.charAt(typedPosition - 1) != '*') {
                // Not a closing ** yet, skip
            } else {
                // We have closing ** at (typedPosition-1, typedPosition)
                int contentEnd = typedPosition - 1; // content ends before closing **
                int openingEnd = -1;
                // Search backwards for opening **
                for (int i = contentEnd - 1; i >= 1; i--) {
                    if (text.charAt(i) == '*' && text.charAt(i - 1) == '*') {
                        openingEnd = i; // opening ** is at (i-1, i)
                        break;
                    }
                    if (text.charAt(i) == '\n') break;
                }
                if (openingEnd >= 1) {
                    int openingStart = openingEnd - 1;
                    String content = text.substring(openingEnd + 1, contentEnd);
                    if (!content.trim().isEmpty()) {
                        // Remove closing ** (2 chars)
                        editable.delete(typedPosition - 1, typedPosition + 1);
                        // Remove opening ** (2 chars)
                        editable.delete(openingStart, openingStart + 2);
                        // Apply bold span
                        int spanStart = openingStart;
                        int spanEnd = openingStart + content.length();
                        RichTextSpanManager.applyFormat(editable, spanStart, spanEnd, FormatType.BOLD);
                        binding.inputField.setSelection(spanEnd);
                        detectActiveFormats();
                        return;
                    }
                }
            }
        }

        // Strikethrough: ~~text~~ — need at least ~~x~~ (5 chars)
        if (typedChar == '~' && typedPosition >= 4) {
            // Check if the just-typed char and the one before it form the closing ~~
            if (text.charAt(typedPosition - 1) != '~') {
                // Not a closing ~~ yet, skip
            } else {
                int contentEnd = typedPosition - 1; // content ends before closing ~~
                int openingEnd = -1;
                for (int i = contentEnd - 1; i >= 1; i--) {
                    if (text.charAt(i) == '~' && text.charAt(i - 1) == '~') {
                        openingEnd = i;
                        break;
                    }
                    if (text.charAt(i) == '\n') break;
                }
                if (openingEnd >= 1) {
                    int openingStart = openingEnd - 1;
                    String content = text.substring(openingEnd + 1, contentEnd);
                    if (!content.trim().isEmpty()) {
                        // Remove closing ~~ (2 chars)
                        editable.delete(typedPosition - 1, typedPosition + 1);
                        // Remove opening ~~ (2 chars)
                        editable.delete(openingStart, openingStart + 2);
                        int spanStart = openingStart;
                        int spanEnd = openingStart + content.length();
                        RichTextSpanManager.applyFormat(editable, spanStart, spanEnd, FormatType.STRIKETHROUGH);
                        binding.inputField.setSelection(spanEnd);
                        detectActiveFormats();
                        return;
                    }
                }
            }
        }

        // Italic: _text_ — need at least _x_ (3 chars)
        if (typedChar == '_' && typedPosition >= 2) {
            // Find the opening _ before the content
            int contentEnd = typedPosition;
            int openingPos = -1;
            for (int i = contentEnd - 1; i >= 0; i--) {
                if (text.charAt(i) == '_') {
                    openingPos = i;
                    break;
                }
                if (text.charAt(i) == '\n') break;
            }
            if (openingPos >= 0 && openingPos < contentEnd) {
                String content = text.substring(openingPos + 1, contentEnd);
                if (!content.trim().isEmpty()) {
                    // Remove closing _
                    editable.delete(contentEnd, contentEnd + 1);
                    // Remove opening _
                    editable.delete(openingPos, openingPos + 1);
                    int spanStart = openingPos;
                    int spanEnd = openingPos + content.length();
                    RichTextSpanManager.applyFormat(editable, spanStart, spanEnd, FormatType.ITALIC);
                    binding.inputField.setSelection(spanEnd);
                    detectActiveFormats();
                }
            }
        }
    }

    /**
     * Handles auto-triggering of underline format when the user types closing </u> tag.
     * <p>
     * Detects the pattern: &lt;u&gt;text&lt;/u&gt;
     * The HTML-like markers are removed and the content is wrapped with the underline span.
     * </p>
     *
     * @param editable      The editable text.
     * @param typedPosition The position where the '>' character was typed.
     */
    private void handleUnderlineAutoTrigger(Editable editable, int typedPosition) {
        if (editable == null || formatSpanWatcher == null) {
            return;
        }

        String text = editable.toString();
        // Check if we just completed </u> — need at least </u> (4 chars) at the end
        // typedPosition is where '>' was typed
        if (typedPosition < 3) return;

        // Check for closing </u>
        String closingTag = "</u>";
        int closingStart = typedPosition - 3; // </u> is 4 chars, '>' is at typedPosition
        if (closingStart < 0) return;
        String possibleClose = text.substring(closingStart, typedPosition + 1);
        if (!possibleClose.equals(closingTag)) return;

        // Now search backwards for opening <u>
        String openingTag = "<u>";
        int searchEnd = closingStart;
        int openingPos = text.lastIndexOf(openingTag, searchEnd - 1);
        if (openingPos < 0) return;

        // Don't cross newlines
        String between = text.substring(openingPos, closingStart);
        if (between.contains("\n")) return;

        // Extract content between <u> and </u>
        int contentStart = openingPos + openingTag.length();
        int contentEnd = closingStart;
        String content = text.substring(contentStart, contentEnd);
        if (content.trim().isEmpty()) return;

        // Remove closing </u> (4 chars)
        editable.delete(closingStart, typedPosition + 1);
        // Remove opening <u> (3 chars)
        editable.delete(openingPos, openingPos + openingTag.length());

        // Apply underline span
        int spanStart = openingPos;
        int spanEnd = openingPos + content.length();
        RichTextSpanManager.applyFormat(editable, spanStart, spanEnd, FormatType.UNDERLINE);
        binding.inputField.setSelection(spanEnd);
        detectActiveFormats();
    }

    /**
     * Handles auto-triggering of link format when the user types closing ) for [text](url) syntax.
     * <p>
     * Detects the pattern: [linkText](url)
     * The markdown markers are removed and the content is wrapped with the link span.
     * </p>
     *
     * @param editable      The editable text.
     * @param typedPosition The position where the ')' character was typed.
     */
    private void handleLinkAutoTrigger(Editable editable, int typedPosition) {
        if (editable == null || formatSpanWatcher == null) {
            return;
        }

        String text = editable.toString();
        // We just typed ')' — look for ](url) pattern and [text] before it
        if (typedPosition < 4) return; // minimum: [x](y) = 6 chars

        // Find the opening ( for the URL
        int urlOpenParen = -1;
        for (int i = typedPosition - 1; i >= 0; i--) {
            if (text.charAt(i) == '(') {
                urlOpenParen = i;
                break;
            }
            if (text.charAt(i) == '\n') return; // don't cross newlines
        }
        if (urlOpenParen < 0) return;

        // Check that ]( is right before the URL
        if (urlOpenParen < 1 || text.charAt(urlOpenParen - 1) != ']') return;

        int closeBracket = urlOpenParen - 1;

        // Find the opening [
        int openBracket = -1;
        for (int i = closeBracket - 1; i >= 0; i--) {
            if (text.charAt(i) == '[') {
                openBracket = i;
                break;
            }
            if (text.charAt(i) == '\n') return;
        }
        if (openBracket < 0) return;

        // Extract link text and URL
        String linkText = text.substring(openBracket + 1, closeBracket);
        String url = text.substring(urlOpenParen + 1, typedPosition);
        if (linkText.trim().isEmpty() || url.trim().isEmpty()) return;

        // Remove the entire [text](url) and replace with just the link text
        editable.delete(openBracket, typedPosition + 1);
        editable.insert(openBracket, linkText);

        // Apply link span
        int spanStart = openBracket;
        int spanEnd = openBracket + linkText.length();
        LinkFormatSpan linkSpan = new LinkFormatSpan(url, getContext());
        editable.setSpan(linkSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.inputField.setSelection(spanEnd);
        detectActiveFormats();
    }

    /**
     * Handles link format application by showing a dialog for URL input.
     * <p>
     * When the Link button is clicked:
     * <ul>
     *   <li>If text is selected, shows a dialog to enter the URL (text field pre-filled)</li>
     *   <li>If no text is selected, shows a dialog to enter both text and URL</li>
     *   <li>If link already exists at selection, removes it (toggle off)</li>
     * </ul>
     * </p>
     * <p>
     * Validates: Requirements 5.1, 5.3, 5.4
     * </p>
     *
     * @param editable       The editable text.
     * @param selectionStart The start of the selection.
     * @param selectionEnd   The end of the selection.
     */
    private void handleLinkFormat(Editable editable, int selectionStart, int selectionEnd) {
        // Check if link already exists at selection - if so, show Edit Link dialog
        if (selectionStart != selectionEnd && 
            RichTextSpanManager.hasFormatInRange(editable, selectionStart, selectionEnd, FormatType.LINK)) {
            // Extract existing link text and URL
            String selectedText = editable.subSequence(selectionStart, selectionEnd).toString();
            String existingUrl = RichTextSpanManager.getLinkUrl(editable, selectionStart, selectionEnd);
            
            String editLinkTitle = getContext().getString(R.string.cometchat_edit_link);
            Utils.showAddLinkDialog(getContext(), selectedText, existingUrl, editLinkTitle, (newDisplayText, newUrl) -> {
                // Remove the existing link span
                RichTextSpanManager.removeFormat(editable, selectionStart, selectionEnd, FormatType.LINK);
                
                if (!newDisplayText.equals(selectedText)) {
                    // Replace the text with the new display text
                    editable.replace(selectionStart, selectionEnd, newDisplayText);
                    int newEnd = selectionStart + newDisplayText.length();
                    RichTextSpanManager.applyLinkFormat(editable, selectionStart, newEnd, newUrl);
                } else {
                    // Apply link format with the new URL to the same text
                    RichTextSpanManager.applyLinkFormat(editable, selectionStart, selectionEnd, newUrl);
                }
                detectActiveFormats();
            });
            return;
        }

        // Show dialog for URL input (works with or without text selection)
        showLinkDialog(editable, selectionStart, selectionEnd);
    }

    /**
     * Shows a styled dialog for entering a URL to create a link.
     * <p>
     * Uses {@link Utils#showAddLinkDialog} to display a styled dialog with:
     * <ul>
     *   <li>A header with "Add Link" title and close (X) button</li>
     *   <li>A "Text" input field pre-filled with selected text (empty if no selection)</li>
     *   <li>A "Link" input field for URL entry</li>
     *   <li>Cancel (outlined) and Save (filled primary) buttons</li>
     * </ul>
     * On Save:
     * <ul>
     *   <li>If text was selected: applies link format to the selection (or replaces if text changed)</li>
     *   <li>If no text was selected: inserts the new link text at cursor position</li>
     * </ul>
     * </p>
     * <p>
     * Validates: Requirements 5a.1, 5a.6
     * </p>
     *
     * @param editable       The editable text.
     * @param selectionStart The start of the selection (or cursor position if no selection).
     * @param selectionEnd   The end of the selection (same as start if no selection).
     */
    private void showLinkDialog(Editable editable, int selectionStart, int selectionEnd) {
        Context context = getContext();
        
        // Get the selected text to pre-fill the Text field (empty if no selection)
        String selectedText = selectionStart != selectionEnd 
                ? editable.subSequence(selectionStart, selectionEnd).toString() 
                : "";
        
        // Show the styled Add Link dialog
        Utils.showAddLinkDialog(context, selectedText, null, (displayText, url) -> {
            if (selectionStart != selectionEnd) {
                // Text was selected - replace or format it
                if (!displayText.equals(selectedText)) {
                    // Replace the selected text with the new display text
                    editable.replace(selectionStart, selectionEnd, displayText);
                    // Apply link format to the new text
                    int newEnd = selectionStart + displayText.length();
                    RichTextSpanManager.applyLinkFormat(editable, selectionStart, newEnd, url);
                } else {
                    // Apply link format to the original selection
                    RichTextSpanManager.applyLinkFormat(editable, selectionStart, selectionEnd, url);
                }
            } else {
                // No text was selected - insert the new link text at cursor position
                editable.insert(selectionStart, displayText);
                int newEnd = selectionStart + displayText.length();
                RichTextSpanManager.applyLinkFormat(editable, selectionStart, newEnd, url);
            }
            detectActiveFormats();
        });
    }

    /**
     * Shows the Edit Link dialog when a user clicks on an existing link.
     * <p>
     * Uses {@link Utils#showEditLinkDialog} to display a styled dialog with:
     * <ul>
     *   <li>"Link" title</li>
     *   <li>Clickable URL text that opens in browser</li>
     *   <li>Edit (outlined) and Remove (filled red) buttons</li>
     * </ul>
     * </p>
     * <p>
     * When Edit is clicked, shows the Add Link dialog pre-filled with current text and URL.
     * When Remove is clicked, removes the link formatting but keeps the text.
     * </p>
     * <p>
     * Validates: Requirements 5b.1, 5b.5, 5b.6
     * </p>
     *
     * @param editable  The editable text.
     * @param spanStart The start position of the link span.
     * @param spanEnd   The end position of the link span.
     * @param linkText  The current display text of the link.
     * @param linkUrl   The current URL of the link.
     */
    private void showEditLinkDialog(Editable editable, int spanStart, int spanEnd, 
                                     String linkText, String linkUrl) {
        Context context = getContext();
        
        Utils.showEditLinkDialog(context, linkText, linkUrl, new Utils.OnLinkEditListener() {
            @Override
            public void onEditClicked(String currentText, String currentUrl) {
                // Show Add Link dialog pre-filled with current text and URL, with "Edit Link" title
                String editLinkTitle = context.getString(R.string.cometchat_edit_link);
                Utils.showAddLinkDialog(context, currentText, currentUrl, editLinkTitle, (newDisplayText, newUrl) -> {
                    // First, remove the existing link span
                    RichTextSpanManager.removeFormat(editable, spanStart, spanEnd, FormatType.LINK);
                    
                    // Check if the display text has changed
                    if (!newDisplayText.equals(linkText)) {
                        // Replace the text with the new display text
                        editable.replace(spanStart, spanEnd, newDisplayText);
                        // Apply link format to the new text
                        int newEnd = spanStart + newDisplayText.length();
                        RichTextSpanManager.applyLinkFormat(editable, spanStart, newEnd, newUrl);
                    } else {
                        // Apply link format with the new URL to the same text
                        RichTextSpanManager.applyLinkFormat(editable, spanStart, spanEnd, newUrl);
                    }
                    detectActiveFormats();
                });
            }

            @Override
            public void onRemoveClicked() {
                // Remove the link formatting but keep the text
                RichTextSpanManager.removeFormat(editable, spanStart, spanEnd, FormatType.LINK);
                detectActiveFormats();
            }
        });
    }

    /**
     * Sets up text change listener.
     * Following the same pattern as CometChatMessageComposer.
     * <p>
     * For WYSIWYG editing, this listener:
     * - Detects active formats for toolbar sync
     * - Integrates FormatSpanWatcher for span management (extension/shrinking)
     * - Does NOT apply markdown-based visual formatting (spans handle visual display)
     * - Clears pending formats when cursor moves (via selection change listener)
     * </p>
     */
    private void setupTextChangeListener() {
        // Initialize FormatSpanWatcher for span management with context for theme-aware spans
        formatSpanWatcher = new FormatSpanWatcher(getContext());
        
        // Initialize ListContinuationHandler for list Enter key handling
        listContinuationHandler = new ListContinuationHandler();
        
        // Track last cursor position to detect cursor movement
        final int[] lastCursorPosition = {-1};
        
        // Set up CometChatTextWatcher for selection change handling
        binding.inputField.setTextWatcher(new CometChatTextWatcher() {
            private String deletedSubString = "";
            private int changeStart = 0;
            private int changeBefore = 0;
            private int changeCount = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count > after) {
                    deletedSubString = s.subSequence(start, start + count).toString();
                }
                changeStart = start;
                changeBefore = count;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String text = charSequence.toString();
                if (viewModel != null) {
                    viewModel.setText(text);
                }
                if (onTextChange != null) {
                    onTextChange.onTextChange(text);
                }
                
                // Handle typing indicator
                if (!disableTypingEvents) {
                    // Typing indicator is handled by ViewModel
                }
                
                // Process selected suggestion list
                sendSelectedSuggestionList();
                
                // Store change info for FormatSpanWatcher
                changeCount = count;
                
                // Handle Enter key for list continuation
                // Check if a newline was inserted (count == 1 and the inserted char is '\n')
                if (enableRichTextFormatting && count == 1 && before == 0 && start < text.length()) {
                    char insertedChar = text.charAt(start);
                    if (insertedChar == '\n') {
                        // Newline was inserted - handle list continuation
                        handleListEnterKey((Editable) charSequence, start);
                    }
                    // Handle backtick auto-trigger for code formatting
                    else if (insertedChar == '`') {
                        handleBacktickAutoTrigger((Editable) charSequence, start);
                    }
                    // Handle space after block markers: "- ", "1. ", "> "
                    else if (insertedChar == ' ') {
                        handleListSyntaxAutoTrigger((Editable) charSequence, start);
                    }
                    // Handle closing markers for inline formats: **bold**, _italic_, ~~strike~~
                    else if (insertedChar == '*' || insertedChar == '_' || insertedChar == '~') {
                        handleInlineFormatAutoTrigger((Editable) charSequence, start);
                    }
                    // Handle closing > for underline <u>text</u>
                    else if (insertedChar == '>') {
                        handleUnderlineAutoTrigger((Editable) charSequence, start);
                    }
                    // Handle closing ) for link [text](url)
                    else if (insertedChar == ')') {
                        handleLinkAutoTrigger((Editable) charSequence, start);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                int selStart = binding.inputField.getSelectionStart();
                
                // Update last cursor position
                lastCursorPosition[0] = selStart;
                
                // Update send button state based on whether we're in edit mode or not
                handleSendButtonState(text);
                
                // Only detect active formats if rich text formatting is enabled
                // Note: We no longer apply markdown-based visual formatting
                // Spans handle visual display directly in WYSIWYG mode
                if (enableRichTextFormatting) {
                    // Let FormatSpanWatcher handle span extension/shrinking FIRST
                    // This applies pending formats to newly typed text
                    if (formatSpanWatcher != null) {
                        formatSpanWatcher.handleTextChanged(s, changeStart, changeBefore, changeCount);
                    }
                    
                    // Then detect active formats to update toolbar
                    detectActiveFormats();
                }
                
                // Handle mention detection - following CometChatMessageComposer pattern
                if (!disableMentions && !deletedSubString.isEmpty()) {
                    char lastDeletedChar = deletedSubString.charAt(deletedSubString.length() - 1);
                    if (tempTextFormatter != null && lastDeletedChar == tempTextFormatter.getTrackingCharacter()) {
                        setSuggestionListVisibility(View.GONE);
                    }
                }
                deletedSubString = "";
                
                // Skip all mention detection when suppressed (during programmatic insertion)
                if (suppressMentionDetection) {
                    setSuggestionListVisibility(View.GONE);
                    return;
                }
                
                // Process text formatters for mentions
                // Scan backwards from cursor (matching CometChatMessageComposer pattern).
                // This ensures only the nearest '@' before the cursor is considered,
                // so previously completed plain-text mentions don't re-trigger.
                // Also skip mention detection entirely when cursor is inside a code
                // block or inline code — mentions are not allowed in code formatting.
                if (!disableMentions && tempTextFormatter == null) {
                    boolean cursorInsideCode = false;
                    if (enableRichTextFormatting) {
                        cursorInsideCode = RichTextSpanManager.hasFormatInRange(
                                s, selStart, selStart, FormatType.CODE_BLOCK)
                                || RichTextSpanManager.hasFormatInRange(
                                s, selStart, selStart, FormatType.INLINE_CODE);
                    }
                    if (cursorInsideCode) {
                        tempTextFormatter = null;
                    } else {
                        Spannable spannable = s;
                        for (int i = selStart - 1; i >= 0; i--) {
                            char c = text.charAt(i);
                            NonEditableSpan[] spans = spannable.getSpans(i, i, NonEditableSpan.class);
                            if (spans.length > 0) {
                                tempTextFormatter = null;
                                break;
                            }
                            // Skip characters inside a consumed plain-text mention
                            ConsumedMentionSpan[] consumedSpans = spannable.getSpans(i, i, ConsumedMentionSpan.class);
                            if (consumedSpans.length > 0) {
                                tempTextFormatter = null;
                                break;
                            }
                            if (cometchatTextFormatterHashMap.containsKey(c)) {
                                if (i == 0 || text.charAt(i - 1) == ' ' || text.charAt(i - 1) == '\n' || text.charAt(i - 1) == '\u200B') {
                                    if (i < selStart - 1 && (text.charAt(i + 1) == ' ' || text.charAt(i + 1) == '\n')) {
                                        // Tracking char is followed by whitespace — already closed
                                        tempTextFormatter = null;
                                    } else {
                                        tempTextFormatter = cometchatTextFormatterHashMap.get(c);
                                        lastTextFormatterOpenedIndex = i;
                                    }
                                    break;
                                } else {
                                    tempTextFormatter = null;
                                }
                            } else {
                                if (i < lastTextFormatterOpenedIndex) {
                                    tempTextFormatter = null;
                                }
                            }
                        }
                    }
                }
                
                // Handle suggestion list visibility based on text formatter
                if (tempTextFormatter != null) {
                    char c = '\0';
                    char c1 = '\0';
                    if (selStart > 0 && selStart <= text.length()) {
                        c = text.charAt(selStart - 1);
                    }
                    if (selStart > 1 && selStart <= text.length()) {
                        c1 = text.charAt(selStart - 2);
                    }
                    if (selStart > 0) {
                        if (c == tempTextFormatter.getTrackingCharacter()) {
                            if (selStart == 1 || Character.isWhitespace(c1) || c1 == '\n' || c1 == '\u200B') {
                                setSuggestionListVisibility(View.VISIBLE);
                            }
                        } else if (c == ' ') {
                            int lastIndex = selStart - 2;
                            while (lastIndex >= 0 && text.charAt(lastIndex) != ' ' && text.charAt(lastIndex) != '\n') {
                                lastIndex--;
                            }
                            if (lastIndex >= 0 && text.charAt(lastIndex) == tempTextFormatter.getTrackingCharacter()) {
                                setSuggestionListVisibility(View.GONE);
                            }
                        }
                    } else if (selStart == 0 && text.length() > 1 && text.charAt(1) == tempTextFormatter.getTrackingCharacter()) {
                        setSuggestionListVisibility(View.VISIBLE);
                    } else {
                        setSuggestionListVisibility(View.GONE);
                    }
                    sendSearchQueryWithInterval(text, selStart, UIKitUtilityConstants.COMPOSER_SEARCH_QUERY_INTERVAL);
                } else {
                    setSuggestionListVisibility(View.GONE);
                }
            }
            
            @Override
            public void onSelectionChanged(int selStart, int selEnd) {
                // When cursor moves (not due to typing), clear pending formats
                // and sync with actual formats at the new cursor position
                if (enableRichTextFormatting && formatSpanWatcher != null) {
                    Editable editable = binding.inputField.getText();
                    
                    // Check if cursor is at the start of a new line following a formatted item
                    // If so, preserve the format in pending formats for continuation
                    Set<FormatType> formatsToPreserve = EnumSet.noneOf(FormatType.class);
                    if (editable != null && selStart > 0 && editable.charAt(selStart - 1) == '\n') {
                        // Cursor is right after a newline - check if previous line had a format to continue
                        int prevLineEnd = selStart - 1;
                        int prevLineStart = prevLineEnd;
                        while (prevLineStart > 0 && editable.charAt(prevLineStart - 1) != '\n') {
                            prevLineStart--;
                        }
                        
                        // Check for list spans on the previous line
                        if (prevLineStart < prevLineEnd) {
                            NumberedListFormatSpan[] numberedSpans = editable.getSpans(
                                    prevLineStart, prevLineEnd, NumberedListFormatSpan.class);
                            if (numberedSpans != null && numberedSpans.length > 0) {
                                formatsToPreserve.add(FormatType.ORDERED_LIST);
                            }
                            
                            BulletListFormatSpan[] bulletSpans = editable.getSpans(
                                    prevLineStart, prevLineEnd, BulletListFormatSpan.class);
                            if (bulletSpans != null && bulletSpans.length > 0) {
                                formatsToPreserve.add(FormatType.BULLET_LIST);
                            }
                            
                            // Check for code block spans on the previous line
                            CodeBlockFormatSpan[] codeBlockSpans = editable.getSpans(
                                    prevLineStart, prevLineEnd, CodeBlockFormatSpan.class);
                            if (codeBlockSpans != null && codeBlockSpans.length > 0) {
                                formatsToPreserve.add(FormatType.CODE_BLOCK);
                            }
                            
                            // Check for blockquote spans on the previous line
                            BlockquoteFormatSpan[] blockquoteSpans = editable.getSpans(
                                    prevLineStart, prevLineEnd, BlockquoteFormatSpan.class);
                            if (blockquoteSpans != null && blockquoteSpans.length > 0) {
                                formatsToPreserve.add(FormatType.BLOCKQUOTE);
                            }
                            
                            // Check for inline format spans that should continue on new line
                            // (BOLD, ITALIC, UNDERLINE, STRIKETHROUGH)
                            RichTextFormatSpan[] inlineSpans = editable.getSpans(
                                    prevLineStart, prevLineEnd, RichTextFormatSpan.class);
                            if (inlineSpans != null) {
                                for (RichTextFormatSpan span : inlineSpans) {
                                    FormatType formatType = span.getFormatType();
                                    // Check if this span ends at or near the newline position
                                    // (meaning it was active at end of line)
                                    int spanEnd = editable.getSpanEnd(span);
                                    // The span should end at prevLineEnd (the newline position)
                                    // or at prevLineEnd + 1 (if the span includes the newline)
                                    if ((spanEnd == prevLineEnd || spanEnd == prevLineEnd + 1) 
                                            && shouldContinueInlineFormatOnNewLine(formatType)) {
                                        formatsToPreserve.add(formatType);
                                    }
                                }
                            }
                        }
                    }
                    
                    // INLINE_CODE should persist until user explicitly turns it off
                    // Save current state before clearing
                    boolean wasInlineCodePending = formatSpanWatcher.isPendingFormat(FormatType.INLINE_CODE);
                    boolean wasInlineCodeDisabled = formatSpanWatcher.isExplicitlyDisabled(FormatType.INLINE_CODE);
                    
                    // Also save inline format states that should continue on new line
                    // This is a fallback in case span detection didn't find them
                    boolean wasBoldPending = formatSpanWatcher.isPendingFormat(FormatType.BOLD);
                    boolean wasItalicPending = formatSpanWatcher.isPendingFormat(FormatType.ITALIC);
                    boolean wasUnderlinePending = formatSpanWatcher.isPendingFormat(FormatType.UNDERLINE);
                    boolean wasStrikethroughPending = formatSpanWatcher.isPendingFormat(FormatType.STRIKETHROUGH);
                    
                    // Clear pending formats when cursor moves
                    formatSpanWatcher.clearPendingFormats();
                    
                    // Re-add formats that should be preserved for continuation
                    for (FormatType format : formatsToPreserve) {
                        formatSpanWatcher.enableFormat(format);
                    }
                    
                    // Restore INLINE_CODE state - it persists until user clicks the button
                    if (wasInlineCodePending) {
                        formatSpanWatcher.enableFormat(FormatType.INLINE_CODE);
                    } else if (wasInlineCodeDisabled) {
                        formatSpanWatcher.disableFormat(FormatType.INLINE_CODE);
                    }
                    
                    // Restore pending text style formats when cursor is right after an emoji.
                    // Emojis don't get formatting spans, so pending formats should persist
                    // to allow the user to continue typing formatted text after an emoji.
                    if (editable != null && selStart > 0 && isCharBeforeCursorEmoji(editable, selStart)) {
                        if (wasBoldPending) {
                            formatSpanWatcher.enableFormat(FormatType.BOLD);
                        }
                        if (wasItalicPending) {
                            formatSpanWatcher.enableFormat(FormatType.ITALIC);
                        }
                        if (wasUnderlinePending) {
                            formatSpanWatcher.enableFormat(FormatType.UNDERLINE);
                        }
                        if (wasStrikethroughPending) {
                            formatSpanWatcher.enableFormat(FormatType.STRIKETHROUGH);
                        }
                    }
                    
                    // Also check if cursor is right after an emoji that sits at the end of
                    // a text style span. This handles the case where the user typed formatted
                    // text, then inserted an emoji — the span doesn't cover the emoji but
                    // the format should continue for subsequent typing.
                    if (editable != null && selStart > 0) {
                        // Find the start of the emoji sequence before cursor
                        int emojiStart = selStart;
                        String textStr = editable.toString();
                        while (emojiStart > 0) {
                            int cp = Character.codePointBefore(textStr, emojiStart);
                            if (RichTextSpanManager.isEmojiCodePoint(cp) || 
                                RichTextSpanManager.isEmojiModifierOrJoiner(cp)) {
                                emojiStart -= Character.charCount(cp);
                            } else {
                                break;
                            }
                        }
                        if (emojiStart < selStart && emojiStart >= 0) {
                            // There's an emoji sequence before cursor — check for spans ending at emojiStart
                            RichTextFormatSpan[] spansBeforeEmoji = editable.getSpans(
                                    0, emojiStart, RichTextFormatSpan.class);
                            for (RichTextFormatSpan span : spansBeforeEmoji) {
                                int spanEnd = editable.getSpanEnd(span);
                                FormatType ft = span.getFormatType();
                                if (spanEnd == emojiStart && RichTextSpanManager.isTextStyleFormat(ft)) {
                                    formatSpanWatcher.enableFormat(ft);
                                }
                            }
                        }
                    }
                    
                    // Restore inline format states if cursor is at start of new line
                    // This is a fallback in case span detection didn't find them
                    // (e.g., if the span was split but not yet detected)
                    if (editable != null && selStart > 0 && editable.charAt(selStart - 1) == '\n') {
                        if (wasBoldPending && !formatsToPreserve.contains(FormatType.BOLD)) {
                            formatSpanWatcher.enableFormat(FormatType.BOLD);
                        }
                        if (wasItalicPending && !formatsToPreserve.contains(FormatType.ITALIC)) {
                            formatSpanWatcher.enableFormat(FormatType.ITALIC);
                        }
                        if (wasUnderlinePending && !formatsToPreserve.contains(FormatType.UNDERLINE)) {
                            formatSpanWatcher.enableFormat(FormatType.UNDERLINE);
                        }
                        if (wasStrikethroughPending && !formatsToPreserve.contains(FormatType.STRIKETHROUGH)) {
                            formatSpanWatcher.enableFormat(FormatType.STRIKETHROUGH);
                        }
                    }
                    
                    // Update toolbar to reflect formats at new cursor position
                    detectActiveFormats();
                }
            }
        });
    }

    /**
     * Sets the visibility of the suggestion list.
     *
     * @param visibility The visibility (View.VISIBLE or View.GONE).
     */
    public void setSuggestionListVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            visibleSuggestionList();
        } else {
            hideSuggestionList();
        }
    }

    /**
     * Shows the suggestion list with shimmer loading.
     */
    private void visibleSuggestionList() {
        binding.suggestionList.setVisibility(View.VISIBLE);
        binding.suggestionList.showShimmer(true);
    }

    /**
     * Hides the suggestion list and clears it.
     */
    private void hideSuggestionList() {
        binding.suggestionList.setVisibility(View.GONE);
        binding.suggestionList.setList(new ArrayList<>());
        if (tempTextFormatter != null) {
            tempTextFormatter.search(getContext(), null);
        }
        tempTextFormatter = null;
        lastTextFormatterOpenedIndex = -1;
    }

    /**
     * Sends search query with interval to formatter.
     */
    private void sendSearchQueryWithInterval(String text, int cursorPosition, int interval) {
        if (queryTimer != null) {
            queryTimer.cancel();
        }
        queryTimer = new Timer();
        queryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (tempTextFormatter != null) {
                    tempTextFormatter.search(getContext(), getQueryString(text, cursorPosition, tempTextFormatter.getTrackingCharacter()));
                }
            }
        }, interval);
    }

    /**
     * Gets the query string for mention search.
     */
    private String getQueryString(String text, int cursorPosition, char trackingCharacter) {
        if (text == null || cursorPosition <= 0) {
            return null;
        }
        
        int startIndex = -1;
        for (int i = cursorPosition - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == trackingCharacter) {
                startIndex = i;
                break;
            } else if (c == ' ' || c == '\n') {
                break;
            }
        }
        
        if (startIndex >= 0 && startIndex < cursorPosition - 1) {
            return text.substring(startIndex + 1, cursorPosition);
        }
        return "";
    }

    /**
     * Sends the selected suggestion list to formatters.
     */
    private void sendSelectedSuggestionList() {
        if (operationTimer != null) {
            operationTimer.cancel();
        }
        operationTimer = new Timer();
        operationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                processTextToSetUniqueSuggestions();
            }
        }, UIKitUtilityConstants.COMPOSER_OPERATION_INTERVAL);
    }

    /**
     * Processes text to set unique suggestions.
     */
    private void processTextToSetUniqueSuggestions() {
        Editable editable = binding.inputField.getText();
        if (editable != null) {
            selectedSuggestionItemHashMap.clear();
            NonEditableSpan[] spans = editable.getSpans(0, editable.length(), NonEditableSpan.class);
            for (NonEditableSpan span : spans) {
                if (span != null) {
                    SuggestionItem suggestionItem = span.getSuggestionItem();
                    if (suggestionItem != null) {
                        if (selectedSuggestionItemHashMap.containsKey(span.getId())) {
                            HashMap<String, SuggestionItem> suggestionItemHashMap = selectedSuggestionItemHashMap.get(span.getId());
                            if (suggestionItemHashMap != null) {
                                suggestionItemHashMap.put(suggestionItem.getId(), suggestionItem);
                            }
                        } else {
                            HashMap<String, SuggestionItem> suggestionItemHashMap = new HashMap<>();
                            suggestionItemHashMap.put(suggestionItem.getId(), suggestionItem);
                            selectedSuggestionItemHashMap.put(span.getId(), suggestionItemHashMap);
                        }
                    }
                }
            }

            Set<Character> keys = cometchatTextFormatterHashMap.keySet();
            for (Character key : keys) {
                sendSelectedListToFormatter(key);
            }
        }
    }

    /**
     * Sends the selected list to the formatter.
     */
    private void sendSelectedListToFormatter(char formatterId) {
        CometChatTextFormatter formatter = cometchatTextFormatterHashMap.get(formatterId);
        if (formatter != null) {
            List<SuggestionItem> suggestionItems = getSelectedSuggestionItems(formatterId);
            formatter.setSelectedList(getContext(), suggestionItems);
        }
    }

    /**
     * Gets the selected suggestion items for a formatter.
     */
    private List<SuggestionItem> getSelectedSuggestionItems(char formatterId) {
        if (selectedSuggestionItemHashMap.containsKey(formatterId)) {
            HashMap<String, SuggestionItem> suggestionItemHashMap = selectedSuggestionItemHashMap.get(formatterId);
            if (suggestionItemHashMap != null) {
                return new ArrayList<>(suggestionItemHashMap.values());
            }
        }
        return new ArrayList<>();
    }

    /**
     * Sets the tag list (suggestion items) from formatter.
     */
    private void setTagList(@Nullable List<SuggestionItem> suggestionItems) {
        if (tempTextFormatter != null && suggestionItems != null && !suggestionItems.isEmpty()) {
            binding.suggestionList.setList(new ArrayList<>());
            binding.suggestionList.setVisibility(View.VISIBLE);
            binding.suggestionList.setList(suggestionItems);
        } else {
            binding.suggestionList.setVisibility(View.GONE);
        }
    }

    /**
     * Sets the loading state visibility for suggestion list.
     */
    private void setLoadingStateVisibility(Boolean aBoolean) {
        binding.suggestionList.showShimmer(aBoolean);
    }

    /**
     * Applies style attributes from XML.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.CometChatSingleLineComposer, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(
                R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerStyle, 0);
        directAttributes.recycle();
        
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.CometChatSingleLineComposer, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Extracts attributes and applies defaults.
     * Colors and tints use CometChatTheme defaults, dimensions use 0 as default.
     *
     * @param typedArray The TypedArray containing the attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) {
            applyDefault();
            return;
        }
        try {
            // Container styles
            setComposerBackgroundColor(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerBackgroundColor, 
                    CometChatTheme.getBackgroundColor3(getContext())));
            setComposerStrokeColor(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerStrokeColor, 0));
            setComposerStrokeWidth(typedArray.getDimensionPixelSize(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerStrokeWidth, 0));
            setComposerCornerRadius(typedArray.getDimensionPixelSize(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerCornerRadius, 0));
            setComposerBackgroundDrawable(typedArray.getDrawable(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerBackgroundDrawable));

            // Compose box styles
            setComposeBoxBackgroundColor(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerComposeBoxBackgroundColor,
                    CometChatTheme.getBackgroundColor1(getContext())));
            setComposeBoxStrokeColor(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerComposeBoxStrokeColor,
                    CometChatTheme.getStrokeColorDefault(getContext())));
            setComposeBoxStrokeWidth(typedArray.getDimensionPixelSize(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerComposeBoxStrokeWidth, 0));
            setComposeBoxCornerRadius(typedArray.getDimensionPixelSize(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerComposeBoxCornerRadius, 0));
            setComposeBoxBackgroundDrawable(typedArray.getDrawable(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerComposeBoxBackgroundDrawable));

            // Input field styles
            setInputTextColor(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerInputTextColor,
                    CometChatTheme.getTextColorPrimary(getContext())));
            setInputHintColor(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerInputHintColor,
                    CometChatTheme.getTextColorTertiary(getContext())));
            setInputTextAppearance(typedArray.getResourceId(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerInputTextAppearance,
                    CometChatTheme.getTextAppearanceBodyRegular(getContext())));
            setInputBackgroundColor(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerInputBackgroundColor, 0));

            // Send button styles
            setActiveSendButtonDrawable(typedArray.getDrawable(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerActiveSendButtonDrawable));
            setInactiveSendButtonDrawable(typedArray.getDrawable(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerInactiveSendButtonDrawable));
            setSendButtonBackgroundColor(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerSendButtonBackgroundColor,
                    CometChatTheme.getBackgroundColor1(getContext())));

            // Attachment button styles
            setAttachmentIcon(typedArray.getDrawable(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerAttachmentIcon));
            setAttachmentIconTint(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerAttachmentIconTint,
                    CometChatTheme.getIconTintSecondary(getContext())));

            // Voice recording button styles
            setVoiceRecordingIcon(typedArray.getDrawable(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerVoiceRecordingIcon));
            setVoiceRecordingIconTint(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerVoiceRecordingIconTint,
                    CometChatTheme.getIconTintSecondary(getContext())));

            // AI button styles
            setAIIcon(typedArray.getDrawable(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerAIIcon));
            setAIIconTint(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerAIIconTint,
                    CometChatTheme.getIconTintSecondary(getContext())));

            // Sticker button styles
            setStickerIcon(typedArray.getDrawable(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerStickerIcon));
            setStickerIconTint(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerStickerIconTint,
                    CometChatTheme.getIconTintSecondary(getContext())));

            // Mention banner styles
            setMentionBannerBackgroundColor(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerMentionBannerBackgroundColor,
                    CometChatTheme.getBackgroundColor3(getContext())));
            setMentionBannerTextColor(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerMentionBannerTextColor,
                    CometChatTheme.getErrorColor(getContext())));
            setMentionBannerTextAppearance(typedArray.getResourceId(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerMentionBannerTextAppearance, 0));
            setMentionBannerIcon(typedArray.getDrawable(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerMentionBannerIcon));
            setMentionBannerIconTint(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerMentionBannerIconTint,
                    CometChatTheme.getErrorColor(getContext())));
            setMentionBannerCloseIcon(typedArray.getDrawable(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerMentionBannerCloseIcon));
            setMentionBannerCloseIconTint(typedArray.getColor(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerMentionBannerCloseIconTint,
                    CometChatTheme.getIconTintPrimary(getContext())));

            // Component style references
            setRichTextToolbarStyle(typedArray.getResourceId(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerRichTextToolbarStyle, 0));
            setSuggestionListStyle(typedArray.getResourceId(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerSuggestionListStyle, 0));
            setAttachmentOptionSheetStyle(typedArray.getResourceId(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerAttachmentOptionSheetStyle, 0));
            setMessagePreviewStyle(typedArray.getResourceId(
                    R.styleable.CometChatSingleLineComposer_cometchatSingleLineComposerMessagePreviewStyle, 0));

            applyDefault();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies default values to the view.
     */
    private void applyDefault() {
        // Apply container styles
        if (backgroundDrawable != null) {
            setBackground(backgroundDrawable);
        } else {
            setCardBackgroundColor(backgroundColor);
        }
        setStrokeColor(strokeColor);
        if (strokeWidth > 0) {
            setStrokeWidth((int) strokeWidth);
        }
        if (cornerRadius > 0) {
            setRadius(cornerRadius);
        }

        // Apply compose box styles
        if (composeBoxBackgroundDrawable != null) {
            binding.composeBoxCard.setBackground(composeBoxBackgroundDrawable);
        } else {
            binding.composeBoxCard.setCardBackgroundColor(composeBoxBackgroundColor);
        }
        binding.composeBoxCard.setStrokeColor(composeBoxStrokeColor);
        if (composeBoxStrokeWidth > 0) {
            binding.composeBoxCard.setStrokeWidth((int) composeBoxStrokeWidth);
        }
        if (composeBoxCornerRadius > 0) {
            binding.composeBoxCard.setRadius(composeBoxCornerRadius);
        }

        // Apply input field styles
        binding.inputField.setTextColor(inputTextColor);
        binding.inputField.setHintTextColor(inputHintColor);
        if (inputTextAppearance != 0) {
            binding.inputField.setTextAppearance(inputTextAppearance);
        }
        if (inputBackgroundColor != 0) {
            binding.inputField.setBackgroundColor(inputBackgroundColor);
        }

        // Apply send button styles
        binding.sendButtonCard.setCardBackgroundColor(sendButtonBackgroundColor);
        updateSendButtonState(false);

        // Apply button icon tints
        binding.btnAttachment.setColorFilter(attachmentIconTint);
        binding.btnVoiceRecording.setColorFilter(voiceRecordingIconTint);
        binding.btnAi.setColorFilter(aiIconTint);
        binding.btnSticker.setColorFilter(stickerIconTint);

        // Apply button icons if set
        if (attachmentIcon != null) {
            binding.btnAttachment.setImageDrawable(attachmentIcon);
        }
        if (voiceRecordingIcon != null) {
            binding.btnVoiceRecording.setImageDrawable(voiceRecordingIcon);
        }
        if (aiIcon != null) {
            binding.btnAi.setImageDrawable(aiIcon);
        }
        if (stickerIcon != null) {
            binding.btnSticker.setImageDrawable(stickerIcon);
        }

        // Apply message preview default styles (if no style resource was set)
        if (messagePreviewStyle == 0) {
            binding.messagePreview.setBackgroundColor(CometChatTheme.getBackgroundColor3(getContext()));
            binding.messagePreview.setTitleTextColor(CometChatTheme.getTextColorHighlight(getContext()));
            binding.messagePreview.setSubtitleTextColor(CometChatTheme.getTextColorSecondary(getContext()));
            binding.messagePreview.setCloseIconTint(CometChatTheme.getIconTintPrimary(getContext()));
            binding.messagePreview.setSeparatorColor(CometChatTheme.getStrokeColorHighlight(getContext()));
        }

        // Apply mention banner styles
        binding.mentionLimitBanner.setCardBackgroundColor(mentionBannerBackgroundColor);
        binding.mentionInfoText.setTextColor(mentionBannerTextColor);
        if (mentionBannerTextAppearance != 0) {
            binding.mentionInfoText.setTextAppearance(mentionBannerTextAppearance);
        }
        if (mentionBannerIcon != null) {
            binding.mentionInfoIcon.setImageDrawable(mentionBannerIcon);
        }
        binding.mentionInfoIcon.setColorFilter(mentionBannerIconTint);
        if (mentionBannerCloseIcon != null) {
            binding.mentionBannerClose.setImageDrawable(mentionBannerCloseIcon);
        }
        binding.mentionBannerClose.setColorFilter(mentionBannerCloseIconTint);

        // Apply rich text toolbar style
        if (richTextToolbarStyle != 0) {
            binding.richTextToolbar.setStyle(richTextToolbarStyle);
        }

        updateButtonVisibilities();
        updateToolbarConfiguration();
    }

    // ==================== Public API Methods ====================

    /**
     * Sets the user for one-on-one messaging.
     *
     * @param user The User object.
     */
    public void setUser(@Nullable User user) {
        this.user = user;
        this.group = null;
        this.isAgentChat = Utils.isAgentChat(user);
        if (viewModel != null) {
            viewModel.setUser(user);
        }
        processMentionsFormatter();
        processFormatters();
        configureAIAssistantComposer();
    }

    /**
     * Sets the group for group messaging.
     *
     * @param group The Group object.
     */
    public void setGroup(@Nullable Group group) {
        this.group = group;
        this.user = null;
        if (viewModel != null) {
            viewModel.setGroup(group);
        }
        processMentionsFormatter();
        processFormatters();
    }

    /**
     * Configures the composer for AI assistant/agent chat mode.
     * Hides attachment, voice recording, AI, and sticker buttons,
     * removes mentions formatter, and sets a custom placeholder.
     */
    private void configureAIAssistantComposer() {
        if (isAgentChat) {
            cometchatTextFormatters.remove(cometchatMentionsFormatter);
            processFormatters();
            binding.btnAttachment.setVisibility(GONE);
            binding.btnVoiceRecording.setVisibility(GONE);
            binding.btnSticker.setVisibility(GONE);
            binding.btnAi.setVisibility(GONE);
            setPlaceholderText("Ask anything...");
            updateSendButtonState(false);
        } else {
            setPlaceholderText(getResources().getString(R.string.cometchat_composer_place_holder_text));
        }
    }

    /**
     * Sets the parent message ID for threaded replies.
     *
     * @param parentMessageId The parent message ID.
     */
    public void setParentMessageId(long parentMessageId) {
        if (viewModel != null) {
            viewModel.setParentMessageId(parentMessageId);
        }
    }

    /**
     * Enables or disables rich text formatting functionality.
     * When enabled (true):
     * - Rich text formatting (bold, italic, etc.) will work
     * - Toolbar visibility is controlled by setRichTextFormattingOptionsVisibility
     * - Text selection menu items are controlled by setShowTextSelectionMenuItems
     * When disabled (false):
     * - No rich text formatting will be applied
     * - Toolbar will be hidden
     * - Text selection menu items will not show formatting options
     *
     * @param enable true to enable rich text formatting, false to disable.
     */
    public void setEnableRichTextFormatting(boolean enable) {
        this.enableRichTextFormatting = enable;
        
        // If disabling, hide the toolbar
        if (!enable) {
            hideRichTextToolbar();
        }
        
        // Update toolbar configuration
        updateToolbarConfiguration();
        
        // Update text selection menu
        setupTextSelectionMenu();
    }

    /**
     * Returns whether rich text formatting is enabled.
     *
     * @return true if rich text formatting is enabled, false otherwise.
     */
    public boolean isEnableRichTextFormatting() {
        return enableRichTextFormatting;
    }

    /**
     * Sets the visibility of the rich text formatting options toolbar.
     * Only takes effect when enableRichTextFormatting is true.
     * When set to View.VISIBLE, showTextSelectionMenuItems is also automatically enabled.
     *
     * @param visibility View.VISIBLE to show the toolbar, View.GONE or View.INVISIBLE to hide.
     */
    public void setRichTextFormattingOptionsVisibility(int visibility) {
        this.richTextFormattingOptionsVisibility = visibility;
        
        // When toolbar is visible, automatically enable text selection menu items
        if (visibility == View.VISIBLE) {
            this.showTextSelectionMenuItems = true;
            setupTextSelectionMenu();
        }
        
        updateToolbarConfiguration();
    }

    /**
     * Returns the visibility of the rich text formatting options toolbar.
     *
     * @return View.VISIBLE, View.GONE, or View.INVISIBLE.
     */
    public int getRichTextFormattingOptionsVisibility() {
        return richTextFormattingOptionsVisibility;
    }

    /**
     * Returns whether the rich text formatting options toolbar is currently visible.
     *
     * @return true if toolbar is visible, false otherwise.
     */
    public boolean isToolbarVisible() {
        return isToolbarVisible;
    }

    /**
     * Sets whether to show formatting options in the text selection menu.
     * When enabled, long-pressing and selecting text will show formatting options
     * (Bold, Italic, Strikethrough, etc.) in the system context menu.
     * Only takes effect when enableRichTextFormatting is true.
     *
     * @param show true to show formatting options in selection menu, false to hide.
     */
    public void setShowTextSelectionMenuItems(boolean show) {
        this.showTextSelectionMenuItems = show;
        setupTextSelectionMenu();
    }

    /**
     * Returns whether formatting options are shown in the text selection menu.
     *
     * @return true if formatting options are shown in selection menu, false otherwise.
     */
    public boolean isShowTextSelectionMenuItems() {
        return showTextSelectionMenuItems;
    }

    /**
     * Sets the placeholder text for the input field.
     *
     * @param text The placeholder text.
     */
    public void setPlaceholderText(@Nullable String text) {
        binding.inputField.setHint(text);
    }

    /**
     * Sets the maximum number of lines for the input field.
     *
     * @param maxLines The maximum number of lines.
     */
    public void setMaxLines(int maxLines) {
        binding.inputField.setMaxLines(maxLines);
    }

    /**
     * Sets the Enter key behavior.
     *
     * @param behavior The EnterKeyBehavior.
     */
    public void setEnterKeyBehavior(@NonNull EnterKeyBehavior behavior) {
        this.enterKeyBehavior = behavior;
    }

    /**
     * Sets the text content.
     *
     * @param text The text to set.
     */
    public void setText(@Nullable String text) {
        binding.inputField.setText(text);
    }

    /**
     * Gets the current text content.
     *
     * @return The text content.
     */
    @NonNull
    public String getText() {
        Editable editable = binding.inputField.getText();
        return editable != null ? editable.toString() : "";
    }

    /**
     * Clears the input field.
     */
    private void clearText() {
        binding.inputField.setText("");
    }

    /**
     * Sets the message to edit.
     * <p>
     * For WYSIWYG editing, this method parses markdown from the message text
     * and displays visual formatting without showing markdown markers.
     * </p>
     *
     * @param message The TextMessage to edit.
     */
    public void setEditMessage(@Nullable TextMessage message) {
        if (viewModel != null) {
            viewModel.setEditMessage(message);
        }
        if (message != null) {
            // Hide the mention limit banner when entering edit mode
            // The banner should only show when trying to add a new mention that exceeds the limit
            hideMentionLimitBanner();
            
            // Suppress info visibility updates during edit mode initialization
            // This prevents the banner from showing when loading existing mentions
            suppressInfoVisibility = true;
            
            // Parse markdown to spannable with visual formatting (WYSIWYG mode)
            // This converts markdown syntax (like links [text](url)) to visual spans
            SpannableString parsedSpannable;
            if (enableRichTextFormatting && message.getText() != null) {
                parsedSpannable = MarkdownConverter.fromMarkdown(message.getText());
            } else {
                parsedSpannable = new SpannableString(message.getText() != null ? message.getText() : "");
            }
            
            // Apply additional text formatters (e.g., mentions)
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(parsedSpannable);
            
            // Replace MarkdownConverter-created code block and inline code spans with
            // composer-appropriate ones. MarkdownConverter uses hardcoded colors and
            // SPAN_EXCLUSIVE_EXCLUSIVE flags (designed for read-only message bubbles).
            // The composer needs theme-aware colors and SPAN_INCLUSIVE_INCLUSIVE flags
            // so spans extend as the user types and render with proper background.
            // Also remove the extra helper spans (LeadingMarginSpan, ForegroundColorSpan,
            // TypefaceSpan) that MarkdownConverter adds for code blocks, since the
            // composer's CodeBlockFormatSpan(context) handles rendering on its own.
            if (enableRichTextFormatting) {
                CodeBlockFormatSpan[] codeBlockSpans = spannableStringBuilder.getSpans(
                        0, spannableStringBuilder.length(), CodeBlockFormatSpan.class);
                for (CodeBlockFormatSpan oldSpan : codeBlockSpans) {
                    int start = spannableStringBuilder.getSpanStart(oldSpan);
                    int end = spannableStringBuilder.getSpanEnd(oldSpan);
                    spannableStringBuilder.removeSpan(oldSpan);
                    // Remove MarkdownConverter's helper spans in the code block range
                    for (android.text.style.LeadingMarginSpan.Standard lms :
                            spannableStringBuilder.getSpans(start, end, android.text.style.LeadingMarginSpan.Standard.class)) {
                        spannableStringBuilder.removeSpan(lms);
                    }
                    for (android.text.style.ForegroundColorSpan fcs :
                            spannableStringBuilder.getSpans(start, end, android.text.style.ForegroundColorSpan.class)) {
                        spannableStringBuilder.removeSpan(fcs);
                    }
                    for (android.text.style.TypefaceSpan tfs :
                            spannableStringBuilder.getSpans(start, end, android.text.style.TypefaceSpan.class)) {
                        spannableStringBuilder.removeSpan(tfs);
                    }
                    spannableStringBuilder.setSpan(new CodeBlockFormatSpan(getContext()),
                            start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    spannableStringBuilder.setSpan(new android.text.style.TypefaceSpan("monospace"),
                            start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                InlineCodeFormatSpan[] inlineCodeSpans = spannableStringBuilder.getSpans(
                        0, spannableStringBuilder.length(), InlineCodeFormatSpan.class);
                for (InlineCodeFormatSpan oldSpan : inlineCodeSpans) {
                    int start = spannableStringBuilder.getSpanStart(oldSpan);
                    int end = spannableStringBuilder.getSpanEnd(oldSpan);
                    spannableStringBuilder.removeSpan(oldSpan);
                    spannableStringBuilder.setSpan(new InlineCodeFormatSpan(getContext()),
                            start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
            
            for (CometChatTextFormatter textFormatter : cometchatTextFormatters) {
                if (textFormatter != null) {
                    spannableStringBuilder = textFormatter.prepareMessageString(
                            getContext(),
                            message,
                            spannableStringBuilder,
                            null,
                            UIKitConstants.FormattingType.MESSAGE_COMPOSER
                    );
                }
            }
            
            // Strip mention formatting (NonEditableSpan) inside code blocks and
            // inline code, converting them to ConsumedMentionSpan. This ensures:
            // 1. Mentions appear as plain text inside code (matching manual behavior)
            // 2. restoreMentionFormattingFromCodeBlock can restore them when the
            //    user later removes the code block
            // 3. The backward scan in afterTextChanged recognises ConsumedMentionSpan
            //    and skips mention detection for the '@' character
            if (enableRichTextFormatting) {
                CodeBlockFormatSpan[] cbSpans = spannableStringBuilder.getSpans(
                        0, spannableStringBuilder.length(), CodeBlockFormatSpan.class);
                for (CodeBlockFormatSpan cb : cbSpans) {
                    int cbStart = spannableStringBuilder.getSpanStart(cb);
                    int cbEnd = spannableStringBuilder.getSpanEnd(cb);
                    if (cbStart < 0 || cbEnd < 0) continue;
                    NonEditableSpan[] mentionSpans = spannableStringBuilder.getSpans(
                            cbStart, cbEnd, NonEditableSpan.class);
                    for (NonEditableSpan mention : mentionSpans) {
                        int mStart = spannableStringBuilder.getSpanStart(mention);
                        int mEnd = spannableStringBuilder.getSpanEnd(mention);
                        if (mStart < 0 || mEnd < 0) continue;
                        ConsumedMentionSpan consumed = new ConsumedMentionSpan(
                                mention.getId(), mention.getText(),
                                mention.getSuggestionItem(), mention.getTextAppearance());
                        spannableStringBuilder.removeSpan(mention);
                        spannableStringBuilder.setSpan(consumed, mStart, mEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                InlineCodeFormatSpan[] icSpans = spannableStringBuilder.getSpans(
                        0, spannableStringBuilder.length(), InlineCodeFormatSpan.class);
                for (InlineCodeFormatSpan ic : icSpans) {
                    int icStart = spannableStringBuilder.getSpanStart(ic);
                    int icEnd = spannableStringBuilder.getSpanEnd(ic);
                    if (icStart < 0 || icEnd < 0) continue;
                    NonEditableSpan[] mentionSpans = spannableStringBuilder.getSpans(
                            icStart, icEnd, NonEditableSpan.class);
                    for (NonEditableSpan mention : mentionSpans) {
                        int mStart = spannableStringBuilder.getSpanStart(mention);
                        int mEnd = spannableStringBuilder.getSpanEnd(mention);
                        if (mStart < 0 || mEnd < 0) continue;
                        ConsumedMentionSpan consumed = new ConsumedMentionSpan(
                                mention.getId(), mention.getText(),
                                mention.getSuggestionItem(), mention.getTextAppearance());
                        spannableStringBuilder.removeSpan(mention);
                        spannableStringBuilder.setSpan(consumed, mStart, mEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            
            // Detect mentions by display name inside code blocks.
            // When a message was sent with a mention inside a code block, the text
            // contains the display name (e.g., "@Dhruv Kushvaha") not the <@uid:>
            // syntax. getComposerSpan can't find these because the server doesn't
            // persist mentionedUsers for display-name mentions. Instead, we stored
            // the mention data in the message metadata (key "codeBlockMentions")
            // during send. We read it back here and create ConsumedMentionSpan so
            // restoreMentionFormattingFromCodeBlock can restore them when the user
            // removes the code block.
            if (enableRichTextFormatting) {
                List<User> codeBlockMentionUsers = getCodeBlockMentionsFromMetadata(message);
                if (!codeBlockMentionUsers.isEmpty()) {
                    // Get the mention tag style from the formatter so restored
                    // NonEditableSpans have proper visual styling (color, background)
                    PromptTextStyle mentionTagStyle = cometchatMentionsFormatter != null
                            ? cometchatMentionsFormatter.getComposerTagStyle() : null;
                    String currentText = spannableStringBuilder.toString();
                    CodeBlockFormatSpan[] cbSpansForDetect = spannableStringBuilder.getSpans(
                            0, spannableStringBuilder.length(), CodeBlockFormatSpan.class);
                    for (CodeBlockFormatSpan cb : cbSpansForDetect) {
                        int cbStart = spannableStringBuilder.getSpanStart(cb);
                        int cbEnd = spannableStringBuilder.getSpanEnd(cb);
                        if (cbStart < 0 || cbEnd < 0) continue;
                        // Check if there are already ConsumedMentionSpan in this range
                        // (from the NonEditableSpan stripping above). If so, skip.
                        ConsumedMentionSpan[] existingConsumed = spannableStringBuilder.getSpans(
                                cbStart, cbEnd, ConsumedMentionSpan.class);
                        if (existingConsumed.length > 0) continue;
                        
                        for (User user : codeBlockMentionUsers) {
                            if (user == null || user.getName() == null) continue;
                            String mentionDisplay = "@" + user.getName();
                            int searchFrom = cbStart;
                            while (searchFrom < cbEnd) {
                                int idx = currentText.indexOf(mentionDisplay, searchFrom);
                                if (idx < 0 || idx >= cbEnd) break;
                                int mentionEnd = idx + mentionDisplay.length();
                                if (mentionEnd > cbEnd) break;
                                
                                // Build a SuggestionItem for restoration
                                SuggestionItem suggestionItem = new SuggestionItem(
                                        user.getUid(), user.getName(),
                                        user.getAvatar(), user.getStatus(),
                                        "@" + user.getName(),
                                        "<@uid:" + user.getUid() + ">",
                                        user.toJson(),
                                        mentionTagStyle);
                                ConsumedMentionSpan consumed = new ConsumedMentionSpan(
                                        '@', mentionDisplay, suggestionItem, mentionTagStyle);
                                spannableStringBuilder.setSpan(consumed, idx, mentionEnd,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                searchFrom = mentionEnd;
                            }
                        }
                    }
                    // Same for inline code spans
                    InlineCodeFormatSpan[] icSpansForDetect = spannableStringBuilder.getSpans(
                            0, spannableStringBuilder.length(), InlineCodeFormatSpan.class);
                    for (InlineCodeFormatSpan ic : icSpansForDetect) {
                        int icStart = spannableStringBuilder.getSpanStart(ic);
                        int icEnd = spannableStringBuilder.getSpanEnd(ic);
                        if (icStart < 0 || icEnd < 0) continue;
                        ConsumedMentionSpan[] existingConsumed = spannableStringBuilder.getSpans(
                                icStart, icEnd, ConsumedMentionSpan.class);
                        if (existingConsumed.length > 0) continue;
                        
                        for (User user : codeBlockMentionUsers) {
                            if (user == null || user.getName() == null) continue;
                            String mentionDisplay = "@" + user.getName();
                            int searchFrom = icStart;
                            while (searchFrom < icEnd) {
                                int idx = currentText.indexOf(mentionDisplay, searchFrom);
                                if (idx < 0 || idx >= icEnd) break;
                                int mentionEnd = idx + mentionDisplay.length();
                                if (mentionEnd > icEnd) break;
                                
                                SuggestionItem suggestionItem = new SuggestionItem(
                                        user.getUid(), user.getName(),
                                        user.getAvatar(), user.getStatus(),
                                        "@" + user.getName(),
                                        "<@uid:" + user.getUid() + ">",
                                        user.toJson(),
                                        mentionTagStyle);
                                ConsumedMentionSpan consumed = new ConsumedMentionSpan(
                                        '@', mentionDisplay, suggestionItem, mentionTagStyle);
                                spannableStringBuilder.setSpan(consumed, idx, mentionEnd,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                searchFrom = mentionEnd;
                            }
                        }
                    }
                }
            }
            
            // Set up the edit preview using messagePreview component
            // Override the title to show "Edit"
            binding.messagePreview.setMessagePreviewTitleText(getContext().getString(R.string.cometchat_edit));
            // Build a separate formatted spannable for the preview subtitle.
            // The main spannableStringBuilder has composer-style spans (editable, theme-aware)
            // which are not suitable for the single-line read-only preview. Instead, apply
            // CometChatRichTextFormatter.prepareConversationSpan which produces spans
            // optimised for single-line display (collapsed newlines, simplified code spans).
            SpannableStringBuilder previewSpannable = new SpannableStringBuilder(message.getText() != null ? message.getText() : "");
            for (CometChatTextFormatter textFormatter : cometchatTextFormatters) {
                if (textFormatter != null) {
                    previewSpannable = textFormatter.prepareMessageString(
                            getContext(),
                            message,
                            previewSpannable,
                            null,
                            UIKitConstants.FormattingType.CONVERSATIONS
                    );
                }
            }
            binding.messagePreview.setMessagePreviewSubtitleText((CharSequence) previewSpannable);
            binding.messagePreview.getSubtitleView().setMaxLines(1);
            
            // Set up close listener for edit mode
            binding.messagePreview.setOnCloseClickListener(() -> {
                cancelEdit();
            });
            
            // Show the preview with animation
            animateVisibilityVisible(binding.messagePreview);
            
            // Suppress mention detection during programmatic text insertion.
            // Without this, afterTextChanged scans for '@' characters in existing
            // mention text (e.g. "@Dhruv Kushvaha") and triggers the suggestion list.
            suppressMentionDetection = true;
            
            // Populate input field with parsed spannable (visual formatting, no markers)
            binding.inputField.setText(spannableStringBuilder);
            // Move cursor to end
            binding.inputField.setSelection(spannableStringBuilder.length());
            binding.inputField.requestFocus();
            
            // Re-enable mention detection after programmatic text insertion is complete.
            // Use post() to ensure this runs after any pending afterTextChanged callbacks.
            binding.inputField.post(() -> {
                suppressMentionDetection = false;
            });
            
            // Send button should be inactive initially since message hasn't changed
            updateSendButtonState(false);
            
            // Re-enable info visibility updates after edit mode initialization is complete
            // Use post() to ensure this runs after any pending LiveData updates
            binding.inputField.post(() -> {
                suppressInfoVisibility = false;
            });
        } else {
            animateVisibilityGone(binding.messagePreview);
        }
    }

    /**
     * Cancels edit mode.
     */
    private void cancelEdit() {
        if (viewModel != null) {
            viewModel.cancelEdit();
        }
        animateVisibilityGone(binding.messagePreview);
        clearText();
        if (onEditCancel != null) {
            onEditCancel.onEditCancel();
        }
    }

    /**
     * Sets the message to reply to.
     *
     * @param message The BaseMessage to reply to.
     */
    public void setReplyMessage(@Nullable BaseMessage message) {
        if (message != null) {
            showQuoteMessagePreview(message);
        } else {
            cancelReply();
        }
    }

    /**
     * Cancels reply mode.
     */
    private void cancelReply() {
        quoteMessage = null;
        animateVisibilityGone(binding.messagePreview);
    }

    /**
     * Shows the rich text toolbar.
     */
    public void showRichTextToolbar() {
        isToolbarVisible = true;
        binding.richTextToolbar.setVisibility(View.VISIBLE);
        if (viewModel != null) {
            viewModel.setToolbarVisible(true);
        }
        if (onToolbarVisibilityChange != null) {
            onToolbarVisibilityChange.onToolbarVisibilityChange(true);
        }
    }

    /**
     * Hides the rich text toolbar.
     */
    public void hideRichTextToolbar() {
        isToolbarVisible = false;
        binding.richTextToolbar.setVisibility(View.GONE);
        if (viewModel != null) {
            viewModel.setToolbarVisible(false);
        }
        if (onToolbarVisibilityChange != null) {
            onToolbarVisibilityChange.onToolbarVisibilityChange(false);
        }
    }

    // ==================== Style Setters ====================

    /**
     * Sets the background color of the composer container.
     * @param color The background color.
     */
    private void setComposerBackgroundColor(@ColorInt int color) {
        this.backgroundColor = color;
    }

    /**
     * Sets the stroke color of the composer container.
     * @param color The stroke color.
     */
    private void setComposerStrokeColor(@ColorInt int color) {
        this.strokeColor = color;
    }

    /**
     * Sets the stroke width of the composer container.
     * @param width The stroke width in pixels.
     */
    private void setComposerStrokeWidth(@Dimension int width) {
        this.strokeWidth = width;
    }

    /**
     * Sets the corner radius of the composer container.
     * @param radius The corner radius in pixels.
     */
    private void setComposerCornerRadius(@Dimension int radius) {
        this.cornerRadius = radius;
    }

    /**
     * Sets the background drawable of the composer container.
     * @param drawable The background drawable.
     */
    private void setComposerBackgroundDrawable(@Nullable Drawable drawable) {
        this.backgroundDrawable = drawable;
    }

    /**
     * Sets the compose box background color.
     * @param color The background color.
     */
    public void setComposeBoxBackgroundColor(@ColorInt int color) {
        this.composeBoxBackgroundColor = color;
        if (binding != null && binding.composeBoxCard != null) {
            binding.composeBoxCard.setCardBackgroundColor(color);
        }
    }

    /**
     * Sets the compose box stroke color.
     * @param color The stroke color.
     */
    public void setComposeBoxStrokeColor(@ColorInt int color) {
        this.composeBoxStrokeColor = color;
        if (binding != null && binding.composeBoxCard != null) {
            binding.composeBoxCard.setStrokeColor(color);
        }
    }

    /**
     * Sets the compose box stroke width.
     * @param width The stroke width in pixels.
     */
    public void setComposeBoxStrokeWidth(@Dimension int width) {
        this.composeBoxStrokeWidth = width;
        if (binding != null && binding.composeBoxCard != null) {
            binding.composeBoxCard.setStrokeWidth(width);
        }
    }

    /**
     * Sets the compose box corner radius.
     * @param radius The corner radius in pixels.
     */
    public void setComposeBoxCornerRadius(@Dimension int radius) {
        this.composeBoxCornerRadius = radius;
        if (binding != null && binding.composeBoxCard != null) {
            binding.composeBoxCard.setRadius(radius);
        }
    }

    /**
     * Sets the compose box background drawable.
     * @param drawable The background drawable.
     */
    public void setComposeBoxBackgroundDrawable(@Nullable Drawable drawable) {
        this.composeBoxBackgroundDrawable = drawable;
        if (drawable != null && binding != null && binding.composeBoxCard != null) {
            binding.composeBoxCard.setBackground(drawable);
        }
    }

    /**
     * Sets the input text color.
     * @param color The text color.
     */
    private void setInputTextColor(@ColorInt int color) {
        this.inputTextColor = color;
    }

    /**
     * Sets the input hint color.
     * @param color The hint color.
     */
    private void setInputHintColor(@ColorInt int color) {
        this.inputHintColor = color;
    }

    /**
     * Sets the input text appearance.
     * @param textAppearance The text appearance resource ID.
     */
    private void setInputTextAppearance(@StyleRes int textAppearance) {
        this.inputTextAppearance = textAppearance;
    }

    /**
     * Sets the input background color.
     * @param color The background color.
     */
    private void setInputBackgroundColor(@ColorInt int color) {
        this.inputBackgroundColor = color;
    }

    /**
     * Sets the active send button drawable.
     * @param drawable The drawable.
     */
    private void setActiveSendButtonDrawable(@Nullable Drawable drawable) {
        this.activeSendButtonDrawable = drawable;
    }

    /**
     * Sets the inactive send button drawable.
     * @param drawable The drawable.
     */
    private void setInactiveSendButtonDrawable(@Nullable Drawable drawable) {
        this.inactiveSendButtonDrawable = drawable;
    }

    /**
     * Sets the send button background color.
     * @param color The background color.
     */
    private void setSendButtonBackgroundColor(@ColorInt int color) {
        this.sendButtonBackgroundColor = color;
    }

    /**
     * Sets the attachment icon.
     * @param drawable The icon drawable.
     */
    private void setAttachmentIcon(@Nullable Drawable drawable) {
        this.attachmentIcon = drawable;
    }

    /**
     * Sets the attachment icon tint.
     * @param color The tint color.
     */
    private void setAttachmentIconTint(@ColorInt int color) {
        this.attachmentIconTint = color;
    }

    /**
     * Sets the voice recording icon.
     * @param drawable The icon drawable.
     */
    private void setVoiceRecordingIcon(@Nullable Drawable drawable) {
        this.voiceRecordingIcon = drawable;
    }

    /**
     * Sets the voice recording icon tint.
     * @param color The tint color.
     */
    private void setVoiceRecordingIconTint(@ColorInt int color) {
        this.voiceRecordingIconTint = color;
    }

    /**
     * Sets the AI icon.
     * @param drawable The icon drawable.
     */
    private void setAIIcon(@Nullable Drawable drawable) {
        this.aiIcon = drawable;
    }

    /**
     * Sets the AI icon tint.
     * @param color The tint color.
     */
    private void setAIIconTint(@ColorInt int color) {
        this.aiIconTint = color;
    }

    /**
     * Sets the sticker icon.
     * @param drawable The icon drawable.
     */
    private void setStickerIcon(@Nullable Drawable drawable) {
        this.stickerIcon = drawable;
    }

    /**
     * Sets the sticker icon tint.
     * @param color The tint color.
     */
    private void setStickerIconTint(@ColorInt int color) {
        this.stickerIconTint = color;
    }

    /**
     * Sets the mention banner background color.
     * @param color The background color.
     */
    private void setMentionBannerBackgroundColor(@ColorInt int color) {
        this.mentionBannerBackgroundColor = color;
    }

    /**
     * Sets the mention banner text color.
     * @param color The text color.
     */
    private void setMentionBannerTextColor(@ColorInt int color) {
        this.mentionBannerTextColor = color;
    }

    /**
     * Sets the mention banner text appearance.
     * @param textAppearance The text appearance resource ID.
     */
    private void setMentionBannerTextAppearance(@StyleRes int textAppearance) {
        this.mentionBannerTextAppearance = textAppearance;
    }

    /**
     * Sets the mention banner icon.
     * @param drawable The icon drawable.
     */
    private void setMentionBannerIcon(@Nullable Drawable drawable) {
        this.mentionBannerIcon = drawable;
    }

    /**
     * Sets the mention banner icon tint.
     * @param color The tint color.
     */
    private void setMentionBannerIconTint(@ColorInt int color) {
        this.mentionBannerIconTint = color;
    }

    /**
     * Sets the mention banner close icon.
     * @param drawable The icon drawable.
     */
    private void setMentionBannerCloseIcon(@Nullable Drawable drawable) {
        this.mentionBannerCloseIcon = drawable;
    }

    /**
     * Sets the mention banner close icon tint.
     * @param color The tint color.
     */
    private void setMentionBannerCloseIconTint(@ColorInt int color) {
        this.mentionBannerCloseIconTint = color;
    }

    /**
     * Sets the rich text toolbar style.
     * @param style The style resource ID.
     */
    private void setRichTextToolbarStyle(@StyleRes int style) {
        this.richTextToolbarStyle = style;
    }

    public @StyleRes int getSuggestionListStyle() {
        return suggestionListStyle;
    }

    /**
     * Sets the suggestion list style.
     * @param style The style resource ID.
     */
    private void setSuggestionListStyle(@StyleRes int style) {
        this.suggestionListStyle = style;
        binding.suggestionList.setStyle(suggestionListStyle);
    }

    /**
     * Sets the message preview style.
     * Following the same pattern as CometChatMessageComposer.setMessagePreviewStyle().
     * @param style The style resource ID.
     */
    private void setMessagePreviewStyle(@StyleRes int style) {
        this.messagePreviewStyle = style;
        if (style == 0) {
            // Apply default styles
            binding.messagePreview.setBackgroundColor(CometChatTheme.getBackgroundColor3(getContext()));
            binding.messagePreview.setTitleTextColor(CometChatTheme.getTextColorHighlight(getContext()));
            binding.messagePreview.setSubtitleTextColor(CometChatTheme.getTextColorSecondary(getContext()));
            binding.messagePreview.setCloseIconTint(CometChatTheme.getIconTintPrimary(getContext()));
            binding.messagePreview.setSeparatorColor(CometChatTheme.getStrokeColorHighlight(getContext()));
            return;
        }
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatMessagePreview);
        try {
            binding.messagePreview.setBackgroundColor(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewBackgroundColor, CometChatTheme.getBackgroundColor3(getContext())));
            binding.messagePreview.setTitleTextColor(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewTitleTextColor, CometChatTheme.getTextColorHighlight(getContext())));
            binding.messagePreview.setSubtitleTextColor(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewSubtitleTextColor, CometChatTheme.getTextColorSecondary(getContext())));
            binding.messagePreview.setCloseIconTint(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewCloseIconTint, CometChatTheme.getIconTintPrimary(getContext())));
            binding.messagePreview.setStrokeColor(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewStrokeColor, 0));
            binding.messagePreview.setSeparatorColor(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewSeparatorColor, CometChatTheme.getStrokeColorHighlight(getContext())));

            int titleTextAppearance = typedArray.getResourceId(R.styleable.CometChatMessagePreview_cometChatMessagePreviewTitleTextAppearance, 0);
            if (titleTextAppearance != 0) {
                binding.messagePreview.setTitleTextAppearance(titleTextAppearance);
            }
            int subtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatMessagePreview_cometChatMessagePreviewSubtitleTextAppearance, 0);
            if (subtitleTextAppearance != 0) {
                binding.messagePreview.setSubtitleTextAppearance(subtitleTextAppearance);
            }
            Drawable closeIconDrawable = typedArray.getDrawable(R.styleable.CometChatMessagePreview_cometChatMessagePreviewCloseIcon);
            if (closeIconDrawable != null) {
                binding.messagePreview.setCloseIcon(closeIconDrawable);
            }
            binding.messagePreview.setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMessagePreview_cometChatMessagePreviewStrokeWidth, 0));
            binding.messagePreview.setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatMessagePreview_cometChatMessagePreviewCornerRadius, 0));
        } finally {
            typedArray.recycle();
        }
    }

    // ==================== Visibility Setters ====================

    /**
     * Sets the attachment button visibility.
     *
     * @param visibility The visibility (View.VISIBLE, View.INVISIBLE, or View.GONE).
     */
    public void setAttachmentButtonVisibility(int visibility) {
        this.attachmentButtonVisibility = visibility;
        binding.btnAttachment.setVisibility(visibility);
    }

    /**
     * Sets the voice recording button visibility.
     *
     * @param visibility The visibility.
     */
    public void setVoiceRecordingButtonVisibility(int visibility) {
        this.voiceRecordingButtonVisibility = visibility;
        binding.btnVoiceRecording.setVisibility(visibility);
    }

    /**
     * Sets the sticker button visibility.
     *
     * @param visibility The visibility.
     */
    public void setStickerButtonVisibility(int visibility) {
        this.stickerButtonVisibility = visibility;
        binding.btnSticker.setVisibility(visibility);
    }

    /**
     * Sets the AI button visibility.
     *
     * @param visibility The visibility.
     */
    public void setAIButtonVisibility(int visibility) {
        this.aiButtonVisibility = visibility;
        binding.btnAi.setVisibility(visibility);
    }

    /**
     * Sets the send button visibility.
     *
     * @param visibility The visibility.
     */
    public void setSendButtonVisibility(int visibility) {
        this.sendButtonVisibility = visibility;
        binding.sendButtonCard.setVisibility(visibility);
    }

    // ==================== Mention Configuration ====================

    /**
     * Disables or enables mentions.
     *
     * @param disable true to disable mentions.
     */
    public void setDisableMentions(boolean disable) {
        this.disableMentions = disable;
    }

    /**
     * Disables or enables mention all.
     *
     * @param disable true to disable mention all.
     */
    public void setDisableMentionAll(boolean disable) {
        this.disableMentionAll = disable;
    }

    /**
     * Sets the mentions style.
     *
     * @param mentionsStyle The style resource ID for mentions.
     */
    public void setMentionsStyle(@StyleRes int mentionsStyle) {
        if (cometchatMentionsFormatter != null) {
            cometchatMentionsFormatter.setMessageComposerMentionTextStyle(getContext(), mentionsStyle);
        }
    }

    // ==================== Attachment Option Visibility ====================

    /**
     * Sets the visibility of the image attachment option.
     *
     * @param visibility The visibility (View.VISIBLE, View.INVISIBLE, or View.GONE).
     */
    public void setImageAttachmentOptionVisibility(int visibility) {
        this.imageAttachmentOptionVisibility = visibility;
        if (additionParameter != null) {
            additionParameter.setImageAttachmentOptionVisibility(visibility);
        }
    }

    /**
     * Gets the visibility of the image attachment option.
     *
     * @return The visibility state.
     */
    public int getImageAttachmentOptionVisibility() {
        return imageAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the camera attachment option.
     *
     * @param visibility The visibility (View.VISIBLE, View.INVISIBLE, or View.GONE).
     */
    public void setCameraAttachmentOptionVisibility(int visibility) {
        this.cameraAttachmentOptionVisibility = visibility;
        if (additionParameter != null) {
            additionParameter.setCameraAttachmentOptionVisibility(visibility);
        }
    }

    /**
     * Gets the visibility of the camera attachment option.
     *
     * @return The visibility state.
     */
    public int getCameraAttachmentOptionVisibility() {
        return cameraAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the video attachment option.
     *
     * @param visibility The visibility (View.VISIBLE, View.INVISIBLE, or View.GONE).
     */
    public void setVideoAttachmentOptionVisibility(int visibility) {
        this.videoAttachmentOptionVisibility = visibility;
        if (additionParameter != null) {
            additionParameter.setVideoAttachmentOptionVisibility(visibility);
        }
    }

    /**
     * Gets the visibility of the video attachment option.
     *
     * @return The visibility state.
     */
    public int getVideoAttachmentOptionVisibility() {
        return videoAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the audio attachment option.
     *
     * @param visibility The visibility (View.VISIBLE, View.INVISIBLE, or View.GONE).
     */
    public void setAudioAttachmentOptionVisibility(int visibility) {
        this.audioAttachmentOptionVisibility = visibility;
        if (additionParameter != null) {
            additionParameter.setAudioAttachmentOptionVisibility(visibility);
        }
    }

    /**
     * Gets the visibility of the audio attachment option.
     *
     * @return The visibility state.
     */
    public int getAudioAttachmentOptionVisibility() {
        return audioAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the file attachment option.
     *
     * @param visibility The visibility (View.VISIBLE, View.INVISIBLE, or View.GONE).
     */
    public void setFileAttachmentOptionVisibility(int visibility) {
        this.fileAttachmentOptionVisibility = visibility;
        if (additionParameter != null) {
            additionParameter.setFileAttachmentOptionVisibility(visibility);
        }
    }

    /**
     * Gets the visibility of the file attachment option.
     *
     * @return The visibility state.
     */
    public int getFileAttachmentOptionVisibility() {
        return fileAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the poll attachment option.
     *
     * @param visibility The visibility (View.VISIBLE, View.INVISIBLE, or View.GONE).
     */
    public void setPollAttachmentOptionVisibility(int visibility) {
        this.pollAttachmentOptionVisibility = visibility;
        if (additionParameter != null) {
            additionParameter.setPollAttachmentOptionVisibility(visibility);
        }
    }

    /**
     * Gets the visibility of the poll attachment option.
     *
     * @return The visibility state.
     */
    public int getPollAttachmentOptionVisibility() {
        return pollAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the collaborative document option.
     *
     * @param visibility The visibility (View.VISIBLE, View.INVISIBLE, or View.GONE).
     */
    public void setCollaborativeDocumentOptionVisibility(int visibility) {
        this.collaborativeDocumentOptionVisibility = visibility;
        if (additionParameter != null) {
            additionParameter.setCollaborativeDocumentOptionVisibility(visibility);
        }
    }

    /**
     * Gets the visibility of the collaborative document option.
     *
     * @return The visibility state.
     */
    public int getCollaborativeDocumentOptionVisibility() {
        return collaborativeDocumentOptionVisibility;
    }

    /**
     * Sets the visibility of the collaborative whiteboard option.
     *
     * @param visibility The visibility (View.VISIBLE, View.INVISIBLE, or View.GONE).
     */
    public void setCollaborativeWhiteboardOptionVisibility(int visibility) {
        this.collaborativeWhiteboardOptionVisibility = visibility;
        if (additionParameter != null) {
            additionParameter.setCollaborativeWhiteboardOptionVisibility(visibility);
        }
    }

    /**
     * Gets the visibility of the collaborative whiteboard option.
     *
     * @return The visibility state.
     */
    public int getCollaborativeWhiteboardOptionVisibility() {
        return collaborativeWhiteboardOptionVisibility;
    }

    // ==================== Sound and Typing Configuration ====================

    /**
     * Disables or enables sound for messages.
     *
     * @param disable true to disable sound.
     */
    public void setDisableSoundForMessages(boolean disable) {
        this.disableSoundForMessages = disable;
    }

    /**
     * Sets a custom sound for messages.
     *
     * @param soundRes The raw resource ID for the sound.
     */
    public void setCustomSoundForMessages(@RawRes int soundRes) {
        this.customSoundForMessages = soundRes;
    }

    /**
     * Disables or enables typing events.
     *
     * @param disable true to disable typing events.
     */
    public void setDisableTypingEvents(boolean disable) {
        this.disableTypingEvents = disable;
        if (viewModel != null) {
            viewModel.setDisableTypingEvents(disable);
        }
    }

    /**
     * Sets the typing debounce duration.
     *
     * @param debounceMs The debounce duration in milliseconds.
     */
    public void setTypingDebounceMs(long debounceMs) {
        if (viewModel != null) {
            viewModel.setTypingDebounceMs(debounceMs);
        }
    }

    // ==================== Callback Setters ====================

    /**
     * Sets the send button click listener.
     *
     * @param listener The listener.
     */
    public void setOnSendClickListener(@Nullable SendButtonClickListener listener) {
        this.onSendClick = listener;
    }

    /**
     * Sets the text change listener.
     *
     * @param listener The listener.
     */
    public void setOnTextChangeListener(@Nullable OnTextChangeListener listener) {
        this.onTextChange = listener;
    }

    /**
     * Sets the attachment click listener.
     *
     * @param listener The listener.
     */
    public void setOnAttachmentClickListener(@Nullable OnAttachmentClickListener listener) {
        this.onAttachmentClick = listener;
    }

    /**
     * Sets the error listener.
     *
     * @param listener The listener.
     */
    public void setOnErrorListener(@Nullable OnErrorListener listener) {
        this.onError = listener;
    }

    /**
     * Sets the voice recording listener.
     *
     * @param listener The listener.
     */
    public void setOnVoiceRecordingListener(@Nullable OnVoiceRecordingListener listener) {
        this.onVoiceRecording = listener;
    }

    /**
     * Sets the edit cancel listener.
     *
     * @param listener The listener.
     */
    public void setOnEditCancelListener(@Nullable OnEditCancelListener listener) {
        this.onEditCancel = listener;
    }

    /**
     * Sets the toolbar visibility change listener.
     *
     * @param listener The listener.
     */
    public void setOnToolbarVisibilityChangeListener(@Nullable OnToolbarVisibilityChangeListener listener) {
        this.onToolbarVisibilityChange = listener;
    }

    // ==================== Custom View Setters ====================

    /**
     * Gets the custom header view.
     *
     * @return The header view, or null if none is set.
     */
    @Nullable
    public View getHeaderView() {
        return headerView;
    }

    /**
     * Sets a custom header view. Uses {@link Utils#handleView(ViewGroup, View, boolean)}
     * to safely replace the container contents, matching the CometChatMessageComposer pattern.
     *
     * @param view The header view, or null to clear.
     */
    public void setHeaderView(@Nullable View view) {
        this.headerView = view;
        Utils.handleView(binding.headerViewContainer, view, false);
        if (view != null) {
            binding.headerViewContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Gets the custom footer view.
     *
     * @return The footer view, or null if none is set.
     */
    @Nullable
    public View getFooterView() {
        return footerView;
    }

    /**
     * Sets a custom footer view. Uses {@link Utils#handleView(ViewGroup, View, boolean)}
     * to safely replace the container contents, matching the CometChatMessageComposer pattern.
     *
     * @param view The footer view, or null to clear.
     */
    public void setFooterView(@Nullable View view) {
        this.footerView = view;
        Utils.handleView(binding.footerViewContainer, view, false);
    }

    /**
     * Gets the custom send button view.
     *
     * @return The send button view, or null if none is set.
     */
    @Nullable
    public View getSendButtonView() {
        return sendButtonView;
    }

    /**
     * Sets a custom send button view. Replaces the default send button card content
     * with the provided view using {@link Utils#handleView(ViewGroup, View, boolean)},
     * matching the CometChatMessageComposer pattern.
     *
     * @param view The send button view, or null to keep the default.
     */
    public void setSendButtonView(@Nullable View view) {
        if (view != null) {
            this.sendButtonView = view;
            Utils.handleView(binding.sendButtonCard, view, false);
        }
    }

    /**
     * Sets a custom auxiliary button view. Replaces the contents of the auxiliary
     * buttons container with the provided view, matching the CometChatMessageComposer pattern.
     *
     * @param view The auxiliary button view, or null to keep the default.
     */
    public void setAuxiliaryButtonView(@Nullable View view) {
        if (view != null) {
            Utils.handleView(binding.auxiliaryButtonsContainer, view, false);
        }
    }

    // ==================== Additional API Props (CometChatMessageComposer Parity) ====================

    /**
     * Sets the AI options for the composer. When set, the AI button becomes functional
     * and displays the provided options when clicked.
     *
     * @param aiOptions The function to retrieve the list of AI composer actions.
     */
    public void setAIOptions(Function4<Context, User, Group, HashMap<String, String>, List<CometChatMessageComposerAction>> aiOptions) {
        if (aiOptions != null) {
            this.aiOptions = aiOptions;
            setAIActions();
        }
    }

    /**
     * Sets up the AI actions for the composer. This method retrieves the available
     * AI options from the aiOptions callback, creates action items for each option,
     * and populates the AI option sheet menu items list.
     */
    private void setAIActions() {
        aiOptionSheetMenuItems.clear();
        if (aiOptions != null && viewModel != null) {
            List<CometChatMessageComposerAction> actions = aiOptions.invoke(getContext(), user, group, viewModel.getIdMap());
            if (actions != null && !actions.isEmpty()) {
                for (CometChatMessageComposerAction option : actions) {
                    if (option != null) {
                        actionHashMap.put(option.getId(), option);
                        aiOptionSheetMenuItems.add(new OptionSheetMenuItem(
                                option.getId(),
                                option.getIcon(),
                                option.getIconTintColor(),
                                option.getIconBackground(),
                                option.getTitle(),
                                option.getTitleFont(),
                                option.getTitleAppearance(),
                                option.getTitleColor(),
                                option.getBackground(),
                                option.getCornerRadius()));
                    }
                }
            }
        }
    }

    /**
     * Returns the list of AI option sheet menu items.
     *
     * @return The list of AI option menu items.
     */
    public List<OptionSheetMenuItem> getAIOptionItems() {
        return aiOptionSheetMenuItems;
    }

    /**
     * Sets a custom view holder listener for the suggestion list items.
     *
     * @param suggestionListViewHolderListener The listener that handles item view interactions.
     */
    public void setSuggestionListItemView(SuggestionListViewHolderListener suggestionListViewHolderListener) {
        binding.suggestionList.setListItemView(suggestionListViewHolderListener);
    }

    /**
     * Sets the click listener for suggestion items.
     *
     * @param onItemClickListener The listener to be called on suggestion item clicks.
     */
    public void setOnSuggestionClickListener(OnItemClickListener<SuggestionItem> onItemClickListener) {
        if (onItemClickListener != null) this.onItemClickListener = onItemClickListener;
    }

    /**
     * Sets the maximum height limit for the suggestion list.
     *
     * @param dp The maximum height limit in density-independent pixels.
     */
    public void setSuggestionListMaxHeight(int dp) {
        binding.suggestionList.setMaxHeightLimit(dp);
    }

    /**
     * Sets a custom label for the "mention all" feature.
     *
     * @param id    The unique identifier for the mention all label.
     * @param label The custom label text to display.
     */
    public void setMentionAllLabelId(String id, String label) {
        if (id != null && !id.isEmpty() && label != null && !label.isEmpty()) {
            if (cometchatMentionsFormatter != null) {
                cometchatMentionsFormatter.setMentionAllLabel(id, label);
            }
            mentionAllLabelId = id;
            mentionAllLabel = label;
        }
    }

    /**
     * Sets the style for the AI option sheet.
     *
     * @param aiOptionSheetStyle The style resource ID.
     */
    public void setAIOptionSheetStyle(@StyleRes int aiOptionSheetStyle) {
        this.aiOptionSheetStyle = aiOptionSheetStyle;
    }

    /**
     * Sets the error listener for the composer. This is a compatibility alias
     * matching the CometChatMessageComposer API. Wraps the provided {@link OnError}
     * into the internal {@link OnErrorListener}.
     *
     * @param onError The {@link OnError} listener to be set.
     */
    public void setOnError(OnError onError) {
        if (onError != null) {
            this.onError = exception -> {
                if (exception instanceof CometChatException) {
                    onError.onError((CometChatException) exception);
                } else {
                    onError.onError(new CometChatException("ERR_UNKNOWN", exception.getMessage(), exception.getMessage()));
                }
            };
        }
    }

    /**
     * Sets the style for the composer.
     *
     * @param style The style resource ID.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                    style, R.styleable.CometChatSingleLineComposer);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    // ==================== Message Input Style ====================

    /**
     * Sets the message input style.
     *
     * @param style The style resource ID.
     */
    public void setMessageInputStyle(@StyleRes int style) {
        // Apply style to input field if needed
    }

    // ==================== Info Banner Setters ====================

    /**
     * Sets the visibility of the info banner (mention limit banner).
     *
     * @param visibility true to show, false to hide.
     */
    public void setInfoVisibility(boolean visibility) {
        // Skip if we're suppressing info visibility updates (e.g., during edit mode initialization)
        if (suppressInfoVisibility && visibility) {
            return;
        }
        if (binding != null && binding.mentionLimitBanner != null) {
            if (visibility) {
                if (binding.mentionLimitBanner.getVisibility() != View.VISIBLE) {
                    animateVisibilityVisible(binding.mentionLimitBanner);
                }
            } else {
                if (binding.mentionLimitBanner.getVisibility() == View.VISIBLE) {
                    animateVisibilityGone(binding.mentionLimitBanner);
                }
            }
        }
    }

    /**
     * Sets the info message text.
     *
     * @param message The info message.
     */
    public void setInfoMessage(@Nullable String message) {
        if (message != null && !message.isEmpty() && binding != null && binding.mentionInfoText != null) {
            binding.mentionInfoText.setText(message);
        }
    }

    /**
     * Sets the info icon.
     *
     * @param icon The info icon drawable.
     */
    public void setInfoIcon(@Nullable Drawable icon) {
        if (icon != null && binding != null && binding.mentionInfoIcon != null) {
            binding.mentionInfoIcon.setImageDrawable(icon);
        }
    }

    /**
     * Sets the info text color.
     *
     * @param color The text color.
     */
    public void setInfoTextColor(@ColorInt int color) {
        if (binding != null && binding.mentionInfoText != null) {
            binding.mentionInfoText.setTextColor(color);
        }
    }

    /**
     * Sets the info text appearance.
     *
     * @param textAppearance The text appearance style resource.
     */
    public void setInfoTextAppearance(@StyleRes int textAppearance) {
        if (binding != null) {
            binding.mentionInfoText.setTextAppearance(textAppearance);
        }
    }

    /**
     * Sets the info background color.
     *
     * @param color The background color.
     */
    public void setInfoBackgroundColor(@ColorInt int color) {
        if (binding != null) {
            binding.mentionLimitBanner.setCardBackgroundColor(color);
        }
    }

    /**
     * Sets the info corner radius.
     *
     * @param radius The corner radius in pixels.
     */
    public void setInfoCornerRadius(@Dimension float radius) {
        if (binding != null) {
            binding.mentionLimitBanner.setRadius(radius);
        }
    }

    /**
     * Sets the info stroke color.
     *
     * @param color The stroke color.
     */
    public void setInfoStrokeColor(@ColorInt int color) {
        if (binding != null) {
            binding.mentionLimitBanner.setStrokeColor(color);
        }
    }

    /**
     * Sets the info stroke width.
     *
     * @param width The stroke width in pixels.
     */
    public void setInfoStrokeWidth(@Dimension float width) {
        if (binding != null) {
            binding.mentionLimitBanner.setStrokeWidth((int) width);
        }
    }

    /**
     * Sets the info icon tint.
     *
     * @param color The icon tint color.
     */
    public void setInfoIconTint(@ColorInt int color) {
        if (binding != null) {
            binding.mentionInfoIcon.setColorFilter(color);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Handles send button click.
     * Delegates message sending to the ViewModel following MVVM pattern.
     * <p>
     * For WYSIWYG editing, this method converts spans to markdown before sending
     * so that the CometChat SDK receives properly formatted markdown text.
     * </p>
     */
    private void handleSendClick() {
        if (viewModel == null) return;

        // Block sending while AI is generating a response
        if (isAgentChat && isAIAssistantGenerating) return;

        Editable editable = binding.inputField.getText();
        if (editable == null || editable.toString().trim().isEmpty()) {
            return;
        }

        // Early guard for edit mode: Check if text has changed BEFORE processing
        // This prevents getProcessedText() from modifying the editable when we're just going to return anyway
        TextMessage editingMessage = viewModel.getEditingMessage().getValue();
        if (editingMessage != null) {
            // Get the formatted original text by:
            // 1. First parsing markdown (converts [text](url) to visual text)
            // 2. Then applying text formatters (converts mention markers to display names)
            String originalText = editingMessage.getText();
            SpannableStringBuilder formattedBuilder;
            if (enableRichTextFormatting && originalText != null) {
                // Parse markdown first
                SpannableString parsedMarkdown = MarkdownConverter.fromMarkdown(originalText);
                formattedBuilder = new SpannableStringBuilder(parsedMarkdown);
            } else {
                formattedBuilder = new SpannableStringBuilder(originalText != null ? originalText : "");
            }
            // Then apply text formatters (for mentions)
            for (CometChatTextFormatter textFormatter : cometchatTextFormatters) {
                if (textFormatter != null) {
                    formattedBuilder = textFormatter.prepareMessageString(
                            getContext(),
                            editingMessage,
                            formattedBuilder,
                            null,
                            UIKitConstants.FormattingType.MESSAGE_COMPOSER
                    );
                }
            }
            String formattedOriginalText = formattedBuilder != null ? formattedBuilder.toString().trim() : null;
            
            // Compare current text with formatted original text
            String currentText = editable.toString().trim();
            if (currentText.equals(formattedOriginalText)) {
                // Text hasn't changed, don't proceed
                return;
            }
        }

        // Collect users from ConsumedMentionSpan (mentions inside code blocks)
        // before getProcessedText() modifies the editable. The display text stays
        // as-is inside code blocks, but we need the user metadata in mentionedUsers
        // so the mention can be detected when editing the message later.
        List<User> consumedMentionUsers = new ArrayList<>();
        {
            Editable preEditable = binding.inputField.getText();
            if (preEditable != null) {
                ConsumedMentionSpan[] cSpans = preEditable.getSpans(
                        0, preEditable.length(), ConsumedMentionSpan.class);
                for (ConsumedMentionSpan cs : cSpans) {
                    if (cs.canRestore() && cs.getSuggestionItem() != null
                            && cs.getSuggestionItem().getData() != null) {
                        try {
                            consumedMentionUsers.add(
                                    User.fromJson(cs.getSuggestionItem().getData().toString()));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }

        // Get processed text with mention spans converted to underlying format
        String processedText = getProcessedText().trim();
        
        // Convert to markdown if rich text formatting is enabled
        String finalText;
        if (enableRichTextFormatting) {
            // Re-get editable since getProcessedText modifies it
            Editable currentEditable = binding.inputField.getText();
            if (currentEditable != null) {
                finalText = MarkdownConverter.toMarkdown(currentEditable);
                // If markdown conversion results in empty, use processed text
                if (finalText.trim().isEmpty()) {
                    finalText = processedText;
                }
            } else {
                finalText = processedText;
            }
        } else {
            finalText = processedText;
        }
        
        if (finalText.trim().isEmpty()) {
            return;
        }

        // Handle edit mode (editingMessage was already checked at the start for early return)
        if (editingMessage != null) {
            editingMessage.setText(finalText.trim());
            // Process mentions before sending
            handleMessagePreSend(editingMessage);
            // Append consumed mention users (mentions inside code blocks) so the
            // SDK stores them even though the text uses display names
            appendConsumedMentionUsers(editingMessage, consumedMentionUsers);
            // Store code-block mention users in metadata so they survive the
            // server round-trip (the server only persists mentionedUsers for
            // <@uid:> patterns in text, not for display-name mentions in code blocks)
            storeCodeBlockMentionsInMetadata(editingMessage, consumedMentionUsers);
            // Delegate edit to ViewModel
            viewModel.editMessage(editingMessage);
            clearText();
            cancelEdit();
            // Clear pending formats after sending and reset toolbar
            if (formatSpanWatcher != null) {
                formatSpanWatcher.clearPendingFormats();
            }
            detectActiveFormats();
            return;
        }

        // Create and send text message via ViewModel
        TextMessage textMessage = viewModel.getTextMessage(finalText.trim());
        if (textMessage == null) {
            if (onError != null) {
                onError.onError(new IllegalStateException("No user or group set"));
            }
            return;
        }

        // Handle reply message - set quoted message ID
        if (quoteMessage != null) {
            textMessage.setQuotedMessage(quoteMessage);
            textMessage.setQuotedMessageId(quoteMessage.getId());
            quoteMessage = null;
            viewModel.onMessageReply(textMessage);
        }

        // Process mentions before sending
        handleMessagePreSend(textMessage);
        // Append consumed mention users (mentions inside code blocks)
        appendConsumedMentionUsers(textMessage, consumedMentionUsers);
        // Store code-block mention users in metadata
        storeCodeBlockMentionsInMetadata(textMessage, consumedMentionUsers);

        // Set AI generating state for agent chat
        if (isAgentChat) {
            updateComposerState(true);
        }

        // Send the message via ViewModel
        viewModel.sendTextMessage(textMessage);

        // Clear the input after sending
        clearText();
        
        // Clear pending formats after sending and reset toolbar
        if (formatSpanWatcher != null) {
            formatSpanWatcher.clearPendingFormats();
        }
        detectActiveFormats();
    }

    /**
     * Retrieves the processed text from the input field, replacing any
     * non-editable spans (mentions) with their underlying text format.
     *
     * @return A String representing the processed text with mentions converted
     *         to their underlying format (e.g., {@code <@uid:user123>}).
     */
    private String getProcessedText() {
        Editable editableText = binding.inputField.getText();
        if (editableText == null) {
            return "";
        }
        NonEditableSpan[] spans = editableText.getSpans(0, editableText.length(), NonEditableSpan.class);

        // Process spans in reverse order to maintain correct indices
        for (int i = spans.length - 1; i >= 0; i--) {
            NonEditableSpan span = spans[i];
            int spanStart = editableText.getSpanStart(span);
            int spanEnd = editableText.getSpanEnd(span);

            if (spanStart >= 0 && spanEnd >= 0 && span.getSuggestionItem() != null) {
                // Replace the span with its underlying text
                String underlyingText = span.getSuggestionItem().getUnderlyingText();
                if (underlyingText != null) {
                    editableText.replace(spanStart, spanEnd, underlyingText);
                    
                    int newSpanEnd = spanStart + underlyingText.length();

                    // Fix: After replacing mention display text with underlying text (e.g. <@uid:...>),
                    // Android's span management may stretch adjacent RichTextFormatSpan instances
                    // (like InlineCodeFormatSpan) into the replaced mention range due to
                    // SPAN_INCLUSIVE_INCLUSIVE flags. Remove any RichTextFormatSpan that now
                    // overlaps with the mention's replaced range to prevent broken markdown output.
                    RichTextFormatSpan[] formatSpans = editableText.getSpans(
                            spanStart, newSpanEnd, RichTextFormatSpan.class);
                    for (RichTextFormatSpan formatSpan : formatSpans) {
                        int fStart = editableText.getSpanStart(formatSpan);
                        int fEnd = editableText.getSpanEnd(formatSpan);
                        
                        // Skip line-based formats — they must cover the entire line to
                        // render correctly (blockquote bar, code block background, list
                        // markers). Splitting them around a mention would break markdown
                        // output (e.g., blockquote ">" prefix would only appear for part
                        // of the line).
                        FormatType ft = formatSpan.getFormatType();
                        if (ft == FormatType.BULLET_LIST
                                || ft == FormatType.ORDERED_LIST
                                || ft == FormatType.BLOCKQUOTE
                                || ft == FormatType.CODE_BLOCK) {
                            continue;
                        }
                        
                        // Only remove spans that leaked INTO the mention range.
                        // Keep spans that legitimately cover text outside the mention.
                        if (fStart >= spanStart && fEnd <= newSpanEnd) {
                            // Span is entirely within the mention range — remove it
                            editableText.removeSpan(formatSpan);
                        } else if (fStart < spanStart && fEnd > spanStart && fEnd <= newSpanEnd) {
                            // Span starts before mention but ends inside it — truncate to mention start
                            editableText.setSpan(formatSpan, fStart, spanStart,
                                    editableText.getSpanFlags(formatSpan));
                        } else if (fStart >= spanStart && fStart < newSpanEnd && fEnd > newSpanEnd) {
                            // Span starts inside mention but ends after it — move start to after mention
                            editableText.setSpan(formatSpan, newSpanEnd, fEnd,
                                    editableText.getSpanFlags(formatSpan));
                        } else if (fStart < spanStart && fEnd > newSpanEnd) {
                            // Span wraps the entire mention — split around it
                            int origFlags = editableText.getSpanFlags(formatSpan);
                            editableText.removeSpan(formatSpan);
                            // Re-apply to portion before mention
                            if (spanStart > fStart) {
                                editableText.setSpan(formatSpan, fStart, spanStart, origFlags);
                            }
                            // Re-apply new span to portion after mention
                            if (fEnd > newSpanEnd) {
                                RichTextFormatSpan afterSpan = RichTextSpanManager.createNewSpanForFormat(ft);
                                if (afterSpan != null) {
                                    editableText.setSpan(afterSpan, newSpanEnd, fEnd, origFlags);
                                }
                            }
                        }
                    }
                }
            }
        }

        return editableText.toString();
    }

    /**
     * Processes the message before sending, applying all registered text formatters.
     * This allows formatters (like mentions) to set their data on the message.
     *
     * @param baseMessage The message to be processed before sending.
     */
    private void handleMessagePreSend(BaseMessage baseMessage) {
        for (CometChatTextFormatter formatter : cometchatTextFormatterHashMap.values()) {
            if (formatter != null) {
                formatter.handlePreMessageSend(getContext(), baseMessage);
                formatter.setSuggestionItemList(new ArrayList<>());
            }
        }
    }

    /**
     * Appends consumed mention users (from code blocks) to the message's
     * mentioned users list, avoiding duplicates. This ensures the SDK stores
     * the mention metadata even though the text uses display names inside
     * code blocks.
     */
    private void appendConsumedMentionUsers(BaseMessage message, List<User> consumedUsers) {
        if (consumedUsers == null || consumedUsers.isEmpty()) {
            return;
        }
        List<User> existing = message.getMentionedUsers();
        if (existing == null) {
            existing = new ArrayList<>();
        }
        java.util.Set<String> existingUids = new java.util.HashSet<>();
        for (User u : existing) {
            if (u != null && u.getUid() != null) existingUids.add(u.getUid());
        }
        for (User cu : consumedUsers) {
            if (cu != null && cu.getUid() != null && !existingUids.contains(cu.getUid())) {
                existing.add(cu);
            }
        }
        message.setMentionedUsers(existing);
    }

    /**
     * Stores code-block mention users in the message metadata so they survive
     * the server round-trip. The CometChat server only persists mentionedUsers
     * when the text contains {@code <@uid:>} patterns. Mentions inside code blocks
     * use display names, so the server ignores them. This method stores the user
     * data in metadata under the key {@code "codeBlockMentions"}.
     */
    private void storeCodeBlockMentionsInMetadata(BaseMessage message, List<User> consumedUsers) {
        if (consumedUsers == null || consumedUsers.isEmpty()) return;
        try {
            JSONObject metadata = message.getMetadata();
            if (metadata == null) {
                metadata = new JSONObject();
            }
            JSONArray mentionsArray = new JSONArray();
            for (User u : consumedUsers) {
                if (u != null) {
                    mentionsArray.put(u.toJson());
                }
            }
            metadata.put("codeBlockMentions", mentionsArray);
            message.setMetadata(metadata);
        } catch (Exception e) {
            // silently ignore
        }
    }

    /**
     * Reads code-block mention users from the message metadata.
     * Returns an empty list if no code-block mentions are stored.
     */
    private List<User> getCodeBlockMentionsFromMetadata(BaseMessage message) {
        List<User> users = new ArrayList<>();
        try {
            JSONObject metadata = message.getMetadata();
            if (metadata != null && metadata.has("codeBlockMentions")) {
                JSONArray mentionsArray = metadata.getJSONArray("codeBlockMentions");
                for (int i = 0; i < mentionsArray.length(); i++) {
                    JSONObject userJson = mentionsArray.getJSONObject(i);
                    User user = User.fromJson(userJson.toString());
                    if (user != null && user.getUid() != null) {
                        users.add(user);
                    }
                }
            }
        } catch (Exception e) {
            // silently ignore
        }
        return users;
    }

    /**
     * Checks if the text has meaningful content for sending.
     * <p>
     * This method strips zero-width spaces (used as placeholders for code formatting)
     * and other invisible characters before checking if the text is empty.
     * </p>
     *
     * @param text The text to check.
     * @return true if the text has meaningful content, false otherwise.
     */
    private boolean hasMeaningfulContent(@Nullable String text) {
        if (text == null) {
            return false;
        }
        // Remove zero-width spaces (U+200B) used as placeholders for code formatting
        // Also remove other zero-width characters that might be used
        String stripped = text
                .replace("\u200B", "")  // Zero-width space
                .replace("\u200C", "")  // Zero-width non-joiner
                .replace("\u200D", "")  // Zero-width joiner
                .replace("\uFEFF", ""); // Zero-width no-break space (BOM)
        return !stripped.trim().isEmpty();
    }

    /**
     * Handles send button state based on current text and edit mode.
     * Also animates sticker and voice recording button visibility.
     * This follows the same pattern as CometChatMessageComposer.
     *
     * @param currentText The current text in the input field.
     */
    private void handleSendButtonState(String currentText) {
        boolean hasText = hasMeaningfulContent(currentText);

        if (isAgentChat) {
            // In agent chat mode, skip secondary button animations
            // and use simple active/inactive based on text presence
            if (!isAIAssistantGenerating) {
                updateSendButtonState(hasText);
            }
            return;
        }

        // Animate sticker and voice recording buttons based on text presence
        updateSecondaryButtonVisibility(hasText);

        if (!hasText) {
            updateSendButtonState(false);
            return;
        }
        
        // Check if we're in edit mode
        TextMessage editMessage = viewModel != null ? viewModel.getEditingMessage().getValue() : null;
        if (editMessage != null) {
            handleEditMessageState(currentText.trim(), editMessage);
        } else {
            handleNewMessageState(currentText.trim());
        }
    }

    // Tracks whether the sticker button has been relocated to the mic position
    private boolean stickerRelocatedToMicPosition = false;

    /**
     * Animates the sticker and voice recording buttons when text changes.
     * <p>
     * When text is entered:
     * <ul>
     *   <li>Mic slides out to the right</li>
     *   <li>Sticker slides from its original position to the mic's position</li>
     * </ul>
     * When text is cleared:
     * <ul>
     *   <li>Sticker slides back to its original position</li>
     *   <li>Mic slides in from the right</li>
     * </ul>
     *
     * @param hasText true if the input field contains meaningful text.
     */
    private void updateSecondaryButtonVisibility(boolean hasText) {
        if (hasText) {
            if (stickerRelocatedToMicPosition) {
                return;
            }
            stickerRelocatedToMicPosition = true;

            boolean micVisible = voiceRecordingButtonVisibility == View.VISIBLE
                    && binding.btnVoiceRecording.getVisibility() == View.VISIBLE;
            boolean stickerVisible = stickerButtonVisibility == View.VISIBLE
                    && binding.btnSticker.getVisibility() == View.VISIBLE;

            int[] micFromLoc = new int[2];
            if (micVisible) {
                binding.btnVoiceRecording.getLocationOnScreen(micFromLoc);
            }

            // Slide mic out from its original position (correcting for layout shift)
            if (micVisible) {
                final int origMicX = micFromLoc[0];
                binding.btnVoiceRecording.post(() -> {
                    int[] micNowLoc = new int[2];
                    binding.btnVoiceRecording.getLocationOnScreen(micNowLoc);
                    float layoutShift = origMicX - micNowLoc[0];
                    binding.btnVoiceRecording.setTranslationX(layoutShift);
                    binding.btnVoiceRecording.animate()
                        .translationX(layoutShift + binding.btnVoiceRecording.getWidth())
                        .setDuration(400)
                        .setInterpolator(new android.view.animation.AccelerateInterpolator())
                        .withEndAction(() -> {
                            binding.btnVoiceRecording.setVisibility(View.GONE);
                            binding.btnVoiceRecording.setTranslationX(0f);
                        })
                        .start();
                });
            }

            if (stickerVisible) {
                slideStickerToMicPosition();
            }
        } else {
            if (!stickerRelocatedToMicPosition) {
                return;
            }
            stickerRelocatedToMicPosition = false;

            boolean stickerAtMic = binding.btnSticker.getParent() == binding.inputRow;
            if (stickerAtMic && stickerButtonVisibility == View.VISIBLE) {
                slideStickerToOriginalPosition();
                if (voiceRecordingButtonVisibility == View.VISIBLE) {
                    slideIn(binding.btnVoiceRecording);
                }
            } else {
                if (stickerButtonVisibility == View.VISIBLE
                        && binding.btnSticker.getVisibility() != View.VISIBLE) {
                    fadeIn(binding.btnSticker);
                }
                if (voiceRecordingButtonVisibility == View.VISIBLE
                        && binding.btnVoiceRecording.getVisibility() != View.VISIBLE) {
                    slideIn(binding.btnVoiceRecording);
                }
            }
        }
    }

    /**
     * Moves the sticker button to the mic's position in the layout tree (no animation).
     */
    private void moveStickerToMicPosition() {
        if (binding.btnSticker.getParent() != null) {
            ((ViewGroup) binding.btnSticker.getParent()).removeView(binding.btnSticker);
        }
        int sendIndex = binding.inputRow.indexOfChild(binding.sendButtonCard);
        if (sendIndex < 0) {
            sendIndex = binding.inputRow.getChildCount();
        }
        binding.inputRow.addView(binding.btnSticker, sendIndex);
    }

    /**
     * Moves the sticker button back to its original container (no animation).
     */
    private void moveStickerToOriginalPosition() {
        if (binding.btnSticker.getParent() != null) {
            ((ViewGroup) binding.btnSticker.getParent()).removeView(binding.btnSticker);
        }
        binding.auxiliaryButtonsContainer.addView(binding.btnSticker, 0);
    }

    /**
     * Slides the sticker button from its current position to the mic button's position.
     * Captures screen coordinates before and after the layout move, then animates
     * the translation so the sticker appears to glide rightward.
     */
    private void slideStickerToMicPosition() {
        int[] fromLoc = new int[2];
        binding.btnSticker.getLocationOnScreen(fromLoc);
        final int fromX = fromLoc[0];

        binding.btnSticker.setVisibility(View.INVISIBLE);
        moveStickerToMicPosition();

        // Use OnPreDrawListener to set translation BEFORE the first draw at the new position
        binding.btnSticker.getViewTreeObserver().addOnPreDrawListener(
            new android.view.ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    binding.btnSticker.getViewTreeObserver().removeOnPreDrawListener(this);

                    int[] toLoc = new int[2];
                    binding.btnSticker.getLocationOnScreen(toLoc);
                    final int toX = toLoc[0];

                    float deltaX = fromX - toX;
                    binding.btnSticker.setTranslationX(deltaX);
                    binding.btnSticker.setVisibility(View.VISIBLE);
                    binding.btnSticker.setAlpha(1f);
                    binding.btnSticker.animate()
                        .translationX(0f)
                        .setDuration(350)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();

                    return false; // skip this draw frame, next frame will have correct offset
                }
            });
    }

    /**
     * Slides the sticker button from the mic position back to its original position.
     */
    private void slideStickerToOriginalPosition() {
        int[] fromLoc = new int[2];
        binding.btnSticker.getLocationOnScreen(fromLoc);
        final int fromX = fromLoc[0];

        binding.btnSticker.setVisibility(View.INVISIBLE);
        moveStickerToOriginalPosition();

        binding.btnSticker.getViewTreeObserver().addOnPreDrawListener(
            new android.view.ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    binding.btnSticker.getViewTreeObserver().removeOnPreDrawListener(this);

                    int[] toLoc = new int[2];
                    binding.btnSticker.getLocationOnScreen(toLoc);
                    final int toX = toLoc[0];

                    float deltaX = fromX - toX;
                    binding.btnSticker.setTranslationX(deltaX);
                    binding.btnSticker.setVisibility(View.VISIBLE);
                    binding.btnSticker.setAlpha(1f);
                    binding.btnSticker.animate()
                        .translationX(0f)
                        .setDuration(500)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();

                    return false;
                }
            });
    }

    /**
     * Slides a view in from the right.
     *
     * @param view The view to animate in.
     */
    private void slideIn(View view) {
        view.setTranslationX(view.getWidth());
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .translationX(0f)
            .setDuration(350)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .start();
    }

    /**
     * Sets a view to VISIBLE and fades it in.
     *
     * @param view The view to animate in.
     */
    private void fadeIn(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(250)
            .start();
    }

    /**
     * Handles send button state when editing a message.
     * Compares current text with the formatted original text.
     *
     * @param currentText The current text in the input field.
     * @param editMessage The message being edited.
     */
    private void handleEditMessageState(String currentText, TextMessage editMessage) {
        // Get the formatted original text by:
        // 1. First parsing markdown (converts [text](url) to visual text)
        // 2. Then applying text formatters (converts mention markers to display names)
        String originalText = editMessage.getText();
        SpannableStringBuilder formattedBuilder;
        if (enableRichTextFormatting && originalText != null) {
            // Parse markdown first
            SpannableString parsedMarkdown = MarkdownConverter.fromMarkdown(originalText);
            formattedBuilder = new SpannableStringBuilder(parsedMarkdown);
        } else {
            formattedBuilder = new SpannableStringBuilder(originalText != null ? originalText : "");
        }
        // Then apply text formatters (for mentions)
        for (CometChatTextFormatter textFormatter : cometchatTextFormatters) {
            if (textFormatter != null) {
                formattedBuilder = textFormatter.prepareMessageString(
                        getContext(),
                        editMessage,
                        formattedBuilder,
                        null,
                        UIKitConstants.FormattingType.MESSAGE_COMPOSER
                );
            }
        }
        String formattedText = formattedBuilder.toString().trim();

        if (currentText.equals(formattedText)) {
            updateSendButtonState(false);
        } else {
            updateSendButtonState(true);
        }
    }

    /**
     * Handles send button state for new messages.
     *
     * @param currentText The current text in the input field.
     */
    private void handleNewMessageState(String currentText) {
        // Activate button only if text has meaningful content
        updateSendButtonState(hasMeaningfulContent(currentText));
    }

    /**
     * Updates send button state.
     *
     * @param active true if active.
     */
    private void updateSendButtonState(boolean active) {
        // Don't override the stop button state while AI is generating
        if (isAgentChat && isAIAssistantGenerating) return;

        // Enable/disable the send button based on active state
        binding.btnSend.setEnabled(active);
        binding.btnSend.setClickable(active);
        binding.sendButtonCard.setEnabled(active);
        binding.sendButtonCard.setClickable(active);

        if (active) {
            if (activeSendButtonDrawable != null) {
                binding.btnSend.setImageDrawable(activeSendButtonDrawable);
            } else {
                if (isAgentChat) {
                    binding.sendButtonCard.setCardBackgroundColor(CometChatTheme.getSecondaryButtonBackgroundColor(getContext()));
                    binding.btnSend.setImageResource(R.drawable.cometchat_ic_arrow_narrow_up);
                    binding.btnSend.setImageTintList(ColorStateList.valueOf(CometChatTheme.getIconTintWhite(getContext())));
                } else {
                    binding.btnSend.setImageResource(R.drawable.cometchat_ic_send_active);
                    binding.btnSend.setImageTintList(null);
                }
            }
            if (!isAgentChat) {
                binding.sendButtonCard.setCardBackgroundColor(CometChatTheme.getPrimaryColor(getContext()));
            }
        } else {
            if (inactiveSendButtonDrawable != null) {
                binding.btnSend.setImageDrawable(inactiveSendButtonDrawable);
            } else {
                if (isAgentChat) {
                    binding.btnSend.setImageResource(R.drawable.cometchat_ic_arrow_narrow_up);
                    binding.btnSend.setImageTintList(null);
                } else {
                    binding.btnSend.setImageResource(R.drawable.cometchat_ic_send_inactive);
                }
            }
            binding.sendButtonCard.setCardBackgroundColor(CometChatTheme.getBackgroundColor4(getContext()));
        }
    }

    /**
     * Updates the composer state when AI assistant is generating or has finished.
     * Shows a stop button during generation, restores send button when done.
     */
    private void updateComposerState(Boolean generating) {
        this.isAIAssistantGenerating = generating;
        if (generating) {
            stopSendButton();
        } else {
            Editable editable = binding.inputField.getText();
            if (editable == null || editable.toString().isEmpty()) {
                updateSendButtonState(false);
            } else {
                updateSendButtonState(true);
            }
        }
    }

    /**
     * Shows a stop button on the send button during AI generation.
     */
    private void stopSendButton() {
        binding.sendButtonCard.setCardBackgroundColor(CometChatTheme.getSecondaryButtonBackgroundColor(getContext()));
        binding.btnSend.setImageResource(R.drawable.cometchat_ic_stop);
        binding.btnSend.setImageTintList(ColorStateList.valueOf(CometChatTheme.getIconTintWhite(getContext())));
        binding.btnSend.setClickable(false);
        binding.btnSend.setEnabled(false);
    }

    /**
     * Updates toolbar visibility based on ViewModel.
     *
     * @param visible true if visible.
     */
    private void updateToolbarVisibility(boolean visible) {
        isToolbarVisible = visible;
        binding.richTextToolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Updates active formats in the toolbar.
     *
     * @param formats The set of active formats.
     */
    private void updateActiveFormats(Set<FormatType> formats) {
        // Update the toolbar's active formats display
        binding.richTextToolbar.setActiveFormats(formats);
    }

    /**
     * Updates edit mode UI.
     * Note: The actual edit preview is now shown via messagePreview component in setEditMessage().
     * This method is kept for ViewModel observer compatibility but no longer shows editPreviewLayout.
     *
     * @param message The message being edited.
     */
    private void updateEditMode(TextMessage message) {
        // Edit preview is now handled by messagePreview component in setEditMessage()
        // Keep editPreviewLayout hidden - it's no longer used for edit preview
        binding.editPreviewLayout.getRoot().setVisibility(View.GONE);
    }

    /**
    /**
     * Updates button visibilities.
     */
    private void updateButtonVisibilities() {
        binding.btnAttachment.setVisibility(attachmentButtonVisibility);
        binding.btnVoiceRecording.setVisibility(voiceRecordingButtonVisibility);
        binding.btnSticker.setVisibility(stickerButtonVisibility);
        binding.btnAi.setVisibility(aiButtonVisibility);
        binding.sendButtonCard.setVisibility(sendButtonVisibility);
    }

    /**
     * Updates toolbar configuration based on current settings.
     */
    private void updateToolbarConfiguration() {
        // If rich text formatting is disabled, hide the toolbar
        if (!enableRichTextFormatting) {
            hideRichTextToolbar();
            return;
        }

        // Show or hide toolbar based on richTextFormattingOptionsVisibility
        if (richTextFormattingOptionsVisibility == View.VISIBLE) {
            showRichTextToolbar();
        } else {
            hideRichTextToolbar();
        }
    }

    /**
     * Detects active formats at cursor position.
     * <p>
     * For WYSIWYG editing, this method uses RichTextSpanManager to detect
     * active formats by examining spans at the cursor position, rather than
     * parsing markdown syntax.
     * </p>
     * <p>
     * Also includes pending formats (formats that will be applied to newly
     * typed text) in the active formats set, and excludes explicitly disabled
     * formats (formats the user has turned off while inside formatted text).
     * </p>
     */
    private void detectActiveFormats() {
        Editable editable = binding.inputField.getText();
        if (editable == null) {
            return;
        }
        
        int cursorPosition = binding.inputField.getSelectionStart();
        int selEnd = binding.inputField.getSelectionEnd();
        
        // Use span-based format detection for WYSIWYG editing
        Set<FormatType> formats = RichTextSpanManager.detectActiveFormats(editable, cursorPosition);
        
        // When there's a selection, also check formats at the end of the selection
        // to ensure toolbar reflects formats that cover the entire selection range
        if (selEnd != cursorPosition) {
            Set<FormatType> formatsAtEnd = RichTextSpanManager.detectActiveFormats(editable, selEnd);
            formats.addAll(formatsAtEnd);
        }
        
        // Handle pending and explicitly disabled formats
        if (formatSpanWatcher != null) {
            // Add pending formats (formats that will be applied to newly typed text)
            formats.addAll(formatSpanWatcher.getPendingFormats());
            
            // Remove explicitly disabled formats (formats user has turned off)
            // This prevents re-selecting a format button when user has explicitly deselected it
            for (FormatType disabledFormat : formatSpanWatcher.getExplicitlyDisabledFormats()) {
                formats.remove(disabledFormat);
            }
        }
        
        // Hide cursor when code block is active, show it otherwise
        boolean codeBlockActive = formats.contains(FormatType.CODE_BLOCK);
        binding.inputField.setCursorVisible(!codeBlockActive);
        
        if (viewModel != null) {
            viewModel.setActiveFormats(formats);
        }
    }

    /**
     * Determines if an inline format should continue on a new line.
     * <p>
     * BOLD, ITALIC, UNDERLINE, and STRIKETHROUGH formats should continue
     * on the new line to maintain formatting continuity when the user
     * presses Enter while these formats are active.
     * </p>
     *
     * @param formatType The format type to check.
     * @return true if the format should continue on the new line, false otherwise.
     */
    private boolean shouldContinueInlineFormatOnNewLine(FormatType formatType) {
        if (formatType == null) {
            return false;
        }
        
        switch (formatType) {
            case BOLD:
            case ITALIC:
            case STRIKETHROUGH:
            case UNDERLINE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if the character immediately before the cursor position is an emoji.
     * This is used to preserve pending text style formats when the cursor moves
     * due to emoji insertion, since emojis don't receive formatting spans.
     *
     * @param editable The editable text.
     * @param cursorPos The current cursor position.
     * @return true if the character before the cursor is an emoji.
     */
    private boolean isCharBeforeCursorEmoji(Editable editable, int cursorPos) {
        if (editable == null || cursorPos <= 0 || cursorPos > editable.length()) {
            return false;
        }
        String text = editable.toString();
        // Get the code point before the cursor (handle surrogate pairs)
        int codePoint = Character.codePointBefore(text, cursorPos);
        return RichTextSpanManager.isEmojiCodePoint(codePoint) ||
               RichTextSpanManager.isEmojiModifierOrJoiner(codePoint);
    }

    /**
     * Hides the mention limit banner.
     */
    private void hideMentionLimitBanner() {
        binding.mentionLimitBanner.setVisibility(View.GONE);
    }

    /**
     * Resets the mention count.
     */
    private void resetMentionCount() {
        hideMentionLimitBanner();
    }

    // ==================== Attachment Sheet Methods ====================

    /**
     * Opens the attachment option sheet.
     */
    private void openAttachmentOptionSheet() {
        try {
            setComposerActions();
            CometChatAttachmentOptionSheet cometchatAttachmentOptionSheet = new CometChatAttachmentOptionSheet(getContext());
            cometchatAttachmentOptionSheet.setAttachmentOptionItems(attachmentOptionSheetMenuItems);
            if (attachmentOptionSheetStyle != 0) {
                cometchatAttachmentOptionSheet.setStyle(attachmentOptionSheetStyle);
            }
            cometchatAttachmentOptionSheet.setAttachmentOptionClickListener(menuItem -> {
                CometChatMessageComposerAction action = actionHashMap.get(menuItem.getId());
                if (action != null) {
                    if (action.getOnClick() != null) {
                        action.getOnClick().onClick();
                        bottomSheetDialog.dismiss();
                    } else {
                        // Fallback to default handling for standard attachment options
                        handleAttachmentOptionItemClick(menuItem);
                        bottomSheetDialog.dismiss();
                    }
                } else {
                    // Action not found in hashmap - still try default handling
                    CometChatLogger.e(TAG, "openAttachmentOptionSheet: action is null for " + menuItem.getText());
                    handleAttachmentOptionItemClick(menuItem);
                    bottomSheetDialog.dismiss();
                }
            });
            showBottomSheet(bottomSheetDialog, true, cometchatAttachmentOptionSheet);
        } catch (Exception e) {
            CometChatLogger.e(TAG, "Error opening attachment sheet: " + e.getMessage());
        }
    }

    /**
     * Handles the default actions based on the action item ID when no onClick is defined.
     * This provides fallback handling for standard attachment options like camera, image,
     * video, audio, and document.
     *
     * @param actionItem The action item clicked in the attachment option sheet.
     */
    private void handleAttachmentOptionItemClick(OptionSheetMenuItem actionItem) {
        switch (actionItem.getId()) {
            case UIKitConstants.ComposerAction.CAMERA:
                RESULT_TO_BE_OPEN = actionItem.getId();
                requestCameraPermission();
                break;
            case UIKitConstants.ComposerAction.IMAGE:
            case UIKitConstants.ComposerAction.VIDEO:
            case UIKitConstants.ComposerAction.AUDIO:
            case UIKitConstants.ComposerAction.DOCUMENT:
                RESULT_TO_BE_OPEN = actionItem.getId();
                requestStoragePermission();
                break;
            default:
                RESULT_TO_BE_OPEN = "";
                break;
        }
    }

    /**
     * Sets up the composer actions for the attachment option sheet.
     * Uses the ViewModel's idMap for proper key mapping, following the same
     * pattern as CometChatMessageComposer.
     */
    private void setComposerActions() {
        attachmentOptionSheetMenuItems.clear();
        List<CometChatMessageComposerAction> tempMessageComposerActions;
        if (messageComposerActions == null || messageComposerActions.isEmpty()) {
            // Use ViewModel's idMap which has proper UIKitConstants.MapId keys
            HashMap<String, String> idMap = viewModel != null ? viewModel.getIdMap() : new HashMap<>();
            tempMessageComposerActions = CometChatUIKit
                    .getDataSource()
                    .getAttachmentOptions(getContext(), user, group, idMap, additionParameter);
        } else {
            tempMessageComposerActions = messageComposerActions;
        }

        for (CometChatMessageComposerAction option : tempMessageComposerActions) {
            if (option != null) {
                actionHashMap.put(option.getId(), option);
                attachmentOptionSheetMenuItems.add(new OptionSheetMenuItem(
                        option.getId(),
                        option.getIcon(),
                        option.getIconTintColor(),
                        option.getIconBackground(),
                        option.getTitle(),
                        option.getTitleFont(),
                        option.getTitleAppearance(),
                        option.getTitleColor(),
                        option.getBackground(),
                        option.getCornerRadius()));
            }
        }
    }

    /**
     * Sets the attachment options for the composer.
     *
     * @param actions The list of composer actions.
     */
    public void setAttachmentOptions(List<CometChatMessageComposerAction> actions) {
        if (actions != null) {
            this.messageComposerActions = actions;
        }
    }

    // ==================== Voice Recording Methods ====================

    /**
     * Handles the voice recording button click.
     * Shows the inline audio recorder if enabled, otherwise opens the legacy bottom sheet.
     */
    private void onVoiceRecordingButtonClick() {
        showInlineAudioRecorder();
    }

    /**
     * Shows the inline audio recorder and hides the input row.
     * The recorder will automatically request microphone permission and start recording.
     */
    private void showInlineAudioRecorder() {
        if (binding.inlineAudioRecorder == null) return;

        // Hide input row and rich text toolbar
        binding.inputRow.setVisibility(View.GONE);
        binding.richTextToolbar.setVisibility(View.GONE);

        // Show inline audio recorder
        binding.inlineAudioRecorder.setVisibility(View.VISIBLE);

        // Set lifecycle owner for ViewModel observation
        if (lifecycleOwner != null) {
            binding.inlineAudioRecorder.setLifecycleOwner(lifecycleOwner);
        }

        // Apply style if set
        if (inlineAudioRecorderStyle != 0) {
            binding.inlineAudioRecorder.setStyle(inlineAudioRecorderStyle);
        }

        // Apply custom icons if set
        if (recorderDeleteButtonIcon != null) {
            binding.inlineAudioRecorder.setDeleteIcon(recorderDeleteButtonIcon);
        }
        if (recorderSendButtonIcon != null) {
            binding.inlineAudioRecorder.setSendIcon(recorderSendButtonIcon);
        }

        // Set submit listener with waveform data to send audio message and hide recorder
        binding.inlineAudioRecorder.setOnSubmitWithWaveformListener((filePath, amplitudes) -> {
            if (filePath != null) {
                File audioFile = new File(filePath);
                if (audioFile.exists()) {
                    sendMediaMessageWithWaveform(audioFile, UIKitConstants.MessageType.AUDIO, amplitudes);
                    
                    // Notify callback
                    if (onVoiceRecording != null) {
                        onVoiceRecording.onRecordingEnd(audioFile);
                    }
                }
            }
            // Hide recorder without cancelling (file already submitted)
            hideInlineAudioRecorderWithoutCancel();
        });

        // Set cancel listener to hide recorder
        binding.inlineAudioRecorder.setOnCancelListener(this::hideInlineAudioRecorder);

        // Set error listener
        binding.inlineAudioRecorder.setOnErrorListener(e -> {
            if (onError != null) {
                onError.onError(e);
            }
            hideInlineAudioRecorder();
        });

        // Update composer state
        if (viewModel != null) {
            viewModel.setInlineRecordingMode(true);
        }

        // Notify callback that recording started
        if (onVoiceRecording != null) {
            onVoiceRecording.onRecordingStart();
        }

        // Start recording (will request permission if needed)
        binding.inlineAudioRecorder.startRecording();
    }

    /**
     * Hides the inline audio recorder and shows the input row.
     * Cancels any active recording.
     */
    private void hideInlineAudioRecorder() {
        if (binding.inlineAudioRecorder == null) return;

        // Cancel any active recording
        binding.inlineAudioRecorder.cancelRecording();

        // Hide inline audio recorder
        binding.inlineAudioRecorder.setVisibility(View.GONE);

        // Show input row
        binding.inputRow.setVisibility(View.VISIBLE);
        
        // Restore rich text toolbar if it was visible before
        if (isToolbarVisible) {
            binding.richTextToolbar.setVisibility(View.VISIBLE);
        }

        // Update composer state
        if (viewModel != null) {
            viewModel.setInlineRecordingMode(false);
        }
    }

    /**
     * Hides the inline audio recorder without cancelling the recording.
     * Used after successful submission to avoid deleting the file.
     */
    private void hideInlineAudioRecorderWithoutCancel() {
        if (binding.inlineAudioRecorder == null) return;

        // Reset the recorder state for next recording (clears waveform, etc.)
        binding.inlineAudioRecorder.reset();

        // Hide inline audio recorder (don't cancel - file was already submitted)
        binding.inlineAudioRecorder.setVisibility(View.GONE);

        // Show input row
        binding.inputRow.setVisibility(View.VISIBLE);
        
        // Restore rich text toolbar if it was visible before
        if (isToolbarVisible) {
            binding.richTextToolbar.setVisibility(View.VISIBLE);
        }

        // Update composer state
        if (viewModel != null) {
            viewModel.setInlineRecordingMode(false);
        }
    }

    /**
     * Requests microphone permission from the user.
     */
    public void requestMicrophonePermission() {
        permissionHandlerBuilder.withPermissions(microPhonePermissions).check();
    }

    /**
     * Opens the media recorder sheet for recording audio messages.
     */
    private void openMediaRecorderSheet() {
        try {
            CometChatMediaRecorder cometchatMediaRecorder = new CometChatMediaRecorder(getContext());
            if (mediaRecorderStyle != 0) {
                cometchatMediaRecorder.setStyle(mediaRecorderStyle);
            }
            cometchatMediaRecorder.setOnCloseClickListener(bottomSheetDialog::dismiss);
            cometchatMediaRecorder.startRecording();
            cometchatMediaRecorder.setOnSubmitClickListener((file, context) -> {
                if (file != null) {
                    sendMediaMessage(file, UIKitConstants.MessageType.AUDIO);
                    
                    // Notify callback
                    if (onVoiceRecording != null) {
                        onVoiceRecording.onRecordingEnd(file);
                    }
                }
                cometchatMediaRecorder.stopRecording();
                bottomSheetDialog.dismiss();
            });
            showBottomSheet(bottomSheetDialog, true, cometchatMediaRecorder);
            
            // Notify callback that recording started
            if (onVoiceRecording != null) {
                onVoiceRecording.onRecordingStart();
            }
        } catch (Exception e) {
            CometChatLogger.e(TAG, "Error opening media recorder: " + e.getMessage());
        }
    }

    /**
     * Sends a media message.
     * Delegates message sending to the ViewModel following MVVM pattern.
     *
     * @param file        The media file to send.
     * @param contentType The content type of the media.
     */
    public void sendMediaMessage(File file, String contentType) {
        if (viewModel == null) return;
        
        if (file == null || !file.exists()) {
            if (onError != null) {
                onError.onError(new IllegalArgumentException("Invalid file"));
            }
            return;
        }

        // Create media message via ViewModel
        MediaMessage mediaMessage = viewModel.getMediaMessage(file, contentType);
        if (mediaMessage == null) {
            if (onError != null) {
                onError.onError(new IllegalStateException("No user or group set"));
            }
            return;
        }

        // Handle reply message - set quoted message ID
        if (quoteMessage != null) {
            mediaMessage.setQuotedMessage(quoteMessage);
            mediaMessage.setQuotedMessageId(quoteMessage.getId());
            quoteMessage = null;
            viewModel.onMessageReply(mediaMessage);
        }

        // Send the message via ViewModel
        viewModel.sendMediaMessage(mediaMessage);
    }
    
    /**
     * Sends a media message with waveform amplitude data stored in metadata.
     * Used for audio messages recorded with the inline audio recorder.
     *
     * @param file The media file to send.
     * @param contentType The content type of the media.
     * @param amplitudes The list of amplitude values for waveform visualization.
     */
    public void sendMediaMessageWithWaveform(File file, String contentType, List<Float> amplitudes) {
        if (viewModel == null) return;
        
        if (file == null || !file.exists()) {
            if (onError != null) {
                onError.onError(new IllegalArgumentException("Invalid file"));
            }
            return;
        }

        // Create media message via ViewModel
        MediaMessage mediaMessage = viewModel.getMediaMessageWithWaveform(file, contentType, amplitudes);
        if (mediaMessage == null) {
            if (onError != null) {
                onError.onError(new IllegalStateException("No user or group set"));
            }
            return;
        }

        // Handle reply message - set quoted message ID
        if (quoteMessage != null) {
            mediaMessage.setQuotedMessage(quoteMessage);
            mediaMessage.setQuotedMessageId(quoteMessage.getId());
            quoteMessage = null;
            viewModel.onMessageReply(mediaMessage);
        }

        // Send the message via ViewModel
        viewModel.sendMediaMessage(mediaMessage);
    }

    // ==================== Sticker/Emoji Keyboard Methods ====================

    /**
     * Toggles the sticker keyboard visibility using the extension decorator pattern.
     * This uses CometChatUIKitHelper.showPanel/hidePanel to show the sticker keyboard
     * in the bottom panel, following the same pattern as CometChatMessageComposer.
     */
    private void toggleStickerKeyboard() {
        if (viewModel == null) return;
        
        if (isStickerKeyboardVisible) {
            hideStickerKeyboard();
        } else {
            showStickerKeyboard();
        }
    }

    /**
     * Shows the sticker keyboard using the extension decorator pattern.
     * The sticker keyboard is obtained from the DataSource via ChatConfigurator,
     * which uses the StickerExtensionDecorator to provide the keyboard with
     * proper sticker sending functionality.
     */
    public void showStickerKeyboard() {
        if (viewModel == null) return;
        
        try {
            // Hide soft keyboard first
            Utils.hideKeyBoard(getContext(), binding.inputField);
            
            // Clear input field focus
            binding.inputField.clearFocus();
            
            // Get the auxiliary option view from the DataSource (which includes sticker keyboard)
            // This uses the extension decorator pattern - StickerExtensionDecorator provides
            // the sticker icon and keyboard with proper message sending logic
            AdditionParameter additionParameter = new AdditionParameter();
            additionParameter.setStickersButtonVisibility(View.VISIBLE);
            additionParameter.setInactiveStickerIcon(getContext().getDrawable(R.drawable.cometchat_ic_sticker));
            additionParameter.setActiveStickerIcon(getContext().getDrawable(R.drawable.cometchat_ic_filled_sticker));
            additionParameter.setInactiveAuxiliaryIconTint(CometChatTheme.getIconTintSecondary(getContext()));
            additionParameter.setActiveAuxiliaryIconTint(CometChatTheme.getIconTintHighlight(getContext()));
            
            // Use CometChatUIKitHelper to show the panel - this triggers the showPanel event
            // which the ViewModel listens to and forwards to the View
            View stickerView = ChatConfigurator.getDataSource().getAuxiliaryOption(
                    getContext(), user, group, viewModel.getIdMap(), additionParameter);
            
            if (stickerView != null) {
                // Simulate click on the sticker icon to trigger the keyboard
                // The StickerExtensionDecorator handles showing the keyboard via CometChatUIKitHelper.showPanel
                View stickerIcon = stickerView.findViewById(R.id.iv_sticker);
                if (stickerIcon != null) {
                    stickerIcon.performClick();
                }
            }
            
            isStickerKeyboardVisible = true;
            
            // Update sticker button to show active state (filled icon with highlight tint)
            binding.btnSticker.setImageResource(R.drawable.cometchat_ic_filled_sticker);
            binding.btnSticker.setColorFilter(CometChatTheme.getIconTintHighlight(getContext()));
            
        } catch (Exception e) {
            CometChatLogger.e(TAG, "Error showing sticker keyboard: " + e.getMessage());
            if (onError != null) {
                onError.onError(e);
            }
        }
    }

    /**
     * Hides the sticker keyboard using CometChatUIKitHelper.hidePanel.
     */
    public void hideStickerKeyboard() {
        if (viewModel == null) return;
        
        // Use CometChatUIKitHelper to hide the panel
        CometChatUIKitHelper.hidePanel(viewModel.getIdMap(), UIKitConstants.CustomUIPosition.COMPOSER_BOTTOM);
        
        binding.stickerKeyboardContainer.removeAllViews();
        binding.stickerKeyboardContainer.setVisibility(View.GONE);
        isStickerKeyboardVisible = false;
        
        // Reset sticker button to inactive state (outline icon with secondary tint)
        binding.btnSticker.setImageResource(R.drawable.cometchat_ic_sticker);
        binding.btnSticker.setColorFilter(stickerIconTint);
    }

    // ==================== Bottom Sheet Helper ====================

    /**
     * Displays a bottom sheet dialog with the provided content view.
     *
     * @param bottomSheetDialog The BottomSheetDialog to use.
     * @param isCancelable      Whether the dialog can be dismissed by the user.
     * @param view              The view to display as the content of the dialog.
     */
    private void showBottomSheet(BottomSheetDialog bottomSheetDialog, boolean isCancelable, View view) {
        try {
            Utils.removeParentFromView(view);
            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.setOnShowListener(dialogInterface -> {
                View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    bottomSheet.setBackgroundResource(R.color.cometchat_color_transparent);
                }
            });
            bottomSheetDialog.setCancelable(isCancelable);
            bottomSheetDialog.show();
        } catch (Exception e) {
            CometChatLogger.e(TAG, "Error showing bottom sheet: " + e.getMessage());
        }
    }

    // ==================== Style Setters ====================

    /**
     * Sets the style for the attachment option sheet.
     *
     * @param style The style resource ID.
     */
    public void setAttachmentOptionSheetStyle(@StyleRes int style) {
        this.attachmentOptionSheetStyle = style;
    }

    /**
     * Sets the style for the media recorder.
     *
     * @param style The style resource ID.
     */
    public void setMediaRecorderStyle(@StyleRes int style) {
        this.mediaRecorderStyle = style;
    }

    /**
     * Sets the style for the inline audio recorder.
     *
     * @param style The style resource ID.
     */
    public void setInlineAudioRecorderStyle(@StyleRes int style) {
        this.inlineAudioRecorderStyle = style;
    }

    /**
     * Sets the delete button icon for the inline audio recorder.
     *
     * @param icon The drawable to use for the delete button.
     */
    public void setRecorderDeleteButtonIcon(@Nullable Drawable icon) {
        this.recorderDeleteButtonIcon = icon;
    }

    /**
     * Sets the send button icon for the inline audio recorder.
     *
     * @param icon The drawable to use for the send button.
     */
    public void setRecorderSendButtonIcon(@Nullable Drawable icon) {
        this.recorderSendButtonIcon = icon;
    }

    // ==================== Mentions Formatter Methods ====================

    /**
     * Retrieves the default CometChatMentionsFormatter from the available text formatters.
     * Following the same pattern as CometChatMessageComposer.processMentionsFormatter().
     */
    private void processMentionsFormatter() {
        List<CometChatTextFormatter> formatters = CometChatUIKit.getDataSource().getTextFormatters(
                getContext(), additionParameter);
        for (CometChatTextFormatter textFormatter : formatters) {
            if (textFormatter instanceof CometChatMentionsFormatter) {
                cometchatMentionsFormatter = (CometChatMentionsFormatter) textFormatter;
                cometchatMentionsFormatter.setDisableMentionAll(disableMentionAll);
                cometchatMentionsFormatter.setMentionAllLabel(mentionAllLabelId, mentionAllLabel);
            } else if (textFormatter instanceof CometChatRichTextFormatter) {
                if (!cometchatTextFormatters.contains(textFormatter)) {
                    cometchatTextFormatters.add(textFormatter);
                }
            }
        }
        if (cometchatMentionsFormatter != null && !cometchatTextFormatters.contains(cometchatMentionsFormatter)) {
            cometchatTextFormatters.add(cometchatMentionsFormatter);
        }
    }

    /**
     * Processes all text formatters and sets up their observers.
     * Following the same pattern as CometChatMessageComposer.processFormatters().
     */
    private void processFormatters() {
        cometchatTextFormatterHashMap = new HashMap<>();
        for (CometChatTextFormatter formatter : cometchatTextFormatters) {
            if (formatter != null) {
                LifecycleOwner owner = Utils.getLifecycleOwner(getContext());
                if (owner != null) {
                    formatter.getSuggestionItemList().observe(owner, this::setTagList);
                    formatter.getTagInfoMessage().observe(owner, this::setInfoMessage);
                    formatter.getTagInfoVisibility().observe(owner, this::setInfoVisibility);
                    formatter.getShowLoadingIndicator().observe(owner, this::setLoadingStateVisibility);
                }
                if (user != null) {
                    formatter.setUser(user);
                    formatter.setGroup(null);
                } else if (group != null) {
                    formatter.setGroup(group);
                    formatter.setUser(null);
                }
                if (formatter.getTrackingCharacter() != '\0') {
                    cometchatTextFormatterHashMap.put(formatter.getTrackingCharacter(), formatter);
                }
            }
        }
    }

    /**
     * Sets up the scroll listener for the suggestion list RecyclerView.
     * Following the same pattern as CometChatMessageComposer.
     */
    private void setupSuggestionListScrollListener() {
        binding.suggestionList.getBinding().recyclerViewSuggestionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!recyclerView.canScrollVertically(1)) {
                        // Reached bottom - load more if needed
                        if (tempTextFormatter != null) {
                            tempTextFormatter.onScrollToBottom();
                        }
                    }
                }
            }
        });
    }

    /**
     * Sets up the click listener for the suggestion list.
     * Following the same pattern as CometChatMessageComposer.
     */
    private void setupSuggestionListClickListener() {
        binding.suggestionList.setItemClickListener(new OnItemClickListener<SuggestionItem>() {
            @Override
            public void OnItemClick(SuggestionItem suggestionItem, int position) {
                if (onItemClickListener != null) {
                    onItemClickListener.OnItemClick(suggestionItem, position);
                }
                if (tempTextFormatter == null || binding.inputField.getText() == null || 
                    binding.inputField.getText().toString().isEmpty() || suggestionItem == null) {
                    return;
                }
                tempTextFormatter.onItemClick(getContext(), suggestionItem, user, group);
                Editable editable = binding.inputField.getEditableText();
                int cursorPosition = binding.inputField.getSelectionStart();
                int startIndex = cursorPosition;
                for (int i = cursorPosition - 1; i >= 0; i--) {
                    if (editable.charAt(i) == tempTextFormatter.getTrackingCharacter()) {
                        startIndex = i;
                        break;
                    }
                }

                int start = Math.max(startIndex, 0);
                int end = Math.max(binding.inputField.getSelectionEnd(), 0);
                Editable message = binding.inputField.getEditableText();

                String tagText = suggestionItem.getPromptText();

                int triggerIndex = message.toString().lastIndexOf(tempTextFormatter.getTrackingCharacter(), Math.min(start, end));
                if (triggerIndex == -1) {
                    triggerIndex = start;
                }

                int spanStart = triggerIndex;
                int spanEnd = spanStart + tagText.length();

                // Check if code formatting is active at the insertion point.
                // If so, treat the mention as plain text inside the code span — no
                // NonEditableSpan should be applied.
                boolean isInsideInlineCode = RichTextSpanManager.hasFormatInRange(
                        message, triggerIndex, triggerIndex, FormatType.INLINE_CODE);
                boolean isInlineCodePending = formatSpanWatcher != null
                        && formatSpanWatcher.isPendingFormat(FormatType.INLINE_CODE)
                        && (formatSpanWatcher.isExplicitlyDisabled(FormatType.INLINE_CODE) == false);
                boolean isInsideCodeBlock = RichTextSpanManager.hasFormatInRange(
                        message, triggerIndex, triggerIndex, FormatType.CODE_BLOCK);
                boolean isCodeBlockPending = formatSpanWatcher != null
                        && formatSpanWatcher.isPendingFormat(FormatType.CODE_BLOCK)
                        && (formatSpanWatcher.isExplicitlyDisabled(FormatType.CODE_BLOCK) == false);
                boolean isInsideCodeFormat = isInsideInlineCode || isInlineCodePending
                        || isInsideCodeBlock || isCodeBlockPending;

                NonEditableSpan span = null;
                if (!isInsideCodeFormat
                        && !tagText.isEmpty() && suggestionItem.getUnderlyingText() != null) {
                    span = new NonEditableSpan(tempTextFormatter.getId(), tagText, suggestionItem);
                }

                // Suppress mention detection during the entire insertion process.
                // The text modifications below trigger afterTextChanged multiple times,
                // and without suppression the scanning loop would find the '@' character
                // in the inserted text and re-show the suggestion list.
                boolean isPlainTextInsertion = (span == null);
                if (isPlainTextInsertion) {
                    suppressMentionDetection = true;
                }

                // Before replacing text, ensure any explicitly disabled inline format
                // spans near the insertion point have EXCLUSIVE_EXCLUSIVE flags. This
                // prevents Android's SpannableStringBuilder from auto-extending them
                // to cover the mention text during the replace() operation.
                if (formatSpanWatcher != null && span != null) {
                    RichTextFormatSpan[] existingSpans = message.getSpans(
                            triggerIndex, Math.max(start, end), RichTextFormatSpan.class);
                    for (RichTextFormatSpan fSpan : existingSpans) {
                        FormatType ft = fSpan.getFormatType();
                        if (RichTextSpanManager.isTextStyleFormat(ft)
                                && formatSpanWatcher.isExplicitlyDisabled(ft)) {
                            int fs = message.getSpanStart(fSpan);
                            int fe = message.getSpanEnd(fSpan);
                            int flags = message.getSpanFlags(fSpan);
                            if ((flags & Spanned.SPAN_INCLUSIVE_INCLUSIVE) == Spanned.SPAN_INCLUSIVE_INCLUSIVE
                                    || (flags & Spanned.SPAN_INCLUSIVE_EXCLUSIVE) == Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                    || (flags & Spanned.SPAN_EXCLUSIVE_INCLUSIVE) == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                                message.removeSpan(fSpan);
                                message.setSpan(fSpan, fs, fe, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }
                }

                message.replace(triggerIndex, Math.max(start, end), tagText, 0, tagText.length());

                if (span != null) {
                    message.setSpan(span, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Remove any format spans that were auto-extended over the mention
                    removeFormatsFromMentions(message, spanStart, spanEnd);
                }

                // Capture tempTextFormatter id before hiding the suggestion list,
                // because hideSuggestionList() sets tempTextFormatter to null.
                char tempFormatterId = tempTextFormatter != null ? tempTextFormatter.getId() : 0;

                binding.inputField.setSelection(spanEnd);
                setSuggestionListVisibility(View.GONE);

                // When mention was inserted as plain text (inline code active),
                // mark it with ConsumedMentionSpan so the scanning loop skips
                // the '@' on subsequent keystrokes, and clear tempTextFormatter.
                if (isPlainTextInsertion) {
                    // Store full mention data so the mention can be restored
                    // when the code block is later removed (deselected).
                    ConsumedMentionSpan consumedSpan = new ConsumedMentionSpan(
                            tempFormatterId, tagText,
                            suggestionItem, suggestionItem.getPromptTextAppearance());
                    message.setSpan(consumedSpan, spanStart, spanEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // tempTextFormatter is already null (cleared by hideSuggestionList)
                }

                if (!binding.inputField.getText().toString().isEmpty()) {
                    binding.inputField.getEditableText().insert(binding.inputField.getSelectionStart(), " ");
                    binding.inputField.setSelection(spanEnd + 1);
                }

                // Cancel any pending search timer and clear suppression flag
                if (isPlainTextInsertion) {
                    if (queryTimer != null) {
                        queryTimer.cancel();
                    }
                    suppressMentionDetection = false;
                }
            }

            @Override
            public void OnItemLongClick(SuggestionItem suggestionItem, int position) {
                // Not used
            }
        });
    }

    /**
     * Sets a list of text formatters for the message composer.
     *
     * @param cometchatTextFormatters A list of CometChatTextFormatter objects to be added.
     */
    public void setTextFormatters(List<CometChatTextFormatter> cometchatTextFormatters) {
        if (cometchatTextFormatters != null) {
            this.cometchatTextFormatters.addAll(cometchatTextFormatters);
            processFormatters();
        }
    }

    /**
     * Gets the CometChatSuggestionList instance.
     *
     * @return The CometChatSuggestionList instance.
     */
    public CometChatSuggestionList getCometChatSuggestionList() {
        return binding.suggestionList;
    }

    /**
     * Destroys timers to prevent memory leaks.
     */
    private void destroyTimers() {
        if (queryTimer != null) {
            queryTimer.cancel();
            queryTimer = null;
        }
        if (operationTimer != null) {
            operationTimer.cancel();
            operationTimer = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isDetachedFromWindow) {
            observeViewModel();
            isDetachedFromWindow = false;
        }
        if (viewModel != null) {
            viewModel.addListeners();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Dismiss bottom sheet if showing
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
        
        // Clean up resources
        if (viewModel != null) {
            // Cancel typing timer and remove event listeners
            viewModel.cancelTypingTimer();
            viewModel.removeListeners();
        }
        
        // Dispose LiveData observers
        disposeObservers();
        
        // Destroy timers
        destroyTimers();
        
        // Reset mention count
        resetMentionCount();
        
        isDetachedFromWindow = true;
        super.onDetachedFromWindow();
    }

    /**
     * Removes all LiveData observers to prevent memory leaks.
     * Following the same pattern as CometChatMessageComposer.disposeObservers().
     */
    public void disposeObservers() {
        if (viewModel != null && lifecycleOwner != null) {
            viewModel.getSendButtonActive().removeObservers(lifecycleOwner);
            viewModel.getToolbarVisible().removeObservers(lifecycleOwner);
            viewModel.getActiveFormats().removeObservers(lifecycleOwner);
            viewModel.getEditingMessage().removeObservers(lifecycleOwner);
            viewModel.getReplyingToMessage().removeObservers(lifecycleOwner);
            viewModel.sentMessage().removeObservers(lifecycleOwner);
            viewModel.getException().removeObservers(lifecycleOwner);
            viewModel.processEdit().removeObservers(lifecycleOwner);
            viewModel.successEdit().removeObservers(lifecycleOwner);
            viewModel.processQuote().removeObservers(lifecycleOwner);
            viewModel.successQuote().removeObservers(lifecycleOwner);
            viewModel.showBottomPanel().removeObservers(lifecycleOwner);
            viewModel.closeBottomPanel().removeObservers(lifecycleOwner);
            viewModel.showTopPanel().removeObservers(lifecycleOwner);
            viewModel.closeTopPanel().removeObservers(lifecycleOwner);
            viewModel.getComposeText().removeObservers(lifecycleOwner);
            viewModel.getIsAIAssistantGenerating().removeObservers(lifecycleOwner);
        }
    }

    // ==================== Callback Interfaces ====================

    /**
     * Interface for send button click callbacks.
     */
    public interface SendButtonClickListener {
        void onSendClick(Context context, BaseMessage message);
    }

    /**
     * Interface for text change callbacks.
     */
    public interface OnTextChangeListener {
        void onTextChange(String text);
    }

    /**
     * Interface for attachment click callbacks.
     */
    public interface OnAttachmentClickListener {
        void onAttachmentClick();
    }

    /**
     * Interface for error callbacks.
     */
    public interface OnErrorListener {
        void onError(Exception exception);
    }

    /**
     * Interface for voice recording callbacks.
     */
    public interface OnVoiceRecordingListener {
        void onRecordingStart();
        void onRecordingEnd(File audioFile);
    }

    /**
     * Interface for edit cancel callbacks.
     */
    public interface OnEditCancelListener {
        void onEditCancel();
    }

    /**
     * Interface for toolbar visibility change callbacks.
     */
    public interface OnToolbarVisibilityChangeListener {
        void onToolbarVisibilityChange(boolean isVisible);
    }
}
