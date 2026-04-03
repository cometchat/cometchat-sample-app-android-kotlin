package com.cometchat.uikit.core.domain.usecase

import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.repository.StickerRepository

/**
 * Use case for fetching stickers from the server.
 * Contains business logic for sticker retrieval.
 *
 * This use case is used by the CometChatStickerKeyboardViewModel to fetch
 * available sticker sets from the CometChat Extensions API.
 *
 * @param repository The repository to fetch stickers from
 */
open class GetStickersUseCase(
    private val repository: StickerRepository
) {
    /**
     * Fetches all available sticker sets.
     *
     * @return Result containing list of StickerSet objects or error
     */
    open suspend operator fun invoke(): Result<List<StickerSet>> {
        return repository.getStickers()
    }
}
