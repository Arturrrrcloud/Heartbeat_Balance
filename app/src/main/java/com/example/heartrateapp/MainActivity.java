package com.example.heartrateapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Uruchamiamy serwis muzyczny
        Intent musicIntent = new Intent(this, MusicService.class);
        startService(musicIntent);

        // 2. Obsługa przycisku muzyki
        FloatingActionButton btnMusic = findViewById(R.id.btnMusicToggle);
        updateMusicIcon(btnMusic);

        btnMusic.setOnClickListener(v -> {
            Intent toggleIntent = new Intent(this, MusicService.class);
            toggleIntent.setAction("ACTION_TOGGLE");
            startService(toggleIntent);

            MusicService.isMusicPlaying = !MusicService.isMusicPlaying;
            updateMusicIcon(btnMusic);
        });

        // --- OBSŁUGA PRZYCISKÓW MENU ---

        // Pomiar
        findViewById(R.id.cardMeasure).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MeasurementActivity.class));
        });

        // NOWY: Historia pomiarów
        findViewById(R.id.btnHistory).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Historyactivity.class));
        });

        // Relaks
        findViewById(R.id.btnRelax).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RelaxMenuActivity.class));
        });

        // Ustawienia
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
    }

    private void updateMusicIcon(FloatingActionButton btn) {
        if (MusicService.isMusicPlaying) {
            btn.setImageResource(R.drawable.ic_music_on);
        } else {
            btn.setImageResource(R.drawable.ic_music_off);
        }
    }
}
