package com.databits.androidscouting.core.domain.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FindMatchedTeamSlotUseCaseTest {

    private val useCase = FindMatchedTeamSlotUseCase()

    @Test
    fun execute_returnsNull_whenNoTeamsConfigured() {
        val result = useCase.execute("1234,data", emptyList())

        assertNull(result)
    }

    @Test
    fun execute_returnsFirstMatchingSlot() {
        val result = useCase.execute(
            scannedValue = "12,1678,meta",
            teamNumbers = listOf("111", "1678", "2056"),
        )

        assertEquals(2, result)
    }

    @Test
    fun execute_ignoresBlankTeamValues() {
        val result = useCase.execute(
            scannedValue = "5,2056,meta",
            teamNumbers = listOf(null, "", "2056"),
        )

        assertEquals(3, result)
    }

    @Test
    fun execute_returnsNull_whenNoMatchFound() {
        val result = useCase.execute(
            scannedValue = "5,9999,meta",
            teamNumbers = listOf("111", "2056", "1678"),
        )

        assertNull(result)
    }
}
