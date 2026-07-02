package com.cometchat.uikit.core.viewmodel.createpoll

import com.cometchat.uikit.core.domain.usecase.CreatePollUseCase
import com.cometchat.uikit.core.viewmodel.CometChatCreatePollViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock

/**
 * Property-based tests for CreatePoll form validity invariants.
 *
 * Tests domain invariants using Kotest property-based testing:
 * - Form validity is determined solely by question + options state
 * - Options list size invariants (minimum 2, addOption increases by 1)
 * - removeOption never goes below 2
 * - Reset always returns to initial state
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.CreatePollFormValidityPropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CreatePollFormValidityPropertyTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()
    lateinit var createPollUseCase: CreatePollUseCase

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        createPollUseCase = mock()
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
    }

    // ==================== Form Validity Properties ====================

    test("PBT: isFormValid iff question.isNotBlank AND count(non-blank options) >= 2") {
        checkAll(
            100,
            Arb.string(0..50),
            Arb.string(0..30),
            Arb.string(0..30)
        ) { question, opt1, opt2 ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            vm.setQuestion(question)
            vm.updateOption(0, opt1)
            vm.updateOption(1, opt2)

            val expectedValid = question.isNotBlank() &&
                opt1.isNotBlank() &&
                opt2.isNotBlank()

            vm.isFormValid() shouldBe expectedValid
        }
    }

    test("PBT: isFormValid with 3+ options only needs 2 non-blank") {
        checkAll(
            50,
            Arb.string(1..30),
            Arb.string(1..20),
            Arb.string(1..20),
            Arb.string(0..20)
        ) { question, opt1, opt2, opt3 ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            vm.setQuestion(question)
            vm.updateOption(0, opt1)
            vm.updateOption(1, opt2)
            vm.addOption()
            vm.updateOption(2, opt3)

            // With question non-blank and opt1+opt2 non-blank, form is always valid
            // regardless of opt3
            vm.isFormValid() shouldBe true
        }
    }

    test("PBT: blank question always makes form invalid regardless of options") {
        checkAll(50, Arb.string(1..30), Arb.string(1..30)) { opt1, opt2 ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            vm.setQuestion("") // blank
            vm.updateOption(0, opt1)
            vm.updateOption(1, opt2)
            vm.isFormValid() shouldBe false

            vm.setQuestion("   ") // whitespace only
            vm.isFormValid() shouldBe false
        }
    }

    // ==================== Options Size Properties ====================

    test("PBT: addOption always increases size by exactly 1") {
        checkAll(100, Arb.int(0..20)) { addCount ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            val initialSize = vm.options.value.size // always 2

            repeat(addCount) { vm.addOption() }

            vm.options.value shouldHaveSize (initialSize + addCount)
        }
    }

    test("PBT: removeOption never reduces below 2") {
        checkAll(50, Arb.int(0..10), Arb.int(0..20)) { addCount, removeAttempts ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            repeat(addCount) { vm.addOption() }

            repeat(removeAttempts) { i ->
                vm.removeOption(0) // always try to remove first
            }

            (vm.options.value.size >= 2) shouldBe true
        }
    }

    test("PBT: after N adds and M removes, size = max(2, 2 + N - validRemoves)") {
        checkAll(30, Arb.int(0..8), Arb.int(0..12)) { adds, removes ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            repeat(adds) { vm.addOption() }

            val sizeAfterAdds = 2 + adds
            // Each remove only works if size > 2
            var expectedSize = sizeAfterAdds
            repeat(removes) {
                if (expectedSize > 2) expectedSize--
            }

            repeat(removes) { vm.removeOption(0) }

            vm.options.value.size shouldBe expectedSize
        }
    }

    // ==================== Reset Properties ====================

    test("PBT: reset always returns to initial state regardless of mutations") {
        checkAll(
            30,
            Arb.string(0..50),
            Arb.list(Arb.string(0..20), 0..5),
            Arb.int(0..5)
        ) { question, optionValues, extraAdds ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)

            // Apply random mutations
            vm.setQuestion(question)
            optionValues.forEachIndexed { index, value ->
                if (index < vm.options.value.size) {
                    vm.updateOption(index, value)
                }
            }
            repeat(extraAdds) { vm.addOption() }

            // Reset
            vm.reset()

            // Verify initial state
            vm.question.value shouldBe ""
            vm.options.value shouldHaveSize 2
            vm.options.value[0] shouldBe ""
            vm.options.value[1] shouldBe ""
        }
    }

    // ==================== updateOption Idempotency ====================

    test("PBT: updateOption at same index twice results in last value") {
        checkAll(50, Arb.int(0..1), Arb.string(0..20), Arb.string(0..20)) { index, val1, val2 ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            vm.updateOption(index, val1)
            vm.updateOption(index, val2)
            vm.options.value[index] shouldBe val2
        }
    }

    test("PBT: updateOption at invalid index never crashes or changes state") {
        checkAll(30, Arb.int(5..100), Arb.string(1..20)) { invalidIndex, value ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            val sizeBefore = vm.options.value.size
            vm.updateOption(invalidIndex, value)
            vm.options.value.size shouldBe sizeBefore
        }
    }

    // ==================== setQuestion Properties ====================

    test("PBT: setQuestion is always reflected in question StateFlow") {
        checkAll(100, Arb.string(0..200)) { question ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            vm.setQuestion(question)
            vm.question.value shouldBe question
        }
    }

    test("PBT: setQuestion does not affect options") {
        checkAll(30, Arb.string(0..50)) { question ->
            val vm = CometChatCreatePollViewModel(createPollUseCase)
            vm.updateOption(0, "Fixed A")
            vm.updateOption(1, "Fixed B")

            vm.setQuestion(question)

            vm.options.value[0] shouldBe "Fixed A"
            vm.options.value[1] shouldBe "Fixed B"
        }
    }
})
