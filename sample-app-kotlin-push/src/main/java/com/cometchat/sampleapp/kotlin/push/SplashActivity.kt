package com.cometchat.sampleapp.kotlin.push

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cometchat.sampleapp.kotlin.push.appflow.DefaultComponentAppFlowActivity
import com.cometchat.sampleapp.kotlin.push.databinding.ActivitySplashBinding
import com.cometchat.sampleapp.kotlin.push.shared.SplashNavigationState
import com.cometchat.sampleapp.kotlin.push.shared.SplashViewModel
import kotlinx.coroutines.launch

/**
 * Splash screen activity that displays the CometChat logo and handles
 * SDK initialization and navigation based on credentials and login state.
 * 
 * Navigation Logic:
 * - No credentials configured → AppCredentialsActivity
 * - Credentials exist, user logged in → the App Flow screen
 * - Credentials exist, user not logged in → LoginActivity (with "Not logged in" toast)
 * - SDK initialization error → Show error toast
 * 
 * Validates: Requirements 1.1, 1.3, 1.7, 1.8
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Apply system window insets padding to avoid overlap with system bars
        applyWindowInsets()
        
        // Observe navigation state from ViewModel
        observeNavigationState()
        
        // Start credentials check and SDK initialization
        splashViewModel.checkCredentialsAndInitialize(applicationContext)
    }

    /**
     * Applies system window insets padding to the main container
     * to avoid overlap with status bar and navigation bar.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Observes the SplashViewModel navigation state and handles navigation
     * to the appropriate screen based on credentials and login status.
     */
    private fun observeNavigationState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.navigationState.collect { state ->
                    when (state) {
                        is SplashNavigationState.Loading -> {
                            // Show loading state - logo is already displayed
                        }
                        is SplashNavigationState.NavigateToAppCredentials -> {
                            navigateToAppCredentials()
                        }
                        is SplashNavigationState.NavigateToLogin -> {
                            Toast.makeText(this@SplashActivity, "Not logged in", Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        }
                        is SplashNavigationState.NavigateToHome -> {
                            navigateToHome()
                        }
                        is SplashNavigationState.Error -> {
                            Toast.makeText(this@SplashActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Navigates to AppCredentialsActivity when no credentials are configured.
     * Finishes this activity to prevent back navigation to splash.
     */
    private fun navigateToAppCredentials() {
        val intent = Intent(this, AppCredentialsActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Navigates to LoginActivity when credentials exist but user is not logged in.
     * Finishes this activity to prevent back navigation to splash.
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Navigates to the App Flow screen when user is already logged in.
     * Finishes this activity to prevent back navigation to splash.
     */
    private fun navigateToHome() {
        val intent = Intent(this, DefaultComponentAppFlowActivity::class.java)
        startActivity(intent)
        finish()
    }
}
