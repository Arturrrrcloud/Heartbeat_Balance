package com.example.heartrateapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Heartraterecord {

    private final int    id;
    private final int    bpm;
    private final long   timestamp;
    private final String note;

    public Heartraterecord(int id, int bpm, long timestamp, String note) {
        this.id        = id;
        this.bpm       = bpm;
        this.timestamp = timestamp;
        this.note      = note;
    }

    public int getId() { return id; }
    public int getBpm() { return bpm; }
    public long getTimestamp() { return timestamp; }
    public String getNote() { return note; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getBpmCategory() {
        if (bpm < 60)  return "Wolne";
        if (bpm <= 100) return "Normalne";
        return "Szybkie";
    }
}
