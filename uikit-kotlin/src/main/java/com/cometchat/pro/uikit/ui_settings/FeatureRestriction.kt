package com.cometchat.pro.uikit.ui_settings

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.exceptions.CometChatException
import kotlinx.coroutines.*
import kotlin.properties.Delegates

class FeatureRestriction() {


    companion object {

        private var publicGroupEnabled: Boolean = false
        private var privateGroupEnabled: Boolean = false
        private var passwordGroupEnabled: Boolean = false
        private var callListEnabled: Boolean = false
        private var dataMaskingEnabled: Boolean = false
        private var imageModerationEnabled: Boolean = false
        private var profanityFilterEnabled: Boolean = false
        private var banningGroupMembersEnabled: Boolean = false
        private var kickingGroupMembersEnabled: Boolean = false
        private var deleteMemberMessageEnabled: Boolean = false
        private var blockUserEnabled: Boolean = false
        private var collaborativeDocumentEnabled: Boolean = false
        private var collaborativeWhiteBoardEnabled: Boolean = false
        private var smartRepliesEnabled: Boolean = false
        private var messageTranslationEnabled: Boolean = false
        private var liveReactionsEnabled: Boolean = false
        private var reactionsEnabled: Boolean = false
        private var linkPreviewEnabled: Boolean = false
        private var thumbnailGenerationEnabled: Boolean = false
        private var groupVideoCallEnabled: Boolean = false
        private var oneOnOneVideoCallEnabled: Boolean  = false
        private var oneOnOneAudioCallEnabled: Boolean = false
        private var messageSearchEnabled: Boolean = false
        private var groupSearchEnabled: Boolean = false
        private var userSearchEnabled: Boolean = false
        private var unreadCountEnabled: Boolean = false
        private var messageHistoryEnabled: Boolean = false
        var deliveryReceiptsEnabled: Boolean = false
        var typingIndicatorsEnabled: Boolean = false
        var userPresenceEnabled: Boolean = false
        var userListEnabled: Boolean = false
        var groupChatEnabled: Boolean = false
        var oneOnOneChatEnabled: Boolean = false
        var stickerEnabled: Boolean = false
        var pollsEnabled: Boolean = false
        var voiceNoteEnabled: Boolean = false
        var filesEnabled = false
        var photosVideosEnabled = false
        var messageRepliesEnabled = false
        var threadFeatureEnabled : Boolean = false

        // Core Chat
        private const val chat_one_on_one_enabled = "core.chat.one-on-one.enabled"
        private const val chat_groups_enabled = "core.chat.groups.enabled"
        private const val chat_groups_public_enabled = "core.chat.groups.public.enabled"
        private const val chat_groups_private_enabled = "core.chat.groups.private.enabled"
        private const val chat_groups_password_enabled = "core.chat.groups.password.enabled"
        private const val chat_users_list_enabled = "core.chat.users.list.enabled"
        private const val chat_users_presence_enabled = "core.chat.users.presence.enabled"
        private const val chat_typing_indicator_enabled = "core.chat.typing-indicator.enabled"
        private const val chat_messages_receipts_enabled = "core.chat.messages.receipts.enabled"
        private const val chat_messages_custom_enabled = "core.chat.messages.custom.enabled"
        private const val chat_messages_threads_enabled = "core.chat.messages.threads.enabled"
        private const val chat_messages_replies_enabled = "core.chat.messages.replies.enabled"
        private const val chat_messages_media_enabled = "core.chat.messages.media.enabled"
        private const val chat_voice_notes_enabled = "core.chat.voice-notes.enabled"
        private const val chat_messages_history_enabled = "core.chat.messages.history.enabled"
        private const val chat_messages_unread_count_enabled = "core.chat.messages.unread-count.enabled"
        private const val chat_users_search_enabled = "core.chat.users.search.enabled"
        private const val chat_groups_search_enabled = "core.chat.groups.search.enabled"
        private const val chat_messages_search_enabled = "core.chat.messages.search.enabled"
        // Voice & Video Calling/Conferencing
        private const val calls_enabled = "core.call.enabled"
        private const val call_one_on_one_audio_enabled = "core.call.one-on-one.audio.enabled"
        private const val call_one_on_one_video_enabled = "core.call.one-on-one.video.enabled"
        private const val call_groups_audio_enabled = "core.call.groups.audio.enabled"
        private const val call_groups_video_enabled = "core.call.groups.video.enabled"
        private const val call_recording_enabled = "core.call.recording.enabled"
        private const val call_live_streaming_enabled = "core.call.live-streaming.enabled"
        private const val call_transcript_enabled = "core.call.transcript.enabled"
        // User Experience
        private const val thumbnail_generation_enabled = "features.ux.thumbnail-generation.enabled"
        private const val link_preview_enabled = "features.ux.link-preview.enabled"
        private const val messages_saved_enabled = "features.ux.messages.saved.enabled"
        private const val messages_pinned_enabled = "features.ux.messages.pinned.enabled"
        private const val rich_media_preview_enabled = "features.ux.rich-media-preview.enabled"
        private const val voice_transcription_enabled = "features.ux.voice-transcription.enabled"
        // User Engagement
        private const val emojis_enabled = "features.ue.emojis.enabled"
        private const val mentions_enabled = "features.ue.mentions.enabled"
        private const val stickers_enabled = "features.ue.stickers.enabled"
        private const val reactions_enabled = "features.ue.reactions.enabled"
        private const val live_reactions_enabled = "features.ue.live-reactions.enabled"
        private const val message_translation_enabled = "features.ue.message-translation.enabled"
        private const val email_replies_enabled = "features.ue.email-replies.enabled"
        private const val smart_replies_enabled = "features.ue.smart-replies.enabled"
        private const val polls_enabled = "features.ue.polls.enabled"
        // Collaboration
        private const val collaboration_whiteboard_enabled = "features.collaboration.whiteboard.enabled"
        private const val collaboration_document_enabled = "features.collaboration.document.enabled"
        // Moderation
        private const val moderation_users_block_enabled = "features.moderation.users.block.enabled"
        private const val moderation_groups_moderators_enabled = "features.moderation.groups.moderators.enabled"
        private const val moderation_groups_kick_enabled = "features.moderation.groups.kick.enabled"
        private const val moderation_groups_ban_enabled = "features.moderation.groups.ban.enabled"
        private const val moderation_xss_filter_enabled = "features.moderation.xss-filter.enabled"
        private const val moderation_profanity_filter_enabled = "features.moderation.profanity-filter.enabled"
        private const val moderation_image_moderation_enabled = "features.moderation.image-moderation.enabled"
        private const val moderation_data_masking_enabled = "features.moderation.data-masking.enabled"
        private const val moderation_malware_scanner_enabled = "features.moderation.malware-scanner.enabled"
        private const val moderation_sentiment_analysis_enabled = "features.moderation.sentiment-analysis.enabled"
        private const val moderation_inflight_message_moderation_enabled = "features.moderation.inflight-message-moderation.enabled"

        private const val dataMasking = "data-masking"
        private const val profanityFilter = "profanity-filter"
        private const val thumbnailGeneration = "thumbnail-generator"
        private const val linkPreview = "link-preview"
        private const val richMediaPreview = "rich-media"
        private const val sticker = "stickers"
        private const val reactions = "reactions"
        private const val messageTranslation = "message-translation"
        private const val smartReplies = "smart-reply"
        private const val collaborationWhiteboard = "whiteboard"
        private const val collaborationDocument = "document"
        private const val pinMessages = "pin-message"
        private const val saveMessages = "save-message"
        private const val voiceTranscription = "voice-transcription"
        private const val polls = "polls"
        private const val xssFilter = "xss-filter"
        private const val imageModeration = "image-moderation"
        private const val malwareScanner = "virus-malware-scanner"
        private const val sentimentAnalysis = "sentiment-analysis"
        private const val emailReplies = "email-replies"
        private const val emojis = "emojis"
        private const val mentions = "mentions"



        fun isOneOnOneChatEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_one_on_one_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isOneOnOneChatEnabled "+ p0.toString())
                    if (p0 != null)
                        oneOnOneChatEnabled = p0 && UIKitSettings.sendMessageInOneOnOne
                    onSuccessListener.onSuccess(oneOnOneChatEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isOneOnOneChatEnabled "+p0.toString() )
                }
            })
        }

        fun isGroupChatEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_groups_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isGroupChatEnabled "+ p0.toString())
                    if (p0 != null)
                        groupChatEnabled = p0 && UIKitSettings.sendMessageInGroup
                    onSuccessListener.onSuccess(groupChatEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isGroupChatEnabled "+p0.toString() )
                }
            })
        }

        // require filter from SDK for below three methods
        fun isPublicGroupEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_groups_public_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isPublicGroupEnabled "+ p0.toString())
                    if (p0 != null)
                        publicGroupEnabled = p0 && UIKitSettings.publicGroup
                    onSuccessListener.onSuccess(publicGroupEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isPublicGroupEnabled "+p0.toString() )
                }
            })
        }
        fun isPrivateGroupEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_groups_private_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isPrivateGroupEnabled "+ p0.toString())
                    if (p0 != null)
                        privateGroupEnabled = p0 && UIKitSettings.privateGroup
                    onSuccessListener.onSuccess(privateGroupEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isPrivateGroupEnabled "+p0.toString() )
                }
            })
        }
        fun isPasswordGroupEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_groups_password_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isPasswordGroupEnabled "+ p0.toString())
                    if (p0 != null)
                        passwordGroupEnabled = p0 && UIKitSettings.passwordGroup
                    onSuccessListener.onSuccess(passwordGroupEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isPasswordGroupEnabled "+p0.toString() )
                }
            })
        }

        fun isUserListEnabled(onSuccessListener: OnSuccessListener) { // modified
            CometChat.isFeatureEnabled(chat_users_list_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isUserListEnabled "+ p0.toString())
                    if (p0 != null)
                        userListEnabled = p0 && UIKitSettings.users
                    onSuccessListener.onSuccess(userListEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isUserListEnabled "+p0.toString() )
                }
            })
        }

        fun isCallListEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(calls_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isCallListEnabled "+ p0.toString())
                    if (p0 != null)
                        callListEnabled = p0 && UIKitSettings.calls
                    onSuccessListener.onSuccess(callListEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isCallListEnabled "+p0.toString() )
                }
            })
        }

        fun isRecentChatListEnabled(onSuccessListener: OnSuccessListener) {
            onSuccessListener.onSuccess(UIKitSettings.conversations)
        }

        fun isGroupListEnabled(onSuccessListener: OnSuccessListener) {
            onSuccessListener.onSuccess(UIKitSettings.groups)
        }

        fun isUserSettingsEnabled(onSuccessListener: OnSuccessListener) {
            onSuccessListener.onSuccess(UIKitSettings.userSettings)
        }

        fun isUserPresenceEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_users_presence_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isUserPresenceEnabled "+ p0.toString())
                    if (p0 != null)
                        userPresenceEnabled = p0 && UIKitSettings.showUserPresence
                    onSuccessListener.onSuccess(userPresenceEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isUserPresenceEnabled "+p0.toString() )
                }
            })
        }

        fun isTypingIndicatorsEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_typing_indicator_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isTypingIndicatorsEnabled "+ p0.toString())
                    if (p0 != null)
                        typingIndicatorsEnabled = p0 && UIKitSettings.sendTypingIndicator
                    onSuccessListener.onSuccess(typingIndicatorsEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isTypingIndicatorsEnabled "+p0.toString() )
                }
            })
        }

        fun isDeliveryReceiptsEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_messages_receipts_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isDeliveryReceiptsEnabled "+ p0.toString())
                    if (p0 != null)
                        deliveryReceiptsEnabled = p0 && UIKitSettings.showReadDeliveryReceipts
                    onSuccessListener.onSuccess(deliveryReceiptsEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isDeliveryReceiptsEnabled "+p0.toString() )
                }
            })
        }

        //require different key for thread chat
        fun isThreadedMessagesEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_messages_threads_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("FeatureRestriction", "onSuccess: isThreadedMessagesEnabled "+ p0.toString())
                    if (p0 != null)
                        threadFeatureEnabled = p0 && UIKitSettings.threadedChats
                    onSuccessListener.onSuccess(threadFeatureEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("FeatureRestriction", "onError: isThreadedMessagesEnabled "+p0.toString() )
                }
            })

        }

        fun isMessageRepliesEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_messages_replies_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isMessageRepliesEnabled "+ p0.toString())
                    if (p0 != null)
                        messageRepliesEnabled = p0 && UIKitSettings.replyingToMessage
                    onSuccessListener.onSuccess(messageRepliesEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isMessageRepliesEnabled" +p0.toString())
                }
            })
        }

        fun isPhotosVideosEnabled(onSuccessListener: OnSuccessListener) { //done
            CometChat.isFeatureEnabled(chat_messages_media_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isPhotosVideosEnabled "+ p0.toString())
                    if (p0 != null)
                        photosVideosEnabled = p0 && UIKitSettings.sendPhotoVideos
                    onSuccessListener.onSuccess(photosVideosEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isPhotosVideosEnabled" +p0.toString())
                }
            })
        }

        fun isFilesEnabled(onSuccessListener: OnSuccessListener) { //done
            CometChat.isFeatureEnabled(chat_messages_media_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isFilesEnabled "+ p0.toString())
                    if (p0 != null)
                        filesEnabled = p0 && UIKitSettings.sendFiles
                    onSuccessListener.onSuccess(filesEnabled)

                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isFilesEnabled" +p0.toString())
                }
            })
        }

        fun isVoiceNotesEnabled(onSuccessListener: OnSuccessListener) { //done
            CometChat.isFeatureEnabled(chat_voice_notes_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isVoiceNotesEnabled "+ p0.toString())
                    if (p0 != null)
                        voiceNoteEnabled = p0 && UIKitSettings.sendVoiceNotes
                    onSuccessListener.onSuccess(voiceNoteEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isVoiceNotesEnabled" +p0.toString())
                }
            })
        }

        fun isMessageHistoryEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_messages_history_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isMessageHistoryEnabled "+ p0.toString())
                    if (p0 != null)
                        messageHistoryEnabled = p0 && UIKitSettings.messageHistory
                    onSuccessListener.onSuccess(messageHistoryEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isMessageHistoryEnabled " +p0.toString())
                }
            })
        }
        fun isUnreadCountEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_messages_unread_count_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isUnreadCountEnabled "+ p0.toString())
                    if (p0 != null)
                        unreadCountEnabled = p0 && UIKitSettings.unreadCount
                    onSuccessListener.onSuccess(unreadCountEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isUnreadCountEnabled " +p0.toString())
                }
            })
        }
        fun isUserSearchEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_users_search_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isUserSearchEnabled "+ p0.toString())
                    if (p0 != null)
                        userSearchEnabled = p0 && UIKitSettings.searchUsers
                    onSuccessListener.onSuccess(userSearchEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isUserSearchEnabled " +p0.toString())
                }
            })
        }

        fun isGroupSearchEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_groups_search_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isGroupSearchEnabled "+ p0.toString())
                    if (p0 != null)
                        groupSearchEnabled = p0 && UIKitSettings.searchGroups
                    onSuccessListener.onSuccess(groupSearchEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isGroupSearchEnabled " +p0.toString())
                }
            })
        }

        fun isMessageSearchEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(chat_messages_search_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isMessageSearchEnabled "+ p0.toString())
                    if (p0 != null)
                        messageSearchEnabled = p0 && UIKitSettings.searchMessages
                    onSuccessListener.onSuccess(messageSearchEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isMessageSearchEnabled " +p0.toString())
                }
            })
        }

        // Voice & Video Calling/Conferencing
        fun isOneOnOneAudioCallEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(call_one_on_one_audio_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isOneOnOneAudioCallEnabled "+ p0.toString())
                    if (p0 != null)
                        oneOnOneAudioCallEnabled = p0 && UIKitSettings.userAudioCall
                    onSuccessListener.onSuccess(oneOnOneAudioCallEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isOneOnOneAudioCallEnabled " +p0.toString())
                }
            })
        }

        fun isOneOnOneVideoCallEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(call_one_on_one_video_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isOneOnOneVideoCallEnabled "+ p0.toString())
                    if (p0 != null)
                        oneOnOneVideoCallEnabled = p0 && UIKitSettings.userVideoCall
                    onSuccessListener.onSuccess(oneOnOneVideoCallEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isOneOnOneVideoCallEnabled " +p0.toString())
                }
            })
        }

        fun isGroupAudioCallEnabled() : Boolean {   //group audio call is not present
            val featureEnabled = checkFeatureEnabled(call_groups_audio_enabled)
            return featureEnabled && UIKitSettings.groupAudioCall
        }
        fun isGroupVideoCallEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(call_groups_video_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isGroupVideoCallEnabled "+ p0.toString())
                    if (p0 != null)
                        groupVideoCallEnabled = p0 && UIKitSettings.groupVideoCall
                    onSuccessListener.onSuccess(groupVideoCallEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isGroupVideoCallEnabled " +p0.toString())
                }
            })
        }

        fun isCallRecordingEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(call_recording_enabled)
            return featureEnabled && UIKitSettings.callRecording
        }

        fun isCallLiveStreamingEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(call_live_streaming_enabled)
            return featureEnabled && UIKitSettings.callLiveStreaming
        }

        fun isCallTranscriptEnabled(): Boolean {
            val featureEnabled = checkFeatureEnabled(call_transcript_enabled)
            return featureEnabled && UIKitSettings.callTranscription
        }

        fun isThumbnailGenerationEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(thumbnail_generation_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isThumbnailGenerationEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(thumbnailGeneration, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    thumbnailGenerationEnabled = p0 && p1 && UIKitSettings.thumbnailGeneration
                                onSuccessListener.onSuccess(thumbnailGenerationEnabled)
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: thumbnail generation extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isThumbnailGenerationEnabled " + p0.toString())
                }
            })
        }
        fun isLinkPreviewEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(link_preview_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isLinkPreviewEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(linkPreview, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    linkPreviewEnabled = p0 && p1 && UIKitSettings.linkPreview
                                onSuccessListener.onSuccess(linkPreviewEnabled)
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: LinkPreview extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isLinkPreviewEnabled " + p0.toString())
                }
            })
        }
        fun isSaveMessagesEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(messages_saved_enabled)
            val extensionEnabled = checkExtensionEnabled("save-message")
            return featureEnabled && extensionEnabled && UIKitSettings.saveMessages
        }

        fun isPinMessagesEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(messages_pinned_enabled)
            val extensionEnabled = checkExtensionEnabled("pin-message")
            return featureEnabled && extensionEnabled && UIKitSettings.pinMessages
        }

        fun isRichMediaPreviewEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(rich_media_preview_enabled)
            val extensionEnabled = checkExtensionEnabled("rich-media")
            return featureEnabled && extensionEnabled && UIKitSettings.richMediaPreview
        }

        fun isVoiceTranscriptionEnabled(): Boolean {
            val featureEnabled = checkFeatureEnabled(voice_transcription_enabled)
            val extensionEnabled = checkExtensionEnabled("voice-transcription")
            return featureEnabled && extensionEnabled && UIKitSettings.voiceTranscription
        }

        //User engagement
        fun isEmojisEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(emojis_enabled)
            return featureEnabled && UIKitSettings.sendEmojis
        }

        fun isMentionsEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(mentions_enabled)
            return featureEnabled && UIKitSettings.mentions
        }

        fun isStickersEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(stickers_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isStickersEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(sticker, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    stickerEnabled = p0 && p1 && UIKitSettings.sendStickers
                                onSuccessListener.onSuccess(stickerEnabled)
                                Log.e("TAG", "onSuccess: isStickers extension" + stickerEnabled.toString())
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: isStickers extension "+p0.toString() )
                            }
                        })
                    }
                }

                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isStickersEnabled" + p0.toString())
                }
            })
        }

        fun isReactionsEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(reactions_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isReactionsEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(reactions, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    reactionsEnabled = p0 && p1 && UIKitSettings.sendMessageReaction
                                onSuccessListener.onSuccess(reactionsEnabled)
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: isReactionsEnabled extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isReactionsEnabled " + p0.toString())
                }
            })
        }

        fun isLiveReactionsEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(live_reactions_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isLiveReactionsEnabled "+ p0.toString())
                    if (p0 != null)
                        liveReactionsEnabled = p0 && UIKitSettings.sendLiveReaction
                    onSuccessListener.onSuccess(liveReactionsEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isLiveReactionsEnabled " +p0.toString())
                }
            })
        }
        fun isMessageTranslationEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(message_translation_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isMessageTranslationEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(messageTranslation, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    messageTranslationEnabled = p0 && p1 && UIKitSettings.messageTranslation
                                onSuccessListener.onSuccess(messageTranslationEnabled)
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: isMessageTranslationEnabled extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isMessageTranslationEnabled " + p0.toString())
                }
            })
        }
        fun isSmartRepliesEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(smart_replies_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isSmartRepliesEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(smartReplies, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    smartRepliesEnabled = p0 && p1 && UIKitSettings.smartReplies
                                onSuccessListener.onSuccess(smartRepliesEnabled)
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: isSmartRepliesEnabled extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isSmartRepliesEnabled " + p0.toString())
                }
            })
        }
        fun isPollsEnabled(onSuccessListener: OnSuccessListener) { //done
            CometChat.isFeatureEnabled(polls_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isFeatureEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(polls, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    pollsEnabled = p0 && p1 && UIKitSettings.polls
                                onSuccessListener.onSuccess(pollsEnabled)
                                Log.e("TAG", "onSuccess: isPollsEnabled "+pollsEnabled.toString() )
                            }

                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: isPollsEnabled extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isPollsEnabled" + p0.toString())
                }
            })
        }

        // Collaboration
        fun isCollaborativeWhiteBoardEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(collaboration_whiteboard_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isCollaborativeWhiteBoardEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(collaborationWhiteboard, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    collaborativeWhiteBoardEnabled = p0 && p1 && UIKitSettings.collaborativeWhiteboard
                                onSuccessListener.onSuccess(collaborativeWhiteBoardEnabled)
                                Log.e("TAG", "onSuccess: isCollaborativeWhiteBoardEnabled "+collaborativeWhiteBoardEnabled.toString() )
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: isCollaborativeWhiteBoardEnabled extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isCollaborativeWhiteBoardEnabled" + p0.toString())
                }
            })
        }
        fun isCollaborativeDocumentEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(collaboration_document_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isCollaborativeDocumentEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(collaborationDocument, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    collaborativeDocumentEnabled = p0 && p1 && UIKitSettings.collaborativeDocument
                                onSuccessListener.onSuccess(collaborativeDocumentEnabled)
                                Log.e("TAG", "onSuccess: isCollaborativeDocumentEnabled "+collaborativeDocumentEnabled.toString() )
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: isCollaborativeDocumentEnabled extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isCollaborativeDocumentEnabled" + p0.toString())
                }
            })
        }

        //Moderation
        fun isBlockUserEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(moderation_users_block_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isBlockUserEnabled "+ p0.toString())
                    if (p0 != null)
                        blockUserEnabled = p0 && UIKitSettings.blockUser
                    onSuccessListener.onSuccess(blockUserEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isBlockUserEnabled " +p0.toString())
                }
            })
        }
        fun isDeleteMemberMessageEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(moderation_groups_moderators_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isDeleteMemberMessageEnabled "+ p0.toString())
                    if (p0 != null)
                        deleteMemberMessageEnabled = p0 && UIKitSettings.allowModeratorToDeleteMemberMessages
                    onSuccessListener.onSuccess(deleteMemberMessageEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isDeleteMemberMessageEnabled " +p0.toString())
                }
            })
        }

        fun isQNAModeEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(moderation_groups_moderators_enabled)
            return featureEnabled && UIKitSettings.setGroupInQnaModeByModerators
        }

        fun isHighlightMessagesEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(moderation_groups_moderators_enabled)
            return featureEnabled && UIKitSettings.highlightMessageFromModerators
        }

        fun isKickingGroupMembersEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(moderation_groups_kick_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isKickingGroupMembersEnabled "+ p0.toString())
                    if (p0 != null)
                        kickingGroupMembersEnabled = p0 && UIKitSettings.kickMember
                    onSuccessListener.onSuccess(kickingGroupMembersEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isKickingGroupMembersEnabled " +p0.toString())
                }
            })
        }

        fun isBanningGroupMembersEnabled(onSuccessListener: OnSuccessListener) { //need to add for popup
            CometChat.isFeatureEnabled(moderation_groups_ban_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isBanningGroupMembersEnabled "+ p0.toString())
                    if (p0 != null)
                        banningGroupMembersEnabled = p0 && UIKitSettings.banMember
                    onSuccessListener.onSuccess(banningGroupMembersEnabled)
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isBanningGroupMembersEnabled " +p0.toString())
                }
            })
        }
        fun isProfanityFilterEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(moderation_profanity_filter_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isProfanityFilterEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(profanityFilter, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    profanityFilterEnabled = p0 && p1 && UIKitSettings.profanityFilter
                                onSuccessListener.onSuccess(profanityFilterEnabled)
                                Log.e("TAG", "onSuccess: isProfanityFilterEnabled "+profanityFilterEnabled.toString() )
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: isProfanityFilterEnabled extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isProfanityFilterEnabled" + p0.toString())
                }
            })
        }

        fun isImageModerationEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(moderation_image_moderation_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isImageModerationEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(imageModeration, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    imageModerationEnabled = p0 && p1 && UIKitSettings.imageModeration
                                onSuccessListener.onSuccess(imageModerationEnabled)
                                Log.e("TAG", "onSuccess: isImageModerationEnabled "+imageModerationEnabled.toString() )
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: isImageModerationEnabled extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isProfanityFilterEnabled" + p0.toString())
                }
            })
        }

        fun isDataMaskingEnabled(onSuccessListener: OnSuccessListener) {
            CometChat.isFeatureEnabled(moderation_data_masking_enabled, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    Log.e("TAG", "onSuccess: isDataMaskingEnabled " + p0.toString())
                    if (p0 != null) {
                        CometChat.isExtensionEnabled(dataMasking, object : CometChat.CallbackListener<Boolean>() {
                            override fun onSuccess(p1: Boolean?) {
                                if (p1 != null)
                                    dataMaskingEnabled = p0 && p1 && UIKitSettings.dataMasking
                                onSuccessListener.onSuccess(dataMaskingEnabled)
                                Log.e("TAG", "onSuccess: isDataMaskingEnabled "+dataMaskingEnabled.toString() )
                            }
                            override fun onError(p0: CometChatException?) {
                                Log.e("TAG", "onError: isDataMaskingEnabled extension "+p0.toString())
                            }
                        })
                    }
                }
                override fun onError(p0: CometChatException?) {
                    Log.e("TAG", "onError: isDataMaskingEnabled" + p0.toString())
                }
            })
        }

        fun isDeleteConversationEnabled(onSuccessListener: OnSuccessListener) {
            onSuccessListener.onSuccess(UIKitSettings.deleteConversation)
        }

        fun isMalwareScannerEnabled(): Boolean {
            val featureEnabled = checkFeatureEnabled(moderation_malware_scanner_enabled)
            val extensionEnabled = checkExtensionEnabled("virus-malware-scanner")
            return featureEnabled && extensionEnabled && UIKitSettings.malwareScanner
        }
        fun isSentimentAnalysisEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(moderation_sentiment_analysis_enabled)
            val extensionEnabled = checkExtensionEnabled("sentiment-analysis")
            return featureEnabled && extensionEnabled && UIKitSettings.sentimentAnalysis
        }

        fun isInFlightMessageModerationEnabled(): Boolean {
            val featureEnabled = checkFeatureEnabled(moderation_inflight_message_moderation_enabled)
            return featureEnabled && UIKitSettings.inflightMessageModeration
        }

        fun isEditMessageEnabled(): Boolean {
            return UIKitSettings.editMessage
        }

        fun isJoinLeaveGroupsEnabled(): Boolean {
            return UIKitSettings.joinOrLeaveGroup
        }

        fun isLargerSizeEmojisEnabled(): Boolean {
            return UIKitSettings.sendEmojisInLargerSize
        }

        fun isGifsEnabled() : Boolean {
            return UIKitSettings.sendGifs
        }

        fun isShareCopyForwardMessageEnabled(): Boolean {
            return UIKitSettings.shareCopyForwardMessage
        }

        fun isSharedMediaEnabled(): Boolean {
            return UIKitSettings.viewShareMedia
        }

        fun isMessagesSoundEnabled(): Boolean {
            return UIKitSettings.enableSoundForMessages
        }
        fun isCallsSoundEnabled(): Boolean {
            return UIKitSettings.enableSoundForCalls
        }

        fun isViewingGroupMembersEnabled() : Boolean {
            return UIKitSettings.viewGroupMembers
        }

        fun isCallActionMessagesEnabled(): Boolean {
            return UIKitSettings.callNotifications
        }

        fun isGroupDeletionEnabled(): Boolean {
            return UIKitSettings.allowDeleteGroup
        }

        fun isAddingGroupMembersEnabled(): Boolean {
            return UIKitSettings.allowAddMembers
        }

        fun isLocationSharingEnabled(): Boolean { //done
            return UIKitSettings.shareLocation
        }

        fun isGroupActionMessagesEnabled(): Boolean {
            return UIKitSettings.joinLeaveNotifications
        }

        fun isGroupCreationEnabled(): Boolean {
            return UIKitSettings.groupCreation
        }

        fun isDeleteMessageEnabled(): Boolean {
            return UIKitSettings.deleteMessage
        }

        fun isEmailRepliesEnabled() : Boolean {
            val featureEnabled = checkFeatureEnabled(email_replies_enabled)
            return featureEnabled && UIKitSettings.emailReplies
        }

        fun isHideDeletedMessagesEnabled(): Boolean {
            return UIKitSettings.hideDeletedMessages
        }

        fun isMessageInPrivateEnabled(): Boolean {
            return UIKitSettings.messageInPrivate
        }

        fun isViewProfileEnabled(): Boolean {
            return UIKitSettings.viewProfile
        }

        fun isChatSearchEnabled(): Boolean {
            return UIKitSettings.searchChats
        }












