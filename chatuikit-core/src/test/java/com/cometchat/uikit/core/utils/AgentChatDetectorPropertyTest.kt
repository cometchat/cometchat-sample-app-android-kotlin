package com.cometchat.uikit.core.utils

import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.forAll
import io.kotest.property.PropTestConfig
import io.kotest.common.ExperimentalKotest

/**
 * Feature: agentic-workflow-improvements
 * Property 15: AgentChatDetector Correctness
 *
 * Validates: Requirements 9.2
 *
 * For any User, AgentChatDetector.isAgentChat(user) SHALL return true if and only if
 * user.uid starts with the UIKitConstants.AIConstants.AGENTIC_USER prefix ("@agentic").
 */
@OptIn(ExperimentalKotest::class)
class AgentChatDetectorPropertyTest : StringSpec({

    val prefix = UIKitConstants.AIConstants.AGENTIC_USER

    "Property 15: isAgentChat returns false for any user whose uid does NOT start with @agentic" {
        forAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..200).filterNot { it.startsWith(prefix) }
        ) { uid ->
            val user = User()
            user.uid = uid
            !AgentChatDetector.isAgentChat(user)
        }
    }
})
