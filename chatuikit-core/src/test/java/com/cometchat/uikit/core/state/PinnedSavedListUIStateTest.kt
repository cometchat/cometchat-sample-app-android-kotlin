package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for the sealed UI state shared by the Pinned and Saved Messages screens.
 */
class PinnedSavedListUIStateTest : FunSpec({

    test("the four states are distinct") {
        val states: List<PinnedSavedListUIState> = listOf(
            PinnedSavedListUIState.Loading,
            PinnedSavedListUIState.Content,
            PinnedSavedListUIState.Empty,
            PinnedSavedListUIState.Error(CometChatException("ERR", "boom"))
        )
        states.distinct() shouldBe states
    }

    test("Error carries its exception and equals another Error with the same exception") {
        val exception = CometChatException("ERR_LIMIT", "cap reached")
        val error = PinnedSavedListUIState.Error(exception)

        error.exception shouldBe exception
        error shouldBe PinnedSavedListUIState.Error(exception)
        error shouldNotBe PinnedSavedListUIState.Error(CometChatException("OTHER", "different"))
    }

    test("states are exhaustively matchable as PinnedSavedListUIState") {
        fun describe(state: PinnedSavedListUIState): String = when (state) {
            PinnedSavedListUIState.Loading -> "loading"
            PinnedSavedListUIState.Content -> "content"
            PinnedSavedListUIState.Empty -> "empty"
            is PinnedSavedListUIState.Error -> "error:${state.exception.code}"
        }

        describe(PinnedSavedListUIState.Loading) shouldBe "loading"
        describe(PinnedSavedListUIState.Content) shouldBe "content"
        describe(PinnedSavedListUIState.Empty) shouldBe "empty"
        describe(PinnedSavedListUIState.Error(CometChatException("E1", "x"))).shouldBeInstanceOf<String>()
    }
})
