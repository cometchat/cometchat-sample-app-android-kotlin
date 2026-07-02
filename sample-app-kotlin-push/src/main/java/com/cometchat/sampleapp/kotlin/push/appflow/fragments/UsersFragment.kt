package com.cometchat.sampleapp.kotlin.push.appflow.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cometchat.sampleapp.kotlin.push.R
import com.cometchat.sampleapp.kotlin.push.appflow.MessagesActivity
import com.cometchat.sampleapp.kotlin.push.databinding.FragmentUsersBinding
import com.google.gson.Gson

/**
 * Fragment displaying the CometChatUsers component.
 * Mirrors the UsersFragment from master-app-kotlin.
 */
class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "UsersFragment"
    }

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
        
        // Set up user click listener to navigate to MessagesActivity
        binding.users.setOnItemClick { user ->
            Log.d(TAG, "Navigating to messages with user: ${user.name}")
            val intent = Intent(requireActivity(), MessagesActivity::class.java)
            intent.putExtra(getString(R.string.app_user), Gson().toJson(user))
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