//        fun isGroupNotificationHidden(): Boolean {
//            return UIKitSettings.hideGroupNotification
//        } not in use



//        fun getColor(): String? {
//            return color
//        }


//        fun isShowUsersBB(): Boolean {
//            return UIKitSettings.showUsersBB
//        } not in use instead isUserListEnabled


        fun isShowGroupsBB(): Boolean {
            return UIKitSettings.showGroupsBB
        }

        fun isShowChatsBB(): Boolean {
            return UIKitSettings.showChatsBB
        }

        fun isShowCallsBB(): Boolean {
            return UIKitSettings.showCallsBB
        }

        fun isShowUserSettingsBB(): Boolean {
            return UIKitSettings.showUserSettingsBB
        }

        fun isChangingGroupMemberScopeEnabled(): Boolean {
            return UIKitSettings.allowPromoteDemoteMembers
        }



//        fun getGroupListing(): String? {
//            return groupListing
//        }
//
//        fun getUserListing(): String? {
//            return userListing
//        } not in use



//
//        fun isEnableSendingMessage(): Boolean {
//            return enableSendingMessage
//        } not on use

//        fun isShowReadDeliveryReceipts(): Boolean {
//            return showReadDeliveryReceipts
//        } not in use



//        fun isSendEmojis(): Boolean {
//            return sendEmojis
//        } not in use

