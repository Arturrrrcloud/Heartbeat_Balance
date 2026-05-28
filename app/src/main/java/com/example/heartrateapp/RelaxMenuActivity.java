package com.example.heartrateapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class RelaxMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relax_menu);

        MaterialCardView cardDynamicRelax = findViewById(R.id.cardDynamicRelax);
        MaterialCardView cardRelaxGame = findViewById(R.id.cardRelaxGame);

        cardDynamicRelax.setOnClickListener(v -> {
            Intent intent = new Intent(RelaxMenuActivity.this, DynamicRelaxActivity.class);
            startActivity(intent);
        });

        cardRelaxGame.setOnClickListener(v -> {
            Intent intent = new Intent(RelaxMenuActivity.this, RelaxGameActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnRelaxBack).setOnClickListener(v -> finish());
    }
}