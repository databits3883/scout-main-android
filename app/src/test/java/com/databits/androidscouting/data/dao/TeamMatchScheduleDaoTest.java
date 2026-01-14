package com.databits.androidscouting.data.dao;

import com.databits.androidscouting.data.entity.TeamMatchSchedule;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class TeamMatchScheduleDaoTest extends DaoTestBase {

    @Test
    public void testGetTeamNumber() {
        TeamMatchScheduleDao dao = db.teamMatchScheduleDao();
        TeamMatchSchedule item = new TeamMatchSchedule();
        item.matchNumber = 1;
        item.crowdPosition = 1;
        item.teamNumber = "1234";
        item.alliance = "RED";
        
        dao.insert(item);

        assertEquals("1234", dao.getTeamNumber(1, 1));
        assertNull(dao.getTeamNumber(1, 2));
    }

    @Test
    public void testGetMatchCount() {
        TeamMatchScheduleDao dao = db.teamMatchScheduleDao();
        TeamMatchSchedule i1 = new TeamMatchSchedule(); i1.matchNumber = 1; i1.crowdPosition = 1; i1.teamNumber = "1"; i1.alliance = "R";
        TeamMatchSchedule i2 = new TeamMatchSchedule(); i2.matchNumber = 2; i2.crowdPosition = 1; i2.teamNumber = "2"; i2.alliance = "B";
        
        dao.insert(i1);
        dao.insert(i2);

        // Assuming getMatchCount returns number of unique matches
        assertEquals(2, dao.getMatchCount());
    }
}
