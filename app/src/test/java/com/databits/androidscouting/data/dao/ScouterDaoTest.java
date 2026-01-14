package com.databits.androidscouting.data.dao;

import com.databits.androidscouting.data.entity.Scouter;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class ScouterDaoTest extends DaoTestBase {

    @Test
    public void testInsertAndGetNames() {
        ScouterDao dao = db.scouterDao();
        Scouter s1 = new Scouter(); s1.name = "Alice";
        Scouter s2 = new Scouter(); s2.name = "Bob";
        
        dao.insert(s1);
        dao.insert(s2);

        List<String> names = dao.getScouterNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("Alice"));
        assertTrue(names.contains("Bob"));
    }
}
