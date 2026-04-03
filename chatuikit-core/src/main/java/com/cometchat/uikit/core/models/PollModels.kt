package com.cometchat.uikit.core.models

import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.CometChatUIKit
import org.json.JSONObject

/**
 * Represents voter information for poll options.
 *
 * @property uid The unique identifier of the voter
 * @property name The display name of the voter
 * @property avatarUrl The URL to the voter's avatar image (nullable)
 */
data class VoterInfo(
    val uid: String,
    val name: String,
    val avatarUrl: String?
)

/**
 * Represents a single poll option with its voting information.
 *
 * @property id The unique identifier for this option (1-indexed position as string)
 * @property text The display text for this option
 * @property voteCount The number of votes this option has received
 * @property voters List of voter information (up to 3 voters for display)
 * @property isSelected Whether the logged-in user has selected this option
 */
data class PollOption(
    val id: String,
    val text: String,
    val voteCount: Int,
    val voters: List<VoterInfo>,
    val isSelected: Boolean
)

/**
 * Represents poll data extracted from a CustomMessage.
 *
 * @property id The unique identifier for the poll
 * @property question The poll question text
 * @property options List of poll options with their voting information
 * @property totalVotes The total number of votes cast in this poll
 */
data class PollData(
    val id: String,
    val question: String,
    val options: List<PollOption>,
    val totalVotes: Int
)

/**
 * JSON field constants for poll data extraction.
 */
private object PollJsonFields {
    const val ID = "id"
    const val QUESTION = "question"
    const val OPTIONS = "options"
    const val VOTERS = "voters"
    const val COUNT = "count"
    const val TOTAL = "total"
    const val RESULTS = "results"
    const val INJECTED = "@injected"
    const val EXTENSIONS = "extensions"
    const val POLLS = "polls"
    const val AVATAR = "avatar"
    const val NAME = "name"
}

/**
 * Extracts poll data from a CustomMessage.
 *
 * This function parses the customData and metadata of a CustomMessage to extract
 * poll information including the question, options, vote counts, and voter information.
 *
 * The poll data structure in customData:
 * ```json
 * {
 *   "id": "poll_id",
 *   "question": "What is your favorite color?",
 *   "options": {
 *     "1": "Red",
 *     "2": "Blue",
 *     "3": "Green"
 *   }
 * }
 * ```
 *
 * The poll results in metadata (under @injected.extensions.polls.results):
 * ```json
 * {
 *   "total": 5,
 *   "options": {
 *     "1": { "count": 2, "voters": { "user1": {"name": "John", "avatar": "url"}, "user2": {...} } },
 *     "2": { "count": 3, "voters": { "user3": {...}, "user4": {...}, "user5": {...} } },
 *     "3": { "count": 0, "voters": {} }
 *   }
 * }
 * ```
 *
 * @param message The CustomMessage containing poll data
 * @return PollData if extraction is successful, null otherwise
 */
