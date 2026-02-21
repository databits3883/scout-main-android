package com.databits.androidscouting.layout;

import com.databits.androidscouting.model.Cell;
import com.databits.androidscouting.model.CellType;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class LayoutParserTest {

    private LayoutParser parser;

    @Before
    public void setup() {
        parser = new LayoutParser();
    }

    @Test
    public void testParseEmptyString() {
        ParseResult result = parser.parse("");
        assertFalse(result.isSuccess());
        assertEquals("Layout JSON is empty", result.getErrorMessage());
    }

    @Test
    public void testParseNullString() {
        ParseResult result = parser.parse(null);
        assertFalse(result.isSuccess());
        assertEquals("Layout JSON is empty", result.getErrorMessage());
    }

    @Test
    public void testParseValidJson() {
        String json = "{\"cells\": [{\"id\": 1, \"title\": \"Test Title\", \"typeString\": \"Title\", \"config\": {}}]}";
        ParseResult result = parser.parse(json);
        
        assertTrue(result.isSuccess());
        List<Cell> cells = result.getCells();
        assertEquals(1, cells.size());
        assertEquals(CellType.TITLE, cells.get(0).getType());
        assertEquals("Test Title", cells.get(0).getTitle());
    }

    @Test
    public void testParseJsonWithLegacyDelimiter() {
        String json = "{\"cells\": [{\"id\": 1, \"title\": \"Legacy\", \"typeString\": \"Title\", \"config\": {}}]}^SomeLegacySuffix";
        ParseResult result = parser.parse(json);
        
        assertTrue(result.isSuccess());
        assertEquals("Legacy", result.getCells().get(0).getTitle());
    }

    @Test
    public void testParseInvalidJson() {
        String json = "{invalid_json}";
        ParseResult result = parser.parse(json);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Failed to parse layout"));
    }
}
