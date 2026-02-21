package com.databits.androidscouting.data.repository.adapter

import com.databits.androidscouting.core.domain.provision.RoleProvisionGateway
import com.databits.androidscouting.data.repository.ProvisionSettingsStore
import com.preference.PowerPreference

class StoreRoleProvisionGateway(
    private val provisionStore: ProvisionSettingsStore,
) : RoleProvisionGateway {
    override fun clearAllData() {
        PowerPreference.clearAllData()
    }

    override fun updateDeviceRole(role: String) {
        provisionStore.setDeviceRole(role)
    }

    override fun updateCrowdPosition(position: Int) {
        provisionStore.setCrowdPosition(position)
    }

    override fun updateCurrentScouter(name: String) {
        provisionStore.setCurrentScouter(name)
    }

    override fun updateRoleLocked(locked: Boolean) {
        provisionStore.setRoleLocked(locked)
    }

    override fun updateSpecialSwitch(enabled: Boolean) {
        provisionStore.setSpecialSwitch(enabled)
    }
}
