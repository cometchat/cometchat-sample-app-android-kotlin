package com.cometchat.pro.uikit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.cometchat.pro.uikit.SharedMedia.SharedFilesFragment;
import com.cometchat.pro.uikit.SharedMedia.SharedImagesFragment;
import com.cometchat.pro.uikit.SharedMedia.SharedVideosFragment;
import com.google.android.material.tabs.TabLayout;

import adapter.TabAdapter;
import listeners.ComposeActionListener;
import utils.Utils;


public class SharedMediaView extends RelativeLayout {

    private Context context;

    private ViewPager viewPager;

    private TabLayout tabLayout;

    private String Id;

    private String type;

    private AttributeSet attrs;

    private TabAdapter adapter;

    public SharedMediaView(Context context) {
        super(context);
        this.context = context;
        initViewComponent(context,null,-1,-1);
    }

    public SharedMediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        this.context = context;
        initViewComponent(context,attrs,-1,-1);
    }

    public SharedMediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        initViewComponent(context,attrs,defStyleAttr,-1);
    }


    private void initViewComponent(Context context,AttributeSet attributeSet,int defStyleAttr,int defStyleRes){

        View view =View.inflate(context, R.layout.cometchat_shared_media,null);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(attributeSet, R.styleable.SharedMediaView, 0, 0);
        addView(view);

        Bundle bundle = new Bundle();
        bundle.putString("Id",Id);
        bundle.putString("type",type);
        if (type!=null) {
            viewPager = this.findViewById(R.id.viewPager);
            tabLayout = view.findViewById(R.id.tabLayout);
            adapter = new TabAdapter(((FragmentActivity)context).getSupportFragmentManager());
            SharedImagesFragment images = new SharedImagesFragment();
            images.setArguments(bundle);
            adapter.addFragment(images, getResources().getString(R.string.images));
            SharedVideosFragment videos = new SharedVideosFragment();
            videos.setArguments(bundle);
            adapter.addFragment(videos, getResources().getString(R.string.videos));
            SharedFilesFragment files = new SharedFilesFragment();
            files.setArguments(bundle);
            adapter.addFragment(files, getResources().getString(R.string.files));
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(3);
            tabLayout.setupWithViewPager(viewPager);

            if(Utils.isDarkMode(context)) {
                tabLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
                tabLayout.setTabTextColors(getResources().getColor(R.color.light_grey),getResources().getColor(R.color.textColorWhite));
            } else {
                tabLayout.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
                tabLayout.setTabTextColors(getResources().getColor(R.color.primaryTextColor),getResources().getColor(R.color.textColorWhite));
            }

        }
    }

    public void setRecieverId(String uid) {
        this.Id = uid;
    }

    public void setRecieverType(String receiverTypeUser) {
        this.type = receiverTypeUser;
    }
    public void reload() {
        initViewComponent(context,null,-1,-1);
    }
}
