package com.databits.androidscouting.core.model

sealed interface ScanPayload {
    data class ScouterList(val scouters: List<String>) : ScanPayload

    data class GoogleConfig(
        val workbookId: String,
        val crowdRange: String,
        val pitRange: String,
        val specialtyRange: String,
    ) : ScanPayload

    data class MatchDataChunk(
        val chunkIndex: Int,
        val rows: List<List<String>>,
    ) : ScanPayload

    data class RoleProvision(
        val role: DeviceRole,
        val crowdPosition: Int,
        val scouterName: String,
        val locked: Boolean,
        val match: Int,
        val deleteData: Boolean,
        val specialEnabled: Boolean,
    ) : ScanPayload

    data class TeamData(val rawCsv: String) : ScanPayload

    data class Unknown(val raw: String) : ScanPayload
}
