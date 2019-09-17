package movieparser.example.android.popularmoviesstage1.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {FavoriteEntry.class}, version = 1, exportSchema = false)
public abstract class FavoriteDatabase extends RoomDatabase {

    private static final String LOG_TAG = FavoriteDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "favorites";
    private static FavoriteDatabase sInstance;

    public static FavoriteDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        FavoriteDatabase.class, FavoriteDatabase.DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract FavoriteDao favoriteDao();
}
