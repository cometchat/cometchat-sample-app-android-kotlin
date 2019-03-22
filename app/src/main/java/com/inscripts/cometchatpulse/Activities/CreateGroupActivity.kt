package com.inscripts.cometchatpulse.Activities

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.Group
import com.inscripts.cometchatpulse.Extensions.setTitleTypeface
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.Appearance
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.inscripts.cometchatpulse.ViewModel.GroupChatViewModel
import com.inscripts.cometchatpulse.ViewModel.GroupViewModel
import com.inscripts.cometchatpulse.databinding.ActivityCreateGroupBinding


class CreateGroupActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    private lateinit var binding: ActivityCreateGroupBinding

    private var groupType: String? = null

    private var guid: String? = null

    private var groupDescription:String?=null

    private var groupName: String? = null

    private var groupPassword: String? = null

    private lateinit var groupViewModel:GroupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_group)

        setSupportActionBar(binding.createGroupToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title=getString(R.string.create_group)

        CommonUtil.setStatusBarColor(this)

        binding.createGroupToolbar.setTitleTypeface(StringContract.Font.title)


        CommonUtil.setStatusBarColor(this)

         if (StringContract.AppDetails.theme==Appearance.AppTheme.AZURE_RADIANCE){
             binding.createGroupToolbar.setTitleTextColor(StringContract.Color.black)
         }
        else {
             binding.createGroupToolbar.setTitleTextColor(StringContract.Color.white)
         }

        binding.createGroupToolbar.setBackgroundColor(StringContract.Color.primaryColor)

        binding.createGroupToolbar.navigationIcon?.setColorFilter(StringContract.Color.iconTint,PorterDuff.Mode.SRC_ATOP)

        binding.spinner.onItemSelectedListener = this


        groupViewModel=ViewModelProviders.of(this).get(GroupViewModel::class.java)

        val adapter = ArrayAdapter.createFromResource(this,
                R.array.group_type, android.R.layout.simple_spinner_item)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.setAdapter(adapter)
        binding.editTextGuid.addTextChangedListener(guidWatcher)
        binding.editTextPassword.addTextChangedListener(passwordWatcher)

        //typeface
        binding.tvName.typeface=StringContract.Font.status
        binding.tvGuid.typeface=StringContract.Font.status
        binding.tvDescription.typeface=StringContract.Font.status
        binding.tvType.typeface=StringContract.Font.status

        binding.editTextDescription.typeface=StringContract.Font.name
        binding.editTextPassword.typeface=StringContract.Font.name
        binding.editTextName.typeface=StringContract.Font.name
        binding.editTextGuid.typeface=StringContract.Font.name


    }

    val guidWatcher:TextWatcher=object :TextWatcher{
        override fun afterTextChanged(p0: Editable?) {

        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.editTextGuid.setError(null)
        }

    }

    val passwordWatcher:TextWatcher=object :TextWatcher{
        override fun afterTextChanged(p0: Editable?) {

        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.editTextPassword.setError(null)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.create_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.menu_create_group) {
            guid = binding.editTextGuid.text?.toString()?.trim()
            groupPassword = binding.editTextPassword.text?.toString()?.trim()
            groupDescription=binding.editTextDescription.text?.toString()?.trim()
            groupName = binding.editTextName.text?.toString()?.trim()

            if (guid.isNullOrEmpty()) {
                binding.editTextGuid.error = getString(R.string.guid_empty_warning)

            }
            else if (groupType?.equals(CometChatConstants.GROUP_TYPE_PASSWORD, ignoreCase = true)!!
                    && groupPassword.isNullOrEmpty()) {
                binding.editTextPassword.error = getString(R.string.password_is_mandatory)

            }
            else if (groupType?.equals(getString(R.string.select_group_type),ignoreCase = true)!!)
            {
                Toast.makeText(this,getString(R.string.please_select_group_type),Toast.LENGTH_SHORT).show()
            }

            else if (guid != null && groupType != null && groupName != null) {

                if (groupType.equals(CometChatConstants.GROUP_TYPE_PASSWORD, ignoreCase = true)) {

                    if (groupPassword != null) {
                        val group = Group(guid, groupName, groupType.toString().toLowerCase(),
                                groupPassword, null, groupDescription)

                         groupViewModel.createGroup(this,group)

                    } /*else {
                            chatroomPasswordField.setText("");
                            chatroomPasswordField.setError(chatroomLang.get26());
                        }*/
                } else {
                    val group = Group(guid, groupName, groupType.toString().toLowerCase(), null,
                            null, groupDescription)
                    groupViewModel.createGroup(this,group)


                }


            }

        }
        return super.onOptionsItemSelected(item)
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

        groupType = p0?.getSelectedItem() as String

        if (groupType.equals(CometChatConstants.GROUP_TYPE_PASSWORD, ignoreCase = true)) {
            binding.llPasswordContainer.setVisibility(View.VISIBLE)
        } else {
            binding.llPasswordContainer.setVisibility(View.GONE)
        }
    }
}
