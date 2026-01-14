package com.databits.androidscouting.data.dao;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import com.databits.androidscouting.data.database.ScoutDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28})
public abstract class DaoTestBase {
    protected ScoutDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, ScoutDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }
}
