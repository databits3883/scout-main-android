package com.databits.androidscouting.core.data.scanner

import com.databits.androidscouting.core.model.ScanPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DelimitedScanPayloadDecoderTest {

    private val decoder = DelimitedScanPayloadDecoder()

    @Test
    fun decode_scouterList_dropsPrefixToken() {
        val payload = decoder.decode("ScoutData,Avery,Jordan")

        assertTrue(payload is ScanPayload.ScouterList)
        val scouters = (payload as ScanPayload.ScouterList).scouters
        assertEquals(listOf("Avery", "Jordan"), scouters)
    }

    @Test
    fun decode_googleConfig_trimsValues() {
        val payload = decoder.decode("GoogleConfig, book123 , Crowd!A1 , Pit!A1 , Specialty!A1 ")

        assertTrue(payload is ScanPayload.GoogleConfig)
        payload as ScanPayload.GoogleConfig
        assertEquals("book123", payload.workbookId)
        assertEquals("Crowd!A1", payload.crowdRange)
        assertEquals("Pit!A1", payload.pitRange)
        assertEquals("Specialty!A1", payload.specialtyRange)
    }

    @Test
    fun decode_scouterList_ignoresEmptyEntries() {
        val payload = decoder.decode("ScoutData,Avery,, ,Jordan,")

        assertTrue(payload is ScanPayload.ScouterList)
        payload as ScanPayload.ScouterList
        assertEquals(listOf("Avery", "Jordan"), payload.scouters)
    }

    @Test
    fun decode_matchData_acceptsMalformedRowLengthsWithoutCrash() {
        val payload = decoder.decode("MatchData,4,[1,2][3,4,5,6][7]")

        assertTrue(payload is ScanPayload.MatchDataChunk)
        payload as ScanPayload.MatchDataChunk
        assertEquals(4, payload.chunkIndex)
        assertEquals(3, payload.rows.size)
        assertEquals(2, payload.rows[0].size)
        assertEquals(4, payload.rows[1].size)
        assertEquals(1, payload.rows[2].size)
    }

    @Test
    fun decode_matchData_invalidChunkIndex_returnsUnknown() {
        val payload = decoder.decode("MatchData,abc,[1,2,3]")

        assertTrue(payload is ScanPayload.Unknown)
    }
}
