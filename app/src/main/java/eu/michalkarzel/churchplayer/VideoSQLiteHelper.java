package eu.michalkarzel.churchplayer;

/**
 * Created by misio on 21.3.15.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VideoSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "videos";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "video";
    public static final String COLUMN_SAVED = "saved";
    public static final String COLUMN_FOLDER = "folder";
    public static final String COLUMN_IMAGE = "image";

    private static final String DATABASE_NAME = "videos.db";
    private static final int DATABASE_VERSION = 8;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table " + TABLE_NAME + "("
            + COLUMN_ID + " integer, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_SAVED + " integer default 0, "
            + COLUMN_FOLDER + " text primary key, "
            + COLUMN_IMAGE + " blob "
            + ");";

    public VideoSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(VideoSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
