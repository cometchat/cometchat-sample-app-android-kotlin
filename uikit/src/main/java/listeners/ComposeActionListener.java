package listeners;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.core.view.inputmethod.InputContentInfoCompat;

public abstract class ComposeActionListener {

    public void onMoreActionClicked(ImageView moreIcon) {}

    public void onPollActionClicked() {}

    public void onCameraActionClicked() {}

    public void onGalleryActionClicked() {}

    public void onAudioActionClicked() {}

    public void onFileActionClicked() {}

    public void onEmojiActionClicked(ImageView emojiIcon) {}

    public void onSendActionClicked(EditText editText ) {}

    public void onVoiceNoteComplete(String string) {}

    public abstract void  beforeTextChanged(CharSequence charSequence, int i, int i1, int i2);

    public abstract void onTextChanged(CharSequence charSequence, int i, int i1, int i2);

    public abstract void afterTextChanged(Editable editable);

    public void onEditTextMediaSelected(InputContentInfoCompat inputContentInfo) {};


    public void getCameraActionView(ImageView cameraIcon) {
        cameraIcon.setVisibility(View.VISIBLE);
    }

    public void getGalleryActionView(ImageView galleryIcon) {
        galleryIcon.setVisibility(View.VISIBLE);
    }

    public void getFileActionView(ImageView fileIcon) {
        fileIcon.setVisibility(View.VISIBLE);
    }

    public void getLocationActionView(ImageView locationIcon) {
        locationIcon.setVisibility(View.VISIBLE);
    }

    public void onLocationActionClicked() {
    }

    public void onStickerClicked() {
    }
}
