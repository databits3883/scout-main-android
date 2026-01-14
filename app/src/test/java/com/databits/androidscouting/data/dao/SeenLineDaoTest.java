package com.databits.androidscouting.data.dao;

import com.databits.androidscouting.data.entity.SeenLine;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class SeenLineDaoTest extends DaoTestBase {

    @Test
    public void testSeenLine() {
        SeenLineDao dao = db.seenLineDao();
        SeenLine line = new SeenLine();
        line.lineHash = "hash123";
        line.dataType = "CROWD";
        
        dao.insert(line);

        assertTrue(dao.hasSeenLine("hash123", "CROWD"));
        assertFalse(dao.hasSeenLine("hash123", "PIT"));
        assertFalse(dao.hasSeenLine("other", "CROWD"));
    }

    @Test
    public void testClearType() {
        SeenLineDao dao = db.seenLineDao();
        SeenLine l1 = new SeenLine(); l1.lineHash = "1"; l1.dataType = "CROWD";
        SeenLine l2 = new SeenLine(); l2.lineHash = "2"; l2.dataType = "PIT";
        
        dao.insert(l1);
        dao.insert(l2);

        dao.clearType("CROWD");
        assertFalse(dao.hasSeenLine("1", "CROWD"));
        assertTrue(dao.hasSeenLine("2", "PIT"));
    }
}
