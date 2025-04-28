package com.cometchat.chatuikit.shared.views.mediaviewer;


import static com.cometchat.chatuikit.shared.views.mediaviewer.CometChatImagePreviewUtils.createImagePreview;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.MediaUtils;

import java.util.HashMap;
import java.util.List;

public class CometChatImageViewerActivity extends AppCompatActivity {

    private static final String ARGS_IMAGE_URLS = "ARGS_IMAGE_URLS";
    private static final String ARGS_FILE_NAME = "ARGS_FILE_NAME";
    private static final String MEDIA_MESSAGE_JSON = "MEDIA_MESSAGE_JSON";
    private static final String MIME_TYPE_URL = "MIME_TYPE_URL";
    private static final String TAG = CometChatImageViewerActivity.class.getSimpleName();
    private List<String> urls;
    private List<String> mimeTypes;
    private List<String> filenames;
    private final int initialPos = 0;
    private ImageAdapter adapter;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private LinearLayout topBar;
    private ImageView shareBtn;

    public static Intent createIntent(Context context, List<String> urls, List<String> mimeType, List<String> filenames) {
        Intent intent = new Intent(context, CometChatImageViewerActivity.class);
        intent.putExtra(ARGS_IMAGE_URLS, (java.io.Serializable) urls);
        intent.putExtra(MIME_TYPE_URL, (java.io.Serializable) mimeType);
        intent.putExtra(ARGS_FILE_NAME, (java.io.Serializable) filenames);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        setContentView(R.layout.cometchat_activity_image_viewer);

        viewPager = findViewById(R.id.viewpager);
        toolbar = findViewById(R.id.toolbar);
        topBar = findViewById(R.id.top_bar_container);
        shareBtn = findViewById(R.id.button_share);

        urls = (List<String>) getIntent().getSerializableExtra(ARGS_IMAGE_URLS);
        mimeTypes = (List<String>) getIntent().getSerializableExtra(MIME_TYPE_URL);
        filenames = (List<String>) getIntent().getSerializableExtra(ARGS_FILE_NAME);

        initToolbar();
        initViewPager();

        shareBtn.setOnClickListener(v -> shareMessage());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (adapter != null) {
                    adapter.clear();
                }
                finish();
            }
        });
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void initViewPager() {
        adapter = new ImageAdapter(this, urls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(initialPos);
    }

    public void shareMessage() {
        if (urls == null || urls.isEmpty() || mimeTypes == null || mimeTypes.isEmpty() || filenames == null || filenames.isEmpty()) {
            CometChatLogger.e(TAG, "Cannot share image, urls or mimeTypes or filenames are null");
            return;
        }
        MediaUtils.downloadFileInNewThread(this,
                                           urls.get(adapter.currentPos),
                                           filenames.get(adapter.currentPos),
                                           mimeTypes.get(adapter.currentPos),
                                           UIKitConstants.files.SHARE);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.cometchat_fade_out_fast);
    }

    @Override
    protected void onDestroy() {
        adapter = null;
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        supportFinishAfterTransition();
        finish();
        return true;
    }

    private void showToolbar() {
        topBar.animate()
              .setInterpolator(new AccelerateDecelerateInterpolator())
              .translationY(0f);
    }

    private void hideToolbar() {
        topBar.animate()
              .setInterpolator(new AccelerateDecelerateInterpolator())
              .translationY(-toolbar.getHeight());
    }

    public class ImageAdapter extends PagerAdapter {

        private final Context context;
        private final List<String> urls;
        private final HashMap<Integer, CometChatImagePreview> previewMap = new HashMap<>();
        private final HashMap<Integer, ImageView> views = new HashMap<>();
        private int currentPos = 0;

        public ImageAdapter(Context context, List<String> urls) {
            this.context = context;
            this.urls = urls;
        }

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = View.inflate(context, R.layout.cometchat_item_image, null);
            ImageView image = view.findViewById(R.id.image);
            FrameLayout frameLayout = view.findViewById(R.id.container);
            container.addView(view);
            loadImage(image, frameLayout, position);
            views.put(position, image);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object obj) {
            container.removeView((View) obj);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object obj) {
            super.setPrimaryItem(container, position, obj);
            this.currentPos = position;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        private void loadImage(ImageView image, ViewGroup container, int position) {

            Glide.with(image.getContext())
                 .load(urls.get(position))
                 .listener(new RequestListener<Drawable>() {
                     @Override
                     public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                         startPostponedEnterTransition();
                         return false;
                     }

                     @Override
                     public boolean onResourceReady(Drawable resource,
                                                    Object model,
                                                    Target<Drawable> target,
                                                    DataSource dataSource,
                                                    boolean isFirstResource) {
                         CometChatImagePreview cometChatImagePreview = createImagePreview(image, container);
                         cometChatImagePreview.setOnViewTranslateListener(new CometChatImagePreview.OnViewTranslateListener() {
                             @Override
                             public void onStart(ImageView view) {
                                 hideToolbar();
                             }

                             @Override
                             public void onViewTranslate(ImageView view, float amount) {

                             }

                             @Override
                             public void onDismiss(ImageView view) {
                                 finishAfterTransition();
                             }

                             @Override
                             public void onRestore(ImageView view) {
                                 showToolbar();
                             }
                         });
                         previewMap.put(position, cometChatImagePreview);
                         if (position == initialPos) {
                             //if need to add bounce back animation for image bubble
//                             setEnterSharedElementCallback(new SharedElementCallback() {
//                                 @Override
//                                 public void onMapSharedElements(@Nullable List<String> names, @Nullable Map<String, View> sharedElements) {
//                                     if (names == null) return;
//
//                                     View view = views.get(currentPos);
//                                     if (view == null) return;
//
//                                     int currentPosition = currentPos;
//                                     view.setTransitionName(context.getString(R.string.cometchat_shared_image_transition, currentPosition));
//
//                                     if (sharedElements != null) {
//                                         sharedElements.clear();
//                                         sharedElements.put(view.getTransitionName(), view);
//                                     }
//                                 }
//                             });

                             startPostponedEnterTransition();
                         }
                         return false;
                     }
                 })
                 .into(image);
        }


        public void clear() {
            for (CometChatImagePreview cometChatImagePreview : previewMap.values()) {
                cometChatImagePreview.cleanup();
            }
            previewMap.clear();
        }
    }
}
