package com.databits.androidscouting.core.domain.provision;

import java.util.List;

public final class ScouterListPayloadComposer {
    private ScouterListPayloadComposer() {
    }

    public static String compose(List<String> scouters) {
        if (scouters == null || scouters.isEmpty()) {
            return "ScoutData";
        }
        return "ScoutData," + String.join(",", scouters);
    }
}
