package com.databits.androidscouting.core.domain.scanner

import com.databits.androidscouting.core.model.ScanPayload

interface ScanPayloadDecoder {
    fun decode(raw: String): ScanPayload
}
