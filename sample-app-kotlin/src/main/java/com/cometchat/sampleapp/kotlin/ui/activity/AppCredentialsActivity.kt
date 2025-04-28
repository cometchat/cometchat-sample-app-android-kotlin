package com.cometchat.sampleapp.kotlin.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.chatuikit.CometChatTheme
import com.cometchat.chatuikit.shared.resources.localise.CometChatLocalize
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.databinding.ActivityAppCredentialsBinding
import com.cometchat.sampleapp.kotlin.viewmodels.AppCredentialsViewModel
import com.google.android.material.card.MaterialCardView

class AppCredentialsActivity : AppCompatActivity() {
    private var binding: ActivityAppCredentialsBinding? = null
    private var viewModel: AppCredentialsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppCredentialsBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)

        initViewModel()

        initClickListeners()
    }

    private fun initViewModel() {
        viewModel = AppCredentialsViewModel()
    }

    private fun initClickListeners() {
        binding!!.btnContinue.setOnClickListener { v: View? ->
            if (viewModel!!.selectedRegion.value == null) {
                Toast
                    .makeText(
                        this, R.string.app_please_select_app_region, Toast.LENGTH_SHORT
                    )
                    .show()
            } else if (binding!!.etAppId.text
                    .toString()
                    .isEmpty()
            ) {
                Toast
                    .makeText(this, R.string.app_invalid_app_id, Toast.LENGTH_SHORT)
                    .show()
            } else if (binding!!.etAuthKey.text
                    .toString()
                    .isEmpty()
            ) {
                Toast
                    .makeText(
                        this, R.string.app_invalid_auth_token, Toast.LENGTH_SHORT
                    )
                    .show()
            } else {
                viewModel!!.initUIKit(
                    this, binding!!.etAppId.text.toString(), binding!!.etAuthKey.text.toString()
                )
            }
        }

        binding!!.cardUs.setOnClickListener { v: View? ->
            viewModel!!.setSelectedRegion(getString(R.string.app_region_us).lowercase(CometChatLocalize.getDefault()))
            regionCardUIHandler(binding!!.cardUs, binding!!.cardEu, binding!!.cardIn)
        }

        binding!!.cardEu.setOnClickListener { v: View? ->
            viewModel!!.setSelectedRegion(getString(R.string.app_region_eu).lowercase(CometChatLocalize.getDefault()))
            regionCardUIHandler(binding!!.cardEu, binding!!.cardUs, binding!!.cardIn)
        }

        binding!!.cardIn.setOnClickListener { v: View? ->
            viewModel!!.setSelectedRegion(getString(R.string.app_region_in).lowercase(CometChatLocalize.getDefault()))
            regionCardUIHandler(binding!!.cardIn, binding!!.cardEu, binding!!.cardUs)
        }
    }

    private fun regionCardUIHandler(
        selected: MaterialCardView,
        unselected1: MaterialCardView,
        unselected2: MaterialCardView
    ) {
        selected.strokeColor = CometChatTheme.getStrokeColorHighlight(this)
        selected.setCardBackgroundColor(CometChatTheme.getExtendedPrimaryColor50(this))

        unselected1.strokeColor = CometChatTheme.getStrokeColorDefault(this)
        unselected1.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(this))

        unselected2.strokeColor = CometChatTheme.getStrokeColorDefault(this)
        unselected2.setCardBackgroundColor(CometChatTheme.getBackgroundColor1(this))
    }
}
