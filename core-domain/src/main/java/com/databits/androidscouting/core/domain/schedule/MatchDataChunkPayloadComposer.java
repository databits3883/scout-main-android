package com.databits.androidscouting.core.domain.schedule;

import java.util.Arrays;

public final class MatchDataChunkPayloadComposer {
    private MatchDataChunkPayloadComposer() {
    }

    public static String compose(int chunkIndex, String[][] rows, int startInclusive, int endExclusive) {
        StringBuilder chunkData = new StringBuilder();
        for (int i = startInclusive; i < endExclusive; i++) {
            chunkData.append(Arrays.toString(rows[i]));
        }
        return "MatchData," + chunkIndex + "," + chunkData;
    }
}
