package com.cometchat.sampleapp.kotlin.ui.home

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityHomeBinding
import com.cometchat.sampleapp.kotlin.ui.calls.CallsFragment
import com.cometchat.sampleapp.kotlin.ui.chats.ChatsFragment
import com.cometchat.sampleapp.kotlin.ui.groups.GroupsFragment
import com.cometchat.sampleapp.kotlin.ui.login.LoginActivity
import com.cometchat.sampleapp.kotlin.ui.newchat.NewChatActivity
import com.cometchat.sampleapp.kotlin.ui.users.UsersFragment
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import kotlinx.coroutines.launch

/**
 * Home screen activity displayed after successful login.
 * Shows bottom navigation with Chats, Calls, Users, Groups tabs.
 * Matches master-app-kotlin flow (no intermediate App Flow card).
 *
 * Features:
 * - Authentication guard in onCreate and onResume
 * - Bottom navigation with Chats, Calls, Users, Groups
 * - Fragment-based navigation
 * - Logout functionality via ViewModel
 *
 * Note: No notification permission request (excluded per user requirement).
 * Note: No VoIP, FCM, Google Login, or showcase features (excluded).
 *
 * Validates: Requirements 2.2, 3.5
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private var currentFragment = R.id.nav_chats // Default to the Chats fragment

    companion object {
        private const val SELECTED_FRAGMENT_KEY = "selected_fragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Authentication guard - redirect to login if not authenticated
        if (!checkAuthentication()) {
            return
        }

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        applyWindowInsets()

        // Restore saved state
        if (savedInstanceState != null) {
            currentFragment = savedInstanceState.getInt(SELECTED_FRAGMENT_KEY, R.id.nav_chats)
        }

        // Set the selected item in the bottom navigation to match the current fragment
        binding.bottomNavigationView.selectedItemId = currentFragment

        configureBottomNavigation()
        observeState()

        // Load the initial fragment
        loadFragment(getFragment(currentFragment))
    }

    override fun onResume() {
        super.onResume()
        // Re-check authentication on resume (user session may have become invalid)
        checkAuthentication()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_FRAGMENT_KEY, currentFragment)
    }

    /**
     * Checks if user is authenticated. If not, navigates to LoginActivity.
     *
     * @return true if user is authenticated, false otherwise
     */
    private fun checkAuthentication(): Boolean {
        val loggedInUser: User? = CometChatUIKit.getLoggedInUser()
        if (loggedInUser == null) {
            navigateToLogin()
            finish()
            return false
        }
        return true
    }

    /**
     * Applies system window insets padding to avoid overlap with system bars.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    /**
     * Configures the bottom navigation view and its item selection listener.
     * Updates the displayed fragment based on user selection.
     */
    private fun configureBottomNavigation() {
        // Create a ColorStateList for icon and text color based on the checked state
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                CometChatTheme.getIconTintHighlight(this),
                CometChatTheme.getIconTintSecondary(this)
            )
        )

        binding.bottomNavigationView.itemIconTintList = colorStateList
        binding.bottomNavigationView.itemTextColor = colorStateList

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            if (currentFragment == item.itemId) {
                return@setOnItemSelectedListener true // No action needed if the fragment is already selected
            }
            currentFragment = item.itemId
            loadFragment(getFragment(currentFragment))
            true
        }
    }

    /**
     * Loads the specified fragment into the fragment container.
     *
     * @param fragment The fragment to be loaded.
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    /**
     * Returns the appropriate fragment based on the selected menu item ID.
     *
     * @param itemId The selected menu item ID.
     * @return The corresponding fragment.
     */
    private fun getFragment(itemId: Int): Fragment {
        return when (itemId) {
            R.id.nav_chats -> ChatsFragment()
            R.id.nav_calls -> CallsFragment()
            R.id.nav_users -> UsersFragment()
            R.id.nav_groups -> GroupsFragment()
            else -> ChatsFragment()
        }
    }

    /**
     * Observes ViewModel state changes and handles navigation events.
     */
    private fun observeState() {
        // Observe logout state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutState.collect { state ->
                    handleLogoutState(state)
                }
            }
        }

        // Observe navigation events
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    handleNavigationEvent(event)
                }
            }
        }
    }

    /**
     * Handles logout state changes.
     *
     * @param state The current logout state
     */
    private fun handleLogoutState(state: LogoutState) {
        when (state) {
            is LogoutState.Idle -> {
                showLoading(false)
            }
            is LogoutState.Loading -> {
                showLoading(true)
            }
            is LogoutState.Success -> {
                showLoading(false)
                // Navigation is handled by navigationEvent
            }
            is LogoutState.Error -> {
                showLoading(false)
                // Even on error, we navigate to login (session is cleared)
            }
        }
    }

    /**
     * Handles navigation events from the ViewModel.
     *
     * @param event The navigation event
     */
    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.ToLogin -> {
                navigateToLogin()
            }
        }
    }

    /**
     * Shows or hides the loading overlay.
     *
     * @param show True to show loading, false to hide
     */
    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * Navigates to NewChatActivity to start a new conversation.
     * Called from ChatsFragment FAB click.
     */
    fun navigateToNewChat() {
        val intent = Intent(this, NewChatActivity::class.java)
        startActivity(intent)
    }

    /**
     * Navigates to LoginActivity when user is not authenticated or after logout.
     * Clears the back stack to prevent returning to protected screens.
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    /**
     * Triggers logout via the ViewModel.
     * Can be called from fragments or menu items.
     */
    fun logout() {
        viewModel.logout()
    }
}
