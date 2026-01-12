package com.databits.androidscouting.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.databits.androidscouting.data.dao.UploadQueueDao;
import com.databits.androidscouting.data.dao.TeamMatchScheduleDao;
import com.databits.androidscouting.data.dao.MatchDataDao;
import com.databits.androidscouting.data.dao.SeenLineDao;
import com.databits.androidscouting.data.dao.ProcessedChunkDao;
import com.databits.androidscouting.data.dao.ScouterDao;
import com.databits.androidscouting.data.dao.PitTeamRemainingDao;
import com.databits.androidscouting.data.entity.UploadQueueItem;
import com.databits.androidscouting.data.entity.TeamMatchSchedule;
import com.databits.androidscouting.data.entity.MatchData;
import com.databits.androidscouting.data.entity.SeenLine;
import com.databits.androidscouting.data.entity.ProcessedChunk;
import com.databits.androidscouting.data.entity.Scouter;
import com.databits.androidscouting.data.entity.PitTeamRemaining;

@Database(
    entities = {
        UploadQueueItem.class,
        TeamMatchSchedule.class,
        MatchData.class,
        SeenLine.class,
        ProcessedChunk.class,
        Scouter.class,
        PitTeamRemaining.class
    },
    version = 1,
    exportSchema = false
)
public abstract class ScoutDatabase extends RoomDatabase {
    private static volatile ScoutDatabase INSTANCE;

    public abstract UploadQueueDao uploadQueueDao();
    public abstract TeamMatchScheduleDao teamMatchScheduleDao();
    public abstract MatchDataDao matchDataDao();
    public abstract SeenLineDao seenLineDao();
    public abstract ProcessedChunkDao processedChunkDao();
    public abstract ScouterDao scouterDao();
    public abstract PitTeamRemainingDao pitTeamRemainingDao();

    public static ScoutDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ScoutDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        ScoutDatabase.class,
                        "scout_database"
                    )
                    .fallbackToDestructiveMigration()  // Fresh start acceptable per user
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
