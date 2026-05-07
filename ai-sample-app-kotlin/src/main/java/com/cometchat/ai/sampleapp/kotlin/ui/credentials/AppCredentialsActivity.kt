package com.cometchat.ai.sampleapp.kotlin.ui.credentials

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
import com.cometchat.ai.sampleapp.kotlin.R
import com.cometchat.ai.sampleapp.kotlin.databinding.ActivityAppCredentialsBinding
import com.cometchat.ai.sampleapp.kotlin.ui.login.LoginActivity
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

/**
 * Activity for entering CometChat app credentials (App ID, Auth Key, Region).
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

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
    }

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

    private fun setupInputListeners() {
        binding.etAppId.doAfterTextChanged { text ->
            viewModel.setAppId(text?.toString() ?: "")
        }
        binding.etAuthKey.doAfterTextChanged { text ->
            viewModel.setAuthKey(text?.toString() ?: "")
        }
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            val errorMessage = viewModel.onContinueClick(this)
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateRegionCardSelection(state.selectedRegion)

                    binding.progressBar.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE
                    binding.btnContinue.isEnabled = !state.isLoading

                    state.error?.let { error ->
                        showErrorDialog(error)
                        viewModel.clearError()
                    }

                    if (state.isInitialized) {
                        navigateToLogin()
                    }
                }
            }
        }
    }

    private fun updateRegionCardSelection(selectedRegion: String?) {
        regionCardUiHandler(binding.cardRegionUs, selected = selectedRegion == REGION_US)
        regionCardUiHandler(binding.cardRegionEu, selected = selectedRegion == REGION_EU)
        regionCardUiHandler(binding.cardRegionIn, selected = selectedRegion == REGION_IN)
    }

    private fun regionCardUiHandler(card: MaterialCardView, selected: Boolean) {
        if (selected) {
            card.strokeColor = CometChatTheme.getStrokeColorHighlight(this)
            card.setCardBackgroundColor(CometChatTheme.getExtendedPrimaryColor50(this))
        } else {
            card.strokeColor = CometChatTheme.getStrokeColorDefault(this)
            card.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(this))
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
