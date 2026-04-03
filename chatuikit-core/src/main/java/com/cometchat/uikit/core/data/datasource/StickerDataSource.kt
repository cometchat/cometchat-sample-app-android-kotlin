package com.cometchat.uikit.core.data.datasource

import com.cometchat.uikit.core.domain.model.StickerSet

/**
 * Interface defining data source operations for stickers.
 * Lives in data layer - defines contract for data fetching.
 * Allows for different implementations (remote, local, mock).
 */
interface StickerDataSource {

    /**
     * Fetches stickers from the data source.
     *
     * @return Result containing list of StickerSet objects or error
     */
    suspend fun fetchStickers(): Result<List<StickerSet>>
}
