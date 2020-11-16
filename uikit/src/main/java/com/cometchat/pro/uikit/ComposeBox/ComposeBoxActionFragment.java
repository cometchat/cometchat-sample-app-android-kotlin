package com.cometchat.pro.uikit.ComposeBox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cometchat.pro.uikit.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ComposeBoxActionFragment extends BottomSheetDialogFragment {

    private TextView galleryMessage;
    private TextView cameraMessage;
    private TextView fileMessage;
    private TextView audioMessage;
    private TextView locationMessage;
    private TextView pollsMessage;
    private TextView stickerMessage;

    private boolean isGalleryVisible;
    private boolean isCameraVisible;
    private boolean isAudioVisible;
    private boolean isFileVisible;
    private boolean isLocationVisible;
    private boolean isPollsVisible;
    private boolean isStickerVisible;

    private ComposeBoxActionListener composeBoxActionListener;

    private String type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null) {
            isGalleryVisible = getArguments().getBoolean("isGalleryVisible");
            isCameraVisible = getArguments().getBoolean("isCameraVisible");
            isFileVisible = getArguments().getBoolean("isFileVisible");
            isAudioVisible = getArguments().getBoolean("isAudioVisible");
            isLocationVisible = getArguments().getBoolean("isLocationVisible");
            isPollsVisible = getArguments().getBoolean("isPollsVisible");
            isStickerVisible = getArguments().getBoolean("isStickerVisible");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_composebox_actions, container, false);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                // androidx should use: com.google.android.material.R.id.design_bottom_sheet
                FrameLayout bottomSheet = (FrameLayout)
                        dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setPeekHeight(0);
            }
        });
        stickerMessage = view.findViewById(R.id.sticker_message);
        pollsMessage = view.findViewById(R.id.polls_message);
        galleryMessage = view.findViewById(R.id.gallery_message);
        cameraMessage = view.findViewById(R.id.camera_message);
        fileMessage = view.findViewById(R.id.file_message);
        audioMessage = view.findViewById(R.id.audio_message);
        locationMessage = view.findViewById(R.id.location_message);

        if (isStickerVisible)
            stickerMessage.setVisibility(View.VISIBLE);
        else
            stickerMessage.setVisibility(View.GONE);
        if (isPollsVisible)
            pollsMessage.setVisibility(View.VISIBLE);
        else
            pollsMessage.setVisibility(View.GONE);
        if (isGalleryVisible)
            galleryMessage.setVisibility(View.VISIBLE);
        else
            galleryMessage.setVisibility(View.GONE);
        if (isCameraVisible)
            cameraMessage.setVisibility(View.VISIBLE);
        else
            cameraMessage.setVisibility(View.GONE);
        if (isFileVisible)
            fileMessage.setVisibility(View.VISIBLE);
        else
            fileMessage.setVisibility(View.GONE);
        if (isAudioVisible)
            audioMessage.setVisibility(View.VISIBLE);
        else
            audioMessage.setVisibility(View.GONE);
        if (isLocationVisible)
            locationMessage.setVisibility(View.VISIBLE);
        else
            locationMessage.setVisibility(View.GONE);

        stickerMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (composeBoxActionListener!=null)
                    composeBoxActionListener.onStickerClick();
                dismiss();
            }
        });
        pollsMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (composeBoxActionListener!=null)
                    composeBoxActionListener.onPollClick();
                dismiss();
            }
        });
        galleryMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (composeBoxActionListener!=null)
                    composeBoxActionListener.onGalleryClick();
                dismiss();
            }
        });
        cameraMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (composeBoxActionListener!=null)
                    composeBoxActionListener.onCameraClick();
                dismiss();
            }
        });
        fileMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (composeBoxActionListener!=null)
                    composeBoxActionListener.onFileClick();
                dismiss();
            }
        });
        audioMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (composeBoxActionListener!=null)
                    composeBoxActionListener.onAudioClick();
                dismiss();
            }
        });
        locationMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (composeBoxActionListener!=null)
                    composeBoxActionListener.onLocationClick();
                dismiss();
            }
        });
        return view;
    }


    public void setComposeBoxActionListener(ComposeBoxActionListener composeBoxActionListener) {
        this.composeBoxActionListener = composeBoxActionListener;

    }

    public interface ComposeBoxActionListener {
        void onGalleryClick();
        void onCameraClick();
        void onFileClick();
        void onAudioClick();
        void onLocationClick();
        void onPollClick();
        void onStickerClick();
    }

}