package com.databits.androidscouting.data.dao;

import com.databits.androidscouting.data.entity.ProcessedChunk;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class ProcessedChunkDaoTest extends DaoTestBase {

    @Test
    public void testInsertAndCheckChunk() {
        ProcessedChunkDao dao = db.processedChunkDao();
        ProcessedChunk chunk = new ProcessedChunk();
        chunk.chunkId = 100;
        
        dao.insert(chunk);

        assertTrue(dao.hasProcessedChunk(100));
        assertFalse(dao.hasProcessedChunk(999));
    }

    @Test
    public void testGetAllChunkIds() {
        ProcessedChunkDao dao = db.processedChunkDao();
        ProcessedChunk c1 = new ProcessedChunk(); c1.chunkId = 1;
        ProcessedChunk c2 = new ProcessedChunk(); c2.chunkId = 2;
        
        dao.insert(c1);
        dao.insert(c2);

        List<Integer> ids = dao.getAllChunkIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains(1));
        assertTrue(ids.contains(2));
    }
}
