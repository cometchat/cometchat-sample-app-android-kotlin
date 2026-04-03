package com.cometchat.uikit.compose.presentation.shared.messagebubble

import androidx.compose.runtime.Composable
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: compose-bubble-factory-api, Property 1: List-to-map key correctness
// Validates: Requirements 2.1

/**
 * Minimal stub implementation of [BubbleFactory] for property testing.
 * Only [getCategory] and [getType] are meaningful; all slot methods use defaults.
 */
private class StubFactory(
    private val category: String,
    private val type: String
) : BubbleFactory {
    override fun getCategory(): String = category
    override fun getType(): String = type

    @Composable
    override fun ContentView(
        message: BaseMessage,
        alignment: UIKitConstants.MessageBubbleAlignment,
        style: CometChatMessageBubbleStyle,
        textFormatters: List<CometChatTextFormatter>
    ) {
        // no-op stub
    }
}

/**
 * Property-based tests for [toFactoryMap] extension function.
 *
 * **Feature: compose-bubble-factory-api, Property 1: List-to-map key correctness**
 *
 * For any list of ComposeBubbleFactory instances, converting the list to a factory map
 * via toFactoryMap() should produce a map where every key equals
 * "${factory.getCategory()}_${factory.getType()}" for the corresponding factory value,
 * and every factory in the input list has a corresponding key in the output map
 * (unless overridden by a later duplicate).
 *
 * **Validates: Requirements 2.1**
 */
class ToFactoryMapPropertyTest : FunSpec({

    // Feature: compose-bubble-factory-api, Property 1: List-to-map key correctness
    // Validates: Requirements 2.1

    // Feature: compose-bubble-factory-api, Property 2: Last-wins duplicate resolution
    // Validates: Requirements 2.2

    context("Property 2: Last-wins duplicate resolution") {

        test("map value for a duplicate key is the last factory in the list with that category+type") {
            checkAll(100, Arb.string(1..10), Arb.string(1..10)) { category, type ->
                // Create multiple factories sharing the same category+type
                val first = StubFactory(category = category, type = type)
                val second = StubFactory(category = category, type = type)
                val last = StubFactory(category = category, type = type)

                val factories = listOf(first, second, last)
                val map = factories.toFactoryMap()

                val expectedKey = "${category}_${type}"
                map[expectedKey] shouldBe last
            }
        }

        test("when duplicates are interleaved with unique factories, last duplicate wins") {
            checkAll(
                100,
                Arb.string(1..10),
                Arb.string(1..10),
                Arb.list(Arb.string(1..10), 1..10)
            ) { dupCategory, dupType, uniqueSuffixes ->
                // Build a list with intentional duplicates interleaved with unique entries
                val duplicateFirst = StubFactory(category = dupCategory, type = dupType)
                val uniqueFactories = uniqueSuffixes.mapIndexed { i, suffix ->
                    StubFactory(category = "unique_${suffix}_$i", type = "utype_$i")
                }
                val duplicateLast = StubFactory(category = dupCategory, type = dupType)

                val factories = listOf(duplicateFirst) + uniqueFactories + listOf(duplicateLast)
                val map = factories.toFactoryMap()

                val dupKey = "${dupCategory}_${dupType}"
                map[dupKey] shouldBe duplicateLast
            }
        }

        test("map size equals the number of distinct category_type keys") {
            checkAll(
                100,
                Arb.list(Arb.string(1..5), 1..20),
                Arb.list(Arb.string(1..5), 1..20)
            ) { rawCategories, rawTypes ->
                val size = minOf(rawCategories.size, rawTypes.size)
                val factories = (0 until size).map { i ->
                    StubFactory(category = rawCategories[i], type = rawTypes[i])
                }

                val map = factories.toFactoryMap()

                val distinctKeys = factories.map { "${it.getCategory()}_${it.getType()}" }.toSet()
                map.size shouldBe distinctKeys.size
            }
        }
    }

    context("Property 1: List-to-map key correctness") {

        test("every key in the map matches category_type of its value") {
            checkAll(100, Arb.list(Arb.string(1..20), 0..30)) { categories ->
                // Generate a list of stub factories with random category/type pairs
                val types = List(categories.size) { "type_$it" }
                val factories = categories.zip(types).map { (cat, typ) ->
                    StubFactory(category = cat, type = typ)
                }

                val map = factories.toFactoryMap()

                // Assert every key matches "category_type" of its value
                for ((key, factory) in map) {
                    key shouldBe "${factory.getCategory()}_${factory.getType()}"
                }
            }
        }

        test("every factory in the input list has a corresponding key in the output map") {
            checkAll(
                100,
                Arb.list(Arb.string(1..20), 0..30),
                Arb.list(Arb.string(1..20), 0..30)
            ) { rawCategories, rawTypes ->
                // Ensure both lists are the same size
                val size = minOf(rawCategories.size, rawTypes.size)
                val factories = (0 until size).map { i ->
                    StubFactory(category = rawCategories[i], type = rawTypes[i])
                }

                val map = factories.toFactoryMap()

                // Every factory's expected key should exist in the map
                for (factory in factories) {
                    val expectedKey = "${factory.getCategory()}_${factory.getType()}"
                    map.containsKey(expectedKey) shouldBe true
                }
            }
        }

        test("map value for each key is a factory with matching category and type") {
            checkAll(
                100,
                Arb.list(Arb.string(1..20), 1..30),
                Arb.list(Arb.string(1..20), 1..30)
            ) { rawCategories, rawTypes ->
                val size = minOf(rawCategories.size, rawTypes.size)
                val factories = (0 until size).map { i ->
                    StubFactory(category = rawCategories[i], type = rawTypes[i])
                }

                val map = factories.toFactoryMap()

                // For every entry, the value's category+type must reconstruct the key
                for ((key, factory) in map) {
                    val reconstructedKey = "${factory.getCategory()}_${factory.getType()}"
                    reconstructedKey shouldBe key
                }
            }
        }

        test("map size is at most the input list size") {
            checkAll(
                100,
                Arb.list(Arb.string(1..20), 0..30),
                Arb.list(Arb.string(1..20), 0..30)
            ) { rawCategories, rawTypes ->
                val size = minOf(rawCategories.size, rawTypes.size)
                val factories = (0 until size).map { i ->
                    StubFactory(category = rawCategories[i], type = rawTypes[i])
                }

                val map = factories.toFactoryMap()

                // Map can't have more entries than input factories
                (map.size <= factories.size) shouldBe true
            }
        }
    }
})
