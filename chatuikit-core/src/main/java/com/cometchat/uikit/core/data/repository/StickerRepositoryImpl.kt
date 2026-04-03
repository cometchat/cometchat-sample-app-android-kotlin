package com.cometchat.uikit.core.data.repository

import com.cometchat.uikit.core.data.datasource.StickerDataSource
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.core.domain.repository.StickerRepository

/**
 * Implementation of StickerRepository that delegates to a data source.
 * This is the default implementation used for data operations.
 *
 * @param dataSource The data source to use for fetching stickers
 */
class StickerRepositoryImpl(
    private val dataSource: StickerDataSource
) : StickerRepository {

    /**
     * Fetches stickers from the data source.
     *
     * @return Result containing list of StickerSet objects or error
     */
    override suspend fun getStickers(): Result<List<StickerSet>> {
        return dataSource.fetchStickers()
    }
}
