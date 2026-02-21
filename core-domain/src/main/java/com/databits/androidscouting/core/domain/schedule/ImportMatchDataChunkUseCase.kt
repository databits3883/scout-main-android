package com.databits.androidscouting.core.domain.schedule

import com.databits.androidscouting.core.model.ScanPayload

class ImportMatchDataChunkUseCase(
    private val gateway: MatchDataImportGateway,
) {
    fun execute(payload: ScanPayload.MatchDataChunk): ImportResult {
        if (gateway.hasProcessedChunk(payload.chunkIndex)) {
            return ImportResult.Duplicate
        }

        if (payload.rows.isEmpty()) {
            return ImportResult.Empty
        }

        gateway.markChunkProcessed(payload.chunkIndex)
        gateway.importTeamSchedule(payload.rows)
        return ImportResult.Imported(payload.rows.size)
    }
}

sealed interface ImportResult {
    data object Duplicate : ImportResult
    data object Empty : ImportResult
    data class Imported(val rowCount: Int) : ImportResult
}
