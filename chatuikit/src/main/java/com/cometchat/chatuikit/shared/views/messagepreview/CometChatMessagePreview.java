package com.cometchat.chatuikit.shared.views.messagepreview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatMessagePreviewBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CometChatMessagePreview extends MaterialCardView {
    private int MIN_WIDTH = 0;
    private int MAX_WIDTH = -1;
    private final CometchatMessagePreviewBinding binding;
    private OnClick onMessagePreviewClick;
    private OnClick onCloseClick;

    public CometChatMessagePreview(Context context) {
        this(context, null);
    }

    public CometChatMessagePreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CometChatMessagePreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Utils.initMaterialCard(this);
        binding = CometchatMessagePreviewBinding.inflate(LayoutInflater.from(context), this, true);
        initClickListeners();
        applyStyleAttributes(attrs, defStyleAttr, 0);
    }

    private void initClickListeners() {
        binding.messagePreviewParent.setOnClickListener(v -> {
            if (onMessagePreviewClick != null) {
                onMessagePreviewClick.onClick();
            }
        });
        binding.ivMessageClose.setOnClickListener(v ->{
            if (onCloseClick != null) {
                onCloseClick.onClick();
            }
        });
    }

    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyle) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMessagePreview, defStyleAttr, defStyle);
        @StyleRes int styleResId = typedArray.getResourceId(R.styleable.CometChatMessagePreview_cometChatMessagePreviewStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMessagePreview, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(typedArray);
    }

    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewBackgroundColor, CometChatTheme.getExtendedPrimaryColor800(getContext())));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewStrokeColor, 0));
            setSeparatorColor(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewSeparatorColor, CometChatTheme.getColorWhite(getContext())));
            setTitleTextColor(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewTitleTextColor, CometChatTheme.getColorWhite(getContext())));
            setSubtitleTextColor(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewSubtitleTextColor, CometChatTheme.getColorWhite(getContext())));
            setCloseIconTint(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewCloseIconTint, CometChatTheme.getIconTintSecondary(getContext())));
            setMessageIconTint(typedArray.getColor(R.styleable.CometChatMessagePreview_cometChatMessagePreviewMessageIconTint, CometChatTheme.getIconTintSecondary(getContext())));

            setStrokeWidth(typedArray.getDimension(R.styleable.CometChatMessagePreview_cometChatMessagePreviewStrokeColor, 0));
            setCornerRadius(typedArray.getDimension(R.styleable.CometChatMessagePreview_cometChatMessagePreviewCornerRadius, 0));
            setTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessagePreview_cometChatMessagePreviewTitleTextAppearance, 0));
            setSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatMessagePreview_cometChatMessagePreviewSubtitleTextAppearance, 0));
            setCloseIcon(typedArray.getDrawable(R.styleable.CometChatMessagePreview_cometChatMessagePreviewCloseIcon));
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minWidthPx = (int) (MIN_WIDTH * getResources().getDisplayMetrics().density);
        int maxWidthPx = MAX_WIDTH == -1 ? -1 : (int) (MAX_WIDTH * getResources().getDisplayMetrics().density);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(MIN_WIDTH == -1 && MAX_WIDTH == -1) {
            return;
        }

        int measuredWidth = getMeasuredWidth();
        int finalWidth = measuredWidth;

        // Apply minimum width constraint
        if (MIN_WIDTH != -1) {
            finalWidth = Math.max(finalWidth, minWidthPx);
        }

        // Apply maximum width constraint
        if (MAX_WIDTH != -1) {
            finalWidth = Math.min(finalWidth, maxWidthPx);
        }

        if (finalWidth != measuredWidth) {
            int newWidthSpec = MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY);
            super.onMeasure(newWidthSpec, heightMeasureSpec);
        }
    }

    public void setMessage(Context context, BaseMessage message, CometChatMessagePreview messagePreview, List<CometChatTextFormatter> textFormatters, UIKitConstants.FormattingType formattingType, UIKitConstants.MessageBubbleAlignment alignment) {
        Utils.setReplyMessagePreview(context, message, messagePreview, textFormatters, formattingType, alignment);
    }

    @Override
    public void setMinimumWidth(int minWidth) {
        this.MIN_WIDTH = minWidth;
        measure(getMeasuredWidth(),getMeasuredHeight());
    }

    public void setMaxWidth(int maxWidth) {
        this.MAX_WIDTH = maxWidth;
        measure(getMeasuredWidth(), getMeasuredHeight());
    }

    public void setOnMessagePreviewClickListener(OnClick listener) {
        this.onMessagePreviewClick = listener;
    }

    public void setOnCloseClickListener(OnClick listener) {
        onCloseClick = listener;
    }

    public void setMessagePreviewTitleText(String text) {
        binding.tvMessageLayoutTitle.setText(text);
    }

    public void setMessagePreviewSubtitleText(String text) {
        binding.tvMessageLayoutSubtitle.setText(text);
    }

    public void setMessagePreviewSubtitleText(CharSequence text) {
        binding.tvMessageLayoutSubtitle.setText(text);
    }

    public void setMessageIconVisibility(int visibility) {
        binding.messageIcon.setVisibility(visibility);
    }

    public void setCloseIconVisibility(int gone) {
        binding.ivMessageClose.setVisibility(gone);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        binding.messagePreviewParent.setCardBackgroundColor(color);
    }

    public void setStrokeColor(@ColorInt int color) {
        binding.messagePreviewParent.setStrokeColor(color);
    }

    public void setStrokeWidth(@Dimension float dimension) {
        binding.messagePreviewParent.setStrokeWidth((int) dimension);
    }

    public void setMessageIconTint(@ColorInt int color) {
        binding.messageIcon.setColorFilter(color);
    }

    public void setCornerRadius(@Dimension float radius) {
        binding.messagePreviewParent.setRadius(radius);
    }

    public void setSeparatorColor(@ColorInt int color) {
        binding.separatorView.setBackgroundColor(color);
    }

    public void setTitleTextColor(@ColorInt int color) {
        binding.tvMessageLayoutTitle.setTextColor(color);
    }

    public void setTitleTextAppearance(@StyleRes int resourceId) {
        binding.tvMessageLayoutTitle.setTextAppearance(resourceId);
    }

    public void setSubtitleTextColor(@ColorInt int color) {
        binding.tvMessageLayoutSubtitle.setTextColor(color);
    }

    public void setSubtitleTextAppearance(@StyleRes int resourceId) {
        binding.tvMessageLayoutSubtitle.setTextAppearance(resourceId);
    }

    public void setCloseIcon(Drawable drawable) {
        binding.ivMessageClose.setImageDrawable(drawable);
    }

    public void setMessageIcon(@DrawableRes int icon) {
        Drawable drawable = AppCompatResources.getDrawable(getContext(), icon);
        binding.messageIcon.setImageDrawable(drawable);
    }

    public void setCloseIconTint(@ColorInt int color) {
        binding.ivMessageClose.setColorFilter(color);
    }

    public void setStyle(@StyleRes int messagePreviewStyle) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(messagePreviewStyle, R.styleable.CometChatMessagePreview);
        extractAttributesAndApplyDefaults(typedArray);
    }

    public TextView getSubtitleView() {
        return binding.tvMessageLayoutSubtitle;
    }
}
