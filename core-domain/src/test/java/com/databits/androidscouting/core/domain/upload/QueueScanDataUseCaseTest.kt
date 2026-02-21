package com.databits.androidscouting.core.domain.upload

import com.databits.androidscouting.core.model.UploadItem
import com.databits.androidscouting.core.model.UploadMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueScanDataUseCaseTest {

    @Test
    fun queueIfNew_ignoresRolePayload() {
        val gateway = FakeGateway()
        val useCase = QueueScanDataUseCase(gateway)

        val queued = useCase.queueIfNew("role,master,crowd,1,name,Test", UploadMode.CROWD)

        assertFalse(queued)
        assertEquals(0, gateway.enqueuedItems.size)
    }

    @Test
    fun queueIfNew_skipsDuplicateLine() {
        val gateway = FakeGateway(hasSeen = true)
        val useCase = QueueScanDataUseCase(gateway)

        val queued = useCase.queueIfNew("1,1678,data", UploadMode.PIT)

        assertFalse(queued)
        assertEquals(0, gateway.enqueuedItems.size)
        assertEquals(0, gateway.markedLines.size)
    }

    @Test
    fun queueIfNew_enqueuesParsedItemForNewLine() {
        val gateway = FakeGateway()
        val useCase = QueueScanDataUseCase(gateway)

        val queued = useCase.queueIfNew("12,254,data", UploadMode.SPECIALTY)

        assertTrue(queued)
        assertEquals(1, gateway.markedLines.size)
        assertEquals(1, gateway.enqueuedItems.size)

        val item = gateway.enqueuedItems.first()
        assertEquals(UploadMode.SPECIALTY, item.uploadType)
        assertEquals("12,254,data", item.dataCsv)
        assertEquals(12, item.matchNumber)
        assertEquals("254", item.teamNumber)
    }

    @Test
    fun queueIfNew_handlesMissingTeamAndMatchGracefully() {
        val gateway = FakeGateway()
        val useCase = QueueScanDataUseCase(gateway)

        val queued = useCase.queueIfNew("abc", UploadMode.CROWD)

        assertTrue(queued)
        val item = gateway.enqueuedItems.first()
        assertNull(item.matchNumber)
        assertNull(item.teamNumber)
    }

    private class FakeGateway(
        private val hasSeen: Boolean = false,
    ) : UploadQueueGateway {
        val markedLines = mutableListOf<Pair<String, String>>()
        val enqueuedItems = mutableListOf<UploadItem>()

        override fun hasSeenLine(lineHash: String, dataType: String): Boolean = hasSeen

        override fun markLineSeen(lineHash: String, dataType: String) {
            markedLines.add(lineHash to dataType)
        }

        override fun enqueueUpload(item: UploadItem) {
            enqueuedItems.add(item)
        }
    }
}
