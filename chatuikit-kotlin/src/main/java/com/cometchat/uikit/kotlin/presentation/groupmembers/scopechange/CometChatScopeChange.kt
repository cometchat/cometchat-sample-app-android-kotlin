package com.cometchat.uikit.kotlin.presentation.groupmembers.scopechange

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatScopeChangeLayoutBinding
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

/**
 * CometChatScopeChange is a self-contained MaterialCardView component for changing
 * a group member's scope/role. Mirrors the original Java CometChatScopeChange.
 *
 * Features:
 * - XML attribute-based theming via [R.styleable.CometChatScopeChange]
 * - [ScopeAdapter] with radio buttons (disabled admin option for moderators)
 * - Direct SDK call via [CometChat.updateGroupMemberScope]
 * - Progress indicator on save button during SDK call
 * - Top-only rounded corners for bottom sheet usage
 */
class CometChatScopeChange @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.cometchatScopeChangeStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatScopeChange::class.java.simpleName
    }

    private val binding: CometchatScopeChangeLayoutBinding
    private val scopeAdapter: ScopeAdapter

    private var onNegativeButtonClick: (() -> Unit)? = null
    private var onPositiveButtonClick: (() -> Unit)? = null
    private var scopeChangeCallback: CometChat.CallbackListener<GroupMember>? = null

    private var group: Group? = null
    private var groupMember: GroupMember? = null

    // Style fields — mirrors Java CometChatScopeChange exactly
    @StyleRes private var titleTextAppearance: Int = 0
    @StyleRes private var subtitleTextAppearance: Int = 0
    @ColorInt private var titleColor: Int = 0
    @ColorInt private var subtitleColor: Int = 0
    private var scopeIcon: Drawable? = null
    @ColorInt private var iconTint: Int = 0
    @ColorInt private var bgColor: Int = 0
    @Dimension private var cRadius: Int = 0
    @ColorInt private var itemTextColor: Int = 0
    @ColorInt private var disableItemTextColor: Int = 0
    @StyleRes private var itemTextAppearance: Int = 0
    @ColorInt private var itemRadioButtonTint: Int = 0
    @ColorInt private var itemDisableRadioButtonTint: Int = 0
    @ColorInt private var dragHandleColor: Int = 0
    @ColorInt private var positiveButtonTextColor: Int = 0
    @ColorInt private var negativeButtonTextColor: Int = 0
    @StyleRes private var buttonTextAppearance: Int = 0
    @ColorInt private var positiveButtonBackgroundColor: Int = 0
    @ColorInt private var negativeButtonBackgroundColor: Int = 0

    init {
        Utils.initMaterialCard(this)
        binding = CometchatScopeChangeLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        scopeAdapter = ScopeAdapter(context, null)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = scopeAdapter
        applyStyleAttributes(attrs, defStyleAttr, 0)

        binding.cometchatScopeChangeCancelButton.setOnClickListener {
            onNegativeButtonClick?.invoke()
        }
        binding.cometchatScopeChangeSaveButton.setOnClickListener {
            if (onPositiveButtonClick != null) {
                onPositiveButtonClick?.invoke()
            } else {
                changeScope(scopeAdapter.getSelectedRole())
            }
        }
    }

    // ==================== Style Attributes ====================

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatScopeChange, defStyleAttr, defStyleRes
        )
        @StyleRes val style = typedArray.getResourceId(
            R.styleable.CometChatScopeChange_cometchatScopeChangeStyle, 0
        )
        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatScopeChange, defStyleAttr, style
        )
        extractAttributesAndApplyDefaults(typedArray)
    }

    private fun extractAttributesAndApplyDefaults(typedArray: TypedArray) {
        try {
            setTitleTextAppearance(typedArray.getResourceId(
                R.styleable.CometChatScopeChange_cometchatScopeChangeTitleTextAppearance, 0))
            setSubtitleTextAppearance(typedArray.getResourceId(
                R.styleable.CometChatScopeChange_cometchatScopeChangeSubtitleTextAppearance, 0))
            setTitleColor(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeTitleColor,
                CometChatTheme.getTextColorPrimary(context)))
            setSubtitleColor(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeSubtitleColor,
                CometChatTheme.getTextColorSecondary(context)))
            typedArray.getDrawable(R.styleable.CometChatScopeChange_cometchatScopeChangeIcon)?.let {
                setScopeIcon(it)
            }
            setIconTint(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeIconTint,
                CometChatTheme.getPrimaryColor(context)))
            setCardBackgroundColor(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeBackgroundColor,
                CometChatTheme.getBackgroundColor1(context)))
            setCornerRadius(typedArray.getDimensionPixelSize(
                R.styleable.CometChatScopeChange_cometchatScopeChangeCornerRadius, 0))
            strokeWidth = typedArray.getDimensionPixelSize(
                R.styleable.CometChatScopeChange_cometchatScopeChangeStrokeWidth, 0)
            strokeColor = typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeStrokeColor,
                CometChatTheme.getStrokeColorLight(context))
            setItemTextColor(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeItemTextColor,
                CometChatTheme.getTextColorPrimary(context)))
            setItemTextAppearance(typedArray.getResourceId(
                R.styleable.CometChatScopeChange_cometchatScopeChangeItemTextAppearance, 0))
            setItemRadioButtonTint(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeItemRadioButtonTint,
                CometChatTheme.getPrimaryColor(context)))
            setItemDisableRadioButtonTint(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeItemDisableRadioButtonTint,
                CometChatTheme.getStrokeColorDefault(context)))
            setDisableItemTextColor(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeDisableItemTextColor,
                CometChatTheme.getTextColorSecondary(context)))
            setDragHandleColor(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeDragHandleColor,
                CometChatTheme.getNeutralColor500(context)))
            setPositiveButtonTextColor(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangePositiveButtonTextColor,
                CometChatTheme.getColorWhite(context)))
            setNegativeButtonTextColor(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeNegativeButtonTextColor,
                CometChatTheme.getPrimaryColor(context)))
            setButtonTextAppearance(typedArray.getResourceId(
                R.styleable.CometChatScopeChange_cometchatScopeChangeButtonTextAppearance, 0))
            setPositiveButtonBackgroundColor(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangePositiveButtonBackgroundColor,
                CometChatTheme.getPrimaryColor(context)))
            setNegativeButtonBackgroundColor(typedArray.getColor(
                R.styleable.CometChatScopeChange_cometchatScopeChangeNegativeButtonBackgroundColor,
                CometChatTheme.getBackgroundColor1(context)))
        } finally {
            typedArray.recycle()
        }
    }


    // ==================== Public Setters ====================

    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        this.titleTextAppearance = appearance
        if (appearance != 0) binding.cometchatScopeChangeTitle.setTextAppearance(appearance)
    }

    fun setSubtitleTextAppearance(@StyleRes appearance: Int) {
        this.subtitleTextAppearance = appearance
        if (appearance != 0) binding.cometchatScopeChangeSubtitle.setTextAppearance(appearance)
    }

    fun setTitleColor(@ColorInt color: Int) {
        this.titleColor = color
        binding.cometchatScopeChangeTitle.setTextColor(color)
    }

    fun setSubtitleColor(@ColorInt color: Int) {
        this.subtitleColor = color
        binding.cometchatScopeChangeSubtitle.setTextColor(color)
    }

    fun setScopeIcon(icon: Drawable?) {
        this.scopeIcon = icon
        icon?.let { binding.ivScopeChange.setImageDrawable(it) }
    }

    fun setIconTint(@ColorInt color: Int) {
        this.iconTint = color
        binding.ivScopeChange.setColorFilter(color)
    }

    override fun setCardBackgroundColor(@ColorInt color: Int) {
        this.bgColor = color
        super.setCardBackgroundColor(color)
    }

    fun setCornerRadius(@Dimension cornerRadius: Int) {
        this.cRadius = cornerRadius
        val model = ShapeAppearanceModel.Builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius.toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius.toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
            .build()
        shapeAppearanceModel = model
    }

    fun setItemTextColor(@ColorInt color: Int) {
        this.itemTextColor = color
        scopeAdapter.setItemTextColor(color)
    }

    fun setDisableItemTextColor(@ColorInt color: Int) {
        this.disableItemTextColor = color
        scopeAdapter.setDisableItemTextColor(color)
    }

    fun setItemTextAppearance(@StyleRes appearance: Int) {
        this.itemTextAppearance = appearance
        scopeAdapter.setItemTextAppearance(appearance)
    }

    fun setItemRadioButtonTint(@ColorInt color: Int) {
        this.itemRadioButtonTint = color
        scopeAdapter.setRadioButtonTint(color)
    }

    fun setItemDisableRadioButtonTint(@ColorInt color: Int) {
        this.itemDisableRadioButtonTint = color
        scopeAdapter.setDisableRadioButtonTint(color)
    }

    fun setDragHandleColor(@ColorInt color: Int) {
        this.dragHandleColor = color
        binding.cometchatScopeChangeDragHandle.setCardBackgroundColor(color)
    }

    fun setPositiveButtonTextColor(@ColorInt color: Int) {
        this.positiveButtonTextColor = color
        binding.cometchatScopeChangeSaveText.setTextColor(color)
    }

    fun setNegativeButtonTextColor(@ColorInt color: Int) {
        this.negativeButtonTextColor = color
        binding.cometchatScopeChangeCancelText.setTextColor(color)
    }

    fun setButtonTextAppearance(@StyleRes appearance: Int) {
        this.buttonTextAppearance = appearance
        if (appearance != 0) {
            binding.cometchatScopeChangeCancelText.setTextAppearance(appearance)
            binding.cometchatScopeChangeSaveText.setTextAppearance(appearance)
        }
    }

    fun setPositiveButtonBackgroundColor(@ColorInt color: Int) {
        this.positiveButtonBackgroundColor = color
        binding.cometchatScopeChangeSaveButton.setCardBackgroundColor(color)
    }

    fun setNegativeButtonBackgroundColor(@ColorInt color: Int) {
        this.negativeButtonBackgroundColor = color
        binding.cometchatScopeChangeCancelButton.setCardBackgroundColor(color)
    }

    fun setStyle(@StyleRes style: Int) {
        if (style != 0) {
            val typedArray = context.theme.obtainStyledAttributes(style, R.styleable.CometChatScopeChange)
            extractAttributesAndApplyDefaults(typedArray)
        }
    }

    // ==================== Data & Callbacks ====================

    fun setRoleData(group: Group, groupMember: GroupMember) {
        this.group = group
        this.groupMember = groupMember
        scopeAdapter.setGroupData(
            listOf("Admin", "Moderator", "Participant"),
            groupMember,
            group
        )
    }

    fun getScopeAdapter(): ScopeAdapter = scopeAdapter

    fun setOnNegativeButtonClick(listener: () -> Unit) {
        onNegativeButtonClick = listener
    }

    fun setOnPositiveButtonClick(listener: () -> Unit) {
        onPositiveButtonClick = listener
    }

    fun setScopeChangeCallback(callback: CometChat.CallbackListener<GroupMember>) {
        this.scopeChangeCallback = callback
    }

    // ==================== Scope Change Logic ====================

    private fun changeScope(scopeChangedTo: String?) {
        if (scopeChangedTo == null) return
        val member = groupMember ?: return
        val grp = group ?: return
        val newScope = scopeChangedTo.lowercase()

        if (!newScope.equals(member.scope, ignoreCase = true)) {
            // Show progress — matches Java setDialogState(INITIATED)
            binding.cometchatScopeChangeSaveText.visibility = GONE
            binding.cometchatScopeChangeSaveProgress.visibility = VISIBLE

            CometChat.updateGroupMemberScope(
                member.uid, grp.guid, newScope,
                object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(successMessage: String) {
                        binding.cometchatScopeChangeSaveText.visibility = VISIBLE
                        binding.cometchatScopeChangeSaveProgress.visibility = GONE
                        member.scope = scopeChangedTo
                        scopeChangeCallback?.onSuccess(member)
                    }

                    override fun onError(e: CometChatException) {
                        binding.cometchatScopeChangeSaveText.visibility = VISIBLE
                        binding.cometchatScopeChangeSaveProgress.visibility = GONE
                        scopeChangeCallback?.onError(e)
                    }
                }
            )
        } else {
            scopeChangeCallback?.onSuccess(member)
        }
    }
}
