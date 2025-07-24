package com.cometchat.sampleapp.java.fcm.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.sampleapp.java.fcm.R;
import com.cometchat.sampleapp.java.fcm.databinding.ActivityAppCredentialsBinding;
import com.cometchat.sampleapp.java.fcm.viewmodels.AppCredentialsViewModel;
import com.google.android.material.card.MaterialCardView;

public class AppCredentialsActivity extends AppCompatActivity {

    private ActivityAppCredentialsBinding binding;
    private AppCredentialsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppCredentialsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyWindowInsets();
        adjustWindowSettings();
        initViewModel();
        initClickListeners();
    }


    private void adjustWindowSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    /**
     * Applies window insets to the parent view to handle system UI visibility.
     */
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    ime.bottom
            );
            return insets;
        });
    }

    private void initViewModel() {
        viewModel = new AppCredentialsViewModel();
    }

    private void initClickListeners() {
        binding.btnContinue.setOnClickListener(v -> {
            if (viewModel.getSelectedRegion().getValue() == null) {
                Toast.makeText(this, R.string.app_please_select_app_region, Toast.LENGTH_SHORT).show();
            } else if (binding.etAppId.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.app_invalid_app_id, Toast.LENGTH_SHORT).show();
            } else if (binding.etAuthKey.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.app_invalid_auth_token, Toast.LENGTH_SHORT).show();
            } else {
                viewModel.initUIKit(this, binding.etAppId.getText().toString(), binding.etAuthKey.getText().toString());
            }
        });

        binding.cardUs.setOnClickListener(v -> {
            viewModel.setSelectedRegion(getString(R.string.app_region_us).toLowerCase());
            regionCardUIHandler(binding.cardUs, binding.cardEu, binding.cardIn);
        });

        binding.cardEu.setOnClickListener(v -> {
            viewModel.setSelectedRegion(getString(R.string.app_region_eu).toLowerCase());
            regionCardUIHandler(binding.cardEu, binding.cardUs, binding.cardIn);
        });

        binding.cardIn.setOnClickListener(v -> {
            viewModel.setSelectedRegion(getString(R.string.app_region_in).toLowerCase());
            regionCardUIHandler(binding.cardIn, binding.cardEu, binding.cardUs);
        });
    }

    private void regionCardUIHandler(MaterialCardView selected, MaterialCardView unselected1, MaterialCardView unselected2) {
        selected.setStrokeColor(CometChatTheme.getStrokeColorHighlight(this));
        selected.setCardBackgroundColor(CometChatTheme.getExtendedPrimaryColor50(this));

        unselected1.setStrokeColor(CometChatTheme.getStrokeColorDefault(this));
        unselected1.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(this));

        unselected2.setStrokeColor(CometChatTheme.getStrokeColorDefault(this));
        unselected2.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(this));
    }
}
