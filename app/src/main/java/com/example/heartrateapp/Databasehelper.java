package com.example.heartrateapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class Databasehelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "heartrate_stable_final.db";
    private static final int    DB_VERSION = 1;

    public static final String TABLE_NAME  = "heart_rate_results";
    public static final String COL_ID      = "_id";
    public static final String COL_BPM     = "bpm";
    public static final String COL_TIME    = "timestamp";
    public static final String COL_NOTE    = "note";

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
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BPM + " INTEGER, " +
                COL_TIME + " INTEGER, " +
                COL_NOTE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertResult(int bpm, String note) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_BPM, bpm);
            values.put(COL_TIME, System.currentTimeMillis());
            values.put(COL_NOTE, note != null ? note : "");
            return db.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            return -1;
        }
    }

    public List<Heartraterecord> getAllResults() {
        List<Heartraterecord> list = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_TIME + " DESC");
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int idIdx = cursor.getColumnIndexOrThrow(COL_ID);
                    int bpmIdx = cursor.getColumnIndexOrThrow(COL_BPM);
                    int timeIdx = cursor.getColumnIndexOrThrow(COL_TIME);
                    int noteIdx = cursor.getColumnIndexOrThrow(COL_NOTE);
                    do {
                        list.add(new Heartraterecord(
                                cursor.getInt(idIdx),
                                cursor.getInt(bpmIdx),
                                cursor.getLong(timeIdx),
                                cursor.getString(noteIdx)
                        ));
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getAverageBpm() {
        double avg = 0;
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery("SELECT AVG(" + COL_BPM + ") FROM " + TABLE_NAME, null);
            if (c != null && c.moveToFirst()) {
                avg = c.getDouble(0);
                c.close();
            }
        } catch (Exception e) {}
        return avg;
    }

    public void deleteOne(int id) {
        try {
            getWritableDatabase().delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {}
    }

    public void deleteAll() {
        try {
            getWritableDatabase().delete(TABLE_NAME, null, null);
        } catch (Exception e) {}
    }
}
