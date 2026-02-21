package com.databits.androidscouting.core.domain.provision

interface RoleProvisionGateway {
    fun clearAllData()
    fun updateDeviceRole(role: String)
    fun updateCrowdPosition(position: Int)
    fun updateCurrentScouter(name: String)
    fun updateRoleLocked(locked: Boolean)
    fun updateSpecialSwitch(enabled: Boolean)
}
