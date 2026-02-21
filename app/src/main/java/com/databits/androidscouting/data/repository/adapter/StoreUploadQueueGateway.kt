package com.databits.androidscouting.data.repository.adapter

import com.databits.androidscouting.core.domain.upload.UploadQueueGateway
import com.databits.androidscouting.core.model.UploadItem
import com.databits.androidscouting.data.entity.UploadQueueItem
import com.databits.androidscouting.data.repository.ScheduleStore

class StoreUploadQueueGateway(
    private val scheduleStore: ScheduleStore,
) : UploadQueueGateway {
    override fun hasSeenLine(lineHash: String, dataType: String): Boolean {
        return scheduleStore.hasSeenLine(lineHash, dataType)
    }

    override fun markLineSeen(lineHash: String, dataType: String) {
        scheduleStore.markLineSeen(lineHash, dataType)
    }

    override fun enqueueUpload(item: UploadItem) {
        val queueItem = UploadQueueItem().apply {
            uploadType = item.uploadType.queueType
            dataCsv = item.dataCsv
            matchNumber = item.matchNumber
            teamNumber = item.teamNumber
        }
        scheduleStore.addUploadItem(queueItem)
    }
}
