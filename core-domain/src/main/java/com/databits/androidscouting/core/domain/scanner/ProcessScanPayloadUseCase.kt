package com.databits.androidscouting.core.domain.scanner

import com.databits.androidscouting.core.model.ScanPayload

class ProcessScanPayloadUseCase(
    private val decoder: ScanPayloadDecoder,
) {
    operator fun invoke(raw: String): ScanPayload = decoder.decode(raw)
}
