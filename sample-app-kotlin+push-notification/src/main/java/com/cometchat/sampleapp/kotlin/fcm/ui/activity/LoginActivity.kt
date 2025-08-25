package com.cometchat.sampleapp.kotlin.fcm.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.fcm.R
import com.cometchat.sampleapp.kotlin.fcm.databinding.ActivityLoginBinding
import com.cometchat.sampleapp.kotlin.fcm.ui.adapters.SampleUsersAdapter
import com.cometchat.sampleapp.kotlin.fcm.viewmodels.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private var sampleUsersAdapter: SampleUsersAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()
        initRecyclerView()
        initViewModel()
        initClickListeners()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }

    private fun initRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this, 3) // 3 columns
        binding.recyclerView.layoutManager = gridLayoutManager
        sampleUsersAdapter = SampleUsersAdapter(ArrayList()) { user: User? ->
            binding.etUid.setText("")
            viewModel.selectedUser(user!!)
        }
        binding.recyclerView.adapter = sampleUsersAdapter
    }

    private fun initViewModel() {
        viewModel = LoginViewModel()
        viewModel.sampleUsers

        viewModel.loginStatus.observe(this, onUserLogin())
        viewModel.users.observe(this, onSampleUserListUpdate())
        viewModel.onError().observe(this, onError())
    }

    private fun initClickListeners() {
        binding.btnContinue.setOnClickListener { v: View? ->
            if (viewModel.selectedUser.value == null && binding.etUid.text.toString().isEmpty()) {
                Toast.makeText(
                    this@LoginActivity, ContextCompat.getString(
                        this@LoginActivity, R.string.app_error_invalid_user_or_uid_login
                    ), Toast.LENGTH_SHORT
                ).show()
            } else {
                val uid = if (viewModel.selectedUser.value != null) {
                    viewModel.selectedUser.value!!.uid
                } else {
                    binding.etUid.text.toString()
                }
                viewModel.login(uid)
            }
        }

        binding.etUid.onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                viewModel.selectedUser(null)
                sampleUsersAdapter!!.clearSelection()
            }
        }

        binding.viewChangeAppCredentials.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this@LoginActivity, AppCredentialsActivity::class.java
                )
            )
        }
    }

    private fun onSampleUserListUpdate(): Observer<List<User>> {
        return Observer { users: List<User> ->
            sampleUsersAdapter!!.updateList(users)
            if (users.isEmpty()) {
                binding.tvSubtitle.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.viewDivider.visibility = View.GONE
            } else {
                binding.tvSubtitle.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                binding.viewDivider.visibility = View.VISIBLE
            }
        }
    }

    private fun onUserLogin(): Observer<Boolean> {
        return Observer { isLoggedIn: Boolean? ->
            if (isLoggedIn != null) {
                if (isLoggedIn) {
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity, ContextCompat.getString(
                            this@LoginActivity, R.string.app_error_invalid_user_or_uid_login
                        ), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun onError(): Observer<CometChatException> {
        return Observer { e: CometChatException ->
            Toast.makeText(
                this, e.message, Toast.LENGTH_SHORT
            ).show()
        }
    }
}
