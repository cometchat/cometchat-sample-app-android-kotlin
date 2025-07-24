package com.cometchat.chatuikit.shared.views.popupmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CometChatPopupMenu {
    private static final String TAG = CometChatPopupMenu.class.getSimpleName();
    private final Context context;
    private final List<MenuItem> menuItems;
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

    // Constructor
    public CometChatPopupMenu(Context context, @StyleRes int style) {
        this.context = context;
        this.menuItems = new ArrayList<>();
        applyStyleAttributes(context, null, R.attr.cometchatPopupMenuStyle, style);
    }

    /**
     * Applies the style attributes from XML, allowing direct attribute overrides.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void applyStyleAttributes(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int style) {
        TypedArray directAttributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatPopupMenu, defStyleAttr, style);
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
            backgroundColor = typedArray.getColor(R.styleable.CometChatPopupMenu_cometchatPopupMenuBackgroundColor,
                                                  CometChatTheme.getBackgroundColor1(context));
            textColor = typedArray.getColor(R.styleable.CometChatPopupMenu_cometchatPopupMenuItemTextColor,
                                            CometChatTheme.getTextColorPrimary(context));
            textAppearance = typedArray.getResourceId(R.styleable.CometChatPopupMenu_cometchatPopupMenuItemTextAppearance, 0);
            strokeColor = typedArray.getColor(R.styleable.CometChatPopupMenu_cometchatPopupMenuStrokeColor,
                                              CometChatTheme.getStrokeColorLight(context));
            strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CometChatPopupMenu_cometchatPopupMenuStrokeWidth, 0);
            startIconTint = typedArray.getColor(R.styleable.CometChatPopupMenu_cometchatPopupMenuItemStartIconTint,
                                                CometChatTheme.getIconTintPrimary(context));
            endIconTint = typedArray.getColor(R.styleable.CometChatPopupMenu_cometchatPopupMenuItemEndIconTint,
                                              CometChatTheme.getIconTintPrimary(context));
        } finally {
            typedArray.recycle();
        }
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        // Set the listener
        this.onMenuItemClickListener = listener;
    }

    public void setStyle(@StyleRes int style) {
        // Apply the new style
        if (style != 0) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(style, R.styleable.CometChatPopupMenu);
            extractAttributesAndApplyDefaults(attributes);
        }
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems.clear();
        this.menuItems.addAll(menuItems);
    }

    // Method to show the popup menu
    public void show(View anchorView) {
        // Inflate the layout for the popup window
        View popupView = View.inflate(context, R.layout.cometchat_popup_recycler_view, null);

        // Initialize the RecyclerView
        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        MaterialCardView cardView = popupView.findViewById(R.id.menu_parent);
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
        // Convert the 12dp and 10dp offsets to pixels
        int marginInPixels = (int) (12 * context.getResources().getDisplayMetrics().density + 0.5f);
        int offsetInPixels = (int) (10 * context.getResources().getDisplayMetrics().density + 0.5f);

        // Get the location of the anchor view on screen
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        // Get screen width and height
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Measure the popup view to get its width and height
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight = popupView.getMeasuredHeight();

        // Calculate the horizontal position (right side of the anchor view with margin)
        int xOffset = location[0] + anchorView.getWidth();
        int adjustedXOffset = Math.min(screenWidth - popupWidth - marginInPixels, xOffset);

        // Calculate the vertical position to show the popup slightly above the bottom
        // of the anchor
        // view
        int yOffset = location[1] + anchorView.getHeight() - offsetInPixels;
        int availableSpaceBelow = screenHeight - (location[1] + anchorView.getHeight());
        int availableSpaceAbove = location[1];

        if (popupHeight > availableSpaceBelow) {
            // If there isn't enough space below, check if there's more space above
            if (popupHeight <= availableSpaceAbove) {
                // Show the popup above the anchor view
                yOffset = location[1] - popupHeight;
            } else {
                // Adjust to fit within the screen as best as possible (if neither above nor
                // below
                // fits perfectly)
                yOffset = Math.max(0, screenHeight - popupHeight);
            }
        }

        // Show the popup window at the calculated position
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, adjustedXOffset, yOffset);
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
