package com.cometchat.ai.sampleapp.kotlin.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cometchat.ai.sampleapp.kotlin.R
import com.cometchat.ai.sampleapp.kotlin.databinding.ActivitySplashBinding
import com.cometchat.ai.sampleapp.kotlin.ui.agents.AIAssistantUsersActivity
import com.cometchat.ai.sampleapp.kotlin.ui.credentials.AppCredentialsActivity
import com.cometchat.ai.sampleapp.kotlin.ui.login.LoginActivity
import kotlinx.coroutines.launch

/**
 * Entry point activity for the AI Assistant Sample App.
 * Handles SDK init, credential checking, and navigation to the next screen.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        observeState()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    state.error?.let { error ->
                        showErrorDialog(
                            error.message ?: getString(R.string.error_sdk_init_failed)
                        )
                        viewModel.clearError()
                    }
                    state.navigation?.let { navigation ->
                        handleNavigation(navigation)
                    }
                }
            }
        }
    }

    private fun handleNavigation(navigation: SplashNavigation) {
        when (navigation) {
            SplashNavigation.ToAppCredentials -> navigateTo(AppCredentialsActivity::class.java)
            SplashNavigation.ToLogin -> navigateTo(LoginActivity::class.java)
            SplashNavigation.ToAIAssistantUsers -> {
                val intent = Intent(this, AIAssistantUsersActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun navigateTo(activity: Class<*>) {
        startActivity(Intent(this, activity))
        finish()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_title)
            .setMessage(message)
            .setPositiveButton(R.string.retry) { dialog, _ ->
                dialog.dismiss()
                viewModel.retry()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                navigateTo(AppCredentialsActivity::class.java)
            }
            .setCancelable(false)
            .show()
    }
}
