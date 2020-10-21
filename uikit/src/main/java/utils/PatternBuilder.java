package utils;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternBuilder {
    // Records the pattern spans to apply to a TextView
    ArrayList<SpannablePatternItem> patterns;

    /* This stores a particular pattern item
       complete with pattern, span styles, and click listener */
    public class SpannablePatternItem {
        public SpannablePatternItem(Pattern pattern, SpannableStyleListener styles, SpannableClickedListener listener) {
            this.pattern = pattern;
            this.styles = styles;
            this.listener = listener;
        }

        public SpannableStyleListener styles;
        public Pattern pattern;
        public SpannableClickedListener listener;
    }

    /* This stores the style listener for a pattern item
       Used to style a particular category of spans */
    public static abstract class SpannableStyleListener {
        public int spanTextColor;

        public SpannableStyleListener() {
        }

        public SpannableStyleListener(int spanTextColor) {
            this.spanTextColor = spanTextColor;
        }

        abstract void onSpanStyled(TextPaint ds);
    }

    /* This stores the click listener for a pattern item
       Used to handle clicks to a particular category of spans */
    public interface SpannableClickedListener {
        void onSpanClicked(String text);
    }

    /* This is the custom clickable span class used
       to handle user clicks to our pattern spans
       applying the styles and invoking click listener.
     */
    public class StyledClickableSpan extends ClickableSpan {
        SpannablePatternItem item;

        public StyledClickableSpan(SpannablePatternItem item) {
            this.item = item;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            if (item.styles != null) {
                item.styles.onSpanStyled(ds);
            }
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View widget) {
            if (item.listener != null) {
                TextView tv = (TextView) widget;
                Spanned span = (Spanned) tv.getText();
                int start = span.getSpanStart(this);
                int end = span.getSpanEnd(this);
                CharSequence text = span.subSequence(start, end);
                item.listener.onSpanClicked(text.toString());
            }
            widget.invalidate();
        }
    }

    /* ----- Constructors ------- */
    public PatternBuilder() {
        this.patterns = new ArrayList<>();
    }

    /* These are the `addPattern` overloaded signatures */
    // Each allows us to add a span pattern with different arguments
    public PatternBuilder addPattern(Pattern pattern, SpannableStyleListener spanStyles, SpannableClickedListener listener) {
        patterns.add(new SpannablePatternItem(pattern, spanStyles, listener));
        return this;
    }

    public PatternBuilder addPattern(Pattern pattern, SpannableStyleListener spanStyles) {
        addPattern(pattern, spanStyles, null);
        return this;
    }

    public PatternBuilder addPattern(Pattern pattern) {
        addPattern(pattern, null, null);
        return this;
    }

    public PatternBuilder addPattern(Pattern pattern, int textColor) {
        addPattern(pattern, textColor, null);
        return this;
    }

    public PatternBuilder addPattern(Pattern pattern, int textColor, SpannableClickedListener listener) {
        SpannableStyleListener styles = new SpannableStyleListener(textColor) {
            @Override
            public void onSpanStyled(TextPaint ds) {
                ds.linkColor = this.spanTextColor;
            }
        };
        addPattern(pattern, styles, listener);
        return this;
    }

    public PatternBuilder addPattern(Pattern pattern, SpannableClickedListener listener) {
        addPattern(pattern, null, listener);
        return this;
    }

    /* BUILDER METHODS */

    // This builds the pattern span and applies to a TextView
    public void into(TextView textView) {
        SpannableStringBuilder result = build(textView.getText());
        textView.setText(result);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // This builds the pattern span into a `SpannableStringBuilder`
    // Requires a CharSequence to be passed in to be applied to
    public SpannableStringBuilder build(CharSequence editable) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(editable);
        for (SpannablePatternItem item : patterns) {
            Matcher matcher = item.pattern.matcher(ssb);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                StyledClickableSpan url = new StyledClickableSpan(item);
                ssb.setSpan(url, start, end, 0);
            }
        }
        return ssb;
    }
}
