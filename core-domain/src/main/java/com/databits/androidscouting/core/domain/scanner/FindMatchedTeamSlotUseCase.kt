package com.databits.androidscouting.core.domain.scanner

class FindMatchedTeamSlotUseCase {
    fun execute(scannedValue: String, teamNumbers: List<String?>): Int? {
        if (teamNumbers.isEmpty()) {
            return null
        }

        for ((index, teamNumber) in teamNumbers.withIndex()) {
            if (!teamNumber.isNullOrBlank() && scannedValue.contains(teamNumber)) {
                return index + 1
            }
        }

        return null
    }
}
