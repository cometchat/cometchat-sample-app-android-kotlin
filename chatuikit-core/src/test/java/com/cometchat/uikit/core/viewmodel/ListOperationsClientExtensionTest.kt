package com.cometchat.uikit.core.viewmodel

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Tests for client extension capability of ListOperations interface.
 * 
 * Validates Requirements 3.2, 3.3, 3.4, 3.5, 3.6:
 * - Client can override addItem with pre-processing
 * - Client can override removeItem with post-processing
 * - Super calls work correctly
 * - Exceptions propagate from overrides
 * - Client can override batch for custom batching logic
 */
class ListOperationsClientExtensionTest : FunSpec({
    
    /**
     * Test data class simulating Conversation.
     */
    data class TestConversation(
        val conversationId: String,
        val name: String = "Test"
    )
    
    /**
     * Base ViewModel implementing ListOperations (simulates CometChatConversationsViewModel).
     */
    open class BaseListViewModel : ListOperations<TestConversation> {
        private val _items = MutableStateFlow<List<TestConversation>>(emptyList())
        val items = _items
        
        protected val listDelegate = ListOperationsDelegate(
            stateFlow = _items,
            equalityChecker = { a, b -> a.conversationId == b.conversationId }
        )
        
        override fun addItem(item: TestConversation) = listDelegate.addItem(item)
        override fun addItems(items: List<TestConversation>) = listDelegate.addItems(items)
        override fun removeItem(item: TestConversation) = listDelegate.removeItem(item)
        override fun removeItemAt(index: Int) = listDelegate.removeItemAt(index)
        override fun updateItem(item: TestConversation, predicate: (TestConversation) -> Boolean) = 
            listDelegate.updateItem(item, predicate)
        override fun clearItems() = listDelegate.clearItems()
        override fun getItems() = listDelegate.getItems()
        override fun getItemAt(index: Int) = listDelegate.getItemAt(index)
        override fun getItemCount() = listDelegate.getItemCount()
        override fun moveItemToTop(item: TestConversation) = listDelegate.moveItemToTop(item)
        override fun batch(operations: ListOperationsBatchScope<TestConversation>.() -> Unit) = 
            listDelegate.batch(operations)
    }
    
    context("Client override with pre-processing (Requirement 3.2)") {
        
        test("client can add validation before addItem") {
            val validationLog = mutableListOf<String>()
            
            class ValidatingViewModel : BaseListViewModel() {
                override fun addItem(item: TestConversation) {
                    // Pre-processing: validate item
                    if (item.conversationId.isBlank()) {
                        validationLog.add("Rejected: blank ID")
                        return
                    }
                    validationLog.add("Validated: ${item.conversationId}")
                    super.addItem(item)
                }
            }
            
            val viewModel = ValidatingViewModel()
            
            viewModel.addItem(TestConversation("conv1"))
            viewModel.addItem(TestConversation(""))  // Should be rejected
            viewModel.addItem(TestConversation("conv2"))
            
            viewModel.getItemCount() shouldBe 2
            validationLog shouldBe listOf("Validated: conv1", "Rejected: blank ID", "Validated: conv2")
        }
        
        test("client can transform item before adding") {
            class TransformingViewModel : BaseListViewModel() {
                override fun addItem(item: TestConversation) {
                    // Pre-processing: transform item name to uppercase
                    val transformed = item.copy(name = item.name.uppercase())
                    super.addItem(transformed)
                }
            }
            
            val viewModel = TransformingViewModel()
            viewModel.addItem(TestConversation("conv1", "alice"))
            
            viewModel.getItemAt(0)?.name shouldBe "ALICE"
        }
    }
    
    context("Client override with post-processing (Requirement 3.3)") {
        
        test("client can add logging after removeItem") {
            val removalLog = mutableListOf<String>()
            
            class LoggingViewModel : BaseListViewModel() {
                override fun removeItem(item: TestConversation): Boolean {
                    val result = super.removeItem(item)
                    // Post-processing: log removal
                    if (result) {
                        removalLog.add("Removed: ${item.conversationId}")
                    } else {
                        removalLog.add("Not found: ${item.conversationId}")
                    }
                    return result
                }
            }
            
            val viewModel = LoggingViewModel()
            viewModel.addItem(TestConversation("conv1"))
            viewModel.addItem(TestConversation("conv2"))
            
            viewModel.removeItem(TestConversation("conv1"))
            viewModel.removeItem(TestConversation("nonexistent"))
            
            removalLog shouldBe listOf("Removed: conv1", "Not found: nonexistent")
        }
        
        test("client can trigger side effects after clearItems") {
            var clearCount = 0
            
            class TrackingViewModel : BaseListViewModel() {
                override fun clearItems() {
                    super.clearItems()
                    // Post-processing: track clear operations
                    clearCount++
                }
            }
            
            val viewModel = TrackingViewModel()
            viewModel.addItems(listOf(TestConversation("conv1"), TestConversation("conv2")))
            viewModel.clearItems()
            viewModel.clearItems()  // Clear again (no-op but still tracked)
            
            clearCount shouldBe 2
        }
    }
    
    context("Super calls work correctly (Requirement 3.4)") {
        
        test("super.addItem adds item to list") {
            class ExtendedViewModel : BaseListViewModel() {
                var preProcessCalled = false
                var postProcessCalled = false
                
                override fun addItem(item: TestConversation) {
                    preProcessCalled = true
                    super.addItem(item)
                    postProcessCalled = true
                }
            }
            
            val viewModel = ExtendedViewModel()
            viewModel.addItem(TestConversation("conv1"))
            
            viewModel.preProcessCalled shouldBe true
            viewModel.postProcessCalled shouldBe true
            viewModel.getItemCount() shouldBe 1
        }
        
        test("super.updateItem updates item correctly") {
            class ExtendedViewModel : BaseListViewModel() {
                var updateCount = 0
                
                override fun updateItem(item: TestConversation, predicate: (TestConversation) -> Boolean): Boolean {
                    updateCount++
                    return super.updateItem(item, predicate)
                }
            }
            
            val viewModel = ExtendedViewModel()
            viewModel.addItem(TestConversation("conv1", "Original"))
            viewModel.updateItem(TestConversation("conv1", "Updated")) { it.conversationId == "conv1" }
            
            viewModel.updateCount shouldBe 1
            viewModel.getItemAt(0)?.name shouldBe "Updated"
        }
    }
    
    context("Exceptions propagate from overrides (Requirement 3.5)") {
        
        test("exception in addItem override propagates to caller") {
            class ThrowingViewModel : BaseListViewModel() {
                override fun addItem(item: TestConversation) {
                    if (item.name == "invalid") {
                        throw IllegalArgumentException("Invalid conversation name")
                    }
                    super.addItem(item)
                }
            }
            
            val viewModel = ThrowingViewModel()
            
            shouldThrow<IllegalArgumentException> {
                viewModel.addItem(TestConversation("conv1", "invalid"))
            }.message shouldBe "Invalid conversation name"
            
            // List should be unchanged
            viewModel.getItemCount() shouldBe 0
        }
        
        test("exception in removeItem override propagates to caller") {
            class ThrowingViewModel : BaseListViewModel() {
                override fun removeItem(item: TestConversation): Boolean {
                    if (item.conversationId == "protected") {
                        throw UnsupportedOperationException("Cannot remove protected conversation")
                    }
                    return super.removeItem(item)
                }
            }
            
            val viewModel = ThrowingViewModel()
            viewModel.addItem(TestConversation("protected"))
            
            shouldThrow<UnsupportedOperationException> {
                viewModel.removeItem(TestConversation("protected"))
            }.message shouldBe "Cannot remove protected conversation"
            
            // Item should still be in list
            viewModel.getItemCount() shouldBe 1
        }
    }

    
    context("Client can override batch for custom batching logic (Requirement 3.6)") {
        
        test("client can add logging around batch operations") {
            val batchLog = mutableListOf<String>()
            
            class LoggingBatchViewModel : BaseListViewModel() {
                override fun batch(operations: ListOperationsBatchScope<TestConversation>.() -> Unit) {
                    batchLog.add("Batch started")
                    super.batch(operations)
                    batchLog.add("Batch completed with ${getItemCount()} items")
                }
            }
            
            val viewModel = LoggingBatchViewModel()
            viewModel.batch {
                add(TestConversation("conv1"))
                add(TestConversation("conv2"))
            }
            
            batchLog shouldBe listOf("Batch started", "Batch completed with 2 items")
        }
        
        test("client can wrap batch with transaction-like behavior") {
            var transactionActive = false
            
            class TransactionalViewModel : BaseListViewModel() {
                override fun batch(operations: ListOperationsBatchScope<TestConversation>.() -> Unit) {
                    transactionActive = true
                    try {
                        super.batch(operations)
                    } finally {
                        transactionActive = false
                    }
                }
                
                fun isTransactionActive() = transactionActive
            }
            
            val viewModel = TransactionalViewModel()
            
            viewModel.isTransactionActive() shouldBe false
            
            viewModel.batch {
                add(TestConversation("conv1"))
            }
            
            viewModel.isTransactionActive() shouldBe false
            viewModel.getItemCount() shouldBe 1
        }
        
        test("client can add validation to batch operations") {
            class ValidatingBatchViewModel : BaseListViewModel() {
                var batchValidated = false
                
                override fun batch(operations: ListOperationsBatchScope<TestConversation>.() -> Unit) {
                    // Validate before batch
                    batchValidated = true
                    super.batch(operations)
                }
            }
            
            val viewModel = ValidatingBatchViewModel()
            viewModel.batch {
                add(TestConversation("conv1"))
                add(TestConversation("conv2"))
                remove(TestConversation("conv1"))
            }
            
            viewModel.batchValidated shouldBe true
            viewModel.getItemCount() shouldBe 1
        }
    }
    
    context("Complex client extension scenarios") {
        
        test("client can combine multiple overrides") {
            val operationLog = mutableListOf<String>()
            
            class FullyExtendedViewModel : BaseListViewModel() {
                override fun addItem(item: TestConversation) {
                    operationLog.add("add:${item.conversationId}")
                    super.addItem(item)
                }
                
                override fun removeItem(item: TestConversation): Boolean {
                    operationLog.add("remove:${item.conversationId}")
                    return super.removeItem(item)
                }
                
                override fun clearItems() {
                    operationLog.add("clear")
                    super.clearItems()
                }
            }
            
            val viewModel = FullyExtendedViewModel()
            viewModel.addItem(TestConversation("conv1"))
            viewModel.addItem(TestConversation("conv2"))
            viewModel.removeItem(TestConversation("conv1"))
            viewModel.clearItems()
            
            operationLog shouldBe listOf("add:conv1", "add:conv2", "remove:conv1", "clear")
        }
        
        test("client can conditionally skip super call") {
            class ConditionalViewModel : BaseListViewModel() {
                var skipNextAdd = false
                
                override fun addItem(item: TestConversation) {
                    if (skipNextAdd) {
                        skipNextAdd = false
                        return  // Skip adding
                    }
                    super.addItem(item)
                }
            }
            
            val viewModel = ConditionalViewModel()
            viewModel.addItem(TestConversation("conv1"))
            viewModel.skipNextAdd = true
            viewModel.addItem(TestConversation("conv2"))  // Should be skipped
            viewModel.addItem(TestConversation("conv3"))
            
            viewModel.getItemCount() shouldBe 2
            viewModel.getItems().map { it.conversationId } shouldBe listOf("conv1", "conv3")
        }
        
        test("client can modify return value") {
            class ModifyingViewModel : BaseListViewModel() {
                override fun removeItem(item: TestConversation): Boolean {
                    val actualResult = super.removeItem(item)
                    // Always return true (for UI purposes, even if item wasn't found)
                    return true
                }
            }
            
            val viewModel = ModifyingViewModel()
            viewModel.addItem(TestConversation("conv1"))
            
            // Remove non-existent item - normally returns false
            val result = viewModel.removeItem(TestConversation("nonexistent"))
            
            // But our override returns true
            result shouldBe true
        }
    }
})
