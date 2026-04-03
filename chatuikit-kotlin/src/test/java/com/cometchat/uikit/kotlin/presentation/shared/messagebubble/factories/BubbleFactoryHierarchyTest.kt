package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.factories

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockito.Mockito.mock

/**
 * Pure JVM tests for BubbleFactory backward compatibility.
 *
 * Feature: kotlin-message-bubble-parity
 */
class BubbleFactoryHierarchyTest : FunSpec({

    /**
     * A custom factory extending BubbleFactory directly should continue to work
     * and return null for all optional slot views (backward compatibility).
     *
     * **Validates: Requirements 6.2, 6.3**
     */
    test("Custom factory extending BubbleFactory directly returns null for all slot views") {
        val customFactory = object : BubbleFactory() {
            override fun createContentView(context: Context): View {
                return mock(View::class.java)
            }

            override fun bindContentView(
                view: View,
                message: BaseMessage,
                alignment: UIKitConstants.MessageBubbleAlignment,
                holder: RecyclerView.ViewHolder?,
                position: Int
            ) {
                // no-op for test
            }
        }

        val mockContext = mock(Context::class.java)

        // All optional slot create methods should return null
        customFactory.createLeadingView(mockContext).shouldBeNull()
        customFactory.createHeaderView(mockContext).shouldBeNull()
        customFactory.createReplyView(mockContext).shouldBeNull()
        customFactory.createBottomView(mockContext).shouldBeNull()
        customFactory.createStatusInfoView(mockContext).shouldBeNull()
        customFactory.createThreadView(mockContext).shouldBeNull()
        customFactory.createFooterView(mockContext).shouldBeNull()

        customFactory.shouldBeInstanceOf<BubbleFactory>()
    }
})
