package listeners;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {
    public static final long NO_HEADER_ID = -1L;
    private Map<Long, RecyclerView.ViewHolder> mHeaderCache;
    private StickyHeaderAdapter mAdapter;
    private boolean mRenderInline;

    public StickyHeaderDecoration(StickyHeaderAdapter var1) {
        this(var1, false);
    }

    public StickyHeaderDecoration(StickyHeaderAdapter var1, boolean var2) {
        this.mAdapter = var1;
        this.mHeaderCache = new HashMap();
        this.mRenderInline = var2;
    }

    public void getItemOffsets(Rect var1, View var2, RecyclerView var3, RecyclerView.State var4) {
        int var5 = var3.getChildAdapterPosition(var2);
        int var6 = 0;
        if (var5 != -1 && this.hasHeader(var5) && this.showHeaderAboveItem(var5)) {
            View var7 = this.getHeader(var3, var5).itemView;
            var6 = this.getHeaderHeightForLayout(var7);
        }

        var1.set(0, var6, 0, 0);
    }

    private boolean showHeaderAboveItem(int var1) {
        if (var1 == 0) {
            return true;
        } else {
            return this.mAdapter.getHeaderId(var1 - 1) != this.mAdapter.getHeaderId(var1);
        }
    }

    public void clearHeaderCache() {
        this.mHeaderCache.clear();
    }

    public View findHeaderViewUnder(float var1, float var2) {
        Iterator var3 = this.mHeaderCache.values().iterator();

        View var5;
        float var6;
        float var7;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            RecyclerView.ViewHolder var4 = (RecyclerView.ViewHolder) var3.next();
            var5 = var4.itemView;
            var6 = ViewCompat.getTranslationX(var5);
            var7 = ViewCompat.getTranslationY(var5);
        }
        while (var1 < (float) var5.getLeft() + var6 || var1 > (float) var5.getRight() + var6 || var2 < (float) var5.getTop() + var7 || var2 > (float) var5.getBottom() + var7);

        return var5;
    }

    private boolean hasHeader(int var1) {
        return this.mAdapter.getHeaderId(var1) != -1L;
    }

    private RecyclerView.ViewHolder getHeader(RecyclerView var1, int var2) {
        long var3 = this.mAdapter.getHeaderId(var2);
        if (this.mHeaderCache.containsKey(var3)) {
            return (RecyclerView.ViewHolder) this.mHeaderCache.get(var3);
        } else {
            RecyclerView.ViewHolder var5 = this.mAdapter.onCreateHeaderViewHolder(var1);
            View var6 = var5.itemView;
            this.mAdapter.onBindHeaderViewHolder(var5, var2, var3);
            int var7 = View.MeasureSpec.makeMeasureSpec(var1.getMeasuredWidth(), 1073741824);
            int var8 = View.MeasureSpec.makeMeasureSpec(var1.getMeasuredHeight(), 0);
            int var9 = ViewGroup.getChildMeasureSpec(var7, var1.getPaddingLeft() + var1.getPaddingRight(), var6.getLayoutParams().width);
            int var10 = ViewGroup.getChildMeasureSpec(var8, var1.getPaddingTop() + var1.getPaddingBottom(), var6.getLayoutParams().height);
            var6.measure(var9, var10);
            var6.layout(0, 0, var6.getMeasuredWidth(), var6.getMeasuredHeight());
            this.mHeaderCache.put(var3, var5);
            return var5;
        }
    }

    public void onDrawOver(Canvas var1, RecyclerView var2, RecyclerView.State var3) {
        int var4 = var2.getChildCount();
        long var5 = -1L;

        for (int var7 = 0; var7 < var4; ++var7) {
            View var8 = var2.getChildAt(var7);
            int var9 = var2.getChildAdapterPosition(var8);
            if (var9 != -1 && this.hasHeader(var9)) {
                long var10 = this.mAdapter.getHeaderId(var9);
                if (var10 != var5) {
                    var5 = var10;
                    View var12 = this.getHeader(var2, var9).itemView;
                    var1.save();
                    int var13 = var8.getLeft();
                    int var14 = this.getHeaderTop(var2, var8, var12, var9, var7);
                    var1.translate((float) var13, (float) var14);
                    var12.setTranslationX((float) var13);
                    var12.setTranslationY((float) var14);
                    var12.draw(var1);
                    var1.restore();
                }
            }
        }

    }

    private int getHeaderTop(RecyclerView var1, View var2, View var3, int var4, int var5) {
        int var6 = this.getHeaderHeightForLayout(var3);
        int var7 = (int) var2.getY() - var6;
        if (var5 == 0) {
            int var8 = var1.getChildCount();
            long var9 = this.mAdapter.getHeaderId(var4);

            for (int var11 = 1; var11 < var8; ++var11) {
                int var12 = var1.getChildAdapterPosition(var1.getChildAt(var11));
                if (var12 != -1) {
                    long var13 = this.mAdapter.getHeaderId(var12);
                    if (var13 != var9) {
                        View var15 = var1.getChildAt(var11);
                        int var16 = (int) var15.getY() - (var6 + this.getHeader(var1, var12).itemView.getHeight());
                        if (var16 < 0) {
                            return var16;
                        }
                        break;
                    }
                }
            }

            var7 = Math.max(0, var7);
        }

        return var7;
    }

    private int getHeaderHeightForLayout(View var1) {
        return this.mRenderInline ? 0 : var1.getHeight();
    }
}