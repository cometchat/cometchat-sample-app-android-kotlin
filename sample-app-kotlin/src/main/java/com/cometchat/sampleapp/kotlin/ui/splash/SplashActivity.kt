package com.cometchat.sampleapp.kotlin.ui.splash

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
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivitySplashBinding
import com.cometchat.sampleapp.kotlin.ui.credentials.AppCredentialsActivity
import com.cometchat.sampleapp.kotlin.ui.home.HomeActivity
import com.cometchat.sampleapp.kotlin.ui.login.LoginActivity
import kotlinx.coroutines.launch

/**
 * Splash screen activity for the CometChat Sample App.
 *
 * This activity serves as the entry point (LAUNCHER) and handles:
 * - SDK initialization
 * - Credential checking
 * - Auto-login for returning users
 * - Navigation routing based on app state
 *
 * ## Navigation Flow:
 * 1. App launches → SplashActivity displays logo and loading indicator
 * 2. Check SharedPreferences for stored App ID
 *    - No App ID → Navigate to AppCredentialsActivity
 * 3. Initialize CometChat SDK with stored credentials
 *    - SDK init fails → Show error dialog with retry option
 * 4. Check if user is logged in
 *    - User logged in → Navigate to HomeActivity
 *    - No user → Navigate to LoginActivity
 * 5. Finish SplashActivity to prevent back navigation
 *
 * ## Architecture:
 * - Uses ViewBinding for type-safe view access
 * - Uses [SplashViewModel] with StateFlow for state management
 * - Follows MVVM pattern with unidirectional data flow
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4
 *
 * @see SplashViewModel
 * @see AppCredentialsActivity
 * @see LoginActivity
 * @see HomeActivity
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        observeState()
    }

    /**
     * Applies system window insets padding to avoid overlap with system bars.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
    }

    /**
     * Observes SplashViewModel.state via StateFlow and handles navigation.
     * Uses repeatOnLifecycle pattern for lifecycle-aware collection.
     */
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Handle errors
                    state.error?.let { error ->
                        showErrorDialog(error.message ?: getString(R.string.error_sdk_init_failed))
                        viewModel.clearError()
                    }

                    // Handle navigation
                    state.navigation?.let { navigation ->
                        handleNavigation(navigation)
                    }
                }
            }
        }
    }

    /**
     * Handles navigation based on the determined destination.
     *
     * @param navigation The navigation destination
     */
    private fun handleNavigation(navigation: SplashNavigation) {
        when (navigation) {
            is SplashNavigation.ToAppCredentials -> navigateToAppCredentials()
            is SplashNavigation.ToLogin -> navigateToLogin()
            is SplashNavigation.ToHome -> navigateToHome()
        }
    }

    /**
     * Navigates to AppCredentialsActivity for first-time setup.
     * Finishes SplashActivity to prevent back navigation.
     *
     * Validates: Requirement 2.2
     */
    private fun navigateToAppCredentials() {
        val intent = Intent(this, AppCredentialsActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Navigates to LoginActivity when user is not logged in.
     * Finishes SplashActivity to prevent back navigation.
     *
     * Validates: Requirement 3.3
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Navigates to HomeActivity when user is already logged in.
     * Uses FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_CLEAR_TASK to clear back stack.
     * Finishes SplashActivity to prevent back navigation.
     *
     * Validates: Requirements 3.2, 3.4, 11.2
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    /**
     * Shows an error dialog with retry option when SDK initialization fails.
     *
     * Validates: Requirement 1.4
     *
     * @param message The error message to display
     */
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
                // Navigate to credentials to allow re-entering
                navigateToAppCredentials()
            }
            .setCancelable(false)
            .show()
    }
}
