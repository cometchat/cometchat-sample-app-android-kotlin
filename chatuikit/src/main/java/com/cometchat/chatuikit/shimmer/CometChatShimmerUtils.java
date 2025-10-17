package com.cometchat.chatuikit.shimmer;

import static android.view.View.GONE;

import android.content.Context;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.chatuikit.R;

public class CometChatShimmerUtils {
    public static void showShimmer(
        CometChatShimmerFrameLayout shimmerFrameLayout
    ) {
        showShimmerWithItems(shimmerFrameLayout, null, 0, 0);
    }

    public static void showShimmerWithItems(
        CometChatShimmerFrameLayout shimmerFrameLayout,
        CometChatShimmer shimmer,
        @LayoutRes int itemLayout,
        int itemCount
    ) {
        Context context = shimmerFrameLayout.getContext();
        if (itemLayout != 0 && itemCount > 0) {
            RecyclerView recyclerView = new RecyclerView(shimmerFrameLayout.getContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(shimmerFrameLayout.getContext()));
            recyclerView.setAdapter(new CometChatShimmerAdapter(itemCount, itemLayout));
            shimmerFrameLayout.addView(recyclerView);
        }

        if (shimmer == null) {
            shimmer = new CometChatShimmer.ColorHighlightBuilder()
                    .setBaseAlpha(1f)
                    .setTilt(1)
                    .build();
        }
        shimmerFrameLayout.setShimmer(shimmer);
        shimmerFrameLayout.startShimmer();
    }

    public static void hideShimmer(CometChatShimmerFrameLayout shimmerFrameLayout) {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(GONE);
    }

    public static CometChatShimmer getCometChatShimmerConfig(Context context) {
        return new CometChatShimmer.ColorHighlightBuilder()
                .setBaseAlpha(1f)
                .setTilt(1)
                .setBaseColor(context.getResources().getColor(R.color.cometchat_shimmer_base_color, context.getTheme()))
                .setHighlightColor(context.getResources().getColor(R.color.cometchat_shimmer_base_highlight_color, context.getTheme()))
                .build();
    }

}
