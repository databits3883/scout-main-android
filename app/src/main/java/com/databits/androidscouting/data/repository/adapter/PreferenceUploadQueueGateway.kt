package com.databits.androidscouting.data.repository.adapter

import com.databits.androidscouting.core.domain.upload.UploadQueueGateway
import com.databits.androidscouting.core.model.UploadItem
import com.databits.androidscouting.data.entity.UploadQueueItem
import com.databits.androidscouting.data.repository.PreferenceRepository

class PreferenceUploadQueueGateway(
    private val repository: PreferenceRepository,
) : UploadQueueGateway {
    override fun hasSeenLine(lineHash: String, dataType: String): Boolean {
        return repository.hasSeenLine(lineHash, dataType)
    }

    override fun markLineSeen(lineHash: String, dataType: String) {
        repository.markLineSeen(lineHash, dataType)
    }

    override fun enqueueUpload(item: UploadItem) {
        val queueItem = UploadQueueItem().apply {
            uploadType = item.uploadType.queueType
            dataCsv = item.dataCsv
            matchNumber = item.matchNumber
            teamNumber = item.teamNumber
        }
        repository.addUploadItem(queueItem)
    }
}
