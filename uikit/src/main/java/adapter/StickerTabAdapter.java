package adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cometchat.pro.uikit.R;

import java.util.ArrayList;
import java.util.List;


public class StickerTabAdapter extends FragmentStatePagerAdapter {
    private Drawable myDrawable;
    private SpannableStringBuilder sb;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private final List<String> mFragmentIconList = new ArrayList<>();
    private Context context;
    public StickerTabAdapter(Context context,FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
       return mFragmentList.get(position);

    }
    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public void addFragment(Fragment fragment, String title,String icon) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
        mFragmentIconList.add(icon);
    }

    public String getPageIcon(int position) { return mFragmentIconList.get(position); }
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        sb = new SpannableStringBuilder(""); // space added before text for convenience
//        myDrawable = context.getResources().getDrawable(R.drawable.default_sticker);
//        myDrawable.setBounds(5, 15, myDrawable.getIntrinsicWidth(), myDrawable.getIntrinsicHeight());
//        ImageSpan span = new ImageSpan(myDrawable, DynamicDrawableSpan.ALIGN_BASELINE);
//        sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        Glide.with(context).load(mFragmentIconList.get(position)).into(new CustomTarget<Drawable>() {
//            @Override
//            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                myDrawable = resource;
//                try {
//                    sb = new SpannableStringBuilder(" ");
//                    myDrawable.setBounds(5, 5, myDrawable.getIntrinsicWidth(), myDrawable.getIntrinsicHeight());
//                    ImageSpan span = new ImageSpan(myDrawable, DynamicDrawableSpan.ALIGN_BASELINE);
//                    sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                } catch (Exception e) {
//                    Log.e("Icon: ", e.getMessage());
//                }
//            }
//
//            @Override
//            public void onLoadCleared(@Nullable Drawable placeholder) {
//
//            }
//        });

        return sb;
    }
    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}
