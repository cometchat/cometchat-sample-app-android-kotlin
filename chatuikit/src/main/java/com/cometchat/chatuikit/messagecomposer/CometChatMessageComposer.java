package com.cometchat.chatuikit.messagecomposer;

import static android.app.Activity.RESULT_OK;
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
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.TypingIndicator;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.ai.AIOptionsStyle;
import com.cometchat.chatuikit.databinding.CometchatAiButtonLayoutBinding;
import com.cometchat.chatuikit.databinding.CometchatMessageComposerBinding;
import com.cometchat.chatuikit.databinding.CometchatSecondaryButtonLayoutBinding;
import com.cometchat.chatuikit.databinding.CometchatSendButtonLayoutBinding;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.constants.UIKitUtilityConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatMentionsFormatter;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.framework.ChatConfigurator;
import com.cometchat.chatuikit.shared.interfaces.Function1;
import com.cometchat.chatuikit.shared.interfaces.OnError;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.models.CometChatMessageComposerAction;
import com.cometchat.chatuikit.shared.permission.CometChatPermissionHandler;
import com.cometchat.chatuikit.shared.permission.builder.ActivityResultHandlerBuilder;
import com.cometchat.chatuikit.shared.permission.builder.PermissionHandlerBuilder;
import com.cometchat.chatuikit.shared.permission.listener.PermissionResultListener;
import com.cometchat.chatuikit.shared.resources.soundmanager.CometChatSoundManager;
import com.cometchat.chatuikit.shared.resources.soundmanager.Sound;
import com.cometchat.chatuikit.shared.resources.utils.MediaUtils;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.itemclicklistener.OnItemClickListener;
import com.cometchat.chatuikit.shared.spans.NonEditableSpan;
import com.cometchat.chatuikit.shared.views.mediarecorder.CometChatMediaRecorder;
import com.cometchat.chatuikit.shared.views.messageinput.CometChatMessageInput;
import com.cometchat.chatuikit.shared.views.messageinput.CometChatTextWatcher;
import com.cometchat.chatuikit.shared.views.optionsheet.OptionSheetMenuItem;
import com.cometchat.chatuikit.shared.views.optionsheet.aioptionsheet.CometChatAIOptionSheet;
import com.cometchat.chatuikit.shared.views.optionsheet.attachmentoptionsheet.CometChatAttachmentOptionSheet;
import com.cometchat.chatuikit.shared.views.suggestionlist.CometChatSuggestionList;
import com.cometchat.chatuikit.shared.views.suggestionlist.SuggestionItem;
import com.cometchat.chatuikit.shared.views.suggestionlist.SuggestionListViewHolderListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kotlin.jvm.functions.Function4;

/**
 * The CometChatMessageComposer class is a custom view that provides a user
 * interface for composing and sending messages. It extends the MaterialCardView
 * class and implements various functionalities related to message composition.
 * <br>
 * Example :<br>
 *
 * <pre>
 * {
 * 	&#64;code
 * 	CometChatMessageComposer cometchatMessageComposer = findViewById(R.id.message_composer);
 * 	if (user != null)
 * 		cometchatMessageComposer.setUser(user);
 * 	else if (group != null)
 * 		cometchatMessageComposer.setGroup(group);
 * }
 * </pre>
 */
public class CometChatMessageComposer extends MaterialCardView {
    private static final String TAG = CometChatMessageComposer.class.getSimpleName();
    /**
     * Action sheets and interaction handlers.
     */
    private final List<OptionSheetMenuItem> aiOptionSheetMenuItems = new ArrayList<>();
    private final List<OptionSheetMenuItem> attachmentOptionSheetMenuItems = new ArrayList<>();
    /**
     * Configuration properties for attachment, media, and auxiliary views.
     */
    private final HashMap<String, CometChatMessageComposerAction> actionHashMap = new HashMap<>();
    private int imageAttachmentOptionVisibility = View.VISIBLE;
    private int cameraAttachmentOptionVisibility = View.VISIBLE;
    private int videoAttachmentOptionVisibility = View.VISIBLE;
    private int audioAttachmentOptionVisibility = View.VISIBLE;
    private int fileAttachmentOptionVisibility = View.VISIBLE;
    private int pollAttachmentOptionVisibility = View.VISIBLE;
    private int collaborativeDocumentOptionVisibility = View.VISIBLE;
    private int collaborativeWhiteboardOptionVisibility = View.VISIBLE;
    private int attachmentButtonVisibility = View.VISIBLE;
    private int voiceNoteButtonVisibility = View.VISIBLE;
    private int stickersButtonVisibility = View.VISIBLE;
    private int sendButtonVisibility = View.VISIBLE;
    private int auxiliaryButtonVisibility = View.VISIBLE;

    /**
     * Holds context, user, group, and messaging-related properties.
     */
    private Activity activity;
    private MessageComposerViewModel composerViewModel;
    private User user;
    private Group group;
    /**
     * Manages sound settings for messaging.
     */
    private boolean disableSoundForMessages;
    private @RawRes int customSoundForMessages;
    private CometChatSoundManager soundManager;
    /**
     * Controls typing events and configurations.
     */
    private boolean disableTypingEvents;
    private Timer typingTimer;
    private Timer queryTimer;
    private Timer operationTimer;
    private List<CometChatMessageComposerAction> messageComposerActions;
    private Function4<Context, User, Group, HashMap<String, String>, List<CometChatMessageComposerAction>> aiOptions;
    private SendButtonClick sendButtonClick;
    private OnError onError;
    private BottomSheetDialog bottomSheetDialog;
    /**
     * Binding components for the view.
     */
    private CometchatMessageComposerBinding binding;
    private CometchatSendButtonLayoutBinding sendButtonLayoutBinding;
    private CometchatSecondaryButtonLayoutBinding secondaryButtonLayoutBinding;
    private CometchatAiButtonLayoutBinding cometchatAiButtonLayoutBinding;
    private HashMap<Character, HashMap<String, SuggestionItem>> selectedSuggestionItemHashMap;
    private PermissionHandlerBuilder permissionHandlerBuilder;
    private ActivityResultHandlerBuilder activityResultHandlerBuilder;
    private PermissionResultListener permissionResultListener;
    private int parentMessageId = -1;
    private String RESULT_TO_BE_OPEN = "";
    /**
     * UI elements and appearance customization.
     */
    private View headerView;
    private View footerView;
    @Nullable
    private View internalBottomPanel, internalTopPanel, sendButtonView;
    private LinearLayout auxiliaryViewContainer;
    private Function4<Context, User, Group, HashMap<String, String>, View> auxiliaryButtonView;
    private View auxiliaryButton;
    /**
     * Text message properties and text formatting settings.
     */
    private TextMessage editMessage;
    private BaseMessage sendMessage;
    private String text, id, type;
    private List<CometChatTextFormatter> cometchatTextFormatters;
    private boolean disableMentions;
    private HashMap<Character, CometChatTextFormatter> cometchatTextFormatterHashMap;
    private CometChatTextFormatter tempTextFormatter;
    private CometChatMentionsFormatter cometchatMentionsFormatter;
    private int lastTextFormatterOpenedIndex = -1;
    private OnItemClickListener<SuggestionItem> onItemClickListener;
    /**
     * Visual styles and drawable resources.
     */
    private @StyleRes int actionSheetStyle;
    private AIOptionsStyle aiOptionsStyle;
    private AdditionParameter additionParameter;
    private Drawable attachmentIcon;
    private @ColorInt int attachmentIconTint;
    private Drawable voiceRecordingIcon;
    private @ColorInt int voiceRecordingIconTint;
    private Drawable AIIcon;
    private @ColorInt int AIIconTint;
    private Drawable inactiveStickerIcon;
    private @ColorInt int inactiveStickerIconTint;
    private Drawable activeSendButtonDrawable;
    private Drawable inactiveSendButtonDrawable;
    /**
     * Edit preview appearance configurations.
     */
    private @StyleRes int editPreviewTitleTextAppearance;
    private @StyleRes int editPreviewMessageTextAppearance;
    private @ColorInt int editPreviewTitleTextColor;
    private @ColorInt int editPreviewMessageTextColor;
    private @ColorInt int editPreviewBackgroundColor;
    private @Dimension int editPreviewCornerRadius;
    private @ColorInt int editPreviewStrokeColor;
    private @Dimension int editPreviewStrokeWidth;
    private Drawable editPreviewCloseIcon;
    private @ColorInt int editPreviewCloseIconTint;
    /**
     * Information and messaging input styles.
     */
    private Drawable infoIcon;
    private @ColorInt int infoTextColor;
    private @StyleRes int infoTextAppearance;
    private @ColorInt int infoBackgroundColor;
    private @Dimension int infoCornerRadius;
    private @ColorInt int infoStrokeColor;
    private @Dimension int infoStrokeWidth;
    private @ColorInt int infoIconTint;
    private @StyleRes int messageInputStyle;
    private @StyleRes int mentionsStyle;
    private @StyleRes int style;
    /**
     * Composed box customization and separator properties.
     */
    private @ColorInt int composeBoxBackgroundColor;
    private @Dimension int composeBoxStrokeWidth;
    private @ColorInt int composeBoxStrokeColor;
    private @Dimension int composeBoxCornerRadius;
    private Drawable composeBoxBackgroundDrawable;
    private @ColorInt int separatorColor;
    /**
     * composer box customization properties.
     */
    private @ColorInt int backgroundColor;
    private @ColorInt int strokeColor;
    private @Dimension int strokeWidth;
    private Drawable backgroundDrawable;
    private int cornerRadius;
    private @StyleRes int mediaRecorderStyle;
    private @StyleRes int aiOptionSheetStyle;
    private @StyleRes int attachmentOptionSheetStyle;
    private @StyleRes int suggestionListStyle;
    private String[] microPhonePermissions;

    /**
     * The constructor for the CometChatMessageComposer class.
     *
     * @param context The context of the CometChatMessageComposer.
     */
    public CometChatMessageComposer(Context context) {
        this(context, null);
    }

    /**
     * The constructor for the CometChatMessageComposer class.
     *
     * @param context The context of the CometChatMessageComposer.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public CometChatMessageComposer(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatMessageComposerStyle);
    }

    /**
     * The constructor for the CometChatMessageComposer class.
     *
     * @param context      The context of the CometChatMessageComposer.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a
     *                     style resource that supplies default values for the view.
     */
    public CometChatMessageComposer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the views and components of the CometChatMessageComposer.
     */
    private void inflateAndInitializeView(AttributeSet attributeSet, int defStyleAttr) {
        binding = CometchatMessageComposerBinding.inflate(LayoutInflater.from(getContext()), this, true);
        bottomSheetDialog = new BottomSheetDialog(getContext());
        sendButtonLayoutBinding = CometchatSendButtonLayoutBinding.bind(View.inflate(getContext(), R.layout.cometchat_send_button_layout, null));
        secondaryButtonLayoutBinding = CometchatSecondaryButtonLayoutBinding.bind(View.inflate(getContext(),
                                                                                               R.layout.cometchat_secondary_button_layout,
                                                                                               null));
        cometchatAiButtonLayoutBinding = CometchatAiButtonLayoutBinding.bind(View.inflate(getContext(), R.layout.cometchat_ai_button_layout, null));

        Utils.initMaterialCard(this);
        Utils.initMaterialCard(binding.tagInfoCard);
        Utils.initMaterialCard(binding.editPreviewLayout.editMessageLayout);
        Utils.initMaterialCard(binding.composeBoxCard);

        initializeComponents();
        initializeTimers();
        initializeCollections();
        configureUIBindings();
        setupViewModel();
        setPlaceHolderText(getResources().getString(R.string.cometchat_composer_place_holder_text));
        initializeComposerActions();
        setupPermissionResultListener();
        setupMicroPhonePermissions();
        setupPermissionHandlerBuilder();
        setupActivityResultHandler();
        setupSuggestionListScrollListener();
        setupSuggestionListClickListener();
        setMessageInputTextChangeListener();
        applyStyleAttributes(attributeSet, defStyleAttr, 0);
    }

    private void setupMicroPhonePermissions() {
        microPhonePermissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            microPhonePermissions = new String[]{Manifest.permission.RECORD_AUDIO};
        }
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.CometChatMessageComposer, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatMessageComposer_cometchatMessageComposerStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMessageComposer, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Initializes the necessary components and variables.
     */
    private void initializeComponents() {
        additionParameter = new AdditionParameter();
        activity = ((AppCompatActivity) getContext());
        soundManager = new CometChatSoundManager(getContext());
        composerViewModel = new MessageComposerViewModel();
        auxiliaryViewContainer = createAuxiliaryViewContainer();
        auxiliaryViewContainer.setGravity(Gravity.CENTER_VERTICAL);
        initializeActionSheets();
        initializeSecondaryView();
        initializeSendButton();
    }

