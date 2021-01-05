package screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.uikit.R
import constant.StringContract
import org.json.JSONException
import org.json.JSONObject
import utils.Extensions
import java.util.*

class CometChatReactionInfoScreenActivity : AppCompatActivity() {
    private lateinit var reactionInfoLayout: LinearLayout

    private lateinit var jsonObject: JSONObject

    private lateinit var closeBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reaction_info_screen)
        reactionInfoLayout = findViewById(R.id.reaction_info_layout)
        closeBtn = findViewById(R.id.close_btn)
        closeBtn.setOnClickListener { view: View? -> onBackPressed() }
        if (intent.hasExtra(StringContract.IntentStrings.REACTION_INFO)) {
            try {
                jsonObject = JSONObject(intent.getStringExtra(StringContract.IntentStrings.REACTION_INFO))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val reactionInfo: HashMap<String, List<String>> = Extensions.getReactionsInfo(jsonObject)
        for ((k,v) in reactionInfo) {
            val view: View = LayoutInflater.from(this).inflate(R.layout.reaction_info_row, null)
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