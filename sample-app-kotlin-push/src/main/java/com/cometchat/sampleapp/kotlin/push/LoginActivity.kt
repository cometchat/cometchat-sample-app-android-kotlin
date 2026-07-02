package com.cometchat.sampleapp.kotlin.push

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.cometchat.sampleapp.kotlin.push.appflow.DefaultComponentAppFlowActivity
import com.cometchat.sampleapp.kotlin.push.databinding.ActivityLoginBinding
import com.cometchat.sampleapp.kotlin.push.shared.LoginViewModel
import kotlinx.coroutines.launch

/**
 * Login screen activity that allows users to authenticate via sample user selection
 * or manual UID entry.
 * 
 * Features:
 * - Displays sample users in a 3-column grid
 * - Supports single-select with toggle behavior
 * - Manual UID input clears sample user selection
 * - Shows/hides grid and divider based on API response
 * - Navigates to the App Flow screen on successful login
 * - Navigates to AppCredentialsActivity on "Change App Credentials" click
 * 
 * Validates: Requirements 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 3.1, 3.2, 3.3, 4.1, 5.1, 5.2, 7.1, 7.2, 7.3, 7.4, 7.5
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var sampleUserAdapter: SampleUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupRecyclerView()
        setupClickListeners()
        setupUidInputListener()
        observeState()

        // Fetch sample users on activity creation
        loginViewModel.fetchSampleUsers()
    }

    /**
     * Applies system window insets padding to avoid overlap with system bars.
     * Validates: Requirement 7.5
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
     * Validates: Requirements 2.2, 8.5
     */
    private fun setupRecyclerView() {
        sampleUserAdapter = SampleUserAdapter { user ->
            // On user tap → call viewModel.selectUser(user)
            loginViewModel.selectUser(user)
        }

        binding.recyclerViewUsers.apply {
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
            val errorMessage = loginViewModel.onContinueClick()
            if (errorMessage != null) {
                // Show error toast if validation fails
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        // Change App Credentials click → navigate to AppCredentialsActivity
        binding.viewChangeAppCredentials.setOnClickListener {
            navigateToAppCredentials()
        }
    }

    /**
     * Sets up the manual UID input field listener.
     * On focus → clear sample user selection
     * On text change → update ViewModel state
     * 
     * Validates: Requirements 3.1, 3.2, 3.3
     */
    private fun setupUidInputListener() {
        // On manual UID focus → call viewModel.onManualUidFocused()
        binding.etUid.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                loginViewModel.onManualUidFocused()
            }
        }

        // Update manual UID in ViewModel on text change
        binding.etUid.doAfterTextChanged { text ->
            loginViewModel.setManualUid(text?.toString() ?: "")
        }
    }

    /**
     * Observes LoginViewModel.state via StateFlow and updates UI accordingly.
     */
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.state.collect { state ->
                    // Update sample users list
                    sampleUserAdapter.submitList(state.users)

                    // Update selection state in adapter
                    sampleUserAdapter.setSelectedUser(state.selectedUser)

                    // Show/hide grid and divider based on API response
                    // If state.users is empty → hide grid and OR divider
                    if (state.users.isEmpty()) {
                        binding.recyclerViewUsers.visibility = View.GONE
                        binding.viewDivider.visibility = View.GONE
                    } else {
                        binding.recyclerViewUsers.visibility = View.VISIBLE
                        binding.viewDivider.visibility = View.VISIBLE
                    }

                    // Clear UID input when a user is selected (sync UI with state)
                    if (state.selectedUser != null && binding.etUid.text.toString() != state.manualUid) {
                        binding.etUid.setText(state.manualUid)
                    }

                    // On state.error → show error toast
                    state.error?.let { error ->
                        Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
                        loginViewModel.clearError()
                    }

                    // On state.isLoggedIn = true → navigate to the App Flow screen
                    if (state.isLoggedIn) {
                        navigateToHome()
                    }
                }
            }
        }
    }

    /**
     * Navigates to the App Flow screen on successful login.
     * Finishes this activity to prevent back navigation.
     * 
     * Validates: Requirement 4.8
     */
    private fun navigateToHome() {
        val intent = Intent(this, DefaultComponentAppFlowActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Navigates to AppCredentialsActivity when "Change App Credentials" is clicked.
     * 
     * Validates: Requirements 5.1, 5.2
     */
    private fun navigateToAppCredentials() {
        val intent = Intent(this, AppCredentialsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
