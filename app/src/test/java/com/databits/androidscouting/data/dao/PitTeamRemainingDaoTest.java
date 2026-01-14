package com.databits.androidscouting.data.dao;

import com.databits.androidscouting.data.entity.PitTeamRemaining;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class PitTeamRemainingDaoTest extends DaoTestBase {

    @Test
    public void testInsertAndRetrieveTeams() {
        PitTeamRemainingDao dao = db.pitTeamRemainingDao();
        PitTeamRemaining team = new PitTeamRemaining();
        team.teamNumber = "1234";
        
        dao.insert(team);

        List<String> teams = dao.getAllTeamNumbers();
        assertEquals(1, teams.size());
        assertEquals("1234", teams.get(0));
    }

    @Test
    public void testRemoveTeam() {
        PitTeamRemainingDao dao = db.pitTeamRemainingDao();
        PitTeamRemaining team = new PitTeamRemaining();
        team.teamNumber = "1234";
        dao.insert(team);

        dao.removeTeam("1234");
        List<String> teams = dao.getAllTeamNumbers();
        assertTrue(teams.isEmpty());
    }
}
