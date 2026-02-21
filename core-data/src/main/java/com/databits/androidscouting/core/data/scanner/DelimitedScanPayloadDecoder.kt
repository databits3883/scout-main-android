package com.databits.androidscouting.core.data.scanner

import com.databits.androidscouting.core.domain.scanner.ScanPayloadDecoder
import com.databits.androidscouting.core.model.DeviceRole
import com.databits.androidscouting.core.model.ScanPayload

class DelimitedScanPayloadDecoder : ScanPayloadDecoder {

    override fun decode(raw: String): ScanPayload {
        return when {
            raw.startsWith("ScoutData") -> {
                val parts = raw.split(',')
                ScanPayload.ScouterList(parts.drop(1).filter { it.isNotBlank() })
            }

            raw.startsWith("GoogleConfig") -> {
                val parts = raw.split(',')
                if (parts.size >= 5) {
                    ScanPayload.GoogleConfig(
                        workbookId = parts[1].trim(),
                        crowdRange = parts[2].trim(),
                        pitRange = parts[3].trim(),
                        specialtyRange = parts[4].trim(),
                    )
                } else {
                    ScanPayload.Unknown(raw)
                }
            }

            raw.startsWith("MatchData") -> {
                val parts = raw.split(',', limit = 3)
                if (parts.size < 3) {
                    return ScanPayload.Unknown(raw)
                }

                val chunkIndex = parts[1].toIntOrNull() ?: return ScanPayload.Unknown(raw)
                val rows = parts[2]
                    .split(Regex("(?<=])(?=\\[)"))
                    .map { it.replace("[", "").replace("]", "") }
                    .filter { it.isNotBlank() }
                    .map { row -> row.split(',').map { it.trim() } }

                ScanPayload.MatchDataChunk(chunkIndex = chunkIndex, rows = rows)
            }

            raw.startsWith("role") -> {
                val parts = raw.split(',')
                if (parts.size >= 14) {
                    ScanPayload.RoleProvision(
                        role = DeviceRole.fromRaw(parts[1]),
                        crowdPosition = parts[3].toIntOrNull() ?: 0,
                        scouterName = parts[5],
                        locked = parts[7].toBooleanStrictOrNull() ?: false,
                        match = parts[9].toIntOrNull() ?: 1,
                        deleteData = parts[11].toBooleanStrictOrNull() ?: false,
                        specialEnabled = parts[13].toBooleanStrictOrNull() ?: false,
                    )
                } else {
                    ScanPayload.Unknown(raw)
                }
            }

            else -> ScanPayload.TeamData(raw)
        }
    }
}
