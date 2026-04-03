package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ScaleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject

/**
 * RecyclerView adapter for displaying poll answer options.
 *
 * Each option row contains:
 * - Radio button (MaterialCardView styled as circle)
 * - Option text
 * - ImageAndCountView (voter avatars + count)
 * - Progress bar showing vote percentage
 */
@SuppressLint("NotifyDataSetChanged")
class PollAnswerAdapter(
    private val onOptionClick: OnOptionClick
) : RecyclerView.Adapter<PollAnswerAdapter.ViewHolder>() {

    private var customMessage: CustomMessage? = null
    private var optionsJson: JSONObject = JSONObject()
    private var myChosenOptionPosition: Int = -1
    private val options: MutableList<String> = mutableListOf()

    // Style properties
    @ColorInt private var progressColor: Int = 0
    @ColorInt private var progressBackgroundColor: Int = 0
    private var selectedStateDrawable: Drawable? = null
    private var unselectedStateDrawable: Drawable? = null
    @ColorInt private var voteCountTextColor: Int = 0
    @ColorInt private var selectedIconTint: Int = 0
    @ColorInt private var selectedRadioButtonStrokeColor: Int = 0
    @Dimension private var selectedRadioButtonCornerRadius: Int = 0
    @Dimension private var selectedRadioButtonStrokeWidth: Int = 0
    @ColorInt private var unselectedRadioButtonStrokeColor: Int = 0
    @ColorInt private var unselectedIconTint: Int = 0
    @Dimension private var unselectedRadioButtonCornerRadius: Int = 0
    @Dimension private var unselectedRadioButtonStrokeWidth: Int = 0
    @StyleRes private var optionAvatarStyle: Int = 0
    @ColorInt private var optionTextColor: Int = 0
    @StyleRes private var optionTextAppearance: Int = 0
    @StyleRes private var voteCountTextAppearance: Int = 0
    @ColorInt private var progressIndeterminateTint: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_poll_answer_row_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.bind(options[position], position)
    }

    override fun getItemCount(): Int = options.size

    fun setMessage(baseMessage: CustomMessage?) {
        if (baseMessage != null) {
            options.clear()
            this.customMessage = baseMessage
            try {
                val jsonObject = baseMessage.customData
                optionsJson = jsonObject.getJSONObject("options")
                for (i in 1..optionsJson.length()) {
                    options.add(optionsJson.getString(i.toString()))
                }
                notifyDataSetChanged()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun setMyChosenOptionPosition(position: Int) {
        this.myChosenOptionPosition = position
        notifyDataSetChanged()
    }

    /**
     * Gets the reactors (voters) for a specific option position.
     */
    private fun getReactors(position: Int): List<ImageTextPoJo> {
        val list = mutableListOf<ImageTextPoJo>()
        try {
            val result = getPollsResult(customMessage)
            if (result.has("options")) {
                val options = result.getJSONObject("options")
                val optionKey = (position + 1).toString()
                if (options.has(optionKey)) {
                    val optionK = options.getJSONObject(optionKey)
                    if (optionK.has("voters")) {
                        val voters = optionK.getJSONObject("voters")
                        var loopSize = 0
                        val keys = voters.keys()
                        while (keys.hasNext() && loopSize < 3) {
                            val key = keys.next()
                            val voter = voters.getJSONObject(key)
                            val imageTextPoJo = ImageTextPoJo()
                            if (voter.has("avatar")) {
                                imageTextPoJo.imageUrl = voter.getString("avatar")
                            }
                            if (voter.has("name")) {
                                imageTextPoJo.text = voter.getString("name")
                            }
                            list.add(imageTextPoJo)
                            loopSize++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return list
    }

    /**
     * Gets the polls result from message metadata.
     */
    private fun getPollsResult(message: CustomMessage?): JSONObject {
        try {
            val metadata = message?.metadata ?: return JSONObject()
            if (!metadata.has("@injected")) return JSONObject()
            val injected = metadata.getJSONObject("@injected")
            if (!injected.has("extensions")) return JSONObject()
            val extensions = injected.getJSONObject("extensions")
            if (!extensions.has("polls")) return JSONObject()
            val polls = extensions.getJSONObject("polls")
            if (!polls.has("results")) return JSONObject()
            return polls.getJSONObject("results")
        } catch (e: Exception) {
            return JSONObject()
        }
    }

    /**
     * Gets the total vote count for the poll.
     */
    private fun getVoteCount(): Int {
        return try {
            val result = getPollsResult(customMessage)
            result.optInt("total", 0)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Gets the voter info for all options.
     */
    private fun getVoterInfo(optionsCount: Int): List<String> {
        val voterInfo = mutableListOf<String>()
        try {
            val result = getPollsResult(customMessage)
            if (result.has("options")) {
                val options = result.getJSONObject("options")
                for (i in 1..optionsCount) {
                    val optionKey = i.toString()
                    if (options.has(optionKey)) {
                        val option = options.getJSONObject(optionKey)
                        voterInfo.add(option.optInt("count", 0).toString())
                    } else {
                        voterInfo.add("0")
                    }
                }
            } else {
                for (i in 1..optionsCount) {
                    voterInfo.add("0")
                }
            }
        } catch (e: Exception) {
            for (i in 1..optionsCount) {
                voterInfo.add("0")
            }
        }
        return voterInfo
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val radioButton: MaterialCardView = itemView.findViewById(R.id.check_button)
        private val imageAndCountView: ImageAndCountView = itemView.findViewById(R.id.image_and_count_view)
        private val optionProgressIndicator: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val progressIndicator: ProgressBar = itemView.findViewById(R.id.progress_indicator)
        private val optionText: TextView = itemView.findViewById(R.id.option_text)

        private val progressBackgroundDrawable: GradientDrawable?
        private val progressLayerDrawable: ScaleDrawable?

        init {
            Utils.initMaterialCard(radioButton)
            val progressDrawable = optionProgressIndicator.progressDrawable as? LayerDrawable
            progressBackgroundDrawable = progressDrawable?.findDrawableByLayerId(R.id.background) as? GradientDrawable
            progressLayerDrawable = progressDrawable?.findDrawableByLayerId(android.R.id.progress) as? ScaleDrawable
        }

        fun bind(option: String, position: Int) {
            optionText.text = option
            radioButton.tag = option
            radioButton.visibility = View.VISIBLE
            progressIndicator.visibility = View.GONE

            val voteCount = getVoteCount()
            val voterInfo = getVoterInfo(optionsJson.length())
            val votedOnOption = voterInfo.getOrNull(position)?.toIntOrNull() ?: 0

            applyStyle()

            if (votedOnOption == 0) {
                imageAndCountView.visibility = View.GONE
            } else {
                imageAndCountView.visibility = View.VISIBLE
                imageAndCountView.setData(getReactors(position), votedOnOption)
            }

            val percentage = if (voteCount != 0) {
                Math.round((votedOnOption.toFloat() * 100) / voteCount)
            } else {
                0
            }
            optionProgressIndicator.progress = percentage

            if (myChosenOptionPosition > -1) {
                if (myChosenOptionPosition == position) {
                    applySelectedStyle()
                } else {
                    applyUnselectedStyle()
                }
            }

            radioButton.setOnClickListener {
                if (myChosenOptionPosition != position) {
                    myChosenOptionPosition = position
                    val message = customMessage
                    if (message != null) {
                        onOptionClick.onClick(message, options[position], position)
                    }
                    radioButton.visibility = View.GONE
                    progressIndicator.visibility = View.VISIBLE
                }
            }
        }

        private fun applyStyle() {
            if (optionTextAppearance != 0) {
                optionText.setTextAppearance(optionTextAppearance)
            }
            if (optionTextColor != 0) {
                optionText.setTextColor(optionTextColor)
            }
            imageAndCountView.setAvatarStyle(optionAvatarStyle)
            if (voteCountTextAppearance != 0) {
                imageAndCountView.setCountTextAppearance(voteCountTextAppearance)
            }
            if (voteCountTextColor != 0) {
                imageAndCountView.setCountTextColor(voteCountTextColor)
            }
            progressBackgroundDrawable?.setColor(progressBackgroundColor)
            progressLayerDrawable?.setColorFilter(progressColor, PorterDuff.Mode.SRC_IN)
            progressIndicator.indeterminateDrawable?.setColorFilter(
                progressIndeterminateTint,
                PorterDuff.Mode.SRC_IN
            )
            applyUnselectedStyle()
        }

        private fun applyUnselectedStyle() {
            radioButton.background = unselectedStateDrawable
            if (unselectedIconTint != 0) {
                radioButton.backgroundTintList = ColorStateList.valueOf(unselectedIconTint)
            }
            radioButton.strokeColor = unselectedRadioButtonStrokeColor
            radioButton.strokeWidth = unselectedRadioButtonStrokeWidth
            if (unselectedRadioButtonCornerRadius > 0) {
                radioButton.radius = unselectedRadioButtonCornerRadius.toFloat()
            }
        }

        private fun applySelectedStyle() {
            radioButton.background = selectedStateDrawable
            if (selectedIconTint != 0) {
                radioButton.backgroundTintList = ColorStateList.valueOf(selectedIconTint)
            }
            if (selectedRadioButtonStrokeColor != 0) {
                radioButton.strokeColor = selectedRadioButtonStrokeColor
            }
            radioButton.strokeWidth = selectedRadioButtonStrokeWidth
            if (selectedRadioButtonCornerRadius > 0) {
                radioButton.radius = selectedRadioButtonCornerRadius.toFloat()
            }
        }
    }

    // Style setters
    fun setProgressColor(@ColorInt color: Int) {
        this.progressColor = color
    }

    fun setProgressBackgroundColor(@ColorInt color: Int) {
        this.progressBackgroundColor = color
    }

    fun setSelectedStateDrawable(drawable: Drawable?) {
        this.selectedStateDrawable = drawable
    }

    fun setUnselectedStateDrawable(drawable: Drawable?) {
        this.unselectedStateDrawable = drawable
    }

    fun setSelectedIconTint(@ColorInt color: Int) {
        this.selectedIconTint = color
    }

    fun setSelectedRadioButtonStrokeColor(@ColorInt color: Int) {
        this.selectedRadioButtonStrokeColor = color
    }

    fun setVoteCountTextColor(@ColorInt color: Int) {
        this.voteCountTextColor = color
    }

    fun setSelectedRadioButtonCornerRadius(@Dimension radius: Int) {
        this.selectedRadioButtonCornerRadius = radius
    }

    fun setSelectedRadioButtonStrokeWidth(@Dimension width: Int) {
        this.selectedRadioButtonStrokeWidth = width
    }

    fun setOptionAvatarStyle(@StyleRes style: Int) {
        this.optionAvatarStyle = style
    }

    fun setOptionTextColor(@ColorInt color: Int) {
        this.optionTextColor = color
    }

    fun setOptionTextAppearance(@StyleRes appearance: Int) {
        this.optionTextAppearance = appearance
    }

    fun setVoteCountTextAppearance(@StyleRes appearance: Int) {
        this.voteCountTextAppearance = appearance
    }

    fun setUnselectedIconTint(@ColorInt color: Int) {
        this.unselectedIconTint = color
    }

    fun setUnselectedRadioButtonStrokeColor(@ColorInt color: Int) {
        this.unselectedRadioButtonStrokeColor = color
    }

    fun setUnselectedRadioButtonCornerRadius(@Dimension radius: Int) {
        this.unselectedRadioButtonCornerRadius = radius
    }

    fun setUnselectedRadioButtonStrokeWidth(@Dimension width: Int) {
        this.unselectedRadioButtonStrokeWidth = width
    }

    fun setProgressIndeterminateTint(@ColorInt color: Int) {
        this.progressIndeterminateTint = color
    }
}
