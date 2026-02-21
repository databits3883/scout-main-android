package com.databits.androidscouting.core.domain.schedule

interface MatchDataImportGateway {
    fun hasProcessedChunk(chunkId: Int): Boolean
    fun markChunkProcessed(chunkId: Int)
    fun importTeamSchedule(rows: List<List<String>>)
}
