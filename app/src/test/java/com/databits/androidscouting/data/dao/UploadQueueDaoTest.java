package com.databits.androidscouting.data.dao;

import com.databits.androidscouting.data.entity.UploadQueueItem;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class UploadQueueDaoTest extends DaoTestBase {

    @Test
    public void testPendingUploads() {
        UploadQueueDao dao = db.uploadQueueDao();
        UploadQueueItem item = new UploadQueueItem();
        item.dataCsv = "data";
        item.uploadStatus = "PENDING";
        
        dao.insert(item);

        List<UploadQueueItem> pending = dao.getPendingUploadsSync();
        assertEquals(1, pending.size());
        assertEquals("data", pending.get(0).dataCsv);
    }

    @Test
    public void testUpdateStatus() {
        UploadQueueDao dao = db.uploadQueueDao();
        UploadQueueItem item = new UploadQueueItem();
        item.uploadStatus = "PENDING";
        long id = dao.insert(item);

        dao.updateStatus(id, "SUCCESS");
        
        List<UploadQueueItem> pending = dao.getPendingUploadsSync();
        assertTrue(pending.isEmpty());
    }
}
