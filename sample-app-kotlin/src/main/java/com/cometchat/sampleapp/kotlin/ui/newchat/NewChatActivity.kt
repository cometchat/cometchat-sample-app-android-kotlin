package com.cometchat.sampleapp.kotlin.ui.newchat

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cometchat.chat.core.GroupsRequest.GroupsRequestBuilder
import com.cometchat.chat.core.UsersRequest.UsersRequestBuilder
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityNewChatBinding
import com.cometchat.sampleapp.kotlin.ui.messages.MessagesActivity
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.tabs.TabLayout

/**
 * Activity for starting new conversations with users or groups.
 *
 * This activity displays a tabbed interface with Users and Groups tabs,
 * allowing users to select a contact or group to start a new conversation.
 *
 * ## Features:
 * - Tabbed interface with Users and Groups tabs
 * - CometChatUsers component showing non-blocked users
 * - CometChatGroups component showing only joined groups
 * - Navigation to MessagesActivity on user/group selection
 * - Back button to close the activity
 *
 * ## Architecture:
 * - Uses ViewBinding for type-safe view access
 * - Uses CometChatUsers and CometChatGroups components from chatuikit-kotlin
 * - Configures request builders to filter users and groups appropriately
 *
 * @see com.cometchat.uikit.kotlin.presentation.users.ui.CometChatUsers
 * @see com.cometchat.uikit.kotlin.presentation.groups.ui.CometChatGroups
 *
 * Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6
 */
class NewChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityNewChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupToolbar()
        setupTabs()
        setupUsers()
        setupGroups()
    }

    /**
     * Applies system window insets padding to avoid overlap with system bars.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Sets up the toolbar with back button and title.
     *
     * Validates: Requirement 7.6 (back button closes activity)
     */
    private fun setupToolbar() {
        // Apply theme colors
        binding.tvTitle.setTextColor(CometChatTheme.getTextColorPrimary(this))

        // Handle back button click
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Sets up the tab layout with Users and Groups tabs.
     *
     * Validates: Requirement 7.1 (tabbed interface with Users and Groups tabs)
     */
    private fun setupTabs() {
        // Apply theme colors to tabs
        binding.tabLayout.setTabTextColors(
            CometChatTheme.getTextColorSecondary(this),
            CometChatTheme.getPrimaryColor(this)
        )

        // Add tabs
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(getString(R.string.tab_users))
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(getString(R.string.tab_groups))
        )

        // Select first tab by default
        binding.tabLayout.getTabAt(0)?.select()

        // Handle tab selection
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        // Show Users, hide Groups
                        binding.users.visibility = View.VISIBLE
                        binding.groups.visibility = View.GONE
                    }
                    1 -> {
                        // Show Groups, hide Users
                        binding.users.visibility = View.GONE
                        binding.groups.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // No action needed
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // No action needed
            }
        })
    }

    /**
     * Sets up the CometChatUsers component.
     *
     * Configures the users list to:
     * - Hide blocked users (Requirement 7.2)
     * - Hide the toolbar (using activity's toolbar instead)
     * - Navigate to MessagesActivity on user selection (Requirement 7.4)
     *
     * Validates: Requirements 7.2, 7.4
     */
    private fun setupUsers() {
        binding.users.apply {
            // Hide component's built-in toolbar
            setToolbarVisibility(View.GONE)
            setSeparatorVisibility(View.GONE)

            // Configure request builder to hide blocked users
            setUsersRequestBuilder(
                UsersRequestBuilder()
                    .hideBlockedUsers(true)
                    .setLimit(30)
            )

            // Handle user selection
            setOnItemClick { user ->
                navigateToMessages(user)
            }
        }
    }

    /**
     * Sets up the CometChatGroups component.
     *
     * Configures the groups list to:
     * - Show only joined groups (Requirement 7.3)
     * - Hide the toolbar (using activity's toolbar instead)
     * - Navigate to MessagesActivity on group selection (Requirement 7.5)
     *
     * Validates: Requirements 7.3, 7.5
     */
    private fun setupGroups() {
        binding.groups.apply {
            // Hide component's built-in toolbar
            setToolbarVisibility(View.GONE)
            setSeparatorVisibility(View.GONE)

            // Configure request builder to show only joined groups
            setGroupsRequestBuilder(
                GroupsRequestBuilder()
                    .joinedOnly(true)
                    .setLimit(30)
            )

            // Handle group selection
            setOnItemClick { group ->
                navigateToMessages(group)
            }

            // Initially hidden (Users tab is selected by default)
            visibility = View.GONE
        }
    }

    /**
     * Navigates to MessagesActivity with the selected user.
     *
     * @param user The selected user to start a conversation with
     *
     * Validates: Requirement 7.4
     */
    private fun navigateToMessages(user: User) {
        MessagesActivity.start(this, user = user)
        finish()
    }

    /**
     * Navigates to MessagesActivity with the selected group.
     *
     * @param group The selected group to start a conversation in
     *
     * Validates: Requirement 7.5
     */
    private fun navigateToMessages(group: Group) {
        MessagesActivity.start(this, group = group)
        finish()
    }
}
