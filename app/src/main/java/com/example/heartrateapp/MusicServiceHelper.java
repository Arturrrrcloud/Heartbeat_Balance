package com.example.heartrateapp;

public class MusicServiceHelper {

    public static void setVolume(float volume) {
        if (MusicService.mediaPlayerStatic != null) {
            MusicService.mediaPlayerStatic.setVolume(volume, volume);
        }
    }
}