package com.databits.androidscouting.core.domain.config;

import org.junit.Assert;
import org.junit.Test;

public class GoogleConfigPayloadCodecTest {

    @Test
    public void composePayload_includesAllFields() {
        String payload = GoogleConfigPayloadCodec.composePayload(
            "book123",
            "Crowd!A2:Z700",
            "Pit!A2:Z700",
            "Special!A2:Z700"
        );

        Assert.assertEquals(
            "GoogleConfig,book123,Crowd!A2:Z700,Pit!A2:Z700,Special!A2:Z700",
            payload
        );
    }

    @Test
    public void parseRange_parsesValidRange() {
        GoogleConfigPayloadCodec.RangeParts parts = GoogleConfigPayloadCodec.parseRange("CrowdRaw!A2:Z700");
        Assert.assertEquals("CrowdRaw", parts.sheetName);
        Assert.assertEquals("A2", parts.lowerBound);
        Assert.assertEquals("Z700", parts.upperBound);
    }
}
