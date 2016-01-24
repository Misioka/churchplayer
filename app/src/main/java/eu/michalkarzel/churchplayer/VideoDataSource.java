package eu.michalkarzel.churchplayer;

/**
 * Created by misio on 21.3.15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class VideoDataSource {

    // Database fields
    private SQLiteDatabase database;
    private VideoSQLiteHelper dbHelper;
    private String[] allColumns = {
            VideoSQLiteHelper.COLUMN_ID,
            VideoSQLiteHelper.COLUMN_NAME,
            VideoSQLiteHelper.COLUMN_FOLDER,
            VideoSQLiteHelper.COLUMN_SAVED,
            VideoSQLiteHelper.COLUMN_IMAGE};

    public VideoDataSource(Context context) {
        dbHelper = new VideoSQLiteHelper(context);
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Video createVideo(String video, String folder) {
        ContentValues values = new ContentValues();
        values.put(VideoSQLiteHelper.COLUMN_NAME, video);
        values.put(VideoSQLiteHelper.COLUMN_FOLDER, folder);
        long insertId = database.insert(VideoSQLiteHelper.TABLE_NAME, null,
                values);
        Cursor cursor = database.query(VideoSQLiteHelper.TABLE_NAME,
                allColumns, VideoSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Video newVideo = cursorToVideo(cursor);
        cursor.close();
        return newVideo;
    }

    void insertUpdate(String name, String folder) {
        ContentValues values = new ContentValues();
        values.put(VideoSQLiteHelper.COLUMN_NAME, name);
        values.put(VideoSQLiteHelper.COLUMN_FOLDER, folder);
        database.execSQL("insert or replace into " + VideoSQLiteHelper.TABLE_NAME + " " +
                "(" + VideoSQLiteHelper.COLUMN_ID + " ,"
                + VideoSQLiteHelper.COLUMN_NAME + " ,"
                + VideoSQLiteHelper.COLUMN_SAVED + " ,"
                + VideoSQLiteHelper.COLUMN_FOLDER + " ,"
                + VideoSQLiteHelper.COLUMN_IMAGE + ") values (" +
                "   (select " + VideoSQLiteHelper.COLUMN_ID + " from " + VideoSQLiteHelper.TABLE_NAME + " where " + VideoSQLiteHelper.COLUMN_FOLDER + " = " + folder + "),"
                + DatabaseUtils.sqlEscapeString(name) + ", " +
                "   (select " + VideoSQLiteHelper.COLUMN_SAVED + " from " + VideoSQLiteHelper.TABLE_NAME + " where " + VideoSQLiteHelper.COLUMN_FOLDER + " = " + folder + "),"
                + DatabaseUtils.sqlEscapeString(folder) + ", " +
                "   (select " + VideoSQLiteHelper.COLUMN_IMAGE + " from " + VideoSQLiteHelper.TABLE_NAME + " where " + VideoSQLiteHelper.COLUMN_FOLDER + " = " + folder + "));");
    }

    public boolean setImage(Bitmap image, String date) {
        if (image != null) {
            ContentValues values = new ContentValues();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
            byte[] buffer = out.toByteArray();
            values.put(VideoSQLiteHelper.COLUMN_IMAGE, buffer);
            String[] whereArgs = new String[]{DatabaseUtils.sqlEscapeString(date)};
            database.update(VideoSQLiteHelper.TABLE_NAME, values, VideoSQLiteHelper.COLUMN_FOLDER + "=?", whereArgs);
            return true;
        }
        return false;
    }

    public void setSaved(String date) {
        ContentValues values = new ContentValues();
        values.put(VideoSQLiteHelper.COLUMN_SAVED, date);
        database.update(VideoSQLiteHelper.TABLE_NAME, values, VideoSQLiteHelper.COLUMN_FOLDER + " = " + date, null);
    }

    public void deleteVideo(Video video) {
        long id = video.getId();
        database.delete(VideoSQLiteHelper.TABLE_NAME, VideoSQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void deleteAll() {
        database.delete(VideoSQLiteHelper.TABLE_NAME, null, null);
    }

    public Video getVideoBy(String column, String value) {
        Cursor cursor = database.query(VideoSQLiteHelper.TABLE_NAME,
                allColumns, column + " = " + value, null, null, null, null);
        cursor.moveToFirst();
        Video video = cursorToVideo(cursor);
        cursor.close();
        return video;
    }

    public List<Video> getAllVideos() {
        List<Video> videos = new ArrayList<Video>();

        Cursor cursor = database.query(VideoSQLiteHelper.TABLE_NAME,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Video video = cursorToVideo(cursor);
            videos.add(video);
            cursor.moveToNext();
        }
        cursor.close();
        return videos;
    }

    private Video cursorToVideo(Cursor cursor) {
        Video video = new Video();
        video.setId(cursor.getLong(0));
        video.setVideo(cursor.getString(1));
        video.setFolder(cursor.getString(2));
        video.setSaved(cursor.getInt(3));
        byte[] image = cursor.getBlob(4);
        if (image != null) {
            video.setImage(BitmapFactory.decodeByteArray(image, 0, image.length));
        }
        return video;
    }
}

