package com.databits.androidscouting.core.domain.provision

import com.databits.androidscouting.core.model.DeviceRole
import com.databits.androidscouting.core.model.ScanPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplyRoleProvisionUseCaseTest {

    @Test
    fun invoke_appliesRoleProvisionWithoutClearWhenDeleteDisabled() {
        val gateway = FakeGateway()
        val useCase = ApplyRoleProvisionUseCase(gateway)

        useCase(
            ScanPayload.RoleProvision(
                role = DeviceRole.CROWD,
                crowdPosition = 2,
                scouterName = "Ari",
                locked = true,
                match = 5,
                deleteData = false,
                specialEnabled = true,
            ),
        )

        assertFalse(gateway.clearCalled)
        assertEquals("crowd", gateway.role)
        assertEquals(2, gateway.position)
        assertEquals("Ari", gateway.scouter)
        assertTrue(gateway.locked)
        assertTrue(gateway.specialEnabled)
    }

    @Test
    fun invoke_clearsDataWhenDeleteEnabled() {
        val gateway = FakeGateway()
        val useCase = ApplyRoleProvisionUseCase(gateway)

        useCase(
            ScanPayload.RoleProvision(
                role = DeviceRole.MASTER,
                crowdPosition = 1,
                scouterName = "Pat",
                locked = false,
                match = 1,
                deleteData = true,
                specialEnabled = false,
            ),
        )

        assertTrue(gateway.clearCalled)
    }

    private class FakeGateway : RoleProvisionGateway {
        var clearCalled: Boolean = false
        var role: String = ""
        var position: Int = -1
        var scouter: String = ""
        var locked: Boolean = false
        var specialEnabled: Boolean = false

        override fun clearAllData() {
            clearCalled = true
        }

        override fun updateDeviceRole(role: String) {
            this.role = role
        }

        override fun updateCrowdPosition(position: Int) {
            this.position = position
        }

        override fun updateCurrentScouter(name: String) {
            this.scouter = name
        }

        override fun updateRoleLocked(locked: Boolean) {
            this.locked = locked
        }

        override fun updateSpecialSwitch(enabled: Boolean) {
            this.specialEnabled = enabled
        }
    }
}
