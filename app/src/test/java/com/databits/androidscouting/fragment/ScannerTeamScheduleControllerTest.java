package com.databits.androidscouting.fragment;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ScannerTeamScheduleControllerTest {

    @Test
    public void getScoutingTeamSlotsReturnsSlotsOneThroughSix() {
        ScannerTeamScheduleController controller = new ScannerTeamScheduleController();
        String[] cached = new String[] {"unused", "101", "102", "103", "201", "202", "203"};

        List<String> slots = controller.getScoutingTeamSlots(cached);

        Assert.assertEquals(6, slots.size());
        Assert.assertEquals("101", slots.get(0));
        Assert.assertEquals("203", slots.get(5));
    }
}
