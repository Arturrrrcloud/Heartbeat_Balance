package com.example.heartrateapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class Historyactivity extends AppCompatActivity {

    private Databasehelper db;
    private List<Heartraterecord> records;
    private HistoryAdapter adapter;

    private TextView txtAvgBpm;
    private TextView txtNoData;
    private ListView listHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db           = Databasehelper.getInstance(this);
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
    }

    private void loadData() {
        records = db.getAllResults();

        if (records.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            listHistory.setVisibility(View.GONE);
            txtAvgBpm.setText("Brak danych");
            return;
        }

        txtNoData.setVisibility(View.GONE);
        listHistory.setVisibility(View.VISIBLE);

        double avg = db.getAverageBpm();
        txtAvgBpm.setText(String.format("Średnia: %.0f BPM", avg));

        adapter = new HistoryAdapter(this, records);
        listHistory.setAdapter(adapter);

        listHistory.setOnItemLongClickListener((parent, view, position, id) -> {
            Heartraterecord record = records.get(position);
            confirmDeleteSingle(record, position);
            return true;
        });
    }

    private void confirmDeleteSingle(Heartraterecord record, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Usuń pomiar")
                .setMessage("Usunąć pomiar " + record.getBpm() + " BPM z dnia " + record.getFormattedDate() + "?")
                .setPositiveButton("Usuń", (dialog, which) -> {
                    db.deleteResult(record.getId());
                    records.remove(position);
                    adapter.notifyDataSetChanged();
                    updateStats();
                    Toast.makeText(this, "Pomiar usunięty", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void confirmClearAll() {
        if (records == null || records.isEmpty()) {
            Toast.makeText(this, "Brak historii do usunięcia", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Wyczyść historię")
                .setMessage("Czy na pewno chcesz usunąć wszystkie " + records.size() + " pomiarów?")
                .setPositiveButton("Wyczyść", (dialog, which) -> {
                    db.deleteAllResults();
                    records.clear();
                    adapter.notifyDataSetChanged();
                    updateStats();
                    txtNoData.setVisibility(View.VISIBLE);
                    listHistory.setVisibility(View.GONE);
                    Toast.makeText(this, "Historia wyczyszczona", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void updateStats() {
        if (records.isEmpty()) {
            txtAvgBpm.setText("Brak danych");
            txtNoData.setVisibility(View.VISIBLE);
            listHistory.setVisibility(View.GONE);
        } else {
            double avg = db.getAverageBpm();
            txtAvgBpm.setText(String.format("Średnia: %.0f BPM", avg));
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
            if (record == null) return convertView;

            TextView txtBpm      = convertView.findViewById(R.id.txtRowBpm);
            TextView txtDate     = convertView.findViewById(R.id.txtRowDate);
            TextView txtCategory = convertView.findViewById(R.id.txtRowCategory);

            txtBpm.setText(record.getBpm() + " BPM");
            txtDate.setText(record.getFormattedDate());

            String category = record.getBpmCategory();
            txtCategory.setText(category);

            switch (category) {
                case "Wolne":
                    txtCategory.setTextColor(Color.parseColor("#64B5F6"));
                    break;
                case "Normalne":
                    txtCategory.setTextColor(Color.parseColor("#81C784"));
                    break;
                case "Szybkie":
                    txtCategory.setTextColor(Color.parseColor("#E57373"));
                    break;
                default:
                    txtCategory.setTextColor(Color.GRAY);
            }

            return convertView;
        }
    }
}
