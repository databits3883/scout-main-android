package com.databits.androidscouting.core.domain.config;

public final class GoogleConfigPayloadCodec {
    private GoogleConfigPayloadCodec() {
    }

    public static String composePayload(
        String workbookId,
        String crowdRange,
        String pitRange,
        String specialtyRange
    ) {
        return "GoogleConfig,"
            + nullToEmpty(workbookId) + ","
            + nullToEmpty(crowdRange) + ","
            + nullToEmpty(pitRange) + ","
            + nullToEmpty(specialtyRange);
    }

    public static String composeRange(String sheetName, String lower, String upper) {
        return nullToEmpty(sheetName) + "!" + nullToEmpty(lower) + ":" + nullToEmpty(upper);
    }

    public static RangeParts parseRange(String range) {
        if (range == null || !range.contains("!") || !range.contains(":")) {
            return new RangeParts("", "", "");
        }
        String[] sheetSplit = range.split("!", 2);
        if (sheetSplit.length < 2) {
            return new RangeParts("", "", "");
        }
        String[] bounds = sheetSplit[1].split(":", 2);
        if (bounds.length < 2) {
            return new RangeParts(sheetSplit[0], "", "");
        }
        return new RangeParts(sheetSplit[0], bounds[0], bounds[1]);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public static final class RangeParts {
        public final String sheetName;
        public final String lowerBound;
        public final String upperBound;

        public RangeParts(String sheetName, String lowerBound, String upperBound) {
            this.sheetName = sheetName;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
    }
}
