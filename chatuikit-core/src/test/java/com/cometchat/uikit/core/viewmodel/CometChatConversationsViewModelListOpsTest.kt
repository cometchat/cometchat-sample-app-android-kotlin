package com.cometchat.uikit.core.viewmodel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Unit tests for ListOperations interface implementation pattern.
 * 
 * Since CometChatConversationsViewModel depends on SDK classes with private constructors,
 * we test the interface implementation pattern using a test ViewModel that mirrors the
 * production implementation.
 * 
 * This validates:
 * - Interface methods correctly delegate to ListOperationsDelegate
 * - Custom equality checker works correctly
 * - Batch operations work through the interface
 * - StateFlow emissions on list changes
 */
class CometChatConversationsViewModelListOpsTest : FunSpec({
    
    /**
     * Test data class that simulates Conversation with conversationId-based equality.
     */
    data class TestConversation(
        val conversationId: String,
        val name: String = "Test"
    )
    
    /**
     * Test ViewModel that implements ListOperations using the same pattern as
     * CometChatConversationsViewModel. This validates the interface + delegation pattern.
     */
    class TestListViewModel : ListOperations<TestConversation> {
        private val _items = MutableStateFlow<List<TestConversation>>(emptyList())
        val items = _items
        
        private val listDelegate = ListOperationsDelegate(
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
    
    context("Interface implementation pattern") {
        
        test("addItem should add conversation to list") {
            val viewModel = TestListViewModel()
            val conv = TestConversation("conv1", "Alice")
            
            viewModel.addItem(conv)
            
            viewModel.getItemCount() shouldBe 1
            viewModel.getItemAt(0)?.conversationId shouldBe "conv1"
        }
        
        test("addItems should add multiple conversations") {
            val viewModel = TestListViewModel()
            val conversations = listOf(
                TestConversation("conv1"),
                TestConversation("conv2"),
                TestConversation("conv3")
            )
            
            viewModel.addItems(conversations)
            
            viewModel.getItemCount() shouldBe 3
        }
        
        test("removeItem should use conversationId equality") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1", "Alice"),
                TestConversation("conv2", "Bob")
            ))
            
            // Create different instance with same conversationId
            val toRemove = TestConversation("conv1", "Different Name")
            val result = viewModel.removeItem(toRemove)
            
            result shouldBe true
            viewModel.getItemCount() shouldBe 1
            viewModel.getItemAt(0)?.conversationId shouldBe "conv2"
        }
        
        test("removeItem should return false when not found") {
            val viewModel = TestListViewModel()
            viewModel.addItem(TestConversation("conv1"))
            
            val result = viewModel.removeItem(TestConversation("nonexistent"))
            
            result shouldBe false
            viewModel.getItemCount() shouldBe 1
        }
        
        test("removeItemAt should remove at valid index") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1"),
                TestConversation("conv2"),
                TestConversation("conv3")
            ))
            
            val removed = viewModel.removeItemAt(1)
            
            removed?.conversationId shouldBe "conv2"
            viewModel.getItemCount() shouldBe 2
        }
        
        test("removeItemAt should return null for invalid index") {
            val viewModel = TestListViewModel()
            viewModel.addItem(TestConversation("conv1"))
            
            viewModel.removeItemAt(-1) shouldBe null
            viewModel.removeItemAt(10) shouldBe null
        }
        
        test("updateItem should replace matching item") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1", "Alice"),
                TestConversation("conv2", "Bob")
            ))
            
            val updated = TestConversation("conv1", "Alice Updated")
            val result = viewModel.updateItem(updated) { it.conversationId == "conv1" }
            
            result shouldBe true
            viewModel.getItemAt(0)?.name shouldBe "Alice Updated"
        }
        
        test("clearItems should empty the list") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1"),
                TestConversation("conv2")
            ))
            
            viewModel.clearItems()
            
            viewModel.getItemCount() shouldBe 0
        }
        
        test("getItems should return copy of all items") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1"),
                TestConversation("conv2")
            ))
            
            val items = viewModel.getItems()
            
            items.size shouldBe 2
            items.map { it.conversationId } shouldBe listOf("conv1", "conv2")
        }
        
        test("moveItemToTop should move existing item to index 0") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1"),
                TestConversation("conv2"),
                TestConversation("conv3")
            ))
            
            viewModel.moveItemToTop(TestConversation("conv3"))
            
            viewModel.getItemAt(0)?.conversationId shouldBe "conv3"
            viewModel.getItemAt(1)?.conversationId shouldBe "conv1"
            viewModel.getItemAt(2)?.conversationId shouldBe "conv2"
        }
        
        test("moveItemToTop should add non-existing item at top") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1"),
                TestConversation("conv2")
            ))
            
            viewModel.moveItemToTop(TestConversation("conv3"))
            
            viewModel.getItemCount() shouldBe 3
            viewModel.getItemAt(0)?.conversationId shouldBe "conv3"
        }
    }
    
    context("Batch operations through interface") {
        
        test("batch should perform multiple operations atomically") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1"),
                TestConversation("conv2")
            ))
            
            viewModel.batch {
                add(TestConversation("conv3"))
                add(TestConversation("conv4"))
                remove(TestConversation("conv1"))
            }
            
            viewModel.getItemCount() shouldBe 3
            viewModel.getItems().map { it.conversationId } shouldBe listOf("conv2", "conv3", "conv4")
        }
        
        test("batch should support all operations") {
            val viewModel = TestListViewModel()
            
            viewModel.batch {
                add(TestConversation("conv1"))
                addAll(listOf(TestConversation("conv2"), TestConversation("conv3")))
                moveToTop(TestConversation("conv3"))
            }
            
            viewModel.getItemCount() shouldBe 3
            viewModel.getItemAt(0)?.conversationId shouldBe "conv3"
        }
        
        test("batch clear should empty the list") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1"),
                TestConversation("conv2")
            ))
            
            viewModel.batch { clear() }
            
            viewModel.getItemCount() shouldBe 0
        }
        
        test("batch update should replace matching item") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1", "Original"),
                TestConversation("conv2", "Bob")
            ))
            
            viewModel.batch {
                update(TestConversation("conv1", "Updated")) { it.conversationId == "conv1" }
            }
            
            viewModel.getItemAt(0)?.name shouldBe "Updated"
        }
        
        test("batch removeAt should remove at index") {
            val viewModel = TestListViewModel()
            viewModel.addItems(listOf(
                TestConversation("conv1"),
                TestConversation("conv2"),
                TestConversation("conv3")
            ))
            
            viewModel.batch { removeAt(1) }
            
            viewModel.getItemCount() shouldBe 2
            viewModel.getItems().map { it.conversationId } shouldBe listOf("conv1", "conv3")
        }
    }
    
    context("StateFlow emissions") {
        
        test("StateFlow should reflect list changes") {
            val viewModel = TestListViewModel()
            
            viewModel.items.value.size shouldBe 0
            
            viewModel.addItem(TestConversation("conv1"))
            viewModel.items.value.size shouldBe 1
            
            viewModel.addItem(TestConversation("conv2"))
            viewModel.items.value.size shouldBe 2
            
            viewModel.removeItem(TestConversation("conv1"))
            viewModel.items.value.size shouldBe 1
            viewModel.items.value[0].conversationId shouldBe "conv2"
        }
        
        test("StateFlow should update after batch operations") {
            val viewModel = TestListViewModel()
            
            viewModel.batch {
                add(TestConversation("conv1"))
                add(TestConversation("conv2"))
            }
            
            viewModel.items.value.size shouldBe 2
        }
    }
    
    context("Custom equality checker") {
        
        test("should use conversationId for equality comparison") {
            val viewModel = TestListViewModel()
            
            // Add with specific name
            viewModel.addItem(TestConversation("conv1", "Original Name"))
            
            // Remove with different name but same ID
            val result = viewModel.removeItem(TestConversation("conv1", "Different Name"))
            
            result shouldBe true
            viewModel.getItemCount() shouldBe 0
        }
        
        test("should not match different conversationIds") {
            val viewModel = TestListViewModel()
            viewModel.addItem(TestConversation("conv1", "Same Name"))
            
            // Try to remove with same name but different ID
            val result = viewModel.removeItem(TestConversation("conv2", "Same Name"))
            
            result shouldBe false
            viewModel.getItemCount() shouldBe 1
        }
    }
})
