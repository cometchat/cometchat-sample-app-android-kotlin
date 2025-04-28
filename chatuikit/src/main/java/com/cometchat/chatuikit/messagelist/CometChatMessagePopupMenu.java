package com.cometchat.chatuikit.messagelist;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatQuickReactionViewBinding;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.interfaces.EmojiPickerClickListener;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.interfaces.ReactionClickListener;
import com.cometchat.chatuikit.shared.models.CometChatMessageTemplate;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.threadheader.CometChatThreadHeader;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CometChatMessagePopupMenu {
    private static final String TAG = CometChatMessagePopupMenu.class.getSimpleName();
    private final Context context;
    private final List<MenuItem> menuItems;
    public ImageView blurIv;
    private PopupWindow popupWindow;
    private OnMenuItemClickListener onMenuItemClickListener;
    private @Dimension int elevation;
    private @Dimension int cornerRadius;
    private @ColorInt int backgroundColor;
    private @ColorInt int textColor;
    private @StyleRes int textAppearance;
    private @ColorInt int strokeColor;
    private @Dimension int strokeWidth;
    private @ColorInt int startIconTint;
    private @ColorInt int endIconTint;
    private List<CometChatMessageTemplate> messageTemplates;
    private List<CometChatTextFormatter> textFormatters;
    private EmojiPickerClickListener emojiPickerClickListener;
    private ReactionClickListener reactionClickListener;
    private List<String> quickReactions;
    private int quickReactionsVisibility = View.VISIBLE;
    private UIKitConstants.MessageListAlignment messageAlignment = UIKitConstants.MessageListAlignment.STANDARD;
    private @StyleRes int style;
    private @DrawableRes int addReactionIcon;

    public CometChatMessagePopupMenu(Context context, @StyleRes int style) {
        this.context = context;
        this.menuItems = new ArrayList<>();
        quickReactions = Arrays.asList(Utils.getDefaultReactionsList());
        setStyle(style);
    }

    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            applyStyleAttributes(context, style);
        }
    }

    /**
     * Applies the style attributes from XML, allowing direct attribute overrides.
     */
    private void applyStyleAttributes(Context context, @StyleRes int style) {
        TypedArray directAttributes = context.getTheme().obtainStyledAttributes(style, R.styleable.CometChatPopupMenu);
        extractAttributesAndApplyDefaults(directAttributes);
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
            elevation = typedArray.getDimensionPixelSize(R.styleable.CometChatPopupMenu_cometchatPopupMenuElevation, 0);
            cornerRadius = typedArray.getDimensionPixelSize(R.styleable.CometChatPopupMenu_cometchatPopupMenuCornerRadius, 0);
            backgroundColor = typedArray.getColor(R.styleable.CometChatPopupMenu_cometchatPopupMenuBackgroundColor, 0);
            textColor = typedArray.getColor(R.styleable.CometChatPopupMenu_cometchatPopupMenuItemTextColor, 0);
            textAppearance = typedArray.getResourceId(R.styleable.CometChatPopupMenu_cometchatPopupMenuItemTextAppearance, 0);
            strokeColor = typedArray.getColor(R.styleable.CometChatPopupMenu_cometchatPopupMenuStrokeColor, 0);
            strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatPopupMenu_cometchatPopupMenuStrokeWidth, 0);
            startIconTint = typedArray.getColor(R.styleable.CometChatPopupMenu_cometchatPopupMenuItemStartIconTint, 0);
            endIconTint = typedArray.getColor(R.styleable.CometChatPopupMenu_cometchatPopupMenuItemEndIconTint, 0);
        } finally {
            typedArray.recycle();
        }
    }

    public void setAddReactionIcon(@DrawableRes int addReactionIcon) {
        this.addReactionIcon = addReactionIcon;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        // Set the listener
        this.onMenuItemClickListener = listener;
    }

    public void setMessageAlignment(UIKitConstants.MessageListAlignment messageAlignment) {
        this.messageAlignment = messageAlignment;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems.clear();
        this.menuItems.addAll(menuItems);
    }

    public void setMessageTemplates(List<CometChatMessageTemplate> messageTemplates) {
        this.messageTemplates = messageTemplates;
    }

    public void setTextFormatters(List<CometChatTextFormatter> textFormatters) {
        this.textFormatters = textFormatters;
    }

    public void setQuickReactions(List<String> quickReactions) {
        if (quickReactions != null) this.quickReactions = quickReactions;
    }

    public void setReactionClickListener(ReactionClickListener reactionClickListener) {
        if (reactionClickListener != null) this.reactionClickListener = reactionClickListener;
    }

    public void setEmojiPickerClickListener(EmojiPickerClickListener emojiPickerClickListener) {
        if (emojiPickerClickListener != null) this.emojiPickerClickListener = emojiPickerClickListener;
    }

    public void setQuickReactionsVisibility(int visibility) {
        this.quickReactionsVisibility = visibility;
    }

    // Method to show the popup menu
    public void show(View anchorView, View parentView, BaseMessage baseMessage) {
        // Inflate the layout for the popup window
        View popupView = View.inflate(context, R.layout.cometchat_message_list_popup, null);

        LinearLayout viewReactions = popupView.findViewById(R.id.view_reactions);
        MaterialCardView reactionCard = popupView.findViewById(R.id.reaction_card);
        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        MaterialCardView cardView = popupView.findViewById(R.id.menu_parent);
        CometChatThreadHeader messagePreview = popupView.findViewById(R.id.message_preview);

        boolean isLeft = !baseMessage
            .getSender()
            .getUid()
            .equals(CometChatUIKit.getLoggedInUser().getUid()) || messageAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED;

        messagePreview.setTemplates(messageTemplates);
        messagePreview.setTextFormatters(textFormatters);
        messagePreview.setAvatarVisibility(View.GONE);
        messagePreview.setParentMessage(baseMessage);
        messagePreview.setLeftBubbleMargin(0, 0, 0, 0);
        messagePreview.setRightBubbleMargin(0, 0, 0, 0);
        messagePreview.setCardBackgroundColor(Color.TRANSPARENT);
        messagePreview.setReplyCountBarVisibility(View.GONE);
        messagePreview.setMaxHeight(Utils.convertDpToPx(context, 350));

        int margin = context.getResources().getDimensionPixelSize(R.dimen.cometchat_margin);
        int cardRadius = context.getResources().getDimensionPixelSize(R.dimen.cometchat_radius_max);
        int reactionTextColor = context.getResources().getColor(R.color.cometchat_color_icon_white, context.getTheme());
        LinearLayout.LayoutParams layoutParams;
        if (quickReactionsVisibility == View.VISIBLE) {
            float startX = isLeft ? -100f : 100f;
            reactionCard.setTranslationX(startX);
            reactionCard.setAlpha(0f);
            reactionCard.setScaleX(0.8f);
            reactionCard.setScaleY(0.8f);

            for (int i = 0; i < quickReactions.size(); i++) {
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.rightMargin = margin;
                layoutParams.leftMargin = (i == 0) ? 0 : margin;
                layoutParams.topMargin = 0;
                layoutParams.bottomMargin = 0;

                CometchatQuickReactionViewBinding reactionChipBinding = CometchatQuickReactionViewBinding.inflate(LayoutInflater.from(context));

                Utils.initMaterialCard(reactionChipBinding.cardReactionChip);
                reactionChipBinding.cardReactionChip.setRadius(cardRadius);

                reactionChipBinding.tvReaction.setText(quickReactions.get(i));
                reactionChipBinding.tvReaction.setTextColor(reactionTextColor);

                viewReactions.addView(reactionChipBinding.getRoot());
                reactionChipBinding.cardReactionChip.setLayoutParams(layoutParams);

                int finalI = i;
                reactionChipBinding.tvReaction.setOnClickListener(view -> reactionClickListener.onReactionClick(baseMessage,
                                                                                                                quickReactions.get(finalI)));

            }

            // Add reaction icon chip with animation
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = margin;
            layoutParams.rightMargin = 0;
            layoutParams.topMargin = 0;
            layoutParams.bottomMargin = 0;

            CometchatQuickReactionViewBinding addReactionChipBinding = CometchatQuickReactionViewBinding.inflate(LayoutInflater.from(context));
            Utils.initMaterialCard(addReactionChipBinding.cardReactionChip);
            addReactionChipBinding.cardReactionChip.setRadius(cardRadius);

            addReactionChipBinding.tvReaction.setBackground(ResourcesCompat.getDrawable(context.getResources(),
                                                                                        addReactionIcon != 0 ? addReactionIcon : R.drawable.cometchat_add_reaction,
                                                                                        context.getTheme()));

            viewReactions.addView(addReactionChipBinding.getRoot());
            addReactionChipBinding.cardReactionChip.setLayoutParams(layoutParams);
            addReactionChipBinding.tvReaction.setOnClickListener(view -> emojiPickerClickListener.onEmojiPickerClick());

            reactionCard
                .animate()
                .translationX(0)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setStartDelay(quickReactions.size() * 40L) // Appears after all emojis
                .start();
        } else {
            reactionCard.setVisibility(View.GONE);
        }


        cardView.setCardElevation(elevation);
        cardView.setRadius(cornerRadius);
        cardView.setCardBackgroundColor(backgroundColor);
        cardView.setStrokeColor(strokeColor);
        cardView.setStrokeWidth(strokeWidth);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        PopupMenuAdapter adapter = new PopupMenuAdapter(context, menuItems, (id, item) -> {
            // Handle item click and call the callback
            for (MenuItem menuItem : menuItems) {
                if (menuItem.getId().equals(id)) {
                    if (menuItem.getOnClick() != null) {
                        menuItem.getOnClick().onClick();
                    }
                }
            }
            if (onMenuItemClickListener != null) {
                onMenuItemClickListener.onMenuItemClick(id, item);
            }
        });

        adapter.setEndIconTint(endIconTint);
        adapter.setStartIconTint(startIconTint);
        adapter.setTextColor(textColor);
        adapter.setTextAppearance(textAppearance);
        recyclerView.setAdapter(adapter);

        // Create the PopupWindow
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);

        popupWindow.setElevation(elevation); // Set the elevation in dp
        popupWindow.setAnimationStyle(R.style.CometChatPopupMenuAnimation); // Set the animation style

        // Get the location of the anchor view on screen
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        // Measure the popup view to get its width and height
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        // Get the horizontal position of the anchor view

        RelativeLayout.LayoutParams relativeParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                                                    RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams reactionParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                                                    RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (isLeft) {
            relativeParam.addRule(RelativeLayout.ALIGN_PARENT_START);
            reactionParam.addRule(RelativeLayout.ALIGN_PARENT_START);
        } else {
            relativeParam.addRule(RelativeLayout.ALIGN_PARENT_END);
            reactionParam.addRule(RelativeLayout.ALIGN_PARENT_END);
        }
        reactionCard.setLayoutParams(reactionParam);
        relativeParam.addRule(RelativeLayout.BELOW, messagePreview.getId());
        relativeParam.topMargin = Utils.convertDpToPx(context, 0);
        cardView.setLayoutParams(relativeParam);
        // Show the popup window at the calculated position
        dimBackground((Activity) context);

        popupWindow.showAtLocation(anchorView,
                                   Gravity.TOP | Gravity.CENTER,
                                   0,
                                   (location[1] - 1000) > 550 ? (location[1] - 1000) : Math.max(Math.max(-(location[1] - 500), location[1] - 500),
                                                                                                300));

        popupWindow.setOnDismissListener(() -> removeDimBackground((Activity) context));
        popupView.setOnTouchListener((v, event) -> {
            dismiss();
            return true;
        });
    }

    private void dimBackground(Activity activity) {
        if (blurIv == null) {
            blurIv = new ImageView(activity);
            Bitmap bitmap = Utils.captureScreen(activity);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                blurIv.setImageBitmap(Utils.applyRenderScriptBlur(activity, bitmap));
                blurIv.setAlpha(1f);
            } else {
                blurIv.setImageBitmap(bitmap);
                blurIv.setRenderEffect(RenderEffect.createBlurEffect(70f, 70f, Shader.TileMode.CLAMP));
            }
            blurIv.setScaleType(ImageView.ScaleType.FIT_XY);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                                                                               WindowManager.LayoutParams.MATCH_PARENT,
                                                                               WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                                                                               WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                                                                               PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.CENTER;
            WindowManager windowManager = activity.getWindowManager();
            windowManager.addView(blurIv, params);
        }
    }

    private void removeDimBackground(Activity activity) {
        if (blurIv != null) {
            WindowManager windowManager = activity.getWindowManager();
            windowManager.removeView(blurIv);
            blurIv = null;
        }
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(String id, String item);
    }

    // Data class for menu items
    public static class MenuItem {
        private final String id;
        private final String name;
        private final Drawable startIcon;
        private final Drawable endIcon;

        private final @ColorInt int startIconTint;
        private final @ColorInt int endIconTint;
        private final @ColorInt int textColor;
        private final @StyleRes int textAppearance;
        private final OnClick onClick;

        public MenuItem(String id, String name, OnClick click) {
            this(id, name, null, null, click);
        }

        public MenuItem(String id, String name, Drawable startIcon, Drawable endIcon, OnClick click) {
            this(id, name, startIcon, endIcon, 0, 0, 0, 0, click);
        }

        public MenuItem(String id,
                        String name,
                        Drawable startIcon,
                        Drawable endIcon,
                        @ColorInt int startIconTint,
                        @ColorInt int endIconTint,
                        @ColorInt int textColor,
                        @StyleRes int textAppearance,
                        OnClick click) {
            this.id = id;
            this.name = name;
            this.startIcon = startIcon;
            this.endIcon = endIcon;
            this.startIconTint = startIconTint;
            this.endIconTint = endIconTint;
            this.textColor = textColor;
            this.textAppearance = textAppearance;
            this.onClick = click;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Drawable getStartIcon() {
            return startIcon;
        }

        public Drawable getEndIcon() {
            return endIcon;
        }

        public @ColorInt int getStartIconTint() {
            return startIconTint;
        }

        public @ColorInt int getEndIconTint() {
            return endIconTint;
        }

        public @ColorInt int getTextColor() {
            return textColor;
        }

        public @StyleRes int getTextAppearance() {
            return textAppearance;
        }

        public OnClick getOnClick() {
            return onClick;
        }
    }
}
