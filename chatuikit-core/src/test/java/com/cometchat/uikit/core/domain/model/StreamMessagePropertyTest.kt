package com.cometchat.uikit.core.domain.model

import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.element
import io.kotest.property.forAll
import io.kotest.property.PropTestConfig
import io.kotest.common.ExperimentalKotest

/**
 * Feature: agentic-workflow-improvements
 * Property 24: StreamMessage Construction Invariants
 *
 * Validates: Requirements 16.2, 16.3
 *
 * For any StreamMessage constructed with valid receiverUid, receiverType, and text parameters,
 * the message's type SHALL be UIKitConstants.MessageType.STREAM, category SHALL be
 * UIKitConstants.MessageCategory.STREAM, and isStreamingInterrupted SHALL be false.
 */
@OptIn(ExperimentalKotest::class)
class StreamMessagePropertyTest : StringSpec({

    "Property 24: type is always STREAM for any valid construction parameters" {
        forAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..200),
            Arb.element("user", "group"),
            Arb.string(0..500)
        ) { receiverUid, receiverType, text ->
            val message = StreamMessage(receiverUid, receiverType, text)
            message.type == UIKitConstants.MessageType.STREAM
        }
    }

    "Property 24: category is always STREAM for any valid construction parameters" {
        forAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..200),
            Arb.element("user", "group"),
            Arb.string(0..500)
        ) { receiverUid, receiverType, text ->
            val message = StreamMessage(receiverUid, receiverType, text)
            message.category == UIKitConstants.MessageCategory.STREAM
        }
    }

    "Property 24: isStreamingInterrupted is always false on construction" {
        forAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..200),
            Arb.element("user", "group"),
            Arb.string(0..500)
        ) { receiverUid, receiverType, text ->
            val message = StreamMessage(receiverUid, receiverType, text)
            message.isStreamingInterrupted == false
        }
    }
})
