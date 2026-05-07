package com.cometchat.ai.sampleapp.kotlin.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.cometchat.ai.sampleapp.kotlin.databinding.ActivityLoginBinding
import com.cometchat.ai.sampleapp.kotlin.ui.agents.AIAssistantUsersActivity
import com.cometchat.ai.sampleapp.kotlin.ui.credentials.AppCredentialsActivity
import com.cometchat.uikit.core.CometChatUIKit
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var sampleUserAdapter: SampleUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-login: if user already logged in, navigate directly to agents screen
        try {
            if (CometChatUIKit.getLoggedInUser() != null) {
                navigateToAgents()
                return
            }
        } catch (e: Exception) {
            // SDK not initialized; continue to show login screen
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupRecyclerView()
        setupClickListeners()
        setupUidInputListener()
        observeState()

        viewModel.fetchSampleUsers()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        sampleUserAdapter = SampleUserAdapter { user ->
            binding.etUid.setText("")
            viewModel.selectUser(user)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@LoginActivity, 3)
            adapter = sampleUserAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            val errorMessage = viewModel.onContinueClick()
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        binding.viewChangeAppCredentials.setOnClickListener {
            startActivity(Intent(this, AppCredentialsActivity::class.java))
        }
    }

    private fun setupUidInputListener() {
        binding.etUid.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.onManualUidFocused()
                sampleUserAdapter.clearSelection()
            }
        }
        binding.etUid.doAfterTextChanged { text ->
            viewModel.setManualUid(text?.toString() ?: "")
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    sampleUserAdapter.submitList(state.users)
                    sampleUserAdapter.setSelectedUser(state.selectedUser)

                    if (state.users.isEmpty()) {
                        binding.tvSubtitle.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.viewDivider.visibility = View.GONE
                    } else {
                        binding.tvSubtitle.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.viewDivider.visibility = View.VISIBLE
                    }

                    state.error?.let { error ->
                        Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }

                    if (state.isLoggedIn) {
                        navigateToAgents()
                    }
                }
            }
        }
    }

    private fun navigateToAgents() {
        val intent = Intent(this, AIAssistantUsersActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
