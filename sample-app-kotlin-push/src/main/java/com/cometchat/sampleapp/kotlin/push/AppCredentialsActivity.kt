package com.cometchat.sampleapp.kotlin.push

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
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
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.sampleapp.kotlin.push.databinding.ActivityAppCredentialsBinding
import com.cometchat.sampleapp.kotlin.push.shared.AppCredentialsViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

/**
 * App Credentials screen activity that allows users to configure CometChat app credentials.
 * 
 * Features:
 * - Displays three region selection cards (US, EU, IN)
 * - Single-select region behavior with visual feedback
 * - App ID and Auth Key input fields
 * - Validates inputs and saves credentials
 * - Navigates to LoginActivity on successful SDK initialization
 * 
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.13, 6.14
 */
class AppCredentialsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppCredentialsBinding
    private val viewModel: AppCredentialsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAppCredentialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupRegionCardListeners()
        setupInputListeners()
        setupContinueButton()
        observeState()
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
     * Sets up click listeners for region cards.
     * On region card tap → call viewModel.selectRegion(region)
     * 
     * Validates: Requirements 6.1, 6.2, 6.3
     */
    private fun setupRegionCardListeners() {
        binding.cardRegionUs.setOnClickListener {
            viewModel.selectRegion("us")
        }
        binding.cardRegionEu.setOnClickListener {
            viewModel.selectRegion("eu")
        }
        binding.cardRegionIn.setOnClickListener {
            viewModel.selectRegion("in")
        }
    }

    /**
     * Sets up text change listeners for App ID and Auth Key input fields.
     * On App ID text change → call viewModel.setAppId(text)
     * On Auth Key text change → call viewModel.setAuthKey(text)
     * 
     * Validates: Requirements 6.4, 6.5
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
     * Sets up the Continue button click listener.
     * On Continue click → call viewModel.onContinueClick(context), show error toast if returned
     * 
     * Validates: Requirements 6.6, 6.7, 6.8, 6.9
     */
    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            val errorMessage = viewModel.onContinueClick(this)
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Observes AppCredentialsViewModel.state via StateFlow and updates UI accordingly.
     * - Updates region card selection visual feedback
     * - On state.isInitialized = true → navigate to LoginActivity
     * - On state.error → show error toast
     */
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Update region card selection visual feedback
                    updateRegionCardSelection(state.selectedRegion)

                    // On state.error → show error toast
                    state.error?.let { error ->
                        Toast.makeText(this@AppCredentialsActivity, error, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }

                    // On state.isInitialized = true → navigate to LoginActivity
                    if (state.isInitialized) {
                        navigateToLogin()
                    }
                }
            }
        }
    }

    /**
     * Updates the visual feedback for region card selection.
     * Selected: strokeColor = cometchatStrokeColorHighlight, backgroundColor = cometchatExtendedPrimaryColor50
     * Unselected: strokeColor = cometchatStrokeColorDefault, backgroundColor = cometchatBackgroundColor1
     * 
     * Validates: Requirements 6.2, 6.3
     * 
     * @param selectedRegion The currently selected region (us, eu, in) or null if none selected
     */
    private fun updateRegionCardSelection(selectedRegion: String?) {
        val highlightStrokeColor = CometChatTheme.getStrokeColorHighlight(this)
        val defaultStrokeColor = CometChatTheme.getStrokeColorDefault(this)
        val selectedBackgroundColor = CometChatTheme.getExtendedPrimaryColor50(this)
        val defaultBackgroundColor = CometChatTheme.getBackgroundColor1(this)

        // Update US card
        updateCardStyle(
            binding.cardRegionUs,
            isSelected = selectedRegion == "us",
            highlightStrokeColor = highlightStrokeColor,
            defaultStrokeColor = defaultStrokeColor,
            selectedBackgroundColor = selectedBackgroundColor,
            defaultBackgroundColor = defaultBackgroundColor
        )

        // Update EU card
        updateCardStyle(
            binding.cardRegionEu,
            isSelected = selectedRegion == "eu",
            highlightStrokeColor = highlightStrokeColor,
            defaultStrokeColor = defaultStrokeColor,
            selectedBackgroundColor = selectedBackgroundColor,
            defaultBackgroundColor = defaultBackgroundColor
        )

        // Update IN card
        updateCardStyle(
            binding.cardRegionIn,
            isSelected = selectedRegion == "in",
            highlightStrokeColor = highlightStrokeColor,
            defaultStrokeColor = defaultStrokeColor,
            selectedBackgroundColor = selectedBackgroundColor,
            defaultBackgroundColor = defaultBackgroundColor
        )
    }

    /**
     * Updates the style of a MaterialCardView based on selection state.
     */
    private fun updateCardStyle(
        card: MaterialCardView,
        isSelected: Boolean,
        highlightStrokeColor: Int,
        defaultStrokeColor: Int,
        selectedBackgroundColor: Int,
        defaultBackgroundColor: Int
    ) {
        if (isSelected) {
            card.strokeColor = highlightStrokeColor
            card.setCardBackgroundColor(ColorStateList.valueOf(selectedBackgroundColor))
        } else {
            card.strokeColor = defaultStrokeColor
            card.setCardBackgroundColor(ColorStateList.valueOf(defaultBackgroundColor))
        }
    }

    /**
     * Navigates to LoginActivity on successful SDK initialization.
     * Finishes this activity to prevent back navigation.
     * 
     * Validates: Requirement 6.13
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
