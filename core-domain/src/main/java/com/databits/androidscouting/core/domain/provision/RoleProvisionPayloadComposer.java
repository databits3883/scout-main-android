package com.databits.androidscouting.core.domain.provision;

public final class RoleProvisionPayloadComposer {
    private RoleProvisionPayloadComposer() {
    }

    public static String compose(
        String role,
        String crowdPosition,
        String scouterName,
        String lockStatus,
        int match,
        String deleteData,
        String specialEnabled
    ) {
        String safeRole = role == null ? "master" : role;
        String safePosition = crowdPosition == null ? "1" : crowdPosition;
        String safeScouter = scouterName == null ? "" : scouterName;
        String safeLock = lockStatus == null ? "true" : lockStatus;
        String safeDelete = deleteData == null ? "false" : deleteData;
        String safeSpecial = specialEnabled == null ? "false" : specialEnabled;
        int safeMatch = Math.max(1, match);

        return String.format(
            "role,%s,crowd_position,%s,name,%s,lock,%s,match,%s,format,%s,special,%s",
            safeRole,
            safePosition,
            safeScouter,
            safeLock,
            safeMatch,
            safeDelete,
            safeSpecial
        );
    }
}
