package com.cometchat.chatuikit.shared.views.searchbox;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.databinding.CometchatSearchBoxBinding;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CometChatSearchBox extends MaterialCardView {
    private static final String TAG = CometChatSearchBox.class.getSimpleName();
    private CometchatSearchBoxBinding binding;
    private @StyleRes int searchInputTextAppearance;
    private @ColorInt int searchInputTextColor;
    private @StyleRes int searchInputPlaceHolderTextAppearance;
    private @ColorInt int searchInputPlaceHolderTextColor;
    private Drawable searchInputStartIcon;
    private Drawable searchInputEndIcon;
    private @ColorInt int searchInputStartIconTint;
    private @ColorInt int searchInputEndIconTint;
    private OnSearch eventListener;
    private OnClick onSearchClick;

    public CometChatSearchBox(@NonNull Context context) {
        this(context, null);
    }

    public CometChatSearchBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CometChatSearchBox(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView();
        }
    }

    private void inflateAndInitializeView() {
        // Inflate the layout for this view
        binding = CometchatSearchBoxBinding.inflate(LayoutInflater.from(getContext()), this, true);

        // Reset the card view to default values
        Utils.initMaterialCard(this);

        binding.etSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                if (!textView.getText().toString().isEmpty()) {
                    if (eventListener != null) {
                        eventListener.onSearch(SearchState.Filter, textView.getText().toString());
                    }
                }
                return true;
            }
            return false;
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (eventListener != null) {
                    if (s.toString().isEmpty()) {
                        setSearchInputEndIconVisibility(GONE);
                    } else {
                        setSearchInputEndIconVisibility(VISIBLE);
                    }
                    eventListener.onSearch(SearchState.TextChange, s.toString());
                }
            }
        });

        binding.ivClear.setOnClickListener(view -> {
            binding.etSearch.setText("");
            if (eventListener != null) {
                eventListener.onSearch(SearchState.Clear, "");
            }
        });

        binding.searchBoxParent.setOnClickListener( view -> {
            if (onSearchClick != null) {
                onSearchClick.onClick();
            }
        });

        binding.etSearch.setOnClickListener( view -> {
            if (onSearchClick != null) {
                onSearchClick.onClick();
            }
        });
    }

    public void setOnSearchClick(OnClick onSearchClick) {
        this.onSearchClick = onSearchClick;
    }

    public void setSearchInputEndIconVisibility(int visibility) {
        binding.ivClear.setVisibility(visibility);
    }

    public void setSearchInputText(String text) {
        binding.etSearch.setText(text);
    }

    public @StyleRes int getSearchInputTextAppearance() {
        return searchInputTextAppearance;
    }

    public void setSearchInputTextAppearance(@StyleRes int searchInputTextAppearance) {
        this.searchInputTextAppearance = searchInputTextAppearance;
        binding.etSearch.setTextAppearance(searchInputTextAppearance);
    }

    public @ColorInt int getSearchInputTextColor() {
        return searchInputTextColor;
    }

    public void setSearchInputTextColor(@ColorInt int searchInputTextColor) {
        this.searchInputTextColor = searchInputTextColor;
        binding.etSearch.setTextColor(searchInputTextColor);
    }

    public @StyleRes int getSearchInputPlaceHolderTextAppearance() {
        return searchInputPlaceHolderTextAppearance;
    }

    public void setSearchInputPlaceHolderTextAppearance(@StyleRes int searchInputPlaceHolderTextAppearance) {
        this.searchInputPlaceHolderTextAppearance = searchInputPlaceHolderTextAppearance;
    }

    public @ColorInt int getSearchInputPlaceHolderTextColor() {
        return searchInputPlaceHolderTextColor;
    }

    public void setSearchInputPlaceHolderTextColor(@ColorInt int searchInputPlaceHolderTextColor) {
        this.searchInputPlaceHolderTextColor = searchInputPlaceHolderTextColor;
        binding.etSearch.setHintTextColor(searchInputPlaceHolderTextColor);
    }

    public Drawable getSearchInputStartIcon() {
        return searchInputStartIcon;
    }

    public void setSearchInputStartIcon(Drawable searchInputStartIcon) {
        this.searchInputStartIcon = searchInputStartIcon;
        binding.ivSearch.setBackground(searchInputStartIcon);
    }

    public @ColorInt int getSearchInputStartIconTint() {
        return searchInputStartIconTint;
    }

    public void setSearchInputStartIconTint(@ColorInt int searchInputStartIconTint) {
        this.searchInputStartIconTint = searchInputStartIconTint;
        binding.ivSearch.setBackgroundTintList(ColorStateList.valueOf(searchInputStartIconTint));
    }

    public Drawable getSearchInputEndIcon() {
        return searchInputEndIcon;
    }

    public void setSearchInputEndIcon(Drawable searchInputEndIcon) {
        this.searchInputEndIcon = searchInputEndIcon;
        binding.ivClear.setBackground(searchInputEndIcon);
    }

    public @ColorInt int getSearchInputEndIconTint() {
        return searchInputEndIconTint;
    }

    public void setSearchInputEndIconTint(@ColorInt int searchInputEndIconTint) {
        this.searchInputEndIconTint = searchInputEndIconTint;
        binding.ivClear.setBackgroundTintList(ColorStateList.valueOf(searchInputEndIconTint));
    }

    /**
     * Sets the placeholder text for the search input field.
     *
     * @param placeholder The text to be set as the placeholder.
     */
    public void setSearchPlaceholderText(String placeholder) {
        if (placeholder != null) {
            binding.etSearch.setHint(placeholder);
        }
    }

    /**
     * Sets an event listener for search events.
     *
     * @param eventListener The event listener to be set.
     */
    public void addOnSearchListener(OnSearch eventListener) {
        if (eventListener != null) {
            this.eventListener = eventListener;
        }
    }

    public CometchatSearchBoxBinding getBinding() {
        return binding;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SearchState.Filter, SearchState.Clear, SearchState.TextChange})
    public @interface SearchState {
        String Filter = "filter";
        String Clear = "clear";
        String TextChange = "textchange";
    }

    public interface OnSearch {
        void onSearch(@SearchState String state, String text);
    }
}
