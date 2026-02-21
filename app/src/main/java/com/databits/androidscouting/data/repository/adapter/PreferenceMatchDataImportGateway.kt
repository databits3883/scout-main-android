package com.databits.androidscouting.data.repository.adapter

import com.databits.androidscouting.core.domain.schedule.MatchDataImportGateway
import com.databits.androidscouting.data.repository.PreferenceRepository

class PreferenceMatchDataImportGateway(
    private val repository: PreferenceRepository,
) : MatchDataImportGateway {
    override fun hasProcessedChunk(chunkId: Int): Boolean {
        return repository.hasProcessedChunk(chunkId)
    }

    override fun markChunkProcessed(chunkId: Int) {
        repository.markChunkProcessed(chunkId)
    }

    override fun importTeamSchedule(rows: List<List<String>>) {
        val csvData = Array(rows.size) { index -> rows[index].toTypedArray() }
        repository.importTeamSchedule(csvData)
    }
}
