package com.databits.androidscouting.fragment;

import com.databits.androidscouting.core.model.DeviceRole;
import com.databits.androidscouting.core.model.ScanPayload;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class ScannerPayloadCoordinatorTest {

    @Test
    public void routeDispatchesScouterList() {
        FakeActions actions = new FakeActions();
        ScannerPayloadCoordinator coordinator = new ScannerPayloadCoordinator(actions);

        coordinator.route(new ScanPayload.ScouterList(Arrays.asList("Alex", "Sam")), "raw");

        Assert.assertEquals(Arrays.asList("Alex", "Sam"), actions.scouters);
    }

    @Test
    public void routeDispatchesGoogleConfig() {
        FakeActions actions = new FakeActions();
        ScannerPayloadCoordinator coordinator = new ScannerPayloadCoordinator(actions);

        ScanPayload.GoogleConfig config = new ScanPayload.GoogleConfig("book", "c", "p", "s");
        coordinator.route(config, "raw");

        Assert.assertEquals("book", actions.googleConfig.getWorkbookId());
    }

    @Test
    public void routeDispatchesRoleProvision() {
        FakeActions actions = new FakeActions();
        ScannerPayloadCoordinator coordinator = new ScannerPayloadCoordinator(actions);

        ScanPayload.RoleProvision provision = new ScanPayload.RoleProvision(
                DeviceRole.CROWD,
                2,
                "Ari",
                true,
                4,
                false,
                true
        );
        coordinator.route(provision, "raw");

        Assert.assertEquals("Ari", actions.roleProvision.getScouterName());
    }

    @Test
    public void routeFallsBackToRawTeamDataForUnknown() {
        FakeActions actions = new FakeActions();
        ScannerPayloadCoordinator coordinator = new ScannerPayloadCoordinator(actions);

        coordinator.route(new ScanPayload.Unknown("mystery"), "fallback-raw");

        Assert.assertEquals("fallback-raw", actions.teamDataRaw);
    }

    private static final class FakeActions implements ScannerPayloadCoordinator.Actions {
        java.util.List<String> scouters = Collections.emptyList();
        ScanPayload.GoogleConfig googleConfig;
        ScanPayload.MatchDataChunk matchDataChunk;
        ScanPayload.RoleProvision roleProvision;
        String teamDataRaw;

        @Override
        public void onScouterList(java.util.List<String> scouters) {
            this.scouters = scouters;
        }

        @Override
        public void onGoogleConfig(ScanPayload.GoogleConfig config) {
            this.googleConfig = config;
        }

        @Override
        public void onMatchDataChunk(ScanPayload.MatchDataChunk payload) {
            this.matchDataChunk = payload;
        }

        @Override
        public void onRoleProvision(ScanPayload.RoleProvision payload) {
            this.roleProvision = payload;
        }

        @Override
        public void onTeamData(String rawCsv) {
            this.teamDataRaw = rawCsv;
        }
    }
}
