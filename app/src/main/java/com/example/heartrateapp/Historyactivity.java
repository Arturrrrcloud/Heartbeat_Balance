package com.example.heartrateapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class Historyactivity extends AppCompatActivity {

    private Databasehelper db;
    private List<Heartraterecord> records = new ArrayList<>();
    private HistoryAdapter adapter;

    private TextView txtAvgBpm;
    private TextView txtNoData;
    private ListView listHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_history);

            db = Databasehelper.getInstance(this);
            
            txtAvgBpm    = findViewById(R.id.txtAvgBpm);
            txtNoData    = findViewById(R.id.txtNoData);
            listHistory  = findViewById(R.id.listHistory);

            Button btnClearAll = findViewById(R.id.btnClearHistory);
            if (btnClearAll != null) {
                btnClearAll.setOnClickListener(v -> confirmClearAll());
            }

            View btnBack = findViewById(R.id.btnHistoryBack);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            loadData();
            
        } catch (Exception e) {
            Log.e("Historyactivity", "Crash in onCreate", e);
            Toast.makeText(this, "Błąd: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadData() {
        try {
            records = db.getAllResults();

            if (records == null || records.isEmpty()) {
                if (txtNoData != null) txtNoData.setVisibility(View.VISIBLE);
                if (listHistory != null) listHistory.setVisibility(View.GONE);
                if (txtAvgBpm != null) txtAvgBpm.setText("Brak danych");
                return;
            }

            if (txtNoData != null) txtNoData.setVisibility(View.GONE);
            if (listHistory != null) listHistory.setVisibility(View.VISIBLE);

            double avg = db.getAverageBpm();
            if (txtAvgBpm != null) {
                txtAvgBpm.setText(String.format(java.util.Locale.getDefault(), "Średnia: %.0f BPM", avg));
            }

            adapter = new HistoryAdapter(this, records);
            if (listHistory != null) {
                listHistory.setAdapter(adapter);
                listHistory.setOnItemLongClickListener((parent, view, position, id) -> {
                    if (position >= 0 && position < records.size()) {
                        confirmDeleteSingle(records.get(position), position);
                    }
                    return true;
                });
            }
        } catch (Exception e) {
            Log.e("Historyactivity", "Error loading data", e);
        }
    }

    private void confirmDeleteSingle(Heartraterecord record, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Usuń pomiar")
                .setMessage("Usunąć ten pomiar?")
                .setPositiveButton("Usuń", (dialog, which) -> {
                    db.deleteOne(record.getId()); // Poprawiono nazwę metody
                    records.remove(position);
                    if (adapter != null) adapter.notifyDataSetChanged();
                    updateStats();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void confirmClearAll() {
        if (records.isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle("Wyczyść historię")
                .setMessage("Usunąć wszystkie wpisy?")
                .setPositiveButton("Wyczyść", (dialog, which) -> {
                    db.deleteAll(); // Poprawiono nazwę metody
                    records.clear();
                    if (adapter != null) adapter.notifyDataSetChanged();
                    updateStats();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void updateStats() {
        if (records.isEmpty()) {
            if (txtAvgBpm != null) txtAvgBpm.setText("Brak danych");
            if (txtNoData != null) txtNoData.setVisibility(View.VISIBLE);
            if (listHistory != null) listHistory.setVisibility(View.GONE);
        } else {
            double avg = db.getAverageBpm();
            if (txtAvgBpm != null) {
                txtAvgBpm.setText(String.format(java.util.Locale.getDefault(), "Średnia: %.0f BPM", avg));
            }
        }
    }

    private static class HistoryAdapter extends ArrayAdapter<Heartraterecord> {
        HistoryAdapter(Context context, List<Heartraterecord> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_history_row, parent, false);
            }

            Heartraterecord record = getItem(position);
            if (record != null) {
                TextView txtBpm      = convertView.findViewById(R.id.txtRowBpm);
                TextView txtDate     = convertView.findViewById(R.id.txtRowDate);
                TextView txtCategory = convertView.findViewById(R.id.txtRowCategory);

                if (txtBpm != null) txtBpm.setText(record.getBpm() + " BPM");
                if (txtDate != null) txtDate.setText(record.getFormattedDate());
                
                String category = record.getBpmCategory();
                if (txtCategory != null) {
                    txtCategory.setText(category);
                    if (category.equals("Wolne")) txtCategory.setTextColor(Color.parseColor("#90CAF9"));
                    else if (category.equals("Normalne")) txtCategory.setTextColor(Color.parseColor("#A5D6A7"));
                    else txtCategory.setTextColor(Color.parseColor("#FF8A80"));
                }
            }
            return convertView;
        }
    }
}
