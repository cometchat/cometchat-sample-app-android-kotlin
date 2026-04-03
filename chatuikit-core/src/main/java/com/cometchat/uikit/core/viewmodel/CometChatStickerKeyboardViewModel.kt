package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.usecase.GetStickersUseCase
import com.cometchat.uikit.core.state.StickerKeyboardUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing sticker keyboard state.
 * Handles fetching stickers, tab selection, and sticker click callbacks.
 * Uses StateFlow for reactive state management.
 *
 * This ViewModel is shared between chatuikit-jetpack (Compose) and
 * chatuikit-kotlin (XML Views) implementations.
 *
 * @param getStickersUseCase Use case for fetching stickers
 */
open class CometChatStickerKeyboardViewModel(
    private val getStickersUseCase: GetStickersUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "StickerKeyboardVM"
    }

    // ==================== UI State ====================

    /** Current UI state (Loading, Content, Empty, Error) */
    private val _uiState = MutableStateFlow<StickerKeyboardUIState>(StickerKeyboardUIState.Loading)
    val uiState: StateFlow<StickerKeyboardUIState> = _uiState.asStateFlow()

    // ==================== Sticker Data ====================

    /** List of all sticker sets */
    private val _stickerSets = MutableStateFlow<List<StickerSet>>(emptyList())
    val stickerSets: StateFlow<List<StickerSet>> = _stickerSets.asStateFlow()

    /** Index of the currently selected sticker set */
    private val _selectedSetIndex = MutableStateFlow(0)
    val selectedSetIndex: StateFlow<Int> = _selectedSetIndex.asStateFlow()

    /** Stickers in the currently selected set */
    private val _currentStickers = MutableStateFlow<List<Sticker>>(emptyList())
    val currentStickers: StateFlow<List<Sticker>> = _currentStickers.asStateFlow()

    // ==================== Callbacks ====================

    /** Callback invoked when a sticker is clicked */
    var onStickerClick: ((Sticker) -> Unit)? = null

    // ==================== State Flags ====================

    /** Flag to prevent concurrent fetch operations */
    private var isFetching = false

    // ==================== Initialization ====================

    init {
        fetchStickers()
    }

    // ==================== Public API Methods ====================

    /**
     * Fetches stickers from the server.
     * Updates UI state based on the result:
     * - Loading while fetching
     * - Content when stickers are available
     * - Empty when no stickers are available
     * - Error when fetch fails
     */
    fun fetchStickers() {
        // Prevent concurrent fetches
        if (isFetching) return

        isFetching = true
        _uiState.value = StickerKeyboardUIState.Loading

        viewModelScope.launch {
            getStickersUseCase()
                .onSuccess { sets ->
                    handleFetchSuccess(sets)
                }
                .onFailure { exception ->
                    handleFetchError(exception as CometChatException)
                }

            isFetching = false
        }
    }

    /**
     * Selects a sticker set by index.
     * Updates the selectedSetIndex and currentStickers StateFlows.
     *
     * @param index The index of the sticker set to select
     */
    fun selectStickerSet(index: Int) {
        val sets = _stickerSets.value
        if (index < 0 || index >= sets.size) return

        _selectedSetIndex.value = index
        _currentStickers.value = sets[index].stickers
    }

    /**
     * Retries fetching stickers after an error.
     * Resets the UI state to Loading and fetches again.
     */
    fun retry() {
        fetchStickers()
    }

    /**
     * Handles a sticker click event.
     * Invokes the onStickerClick callback with the clicked sticker.
     *
     * @param sticker The sticker that was clicked
     */
    fun onStickerClicked(sticker: Sticker) {
        onStickerClick?.invoke(sticker)
    }

    // ==================== Private Methods ====================

    /**
     * Handles successful fetch of stickers.
     */
    private fun handleFetchSuccess(sets: List<StickerSet>) {
        _stickerSets.value = sets

        if (sets.isEmpty()) {
            _uiState.value = StickerKeyboardUIState.Empty
            _currentStickers.value = emptyList()
        } else {
            _uiState.value = StickerKeyboardUIState.Content
            // Select first set by default
            _selectedSetIndex.value = 0
            _currentStickers.value = sets[0].stickers
        }
    }

    /**
     * Handles fetch error.
     */
    private fun handleFetchError(exception: CometChatException) {
        _uiState.value = StickerKeyboardUIState.Error(exception)
    }
}
