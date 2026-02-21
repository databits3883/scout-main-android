package com.databits.androidscouting.core.domain.upload

import com.databits.androidscouting.core.model.UploadItem

interface UploadQueueGateway {
    fun hasSeenLine(lineHash: String, dataType: String): Boolean
    fun markLineSeen(lineHash: String, dataType: String)
    fun enqueueUpload(item: UploadItem)
}
