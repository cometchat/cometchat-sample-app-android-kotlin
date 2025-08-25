package com.cometchat.chatuikit.shared.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.databinding.CometchatTailBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.views.badge.CometChatBadge;
import com.cometchat.chatuikit.shared.views.date.CometChatDate;
import com.google.android.material.card.MaterialCardView;

/**
 * ConversationTailView is a custom view extending MaterialCardView, designed to
 * display the tail information of a conversation, including a date and a badge
 * for unread counts.
 */
public class ConversationTailView extends MaterialCardView {
    private static final String TAG = ConversationTailView.class.getSimpleName();
    // Binding object to access the views defined in the layout file
    private CometchatTailBinding binding;

    /**
     * Constructor used when creating the view programmatically.
     *
     * @param context The Context the view is running in, allowing access to resources,
     *                themes, etc.
     */
    public ConversationTailView(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating the view from XML.
     *
     * @param context The Context the view is running in.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public ConversationTailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor called when inflating the view from XML with a default style
     * attribute.
     *
     * @param context      The Context the view is running in.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a
     *                     style resource that supplies default values for the view. Can be 0
     *                     to not look for defaults.
     */
    public ConversationTailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initializes the view by setting up the layout and customizing the
     * MaterialCardView properties.
     */
    private void init() {
        Utils.initMaterialCard(this); // Initializes the MaterialCardView properties
        setBackgroundColor(getResources().getColor(android.R.color.transparent)); // Sets the background color to
        // transparent
        setStrokeWidth(0);
        binding = CometchatTailBinding.inflate(LayoutInflater.from(getContext()), this, true); // Inflates the layout
        binding.date.setDateTextAlignment(View.TEXT_ALIGNMENT_VIEW_END); // Aligns the date text to the end
        binding.date.setDateTextColor(CometChatTheme.getTextColorSecondary(getContext()));
    }

    /**
     * Sets the badge count to display unread messages.
     *
     * @param count The number of unread messages.
     */
    public void setBadgeCount(int count) {
        if (count > 0) {
            binding.badge.setCount(count); // Sets the badge count
        } else {
            binding.badge.setVisibility(INVISIBLE); // Hides the badge if the count is zero or less
        }
    }

    /**
     * Gets the CometChatDate view component.
     *
     * @return The CometChatDate component displaying the date.
     */
    public CometChatDate getDate() {
        return binding.date;
    }

    /**
     * Gets the CometChatBadge view component.
     *
     * @return The CometChatBadge component displaying the unread count.
     */
    public CometChatBadge getBadge() {
        return binding.badge;
    }
}
