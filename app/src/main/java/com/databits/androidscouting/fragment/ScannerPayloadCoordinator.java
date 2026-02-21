package com.databits.androidscouting.fragment;

import com.databits.androidscouting.core.model.ScanPayload;
import java.util.List;

final class ScannerPayloadCoordinator {

    interface Actions {
        void onScouterList(List<String> scouters);
        void onGoogleConfig(ScanPayload.GoogleConfig config);
        void onMatchDataChunk(ScanPayload.MatchDataChunk payload);
        void onRoleProvision(ScanPayload.RoleProvision payload);
        void onTeamData(String rawCsv);
    }

    private final Actions actions;

    ScannerPayloadCoordinator(Actions actions) {
        this.actions = actions;
    }

    void route(ScanPayload payload, String rawValue) {
        if (payload instanceof ScanPayload.ScouterList) {
            actions.onScouterList(((ScanPayload.ScouterList) payload).getScouters());
            return;
        }

        if (payload instanceof ScanPayload.GoogleConfig) {
            actions.onGoogleConfig((ScanPayload.GoogleConfig) payload);
            return;
        }

        if (payload instanceof ScanPayload.MatchDataChunk) {
            actions.onMatchDataChunk((ScanPayload.MatchDataChunk) payload);
            return;
        }

        if (payload instanceof ScanPayload.RoleProvision) {
            actions.onRoleProvision((ScanPayload.RoleProvision) payload);
            return;
        }

        if (payload instanceof ScanPayload.TeamData) {
            actions.onTeamData(((ScanPayload.TeamData) payload).getRawCsv());
            return;
        }

        actions.onTeamData(rawValue);
    }
}
