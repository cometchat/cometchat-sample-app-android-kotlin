package com.cometchat.sampleapp.kotlin.ui.credentials

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityAppCredentialsBinding
import com.cometchat.sampleapp.kotlin.viewmodel.AppCredentialsViewModel
import com.cometchat.sampleapp.kotlin.ui.login.LoginActivity
import kotlinx.coroutines.launch

/**
 * Activity for entering CometChat app credentials (App ID, Auth Key, Region).
 *
 * This activity allows users to configure their CometChat credentials for first-time setup.
 * It displays region selection cards and input fields for App ID and Auth Key.
 *
 * ## Features:
 * - Region selection cards (US, EU, IN) with flag icons and visual highlighting
 * - App ID input field with validation
 * - Auth Key input field with validation
 * - Toast message for missing region selection
 * - Inline validation errors for empty fields
 * - SharedPreferences storage for credentials
 * - UIKit initialization with provided credentials
 * - Navigation to LoginActivity after successful setup
 *
 * ## Navigation Flow:
 * 1. User selects a region (US, EU, or IN)
 * 2. User enters App ID and Auth Key
 * 3. User taps Continue
 * 4. Credentials are validated and saved
 * 5. CometChat UIKit is initialized
 * 6. User is navigated to LoginActivity
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7
 *
 * @see AppCredentialsViewModel
 * @see LoginActivity
 */
class AppCredentialsActivity : AppCompatActivity() {

    companion object {
        private const val REGION_US = "us"
        private const val REGION_EU = "eu"
        private const val REGION_IN = "in"
    }

    private lateinit var binding: ActivityAppCredentialsBinding
    private val viewModel: AppCredentialsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppCredentialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupRegionCards()
        setupInputListeners()
        setupClickListeners()
        observeState()
    }

    /**
     * Applies system window insets padding to avoid overlap with system bars.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
    }

    /**
     * Sets up click listeners for region selection cards.
     *
     * Validates: Requirements 4.1, 4.2
     */
    private fun setupRegionCards() {
        binding.cardRegionUs.setOnClickListener {
            viewModel.selectRegion(REGION_US)
        }

        binding.cardRegionEu.setOnClickListener {
            viewModel.selectRegion(REGION_EU)
        }

        binding.cardRegionIn.setOnClickListener {
            viewModel.selectRegion(REGION_IN)
        }
    }

    /**
     * Sets up text change listeners for input fields.
     */
    private fun setupInputListeners() {
        binding.etAppId.doAfterTextChanged { text ->
            viewModel.setAppId(text?.toString() ?: "")
        }

        binding.etAuthKey.doAfterTextChanged { text ->
            viewModel.setAuthKey(text?.toString() ?: "")
        }
    }

    /**
     * Sets up click listener for the Continue button.
     */
    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            val errorMessage = viewModel.onContinueClick(this)
            if (errorMessage != null) {
                // Show toast for region selection error
                // Validates: Requirement 4.3
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Observes AppCredentialsViewModel.state via StateFlow and updates UI accordingly.
     * Uses repeatOnLifecycle pattern for lifecycle-aware collection.
     */
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Update region card selection states
                    // Validates: Requirement 4.2
                    updateRegionCardSelection(state.selectedRegion)

                    // Show/hide loading indicator
                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.btnContinue.isEnabled = !state.isLoading

                    // Handle errors
                    state.error?.let { error ->
                        showErrorDialog(error)
                        viewModel.clearError()
                    }

                    // Navigate to LoginActivity on success
                    // Validates: Requirement 4.7
                    if (state.isInitialized) {
                        navigateToLogin()
                    }
                }
            }
        }
    }

    /**
     * Updates the visual selection state of region cards.
     *
     * Validates: Requirement 4.2
     *
     * @param selectedRegion The currently selected region code, or null if none selected
     */
    private fun updateRegionCardSelection(selectedRegion: String?) {
        // Reset all cards to unselected state
        binding.cardRegionUs.isChecked = false
        binding.cardRegionEu.isChecked = false
        binding.cardRegionIn.isChecked = false

        // Set the selected card
        when (selectedRegion) {
            REGION_US -> binding.cardRegionUs.isChecked = true
            REGION_EU -> binding.cardRegionEu.isChecked = true
            REGION_IN -> binding.cardRegionIn.isChecked = true
            null -> { /* No selection */ }
        }
    }

    /**
     * Shows an error dialog when SDK initialization fails.
     *
     * @param message The error message to display
     */
    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Navigates to LoginActivity after credentials are saved successfully.
     * Finishes this activity to prevent back navigation.
     *
     * Validates: Requirement 4.7
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
