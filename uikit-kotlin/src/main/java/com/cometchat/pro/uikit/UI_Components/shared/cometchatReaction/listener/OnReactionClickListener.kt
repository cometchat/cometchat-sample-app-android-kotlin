package com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.listener

import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.model.Reaction

interface OnReactionClickListener {
    fun onEmojiClicked(emojicon: Reaction)
}