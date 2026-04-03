package com.cometchat.uikit.core.domain.repository

import com.cometchat.uikit.core.domain.model.StickerSet

/**
 * Repository interface defining data operations contract for stickers.
 * Lives in domain layer - no implementation details.
 *
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data fetching strategies (remote, local, cached).
 */
interface StickerRepository {

    /**
     * Fetches stickers from the data source.
     *
     * @return Result containing list of StickerSet objects or error
     */
    suspend fun getStickers(): Result<List<StickerSet>>
}
