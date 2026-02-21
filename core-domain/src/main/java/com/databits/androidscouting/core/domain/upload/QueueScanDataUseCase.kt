package com.databits.androidscouting.core.domain.upload

import com.databits.androidscouting.core.model.UploadItem
import com.databits.androidscouting.core.model.UploadMode

class QueueScanDataUseCase(
    private val uploadQueueGateway: UploadQueueGateway,
) {
    fun queueIfNew(rawCsv: String, uploadMode: UploadMode): Boolean {
        if (rawCsv.startsWith("role", ignoreCase = true)) {
            return false
        }

        val dataType = uploadMode.queueType
        if (uploadQueueGateway.hasSeenLine(rawCsv, dataType)) {
            return false
        }

        uploadQueueGateway.markLineSeen(rawCsv, dataType)
        uploadQueueGateway.enqueueUpload(
            UploadItem(
                uploadType = uploadMode,
                dataCsv = rawCsv,
                matchNumber = parseMatchNumber(rawCsv),
                teamNumber = parseTeamNumber(rawCsv),
            ),
        )
        return true
    }

    private fun parseMatchNumber(rawCsv: String): Int? {
        return rawCsv.split(',').firstOrNull()?.trim()?.toIntOrNull()
    }

    private fun parseTeamNumber(rawCsv: String): String? {
        val parts = rawCsv.split(',')
        if (parts.size < 2) {
            return null
        }
        val team = parts[1].trim()
        return if (team.isEmpty()) null else team
    }
}
