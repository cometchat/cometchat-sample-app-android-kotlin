package com.cometchat.uikit.core.data.datasource

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for MessageHeaderDataSourceImpl.
 * Layer 1 — bridges CometChat SDK static methods to coroutines.
 *
 * Note: MessageHeaderDataSourceImpl uses CometChat.getUser() and CometChat.getGroup()
 * which are static methods that cannot be mocked without SDK initialization.
 * The actual data fetching logic is tested at the Repository layer
 * (MessageHeaderRepositoryImplTest) which mocks the DataSource interface.
 *
 * This test file verifies the DataSource implementation exists and conforms
 * to the interface contract. Full behavior testing happens via:
 * - MessageHeaderRepositoryImplTest (mocks DataSource interface)
 * - MessageHeaderFullChainIntegrationTest (uses fake DataSource implementation)
 *
 * Reference: ConversationListDataSourceImplTest.kt (same pattern for static methods)
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageHeaderDataSourceImplTest"
 */
class MessageHeaderDataSourceImplTest : FunSpec({

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    test("MessageHeaderDataSourceImpl should implement MessageHeaderDataSource interface") {
        val dataSource = MessageHeaderDataSourceImpl()
        println("    → Created MessageHeaderDataSourceImpl instance")
        dataSource.shouldBeInstanceOf<MessageHeaderDataSource>()
        println("    → Verified: implements MessageHeaderDataSource interface")
    }

    test("MessageHeaderDataSourceImpl should be instantiable without parameters") {
        val dataSource = MessageHeaderDataSourceImpl()
        println("    → Instantiated without parameters")
        dataSource.shouldBeInstanceOf<MessageHeaderDataSourceImpl>()
    }
})
