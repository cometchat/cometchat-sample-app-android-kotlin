package com.cometchat.sampleapp.java.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;

import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.User;
import com.cometchat.sampleapp.java.R;
import com.cometchat.sampleapp.java.databinding.ActivityLoginBinding;
import com.cometchat.sampleapp.java.ui.adapters.SampleUsersAdapter;
import com.cometchat.sampleapp.java.viewmodels.LoginViewModel;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private SampleUsersAdapter sampleUsersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        initRecyclerView();
        initViewModel();
        initClickListeners();
    }

    private void initRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3); // 3 columns
        binding.recyclerView.setLayoutManager(gridLayoutManager);
        sampleUsersAdapter = new SampleUsersAdapter(new ArrayList<>(), user -> {
            binding.etUid.setText("");
            viewModel.selectedUser(user);
        });
        binding.recyclerView.setAdapter(sampleUsersAdapter);
    }

    private void initViewModel() {
        viewModel = new LoginViewModel();
        viewModel.getSampleUsers();

        viewModel.getLoginStatus().observe(this, onUserLogin());
        viewModel.getUsers().observe(this, onSampleUserListUpdate());
        viewModel.onError().observe(this, onError());
    }

    private void initClickListeners() {
        binding.btnContinue.setOnClickListener(v -> {
            if (viewModel.getSelectedUser().getValue() == null && binding.etUid.getText().toString().isEmpty()) {
                Toast.makeText(
                        LoginActivity.this,
                        ContextCompat.getString(LoginActivity.this, R.string.app_error_invalid_user_or_uid_login),
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                String uid;
                if (viewModel.getSelectedUser().getValue() != null) {
                    uid = viewModel.getSelectedUser().getValue().getUid();
                } else {
                    uid = binding.etUid.getText().toString();
                }
                viewModel.login(uid);
            }
        });

        binding.etUid.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                viewModel.selectedUser(null);
                sampleUsersAdapter.clearSelection();
            }
        });

        binding.viewChangeAppCredentials.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, AppCredentialsActivity.class));
        });
    }

    @NonNull
    private Observer<Boolean> onUserLogin() {
        return isLoggedIn -> {
            if (isLoggedIn != null) {
                if (isLoggedIn) {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(
                            LoginActivity.this,
                            ContextCompat.getString(LoginActivity.this, R.string.app_error_invalid_user_or_uid_login),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        };
    }

    @NonNull
    private Observer<List<User>> onSampleUserListUpdate() {
        return users -> {
            sampleUsersAdapter.updateList(users);
            if (users.isEmpty()) {
                binding.tvSubtitle.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.GONE);
                binding.viewDivider.setVisibility(View.GONE);
            } else {
                binding.tvSubtitle.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.viewDivider.setVisibility(View.VISIBLE);
            }
        };
    }

    private Observer<CometChatException> onError() {
        return e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        };
    }

}
