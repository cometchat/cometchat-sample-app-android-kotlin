package com.cometchat.uikit.compose.presentation.groupmembers.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.viewmodel.CometChatGroupMembersViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State holder for CometChatGroupMembers that exposes selection state.
 * Consumers can use this to programmatically access selected members.
 *
 * This class follows the Compose state holder pattern, allowing consumers to:
 * - Access selected members synchronously via [getSelectedMembers]
 * - Observe selection changes reactively via [selectedMembersFlow]
 *
 * The state holder is wired to the internal ViewModel when passed to the
 * CometChatGroupMembers composable via the `state` parameter.
 *
 * Usage:
 * ```kotlin
 * val groupMembersState = rememberCometChatGroupMembersState()
 *
 * CometChatGroupMembers(
 *     group = myGroup,
 *     selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
 *     state = groupMembersState
 * )
 *
 * // Access selected members programmatically
 * Button(onClick = {
 *     val selected = groupMembersState.getSelectedMembers()
 *     processSelectedMembers(selected)
 * }) {
 *     Text("Process Selection")
 * }
 * ```
 *
 * @see rememberCometChatGroupMembersState
 */
@Stable
class CometChatGroupMembersState {

    // Internal ViewModel reference - set when wired to CometChatGroupMembers
    private var viewModel: CometChatGroupMembersViewModel? = null

    // Fallback empty flow for when ViewModel is not yet set
    private val emptyFlow = MutableStateFlow<Map<String, GroupMember>>(emptyMap()).asStateFlow()

    /**
     * Returns the selected members as a StateFlow for reactive observation.
     *
     * This flow emits the current selection state as a Map where:
     * - Key: The member's UID (String)
     * - Value: The GroupMember object
     *
     * If the state holder is not yet wired to a ViewModel, returns an empty flow.
     *
     * Usage:
     * ```kotlin
     * val selectedMembers by groupMembersState.selectedMembersFlow.collectAsStateWithLifecycle()
     * ```
     */
    val selectedMembersFlow: StateFlow<Map<String, GroupMember>>
        get() = viewModel?.selectedMembers ?: emptyFlow

    /**
     * Returns the currently selected group members.
     *
     * This method provides a synchronous snapshot of the current selection state.
     * The returned list is independent of internal state - subsequent changes
     * to the selection will not affect the returned list.
     *
     * @return List of selected GroupMember objects. Returns an empty list if:
     *         - No members are selected
     *         - The state holder is not yet wired to a ViewModel
     */
    fun getSelectedMembers(): List<GroupMember> {
        return viewModel?.selectedMembers?.value?.values?.toList() ?: emptyList()
    }

    /**
     * Internal method to wire this state holder to the ViewModel.
     * Called by CometChatGroupMembers when the state parameter is provided.
     *
     * @param viewModel The CometChatGroupMembersViewModel to wire to
     */
    internal fun setViewModel(viewModel: CometChatGroupMembersViewModel) {
        this.viewModel = viewModel
    }
}

/**
 * Creates and remembers a [CometChatGroupMembersState] instance.
 *
 * This composable function creates a state holder that survives recomposition.
 * Pass the returned state to CometChatGroupMembers via the `state` parameter
 * to enable programmatic access to selection state.
 *
 * Usage:
 * ```kotlin
 * val groupMembersState = rememberCometChatGroupMembersState()
 *
 * CometChatGroupMembers(
 *     group = myGroup,
 *     selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
 *     state = groupMembersState
 * )
 *
 * // Access selected members
 * val selected = groupMembersState.getSelectedMembers()
 * ```
 *
 * @return A remembered CometChatGroupMembersState instance
 */
@Composable
fun rememberCometChatGroupMembersState(): CometChatGroupMembersState {
    return remember { CometChatGroupMembersState() }
}
