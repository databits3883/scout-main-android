package com.databits.androidscouting.data.repository.adapter

import com.databits.androidscouting.core.domain.schedule.MatchDataImportGateway
import com.databits.androidscouting.data.repository.ScheduleStore

class StoreMatchDataImportGateway(
    private val scheduleStore: ScheduleStore,
) : MatchDataImportGateway {
    override fun hasProcessedChunk(chunkId: Int): Boolean {
        return scheduleStore.hasProcessedChunk(chunkId)
    }

    override fun markChunkProcessed(chunkId: Int) {
        scheduleStore.markChunkProcessed(chunkId)
    }

    override fun importTeamSchedule(rows: List<List<String>>) {
        val csvData = Array(rows.size) { index -> rows[index].toTypedArray() }
        scheduleStore.importTeamSchedule(csvData)
    }
}