    /**
     * Sets up the action sheets used in the composer.
     */
    private void openAIOptionSheet() {
        CometChatAIOptionSheet cometchatAIOptionSheet = new CometChatAIOptionSheet(getContext());
        cometchatAIOptionSheet.setAIOptionItems(aiOptionSheetMenuItems);
        cometchatAIOptionSheet.setStyle(aiOptionSheetStyle);
        cometchatAIOptionSheet.setOptionSheetClickListener(menuIem -> {
            CometChatMessageComposerAction action = actionHashMap.get(menuIem.getId());
            if (action != null) {
                if (action.getOnClick() != null) {
                    action.getOnClick().onClick();
                    bottomSheetDialog.dismiss();
                } else {
                    handleAttachmentOptionItemClick(menuIem);
                    bottomSheetDialog.dismiss();
                }
            }
        });
        showBottomSheet(bottomSheetDialog, true, cometchatAIOptionSheet);
    }

    /**
     * Initializes the timers used for various operations.
     */
    private void initializeTimers() {
        typingTimer = new Timer();
        queryTimer = new Timer();
        operationTimer = new Timer();
    }

    /**
     * Sets up the ViewModel and observers for handling UI updates and actions.
     */
    private void setupViewModel() {
        composerViewModel = new ViewModelProvider.NewInstanceFactory().create(MessageComposerViewModel.class);
        composerViewModel.sentMessage().observe((LifecycleOwner) getContext(), this::messageSentSuccess);
        composerViewModel.getException().observe((LifecycleOwner) getContext(), this::messageSendException);
        composerViewModel.processEdit().observe((LifecycleOwner) getContext(), this::showEditMessagePreview);
        composerViewModel.successEdit().observe((LifecycleOwner) getContext(), this::onMessageEditSuccess);
        composerViewModel.closeTopPanel().observe((LifecycleOwner) getContext(), this::closeInternalTopPanel);
        composerViewModel.closeBottomPanel().observe((LifecycleOwner) getContext(), this::closeInternalBottomPanel);
        composerViewModel.showTopPanel().observe((LifecycleOwner) getContext(), this::showInternalTopPanel);
        composerViewModel.showBottomPanel().observe((LifecycleOwner) getContext(), this::showInternalBottomPanel);
        composerViewModel.getComposeText().observe((LifecycleOwner) getContext(), this::setInitialComposerText);
    }

    /**
     * Initializes collections like formatters and suggestion items.
     */
    private void initializeCollections() {
        cometchatTextFormatterHashMap = new HashMap<>();
        selectedSuggestionItemHashMap = new HashMap<>();
        this.cometchatTextFormatters = new ArrayList<>();
        getDefaultMentionsFormatter();
        setTextFormatters(null);
    }

    /**
     * Configures the UI elements related to suggestions and input.
     */
    private void configureUIBindings() {
        binding.suggestionList.setMaxHeightLimit(getResources().getDimensionPixelSize(R.dimen.cometchat_250dp));
        binding.messageInput.setAuxiliaryButtonAlignment(UIKitConstants.AuxiliaryButtonAlignment.LEFT);
    }

    /**
     * Creates the auxiliary view container and sets its properties.
     */
    private LinearLayout createAuxiliaryViewContainer() {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        setAuxiliaryButtonAlignmentBasedOnLayout();
        return container;
    }

    /**
     * Sets the alignment for auxiliary buttons based on layout direction.
     */
    private void setAuxiliaryButtonAlignmentBasedOnLayout() {
        if (getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            setAuxiliaryButtonAlignment(UIKitConstants.AuxiliaryButtonAlignment.LEFT);
        } else {
            setAuxiliaryButtonAlignment(UIKitConstants.AuxiliaryButtonAlignment.RIGHT);
        }
    }

    /**
     * Initializes the message composer actions and AI options.
     */
    private void initializeComposerActions() {

        aiOptions = (context, user, group, map) -> ChatConfigurator
            .getDataSource()
            .getAIOptions(context, user, group, composerViewModel.getIdMap(), aiOptionsStyle, additionParameter);
    }

    /**
     * Sets up the permission result listener for handling different permissions.
     */
    private void setupPermissionResultListener() {
        permissionResultListener = (grantedPermission, deniedPermission) -> {
            if (!deniedPermission.isEmpty()) {
                handleDeniedPermission(deniedPermission);
            } else {
                if (grantedPermission.contains(Manifest.permission.RECORD_AUDIO)) {
                    openMediaRecorderSheet();
                } else
                    handleGrantedPermission();
            }
        };
    }

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

        builder.setPositiveButton(R.string.cometchat_settings_button, (dialog, which) -> {
            Utils.openAppSettings(getContext());
        });

        builder.create().show();
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
            showPermissionDialog(deniedPermissions);
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
     * Sets up the permission handler builder with the current context and listener.
     */
    private void setupPermissionHandlerBuilder() {
        permissionHandlerBuilder = CometChatPermissionHandler.withContext(getContext()).withListener(permissionResultListener);
    }

