package com.databits.androidscouting.core.data.csv;

import org.junit.Assert;
import org.junit.Test;

public class FlexibleCsvParserTest {

    @Test
    public void parse_handlesCommaSeparatedValues() {
        String[] values = FlexibleCsvParser.parse("1, 254, data", 2);
        Assert.assertArrayEquals(new String[]{"1", "254", "data"}, values);
    }

    @Test
    public void parse_handlesPipeSeparatedValues() {
        String[] values = FlexibleCsvParser.parse("1|254|data", 2);
        Assert.assertArrayEquals(new String[]{"1", "254", "data"}, values);
    }

    @Test
    public void parse_returnsNullWhenInsufficientFields() {
        Assert.assertNull(FlexibleCsvParser.parse("1", 2));
    }
}
