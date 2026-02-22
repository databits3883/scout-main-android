package com.databits.androidscouting.core.domain.schedule;

import org.junit.Assert;
import org.junit.Test;

public class MatchDataChunkPayloadComposerTest {

    @Test
    public void compose_serializesChunkRows() {
        String[][] rows = new String[][]{
            {"1", "111", "112", "113", "114", "115", "116"},
            {"2", "211", "212", "213", "214", "215", "216"}
        };

        String payload = MatchDataChunkPayloadComposer.compose(0, rows, 0, 2);

        Assert.assertEquals(
            "MatchData,0,[1, 111, 112, 113, 114, 115, 116][2, 211, 212, 213, 214, 215, 216]",
            payload
        );
    }
}
