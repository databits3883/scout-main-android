package com.databits.androidscouting.core.model

data class UploadItem(
    val uploadType: UploadMode,
    val dataCsv: String,
    val matchNumber: Int?,
    val teamNumber: String?,
)
