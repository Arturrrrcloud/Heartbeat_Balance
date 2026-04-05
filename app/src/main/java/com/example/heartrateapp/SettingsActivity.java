package com.example.heartrateapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar seekVolume, seekTime;
    private TextView txtTimeValue;
    private TextView txtVolumeValue;


    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekVolume = findViewById(R.id.seekVolume);
        seekTime = findViewById(R.id.seekTime);
        txtTimeValue = findViewById(R.id.txtTimeValue);
        txtVolumeValue = findViewById(R.id.txtVolumeValue);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // Load saved settings
        int volume = prefs.getInt("volume", 50);
        int time = prefs.getInt("time", 30);

        seekVolume.setProgress(volume);
        seekTime.setProgress(time);
        txtVolumeValue.setText(volume + " %");
        txtTimeValue.setText(time + " sek");

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

        // ⏱️ Czas
        seekTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtTimeValue.setText(progress + " sek");
                prefs.edit().putInt("time", progress).apply();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.btnSaveSettings).setOnClickListener(v -> finish());

    }
}