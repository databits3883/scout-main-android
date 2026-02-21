package com.databits.androidscouting.data.repository.adapter

import com.databits.androidscouting.core.domain.provision.RoleProvisionGateway
import com.databits.androidscouting.data.repository.PreferenceRepository
import com.preference.PowerPreference

class PreferenceRoleProvisionGateway(
    private val repository: PreferenceRepository,
) : RoleProvisionGateway {
    override fun clearAllData() {
        PowerPreference.clearAllData()
    }

    override fun updateDeviceRole(role: String) {
        repository.setDeviceRole(role)
    }

    override fun updateCrowdPosition(position: Int) {
        repository.setCrowdPosition(position)
    }

    override fun updateCurrentScouter(name: String) {
        repository.setCurrentScouter(name)
    }

    override fun updateRoleLocked(locked: Boolean) {
        repository.setRoleLocked(locked)
    }

    override fun updateSpecialSwitch(enabled: Boolean) {
        repository.setSpecialSwitch(enabled)
    }
}
