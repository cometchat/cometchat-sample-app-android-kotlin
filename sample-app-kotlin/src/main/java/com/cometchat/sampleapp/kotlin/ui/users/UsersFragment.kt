package com.cometchat.sampleapp.kotlin.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.databinding.FragmentUsersBinding
import com.cometchat.sampleapp.kotlin.ui.messages.MessagesActivity

/**
 * Fragment displaying the list of users (Users tab).
 *
 * This fragment uses the CometChatUsers component from chatuikit-kotlin
 * to display all available users. It handles:
 * - Displaying user list with avatars, names, and online status
 * - Alphabetical section headers
 * - Search functionality to filter users
 * - Navigation to message screen on user tap
 * - Loading and empty states
 *
 * ## Architecture:
 * - Uses ViewBinding for type-safe view access
 * - Delegates business logic to CometChatUsers component
 * - The component internally uses CometChatUsersViewModel from chatuikit-core
 *
 * @see com.cometchat.uikit.kotlin.presentation.users.ui.CometChatUsers
 *
 * Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7
 */
class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUsersList()
    }

    /**
     * Sets up the CometChatUsers component.
     *
     * Configures click handlers and customizations for the users list.
     */
    private fun setupUsersList() {
        binding.usersList.apply {
            // Set click handler for user items
            setOnItemClick { user ->
                navigateToMessages(user)
            }
        }
    }

    /**
     * Navigates to the Messages screen for the selected user.
     *
     * @param user The selected user
     *
     * Validates: Requirements 8.5
     */
    private fun navigateToMessages(user: User) {
        MessagesActivity.start(requireContext(), user = user)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
