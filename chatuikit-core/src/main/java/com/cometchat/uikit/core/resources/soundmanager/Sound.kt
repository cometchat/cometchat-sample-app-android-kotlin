package com.cometchat.uikit.core.resources.soundmanager

import androidx.annotation.RawRes
import com.cometchat.uikit.core.R

/**
 * Enum representing different sounds used in the application.
 * Each sound type has an associated raw audio resource file.
 */
enum class Sound(@RawRes private var rawFile: Int) {
    /**
     * Sound played when receiving an incoming call.
     */
    INCOMING_CALL(R.raw.cometchat_incoming_call),
    
    /**
     * Sound played when making an outgoing call.
     */
    OUTGOING_CALL(R.raw.cometchat_outgoing_call),
    
    /**
     * Sound played when receiving an incoming message.
     */
    INCOMING_MESSAGE(R.raw.comechat_incoming_message),
    
    /**
     * Sound played when sending an outgoing message.
     */
    OUTGOING_MESSAGE(R.raw.cometchat_outgoing_message),
    
    /**
     * Sound played when receiving a message from another conversation.
     */
    INCOMING_MESSAGE_FROM_OTHER(R.raw.cometchat_incoming_message_other);

    /**
     * Returns the raw file resource ID associated with the sound.
     *
     * @return The raw file resource ID.
     */
    fun getRawFile(): Int = rawFile

    /**
     * Sets the raw file resource ID associated with the sound.
     * Allows customization of sound files at runtime.
     *
     * @param rawFile The raw file resource ID to be set.
     * @return The updated raw file resource ID.
     */
    fun setRawFile(@RawRes rawFile: Int): Int {
        this.rawFile = rawFile
        return this.rawFile
    }
}
