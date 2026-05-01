package com.example.heartrateapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class Databasehelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "heartrateapp.db";
    private static final int    DB_VERSION = 1;

    public static final String TABLE_NAME  = "heart_rate_results";
    public static final String COL_ID        = "_id";
    public static final String COL_BPM       = "bpm";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_NOTE      = "note";

    private static Databasehelper instance;

    public static synchronized Databasehelper getInstance(Context context) {
        if (instance == null) {
            instance = new Databasehelper(context.getApplicationContext());
        }
        return instance;
    }

    private Databasehelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COL_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_BPM       + " INTEGER NOT NULL, " +
                        COL_TIMESTAMP + " INTEGER NOT NULL, " +
                        COL_NOTE      + " TEXT" +
                        ");";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertResult(int bpm, String note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BPM, bpm);
        values.put(COL_TIMESTAMP, System.currentTimeMillis());
        values.put(COL_NOTE, note != null ? note : "");
        return db.insert(TABLE_NAME, null, values);
    }

    public List<Heartraterecord> getAllResults() {
        List<Heartraterecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                null, null,
                null, null,
                COL_TIMESTAMP + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int    id        = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                int    bpm       = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BPM));
                long   timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
                String note      = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE));
                list.add(new Heartraterecord(id, bpm, timestamp, note));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public int deleteResult(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteAllResults() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public double getAverageBpm() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(" + COL_BPM + ") FROM " + TABLE_NAME, null);
        double avg = 0;
        if (cursor != null) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                avg = cursor.getDouble(0);
            }
            cursor.close();
        }
        return avg;
    }
}
