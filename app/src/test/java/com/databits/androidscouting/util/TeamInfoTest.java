package com.databits.androidscouting.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.databits.androidscouting.data.repository.ProvisionSettingsStore;
import com.databits.androidscouting.data.repository.ScheduleStore;
import org.junit.Before;
import org.junit.Test;

public class TeamInfoTest {

    private ProvisionSettingsStore provisionStore;
    private ScheduleStore scheduleStore;
    private TeamInfo teamInfo;

    @Before
    public void setUp() {
        provisionStore = mock(ProvisionSettingsStore.class);
        scheduleStore = mock(ScheduleStore.class);
        Context context = mock(Context.class);
        teamInfo = new TeamInfo(context, provisionStore, scheduleStore);
    }

    @Test
    public void getTeam_usesManualOverrideWhenEnabled() {
        when(provisionStore.isManualTeamOverrideEnabled()).thenReturn(true);
        when(provisionStore.getManualTeamOverrideValue()).thenReturn(4321);

        assertEquals(4321, teamInfo.getTeam(5));
    }

    @Test
    public void getTeam_returnsZeroForMalformedTeamNumber() {
        when(provisionStore.isManualTeamOverrideEnabled()).thenReturn(false);
        when(provisionStore.getCrowdPosition()).thenReturn(1);
        when(scheduleStore.getTeamMatchListSize()).thenReturn(10);
        when(scheduleStore.getTeamNumber(3, 1)).thenReturn("not-a-number");

        assertEquals(0, teamInfo.getTeam(3));
    }

    @Test
    public void getScouterName_returnsEmptyWhenMissing() {
        when(provisionStore.getCurrentScouter()).thenReturn(null);

        assertEquals("", teamInfo.getScouterName());
    }
}