    /**
     * Sets up the activity result handler for handling camera, file, and other
     * media actions.
     */
    private void setupActivityResultHandler() {
        activityResultHandlerBuilder = CometChatPermissionHandler.withContext(getContext()).registerListener(result -> {
            if (result.getResultCode() == RESULT_OK) {
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
                showWarning(getResources().getString(R.string.cometchat_file_not_exist));
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
        sendMediaMessage(file, contentType);
    }

    /**
     * Handles the result when the action was to open the camera.
     */
    private File handleCameraResult() {
        if (Build.VERSION.SDK_INT >= 29) {
            return MediaUtils.getRealPath(getContext(), MediaUtils.uri, false);
        } else {
            return new File(MediaUtils.pictureImagePath);
        }
    }

    /**
     * Handles the result when the action was to select a file.
     */
    private File handleFileResult(ActivityResult result) {
        if (result.getData() != null && result.getData().getData() != null) {
            return MediaUtils.getRealPath(getContext(), result.getData().getData(), false);
        }
        return null;
    }

    /**
     * Handles the result for other types of media actions.
     */
    private File handleOtherMediaResult(ActivityResult result) {
        if (result.getData() != null && result.getData().getData() != null) {
            return MediaUtils.getRealPath(getContext(), result.getData().getData(), false);
        }
        return null;
    }

    /**
     * Sets up the scroll listener for the suggestion list RecyclerView.
     */
    private void setupSuggestionListScrollListener() {
        getCometChatSuggestionList().getBinding().recyclerViewSuggestionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /**
             * Called when the scroll state changes in the RecyclerView.
             *
             * @param recyclerView
             *            The RecyclerView whose scroll state has changed.
             * @param newState
             *            The updated scroll state.
             */
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (tempTextFormatter != null) {
                        tempTextFormatter.onScrollToBottom();
                    }
                }
            }

            /**
             * Called when the RecyclerView is scrolled.
             *
             * @param recyclerView
             *            The RecyclerView which scrolled.
             * @param dx
             *            The amount of horizontal scroll.
             * @param dy
             *            The amount of vertical scroll.
             */
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    /**
     * Sets up the click listener for the suggestion list.
     */
    private void setupSuggestionListClickListener() {
        binding.suggestionList.setItemClickListener(new OnItemClickListener<SuggestionItem>() {
            @Override
            public void OnItemClick(SuggestionItem suggestionItem, int position) {
                if (onItemClickListener != null) {
                    onItemClickListener.OnItemClick(suggestionItem, position);
                }
                if (tempTextFormatter == null || binding.messageInput.getText() == null || binding.messageInput
                    .getText()
                    .isEmpty() || suggestionItem == null) {
                    return;
                }
                tempTextFormatter.onItemClick(getContext(), suggestionItem, user, group);
                Editable editable = binding.messageInput.getEditableText();
                int cursorPosition = binding.messageInput.getSelectionStart();
                int startIndex = cursorPosition;
                for (int i = cursorPosition - 1; i >= 0; i--) {
                    if (editable.charAt(i) == tempTextFormatter.getTrackingCharacter()) {
                        startIndex = i;
                        break;
                    }
                }

                int start = Math.max(startIndex, 0);
                int end = Math.max(binding.messageInput.getSelectionEnd(), 0);
                Editable message = binding.messageInput.getEditableText();

                String tagText = suggestionItem.getPromptText();

                int triggerIndex = message.toString().lastIndexOf(tempTextFormatter.getTrackingCharacter(), Math.min(start, end));
                if (triggerIndex == -1) {
                    triggerIndex = start;
                }

                int spanStart = triggerIndex;
                int spanEnd = spanStart + tagText.length();
                NonEditableSpan span = null;
                if (!tagText.isEmpty() && suggestionItem.getUnderlyingText() != null)
                    span = new NonEditableSpan(tempTextFormatter.getId(), tagText, suggestionItem);

                message.replace(triggerIndex, Math.max(start, end), tagText, 0, tagText.length());

                if (span != null)
                    message.setSpan(span, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                binding.messageInput.setSelection(spanEnd);
                setSuggestionListVisibility(View.GONE);
                if (!binding.messageInput.getText().isEmpty()) {
                    binding.messageInput.getEditableText().insert(binding.messageInput.getSelectionStart(), " ");
                    binding.messageInput.setSelection(spanEnd + 1);
                }
            }
        });
    }

    /**
     * Sets the Message input text change listener.
     */
    private void setMessageInputTextChangeListener() {
        binding.messageInput.setOnTextChangedListener(new CometChatTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                text = charSequence.toString();
                if (!disableTypingEvents) sendTypingIndicator(charSequence.length() <= 0);
                sendSelectedSuggestionList();
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                if (tempTextFormatter != null && count > 0 && after == 0) {
                    int endIndex = start + count;
                    String deletedSubString = charSequence.subSequence(start, endIndex).toString();
                    char lastDeletedChar = deletedSubString.charAt(deletedSubString.length() - 1);
                    if (lastDeletedChar == tempTextFormatter.getTrackingCharacter()) {
                        setSuggestionListVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()) {
                    sendButtonLayoutBinding.ivSendBtn.setBackground(activeSendButtonDrawable);
                } else {
                    sendButtonLayoutBinding.ivSendBtn.setBackground(inactiveSendButtonDrawable);
                }
                if (typingTimer == null) {
                    typingTimer = new Timer();
                }
                if (!disableTypingEvents) endTypingTimer();
            }

            @Override
            public void onSelectionChanged(int selStart, int selEnd) {
                String charSequence = binding.messageInput.getEditableText().toString();
                char c1 = 0;
                if (!charSequence.isEmpty()) {
                    c1 = charSequence.charAt(Math.max(selStart - 1, 0));
                    Editable spannable = binding.messageInput.getEditableText();
                    for (int i = selStart - 1; i >= 0; i--) {
                        char c = charSequence.charAt(i);
                        NonEditableSpan[] spans = spannable.getSpans(i, i, NonEditableSpan.class);
                        if (spans.length == 0) {
                            if (cometchatTextFormatterHashMap.containsKey(c)) {
                                if (i == 0 || charSequence.charAt(i - 1) == ' ' || charSequence.charAt(i - 1) == '\n') {
                                    if (i < selStart - 1 && (charSequence.charAt(i + 1) == ' ' || charSequence.charAt(i + 1) == '\n')) {
                                        tempTextFormatter = null;
                                    } else {
                                        // Set tempTextFormatter to the associated formatter
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
                        } else {
                            tempTextFormatter = null;
                            break;
                        }
                    }
                }

                if (!charSequence.isEmpty() && tempTextFormatter != null && !tempTextFormatter.getDisableSuggestions()) {
                    if (selStart > 0) {
                        char c = charSequence.charAt(Math.max(selStart - 2, 0));
                        if (c1 == tempTextFormatter.getTrackingCharacter()) {
                            if (selStart == 1 || Character.isWhitespace(c)) {
                                setSuggestionListVisibility(View.VISIBLE);
                            }
                        } else if (c1 == ' ') {
                            int lastIndex = Math.max(selStart - 2, 0);
                            while (lastIndex >= 0 && Character.isWhitespace(charSequence.charAt(lastIndex))) {
                                lastIndex--;
                            }
                            if (lastIndex >= 0 && charSequence.charAt(lastIndex) == tempTextFormatter.getTrackingCharacter()) {
                                setSuggestionListVisibility(View.GONE);
                            }
                        }
                    } else if (selStart == 0 && charSequence.length() > 1 && charSequence.charAt(1) == tempTextFormatter.getTrackingCharacter()) {
                        setSuggestionListVisibility(View.VISIBLE);
                    } else {
                        setSuggestionListVisibility(View.GONE);
                    }
                    sendSearchQueryWithInterval(charSequence, selStart, UIKitUtilityConstants.COMPOSER_SEARCH_QUERY_INTERVAL);
                } else {
                    setSuggestionListVisibility(View.GONE);
                }
            }

            @Override
            public void onSpanDeleted(NonEditableSpan span) {
            }
        });
    }

    /**
     * Handles the default actions based on the action item ID when no onClick is
     * defined.
     *
     * @param actionItem The action item clicked in the main action sheet.
     */
    private void handleAttachmentOptionItemClick(OptionSheetMenuItem actionItem) {
        CometChatLogger.e(TAG, "handleAttachmentOptionItemClick: " + actionItem.getId());
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

    public @StyleRes int getSuggestionListStyle() {
        return suggestionListStyle;
    }

    public void setSuggestionListStyle(@StyleRes int suggestionListStyle) {
        this.suggestionListStyle = suggestionListStyle;
        binding.suggestionList.setStyle(suggestionListStyle);
    }

    /**
     * Retrieves the default {@link CometChatMentionsFormatter} from the available
     * text formatters.
     *
     * <p>
     * This method iterates through the list of text formatters obtained from
     * {@link CometChatMentionsFormatter}. If found, it sets the
     * `cometchatMentionsFormatter` variable to that instance and adds it to the
     * `cometchatTextFormatters` list.
     */
    private void getDefaultMentionsFormatter() {
        for (CometChatTextFormatter textFormatter : CometChatUIKit.getDataSource().getTextFormatters(getContext(), additionParameter)) {
            if (textFormatter instanceof CometChatMentionsFormatter) {
                cometchatMentionsFormatter = (CometChatMentionsFormatter) textFormatter;
                break;
            }
        }
        this.cometchatTextFormatters.add(cometchatMentionsFormatter);
    }

    /**
     * sets the list of suggestion items
     */
    private void sendSelectedSuggestionList() {
        operationTimer.cancel();
        operationTimer = new Timer();
        operationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                processTextToSetUniqueSuggestions();
            }
        }, UIKitUtilityConstants.COMPOSER_OPERATION_INTERVAL);
    }

    /**
     * sets the list of suggestion items
     */
    private void processTextToSetUniqueSuggestions() {
        Editable editable = binding.messageInput.getEditableText();
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
     * sends the selected list to the formatter
     *
     * @param formatterId
     */
    private void sendSelectedListToFormatter(char formatterId) {
        CometChatTextFormatter formatter = cometchatTextFormatterHashMap.get(formatterId);
        if (formatter != null) {
            List<SuggestionItem> suggestionItems = getSelectedSuggestionItems(formatterId);
            formatter.setSelectedList(getContext(), suggestionItems);
        }
    }

    /**
     * gets the selected suggestion items
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
     * sends search query with interval to formatter
     */
    private void sendSearchQueryWithInterval(String text, int cursorPosition, int interval) {
        queryTimer.cancel();
        queryTimer = new Timer();
        queryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (tempTextFormatter != null)
                    tempTextFormatter.search(getContext(), getQueryString(text, cursorPosition, tempTextFormatter.getTrackingCharacter()));
            }
        }, interval);
    }

    /**
     * gets the query string
     *
     * @param text
     * @param cursorPosition
     * @param triggerKey
     * @return
     */
    public String getQueryString(String text, int cursorPosition, char triggerKey) {
        String query = "";

        if (text != null && !text.isEmpty()) {
            int startIndex = cursorPosition;
            for (int i = cursorPosition - 1; i >= 0; i--) {
                if (text.charAt(i) == triggerKey) {
                    startIndex = i;
                    break;
                }
            }
            try {
                query = text.substring(startIndex + 1, cursorPosition);
            } catch (Exception ignored) {

            }
        }
        return query;
    }

    /**
     * sets the visibility of the suggestion list
     *
     * @param visibility
     */
    public void setSuggestionListVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            visibleSuggestionList();
        } else {
            hideSuggestionList();
        }
    }

    /**
     * sets the visibility of the suggestion list
     */
    private void visibleSuggestionList() {
        binding.suggestionList.setVisibility(View.VISIBLE);
        binding.suggestionList.showShimmer(true);
    }

    /**
     * hides the suggestion list
     */
    private void hideSuggestionList() {
        binding.suggestionList.setVisibility(View.GONE);
        binding.suggestionList.setList(new ArrayList<>());
        if (tempTextFormatter != null) tempTextFormatter.search(getContext(), null);
        tempTextFormatter = null;
        lastTextFormatterOpenedIndex = -1;
    }

    /**
     * Sets a list of text formatters for the message composer.
     *
     * <p>
     * This method allows you to provide a list of {@link CometChatTextFormatter}
     * objects, which will be used to format and style the text input in the message
     * composer. If the provided list is not null, the formatters are added to the
     * existing list of text formatters, and the {@code
     * processFormatters()} method is called to apply them.
     *
     * @param cometchatTextFormatters A list of {@link CometChatTextFormatter} objects to be added. If
     *                                the list is null, no formatters are added.
     */
    public void setTextFormatters(List<CometChatTextFormatter> cometchatTextFormatters) {
        if (cometchatTextFormatters != null) {
            this.cometchatTextFormatters.addAll(cometchatTextFormatters);
            processFormatters();
        }
    }

    /**
     * Sets the listener for the item views in the suggestion list.
     *
     * <p>
     * This method allows customization of how individual items in the suggestion
     * list are displayed. It accepts a {@link SuggestionListViewHolderListener}
     * that defines the behavior for the list items.
     *
     * @param suggestionListViewHolderListener The listener that handles item view interactions.
     */
    public void setSuggestionListItemView(SuggestionListViewHolderListener suggestionListViewHolderListener) {
        binding.suggestionList.setListItemView(suggestionListViewHolderListener);
    }

    /**
     * Sets the click listener for suggestion items.
     *
     * <p>
     * This method allows the caller to specify an action to be performed when a
     * suggestion item is clicked. If the provided listener is not {@code null}, it
     * will be assigned to handle suggestion item clicks.
     *
     * @param onItemClickListener The listener to be called on suggestion item clicks. If {@code
     *                            null}, no action will be set.
     */
    public void setOnSuggestionClickListener(OnItemClickListener<SuggestionItem> onItemClickListener) {
        if (onItemClickListener != null) this.onItemClickListener = onItemClickListener;
    }

    /**
     * Sets the maximum height limit for the suggestion list.
     *
     * <p>
     * This method configures the maximum height that the suggestion list can
     * occupy, specified in density-independent pixels (dp). This helps ensure the
     * suggestion list fits within the layout constraints of the UI.
     *
     * @param dp The maximum height limit in density-independent pixels for the
     *           suggestion list.
     */
    public void setSuggestionListMaxHeight(int dp) {
        binding.suggestionList.setMaxHeightLimit(dp);
    }

    public void setMediaRecorderStyle(@StyleRes int mediaRecorderStyle) {
        this.mediaRecorderStyle = mediaRecorderStyle;
    }

    /**
     * Opens the appropriate storage option based on the current action to be
     * performed.
     *
     * <p>
     * This method checks the value of the `RESULT_TO_BE_OPEN` variable to determine
     * which storage option to open: files, audio, or gallery. It calls the
     * respective method to initiate the appropriate storage action.
     */
    private void openStorage() {
        if (UIKitConstants.ComposerAction.DOCUMENT.equals(RESULT_TO_BE_OPEN)) openDocument();
        else if (UIKitConstants.ComposerAction.IMAGE.equals(RESULT_TO_BE_OPEN)) openImage();
        else if (UIKitConstants.ComposerAction.VIDEO.equals(RESULT_TO_BE_OPEN)) openVideo();
        else if (UIKitConstants.ComposerAction.AUDIO.equals(RESULT_TO_BE_OPEN)) openAudio();
    }

    /**
     * Displays an internal bottom panel by applying the provided view function.
     *
     * <p>
     * This method removes any existing internal bottom panel from its parent
     * layout, and if the provided view function is not null, it creates a new view
     * and adds it to the footer layout. The context is passed to the function to
     * generate the new view.
     *
     * @param view A function that takes a {@link Context} and returns a {@link View}
     *             to be displayed as the internal bottom panel. If null, the method
     *             does not change the current internal bottom panel.
     */
    private void showInternalBottomPanel(Function1<Context, View> view) {
        if (view != null) {
            Utils.removeParentFromView(internalBottomPanel);
            if (internalBottomPanel != null)
                binding.footerViewLayout.removeView(internalBottomPanel);
            this.internalBottomPanel = view.apply(getContext());
            binding.footerViewLayout.addView(internalBottomPanel);
            animateVisibilityVisible(binding.footerViewLayout);
        }
    }

    /**
     * Displays an internal top panel by applying the provided view function.
     *
     * <p>
     * This method removes any existing internal top panel from its parent layout,
     * and if the provided view function is not null, it creates a new view and adds
     * it to the header layout. The context is passed to the function to generate
     * the new view.
     *
     * @param view A function that takes a {@link Context} and returns a {@link View}
     *             to be displayed as the internal top panel. If null, the method
     *             does not change the current internal top panel.
     */
    private void showInternalTopPanel(Function1<Context, View> view) {
        if (view != null) {
            Utils.removeParentFromView(internalTopPanel);
            if (internalTopPanel != null) binding.headerViewLayout.removeView(internalTopPanel);
            this.internalTopPanel = view.apply(getContext());
            binding.headerViewLayout.addView(internalTopPanel);
            binding.headerViewLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Closes and removes the internal top panel from the header layout.
     *
     * <p>
     * This method removes the currently displayed internal top panel, if it exists,
     * and sets the reference to null to indicate that no panel is currently
     * displayed.
     */
    private void closeInternalTopPanel(Void avoid) {
        binding.headerViewLayout.removeView(internalTopPanel);
        binding.headerViewLayout.setVisibility(View.GONE);
        internalTopPanel = null;
    }

    /**
     * Closes and removes the internal bottom panel from the footer layout.
     *
     * <p>
     * This method removes the currently displayed internal bottom panel, if it
     * exists, and sets the reference to null to indicate that no panel is currently
     * displayed.
     */
    private void closeInternalBottomPanel(Void avoid) {
        animateVisibilityGone(binding.footerViewLayout);
        binding.footerViewLayout.removeView(internalBottomPanel);
        internalBottomPanel = null;
    }

    /**
     * Determines the content type of a given URI.
     *
     * <p>
     * This method checks the MIME type of the URI using the `ContentResolver`. If
     * the MIME type is recognized as an image, video, audio, or file, the
     * corresponding `CometChatConstants.MESSAGE_TYPE_*` value is returned.
     * Otherwise, the original MIME type or `null` is returned.
     *
     * @param uri The URI to check.
     * @return The content type of the URI, or `null` if the URI is invalid or the
     * content type cannot be determined.
     */
    public String getContentType(Uri uri) {
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
            } else return null;
        } else return null;
    }

    /**
     * Sends a media message with the specified file and content type.
     *
     * <p>
     * This method triggers the sending of a media message using the provided file
     * and its associated content type. It utilizes the
     * {@link MessageComposerViewModel} to handle the underlying message sending
     * process.
     *
     * @param file        The media file to be sent. This should be a valid file that exists
     *                    on the device.
     * @param contentType The MIME type of the media file. This is used to identify the type
     *                    of content being sent (e.g., image, audio, video).
     */
    public void sendMediaMessage(File file, String contentType) {
        composerViewModel.sendMediaMessage(file, contentType);
    }    /**
     * @param color The new color to set for the card background
     */
    @Override
    public void setCardBackgroundColor(@ColorInt int color) {
        this.backgroundColor = color;
        super.setCardBackgroundColor(color);
    }

    /**
     * Plays a sound for outgoing messages if sound notifications are not disabled.
     *
     * <p>
     * This method utilizes the {@link CometChatSoundManager} to play a predefined
     * sound for outgoing messages. The sound will only play if the sound
     * notifications for messages are enabled. The specific sound played is
     * determined by the {@link Sound} class.
     */
    public void playSound() {
        if (!disableSoundForMessages)
            soundManager.play(Sound.outgoingMessage, customSoundForMessages);
    }

    public void openImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        activityResultHandlerBuilder.withIntent(intent).launch();
    }

    public void openVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        activityResultHandlerBuilder.withIntent(intent).launch();
    }

    /**
     * Opens a file picker to select files from the device.
     *
     * <p>
     * This method utilizes the {@link MediaUtils#getFileIntent()} to create an
     * intent for selecting files. The user will be presented with a file picker UI
     * to choose the desired files.
     */
    public void openDocument() {
        activityResultHandlerBuilder.withIntent(MediaUtils.getFileIntent()).launch();
    }

    /**
     * Opens an audio selection interface to choose audio files from the device.
     *
     * <p>
     * This method uses {@link MediaUtils#openAudio(Activity)} (Context)} to create
     * an intent for selecting audio files. The user will be presented with an audio
     * picker UI to choose the desired audio files.
     */
    public void openAudio() {
        activityResultHandlerBuilder.withIntent(MediaUtils.openAudio(activity)).launch();
    }

    /**
     * Launches the camera for capturing photos or videos.
     *
     * <p>
     * This method utilizes {@link MediaUtils#openCamera(Context)} to create an
     * intent that opens the camera application. The user can take a picture or
     * record a video, which can then be handled in the activity result.
     */
    public void launchCamera() {
        activityResultHandlerBuilder.withIntent(MediaUtils.openCamera(getContext())).launch();
    }

    /**
     * Requests camera permission from the user.
     *
     * <p>
     * This method checks for the {@link Manifest.permission#CAMERA} permission. If
     * the permission is not granted, it will prompt the user to allow access to the
     * camera.
     */
    public void requestCameraPermission() {
        permissionHandlerBuilder.withPermissions(new String[]{Manifest.permission.CAMERA}).check();
    }

    /**
     * Requests storage permission from the user.
     *
     * <p>
     * This method checks for the {@link Manifest.permission#WRITE_EXTERNAL_STORAGE}
     * permission. If the permission is not granted, it will prompt the user to
     * allow access to external storage.
     */
    public void requestStoragePermission() {
        permissionHandlerBuilder.withPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}).check();
    }

    /**
     * Requests audio permission from the user.
     *
     * <p>
     * This method checks for the {@link Manifest.permission#RECORD_AUDIO} permission.
     * If the permission is not granted, it will prompt the user to allow access to
     * the device's microphone.
     */
    public void requestMicrophonePermission() {
        permissionHandlerBuilder.withPermissions(microPhonePermissions).check();
    }

    /**
     * Ends the typing timer and sends a typing indicator after a debounce period.
     *
     * <p>
     * This method schedules a task to send a typing indicator after a defined
     * debounce period specified by
     * {@link UIKitUtilityConstants#TYPING_INDICATOR_DEBOUNCER}. The typing
     * indicator will be sent as an end notification.
     */
    private void endTypingTimer() {
        if (typingTimer != null) {
            typingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendTypingIndicator(true);
                }
            }, UIKitUtilityConstants.TYPING_INDICATOR_DEBOUNCER);
        }
    }

    /**
     * Sends a typing indicator to indicate the user's typing status.
     *
     * <p>
     * This method starts or ends a typing indicator based on the value of the
     * {@code isEnd} parameter. It checks if the user is blocked before sending the
     * typing indicator. If {@code
     * isEnd} is true, it sends an end typing indicator; otherwise, it sends a start
     * typing indicator.
     *
     * @param isEnd A boolean indicating whether to end the typing indicator.
     */
    public void sendTypingIndicator(boolean isEnd) {
        if (!Utils.isBlocked(user)) {
            if (isEnd) CometChat.endTyping(new TypingIndicator(id, type));
            else CometChat.startTyping(new TypingIndicator(id, type));
        }
    }

    /**
     * Handles the success event of a sent message.
     *
     * <p>
     * This method is called when a message is successfully sent. It plays a sound
     * notification and resets the {@code sendMessage} variable.
     *
     * @param baseMessage The message that was successfully sent. May be null.
     */
    public void messageSentSuccess(BaseMessage baseMessage) {
        if (baseMessage != null) {
            playSound();
            sendMessage = null;
        }
    }

    /**
     * Handles exceptions that occur during message sending.
     *
     * <p>
     * This method is called when a message sending operation fails. If an error
     * listener is registered, it invokes the listener's {@code onError} method,
     * passing the context and the exception.
     *
     * @param exception The exception that occurred during the message send operation.
     */
    public void messageSendException(CometChatException exception) {
        if (onError != null) onError.onError(exception);
    }

    /**
     * Displays a warning message as a Toast notification.
     *
     * <p>
     * This method shows a short Toast message with the specified warning text to
     * inform the user about a potential issue or warning.
     *
     * @param warning The warning message to be displayed.
     */
    private void showWarning(String warning) {
        Toast.makeText(getContext(), warning, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a preview of a message that is being edited.
     *
     * <p>
     * This method sets up the editing interface for a specified message. It updates
     * the title and subtitle of the edit preview layout to reflect the current
     * message being edited. The message text is formatted using registered text
     * formatters and displayed in the message input field.
     *
     * <p>
     * If the provided message is not null, the following actions are performed: -
     * The edit preview layout is updated to show the edit message title. - The
     * message text is formatted using the registered text formatters and set as the
     * subtitle of the preview. - The message input field is populated with the
     * formatted message text and its cursor is positioned at the end. - A close
     * button is set up to hide the edit preview when clicked.
     *
     * @param message The message to be edited. It must be a non-null instance of
     *                {@link TextMessage}.
     */
    private void showEditMessagePreview(BaseMessage message) {
        if (message != null) {
            editMessage = (TextMessage) message;

            binding.editPreviewLayout.tvMessageLayoutTitle.setText(getResources().getString(R.string.cometchat_edit));
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(((TextMessage) message).getText());
            for (CometChatTextFormatter textFormatter : cometchatTextFormatters) {
                if (textFormatter != null)
                    spannableStringBuilder = textFormatter.prepareMessageString(binding.editPreviewLayout.getRoot().getContext(),
                                                                                message,
                                                                                spannableStringBuilder,
                                                                                null,
                                                                                UIKitConstants.FormattingType.MESSAGE_COMPOSER);
            }

            binding.editPreviewLayout.tvMessageLayoutSubtitle.setText(spannableStringBuilder + "");
            binding.messageInput.setSpannableText(SpannableString.valueOf(spannableStringBuilder));
            String editMessageString = SpannableString.valueOf(spannableStringBuilder) + "";
            binding.messageInput.setSelection(editMessageString.length());
            binding.messageInput.getComposeBox().setFocusable(true);
            binding.editPreviewLayout.ivMessageClose.setOnClickListener(view1 -> {
                editMessage = null;
                animateVisibilityGone(binding.editPreviewLayout.editMessageLayout);
            });
            animateVisibilityVisible(binding.editPreviewLayout.editMessageLayout);
        }
    }

    /**
     * Handles the successful editing of a message.
     *
     * <p>
     * This method is called when a message has been successfully edited. It resets
     * the editing state by nullifying the `editMessage` variable and hiding the
     * edit message layout from the UI. This indicates to the user that the edit
     * operation has been completed successfully.
     *
     * @param message The edited message. This parameter can be non-null, indicating the
     *                successful editing of a message.
     */
    private void onMessageEditSuccess(BaseMessage message) {
        if (message != null) {
            editMessage = null;
            animateVisibilityGone(binding.editPreviewLayout.editMessageLayout);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
        super.onDetachedFromWindow();
        composerViewModel.removeListeners();
    }

    /**
     * Initializes the send button functionality for the message composer.
     *
     * <p>
     * This method sets the drawable for the send button to the inactive state and
     * configures the button's click listener. When the send button is clicked, it
     * performs the following actions:
     *
     * <ul>
     * <li>If a custom click listener {@link #sendButtonClick} is defined, it
     * invokes the listener with the context and the processed text message.
     * <li>If no custom listener is defined, it retrieves the processed text from
     * the input field, trims any whitespace, and checks if it is not empty.
     * <li>If editing an existing message, it updates the message with the processed
     * text, calls {@link #handleMessagePreSend(BaseMessage)}, and sends the edited
     * message.
     * <li>If not editing, it creates a new {@link TextMessage} using the processed
     * text, handles it with {@link #handleMessagePreSend(BaseMessage)}, and sends
     * the new message.
     * </ul>
     * <p>
     * Finally, it clears the input field after the message is sent or edited.
     */
    private void initializeSendButton() {
        sendButtonLayoutBinding.ivSendBtn.setImageDrawable(inactiveSendButtonDrawable);
        sendButtonLayoutBinding.sendButton.setOnClickListener(view1 -> {
            if (sendButtonClick != null) {
                sendButtonClick.onClick(getContext(), composerViewModel.getTextMessage(getProcessedText()));
            } else {
                String processedText = getProcessedText().trim();
                if (!processedText.isEmpty()) {
                    if (editMessage != null) {
                        editMessage.setText(getProcessedText());
                        handleMessagePreSend(editMessage);
                        composerViewModel.editMessage(editMessage);
                    } else {
                        TextMessage textMessage1 = composerViewModel.getTextMessage(getProcessedText());
                        handleMessagePreSend(textMessage1);
                        composerViewModel.sendTextMessage(textMessage1);
                    }
                }
            }
            binding.messageInput.setText("");
        });

        binding.messageInput.setPrimaryButtonView(sendButtonLayoutBinding.getRoot());
    }

    /**
     * Processes the given message before sending it, applying all registered text
     * formatters.
     *
     * <p>
     * This method iterates through the registered {@link CometChatTextFormatter}
     * instances and invokes their
     * {@link CometChatTextFormatter#handlePreMessageSend} method to allow each
     * formatter to perform any necessary modifications or checks on the provided
     * message. After processing, it clears the suggestion item list for each
     * formatter.
     *
     * @param baseMessage The {@link BaseMessage} to be processed before sending. This
     *                    message may be modified by the formatters.
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
     * Retrieves the processed text from the message input field, replacing any
     * non-editable spans with their underlying text.
     *
     * <p>
     * This method fetches the current editable text from the message input. It
     * identifies all non-editable spans present in the text and replaces each span
     * with its corresponding underlying text. The resulting string is returned.
     *
     * @return A {@link String} representing the processed text, with non-editable
     * spans replaced by their underlying text.
     */
    public String getProcessedText() {
        Editable editableText = binding.messageInput.getEditableText();
        NonEditableSpan[] spans = editableText.getSpans(0, editableText.length(), NonEditableSpan.class);

        for (NonEditableSpan span : spans) {
            int spanStart = editableText.getSpanStart(span);
            int spanEnd = editableText.getSpanEnd(span);

            // Replace the span with its underlying text.
            String underlyingText = span.getSuggestionItem().getUnderlyingText();
            editableText.replace(spanStart, spanEnd, underlyingText);
        }
        return editableText.toString();
    }

    private void initializeActionSheets() {
        cometchatAiButtonLayoutBinding.ivAiBot.setOnClickListener(view1 -> openAIOptionSheet());
    }

    private void initializeSecondaryView() {
        secondaryButtonLayoutBinding.ivAttachments.setOnClickListener(v -> openAttachmentOptionSheet());
        secondaryButtonLayoutBinding.ivMicrophone.setOnClickListener(view1 -> {
            requestMicrophonePermission();
        });
        binding.messageInput.setSecondaryButtonView(secondaryButtonLayoutBinding.getRoot());
    }

    private void openAttachmentOptionSheet() {
        try {
            setComposerActions();
            CometChatAttachmentOptionSheet cometchatAttachmentOptionSheet = new CometChatAttachmentOptionSheet(getContext());
            cometchatAttachmentOptionSheet.setAttachmentOptionItems(attachmentOptionSheetMenuItems);
            cometchatAttachmentOptionSheet.setStyle(attachmentOptionSheetStyle);
            cometchatAttachmentOptionSheet.setAttachmentOptionClickListener(menuIem -> {
                CometChatMessageComposerAction action = actionHashMap.get(menuIem.getId());
                if (action != null) {
                    if (action.getOnClick() != null) {
                        action.getOnClick().onClick();
                        bottomSheetDialog.dismiss();
                    } else {
                        CometChatLogger.e(TAG, "openAttachmentOptionSheet: action is null" + menuIem.getText());
                        handleAttachmentOptionItemClick(menuIem);
                        bottomSheetDialog.dismiss();
                    }
                }
            });
            showBottomSheet(bottomSheetDialog, true, cometchatAttachmentOptionSheet);
        } catch (Exception ignored) {
        }
    }

    /**
     * Opens the CometChat media recorder sheet for recording audio messages.
     *
     * <p>
     * This method creates a new `CometChatMediaRecorder` instance, configures its
     * style and click listeners, and then displays it using the provided
     * `BottomSheetDialog`.
     *
     * <p>
     * - If a `sendButtonClick` listener is defined, it's used to send the recorded
     * audio message. - Otherwise, the `sendMediaMessage` method is called directly
     * to send the message.
     *
     * <p>
     * In both cases, the `BottomSheetDialog` is dismissed after the recording is
     * finished.
     */
    private void openMediaRecorderSheet() {
        try {
            CometChatMediaRecorder cometchatMediaRecorder = new CometChatMediaRecorder(getContext());
            cometchatMediaRecorder.setStyle(mediaRecorderStyle);
            cometchatMediaRecorder.setOnCloseClickListener(bottomSheetDialog::dismiss);
            cometchatMediaRecorder.startRecording();
            cometchatMediaRecorder.setOnSubmitClickListener((file, context) -> {
                if (sendButtonClick != null) {
                    sendButtonClick.onClick(context, composerViewModel.getMediaMessage(file, UIKitConstants.MessageType.AUDIO));
                } else if (file != null) {
                    sendMediaMessage(file, UIKitConstants.MessageType.AUDIO);
                }
                cometchatMediaRecorder.stopRecording();
                bottomSheetDialog.dismiss();
            });
            showBottomSheet(bottomSheetDialog, true, cometchatMediaRecorder);
        } catch (Exception ignored) {
        }
    }

    /**
     * Displays a bottom sheet dialog with the provided content view.
     *
     * <p>
     * This method removes the view from any existing parent, sets it as the content
     * view for the `BottomSheetDialog`, sets a listener to customize the background
     * on show, configures cancelable behavior, and then shows the dialog.
     *
     * @param bottomSheetDialog The `BottomSheetDialog` to use.
     * @param isCancelable      Whether the dialog can be dismissed by the user.
     * @param view              The view to display as the content of the dialog.
     * @throws Exception If any error occurs while displaying the bottom sheet.
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
        } catch (Exception ignored) {
        }
    }

    /**
     * Sets whether to disable sound for messages.
     *
     * @param disableSoundForMessages {@code true} to disable sound for messages, {@code false}
     *                                otherwise.
     */
    public void disableSoundForMessages(boolean disableSoundForMessages) {
        this.disableSoundForMessages = disableSoundForMessages;
    }

    /**
     * Sets whether to disable typing events.
     *
     * @param disableTypingEvents {@code true} to disable typing events, {@code false} otherwise.
     */
    public void disableTypingEvents(boolean disableTypingEvents) {
        this.disableTypingEvents = disableTypingEvents;
    }

    /**
     * Sets the placeholder text of the input field.
     *
     * @param placeHolderText The placeholder text to set.
     */
    public void setPlaceHolderText(String placeHolderText) {
        binding.messageInput.setPlaceHolderText(placeHolderText);
    }

    /**
     * Sets the listener for text changed events in the message input field.
     *
     * @param textChangedListener The text changed listener to set.
     */
    public void setOnTextChangedListener(CometChatTextWatcher textChangedListener) {
        if (textChangedListener != null)
            binding.messageInput.setOnTextChangedListener(textChangedListener);
    }

    /**
     * Sets the maximum number of lines for the input field.
     *
     * @param maxLine The maximum number of lines to set.
     */
    public void setMaxLine(int maxLine) {
        binding.messageInput.setMaxLine(maxLine);
    }

    /**
     * Sets the attachment options for the composer.
     *
     * @param cometchatMessageComposerActions The function to retrieve the list of composer actions.
     */
    public void setAttachmentOptions(List<CometChatMessageComposerAction> cometchatMessageComposerActions) {
        if (cometchatMessageComposerActions != null) {
            this.messageComposerActions = cometchatMessageComposerActions;
        }
    }

    /**
     * Sets up the composer actions for the message composer.
     *
     * <p>
     * This method clears the existing option sheet menu items, retrieves the
     * available composer actions from the `cometchatMessageComposerActions`
     * callback, creates option sheet menu items for each action, adds the items to
     * the `optionSheetMenuItems` list, and stores the actions in the
     * `actionHashMap`.
     */
    private void setComposerActions() {
        attachmentOptionSheetMenuItems.clear();
        List<CometChatMessageComposerAction> tempMessageComposerActions;
        if (messageComposerActions == null || messageComposerActions.isEmpty()) {
            tempMessageComposerActions = CometChatUIKit
                .getDataSource()
                .getAttachmentOptions(getContext(), user, group, composerViewModel.getIdMap(), additionParameter);
        } else {
            tempMessageComposerActions = messageComposerActions;
        }

        for (CometChatMessageComposerAction option : tempMessageComposerActions) {
            if (option != null) {
                actionHashMap.put(option.getId(), option);
                attachmentOptionSheetMenuItems.add(new OptionSheetMenuItem(option.getId(),
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
     * @param aiOptions The function to retrieve the list of composer actions.
     */
    public void setAIOptions(Function4<Context, User, Group, HashMap<String, String>, List<CometChatMessageComposerAction>> aiOptions) {
        if (aiOptions != null) {
            this.aiOptions = aiOptions;
            setAIActions();
        }
    }

    /**
     * Sets up the AI actions for the message composer. This method retrieves the
     * available AI options from the `aiOptions` callback, creates action items for
     * each option, adds the AI button layout to the auxiliary view container, and
     * sets the list of action items to the AI action sheet.
     */
    private void setAIActions() {
        aiOptionSheetMenuItems.clear();
        if (aiOptions != null) {
            List<CometChatMessageComposerAction> actions = aiOptions.invoke(getContext(), user, group, composerViewModel.getIdMap());
            if (actions != null && !actions.isEmpty()) {
                auxiliaryViewContainer.addView(cometchatAiButtonLayoutBinding.getRoot());
                for (CometChatMessageComposerAction option : actions) {
                    if (option != null) {
                        actionHashMap.put(option.getId(), option);
                        aiOptionSheetMenuItems.add(new OptionSheetMenuItem(option.getId(),
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

    public List<OptionSheetMenuItem> getAIOptionItems() {
        return aiOptionSheetMenuItems;
    }

    public List<OptionSheetMenuItem> getAttachmentActionItems() {
        return attachmentOptionSheetMenuItems;
    }

    /**
     * Sets the alignment of the auxiliary button.
     *
     * @param alignment The alignment to set for the auxiliary button.
     */
    public void setAuxiliaryButtonAlignment(UIKitConstants.AuxiliaryButtonAlignment alignment) {
        binding.messageInput.setAuxiliaryButtonAlignment(alignment);
    }

    /**
     * Sets the listener for send button clicks.
     *
     * @param sendButtonClick The callback function to be invoked when the send button is
     *                        clicked.
     */
    public void setOnSendButtonClick(SendButtonClick sendButtonClick) {
        this.sendButtonClick = sendButtonClick;
    }

    /**
     * Retrieves the configured addition parameter object.
     *
     * @return the AdditionParameter.
     */
    public AdditionParameter getAdditionParameter() {
        return additionParameter;
    }

    /**
     * Gets the `MessageComposerViewModel` instance associated with this message
     * composer.
     *
     * @return The `MessageComposerViewModel` instance.
     */
    public MessageComposerViewModel getComposerViewModel() {
        return composerViewModel;
    }

    /**
     * Gets the `CometChatMessageInput` instance used for entering text messages.
     *
     * @return The `CometChatMessageInput` instance.
     */
    public CometChatMessageInput getMessageInput() {
        return binding.messageInput;
    }

    /**
     * Gets the root view of this message composer component.
     *
     * @return The root view of the message composer.
     */
    public LinearLayout getView() {
        return binding.getRoot();
    }

    /**
     * Gets the current user information.
     *
     * @return The current user information.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user for the message receiver.
     *
     * @param user The user object to set as the receiver.
     */
    public void setUser(User user) {
        if (user != null) {
            this.user = user;
            this.id = user.getUid();
            this.type = UIKitConstants.ReceiverType.USER;
            group = null;
            composerViewModel.setGroup(null);
            composerViewModel.setUser(user);
            processFormatters();
            setAuxiliaryButtonViewInternally();
        }
    }

    /**
     * sends the typing indicator
     */
    private void processFormatters() {
        cometchatTextFormatterHashMap = new HashMap<>();
        for (CometChatTextFormatter formatter : cometchatTextFormatters) {
            if (formatter != null) {
                formatter.getSuggestionItemList().observe((LifecycleOwner) getContext(), this::setTagList);
                formatter.getTagInfoMessage().observe((LifecycleOwner) getContext(), this::setInfoMessage);
                formatter.getTagInfoVisibility().observe((LifecycleOwner) getContext(), this::setInfoVisibility);
                formatter.getShowLoadingIndicator().observe((LifecycleOwner) getContext(), this::setLoadingStateVisibility);
                if (user != null) {
                    formatter.setUser(user);
                    formatter.setGroup(null);
                } else if (group != null) {
                    formatter.setGroup(group);
                    formatter.setUser(null);
                }
                if (formatter.getTrackingCharacter() != '\0')
                    cometchatTextFormatterHashMap.put(formatter.getTrackingCharacter(), formatter);
            }
        }
    }

    /**
     * Sets the auxiliary view for the message composer.
     */
    private void setAuxiliaryButtonViewInternally() {
        auxiliaryViewContainer.removeAllViews();
        View view;
        if (auxiliaryButtonView == null) {
            view = ChatConfigurator.getDataSource().getAuxiliaryOption(getContext(), user, group, composerViewModel.getIdMap(), additionParameter);
        } else {
            view = auxiliaryButtonView.invoke(getContext(), user, group, composerViewModel.getIdMap());
        }
        if (view != null) auxiliaryViewContainer.addView(view);
        setAIActions();
        binding.messageInput.setAuxiliaryButtonView(auxiliaryViewContainer);
    }

    /**
     * Sets the list of suggestion items for the tag list.
     *
     * <p>
     * This method updates the suggestion list with the provided list of
     * {@link SuggestionItem}s. If the list is not empty and the temporary text
     * formatter is available, the suggestion list is displayed with the provided
     * items. If the list is empty or null, the suggestion list will be hidden.
     *
     * @param suggestionItems A list of {@link SuggestionItem}s to be displayed in the
     *                        suggestion list. If this is null or empty, the suggestion list
     *                        will be hidden.
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
     * @param visibility sets the visibility of the info view
     */
    public void setInfoVisibility(boolean visibility) {
        if (visibility) {
            if (binding.tagInfoParentLay.getVisibility() != VISIBLE) {
                animateVisibilityVisible(binding.tagInfoParentLay);
            }
        } else {
            animateVisibilityGone(binding.tagInfoParentLay);
        }
    }

    /**
     * Sets the visibility of the loading state in the suggestion list.
     *
     * <p>
     * This method updates the suggestion list's loading indicator based on the
     * provided boolean value. If {@code true}, the loading state is visible; if
     * {@code false}, it is hidden.
     *
     * @param aBoolean A boolean value indicating whether to show or hide the loading
     *                 state.
     */
    private void setLoadingStateVisibility(Boolean aBoolean) {
        getCometChatSuggestionList().showShimmer(aBoolean);
    }

    /**
     * Gets the `CometChatSuggestionList` instance used for displaying suggestions.
     *
     * @return The `CometChatSuggestionList` instance.
     */
    public CometChatSuggestionList getCometChatSuggestionList() {
        return binding.suggestionList;
    }

    /**
     * Gets the group information for the current conversation.
     *
     * @return The group information.
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Sets the group for the message receiver.
     *
     * @param group The group object to set as the receiver.
     */
    public void setGroup(Group group) {
        if (group != null) {
            this.group = group;
            this.id = group.getGuid();
            this.type = UIKitConstants.ReceiverType.GROUP;
            user = null;
            composerViewModel.setUser(null);
            composerViewModel.setGroup(group);
            processFormatters();
            setAuxiliaryButtonViewInternally();
        }
    }

    /**
     * Gets the visibility state of the attachment button.
     *
     * @return The visibility state of the attachment button.
     */
    public int getAttachmentButtonVisibility() {
        return attachmentButtonVisibility;
    }

    /**
     * Sets the visibility of the attachment button in the secondary button layout.
     *
     * <p>
     * This method allows you to control whether the attachment button is displayed
     * or hidden. The visibility can be set to one of the following values:
     *
     * <ul>
     * <li>View.VISIBLE - to show the attachment button.
     * <li>View.INVISIBLE - to hide the attachment button but still reserve space
     * for it.
     * <li>View.GONE - to hide the attachment button and do not reserve space for
     * it.
     * </ul>
     *
     * @param attachmentVisibility The visibility state to set for the attachment button.
     */
    public void setAttachmentButtonVisibility(int attachmentVisibility) {
        this.attachmentButtonVisibility = attachmentVisibility;
        secondaryButtonLayoutBinding.ivAttachments.setVisibility(attachmentVisibility);
    }

    /**
     * Checks if sound is disabled for incoming messages.
     *
     * @return `true` if sound is disabled, `false` otherwise.
     */
    public boolean isDisableSoundForMessages() {
        return disableSoundForMessages;
    }

    /**
     * Gets the custom sound ID for incoming messages.
     *
     * @return The custom sound ID, or `0` if no custom sound is set.
     */
    public int getCustomSoundForMessages() {
        return customSoundForMessages;
    }

    /**
     * Sets a custom sound for messages.
     *
     * @param customSoundForMessages The resource ID of the custom sound to set for messages.
     */
    public void setCustomSoundForMessages(@RawRes int customSoundForMessages) {
        this.customSoundForMessages = customSoundForMessages;
    }

    /**
     * Checks if typing events are disabled for this message composer.
     *
     * @return `true` if typing events are disabled, `false` otherwise.
     */
    public boolean isDisableTypingEvents() {
        return disableTypingEvents;
    }

    /**
     * Gets the `CometChatSoundManager` instance used for managing audio playback.
     *
     * @return The `CometChatSoundManager` instance.
     */
    public CometChatSoundManager getSoundManager() {
        return soundManager;
    }

    /**
     * Gets the drawable used for the attachment button icon.
     *
     * @return The attachment button icon drawable.
     */
    public Drawable getAttachmentIcon() {
        return attachmentIcon;
    }

    /**
     * Sets the icon for the attachment button.
     *
     * @param attachmentIcon The drawable of the attachment icon to set.
     */
    public void setAttachmentIcon(Drawable attachmentIcon) {
        this.attachmentIcon = attachmentIcon;
        if (attachmentIcon != null) {
            secondaryButtonLayoutBinding.ivAttachments.setImageDrawable(attachmentIcon);
        }
    }

    /**
     * Gets the tint color for the attachment button icon.
     *
     * @return The attachment button icon tint color.
     */
    public int getAttachmentIconTint() {
        return attachmentIconTint;
    }

    /**
     * Sets the tint color for the attachment icon.
     *
     * <p>
     * This method updates the tint color of the attachment icon in the secondary
     * button layout.
     *
     * @param color The color to set as the tint for the attachment icon.
     */
    public void setAttachmentIconTint(@ColorInt int color) {
        this.attachmentIconTint = color;
        secondaryButtonLayoutBinding.ivAttachments.setImageTintList(ColorStateList.valueOf(color));
    }

    /**
     * Gets the ID of the parent message for a reply.
     *
     * @return The parent message ID, or `0` if it's not a reply.
     */
    public int getParentMessageId() {
        return parentMessageId;
    }

    /**
     * Sets the parent message ID for the composer.
     *
     * @param parentMessageId The parent message ID.
     */
    public void setParentMessageId(int parentMessageId) {
        this.parentMessageId = parentMessageId;
        composerViewModel.setParentMessageId(parentMessageId);
    }

    /**
     * Gets the callback function for handling the send button click.
     *
     * @return The send button click callback function.
     */
    public SendButtonClick getSendButtonClick() {
        return sendButtonClick;
    }

    /**
     * Gets the visibility state of the voice note button.
     *
     * @return The visibility state of the voice note button.
     */
    public int getVoiceNoteButtonVisibility() {
        return voiceNoteButtonVisibility;
    }

    /**
     * Sets the visibility of the voice recording view in the media recorder.
     *
     * @param voiceRecordingVisibility The visibility constant for the voice recording view. Use
     *                                 {@link View#VISIBLE}, {@link View#INVISIBLE}, or
     *                                 {@link View#GONE}.
     */
    public void setVoiceNoteButtonVisibility(int voiceRecordingVisibility) {
        this.voiceNoteButtonVisibility = voiceRecordingVisibility;
        secondaryButtonLayoutBinding.ivMicrophone.setVisibility(voiceRecordingVisibility);
    }

    /**
     * Gets the text content that the user has entered in the message composer.
     *
     * @return The text content of the message.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text of the input field.
     *
     * @param text The text to set.
     */
    public void setInitialComposerText(String text) {
        binding.messageInput.setText(text);
    }

    /**
     * Gets the type of message being composed.
     *
     * @return The message type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the message being edited, if applicable.
     *
     * @return The message being edited, or `null` if no message is being edited.
     */
    public TextMessage getEditMessage() {
        return editMessage;
    }

    /**
     * Gets the message that is ready to be sent.
     *
     * @return The message to be sent.
     */
    public BaseMessage getSendMessage() {
        return sendMessage;
    }

    /**
     * Gets the header view for the message composer.
     *
     * @return The header view.
     */
    public View getHeaderView() {
        return headerView;
    }

    /**
     * Sets the header view.
     *
     * @param headerView The header view to set.
     */
    public void setHeaderView(View headerView) {
        this.headerView = headerView;
        Utils.handleView(binding.headerViewLayout, headerView, false);
        binding.headerViewLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Gets the footer view for the message composer.
     *
     * @return The footer view.
     */
    public View getFooterView() {
        return footerView;
    }

    /**
     * Sets the footer view.
     *
     * @param footerView The footer view to set.
     */
    public void setFooterView(View footerView) {
        this.footerView = footerView;
        Utils.handleView(binding.footerViewLayout, footerView, false);
    }

    /**
     * Gets the view for the send button.
     *
     * @return The send button view.
     */
    @Nullable
    public View getSendButtonView() {
        return sendButtonView;
    }

    /**
     * Sets the send button view for the composer.
     *
     * @param sendButtonView The send button view.
     */
    public void setSendButtonView(View sendButtonView) {
        if (sendButtonView != null) {
            this.sendButtonView = sendButtonView;
            binding.messageInput.setPrimaryButtonView(sendButtonView);
        }
    }

    /**
     * Gets the style resource ID for the action sheet.
     *
     * @return The action sheet style resource ID.
     */
    public @StyleRes int getActionSheetStyle() {
        return actionSheetStyle;
    }

    /**
     * Sets the style for the action sheet.
     *
     * <p>
     * This method allows customization of the appearance of the action sheet by
     * applying a specified style resource. The style can define various attributes
     * such as background color, text appearance, and other visual properties of the
     * action sheet.
     *
     * @param actionSheetStyle The resource ID of the style to be applied to the action sheet.
     *                         This should be a valid style resource defined in XML.
     */
    private void setActionSheetStyle(@StyleRes int actionSheetStyle) {
        this.actionSheetStyle = actionSheetStyle;
    }

    /**
     * Gets the callback function for handling errors.
     *
     * @return The error callback function.
     */
    public OnError getOnError() {
        return onError;
    }

    /**
     * Sets the error listener for the message composer. The error listener will be
     * triggered when an error occurs during message composition or sending.
     *
     * @param onError The {@link OnError} listener to be set.
     */
    public void setOnError(OnError onError) {
        this.onError = onError;
    }

    /**
     * Gets the callback function used to retrieve the auxiliary button view.
     *
     * @return The auxiliary button view callback function.
     */
    public Function4<Context, User, Group, HashMap<String, String>, View> getAuxiliaryButtonView() {
        return auxiliaryButtonView;
    }

    /**
     * Sets the auxiliary button view for the composer.
     *
     * @param auxiliaryButtonView The function to retrieve the auxiliary button view.
     */
    public void setAuxiliaryButtonView(View auxiliaryButtonView) {
        if (auxiliaryButtonView != null) {
            this.auxiliaryButton = auxiliaryButtonView;
            binding.messageInput.setAuxiliaryButtonView(auxiliaryButtonView);
        }
    }

    /**
     * Gets the style resource ID for the media recorder.
     *
     * @return The media recorder style resource ID.
     */
    public @StyleRes int getCometChatMediaRecorder() {
        return mediaRecorderStyle;
    }

    /**
     * Gets the style resource ID for the attachment option sheet.
     *
     * @return The attachment option sheet style resource ID.
     */
    public @StyleRes int getAttachmentOptionSheetStyle() {
        return attachmentOptionSheetStyle;
    }

    public void setAttachmentOptionSheetStyle(@StyleRes int attachmentOptionSheetStyle) {
        this.attachmentOptionSheetStyle = attachmentOptionSheetStyle;
    }

    /**
     * Gets the style resource ID for the AI option sheet.
     *
     * @return The AI option sheet style resource ID.
     */
    public @StyleRes int getAIOptionSheetStyle() {
        return aiOptionSheetStyle;
    }

    public void setAIOptionSheetStyle(@StyleRes int aiOptionSheetStyle) {
        this.aiOptionSheetStyle = aiOptionSheetStyle;
    }

    /**
     * Gets the list of `CometChatTextFormatter` instances used for formatting text.
     *
     * @return The list of text formatters.
     */
    public List<CometChatTextFormatter> getCometChatTextFormatters() {
        return cometchatTextFormatters;
    }

    /**
     * Checks if mentions are disabled for this message composer.
     *
     * @return `true` if mentions are disabled, `false` otherwise.
     */
    public boolean isDisableMentions() {
        return disableMentions;
    }

    /**
     * Enables or disables the mentions feature in the message composer.
     *
     * <p>
     * This method allows you to disable or enable mentions functionality. When
     * disabled, the mentions formatter ({@link CometChatMentionsFormatter}) is
     * removed from the list of text formatters, and the {@code processFormatters()}
     * method is called to update the formatting accordingly.
     *
     * @param disable A boolean value indicating whether to disable mentions. If
     *                {@code true}, mentions are disabled; otherwise, they remain
     *                enabled.
     */
    public void setDisableMentions(boolean disable) {
        this.disableMentions = disable;
        if (disable) {
            cometchatTextFormatters.remove(cometchatMentionsFormatter);
            processFormatters();
        }
    }

    /**
     * Gets the parent layout for the tag info view.
     *
     * @return The parent layout for the tag info view.
     */
    public LinearLayout getTagInfoParentLayout() {
        return binding.tagInfoParentLay;
    }

    /**
     * Gets the text view for displaying the tag info message.
     *
     * @return The tag info message text view.
     */
    public TextView getInfoMessage() {
        return binding.tagInfoMessage;
    }    /**
     * @param strokeWidth The new width to set for the stroke
     */
    @Override
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    /**
     * sets text for info message
     */
    public void setInfoMessage(String infoMessage) {
        if (infoMessage != null && !infoMessage.isEmpty())
            binding.tagInfoMessage.setText(infoMessage);
    }

    /**
     * Gets the image view for displaying the tag info icon.
     *
     * @return The tag info icon image view.
     */
    public ImageView getTagInfoIcon() {
        return binding.tagInfoIcon;
    }

    /**
     * Gets the item click listener for the suggestion list.
     *
     * @return The item click listener.
     */
    public OnItemClickListener<SuggestionItem> getOnItemClickListener() {
        return onItemClickListener;
    }

    /**
     * Gets the background drawable for the message composer.
     *
     * @return The background drawable.
     */
    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * @param backgroundDrawable The new drawable to set for the background
     */
    @Override
    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        if (backgroundDrawable != null) {
            this.backgroundDrawable = backgroundDrawable;
            super.setBackgroundDrawable(backgroundDrawable);
        }
    }

    /**
     * Gets the background color for the compose box.
     *
     * @return The compose box background color.
     */
    public @ColorInt int getComposeBoxBackgroundColor() {
        return composeBoxBackgroundColor;
    }

    /**
     * Gets the stroke width for the compose box.
     *
     * @return The compose box stroke width.
     */
    public @Dimension int getComposeBoxStrokeWidth() {
        return composeBoxStrokeWidth;
    }

    /**
     * @param strokeWidth The new width to set for the compose box card stroke
     */
    public void setComposeBoxStrokeWidth(@Dimension int strokeWidth) {
        this.composeBoxStrokeWidth = strokeWidth;
        binding.composeBoxCard.setStrokeWidth(strokeWidth);
    }

    /**
     * Gets the stroke color for the compose box.
     *
     * @return The compose box stroke color.
     */
    public @ColorInt int getComposeBoxStrokeColor() {
        return composeBoxStrokeColor;
    }

    /**
     * @param strokeColor The new color to set for the compose box card stroke
     */
    public void setComposeBoxStrokeColor(@ColorInt int strokeColor) {
        this.composeBoxStrokeColor = strokeColor;
        binding.composeBoxCard.setStrokeColor(strokeColor);
    }

    /**
     * Gets the corner radius for the compose box.
     *
     * @return The compose box corner radius.
     */
    public @Dimension int getComposeBoxCornerRadius() {
        return composeBoxCornerRadius;
    }

    /**
     * @param cornerRadius The new radius to set for the compose box card
     */
    public void setComposeBoxCornerRadius(@Dimension int cornerRadius) {
        this.composeBoxCornerRadius = cornerRadius;
        binding.composeBoxCard.setRadius(cornerRadius);
    }

    /**
     * Gets the background drawable for the compose box.
     *
     * @return The compose box background drawable.
     */
    public Drawable getComposeBoxBackgroundDrawable() {
        return composeBoxBackgroundDrawable;
    }

    /**
     * @param backgroundDrawable The new drawable to set for the compose box card background
     */
    public void setComposeBoxBackgroundDrawable(Drawable backgroundDrawable) {
        if (backgroundDrawable != null) {
            this.composeBoxBackgroundDrawable = backgroundDrawable;
            binding.composeBoxCard.setBackgroundDrawable(backgroundDrawable);
        }
    }

    /**
     * Gets the separator color for the message composer.
     *
     * @return The separator color.
     */
    public @ColorInt int getSeparatorColor() {
        return separatorColor;
    }

    /**
     * @param separatorColor The new color to set for the attachment icon
     */
    public void setSeparatorColor(@ColorInt int separatorColor) {
        this.separatorColor = separatorColor;
        binding.messageInput.setSeparatorColor(separatorColor);
    }

    /**
     * Gets the drawable used for the voice recording button icon.
     *
     * @return The voice recording button icon drawable.
     */
    public Drawable getVoiceRecordingIcon() {
        return voiceRecordingIcon;
    }

    /**
     * @param voiceRecordingIcon The new drawable to set for the voice recording icon
     */
    public void setVoiceRecordingIcon(Drawable voiceRecordingIcon) {
        this.voiceRecordingIcon = voiceRecordingIcon;
        secondaryButtonLayoutBinding.ivMicrophone.setImageDrawable(voiceRecordingIcon);
    }

    /**
     * Gets the tint color for the voice recording button icon.
     *
     * @return The voice recording button icon tint color.
     */
    public @ColorInt int getVoiceRecordingIconTint() {
        return voiceRecordingIconTint;
    }

    /**
     * Sets the tint color for the voice recording icon in the media recorder.
     *
     * @param color The color integer value to set as the tint color for the voice
     *              recording icon.
     */
    public void setVoiceRecordingIconTint(@ColorInt int color) {
        this.voiceRecordingIconTint = color;
        secondaryButtonLayoutBinding.ivMicrophone.setImageTintList(ColorStateList.valueOf(color));
    }

    /**
     * Gets the drawable used for the AI button icon.
     *
     * @return The AI button icon drawable.
     */
    public Drawable getAIIcon() {
        return AIIcon;
    }

    /**
     * @param AIIcon The new Drawable AI to set for the AI icon
     */
    public void setAIIcon(Drawable AIIcon) {
        this.AIIcon = AIIcon;
        cometchatAiButtonLayoutBinding.ivAiBot.setImageDrawable(AIIcon);
    }

    /**
     * Gets the tint color for the AI button icon.
     *
     * @return The AI button icon tint color.
     */
    public @ColorInt int getAIIconTint() {
        return AIIconTint;
    }

    /**
     * Sets the tint color for the AI icon in the message composer.
     *
     * @param color The color integer value to set as the tint color for the AI icon.
     */
    public void setAIIconTint(@ColorInt int color) {
        this.AIIconTint = color;
        cometchatAiButtonLayoutBinding.ivAiBot.setImageTintList(ColorStateList.valueOf(color));
    }

    /**
     * Gets the drawable used for the inactive sticker icon.
     *
     * @return The inactive sticker icon drawable.
     */
    public Drawable getInactiveStickerIcon() {
        return inactiveStickerIcon;
    }

    /**
     * @param inactiveStickerIcon The new color to set for the inactive sticker icon
     */
    public void setInactiveStickerIcon(Drawable inactiveStickerIcon) {
        this.inactiveStickerIcon = inactiveStickerIcon;
        additionParameter.setInactiveStickerIcon(inactiveStickerIcon);
    }

    /**
     * Gets the tint color for the inactive sticker icon.
     *
     * @return The inactive sticker icon tint color.
     */
    public @ColorInt int getInactiveStickerIconTint() {
        return inactiveStickerIconTint;
    }

    /**
     * @param inactiveStickerIconTint The new inactive stickerIconTint to set for the attachment icon
     */
    public void setInactiveStickerIconTint(@ColorInt int inactiveStickerIconTint) {
        this.inactiveStickerIconTint = inactiveStickerIconTint;
        additionParameter.setInactiveAuxiliaryIconTint(inactiveStickerIconTint);
    }

    /**
     * Gets the drawable used for the active send button.
     *
     * @return The active send button drawable.
     */
    public Drawable getActiveSendButtonDrawable() {
        return activeSendButtonDrawable;
    }

    /**
     * @param activeSendButtonDrawable The new drawable to set for the active send button
     */
    public void setActiveSendButtonDrawable(Drawable activeSendButtonDrawable) {
        this.activeSendButtonDrawable = activeSendButtonDrawable;
        sendButtonLayoutBinding.ivSendBtn.setBackground(activeSendButtonDrawable);
    }

    /**
     * Gets the drawable used for the inactive send button.
     *
     * @return The inactive send button drawable.
     */
    public Drawable getInactiveSendButtonDrawable() {
        return inactiveSendButtonDrawable;
    }

    /**
     * @param inactiveSendButtonDrawable The new drawable to set for the inactive send button
     */
    public void setInactiveSendButtonDrawable(Drawable inactiveSendButtonDrawable) {
        this.inactiveSendButtonDrawable = inactiveSendButtonDrawable;
        sendButtonLayoutBinding.ivSendBtn.setBackground(inactiveSendButtonDrawable);
    }

    /**
     * Gets the text appearance resource ID for the edit preview title.
     *
     * @return The edit preview title text appearance resource ID.
     */
    public @StyleRes int getEditPreviewTitleTextAppearance() {
        return editPreviewTitleTextAppearance;
    }

    /**
     * @param editPreviewTitleTextAppearance The new style to set for the title text appearance icon
     */
    public void setEditPreviewTitleTextAppearance(@StyleRes int editPreviewTitleTextAppearance) {
        this.editPreviewTitleTextAppearance = editPreviewTitleTextAppearance;
        binding.editPreviewLayout.tvMessageLayoutTitle.setTextAppearance(editPreviewTitleTextAppearance);
    }

    /**
     * Gets the text appearance resource ID for the edit preview message.
     *
     * @return The edit preview message text appearance resource ID.
     */
    public @StyleRes int getEditPreviewMessageTextAppearance() {
        return editPreviewMessageTextAppearance;
    }

    /**
     * @param editPreviewMessageTextAppearance The new style to set for the message text appearance icon
     */
    public void setEditPreviewMessageTextAppearance(@StyleRes int editPreviewMessageTextAppearance) {
        this.editPreviewMessageTextAppearance = editPreviewMessageTextAppearance;
        binding.editPreviewLayout.tvMessageLayoutSubtitle.setTextAppearance(editPreviewMessageTextAppearance);
    }

    /**
     * Gets the text color for the edit preview title.
     *
     * @return The edit preview title text color.
     */
    public @ColorInt int getEditPreviewTitleTextColor() {
        return editPreviewTitleTextColor;
    }

    /**
     * @param editPreviewTitleTextColor The new color to set for the title text color
     */
    public void setEditPreviewTitleTextColor(@ColorInt int editPreviewTitleTextColor) {
        this.editPreviewTitleTextColor = editPreviewTitleTextColor;
        binding.editPreviewLayout.tvMessageLayoutTitle.setTextColor(editPreviewTitleTextColor);
    }

    /**
     * Gets the text color for the edit preview message.
     *
     * @return The edit preview message text color.
     */
    public @ColorInt int getEditPreviewMessageTextColor() {
        return editPreviewMessageTextColor;
    }

    /**
     * @param editPreviewMessageTextColor The new color to set for the message text color
     */
    public void setEditPreviewMessageTextColor(@ColorInt int editPreviewMessageTextColor) {
        this.editPreviewMessageTextColor = editPreviewMessageTextColor;
        binding.editPreviewLayout.tvMessageLayoutSubtitle.setTextColor(editPreviewMessageTextColor);
    }

    /**
     * Gets the background color for the edit preview.
     *
     * @return The edit preview background color.
     */
    public @ColorInt int getEditPreviewBackgroundColor() {
        return editPreviewBackgroundColor;
    }

    /**
     * @param editPreviewBackgroundColor The new color to set for the edit preview background
     */
    public void setEditPreviewBackgroundColor(@ColorInt int editPreviewBackgroundColor) {
        this.editPreviewBackgroundColor = editPreviewBackgroundColor;
        binding.editPreviewLayout.editMessageLayout.setCardBackgroundColor(editPreviewBackgroundColor);
    }

    /**
     * Gets the corner radius for the edit preview.
     *
     * @return The edit preview corner radius.
     */
    public @Dimension int getEditPreviewCornerRadius() {
        return editPreviewCornerRadius;
    }

    /**
     * @param editPreviewCornerRadius The new radius to set for the edit preview card
     */
    public void setEditPreviewCornerRadius(@Dimension int editPreviewCornerRadius) {
        this.editPreviewCornerRadius = editPreviewCornerRadius;
        binding.editPreviewLayout.editMessageLayout.setRadius(editPreviewCornerRadius);
    }

    /**
     * Gets the stroke color for the edit preview.
     *
     * @return The edit preview stroke color.
     */
    public @ColorInt int getEditPreviewStrokeColor() {
        return editPreviewStrokeColor;
    }

    /**
     * @param editPreviewStrokeColor The new color to set for the edit preview stroke
     */
    public void setEditPreviewStrokeColor(@ColorInt int editPreviewStrokeColor) {
        this.editPreviewStrokeColor = editPreviewStrokeColor;
        binding.editPreviewLayout.editMessageLayout.setStrokeColor(editPreviewStrokeColor);
    }

    /**
     * Gets the stroke width for the edit preview.
     *
     * @return The edit preview stroke width.
     */
    public @Dimension int getEditPreviewStrokeWidth() {
        return editPreviewStrokeWidth;
    }

    /**
     * @param editPreviewStrokeWidth The new width to set for the edit preview stroke
     */
    public void setEditPreviewStrokeWidth(@Dimension int editPreviewStrokeWidth) {
        this.editPreviewStrokeWidth = editPreviewStrokeWidth;
        binding.editPreviewLayout.editMessageLayout.setStrokeWidth(editPreviewStrokeWidth);
    }

    /**
     * Gets the drawable used for the info icon.
     *
     * @return The info icon drawable.
     */
    public Drawable getInfoIcon() {
        return infoIcon;
    }

    /**
     * @param infoIcon The new drawable to set for the info icon
     */
    public void setInfoIcon(Drawable infoIcon) {
        this.infoIcon = infoIcon;
        binding.tagInfoIcon.setImageDrawable(infoIcon);
    }

    /**
     * Gets the text color for the info text.
     *
     * @return The info text color.
     */
    public @ColorInt int getInfoTextColor() {
        return infoTextColor;
    }

    /**
     * @param infoTextColor The new color to set info text color
     */
    public void setInfoTextColor(@ColorInt int infoTextColor) {
        this.infoTextColor = infoTextColor;
        binding.tagInfoMessage.setTextColor(infoTextColor);
    }

    /**
     * Gets the text appearance resource ID for the info text.
     *
     * @return The info text appearance resource ID.
     */
    public @StyleRes int getInfoTextAppearance() {
        return infoTextAppearance;
    }

    /**
     * @param infoTextAppearance The new style to set for the info text appearance
     */
    public void setInfoTextAppearance(@StyleRes int infoTextAppearance) {
        this.infoTextAppearance = infoTextAppearance;
        binding.tagInfoMessage.setTextAppearance(infoTextAppearance);
    }

    /**
     * Gets the background color for the info view.
     *
     * @return The info background color.
     */
    public @ColorInt int getInfoBackgroundColor() {
        return infoBackgroundColor;
    }

    /**
     * @param infoBackgroundColor The new color to set for the info background
     */
    public void setInfoBackgroundColor(@ColorInt int infoBackgroundColor) {
        this.infoBackgroundColor = infoBackgroundColor;
        binding.tagInfoCard.setCardBackgroundColor(infoBackgroundColor);
    }

    /**
     * Gets the corner radius for the info view.
     *
     * @return The info corner radius.
     */
    public @Dimension int getInfoCornerRadius() {
        return infoCornerRadius;
    }

    /**
     * @param infoCornerRadius The new radius to set for the info card
     */
    public void setInfoCornerRadius(@Dimension int infoCornerRadius) {
        this.infoCornerRadius = infoCornerRadius;
        binding.tagInfoCard.setRadius(infoCornerRadius);
    }

    /**
     * Gets the stroke color for the info view.
     *
     * @return The info stroke color.
     */
    public @ColorInt int getInfoStrokeColor() {
        return infoStrokeColor;
    }

    /**
     * @param infoStrokeColor The new color to set for the info stroke
     */
    public void setInfoStrokeColor(@ColorInt int infoStrokeColor) {
        this.infoStrokeColor = infoStrokeColor;
        binding.tagInfoCard.setStrokeColor(infoStrokeColor);
    }

    /**
     * Gets the stroke width for the info view.
     *
     * @return The info stroke width.
     */
    public @Dimension int getInfoStrokeWidth() {
        return infoStrokeWidth;
    }

    /**
     * @param infoStrokeWidth The new width to set for the info stroke
     */
    public void setInfoStrokeWidth(@Dimension int infoStrokeWidth) {
        this.infoStrokeWidth = infoStrokeWidth;
        binding.tagInfoCard.setStrokeWidth(infoStrokeWidth);
    }

    /**
     * Gets the tint color for the info icon.
     *
     * @return The info icon tint color.
     */
    public @ColorInt int getInfoIconTint() {
        return infoIconTint;
    }

    /**
     * @param tint The new color to set for the info icon
     */
    public void setInfoIconTint(@ColorInt int tint) {
        this.infoIconTint = tint;
        binding.tagInfoIcon.setImageTintList(ColorStateList.valueOf(tint));
    }

    /**
     * Gets the drawable used for the edit preview close icon.
     *
     * @return The edit preview close icon drawable.
     */
    public Drawable getEditPreviewCloseIcon() {
        return editPreviewCloseIcon;
    }

    /**
     * @param editPreviewCloseIcon The new drawable to set for the edit preview close icon
     */
    public void setEditPreviewCloseIcon(Drawable editPreviewCloseIcon) {
        this.editPreviewCloseIcon = editPreviewCloseIcon;
        binding.editPreviewLayout.ivMessageClose.setImageDrawable(editPreviewCloseIcon);
    }

    /**
     * Gets the tint color for the edit preview close icon.
     *
     * @return The edit preview close icon tint color.
     */
    public @ColorInt int getEditPreviewCloseIconTint() {
        return editPreviewCloseIconTint;
    }

    /**
     * @param editPreviewCloseIconTint The new color to set for the edit preview close icon
     */
    public void setEditPreviewCloseIconTint(@ColorInt int editPreviewCloseIconTint) {
        this.editPreviewCloseIconTint = editPreviewCloseIconTint;
        binding.editPreviewLayout.ivMessageClose.setImageTintList(ColorStateList.valueOf(editPreviewCloseIconTint));
    }

    /**
     * Gets the style resource ID for the message input.
     *
     * @return The message input style resource ID.
     */
    public @StyleRes int getMessageInputStyle() {
        return messageInputStyle;
    }

    /**
     * Sets the style for the message input field.
     *
     * <p>
     * This method allows customization of the appearance of the message input
     * component by applying a specified style resource.
     *
     * @param style The resource identifier of the style to be applied to the message
     *              input. This should be a valid style resource defined in the
     *              application's resources.
     */
    public void setMessageInputStyle(@StyleRes int style) {
        this.messageInputStyle = style;
        binding.messageInput.setStyle(style);
    }

    /**
     * Gets the style resource ID for the mentions.
     *
     * @return The mentions style resource ID.
     */
    public @StyleRes int getMentionsStyle() {
        return mentionsStyle;
    }

    /**
     * @param mentionsStyle The new style to set for the mentions
     */
    public void setMentionsStyle(@StyleRes int mentionsStyle) {
        this.mentionsStyle = mentionsStyle;
        cometchatMentionsFormatter.setMessageComposerMentionTextStyle(getContext(), mentionsStyle);
    }

    /**
     * Gets the background color for the message composer.
     *
     * @return The background color.
     */
    public @ColorInt int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Gets the corner radius for the message composer.
     *
     * @return The corner radius.
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * @param radius The new radius to set for the card
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        super.setRadius(radius);
    }

    /**
     * Gets the style resource ID for the message composer.
     *
     * @return The style resource ID.
     */
    public @StyleRes int getStyle() {
        return style;
    }

    /**
     * Sets the style of the text bubble from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatMessageComposer);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Extracts attributes from the given {@link TypedArray} and applies default
     * values.
     *
     * @param typedArray The TypedArray containing the view's attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {


            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageComposer_cometchatMessageComposerStrokeWidth, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerStrokeColor, 0));
            setSeparatorColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerSeparatorColor, 0));
            setVoiceRecordingIcon(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerVoiceRecordingIcon));
            setInactiveStickerIconTint(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerInactiveStickerIconTint, 0));
            setVoiceRecordingIconTint(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerVoiceRecordingIconTint, 0));
            setEditPreviewTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewTitleTextAppearance,
                                                                       0));
            setInfoTextColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoTextColor, 0));
            setInfoBackgroundColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoBackgroundColor, 0));
            setInfoCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoCornerRadius, 0));
            setInfoStrokeColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoStrokeColor, 0));
            setInfoStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoStrokeWidth, 0));

            setBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerBackgroundDrawable));
            setCardBackgroundColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerBackgroundColor, 0));
            setRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageComposer_cometchatMessageComposerCornerRadius, 0));
            setComposeBoxCardBackgroundColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerComposeBoxBackgroundColor,
                                                                 0));
            setComposeBoxStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageComposer_cometchatMessageComposerComposeBoxStrokeWidth,
                                                                      0));
            setMessageInputStyle(typedArray.getResourceId(R.styleable.CometChatMessageComposer_cometchatMessageInputStyle, 0));
            setComposeBoxStrokeColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerComposeBoxStrokeColor, 0));
            setComposeBoxCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageComposer_cometchatMessageComposerComposeBoxCornerRadius,
                                                                       0));
            setComposeBoxBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerComposeBoxBackgroundDrawable));
            setActiveSendButtonDrawable(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerActiveSendButtonDrawable));
            setInactiveSendButtonDrawable(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerInactiveSendButtonDrawable));
            setAttachmentIcon(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerAttachmentIcon));
            setAttachmentIconTint(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerAttachmentIconTint, 0));
            setAIIcon(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerAIIcon));
            setAIIconTint(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerAIIconTint, 0));
            setInactiveStickerIcon(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerInactiveStickerIcon));
            setActiveStickerIcon(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerActiveStickerIcon));
            setActiveStickerIconTint(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerActiveStickerIconTint, 0));
            setEditPreviewMessageTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewMessageTextAppearance,
                                                                         0));
            setEditPreviewTitleTextColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewTitleTextColor,
                                                             0));
            setEditPreviewMessageTextColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewMessageTextColor,
                                                               0));
            setEditPreviewBackgroundColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewBackgroundColor,
                                                              0));
            setEditPreviewCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewCornerRadius,
                                                                        0));
            setEditPreviewStrokeColor(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewStrokeColor, 0));
            setEditPreviewStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewStrokeWidth,
                                                                       0));
            setEditPreviewCloseIcon(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewCloseIcon));
            setEditPreviewCloseIconTint(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerEditPreviewCloseIconTint,
                                                            0));
            setInfoIcon(typedArray.getDrawable(R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoIcon));
            setInfoIconTint(typedArray.getColor(R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoIconTint, 0));
            setInfoTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessageComposer_cometchatMessageComposerInfoTextAppearance, 0));
            setMentionsStyle(typedArray.getResourceId(R.styleable.CometChatMessageComposer_cometchatMessageComposerMentionsStyle, 0));
            setSuggestionListStyle(typedArray.getResourceId(R.styleable.CometChatMessageComposer_cometchatMessageComposerSuggestionListStyle, 0));
            setAttachmentOptionSheetStyle(typedArray.getResourceId(R.styleable.CometChatMessageComposer_cometchatMessageComposerAttachmentOptionSheetStyle,
                                                                   0));
            setAIOptionSheetStyle(typedArray.getResourceId(R.styleable.CometChatMessageComposer_cometchatMessageComposerAIOptionSheetStyle, 0));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * @param color The new color to set for the stroke color
     */
    @Override
    public void setStrokeColor(@ColorInt int color) {
        this.strokeColor = color;
        super.setStrokeColor(color);
    }

    /**
     * @param backgroundColor The new color to set for the compose box card background
     */
    public void setComposeBoxCardBackgroundColor(@ColorInt int backgroundColor) {
        this.composeBoxBackgroundColor = backgroundColor;
        binding.composeBoxCard.setCardBackgroundColor(backgroundColor);
    }

    /**
     * @param activeStickerIcon The new color to set for the active sticker icon
     */
    public void setActiveStickerIcon(Drawable activeStickerIcon) {
        additionParameter.setActiveStickerIcon(activeStickerIcon);
    }

    /**
     * @param activeStickerIconTint The new activeStickerIconTint to set for the attachment icon
     */
    public void setActiveStickerIconTint(@ColorInt int activeStickerIconTint) {
        additionParameter.setActiveAuxiliaryIconTint(activeStickerIconTint);
    }

    @Nullable
    @Override
    public ColorStateList getStrokeColorStateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    /**
     * Retrieves the visibility status of the image attachment option.
     *
     * @return An integer representing the visibility of the image attachment option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getImageAttachmentOptionVisibility() {
        return imageAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the image attachment option.
     *
     * @param visibility An integer representing the visibility status of the image attachment option.
     *                   Accepts values such as {@code View.VISIBLE}, {@code View.INVISIBLE}, or {@code View.GONE}.
     */
    public void setImageAttachmentOptionVisibility(int visibility) {
        this.imageAttachmentOptionVisibility = visibility;
        additionParameter.setImageAttachmentOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the camera attachment option.
     *
     * @return An integer representing the visibility of the camera attachment option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getCameraAttachmentOptionVisibility() {
        return cameraAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the camera attachment option.
     *
     * @param visibility An integer representing the visibility status of the camera attachment option.
     */
    public void setCameraAttachmentOptionVisibility(int visibility) {
        this.cameraAttachmentOptionVisibility = visibility;
        additionParameter.setCameraAttachmentOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the video attachment option.
     *
     * @return An integer representing the visibility of the video attachment option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getVideoAttachmentOptionVisibility() {
        return videoAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the video attachment option.
     *
     * @param visibility An integer representing the visibility status of the video attachment option.
     */
    public void setVideoAttachmentOptionVisibility(int visibility) {
        this.videoAttachmentOptionVisibility = visibility;
        additionParameter.setVideoAttachmentOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the audio attachment option.
     *
     * @return An integer representing the visibility of the audio attachment option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getAudioAttachmentOptionVisibility() {
        return audioAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the audio attachment option.
     *
     * @param visibility An integer representing the visibility status of the audio attachment option.
     */
    public void setAudioAttachmentOptionVisibility(int visibility) {
        this.audioAttachmentOptionVisibility = visibility;
        additionParameter.setAudioAttachmentOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the file attachment option.
     *
     * @return An integer representing the visibility of the file attachment option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getFileAttachmentOptionVisibility() {
        return fileAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the file attachment option.
     *
     * @param visibility An integer representing the visibility status of the file attachment option.
     */
    public void setFileAttachmentOptionVisibility(int visibility) {
        this.fileAttachmentOptionVisibility = visibility;
        additionParameter.setFileAttachmentOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the poll attachment option.
     *
     * @return An integer representing the visibility of the poll attachment option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getPollAttachmentOptionVisibility() {
        return pollAttachmentOptionVisibility;
    }

    /**
     * Sets the visibility of the poll attachment option.
     *
     * @param visibility An integer representing the visibility status of the poll attachment option.
     */
    public void setPollAttachmentOptionVisibility(int visibility) {
        this.pollAttachmentOptionVisibility = visibility;
        additionParameter.setPollAttachmentOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the collaborative document option.
     *
     * @return An integer representing the visibility of the collaborative document option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getCollaborativeDocumentOptionVisibility() {
        return collaborativeDocumentOptionVisibility;
    }

    /**
     * Sets the visibility of the collaborative document option.
     *
     * @param visibility An integer representing the visibility status of the collaborative document option.
     */
    public void setCollaborativeDocumentOptionVisibility(int visibility) {
        this.collaborativeDocumentOptionVisibility = visibility;
        additionParameter.setCollaborativeDocumentOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the collaborative whiteboard option.
     *
     * @return An integer representing the visibility of the collaborative whiteboard option.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getCollaborativeWhiteboardOptionVisibility() {
        return collaborativeWhiteboardOptionVisibility;
    }

    /**
     * Sets the visibility of the collaborative whiteboard option.
     *
     * @param visibility An integer representing the visibility status of the collaborative whiteboard option.
     */
    public void setCollaborativeWhiteboardOptionVisibility(int visibility) {
        this.collaborativeWhiteboardOptionVisibility = visibility;
        additionParameter.setCollaborativeWhiteboardOptionVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the stickers button.
     *
     * @return An integer representing the visibility of the stickers button.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getStickersButtonVisibility() {
        return stickersButtonVisibility;
    }

    /**
     * Sets the visibility of the stickers button.
     *
     * @param visibility An integer representing the visibility status of the stickers button.
     */
    public void setStickersButtonVisibility(int visibility) {
        this.stickersButtonVisibility = visibility;
        additionParameter.setStickersButtonVisibility(visibility);
        setAuxiliaryButtonViewInternally();
    }

    /**
     * Retrieves the visibility status of the send button.
     *
     * @return An integer representing the visibility of the send button.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getSendButtonVisibility() {
        return sendButtonVisibility;
    }

    /**
     * Sets the visibility of the send button.
     *
     * @param visibility An integer representing the visibility status of the send button.
     */
    public void setSendButtonVisibility(int visibility) {
        this.sendButtonVisibility = visibility;
        sendButtonLayoutBinding.ivSendBtn.setVisibility(visibility);
    }

    /**
     * Retrieves the visibility status of the auxiliary button.
     *
     * @return An integer representing the visibility of the auxiliary button.
     * Possible values include {@code View.VISIBLE}, {@code View.INVISIBLE}, and {@code View.GONE}.
     */
    public int getAuxiliaryButtonVisibility() {
        return auxiliaryButtonVisibility;
    }

    /**
     * Sets the visibility of the auxiliary button.
     *
     * @param visibility An integer representing the visibility status of the auxiliary button.
     */
    public void setAuxiliaryButtonVisibility(int visibility) {
        this.auxiliaryButtonVisibility = visibility;
        binding.messageInput.setAuxiliaryButtonVisibility(visibility);
    }

    /**
     * Interface representing a callback to be invoked when the send button is
     * clicked.
     *
     * <p>
     * Implementing this interface allows you to define custom behavior for handling
     * the action of sending a message. The `onClick` method will be called with the
     * context and the message that needs to be sent.
     */
    public interface SendButtonClick {
        /**
         * Called when the send button is clicked.
         *
         * @param context The context in which the send button was clicked.
         * @param message The message to be sent, represented as a {@link BaseMessage}.
         */
        void onClick(Context context, BaseMessage message);
    }






    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        composerViewModel.addListeners();
    }


    @Override
    public @Dimension int getStrokeWidth() {
        return strokeWidth;
    }


}
