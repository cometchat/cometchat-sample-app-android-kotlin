package com.cometchat.uikit.compose.preview.presentation.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatGroupMemberShimmer
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatListItemShimmer
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatMessageListShimmer
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatReactionListShimmer
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatShimmerBox
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.ProvideShimmerAnimation
import com.cometchat.uikit.compose.theme.CometChatTheme

@Preview(showBackground = true, name = "Shimmer - List Item")
@Composable
fun PreviewShimmerListItem() {
    CometChatTheme {
        ProvideShimmerAnimation {
            Column(modifier = Modifier.padding(16.dp)) {
                CometChatListItemShimmer()
                CometChatListItemShimmer()
                CometChatListItemShimmer()
            }
        }
    }
}

@Preview(showBackground = true, name = "Shimmer - Message List")
@Composable
fun PreviewShimmerMessageList() {
    CometChatTheme {
        ProvideShimmerAnimation {
            CometChatMessageListShimmer(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, name = "Shimmer - Group Member")
@Composable
fun PreviewShimmerGroupMember() {
    CometChatTheme {
        ProvideShimmerAnimation {
            Column(modifier = Modifier.padding(16.dp)) {
                CometChatGroupMemberShimmer()
                CometChatGroupMemberShimmer()
                CometChatGroupMemberShimmer()
            }
        }
    }
}

@Preview(showBackground = true, name = "Shimmer - Reaction List")
@Composable
fun PreviewShimmerReactionList() {
    CometChatTheme {
        ProvideShimmerAnimation {
            CometChatReactionListShimmer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Shimmer - Box")
@Composable
fun PreviewShimmerBox() {
    CometChatTheme {
        ProvideShimmerAnimation {
            CometChatShimmerBox(
                width = 200.dp,
                height = 20.dp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
