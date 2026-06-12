package com.example.heartrateapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String resultText = getIntent().getStringExtra("RESULT_TEXT");
        if (resultText == null) {
            resultText = "Błąd odczytu";
        }

        TextView txtResult = findViewById(R.id.txtResult);
        txtResult.setText(resultText);

        // Próba zapisu do bazy
        try {
            int bpm = Integer.parseInt(resultText.trim());
            Databasehelper db = Databasehelper.getInstance(this);
            long id = db.insertResult(bpm, "Pomiar automatyczny");
            
            if (id != -1) {
                Toast.makeText(this, "Wynik zapisany w historii", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            // Wynik nie jest liczbą (np. "Błąd pomiaru") - nie zapisujemy
        }

        findViewById(R.id.btnRetry).setOnClickListener(v -> {
            startActivity(new Intent(ResultActivity.this, MeasurementActivity.class));
            finish();
        });

        findViewById(R.id.btnRelax).setOnClickListener(v -> {
            startActivity(new Intent(ResultActivity.this, RelaxMenuActivity.class));
        });

        Button btnHistory = findViewById(R.id.btnHistory);
        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> {
                startActivity(new Intent(ResultActivity.this, Historyactivity.class));
            });
        }

        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
    }
}
