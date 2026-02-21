package com.databits.androidscouting.feature.scanner

import com.databits.androidscouting.core.data.scanner.DelimitedScanPayloadDecoder
import com.databits.androidscouting.core.domain.scanner.ProcessScanPayloadUseCase

object ScannerFeatureBootstrap {
    @JvmStatic
    fun provideProcessScanPayloadUseCase(): ProcessScanPayloadUseCase {
        return ProcessScanPayloadUseCase(DelimitedScanPayloadDecoder())
    }
}