//        fun isSendVoiceNotes(): Boolean {
//            return UIKitSettings.sendVoiceNotes
//        } not in use

//        fun isSendFiles(): Boolean {
//            return sendFiles
//        } not in use

//        fun isSendPolls(): Boolean {
//            return sendPolls
//        } not in use

//        fun isStickerVisible(): Boolean {
//            return sendStickers
//        } not in use

//        fun isSendPhotosVideo(): Boolean {
//            return sendPhotosVideo
//        } not in use

//        fun isEnableThreadedReplies(): Boolean {
//            return enableThreadedReplies
//        } not in use

//        fun isEnableReplyToMessage(): Boolean {
//            return enableReplyToMessage
//        } not in use








//        fun isBlockUser(): Boolean {
//            return blockUser
//        } not in use

//        fun isShowTypingIndicators(): Boolean {
//            return showTypingIndicators
//        } not in use



//        fun isShowUserPresence(): Boolean {
//            return showUserPresence
//        } not in use



//        fun isAllowBanKickMembers(): Boolean {
//            return allowBanKickMembers
//        } not in use



//        fun isAllowModeratorToDeleteMessages(): Boolean {
//            return allowModeratorToDeleteMessages
//        } not in use



        fun isViewGroupMember(): Boolean {
            return UIKitSettings.viewGroupMembers
        }







