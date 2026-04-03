package com.cometchat.sampleapp.kotlin.ui.login

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
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.sampleapp.kotlin.databinding.ActivityLoginBinding
import com.cometchat.sampleapp.kotlin.viewmodel.LoginViewModel
import com.cometchat.sampleapp.kotlin.ui.credentials.AppCredentialsActivity
import com.cometchat.sampleapp.kotlin.ui.home.HomeActivity
import kotlinx.coroutines.launch

/**
 * Login screen activity for the CometChat Sample App.
 * UI parity with master-app-kotlin2 LoginActivity.
 *
 * Features:
 * - Displays sample users in a 3-column grid
 * - Supports single-select with toggle behavior
 * - Manual UID input clears sample user selection
 * - Shows/hides grid and divider based on API response
 * - Navigates to HomeActivity on successful login
 * - Auto-login check: redirects to HomeActivity if already logged in
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var sampleUserAdapter: SampleUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-login check: if user is already logged in, navigate to HomeActivity
        // Note: Only check if SDK is initialized to avoid crash
        try {
            if (CometChatUIKit.getLoggedInUser() != null) {
                navigateToHome()
                return
            }
        } catch (e: Exception) {
            // SDK not initialized yet, continue to show login screen
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupRecyclerView()
        setupClickListeners()
        setupUidInputListener()
        observeState()
        
        // Fetch sample users from API
        viewModel.fetchSampleUsers()
    }

    /**
     * Applies system window insets padding to avoid overlap with system bars.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Sets up the RecyclerView with GridLayoutManager(3) for sample users grid.
     */
    private fun setupRecyclerView() {
        sampleUserAdapter = SampleUserAdapter { user ->
            // On user tap → clear UID field first, then call viewModel.selectUser(user)
            // Matches master-app-kotlin behavior
            binding.etUid.setText("")
            viewModel.selectUser(user)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@LoginActivity, 3)
            adapter = sampleUserAdapter
        }
    }

    /**
     * Sets up click listeners for Continue button and Change App Credentials link.
     */
    private fun setupClickListeners() {
        // Continue button click
        binding.btnContinue.setOnClickListener {
            val errorMessage = viewModel.onContinueClick()
            if (errorMessage != null) {
                // Show error toast if validation fails
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        // Change App Credentials click
        // Validates: UI parity with master-app-kotlin - navigates to AppCredentialsActivity
        binding.viewChangeAppCredentials.setOnClickListener {
            val intent = Intent(this, AppCredentialsActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Sets up the manual UID input field listener.
     * On focus → clear sample user selection (both ViewModel and adapter)
     * On text change → update ViewModel state
     */
    private fun setupUidInputListener() {
        // On manual UID focus → clear selection in both ViewModel and adapter
        // Matches master-app-kotlin behavior
        binding.etUid.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.onManualUidFocused()
                sampleUserAdapter.clearSelection()
            }
        }

        // Update manual UID in ViewModel on text change
        binding.etUid.doAfterTextChanged { text ->
            viewModel.setManualUid(text?.toString() ?: "")
        }
    }

    /**
     * Observes LoginViewModel.state via StateFlow and updates UI accordingly.
     * Uses repeatOnLifecycle pattern matching master-app-kotlin2.
     */
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Update sample users list using submitList()
                    sampleUserAdapter.submitList(state.users)

                    // Update selection state in adapter
                    sampleUserAdapter.setSelectedUser(state.selectedUser)

                    // Show/hide grid and divider based on API response
                    if (state.users.isEmpty()) {
                        binding.tvSubtitle.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.viewDivider.visibility = View.GONE
                    } else {
                        binding.tvSubtitle.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.viewDivider.visibility = View.VISIBLE
                    }

                    // Clear UID input when a user is selected (sync UI with state)
                    if (state.selectedUser != null && binding.etUid.text.toString() != state.manualUid) {
                        binding.etUid.setText(state.manualUid)
                    }

                    // On state.error → show error toast
                    state.error?.let { error ->
                        Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }

                    // On state.isLoggedIn = true → navigate to HomeActivity
                    if (state.isLoggedIn) {
                        navigateToHome()
                    }
                }
            }
        }
    }

    /**
     * Navigates to HomeActivity on successful login.
     * Finishes this activity to prevent back navigation.
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