fun extractPollData(message: CustomMessage): PollData? {
    return try {
        val customData = message.customData ?: return null
        
        // Extract poll ID - use message ID as fallback
        val pollId = if (customData.has(PollJsonFields.ID)) {
            customData.getString(PollJsonFields.ID)
        } else {
            message.id.toString()
        }
        
        // Extract question
        val question = if (customData.has(PollJsonFields.QUESTION)) {
            customData.getString(PollJsonFields.QUESTION)
        } else {
            return null
        }
        
        // Extract options from customData
        val optionsJson = if (customData.has(PollJsonFields.OPTIONS)) {
            customData.getJSONObject(PollJsonFields.OPTIONS)
        } else {
            return null
        }
        
        // Get poll results from metadata
        val pollResults = getPollResults(message)
        val resultsOptions = pollResults?.optJSONObject(PollJsonFields.OPTIONS)
        val totalVotes = pollResults?.optInt(PollJsonFields.TOTAL, 0) ?: 0
        
        // Get logged-in user UID for determining selected option
        val loggedInUserUid = CometChatUIKit.getLoggedInUser()?.uid
        
        // Build options list
        val options = mutableListOf<PollOption>()
        val optionCount = optionsJson.length()
        
        for (i in 1..optionCount) {
            val optionKey = i.toString()
            val optionText = optionsJson.optString(optionKey, "")
            
            if (optionText.isNotEmpty()) {
                // Get vote information from results
                val optionResult = resultsOptions?.optJSONObject(optionKey)
                val voteCount = optionResult?.optInt(PollJsonFields.COUNT, 0) ?: 0
                val votersJson = optionResult?.optJSONObject(PollJsonFields.VOTERS)
                
                // Extract voter information (up to 3 voters for display)
                val voters = mutableListOf<VoterInfo>()
                var isSelected = false
                
                if (votersJson != null) {
                    val voterKeys = votersJson.keys()
                    var voterCount = 0
                    while (voterKeys.hasNext()) {
                        val voterUid = voterKeys.next()
                        
                        // Check if logged-in user voted for this option
                        if (voterUid == loggedInUserUid) {
                            isSelected = true
                        }
                        
                        // Only extract first 3 voters for display
                        if (voterCount < 3) {
                            val voterData = votersJson.optJSONObject(voterUid)
                            val voterName = voterData?.optString(PollJsonFields.NAME, voterUid) ?: voterUid
                            val voterAvatar = voterData?.optString(PollJsonFields.AVATAR, null)
                            
                            voters.add(
                                VoterInfo(
                                    uid = voterUid,
                                    name = voterName,
                                    avatarUrl = voterAvatar
                                )
                            )
                            voterCount++
                        }
                    }
                }
                
                options.add(
                    PollOption(
                        id = optionKey,
                        text = optionText,
                        voteCount = voteCount,
                        voters = voters,
                        isSelected = isSelected
                    )
                )
            }
        }
        
        PollData(
            id = pollId,
            question = question,
            options = options,
            totalVotes = totalVotes
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Extracts poll results from message metadata.
 *
 * Poll results are stored in the message metadata under:
 * metadata.@injected.extensions.polls.results
 *
 * @param message The CustomMessage to extract results from
 * @return JSONObject containing poll results, or null if not found
 */
private fun getPollResults(message: CustomMessage): JSONObject? {
    return try {
        val metadata = message.metadata ?: return null
        val injected = metadata.optJSONObject(PollJsonFields.INJECTED) ?: return null
        val extensions = injected.optJSONObject(PollJsonFields.EXTENSIONS) ?: return null
        val polls = extensions.optJSONObject(PollJsonFields.POLLS) ?: return null
        polls.optJSONObject(PollJsonFields.RESULTS)
    } catch (e: Exception) {
        null
    }
}

/**
 * Determines which option the logged-in user has voted for.
 *
 * @param message The CustomMessage containing poll data
 * @return The 1-indexed position of the voted option, or 0 if the user hasn't voted
 */
fun getUserVotedOption(message: CustomMessage): Int {
    val pollData = extractPollData(message) ?: return 0
    
    for ((index, option) in pollData.options.withIndex()) {
        if (option.isSelected) {
            return index + 1 // Return 1-indexed position
        }
    }
    
    return 0
}

/**
 * Calculates the vote percentage for a given option.
 *
 * @param voteCount The number of votes for the option
 * @param totalVotes The total number of votes in the poll
 * @return The percentage as an integer (0-100), or 0 if totalVotes is 0
 */
fun calculateVotePercentage(voteCount: Int, totalVotes: Int): Int {
    return if (totalVotes > 0) {
        Math.round((voteCount.toFloat() * 100) / totalVotes)
    } else {
        0
    }
}

/**
 * Extension function to check if a CustomMessage is a poll message.
 *
 * @return true if the message contains poll data, false otherwise
 */
fun CustomMessage.isPollMessage(): Boolean {
    return try {
        val customData = this.customData ?: return false
        customData.has(PollJsonFields.QUESTION) && customData.has(PollJsonFields.OPTIONS)
    } catch (e: Exception) {
        false
    }
}
