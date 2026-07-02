package com.cometchat.uikit.kotlin.presentation.search

import com.cometchat.uikit.kotlin.presentation.search.adapter.CometChatSearchMessageListAdapterTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for the Search component (Kotlin/XML) presentation layer.
 *
 * Runs all search-related unit tests in one go.
 *
 * Usage:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "com.cometchat.uikit.kotlin.presentation.search.CometChatSearchTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    CometChatSearchViewTest::class,
    CometChatSearchMessageListAdapterTest::class
)
class CometChatSearchTestSuite
