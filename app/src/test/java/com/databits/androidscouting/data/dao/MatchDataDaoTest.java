package com.databits.androidscouting.data.dao;

import com.databits.androidscouting.data.entity.MatchData;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class MatchDataDaoTest extends DaoTestBase {

    @Test
    public void testInsertAndRetrieveMatchData() {
        MatchDataDao dao = db.matchDataDao();
        MatchData matchData = new MatchData();
        matchData.matchNumber = 1;
        matchData.crowdDataCsv = "match1,crowd";
        matchData.pitDataCsv = "match1,pit";

        dao.insert(matchData);

        MatchData retrieved = dao.getMatchData(1);
        assertNotNull(retrieved);
        assertEquals("match1,crowd", retrieved.crowdDataCsv);
        assertEquals("match1,pit", retrieved.pitDataCsv);
    }

    @Test
    public void testUpdateMatchData() {
        MatchDataDao dao = db.matchDataDao();
        MatchData matchData = new MatchData();
        matchData.matchNumber = 1;
        matchData.crowdDataCsv = "initial";
        dao.insert(matchData);

        matchData.crowdDataCsv = "updated";
        dao.update(matchData);

        MatchData retrieved = dao.getMatchData(1);
        assertEquals("updated", retrieved.crowdDataCsv);
    }
}
