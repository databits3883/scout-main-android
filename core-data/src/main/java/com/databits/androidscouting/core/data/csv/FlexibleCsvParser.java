package com.databits.androidscouting.core.data.csv;

import java.util.regex.Pattern;

public final class FlexibleCsvParser {
    private FlexibleCsvParser() {
    }

    public static String[] parse(String raw, int minFields) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String separator = detectSeparator(raw);
        if (separator == null) {
            return null;
        }

        String[] fields = raw.split(Pattern.quote(separator));
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

        return fields.length < minFields ? null : fields;
    }

    private static String detectSeparator(String data) {
        char[] separators = {',', ';', '|', '\t'};
        for (char separator : separators) {
            if (data.indexOf(separator) >= 0) {
                return String.valueOf(separator);
            }
        }
        return null;
    }
}
