package com.cometchat.pro.uikit.ui_components.messages.extensions.message_reaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import org.json.JSONException
import org.json.JSONObject
import com.cometchat.pro.uikit.ui_components.messages.extensions.Extensions
import java.util.*

class CometChatReactionInfoActivity : AppCompatActivity() {
    private lateinit var reactionInfoLayout: LinearLayout

    private lateinit var jsonObject: JSONObject

    private lateinit var closeBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_reaction_info)
        reactionInfoLayout = findViewById(R.id.reaction_info_layout)
        closeBtn = findViewById(R.id.close_btn)
        closeBtn.setOnClickListener { view: View? -> onBackPressed() }
        if (intent.hasExtra(UIKitConstants.IntentStrings.REACTION_INFO)) {
            try {
                jsonObject = JSONObject(intent.getStringExtra(UIKitConstants.IntentStrings.REACTION_INFO))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val reactionInfo: HashMap<String, List<String>> = Extensions.getReactionsInfo(jsonObject)
        for ((k,v) in reactionInfo) {
            val view: View = LayoutInflater.from(this).inflate(R.layout.reaction_info_item, null)
            val react :TextView = view.findViewById(R.id.react_tv)
            val users :TextView = view.findViewById(R.id.users_tv)
            react.text = k
            val usernames = reactionInfo[k]
            for (uname in usernames!!) {
                if (users.text.toString().trim { it <= ' ' }.isEmpty()) users.text = uname else users.text = users.text.toString() + ", " + uname
            }
            reactionInfoLayout.addView(view)
        }
    }
}