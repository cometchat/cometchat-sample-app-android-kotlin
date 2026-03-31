package com.cometchat.chatuikit.conversations;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.cometchat.chat.core.ConversationsRequest;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.Conversation;
import com.cometchat.chat.models.TypingIndicator;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatConversationsListViewBinding;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatMentionsFormatter;
import com.cometchat.chatuikit.shared.formatters.CometChatRichTextFormatter;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;
import com.cometchat.chatuikit.shared.interfaces.Function2;
import com.cometchat.chatuikit.shared.interfaces.OnBackPress;
import com.cometchat.chatuikit.shared.interfaces.OnEmpty;
import com.cometchat.chatuikit.shared.interfaces.OnError;
import com.cometchat.chatuikit.shared.interfaces.OnItemClick;
import com.cometchat.chatuikit.shared.interfaces.OnItemLongClick;
import com.cometchat.chatuikit.shared.interfaces.OnLoad;
import com.cometchat.chatuikit.shared.interfaces.OnSelection;
import com.cometchat.chatuikit.shared.models.AdditionParameter;
import com.cometchat.chatuikit.shared.resources.soundmanager.CometChatSoundManager;
import com.cometchat.chatuikit.shared.resources.soundmanager.Sound;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.resources.utils.custom_dialog.CometChatConfirmDialog;
import com.cometchat.chatuikit.shared.viewholders.ConversationsViewHolderListener;
import com.cometchat.chatuikit.shared.views.popupmenu.CometChatPopupMenu;
import com.cometchat.chatuikit.shimmer.CometChatShimmerAdapter;
import com.cometchat.chatuikit.shimmer.CometChatShimmerUtils;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CometChatConversations extends MaterialCardView {
    private boolean isDetachedFromWindow;
    private static final String TAG = CometChatConversations.class.getSimpleName();
    private final HashMap<Conversation, Boolean> hashMap = new HashMap<>();
    private final List<CometChatTextFormatter> textFormatters = new ArrayList<>();
    private boolean isFurtherSelectionEnabled = true;
    private CometchatConversationsListViewBinding binding;
    private boolean isConversationListEmpty = true;
    private boolean disableSoundForMessages;
    private ConversationsAdapter conversationsAdapter;
    private SelectedConversationsAdapter selectedConversationsAdapter;
    private LifecycleOwner lifecycleOwner;

    private Drawable searchInputStartIcon;
    private Drawable searchInputEndIcon;
    private @StyleRes int searchInputTextAppearance;
    private @ColorInt int searchInputTextColor;
    private @StyleRes int searchInputPlaceHolderTextAppearance;
    private @ColorInt int searchInputPlaceHolderTextColor;
    private @ColorInt int searchInputStartIconTint;
    private @ColorInt int searchInputEndIconTint;
    private @ColorInt int searchInputBackgroundColor;
    private String searchPlaceholderText;
    private int searchInputStrokeWidth;
    private int searchInputStrokeColor;
    private int searchInputCornerRadius;
    private int searchBoxVisibility = VISIBLE;
    private String mentionAllLabelId;
    private String mentionAllLabel;

    /**
     * Observer for updating a specific conversation in the list. Notifies the
     * adapter to refresh the item at the given position.
     */
    Observer<Integer> updateConversation = new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            conversationsAdapter.notifyItemChanged(integer);
        }
    };
    /**
     * Observer for monitoring typing indicators in conversations. Updates the
     * adapter with the current typing indicators.
     */
    Observer<HashMap<Conversation, TypingIndicator>> typing = new Observer<HashMap<Conversation, TypingIndicator>>() {
        @Override
        public void onChanged(HashMap<Conversation, TypingIndicator> typingIndicatorHashMap) {
            conversationsAdapter.typing(typingIndicatorHashMap);
        }
    };
    /**
     * Observer for removing a conversation from the list. Notifies the adapter to
     * remove the item at the specified position.
     */
    Observer<Integer> remove = new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            conversationsAdapter.notifyItemRemoved(integer);
        }
    };
    private DateTimeFormatterCallback dateTimeFormatter;
    private int toolbarVisibility = View.VISIBLE;
    private int deleteConversationOptionVisibility = View.VISIBLE;
    private int backIconVisibility = View.GONE;
    private int userStatusVisibility = View.VISIBLE;
    private int groupTypeVisibility = View.VISIBLE;
    private int receiptsVisibility = View.VISIBLE;
    private int errorStateVisibility = View.VISIBLE;
    private int loadingStateVisibility = View.VISIBLE;
    private int emptyStateVisibility = View.VISIBLE;
    private int selectedConversationsListVisibility = VISIBLE;
    private AdditionParameter additionParameter;
    private OnBackPress onBackPress;
    private OnSearchClick onSearchClick;
    private Function2<Context, Conversation, List<CometChatPopupMenu.MenuItem>> addOptions;
    private Function2<Context, Conversation, List<CometChatPopupMenu.MenuItem>> options;
    private View overflowMenu = null;
    private OnError onError;
    /**
     * Observer for handling CometChat exceptions. Calls the onError callback if an
     * exception occurs.
     */
    Observer<CometChatException> cometchatExceptionObserver = exception -> {
        if (onError != null) onError.onError(exception);
    };
    private OnLoad<Conversation> onLoad;
    /**
     * Observer for monitoring changes in the conversation list. Updates the
     * conversations adapter with the new list of conversations.
     */
    Observer<List<Conversation>> listObserver = new Observer<List<Conversation>>() {
        @Override
        public void onChanged(List<Conversation> conversations) {
            isConversationListEmpty = conversations.isEmpty();
            conversationsAdapter.setList(conversations);
            if (onLoad != null) onLoad.onLoad(conversations);
        }
    };
    private OnEmpty onEmpty;
    private CometChatSoundManager soundManager;
    private ConversationsViewModel conversationsViewModel;
    private RecyclerView.LayoutManager layoutManager;
    /**
     * Observer for inserting a new conversation at the top of the list. Notifies
     * the adapter and scrolls to the top of the list.
     */
    Observer<Integer> insertAtTop = new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            conversationsAdapter.notifyItemInserted(integer);
            scrollToTop();
        }
    };
    /**
     * Observer for moving a conversation to the top of the list. Notifies the
     * adapter of data changes and scrolls to the top.
     */
    Observer<Integer> moveToTop = new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            conversationsAdapter.notifyDataSetChanged();
            scrollToTop();
        }
    };
    private CometChatMentionsFormatter cometchatMentionsFormatter;
    @Nullable
    private View customEmptyView = null;
    private View customErrorView = null;
    private View customLoadingView = null;
    /**
     * Observer for handling conversation states. Depending on the state, it
     * triggers appropriate methods to handle each state.
     */
    Observer<UIKitConstants.States> stateChangeObserver = states -> {
        hideAllStates();

        switch (states) {
            case LOADING:
                handleLoadingState();
                break;
            case NON_EMPTY:
                setRecyclerViewVisibility(View.VISIBLE);
                break;
            case ERROR:
                handleErrorState();
                break;
            case EMPTY:
                handleEmptyState();
                break;
            default:
                break;
        }
    };
    private OnSelection<Conversation> onSelection;
    private UIKitConstants.SelectionMode selectionMode = UIKitConstants.SelectionMode.NONE;
    private OnItemClick<Conversation> onItemClick;
    private OnItemLongClick<Conversation> onItemLongClick;
    private @RawRes int customSoundForMessage = 0;
    private @ColorInt int backIconTint;
    private @ColorInt int strokeColor;
    private @ColorInt int backgroundColor;
    private @ColorInt int titleTextColor;
    private @ColorInt int emptyStateTitleTextColor;
    private @ColorInt int emptyStateSubtitleTextColor;
    private @ColorInt int errorStateTitleTextColor;
    private @ColorInt int errorStateSubtitleTextColor;
    private @ColorInt int itemTitleTextColor;
    private @ColorInt int itemSubtitleTextColor;
    private @ColorInt int itemMessageTypeIconTint;
    private @Dimension int strokeWidth;
    private @Dimension int cornerRadius;
    private @StyleRes int titleTextAppearance;
    private @StyleRes int emptyStateTextTitleAppearance;
    private @StyleRes int emptyStateTextSubtitleAppearance;
    private @StyleRes int errorStateTextTitleAppearance;
    private @StyleRes int errorStateTextSubtitleAppearance;
    private @StyleRes int itemTitleTextAppearance;
    private @StyleRes int itemSubtitleTextAppearance;
    private @StyleRes int avatarStyle;
    private @StyleRes int statusIndicatorStyle;
    private @StyleRes int dateStyle;
    private @StyleRes int badgeStyle;
    private @StyleRes int receiptStyle;
    private @StyleRes int typingIndicatorStyle;
    private @StyleRes int mentionsStyle;
    private @Dimension int separatorHeight;
    private @ColorInt int separatorColor;
    private @StyleRes int optionListStyle;
    private Drawable backIcon;
    private Drawable deleteOptionIcon;
    private @ColorInt int deleteOptionIconTint;
    private @ColorInt int deleteOptionTextColor;
    private @StyleRes int deleteOptionTextAppearance;
    private Drawable selectedConversationItemRemoveIcon;
    private @ColorInt int selectedConversationItemTextColor;
    private @StyleRes int selectedConversationItemTextAppearance;
    private @StyleRes int selectedConversationAvatarStyle;
    private @ColorInt int selectedConversationItemRemoveIconTint;
    private @LayoutRes int emptyView;
    private @LayoutRes int errorView;
    private @LayoutRes int loadingView;
    private CometChatConfirmDialog deleteAlertDialog;
    /**
     * Observer for monitoring the conversation deletion state. Displays a progress
     * dialog based on the current deletion state.
     */
    Observer<UIKitConstants.DeleteState> conversationDeleteObserver = new Observer<UIKitConstants.DeleteState>() {
        @Override
        public void onChanged(UIKitConstants.DeleteState progressState) {
            if (UIKitConstants.DeleteState.SUCCESS_DELETE.equals(progressState)) {
                if (deleteAlertDialog != null) deleteAlertDialog.dismiss();
            } else if (UIKitConstants.DeleteState.FAILURE_DELETE.equals(progressState)) {
                if (deleteAlertDialog != null) deleteAlertDialog.dismiss();
                Toast.makeText(getContext(), getContext().getString(R.string.cometchat_conversation_delete_error), Toast.LENGTH_SHORT).show();
            } else if (UIKitConstants.DeleteState.INITIATED_DELETE.equals(progressState)) {
                deleteAlertDialog.hidePositiveButtonProgressBar(false);
            }
        }
    };

    private Drawable discardSelectionIcon;
    private @ColorInt int discardSelectionIconTint;
    private Drawable submitSelectionIcon;
    private @ColorInt int submitSelectionIconTint;
    private int checkBoxStrokeWidth;
    private int checkBoxCornerRadius;
    private @ColorInt int checkBoxStrokeColor;
    private @ColorInt int checkBoxBackgroundColor;
    private @ColorInt int checkBoxCheckedBackgroundColor;
    private Drawable checkBoxSelectIcon;
    private @ColorInt int checkBoxSelectIconTint;
    private @ColorInt int itemSelectedBackgroundColor;
    private @ColorInt int itemBackgroundColor;
    private CometChatPopupMenu cometchatPopUpMenu;

    /**
     * Constructs a new CometChatConversations object with the given context.
     *
     * @param context The context of the view.
     */
    public CometChatConversations(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatConversations object with the given context and
     * attribute set.
     *
     * @param context The context of the view.
     * @param attrs   The attribute set for the view.
     */
    public CometChatConversations(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatConversationsStyle);
    }

    /**
     * Constructs a new CometChatConversations object with the given context,
     * attribute set, and default style attribute.
     *
     * @param context      The context of the view.
     * @param attrs        The attribute set for the view.
     * @param defStyleAttr The default style attribute.
     */
    public CometChatConversations(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates and initializes the view by setting up the layout, retrieving the
     * attributes, and applying styles.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        // Inflate the layout for this view
        binding = CometchatConversationsListViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
        // Reset the card view to default values
        Utils.initMaterialCard(this);
        // Set default values
        init();
        // Apply style attributes
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Sets the default values for the CometChatAvatar view.
     */
    private void init() {
        soundManager = new CometChatSoundManager(getContext());
        additionParameter = new AdditionParameter();
        initRecyclerView();
        getDefaultMentionsFormatter();
        initViewModels();
        clickEvents();
        configureSelectedConversationsView();
    }

    /**
     * Applies the style attributes from XML, allowing direct attribute overrides.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatConversations, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatConversations_cometchatConversationsStyle, 0);
        directAttributes = styleResId != 0 ? getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.CometChatConversations, defStyleAttr, styleResId) : null;
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Initializes the RecyclerView with a LinearLayoutManager and sets up the
     * adapter. Disables change animations for the RecyclerView's ItemAnimator. Adds
     * a scroll listener to fetch more conversations when scrolled to the bottom.
     */
    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        conversationsAdapter = new ConversationsAdapter(getContext());
        RecyclerView.ItemAnimator animator = binding.recyclerviewConversationsList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false); // Disables change animations
        }
        binding.recyclerviewConversationsList.setLayoutManager(layoutManager);
        binding.recyclerviewConversationsList.setAdapter(conversationsAdapter);
        binding.recyclerviewConversationsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!binding.recyclerviewConversationsList.canScrollVertically(1)) {
                    conversationsViewModel.fetchConversation();
                }
            }
        });
    }

    /**
     * Retrieves the default mentions formatter from the available text formatters
     * and adds it to the list. This method searches through the available text
     * formatters and assigns the first instance of CometChatMentionsFormatter found
     * to cometchatMentionsFormatter.
     */
    private void getDefaultMentionsFormatter() {
        for (CometChatTextFormatter textFormatter : CometChatUIKit.getDataSource().getTextFormatters(getContext(), additionParameter)) {
            if (textFormatter instanceof CometChatMentionsFormatter) {
                cometchatMentionsFormatter = (CometChatMentionsFormatter) textFormatter;
                cometchatMentionsFormatter.setMentionAllLabel(mentionAllLabelId, mentionAllLabel);
            } else if (textFormatter instanceof CometChatRichTextFormatter) {
                this.textFormatters.add(textFormatter);
            }
        }
        this.textFormatters.add(cometchatMentionsFormatter);
        processFormatters();
    }

    /**
     * Initializes the ViewModels for managing conversation data and observing
     * changes. Sets up observers to handle various state changes and data updates
     * related to conversations.
     */
    private void initViewModels() {
        conversationsViewModel = new ViewModelProvider.NewInstanceFactory().create(ConversationsViewModel.class);
        lifecycleOwner = Utils.getLifecycleOwner(getContext());
        if (lifecycleOwner == null) return;
        attachObservers();
    }

    public void attachObservers() {
        conversationsViewModel.getMutableConversationList().observe(lifecycleOwner, listObserver);
        conversationsViewModel.getStates().observe(lifecycleOwner, stateChangeObserver);
        conversationsViewModel.insertAtTop().observe(lifecycleOwner, insertAtTop);
        conversationsViewModel.moveToTop().observe(lifecycleOwner, moveToTop);
        conversationsViewModel.getTyping().observe(lifecycleOwner, typing);
        conversationsViewModel.updateConversation().observe(lifecycleOwner, updateConversation);
        conversationsViewModel.playSound().observe(lifecycleOwner, this::playSound);
        conversationsViewModel.remove().observe(lifecycleOwner, remove);
        conversationsViewModel.progressState().observe(lifecycleOwner, conversationDeleteObserver);
        conversationsViewModel.getCometChatException().observe(lifecycleOwner, cometchatExceptionObserver);
    }

    /**
     * Sets up click events for the conversations list. It adds an item touch
     * listener to the RecyclerView to handle single and multiple selection modes
     * for conversations.
     */
    private void clickEvents() {
        cometchatPopUpMenu = new CometChatPopupMenu(getContext(), 0);

        conversationsAdapter.setOnItemClick((view, position, conversation) -> {
            if (onItemClick != null) {
                onItemClick.click(view, position, conversation);
            } else {
                if (!UIKitConstants.SelectionMode.NONE.equals(selectionMode)) {
                    selectConversation(conversation, selectionMode);
                }
            }
        });

        conversationsAdapter.setOnLongClick((view, position, conversation) -> {
            if (onItemLongClick != null) {
                onItemLongClick.longClick(view, position, conversation);
            } else {
                preparePopupMenu(view, conversation);
            }
        });

        binding.ivDiscardSelection.setOnClickListener(v -> clearSelection());

        binding.ivSubmitSelection.setOnClickListener(v -> {
            if (onSelection != null) {
                onSelection.onSelection(getSelectedConversations());
            }
        });

        binding.btnRetry.setOnClickListener(v -> conversationsViewModel.fetchConversation());

        binding.ivBack.setOnClickListener(v -> {
            if (onBackPress != null) {
                onBackPress.onBack();
            }
        });

        binding.searchBox.setOnSearchClick(() -> {
            if (onSearchClick != null) {
                onSearchClick.onSearchClick();
            }
        });

        binding.searchBox.getBinding().etSearch.setFocusable(false);
    }

    /**
     * Clears the current selection of users. This method resets the selection count
     * to zero, hides the selection count visibility, and informs the users adapter
     * to deselect any selected users.
     */
    public void clearSelection() {
        hashMap.clear();
        binding.rvSelectedConversation.setAdapter(null);
        binding.rvSelectedConversation.setVisibility(GONE);
        setSelectionCount(0);
        setDiscardSelectionVisibility(GONE);
        setTitleVisibility(VISIBLE);
        setSelectionCountVisibility(GONE);
        setSubmitSelectionIconVisibility(GONE);
        conversationsAdapter.selectConversation(hashMap);
    }

    /**
     * Plays a sound based on the provided boolean value.
     *
     * @param play true to play the sound, false otherwise.
     */
    private void playSound(Boolean play) {
        if (play) playSound();
    }

    public void selectConversation(Conversation conversation, @Nullable UIKitConstants.SelectionMode mode) {
        if (mode != null && conversation != null) {
            this.selectionMode = mode;
            if (UIKitConstants.SelectionMode.SINGLE.equals(selectionMode)) {
                hashMap.clear();
                hashMap.put(conversation, true);
                setSubmitSelectionIconVisibility(VISIBLE);
                conversationsAdapter.selectConversation(hashMap);
            } else if (UIKitConstants.SelectionMode.MULTIPLE.equals(selectionMode)) {
                if (hashMap.containsKey(conversation)) {
                    hashMap.remove(conversation);
                } else {
                    if (isFurtherSelectionEnabled) {
                        hashMap.put(conversation, true);
                    }
                }
                if (hashMap.isEmpty()) {
                    setDiscardSelectionVisibility(GONE);
                    setSubmitSelectionIconVisibility(GONE);
                    setSelectionCountVisibility(GONE);
                    setTitleVisibility(VISIBLE);
                } else {
                    setSelectionCount(hashMap.size());
                    setDiscardSelectionVisibility(VISIBLE);
                    setSubmitSelectionIconVisibility(VISIBLE);
                    setSelectionCountVisibility(VISIBLE);
                }
                conversationsAdapter.selectConversation(hashMap);
                updateSelectionUI();
            }
        }
    }

    private void updateSelectionUI() {
        boolean hasSelection = !getSelectedConversations().isEmpty();
        if (selectedConversationsListVisibility == VISIBLE && hasSelection) {
            if (binding.rvSelectedConversation.getAdapter() == null) {
                binding.rvSelectedConversation.setAdapter(selectedConversationsAdapter);
            }
            binding.rvSelectedConversation.setVisibility(VISIBLE);
            refreshSelectedConversations();
        } else {
            binding.rvSelectedConversation.setVisibility(GONE);
            if (binding.rvSelectedConversation.getAdapter() != null) {
                binding.rvSelectedConversation.setAdapter(null);
            }
        }

        setSelectionCount(getSelectedConversations().size());

        if (hasSelection) {
            setDiscardSelectionVisibility(VISIBLE);
            setTitleVisibility(GONE);
        } else {
            setDiscardSelectionVisibility(GONE);
            setSubmitSelectionIconVisibility(GONE);
            setTitleVisibility(VISIBLE);
            binding.tvSelectionCount.setVisibility(GONE);
        }
    }

    /**
     * Prepares and displays a popup menu for the given conversation.
     *
     * <p>
     * This method retrieves the menu items based on the provided conversation.
     * If the {@link #options} function is set, it generates a list of menu
     * items and sets up a click listener to handle user interactions with the menu
     * options. If a menu item has a defined click action, that action is executed;
     * otherwise, the method handles default click events.
     *
     * @param conversation The {@link Conversation} for whom the popup menu is being prepared.
     */
    private void preparePopupMenu(View view, Conversation conversation) {
        List<CometChatPopupMenu.MenuItem> optionsArrayList = new ArrayList<>();
        if (options != null) {
            optionsArrayList.addAll(options.apply(getContext(), conversation));
        } else {
            if (deleteConversationOptionVisibility == VISIBLE)
                optionsArrayList.add(new CometChatPopupMenu.MenuItem(UIKitConstants.ConversationOption.DELETE,
                                                                     getContext().getString(R.string.cometchat_delete),
                                                                     deleteOptionIcon,
                                                                     null,
                                                                     deleteOptionIconTint,
                                                                     0,
                                                                     deleteOptionTextColor,
                                                                     deleteOptionTextAppearance,
                                                                     null));

            if (addOptions != null) optionsArrayList.addAll(addOptions.apply(getContext(), conversation));
        }
        cometchatPopUpMenu.setMenuItems(optionsArrayList);
        cometchatPopUpMenu.setOnMenuItemClickListener((id, name) -> {
            for (CometChatPopupMenu.MenuItem item : optionsArrayList) {
                if (id.equalsIgnoreCase(item.getId())) {
                    if (item.getOnClick() != null) {
                        item.getOnClick().onClick();
                    } else {
                        handleDefaultClickEvents(item, conversation);
                    }
                    break;
                }
            }
            cometchatPopUpMenu.dismiss();
        });

        cometchatPopUpMenu.show(view);
    }

    /**
     * Plays a sound for incoming messages if sound is not disabled.
     */
    private void playSound() {
        if (!disableSoundForMessages) soundManager.play(Sound.incomingMessageFromOther, customSoundForMessage);
    }

    /**
     * Sets the selection count text.
     *
     * @param count the number of selected items
     */
    public void setSelectionCount(int count) {
        binding.tvSelectionCount.setText(String.valueOf(count));
    }

    /**
     * Handles default click events for the given menu item associated with a group
     * member.
     *
     * <p>
     * This method performs specific actions based on the ID of the menu item. If
     * the item ID corresponds to changing the scope of the group member, it
     * displays a bottom sheet dialog to allow the user to delete conversation.
     * If the item ID indicates a ban or kick action, it shows a confirmation dialog
     * to confirm the action.
     *
     * @param item         The {@link CometChatPopupMenu.MenuItem} that was clicked.
     * @param conversation The {@link Conversation} associated with the clicked menu item.
     */
    private void handleDefaultClickEvents(CometChatPopupMenu.MenuItem item, Conversation conversation) {
        if (item.getId().equalsIgnoreCase(UIKitConstants.ConversationOption.DELETE)) {
            deleteAlertDialog = new CometChatConfirmDialog(getContext(), R.style.CometChatConfirmDialogStyle);
            showDeleteConversationAlertDialog(conversation);
        }
    }

    private void showDeleteConversationAlertDialog(Conversation conversation) {
        deleteAlertDialog.setConfirmDialogIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.cometchat_ic_delete, null));
        deleteAlertDialog.setTitleText(getContext().getString(R.string.cometchat_conversation_delete_message_title));
        deleteAlertDialog.setSubtitleText(getContext().getString(R.string.cometchat_conversation_delete_message_subtitle));
        deleteAlertDialog.setPositiveButtonText(getContext().getString(R.string.cometchat_delete));
        deleteAlertDialog.setNegativeButtonText(getContext().getString(R.string.cometchat_cancel));
        deleteAlertDialog.setOnPositiveButtonClick(v -> {
            conversationsViewModel.deleteConversation(conversation);
        });
        deleteAlertDialog.setOnNegativeButtonClick(v -> deleteAlertDialog.dismiss());
        deleteAlertDialog.setConfirmDialogElevation(0);
        deleteAlertDialog.setCancelable(false);
        deleteAlertDialog.show();
    }

    /**
     * Sets the visibility of the search input end icon.
     *
     * @param visibility The visibility state (e.g., View.VISIBLE, View.GONE).
     */
    public void setSearchInputEndIconVisibility(int visibility) {
        binding.searchBox.setSearchInputEndIconVisibility(visibility);
    }

    /**
     * Sets the text of the search input field.
     *
     * @param text The text to be set in the search input field.
     */
    public void setSearchInputText(String text) {
        binding.searchBox.setSearchInputText(text);
    }

    /**
     * Retrieves the text from the search input field.
     *
     * @return The current text in the search input field.
     */
    public @StyleRes int getSearchInputTextAppearance() {
        return searchInputTextAppearance;
    }

    /**
     * Sets the text appearance of the search input field.
     * @param searchInputTextAppearance The style resource for the text appearance.
     */
    public void setSearchInputTextAppearance(@StyleRes int searchInputTextAppearance) {
        this.searchInputTextAppearance = searchInputTextAppearance;
        binding.searchBox.setSearchInputTextAppearance(searchInputTextAppearance);
    }

    /**
     * Retrieves the text color of the search input field.
     *
     * @return The current text color of the search input field.
     */
    public @ColorInt int getSearchInputTextColor() {
        return searchInputTextColor;
    }

    /**
     * Sets the text color of the search input field.
     * @param searchInputTextColor The color integer for the text color.
     */
    public void setSearchInputTextColor(@ColorInt int searchInputTextColor) {
        this.searchInputTextColor = searchInputTextColor;
        binding.searchBox.setSearchInputTextColor(searchInputTextColor);
    }

    /**
     * Retrieves the placeholder text appearance of the search input field.
     *
     * @return The current placeholder text appearance of the search input field.
     */
    public @StyleRes int getSearchInputPlaceHolderTextAppearance() {
        return searchInputPlaceHolderTextAppearance;
    }

    /**
     * Sets the placeholder text appearance of the search input field.
     * @param searchInputPlaceHolderTextAppearance The style resource for the placeholder text appearance.
     */
    public void setSearchInputPlaceHolderTextAppearance(@StyleRes int searchInputPlaceHolderTextAppearance) {
        this.searchInputPlaceHolderTextAppearance = searchInputPlaceHolderTextAppearance;
        binding.searchBox.setSearchInputPlaceHolderTextAppearance(searchInputPlaceHolderTextAppearance);
    }

    /**
     * Retrieves the placeholder text color of the search input field.
     *
     * @return The current placeholder text color of the search input field.
     */
    public @ColorInt int getSearchInputPlaceHolderTextColor() {
        return searchInputPlaceHolderTextColor;
    }

    /**
     * Sets the placeholder text color of the search input field.
     * @param searchInputPlaceHolderTextColor The color integer for the placeholder text color.
     */
    public void setSearchInputPlaceHolderTextColor(@ColorInt int searchInputPlaceHolderTextColor) {
        this.searchInputPlaceHolderTextColor = searchInputPlaceHolderTextColor;
        binding.searchBox.setSearchInputPlaceHolderTextColor(searchInputPlaceHolderTextColor);
    }

    /**
     * Retrieves the start icon drawable of the search input field.
     *
     * @return The current start icon drawable of the search input field.
     */
    public Drawable getSearchInputStartIcon() {
        return searchInputStartIcon;
    }

    /**
     * Sets the start icon drawable of the search input field.
     * @param searchInputStartIcon The drawable for the start icon.
     */
    public void setSearchInputStartIcon(Drawable searchInputStartIcon) {
        this.searchInputStartIcon = searchInputStartIcon;
        binding.searchBox.setSearchInputStartIcon(searchInputStartIcon);
    }

    /**
     * Retrieves the start icon tint color of the search input field.
     *
     * @return The current start icon tint color of the search input field.
     */
    public @ColorInt int getSearchInputStartIconTint() {
        return searchInputStartIconTint;
    }

    /**
     * Sets the start icon tint color of the search input field.
     * @param searchInputStartIconTint The color integer for the start icon tint.
     */
    public void setSearchInputStartIconTint(@ColorInt int searchInputStartIconTint) {
        this.searchInputStartIconTint = searchInputStartIconTint;
        binding.searchBox.setSearchInputStartIconTint(searchInputStartIconTint);
    }

    /**
     * Retrieves the end icon drawable of the search input field.
     *
     * @return The current end icon drawable of the search input field.
     */
    public Drawable getSearchInputEndIcon() {
        return searchInputEndIcon;
    }

    /**
     * Sets the end icon drawable of the search input field.
     * @param searchInputEndIcon The drawable for the end icon.
     */
    public void setSearchInputEndIcon(Drawable searchInputEndIcon) {
        this.searchInputEndIcon = searchInputEndIcon;
        binding.searchBox.setSearchInputEndIcon(searchInputEndIcon);
    }

    /**
     * Retrieves the end icon tint color of the search input field.
     *
     * @return The current end icon tint color of the search input field.
     */
    public @ColorInt int getSearchInputEndIconTint() {
        return searchInputEndIconTint;
    }

    /**
     * Sets the end icon tint color of the search input field.
     * @param searchInputEndIconTint The color integer for the end icon tint.
     */
    public void setSearchInputEndIconTint(@ColorInt int searchInputEndIconTint) {
        this.searchInputEndIconTint = searchInputEndIconTint;
        binding.searchBox.setSearchInputEndIconTint(searchInputEndIconTint);
    }

    /**
     * Gets the placeholder text for the search input.
     *
     * @return The search placeholder text.
     */
    public String getSearchPlaceholderText() {
        return searchPlaceholderText;
    }

    /**
     * Sets the placeholder text for the search input field.
     *
     * @param placeholder The text to be set as the placeholder.
     */
    public void setSearchPlaceholderText(String placeholder) {
        if (placeholder != null) {
            binding.searchBox.setSearchPlaceholderText(placeholder);
        }
    }

    /**
     * Gets the stroke width for the search input.
     *
     * @return the stroke width for the search input.
     */
    public @Dimension int getSearchInputStrokeWidth() {
        return searchInputStrokeWidth;
    }

    /**
     * Sets the stroke width for the search input.
     *
     * @param searchInputStrokeWidth the stroke width for the search input to set.
     */
    public void setSearchInputStrokeWidth(@Dimension int searchInputStrokeWidth) {
        this.searchInputStrokeWidth = searchInputStrokeWidth;
        binding.searchBox.setStrokeWidth(searchInputStrokeWidth);
    }

    /**
     * Gets the stroke color for the search input.
     *
     * @return the stroke color for the search input.
     */
    public @ColorInt int getSearchInputStrokeColor() {
        return searchInputStrokeColor;
    }

    /**
     * Sets the stroke color for the search input.
     *
     * @param searchInputStrokeColor the stroke color for the search input to set.
     */
    public void setSearchInputStrokeColor(@ColorInt int searchInputStrokeColor) {
        this.searchInputStrokeColor = searchInputStrokeColor;
        binding.searchBox.setStrokeColor(searchInputStrokeColor);
    }

    /**
     * Gets the background color for the search input.
     *
     * @return the background color for the search input.
     */
    public @ColorInt int getSearchInputBackgroundColor() {
        return searchInputBackgroundColor;
    }

    /**
     * Sets the background color for the search input.
     *
     * @param searchInputBackgroundColor the background color for the search input to set.
     */
    public void setSearchInputBackgroundColor(@ColorInt int searchInputBackgroundColor) {
        this.searchInputBackgroundColor = searchInputBackgroundColor;
        binding.searchBox.setCardBackgroundColor(searchInputBackgroundColor);
    }

    /**
     * Retrieves the visibility status of the search box.
     *
     * @return An integer representing the visibility of the search box.
     */
    public int getSearchBoxVisibility() {
        return searchBoxVisibility;
    }

    /**
     * Sets the visibility of the search box based on the provided visibility
     * parameter. If {@code
     * hideSearchBox} is true, the search box will be hidden (set to
     * {@code View.GONE}). Otherwise, the visibility will be set according to the
     * provided {@code visibility} parameter.
     *
     * @param visibility the desired visibility state of the search box.
     */
    public void setSearchBoxVisibility(int visibility) {
        this.searchBoxVisibility = visibility;
        binding.searchBox.setVisibility(visibility);
        binding.searchBoxLayout.setVisibility(visibility);
    }

    /**
     * Sets the corner radius for the search input.
     *
     * @param searchInputCornerRadius the corner radius for the search input to set.
     */
    public void setSearchInputCornerRadius(@Dimension int searchInputCornerRadius) {
        this.searchInputCornerRadius = searchInputCornerRadius;
        binding.searchBox.setRadius(searchInputCornerRadius);
    }

    /**
     * Gets the corner radius for the search input.
     *
     * @return the corner radius for the search input.
     */
    public @Dimension int getSearchInputCornerRadius() {
        return searchInputCornerRadius;
    }

    /**
     * Sets the style for the CometChatConversations view by applying a style
     * resource.
     *
     * @param style The style resource to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatConversations);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Extracts the attributes and applies the default values if they are not set in
     * the XML.
     *
     * @param typedArray The TypedArray containing the attributes to be extracted.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            // Extract attributes or apply default values
            // Colors
            backIconTint = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsBackIconTint, CometChatTheme.getIconTintPrimary(getContext()));
            deleteOptionIconTint = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsDeleteOptionIconTint, CometChatTheme.getErrorColor(getContext()));
            strokeColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsStrokeColor, 0);
            backgroundColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));
            titleTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsTitleTextColor, CometChatTheme.getTextColorPrimary(getContext()));
            emptyStateTitleTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsEmptyStateTitleTextColor, CometChatTheme.getTextColorPrimary(getContext()));
            emptyStateSubtitleTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsEmptyStateSubtitleTextColor, CometChatTheme.getTextColorSecondary(getContext()));
            errorStateTitleTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsErrorStateTitleTextColor, CometChatTheme.getTextColorPrimary(getContext()));
            errorStateSubtitleTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsErrorStateSubtitleTextColor, CometChatTheme.getTextColorSecondary(getContext()));
            itemTitleTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsItemTitleTextColor, CometChatTheme.getTextColorPrimary(getContext()));
            deleteOptionTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsDeleteOptionTextColor, 0);
            itemSubtitleTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsItemSubtitleTextColor, CometChatTheme.getTextColorSecondary(getContext()));
            itemMessageTypeIconTint = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsItemMessageTypeIconTint, CometChatTheme.getIconTintHighlight(getContext()));
            // Dimensions
            strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatConversations_cometchatConversationsStrokeWidth, 0);
            cornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatConversations_cometchatConversationsCornerRadius, 0);
            // Styles
            titleTextAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsTitleTextAppearance, 0);
            emptyStateTextTitleAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsEmptyStateTextTitleAppearance, 0);
            deleteOptionTextAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsDeleteOptionTextAppearance, 0);
            emptyStateTextSubtitleAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsEmptyStateTextSubtitleAppearance, 0);
            errorStateTextTitleAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsErrorStateTextTitleAppearance, 0);
            errorStateTextSubtitleAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsErrorStateTextSubtitleAppearance, 0);
            itemTitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsItemTitleTextAppearance, 0);
            itemSubtitleTextAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsItemSubtitleTextAppearance, 0);
            avatarStyle = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsAvatarStyle, 0);
            statusIndicatorStyle = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsStatusIndicatorStyle, 0);
            dateStyle = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsDateStyle, 0);
            badgeStyle = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsBadgeStyle, 0);
            receiptStyle = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsReceiptStyle, 0);
            typingIndicatorStyle = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsTypingIndicatorStyle, 0);
            mentionsStyle = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsMentionsStyle, 0);
            separatorColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsSeparatorColor, CometChatTheme.getStrokeColorLight(getContext()));
            separatorHeight = typedArray.getDimensionPixelSize(R.styleable.CometChatConversations_cometchatConversationsSeparatorHeight, 1);

            selectedConversationAvatarStyle = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsSelectedConversationsAvatarStyle,0);
            selectedConversationItemTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsSelectedConversationsItemTextColor,
                    CometChatTheme.getTextColorSecondary(getContext()));
            selectedConversationItemTextAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsSelectedConversationsItemTextAppearance,0);
            selectedConversationItemRemoveIcon = typedArray.getDrawable(R.styleable.CometChatConversations_cometchatConversationsSelectedConversationsItemRemoveIcon);
            selectedConversationItemRemoveIconTint = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsSelectedConversationsItemRemoveIconTint,
                    CometChatTheme.getIconTintWhite(getContext()));
            discardSelectionIcon = typedArray.getDrawable(R.styleable.CometChatConversations_cometchatConversationsDiscardSelectionIcon);
            discardSelectionIconTint = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsDiscardSelectionIconTint, CometChatTheme.getIconTintPrimary(getContext()));
            submitSelectionIcon = typedArray.getDrawable(R.styleable.CometChatConversations_cometchatConversationsSubmitSelectionIcon);
            submitSelectionIconTint = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsSubmitSelectionIconTint, CometChatTheme.getIconTintPrimary(getContext()));
            checkBoxStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatConversations_cometchatConversationsCheckBoxStrokeWidth, 0);
            checkBoxCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatConversations_cometchatConversationsCheckBoxCornerRadius, 0);
            checkBoxStrokeColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsCheckBoxStrokeColor, CometChatTheme.getStrokeColorDefault(getContext()));
            checkBoxBackgroundColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsCheckBoxBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));
            checkBoxCheckedBackgroundColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsCheckBoxCheckedBackgroundColor, CometChatTheme.getIconTintHighlight(getContext()));
            checkBoxSelectIcon = typedArray.getDrawable(R.styleable.CometChatConversations_cometchatConversationsCheckBoxSelectIcon);
            checkBoxSelectIconTint = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsCheckBoxSelectIconTint, CometChatTheme.getColorWhite(getContext()));
            itemSelectedBackgroundColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsItemSelectedBackgroundColor, CometChatTheme.getBackgroundColor4(getContext()));
            itemBackgroundColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsItemBackgroundColor, CometChatTheme.getBackgroundColor1(getContext()));
            // Drawables
            backIcon = typedArray.getDrawable(R.styleable.CometChatConversations_cometchatConversationsBackIcon);
            deleteOptionIcon = typedArray.getDrawable(R.styleable.CometChatConversations_cometchatConversationsDeleteOptionIcon);
            optionListStyle = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsOptionListStyle, 0);

            // Search
            searchInputBackgroundColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsSearchInputBackgroundColor, CometChatTheme.getBackgroundColor3(getContext()));
            searchInputTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsSearchInputTextColor, CometChatTheme.getTextColorPrimary(getContext()));
            searchInputTextAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsSearchInputTextAppearance, 0);
            searchInputPlaceHolderTextColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsSearchInputPlaceHolderTextColor, CometChatTheme.getTextColorTertiary(getContext()));
            searchInputPlaceHolderTextAppearance = typedArray.getResourceId(R.styleable.CometChatConversations_cometchatConversationsSearchInputPlaceHolderTextAppearance, 0);
            searchInputEndIcon = typedArray.getDrawable(R.styleable.CometChatConversations_cometchatConversationsSearchInputEndIcon);
            searchInputEndIconTint = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsSearchInputEndIconTint, CometChatTheme.getIconTintSecondary(getContext()));
            searchInputStartIcon = typedArray.getDrawable(R.styleable.CometChatConversations_cometchatConversationsSearchInputStartIcon);
            searchInputStartIconTint = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsSearchInputStartIconTint, CometChatTheme.getIconTintSecondary(getContext()));
            searchInputCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatConversations_cometchatConversationsSearchInputCornerRadius, 0);
            searchInputStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatConversations_cometchatConversationsSearchInputStrokeWidth, 0);
            searchInputStrokeColor = typedArray.getColor(R.styleable.CometChatConversations_cometchatConversationsSearchInputStrokeColor, 0);
            // Apply default styles
            updateUI();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies the extracted or default values to the avatar's views.
     */
    private void updateUI() {
        // Styles
        setTitleTextAppearance(titleTextAppearance);
        setEmptyStateTextTitleAppearance(emptyStateTextTitleAppearance);
        setEmptyStateTextSubtitleAppearance(emptyStateTextSubtitleAppearance);
        setErrorStateTextTitleAppearance(errorStateTextTitleAppearance);
        setErrorStateTextSubtitleAppearance(errorStateTextSubtitleAppearance);
        setItemTitleTextAppearance(itemTitleTextAppearance);
        setDeleteOptionTextAppearance(deleteOptionTextAppearance);
        setItemSubtitleTextAppearance(itemSubtitleTextAppearance);
        setAvatarStyle(avatarStyle);
        setStatusIndicatorStyle(statusIndicatorStyle);
        setDateStyle(dateStyle);
        setBadgeStyle(badgeStyle);
        setReceiptStyle(receiptStyle);
        setTypingIndicatorStyle(typingIndicatorStyle);
        setMentionsStyle(mentionsStyle);
        // Colors
        setBackIconTint(backIconTint);
        setDeleteOptionIconTint(deleteOptionIconTint);
        setStrokeColor(strokeColor);
        setBackgroundColor(backgroundColor);
        setTitleTextColor(titleTextColor);
        setEmptyStateTitleTextColor(emptyStateTitleTextColor);
        setEmptyStateSubtitleTextColor(emptyStateSubtitleTextColor);
        setErrorStateTitleTextColor(errorStateTitleTextColor);
        setErrorStateSubtitleTextColor(errorStateSubtitleTextColor);
        setItemTitleTextColor(itemTitleTextColor);
        setDeleteOptionTextColor(deleteOptionTextColor);
        setItemSubtitleTextColor(itemSubtitleTextColor);
        setItemMessageTypeIconTint(itemMessageTypeIconTint);
        setSeparatorHeight(separatorHeight);
        setSeparatorColor(separatorColor);
        // Dimensions
        setStrokeWidth(strokeWidth);
        setCornerRadius(cornerRadius);
        // Drawables
        setBackIcon(backIcon);

        setDiscardSelectionIcon(discardSelectionIcon);
        setDiscardSelectionIconTint(discardSelectionIconTint);
        setSubmitSelectionIcon(submitSelectionIcon);
        setSubmitSelectionIconTint(submitSelectionIconTint);
        setCheckBoxStrokeWidth(checkBoxStrokeWidth);
        setCheckBoxCornerRadius(checkBoxCornerRadius);
        setCheckBoxStrokeColor(checkBoxStrokeColor);
        setCheckBoxBackgroundColor(checkBoxBackgroundColor);
        setCheckBoxCheckedBackgroundColor(checkBoxCheckedBackgroundColor);
        setCheckBoxSelectIcon(checkBoxSelectIcon);
        setCheckBoxSelectIconTint(checkBoxSelectIconTint);
        setItemSelectedBackgroundColor(itemSelectedBackgroundColor);
        setItemBackgroundColor(itemBackgroundColor);

        // View Handing
        binding.tvSelectionCount.setTextAppearance(itemTitleTextAppearance);
        binding.tvSelectionCount.setTextColor(itemTitleTextColor);
        setSelectionMode(UIKitConstants.SelectionMode.NONE);
        setToolbarVisibility(toolbarVisibility);
        setBackIconVisibility(backIconVisibility);
        setDeleteOptionIcon(deleteOptionIcon);
        setOptionListStyle(optionListStyle);
        setSearchInputBackgroundColor(searchInputBackgroundColor);
        setSearchInputTextColor(searchInputTextColor);
        setSearchInputTextAppearance(searchInputTextAppearance);
        setSearchInputTextAppearance(searchInputTextAppearance);
        setSearchInputPlaceHolderTextColor(searchInputPlaceHolderTextColor);
        setSearchInputPlaceHolderTextAppearance(searchInputPlaceHolderTextAppearance);
        setSearchInputEndIcon(searchInputEndIcon);
        setSearchInputEndIconTint(searchInputEndIconTint);
        setSearchInputStartIcon(searchInputStartIcon);
        setSearchInputStartIconTint(searchInputStartIconTint);
        setSearchInputCornerRadius(searchInputCornerRadius);
        setSearchInputStrokeWidth(searchInputStrokeWidth);
        setSearchInputStrokeColor(searchInputStrokeColor);
        setSelectedConversationAvatarStyle(selectedConversationAvatarStyle);
        setSelectedConversationItemTextColor(selectedConversationItemTextColor);
        setSelectedConversationItemTextAppearance(selectedConversationItemTextAppearance);
        setSelectedConversationItemRemoveIcon(selectedConversationItemRemoveIcon);
        setSelectedConversationItemRemoveIconTint(selectedConversationItemRemoveIconTint);
    }

    /**
     * Sets up the layout and adapter for displaying selected conversations avatar.
     */
    private void configureSelectedConversationsView() {
        selectedConversationsAdapter = new SelectedConversationsAdapter();
        selectedConversationsAdapter.setOnRemoveClickListener(group -> {
            hashMap.remove(group);
            conversationsAdapter.selectConversation(hashMap);
            updateSelectionUI();
        });

        binding.rvSelectedConversation.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );
        binding.rvSelectedConversation.setVisibility(View.GONE);
    }

    /**
     * Updates the selected conversations list based on the current selection.
     */
    private void refreshSelectedConversations() {
        List<Conversation> selectedConversations = getSelectedConversations();
        List<Conversation> currentConversations = selectedConversationsAdapter.getConversations();

        for (Conversation conversation : selectedConversations) {
            if (!containsConversationWithId(currentConversations, conversation)) {
                selectedConversationsAdapter.addConversation(conversation);
            }
        }

        for (Conversation conversation : currentConversations) {
            if (!containsConversationWithId(selectedConversations, conversation)) {
                selectedConversationsAdapter.removeConversation(conversation);
            }
        }

        if (!selectedConversations.isEmpty()) {
            binding.rvSelectedConversation.smoothScrollToPosition(
                    selectedConversations.size() - 1
            );
        }
    }

    /**
     * Helper method to check if the given conversation is present in the provided list.
     */
    private boolean containsConversationWithId(List<Conversation> list, Conversation conversation) {
        for (Conversation c : list) {
            if (c.getConversationId().equals(conversation.getConversationId())) return true;
        }
        return false;
    }

    /**
     * Sets the style resource ID for the conversations date.
     *
     * @param dateStyle the style resource ID to use for the date.
     */
    public void setDateStyle(@StyleRes int dateStyle) {
        this.dateStyle = dateStyle;
        conversationsAdapter.setConversationsDateStyle(dateStyle);
    }

    /**
     * Sets the selection mode for the users view.
     *
     * @param selectionMode The selection mode to set.
     */
    public void setSelectionMode(@NonNull UIKitConstants.SelectionMode selectionMode) {
        hashMap.clear();
        conversationsAdapter.selectConversation(hashMap);
        this.selectionMode = selectionMode;
        if (UIKitConstants.SelectionMode.MULTIPLE.equals(selectionMode) || UIKitConstants.SelectionMode.SINGLE.equals(selectionMode)) {
            isFurtherSelectionEnabled = true;
            conversationsAdapter.setSelectionEnabled(true);
            setDiscardSelectionVisibility(VISIBLE);
            setSubmitSelectionIconVisibility(GONE);
            setSelectionCountVisibility(VISIBLE);
        } else {
            isFurtherSelectionEnabled = false;
            conversationsAdapter.setSelectionEnabled(false);
            setDiscardSelectionVisibility(GONE);
            setSubmitSelectionIconVisibility(GONE);
            setSelectionCountVisibility(GONE);
        }
    }

    public void setOptionListStyle(@StyleRes int optionListStyle) {
        if (optionListStyle == 0) return;
        this.optionListStyle = optionListStyle;
        cometchatPopUpMenu.setStyle(optionListStyle);
    }

    /**
     * Sets the visibility of the discard selection icon based on the provided
     * visibility parameter. If {@code hideDiscardSelectionIcon} is true or the
     * visibility is set to {@code View.GONE}, the discard selection icon will be
     * hidden. Otherwise, the discard selection icon will be visible, and the back
     * icon will be hidden.
     *
     * @param visibility the desired visibility state of the discard selection icon.
     */
    private void setDiscardSelectionVisibility(int visibility) {
        if (visibility == View.GONE) {
            binding.ivDiscardSelection.setVisibility(View.GONE);
        } else {
            if (!hashMap.isEmpty()) {
                binding.ivDiscardSelection.setVisibility(View.VISIBLE);
                binding.ivBack.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Sets the visibility of the submit selection icon based on the provided
     * visibility parameter. If {@code hideSubmitSelectionIcon} is true, the icon
     * will be hidden (set to {@code View.GONE}). Otherwise, the visibility will be
     * set according to the provided {@code visibility} parameter.
     *
     * @param visibility the desired visibility state of the submit selection icon.
     */
    private void setSubmitSelectionIconVisibility(int visibility) {
        if (visibility == GONE) {
            binding.ivSubmitSelection.setVisibility(GONE);
        } else {
            binding.ivSubmitSelection.setVisibility(VISIBLE);
        }
    }

    /**
     * Sets the visibility of the selection count based on the provided visibility
     * parameter. If {@code hideSelectionCount} is true, the selection count will be
     * hidden (set to {@code GONE}). Otherwise, the visibility will be set according
     * to the provided {@code visibility} parameter.
     *
     * @param visibility the desired visibility state of the selection count.
     */
    private void setSelectionCountVisibility(int visibility) {
        if (visibility == GONE) {
            binding.tvSelectionCount.setVisibility(GONE);
        } else {
            binding.tvSelectionCount.setVisibility(VISIBLE);
            if (!hashMap.isEmpty()) setTitleVisibility(GONE);
        }
    }

    /**
     * Sets the visibility of the title based on the provided visibility parameter.
     * If the selection count is visible, the title will be hidden (set to
     * {@code GONE}). Otherwise, the visibility will be set according to the provided
     * {@code visibility} parameter.
     *
     * @param visibility the desired visibility state of the title.
     */
    private void setTitleVisibility(int visibility) {
        binding.tvTitle.setVisibility(visibility);
    }

    public DateTimeFormatterCallback getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public void setDateTimeFormatter(@NonNull DateTimeFormatterCallback dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
        conversationsAdapter.setDateTimeFormatter(dateTimeFormatter);
    }

    /**
     * Gets the tint color of the back icon in the conversations toolbar.
     *
     * @return the color used for tinting the back icon.
     */
    public @ColorInt int getBackIconTint() {
        return backIconTint;
    }

    /**
     * Sets the tint color for the back icon in the conversations toolbar.
     *
     * @param backIconTint the color to tint the back icon.
     */
    public void setBackIconTint(@ColorInt int backIconTint) {
        this.backIconTint = backIconTint;
        binding.ivBack.setImageTintList(ColorStateList.valueOf(backIconTint));
    }

    /**
     * Gets the background color for the conversations card.
     *
     * @return the background color used for the conversations card.
     */
    public @ColorInt int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for the conversations card.
     *
     * @param backgroundColor the color to use for the card's background.
     */
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        setCardBackgroundColor(backgroundColor);
    }

    /**
     * Gets the text color for the title in the conversations toolbar.
     *
     * @return the text color used for the title.
     */
    public @ColorInt int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the text color for the title in the conversations toolbar.
     *
     * @param titleTextColor the color to use for the title text.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        binding.tvTitle.setTextColor(titleTextColor);
    }

    /**
     * Gets the text color for the title in the empty state of conversations.
     *
     * @return the text color used for the empty state title.
     */
    public @ColorInt int getEmptyStateTitleTextColor() {
        return emptyStateTitleTextColor;
    }

    /**
     * Sets the text color for the title in the empty state of conversations.
     *
     * @param emptyStateTitleTextColor the color to use for the empty state title text.
     */
    public void setEmptyStateTitleTextColor(@ColorInt int emptyStateTitleTextColor) {
        this.emptyStateTitleTextColor = emptyStateTitleTextColor;
        binding.tvEmptyConversationsTitle.setTextColor(emptyStateTitleTextColor);
    }

    /**
     * Gets the text color for the subtitle in the empty state of conversations.
     *
     * @return the text color used for the empty state subtitle.
     */
    public @ColorInt int getEmptyStateSubtitleTextColor() {
        return emptyStateSubtitleTextColor;
    }

    /**
     * Sets the text color for the subtitle in the empty state of conversations.
     *
     * @param emptyStateSubtitleTextColor the color to use for the empty state subtitle text.
     */
    public void setEmptyStateSubtitleTextColor(@ColorInt int emptyStateSubtitleTextColor) {
        this.emptyStateSubtitleTextColor = emptyStateSubtitleTextColor;
        binding.tvEmptyConversationsSubtitle.setTextColor(emptyStateSubtitleTextColor);
    }

    /**
     * Gets the text color for the title in the error state of conversations.
     *
     * @return the text color used for the error state title.
     */
    public @ColorInt int getErrorStateTitleTextColor() {
        return errorStateTitleTextColor;
    }

    /**
     * Sets the text color for the title in the error state of conversations.
     *
     * @param errorStateTitleTextColor the color to use for the error state title text.
     */
    public void setErrorStateTitleTextColor(@ColorInt int errorStateTitleTextColor) {
        this.errorStateTitleTextColor = errorStateTitleTextColor;
        binding.tvErrorTitle.setTextColor(errorStateTitleTextColor);
    }

    /**
     * Gets the text color for the subtitle in the error state of conversations.
     *
     * @return the text color used for the error state subtitle.
     */
    public @ColorInt int getErrorStateSubtitleTextColor() {
        return errorStateSubtitleTextColor;
    }

    /**
     * Sets the text color for the subtitle in the error state of conversations.
     *
     * @param errorStateSubtitleTextColor the color to use for the error state subtitle text.
     */
    public void setErrorStateSubtitleTextColor(@ColorInt int errorStateSubtitleTextColor) {
        this.errorStateSubtitleTextColor = errorStateSubtitleTextColor;
        binding.tvErrorSubtitle.setTextColor(errorStateSubtitleTextColor);
    }

    /**
     * Gets the text color for the item title in the conversations list.
     *
     * @return the text color used for item titles.
     */
    public @ColorInt int getItemTitleTextColor() {
        return itemTitleTextColor;
    }

    /**
     * Sets the text color for item titles in the conversations list.
     *
     * @param itemTitleTextColor the color to use for item title text.
     */
    public void setItemTitleTextColor(@ColorInt int itemTitleTextColor) {
        this.itemTitleTextColor = itemTitleTextColor;
        conversationsAdapter.setConversationsItemTitleTextColor(itemTitleTextColor);
    }

    /**
     * Gets the text color for item subtitles in the conversations list.
     *
     * @return the text color used for item subtitles.
     */
    public @ColorInt int getItemSubtitleTextColor() {
        return itemSubtitleTextColor;
    }

    /**
     * Sets the text color for item subtitles in the conversations list.
     *
     * @param itemSubtitleTextColor the color to use for item subtitle text.
     */
    public void setItemSubtitleTextColor(@ColorInt int itemSubtitleTextColor) {
        this.itemSubtitleTextColor = itemSubtitleTextColor;
        conversationsAdapter.setConversationsItemSubtitleTextColor(itemSubtitleTextColor);
    }

    /**
     * Gets the tint color for the message type icon in the conversations list.
     *
     * @return the tint color used for message type icons.
     */
    public @ColorInt int getItemMessageTypeIconTint() {
        return itemMessageTypeIconTint;
    }

    /**
     * Sets the tint color for the message type icon in the conversations list.
     *
     * @param itemMessageTypeIconTint the color to use for the message type icon tint.
     */
    public void setItemMessageTypeIconTint(@ColorInt int itemMessageTypeIconTint) {
        this.itemMessageTypeIconTint = itemMessageTypeIconTint;
        conversationsAdapter.setConversationsItemMessageTypeIconTint(itemMessageTypeIconTint);
    }

    /**
     * Gets the color of the conversations separator.
     *
     * @return the color of the separator as an integer annotated with @ColorInt.
     */
    public @ColorInt int getSeparatorColor() {
        return separatorColor;
    }

    /**
     * Sets the color of the conversations separator.
     *
     * @param separatorColor the color to set for the separator, annotated with @ColorInt.
     */
    public void setSeparatorColor(@ColorInt int separatorColor) {
        this.separatorColor = separatorColor;
        binding.viewSeparator.setBackgroundColor(separatorColor);
    }

    /**
     * Gets the height of the conversations separator.
     *
     * @return the height of the separator as an integer annotated with @Dimension.
     */
    public @Dimension int getSeparatorHeight() {
        return separatorHeight;
    }

    /**
     * Sets the height of the conversations separator.
     *
     * @param separatorHeight the height to set for the separator, annotated with @Dimension.
     */
    public void setSeparatorHeight(@Dimension int separatorHeight) {
        this.separatorHeight = separatorHeight;
        binding.viewSeparator.getLayoutParams().height = separatorHeight;
    }

    /**
     * Gets the visibility of the separator in conversations toolbar.
     *
     * @return the visibility of the separator.
     */
    public int getSeparatorVisibility() {
        return binding.viewSeparator.getVisibility();
    }

    /**
     * set the visibility of the separator in conversations toolbar.
     *
     * @param visibility the visibility of the separator.
     */
    public void setSeparatorVisibility(int visibility) {
        binding.viewSeparator.setVisibility(visibility);
    }

    /**
     * Gets the drawable for the delete option icon in the conversations popup menu.
     *
     * @return the drawable used for the delete option.
     */
    public Drawable getDeleteOptionIcon() {
        return deleteOptionIcon;
    }

    /**
     * Sets the drawable for the delete option icon in the conversations popup menu.
     *
     * @param deleteOptionIcon the drawable to use for the delete option.
     */
    public void setDeleteOptionIcon(Drawable deleteOptionIcon) {
        this.deleteOptionIcon = deleteOptionIcon;
    }

    /**
     * Gets the tint color for the delete option icon in the conversations popup menu.
     *
     * @return the color used for the delete option icon tint.
     */
    public int getDeleteOptionIconTint() {
        return deleteOptionIconTint;
    }

    /**
     * Sets the tint color for the delete option icon in the conversations popup menu.
     *
     * @param deleteOptionIconTint the color to use for the delete option icon tint.
     */
    public void setDeleteOptionIconTint(int deleteOptionIconTint) {
        this.deleteOptionIconTint = deleteOptionIconTint;
    }

    /**
     * Gets the text color for the delete option in the conversations popup menu.
     *
     * @return the color used for the delete option text.
     */
    public int getDeleteOptionTextColor() {
        return deleteOptionTextColor;
    }

    /**
     * Sets the text color for the delete option in the conversations popup menu.
     *
     * @param deleteOptionTextColor the color to use for the delete option text.
     */
    public void setDeleteOptionTextColor(int deleteOptionTextColor) {
        this.deleteOptionTextColor = deleteOptionTextColor;
    }    /**
     * Gets the stroke color for the conversations card.
     *
     * @return the stroke color used for the conversations card.
     */
    public @ColorInt int getStrokeColor() {
        return strokeColor;
    }

    /**
     * Gets the text appearance style resource ID for the delete option in the
     * conversations popup menu.
     *
     * @return the style resource ID used for the delete option text appearance.
     */
    public int getDeleteOptionTextAppearance() {
        return deleteOptionTextAppearance;
    }

    /**
     * Sets the text appearance style for the delete option in the conversations
     * popup menu.
     *
     * @param deleteOptionTextAppearance the style resource ID to use for the delete option text appearance.
     */
    public void setDeleteOptionTextAppearance(int deleteOptionTextAppearance) {
        this.deleteOptionTextAppearance = deleteOptionTextAppearance;
    }

    /**
     * Gets the corner radius for the conversations card.
     *
     * @return the corner radius used for the conversations card.
     */
    public @Dimension int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the conversations card.
     *
     * @param cornerRadius the radius to use for the card's corners.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        super.setRadius(cornerRadius);
    }

    /**
     * Gets the text appearance style resource ID for the title in the conversations
     * toolbar.
     *
     * @return the style resource ID used for the title text appearance.
     */
    public @StyleRes int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the text appearance style for the title in the conversations toolbar.
     *
     * @param titleTextAppearance the style resource ID to use for the title text appearance.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.tvTitle.setTextAppearance(titleTextAppearance);
    }

    /**
     * Gets the text appearance style resource ID for the title in the empty state
     * of conversations.
     *
     * @return the style resource ID used for the empty state title text appearance.
     */
    public @StyleRes int getEmptyStateTextTitleAppearance() {
        return emptyStateTextTitleAppearance;
    }

    /**
     * Sets the text appearance style for the title in the empty state of
     * conversations.
     *
     * @param emptyStateTextTitleAppearance the style resource ID to use for the empty state title text
     *                                      appearance.
     */
    public void setEmptyStateTextTitleAppearance(@StyleRes int emptyStateTextTitleAppearance) {
        this.emptyStateTextTitleAppearance = emptyStateTextTitleAppearance;
        binding.tvEmptyConversationsTitle.setTextAppearance(emptyStateTextTitleAppearance);
    }

    /**
     * Gets the text appearance style resource ID for the subtitle in the empty
     * state of conversations.
     *
     * @return the style resource ID used for the empty state subtitle text
     * appearance.
     */
    public @StyleRes int getEmptyStateTextSubtitleAppearance() {
        return emptyStateTextSubtitleAppearance;
    }

    /**
     * Sets the text appearance style for the subtitle in the empty state of
     * conversations.
     *
     * @param emptyStateTextSubtitleAppearance the style resource ID to use for the empty state subtitle text
     *                                         appearance.
     */
    public void setEmptyStateTextSubtitleAppearance(@StyleRes int emptyStateTextSubtitleAppearance) {
        this.emptyStateTextSubtitleAppearance = emptyStateTextSubtitleAppearance;
        binding.tvEmptyConversationsSubtitle.setTextAppearance(emptyStateTextSubtitleAppearance);
    }

    /**
     * Gets the text appearance style resource ID for the title in the error state
     * of conversations.
     *
     * @return the style resource ID used for the error state title text appearance.
     */
    public @StyleRes int getErrorStateTextTitleAppearance() {
        return errorStateTextTitleAppearance;
    }

    /**
     * Sets the text appearance style for the title in the error state of
     * conversations.
     *
     * @param errorStateTextTitleAppearance the style resource ID to use for the error state title text
     *                                      appearance.
     */
    public void setErrorStateTextTitleAppearance(@StyleRes int errorStateTextTitleAppearance) {
        this.errorStateTextTitleAppearance = errorStateTextTitleAppearance;
        binding.tvErrorTitle.setTextAppearance(errorStateTextTitleAppearance);
    }

    /**
     * Gets the text appearance style resource ID for the subtitle in the error
     * state of conversations.
     *
     * @return the style resource ID used for the error state subtitle text
     * appearance.
     */
    public @StyleRes int getErrorStateTextSubtitleAppearance() {
        return errorStateTextSubtitleAppearance;
    }

    /**
     * Sets the text appearance style for the subtitle in the error state of
     * conversations.
     *
     * @param errorStateTextSubtitleAppearance the style resource ID to use for the error state subtitle text
     *                                         appearance.
     */
    public void setErrorStateTextSubtitleAppearance(@StyleRes int errorStateTextSubtitleAppearance) {
        this.errorStateTextSubtitleAppearance = errorStateTextSubtitleAppearance;
        binding.tvErrorSubtitle.setTextAppearance(errorStateTextSubtitleAppearance);
    }

    /**
     * Gets the text appearance style resource ID for the title in the conversations
     * item.
     *
     * @return the style resource ID used for item title text appearance.
     */
    public @StyleRes int getItemTitleTextAppearance() {
        return itemTitleTextAppearance;
    }

    /**
     * Sets the text appearance style for the title in the conversations item.
     *
     * @param itemTitleTextAppearance the style resource ID to use for the item title text appearance.
     */
    public void setItemTitleTextAppearance(@StyleRes int itemTitleTextAppearance) {
        this.itemTitleTextAppearance = itemTitleTextAppearance;
        conversationsAdapter.setConversationsItemTitleTextAppearance(itemTitleTextAppearance);
    }

    /**
     * Gets the text appearance style resource ID for the subtitle in the
     * conversations item.
     *
     * @return the style resource ID used for item subtitle text appearance.
     */
    public @StyleRes int getItemSubtitleTextAppearance() {
        return itemSubtitleTextAppearance;
    }

    /**
     * Sets the text appearance style for the subtitle in the conversations item.
     *
     * @param itemSubtitleTextAppearance the style resource ID to use for the item subtitle text
     *                                   appearance.
     */
    public void setItemSubtitleTextAppearance(@StyleRes int itemSubtitleTextAppearance) {
        this.itemSubtitleTextAppearance = itemSubtitleTextAppearance;
        conversationsAdapter.setConversationsItemSubtitleTextAppearance(itemSubtitleTextAppearance);
    }

    /**
     * Gets the style resource ID for the conversations avatar.
     *
     * @return the style resource ID used for the avatar.
     */
    public @StyleRes int getAvatarStyle() {
        return avatarStyle;
    }

    /**
     * Sets the style resource ID for the conversations avatar.
     *
     * @param avatarStyle the style resource ID to use for the avatar.
     */
    public void setAvatarStyle(@StyleRes int avatarStyle) {
        this.avatarStyle = avatarStyle;
        conversationsAdapter.setConversationsAvatarStyle(avatarStyle);
    }

    /**
     * Gets the style resource ID for the conversations status indicator.
     *
     * @return the style resource ID used for the status indicator.
     */
    public @StyleRes int getStatusIndicatorStyle() {
        return statusIndicatorStyle;
    }

    /**
     * Sets the style resource ID for the conversations status indicator.
     *
     * @param statusIndicatorStyle the style resource ID to use for the status indicator.
     */
    public void setStatusIndicatorStyle(@StyleRes int statusIndicatorStyle) {
        this.statusIndicatorStyle = statusIndicatorStyle;
        conversationsAdapter.setConversationsStatusIndicatorStyle(statusIndicatorStyle);
    }

    /**
     * Gets the style resource ID for the conversations badge.
     *
     * @return the style resource ID used for the badge.
     */
    public @StyleRes int getBadgeStyle() {
        return badgeStyle;
    }

    /**
     * Sets the style resource ID for the conversations badge.
     *
     * @param badgeStyle the style resource ID to use for the badge.
     */
    public void setBadgeStyle(@StyleRes int badgeStyle) {
        this.badgeStyle = badgeStyle;
        conversationsAdapter.setConversationsBadgeStyle(badgeStyle);
    }

    /**
     * Gets the style resource ID for the conversations receipt.
     *
     * @return the style resource ID used for the receipt.
     */
    public @StyleRes int getReceiptStyle() {
        return receiptStyle;
    }

    /**
     * Sets the style resource ID for the conversations receipt.
     *
     * @param receiptStyle the style resource ID to use for the receipt.
     */
    public void setReceiptStyle(@StyleRes int receiptStyle) {
        this.receiptStyle = receiptStyle;
        conversationsAdapter.setConversationsReceiptStyle(receiptStyle);
    }

    /**
     * Gets the style resource ID for the typing indicator in conversations.
     *
     * @return the style resource ID used for the typing indicator.
     */
    public @StyleRes int getTypingIndicatorStyle() {
        return typingIndicatorStyle;
    }

    /**
     * Sets the style resource ID for the typing indicator in conversations.
     *
     * @param typingIndicatorStyle the style resource ID to use for the typing indicator.
     */
    public void setTypingIndicatorStyle(@StyleRes int typingIndicatorStyle) {
        this.typingIndicatorStyle = typingIndicatorStyle;
        conversationsAdapter.setConversationsTypingIndicatorStyle(typingIndicatorStyle);
    }

    /**
     * Gets the style resource ID for conversation mentions.
     *
     * @return the style resource ID used for conversation mentions.
     */
    public @StyleRes int getMentionsStyle() {
        return mentionsStyle;
    }

    /**
     * Sets the style resource ID for conversation mentions.
     *
     * @param mentionsStyle the style resource ID to use for conversation mentions.
     */
    public void setMentionsStyle(@StyleRes int mentionsStyle) {
        this.mentionsStyle = mentionsStyle;
        cometchatMentionsFormatter.setConversationsMentionTextStyle(getContext(), mentionsStyle);
        conversationsAdapter.notifyDataSetChanged();
    }    /**
     * Sets the stroke color for the conversations card.
     *
     * @param strokeColor the color to use for the card's stroke.
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }

    /**
     * Gets the drawable for the back icon in conversations.
     *
     * @return the drawable used for the back icon.
     */
    public Drawable getBackIcon() {
        return backIcon;
    }

    /**
     * Sets the drawable for the back icon in conversations.
     *
     * @param backIcon the drawable to use for the back icon.
     */
    public void setBackIcon(Drawable backIcon) {
        this.backIcon = backIcon;
        binding.ivBack.setImageDrawable(backIcon);
    }

    /**
     * Sets the list of text formatters to use for formatting messages.
     *
     * @param cometchatTextFormatters the list of text formatters to apply.
     */
    public void setTextFormatters(List<CometChatTextFormatter> cometchatTextFormatters) {
        if (cometchatTextFormatters != null) {
            this.textFormatters.addAll(cometchatTextFormatters);
            processFormatters();
        }
    }

    /**
     * Processes the current list of text formatters and updates the conversations
     * adapter.
     */
    private void processFormatters() {
        conversationsAdapter.setTextFormatters(textFormatters);
    }

    public @LayoutRes int getEmptyView() {
        return emptyView;
    }

    /**
     * Sets the layout resource for the empty state view.
     *
     * @param id The layout resource ID for the empty state view.
     */
    public void setEmptyView(@LayoutRes int id) {
        if (id != 0) {
            try {
                this.emptyView = id;
                customEmptyView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customEmptyView = null;
            }
        }
    }

    public @LayoutRes int getErrorView() {
        return errorView;
    }

    /**
     * Sets the layout resource for the error state view.
     *
     * @param id The layout resource ID for the error state view.
     */
    public void setErrorView(@LayoutRes int id) {
        if (id != 0) {
            try {
                this.errorView = id;
                customErrorView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customErrorView = null;
            }
        }
    }

    public @LayoutRes int getLoadingView() {
        return loadingView;
    }

    /**
     * Sets the layout resource for the loading state view.
     *
     * @param id The layout resource ID for the loading state view.
     */
    public void setLoadingView(@LayoutRes int id) {
        if (id != 0) {
            try {
                this.loadingView = id;
                customLoadingView = View.inflate(getContext(), id, null);
            } catch (Exception e) {
                customLoadingView = null;
            }
        }
    }

    /**
     * Disables or enables sound for incoming messages.
     *
     * @param disableSoundForMessages true to disable sound for incoming messages, false to enable it.
     */
    public void disableSoundForMessages(boolean disableSoundForMessages) {
        this.disableSoundForMessages = disableSoundForMessages;
    }

    /**
     * Sets a custom sound for incoming messages.
     *
     * @param customSoundForMessages The resource ID of the custom sound for incoming messages.
     */
    public void setCustomSoundForMessages(@RawRes int customSoundForMessages) {
        this.customSoundForMessage = customSoundForMessages;
    }

    /**
     * Sets the callback for handling errors in the conversations view.
     *
     * @param onError The OnError callback for handling errors.
     */
    public void setOnError(OnError onError) {
        this.onError = onError;
    }

    public void setOnLoad(OnLoad<Conversation> onLoad) {
        this.onLoad = onLoad;
    }

    public void setOnEmpty(OnEmpty onEmpty) {
        this.onEmpty = onEmpty;
    }

    /**
     * Hide or show the read receipt in the conversations view.
     *
     * @param hideReceipts true to hide the read receipt, false to show it.
     */
    public void hideReceipts(boolean hideReceipts) {
        conversationsAdapter.hideReceipts(hideReceipts);
    }

    /**
     * Sets the date pattern for displaying dates in the conversations view.
     *
     * @param dateFormat The function that formats the date pattern for conversations.
     */
    public void setDateFormat(SimpleDateFormat dateFormat) {
        conversationsAdapter.setDateFormat(dateFormat);
    }

    /**
     * Sets the custom view for the subtitle area within conversation items in the
     * list.
     *
     * @param subtitleView The listener interface that defines callbacks for interactions
     *                     with the subtitle view.
     *                     <p>
     *                     This method allows you to specify a custom view to be displayed
     *                     below the main title or name of each conversation item in the
     *                     list. The provided `ConversationsViewHolderListener` interface
     *                     defines callbacks that will be invoked when various interactions
     *                     occur with the subtitle view, similar to the subtitle views in
     *                     user and group lists.
     *                     <p>
     *                     By implementing the `ConversationsViewHolderListener` interface
     *                     and passing an instance to this method, you can customize the
     *                     appearance and behavior of the subtitle area within each
     *                     conversation item according to your specific needs.
     */
    public void setSubtitleView(ConversationsViewHolderListener subtitleView) {
        conversationsAdapter.setSubtitleView(subtitleView);
    }

    public void setTitleView(ConversationsViewHolderListener titleView) {
        conversationsAdapter.setTitleView(titleView);
    }

    public void setLeadingView(ConversationsViewHolderListener leadingView) {
        conversationsAdapter.setLeadingView(leadingView);
    }

    /**
     * Sets the custom view for the tail element at the end of the conversation
     * list.
     *
     * @param tailView The listener interface that defines callbacks for interactions
     *                 with the tail view.
     *                 <p>
     *                 This method allows you to specify a custom view to be displayed at
     *                 the end of the conversation list item.
     *                 <p>
     *                 The provided `ConversationsViewHolderListener` interface defines
     *                 callbacks that will be invoked when various interactions occur
     *                 with the tail view, allowing you to customize its behavior based
     *                 on user actions.
     */
    public void setTrailingView(ConversationsViewHolderListener tailView) {
        conversationsAdapter.setTrailingView(tailView);
    }

    /**
     * Sets the custom view for each conversation item in the list.
     *
     * @param viewHolderListener The listener interface that defines callbacks for interactions
     *                           with the conversation list item view.
     *                           <p>
     *                           This method allows you to specify a custom view to be used for
     *                           each item in the conversation list. The provided
     *                           `ConversationsViewHolderListener` interface defines callbacks that
     *                           will be invoked when various interactions occur with the list item
     *                           view, such as:
     *                           <p>
     *                           * Clicking on the conversation item * Long pressing on the
     *                           conversation item * Triggering other actions specific to the
     *                           conversation item view
     *                           <p>
     *                           By implementing the `ConversationsViewHolderListener` interface
     *                           and passing an instance to this method, you can customize the
     *                           appearance and behavior of each conversation item in the list
     *                           according to your specific needs.
     */
    public void setItemView(ConversationsViewHolderListener viewHolderListener) {
        conversationsAdapter.setItemView(viewHolderListener);
    }

    /**
     * Sets the listener for conversation item selection events.
     *
     * @param onSelection The listener to handle conversation item selection events.
     */
    public void setOnSelect(OnSelection<Conversation> onSelection) {
        this.onSelection = onSelection;
    }

    /**
     * Retrieves the selected conversations from the view.
     *
     * @return The list of selected Conversation objects.
     */
    public List<Conversation> getSelectedConversations() {
        return new ArrayList<>(hashMap.keySet());
    }

    public View getOverflowMenu() {
        return overflowMenu;
    }

    public void setOverflowMenu(View view) {
        this.overflowMenu = view;
        if (view != null) {
            Utils.handleView(binding.overflowMenuLayout, view, true);
        }
    }

    /**
     * add options inside the popup menu
     *
     * @param options the options to be added inside the popup menu
     */
    public void addOptions(Function2<Context, Conversation, List<CometChatPopupMenu.MenuItem>> options) {
        addOptions = options;
    }

    /**
     * Retrieves the visibility status of the selected conversations recyclerview.
     *
     * @return An integer representing the visibility of the selected conversations recyclerview.
     */
    public int getSelectedConversationsListVisibility(){
        return selectedConversationsListVisibility;
    }

    /**
     * Sets the visibility of the selected conversations recyclerview.
     * If the visibility is not {@code View.VISIBLE}, the selected conversations recyclerview is hidden.
     *
     * @param visibility An integer representing the visibility status of the selected conversations recyclerview.
     *                   Accepts values such as {@code View.VISIBLE}, {@code View.INVISIBLE},
     *                   or {@code View.GONE}.
     */
    public void setSelectedConversationsListVisibility(int visibility){
        this.selectedConversationsListVisibility = visibility;
        binding.rvSelectedConversation.setVisibility(visibility);
    }

    /**
     * Gets the selected conversation avatar style resource.
     *
     * @return the selected conversation avatar style resource.
     */
    public @StyleRes int getSelectedConversationAvatarStyle() {
        return selectedConversationAvatarStyle;
    }

    /**
     * Sets the avatar style resource for the selected conversation list.
     *
     * @param avatarStyle the avatar style resource to set.
     */
    public void setSelectedConversationAvatarStyle(@StyleRes int avatarStyle) {
        this.selectedConversationAvatarStyle = avatarStyle;
        selectedConversationsAdapter.setAvatarStyle(avatarStyle);
    }

    /**
     * Gets the selected conversation item text color.
     *
     * @return the selected conversation text color.
     */
    public @ColorInt int getSelectedConversationItemTextColor() {
        return selectedConversationItemTextColor;
    }

    /**
     * Sets the selected conversation title text color.
     *
     * @param conversationItemTextColor the conversation title text color to set.
     */
    public void setSelectedConversationItemTextColor(@ColorInt int conversationItemTextColor) {
        this.selectedConversationItemTextColor = conversationItemTextColor;
        selectedConversationsAdapter.setItemTitleTextColor(conversationItemTextColor);
    }

    /**
     * Gets the text appearance for the selected conversation item.
     *
     * @return the text appearance for the selected conversation item.
     */
    public @StyleRes int getSelectedConversationItemTextAppearance() {
        return selectedConversationItemTextAppearance;
    }

    /**
     * Sets the text appearance for the selected conversation item.
     *
     * @param conversationItemTextAppearance the text appearance for the selected conversation item.
     */
    public void setSelectedConversationItemTextAppearance(@StyleRes int conversationItemTextAppearance) {
        this.selectedConversationItemTextAppearance = conversationItemTextAppearance;
        selectedConversationsAdapter.setItemTitleTextAppearance(conversationItemTextAppearance);
    }

    /**
     * Returns the selected conversations list remove item icon drawable.
     *
     * @return the selected conversations list remove item
     */
    public Drawable getSelectedConversationItemRemoveIcon() {
        return selectedConversationItemRemoveIcon;
    }

    /**
     * Sets the selected conversation list remove item icon drawable.
     *
     * @param removeItemIcon the drawable to set as the selected conversation list remove item icon
     */
    public void setSelectedConversationItemRemoveIcon(Drawable removeItemIcon) {
        this.selectedConversationItemRemoveIcon = removeItemIcon;
        selectedConversationsAdapter.setRemoveButtonIcon(removeItemIcon);
    }

    /**
     * Returns the tint color for the selected conversation list remove icon.
     *
     * @return the selected conversation list remove item icon tint color
     */
    public @ColorInt int getSelectedConversationItemRemoveIconTint() {
        return selectedConversationItemRemoveIconTint;
    }

    /**
     * Sets the tint color for the selected conversation list remove icon.
     *
     * @param conversationItemRemoveIconTint the tint color to set selected conversation list remove icon tint color
     */
    public void setSelectedConversationItemRemoveIconTint(@ColorInt int conversationItemRemoveIconTint) {
        this.selectedConversationItemRemoveIconTint = conversationItemRemoveIconTint;
        selectedConversationsAdapter.setRemoveButtonIconTint(conversationItemRemoveIconTint);
    }

    public Function2<Context, Conversation, List<CometChatPopupMenu.MenuItem>> getOptions() {
        return options;
    }

    /**
     * replace options inside the popup menu
     *
     * @param options the options to be added inside the popup menu
     */
    public void setOptions(Function2<Context, Conversation, List<CometChatPopupMenu.MenuItem>> options) {
        this.options = options;
    }

    /**
     * Returns the RecyclerView used in the conversations view.
     *
     * @return The RecyclerView instance.
     */
    public RecyclerView getRecyclerView() {
        return binding.recyclerviewConversationsList;
    }

    /**
     * Returns the ViewModel associated with the conversations view.
     *
     * @return The ConversationsViewModel instance.
     */
    public ConversationsViewModel getViewModel() {
        return conversationsViewModel;
    }

    /**
     * Returns the ConversationsAdapter used in the conversations view.
     *
     * @return The ConversationsAdapter instance.
     */
    public ConversationsAdapter getConversationsAdapter() {
        return conversationsAdapter;
    }

    /**
     * Sets the adapter for the conversations view.
     *
     * @param adapter The RecyclerView.Adapter to be set.
     */
    public void setAdapter(ConversationsAdapter adapter) {
        if (adapter != null) {
            conversationsAdapter = adapter;
            binding.recyclerviewConversationsList.setAdapter(adapter);
        }
    }

    /**
     * Scrolls to the top of the conversation list if the first visible item
     * position is less than 5.
     */
    private void scrollToTop() {
        if (((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition() < 5) layoutManager.scrollToPosition(0);
    }

    /**
     * Handles the loading state by displaying a loading view or the shimmer effect.
     */
    private void handleLoadingState() {
        if (loadingStateVisibility == VISIBLE) {
            if (customLoadingView != null) {
                Utils.handleView(binding.customLayout, customLoadingView, true);
            } else {
                setShimmerVisibility(View.VISIBLE);
            }
        } else {
            setShimmerVisibility(View.GONE);
        }
    }

    /**
     * Sets the visibility of the shimmer effect, which is used to display a loading
     * state.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    private void setShimmerVisibility(int visibility) {
        if (visibility == View.GONE) {
            binding.shimmerEffectFrame.stopShimmer();
        } else {
            CometChatShimmerAdapter adapter = new CometChatShimmerAdapter(30, R.layout.shimmer_list_base);
            binding.shimmerRecyclerview.setAdapter(adapter);
            binding.shimmerEffectFrame.setShimmer(CometChatShimmerUtils.getCometChatShimmerConfig(getContext()));
            binding.shimmerEffectFrame.startShimmer();
        }
        binding.shimmerParentLayout.setVisibility(visibility);
    }

    /**
     * Handles the error state by displaying a custom error view or default error
     * message if available.
     */
    private void handleErrorState() {
        if (errorStateVisibility == VISIBLE) {
            if (customErrorView != null) {
                Utils.handleView(binding.errorStateView, customErrorView, true);
            } else {
                setErrorStateVisibility(View.VISIBLE);
            }
        } else {
            setErrorStateVisibility(View.GONE);
        }
    }

    /**
     * Handles the empty state by displaying a custom empty view or default empty
     * message if available.
     */
    private void handleEmptyState() {
        if (onEmpty != null) {
            onEmpty.onEmpty();
        }
        if (emptyStateVisibility == VISIBLE) {
            if (customEmptyView != null) {
                Utils.handleView(binding.emptyStateView, customEmptyView, true);
            } else {
                setEmptyStateVisibility(View.VISIBLE);
            }
        } else {
            setEmptyStateVisibility(View.GONE);
        }
    }

    /**
     * Sets the visibility of the empty state view.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    public void setEmptyStateVisibility(int visibility) {
        this.emptyStateVisibility = visibility;
        binding.emptyStateView.setVisibility(visibility);
    }

    public Drawable getDiscardSelectionIcon() {
        return discardSelectionIcon;
    }

    public void setDiscardSelectionIcon(Drawable discardSelectionIcon) {
        this.discardSelectionIcon = discardSelectionIcon;
        binding.ivDiscardSelection.setImageDrawable(discardSelectionIcon);
    }

    public int getDiscardSelectionIconTint() {
        return discardSelectionIconTint;
    }

    public void setDiscardSelectionIconTint(@ColorInt int discardSelectionIconTint) {
        this.discardSelectionIconTint = discardSelectionIconTint;
        binding.ivDiscardSelection.setImageTintList(ColorStateList.valueOf(discardSelectionIconTint));
    }

    public Drawable getSubmitSelectionIcon() {
        return submitSelectionIcon;
    }

    public void setSubmitSelectionIcon(Drawable submitSelectionIcon) {
        this.submitSelectionIcon = submitSelectionIcon;
        binding.ivSubmitSelection.setImageDrawable(submitSelectionIcon);
    }

    public int getSubmitSelectionIconTint() {
        return submitSelectionIconTint;
    }

    public void setSubmitSelectionIconTint(@ColorInt int submitSelectionIconTint) {
        this.submitSelectionIconTint = submitSelectionIconTint;
        binding.ivSubmitSelection.setImageTintList(ColorStateList.valueOf(submitSelectionIconTint));
    }

    public int getCheckBoxStrokeWidth() {
        return checkBoxStrokeWidth;
    }

    public void setCheckBoxStrokeWidth(@Dimension int checkBoxStrokeWidth) {
        this.checkBoxStrokeWidth = checkBoxStrokeWidth;
        conversationsAdapter.setCheckBoxStrokeWidth(checkBoxStrokeWidth);
    }

    public int getCheckBoxCornerRadius() {
        return checkBoxCornerRadius;
    }

    public void setCheckBoxCornerRadius(@Dimension int checkBoxCornerRadius) {
        this.checkBoxCornerRadius = checkBoxCornerRadius;
        conversationsAdapter.setCheckBoxCornerRadius(checkBoxCornerRadius);
    }

    public int getCheckBoxStrokeColor() {
        return checkBoxStrokeColor;
    }

    public void setCheckBoxStrokeColor(@ColorInt int checkBoxStrokeColor) {
        this.checkBoxStrokeColor = checkBoxStrokeColor;
        conversationsAdapter.setCheckBoxStrokeColor(checkBoxStrokeColor);
    }    /**
     * Gets the stroke width for the conversations card.
     *
     * @return the stroke width used for the conversations card.
     */
    public @Dimension int getStrokeWidth() {
        return strokeWidth;
    }

    public int getCheckBoxBackgroundColor() {
        return checkBoxBackgroundColor;
    }

    public void setCheckBoxBackgroundColor(@ColorInt int checkBoxBackgroundColor) {
        this.checkBoxBackgroundColor = checkBoxBackgroundColor;
        conversationsAdapter.setCheckBoxBackgroundColor(checkBoxBackgroundColor);
    }

    public int getCheckBoxCheckedBackgroundColor() {
        return checkBoxCheckedBackgroundColor;
    }

    public void setCheckBoxCheckedBackgroundColor(@ColorInt int checkBoxCheckedBackgroundColor) {
        this.checkBoxCheckedBackgroundColor = checkBoxCheckedBackgroundColor;
        conversationsAdapter.setCheckBoxCheckedBackgroundColor(checkBoxCheckedBackgroundColor);
    }

    public Drawable getCheckBoxSelectIcon() {
        return checkBoxSelectIcon;
    }

    public void setCheckBoxSelectIcon(Drawable checkBoxSelectIcon) {
        this.checkBoxSelectIcon = checkBoxSelectIcon;
        conversationsAdapter.setCheckBoxSelectIcon(checkBoxSelectIcon);
    }

    public int getCheckBoxSelectIconTint() {
        return checkBoxSelectIconTint;
    }

    public void setCheckBoxSelectIconTint(@ColorInt int checkBoxSelectIconTint) {
        this.checkBoxSelectIconTint = checkBoxSelectIconTint;
        conversationsAdapter.setCheckBoxSelectIconTint(checkBoxSelectIconTint);
    }

    public int getItemSelectedBackgroundColor() {
        return itemSelectedBackgroundColor;
    }

    public void setItemSelectedBackgroundColor(@ColorInt int itemSelectedBackgroundColor) {
        this.itemSelectedBackgroundColor = itemSelectedBackgroundColor;
        conversationsAdapter.setItemSelectedBackgroundColor(itemSelectedBackgroundColor);
    }

    public int getItemBackgroundColor() {
        return itemBackgroundColor;
    }

    public void setItemBackgroundColor(@ColorInt int itemBackgroundColor) {
        this.itemBackgroundColor = itemBackgroundColor;
        conversationsAdapter.setItemBackgroundColor(itemBackgroundColor);
    }

    /**
     * Hides all possible UI states like recycler view, empty state, error state,
     * shimmer, and custom loader.
     */
    private void hideAllStates() {
        setRecyclerViewVisibility(isConversationListEmpty ? View.GONE : View.VISIBLE);
        binding.emptyStateView.setVisibility(View.GONE);
        binding.errorStateView.setVisibility(View.GONE);
        setShimmerVisibility(View.GONE);
        setCustomLoaderVisibility(View.GONE);
    }

    /**
     * Sets the visibility of the recycler view.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    private void setRecyclerViewVisibility(int visibility) {
        binding.recyclerviewConversationsList.setVisibility(visibility);
    }

    /**
     * Sets the visibility of the custom loader view.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    private void setCustomLoaderVisibility(int visibility) {
        binding.customLayout.setVisibility(visibility);
    }

    /**
     * get back press listener
     *
     * @return onBackPress
     */
    public OnBackPress getOnBackPressListener() {
        return onBackPress;
    }

    /**
     * call back when back pressed on toolbar
     *
     * @param onBackPress
     */
    public void setOnBackPressListener(OnBackPress onBackPress) {
        this.onBackPress = onBackPress;
    }

    public void setOnSearchClickListener(OnSearchClick onSearchClick) {
        this.onSearchClick = onSearchClick;
    }

    /**
     * Sets the ConversationsRequestBuilder for fetching conversations.
     *
     * @param conversationsRequestBuilder The ConversationsRequestBuilder instance.
     */
    public void setConversationsRequestBuilder(ConversationsRequest.ConversationsRequestBuilder conversationsRequestBuilder) {
        conversationsViewModel.setConversationsRequestBuilder(conversationsRequestBuilder);
    }

    /**
     * Gets the current OnItemClick listener.
     *
     * @return the OnItemClick listener instance.
     */
    public OnItemClick<Conversation> getOnItemClick() {
        return onItemClick;
    }

    /**
     * Sets the OnItemClick listener.
     *
     * @param onItemClick the OnItemClick listener to set.
     */
    public void setOnItemClick(OnItemClick<Conversation> onItemClick) {
        this.onItemClick = onItemClick;
    }

    /**
     * Gets the current OnItemLongClick listener.
     *
     * @return the OnItemLongClick listener instance.
     */
    public OnItemLongClick<Conversation> getOnItemLongClick() {
        return onItemLongClick;
    }

    /**
     * Sets the OnItemLongClick listener.
     *
     * @param onItemLongClick the OnItemLongClick listener to set.
     */
    public void setOnItemLongClick(OnItemLongClick<Conversation> onItemLongClick) {
        this.onItemLongClick = onItemLongClick;
    }

    public CometchatConversationsListViewBinding getBinding() {
        return binding;
    }

    /**
     * boolean to know if hide user status is enabled or not
     *
     * @return boolean
     */
    public boolean isDisableSoundForMessages() {
        return disableSoundForMessages;
    }

    /**
     * boolean to know if hide user status is enabled or not
     *
     * @return int
     */
    public int getCustomSoundForMessage() {
        return customSoundForMessage;
    }

    public void setLoadingStateVisibility(int loadingStateVisibility) {
        this.loadingStateVisibility = loadingStateVisibility;
        setShimmerVisibility(loadingStateVisibility);
    }

    /**
     * Gets the visibility of the toolbar in the conversations view.
     *
     * @return the visibility of the toolbar.
     */
    public int getToolbarVisibility() {
        return toolbarVisibility;
    }

    /**
     * Sets the visibility of the toolbar in the conversations view.
     *
     * @param toolbarVisibility GONE to hide group type indicators, VISIBLE to enable them.
     */
    public void setToolbarVisibility(int toolbarVisibility) {
        this.toolbarVisibility = toolbarVisibility;
        binding.toolbar.setVisibility(toolbarVisibility);
    }

    /**
     * Gets the visibility of the delete conversation option in the conversations view.
     *
     * @return the visibility of the delete conversation option.
     */
    public int getDeleteConversationOptionVisibility() {
        return deleteConversationOptionVisibility;
    }

    /**
     * Sets the visibility of the delete conversation option in the conversations view.
     *
     * @param deleteConversationOptionVisibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    public void setDeleteConversationOptionVisibility(int deleteConversationOptionVisibility) {
        this.deleteConversationOptionVisibility = deleteConversationOptionVisibility;
    }    /**
     * Sets the stroke width for the conversations card.
     *
     * @param strokeWidth the width to use for the card's stroke.
     */
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    /**
     * Gets the visibility of the back icon in the conversations view.
     *
     * @return the visibility of the back icon.
     */
    public int getBackIconVisibility() {
        return backIconVisibility;
    }

    /**
     * Sets the visibility of the back icon in the conversations view.
     *
     * @param backIconVisibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    public void setBackIconVisibility(int backIconVisibility) {
        this.backIconVisibility = backIconVisibility;
        binding.ivBack.setVisibility(backIconVisibility);
    }

    /**
     * Gets the visibility of the user status in the conversations view.
     *
     * @return the visibility of the user status.
     */
    public int getUserStatusVisibility() {
        return userStatusVisibility;
    }

    /**
     * Sets the visibility of the user status in the conversations view.
     *
     * @param userStatusVisibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    public void setUserStatusVisibility(int userStatusVisibility) {
        this.userStatusVisibility = userStatusVisibility;
        conversationsAdapter.hideUserStatus(userStatusVisibility != VISIBLE);
    }

    /**
     * Gets the visibility of the group type in the conversations view.
     *
     * @return the visibility of the group type.
     */
    public int getGroupTypeVisibility() {
        return groupTypeVisibility;
    }

    /**
     * Sets the visibility of the group type in the conversations view.
     *
     * @param groupTypeVisibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    public void setGroupTypeVisibility(int groupTypeVisibility) {
        this.groupTypeVisibility = groupTypeVisibility;
        conversationsAdapter.hideGroupType(receiptsVisibility != VISIBLE);
    }

    /**
     * Gets the visibility of the read receipt in the conversations view.
     *
     * @return the visibility of the read receipt.
     */
    public int getReceiptsVisibility() {
        return receiptsVisibility;
    }

    /**
     * Sets the visibility of the read receipt in the conversations view.
     *
     * @param receiptsVisibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    public void setReceiptsVisibility(int receiptsVisibility) {
        this.receiptsVisibility = receiptsVisibility;
        conversationsAdapter.hideReceipts(receiptsVisibility != VISIBLE);
    }

    /**
     * Gets the visibility of the empty state view.
     *
     * @return the visibility of the empty state view.
     */
    public int getErrorStateVisibility() {
        return errorStateVisibility;
    }

    /**
     * Sets the visibility of the error state view.
     *
     * @param visibility Visibility constant (View.VISIBLE, View.GONE, etc.).
     */
    public void setErrorStateVisibility(int visibility) {
        this.errorStateVisibility = visibility;
        binding.errorStateView.setVisibility(View.VISIBLE);
    }

    /**
     * Sets a custom label for the "mention all" feature for a specific ID.
     *
     * @param id The unique identifier (such as a group or user ID) for which the mention all label should be set.
     * @param mentionAllLabel The custom label to display when mentioning all members.
     *
     * If either parameter is null or empty, or if the mentions formatter is not initialized, this method does nothing.
     */
    public void setMentionAllLabelId(String id, String mentionAllLabel) {
        if (id != null && !id.isEmpty() && mentionAllLabel != null && !mentionAllLabel.isEmpty()) {
            if (cometchatMentionsFormatter != null) {
                cometchatMentionsFormatter.setMentionAllLabel(id, mentionAllLabel);
            }
            this.mentionAllLabelId = id;
            this.mentionAllLabel = mentionAllLabel;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isDetachedFromWindow) {
            attachObservers();
            isDetachedFromWindow = false;
        }
        processFormatters();
        conversationsViewModel.addListener();
        conversationsViewModel.fetchConversation();
    }

    @Override
    protected void onDetachedFromWindow() {
        isDetachedFromWindow = true;
        conversationsViewModel.removeListener();
        disposeObservers();
        super.onDetachedFromWindow();
    }

    public void disposeObservers() {
        if (lifecycleOwner != null) {
            conversationsViewModel.getMutableConversationList().removeObservers(lifecycleOwner);
            conversationsViewModel.getStates().removeObservers(lifecycleOwner);
            conversationsViewModel.insertAtTop().removeObservers(lifecycleOwner);
            conversationsViewModel.moveToTop().removeObservers(lifecycleOwner);
            conversationsViewModel.getTyping().removeObservers(lifecycleOwner);
            conversationsViewModel.updateConversation().removeObservers(lifecycleOwner);
            conversationsViewModel.playSound().removeObservers(lifecycleOwner);
            conversationsViewModel.remove().removeObservers(lifecycleOwner);
            conversationsViewModel.progressState().removeObservers(lifecycleOwner);
            conversationsViewModel.getCometChatException().removeObservers(lifecycleOwner);
        }
    }
}
