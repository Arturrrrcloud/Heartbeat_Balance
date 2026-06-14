package com.example.heartrateapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar seekVolume;
    private TextView txtVolumeValue;


    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekVolume = findViewById(R.id.seekVolume);
        txtVolumeValue = findViewById(R.id.txtVolumeValue);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // Load saved settings
        int volume = prefs.getInt("volume", 50);

        seekVolume.setProgress(volume);
        txtVolumeValue.setText(volume + " %");

        // 🎵 Głośność
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                txtVolumeValue.setText(progress + "%");

                float vol = progress / 100f;
                MusicServiceHelper.setVolume(vol);

                prefs.edit().putInt("volume", progress).apply();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.btnSaveSettings).setOnClickListener(v -> finish());

    }
}