//        fun isEnableVideoCalling(): Boolean {
//            return enableVideoCalling
//        } not in use instead isOneOnOneVideoCallEnabled isGroupVideoCallEnabled

//        fun isEnableVoiceCalling(): Boolean {
//            return enableVoiceCalling
//        } not in use instead isOneOnOneAudioCallEnabled




//        fun getEmailColor(): Int {
//            return emailColor
//        }
//
//        fun getPhoneColor(): Int {
//            return phoneColor
//        }
//
//        fun getUrlColor(): Int {
//            return urlColor
//        }

//        fun showLiveReaction(): Boolean {
//            return liveReaction
//        } not in use

//        fun isReactionVisible(): Boolean {
//            return allowReactionOnMessage
//        } not in use

//        fun isWhiteBoardVisible(): Boolean {
//            return sendWhiteBoard
//        } not in use

//        fun isWriteBoardVisible(): Boolean {
//            return sendWriteBoard
//        } not in use

//        fun isTranslationAllowed(): Boolean {
//            return allowMessageTranslation
//        } not in use

//        fun isShowReplyPrivately(): Boolean {
//            return showReplyPrivately
//        } currently not in use

        private fun checkFeatureEnabled(key: String) : Boolean {
            var featureEnabled = false
            CometChat.isFeatureEnabled(key, object : CometChat.CallbackListener<Boolean>(){
                override fun onSuccess(p0: Boolean?) {
                    if (p0 != null) {
//                        response.complete(p0)
                        featureEnabled = p0
//                        onSuccessListener.onSuccess(p0)

                    }
                }
                override fun onError(p0: CometChatException?) {
                }
            })
            return featureEnabled
        }

        private fun checkExtensionEnabled(extensionId: String) :Boolean {
            var extensionEnabled = false
            CometChat.isExtensionEnabled(extensionId, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean) {
                    extensionEnabled = p0
                }
                override fun onError(p0: CometChatException?) {
                }
            })
            return extensionEnabled
        }

        private suspend fun checkExtensionEnabled1(extensionId: String) :Boolean {
            var extensionEnabled = false
            val response = CompletableDeferred<Boolean>()
            CometChat.isExtensionEnabled(extensionId, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean) {
                    response.complete(p0)
//                    extensionEnabled = p0
                }
                override fun onError(p0: CometChatException?) {
                }
            })
            return response.await()
        }

        private suspend fun checkFeatureEnabled1(key: String) : Boolean {
            val response = CompletableDeferred<Boolean>()
//            var featureEnabled = false
            CometChat.isFeatureEnabled(key, object : CometChat.CallbackListener<Boolean>(){
                override fun onSuccess(p0: Boolean?) {
                    if (p0 != null) {
                        response.complete(p0)
//                        featureEnabled = p0

                    }
                }
                override fun onError(p0: CometChatException?) {
                }
            }) 
            return response.await()


        }

        private fun checkFeatureEnabled2(key: String, action: (Boolean) -> Boolean) : Boolean{
            CometChat.isFeatureEnabled(key, object : CometChat.CallbackListener<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                    if (p0 != null) {
                        Log.e("TAG", "onSuccess: p0 "+p0.toString() )
//                        response.complete(p0)
//                        featureEnabled = p0

                        action(p0)

                    }
                }

                override fun onError(p0: CometChatException?) {
                }
            })
            return true
        }

         private fun checkFeatureEnabled3(key: String, onSuccessListener : OnSuccessListener) {
             CometChat.isFeatureEnabled(key, object : CometChat.CallbackListener<Boolean>() {
                 override fun onSuccess(p0: Boolean?) {
                     if (p0 != null) {
                         onSuccessListener.onSuccess(p0)
                     }
                 }
                 override fun onError(p0: CometChatException?) {
                 }
             })

         }

    }

    interface OnSuccessListener {
        fun onSuccess(p0 : Boolean)
    }

}