package com.databits.androidscouting.core.domain.provision

import com.databits.androidscouting.core.model.ScanPayload

class ApplyRoleProvisionUseCase(
    private val gateway: RoleProvisionGateway,
) {
    operator fun invoke(payload: ScanPayload.RoleProvision) {
        if (payload.deleteData) {
            gateway.clearAllData()
        }

        gateway.updateDeviceRole(payload.role.rawValue)
        gateway.updateCrowdPosition(payload.crowdPosition)
        gateway.updateCurrentScouter(payload.scouterName)
        gateway.updateRoleLocked(payload.locked)
        gateway.updateSpecialSwitch(payload.specialEnabled)
    }
}
