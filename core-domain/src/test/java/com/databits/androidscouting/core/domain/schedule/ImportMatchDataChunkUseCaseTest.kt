package com.databits.androidscouting.core.domain.schedule

import com.databits.androidscouting.core.model.ScanPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportMatchDataChunkUseCaseTest {

    @Test
    fun execute_returnsDuplicate_whenChunkAlreadyProcessed() {
        val gateway = FakeGateway(hasProcessed = true)
        val useCase = ImportMatchDataChunkUseCase(gateway)

        val result = useCase.execute(
            ScanPayload.MatchDataChunk(chunkIndex = 2, rows = listOf(listOf("1", "2"))),
        )

        assertTrue(result is ImportResult.Duplicate)
        assertEquals(0, gateway.markedChunks.size)
        assertEquals(0, gateway.importedRows.size)
    }

    @Test
    fun execute_returnsEmpty_whenRowsMissing() {
        val gateway = FakeGateway()
        val useCase = ImportMatchDataChunkUseCase(gateway)

        val result = useCase.execute(ScanPayload.MatchDataChunk(chunkIndex = 4, rows = emptyList()))

        assertTrue(result is ImportResult.Empty)
        assertEquals(0, gateway.markedChunks.size)
    }

    @Test
    fun execute_marksAndImports_whenNewChunkContainsRows() {
        val gateway = FakeGateway()
        val useCase = ImportMatchDataChunkUseCase(gateway)

        val rows = listOf(
            listOf("1", "111", "222"),
            listOf("2", "333", "444"),
        )
        val result = useCase.execute(ScanPayload.MatchDataChunk(chunkIndex = 6, rows = rows))

        assertTrue(result is ImportResult.Imported)
        assertEquals(listOf(6), gateway.markedChunks)
        assertEquals(1, gateway.importedRows.size)
        assertEquals(rows, gateway.importedRows.first())
    }

    private class FakeGateway(
        private val hasProcessed: Boolean = false,
    ) : MatchDataImportGateway {
        val markedChunks = mutableListOf<Int>()
        val importedRows = mutableListOf<List<List<String>>>()

        override fun hasProcessedChunk(chunkId: Int): Boolean = hasProcessed

        override fun markChunkProcessed(chunkId: Int) {
            markedChunks.add(chunkId)
        }

        override fun importTeamSchedule(rows: List<List<String>>) {
            importedRows.add(rows)
        }
    }
}
