package com.example.heartrateapp;

import android.app.AlertDialog;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class DynamicRelaxActivity extends AppCompatActivity {

    private static final int TOTAL_TIME_SECONDS = 240;

    private static class Step {
        final String title;
        final String hint;
        final String videoName;

        Step(String title, String hint, String videoName) {
            this.title = title;
            this.hint = hint;
            this.videoName = videoName;
        }
    }

    private Step[] steps;
    private int currentStep = 0;
    private boolean running = false;
    private long remainingMs = TOTAL_TIME_SECONDS * 1000L;
    private CountDownTimer timer;

    private TextView txtStepTitle;
    private TextView txtStepHint;
    private TextView txtStepTimer;
    private TextView txtStepCounter;
    private TextView txtVideoHint;

    private TextView[] stepTabs;

    private VideoView videoPreview;

    private Button btnStartPause;
    private Button btnPrev;
    private Button btnNext;

    private final int calmBackground = Color.parseColor("#102A36");
    private final int calmSurface = Color.parseColor("#1D5663");
    private final int calmAccent = Color.parseColor("#64FFDA");
    private final int textPrimary = Color.parseColor("#FFFFFF");
    private final int textHint = Color.parseColor("#B0BEC5");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        steps = new Step[] {
                new Step(
                        "Przysiady",
                        "Stań prosto, ustaw stopy na szerokość bioder. Powoli ugnij kolana, zejdź w dół jak do siadania na krześle, a następnie wróć do pozycji stojącej. Plecy trzymaj prosto.",
                        "exercise_squats"
                ),
                new Step(
                        "Pajacyki",
                        "Stań prosto z rękami wzdłuż ciała. Wykonuj podskoki z jednoczesnym rozkładaniem nóg i unoszeniem rąk nad głowę. Ćwicz spokojnym tempem.",
                        "exercise_jumping_jacks"
                ),
                new Step(
                        "Marsz w miejscu",
                        "Maszeruj w miejscu, unosząc kolana na wygodną wysokość. Pracuj rękami naturalnie jak podczas zwykłego marszu. Oddychaj równo.",
                        "exercise_march"
                ),
                new Step(
                        "Pompki",
                        "Przyjmij pozycję podporu przodem. Dłonie ustaw pod barkami, ciało trzymaj w jednej linii. Uginaj łokcie i opuszczaj klatkę w stronę podłoża, a potem wróć do góry. Możesz wykonywać pompki na kolanach.",
                        "exercise_pushups"
                ),
                new Step(
                        "Krążenia ramion",
                        "Stań swobodnie i wykonuj powolne krążenia ramionami. Najpierw krąż do przodu, a w połowie czasu zmień kierunek do tyłu.",
                        "exercise_arm_circles"
                )
        };

        createLayout();

        btnStartPause.setOnClickListener(v -> {
            if (running) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentStep > 0) {
                setStep(currentStep - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentStep < steps.length - 1) {
                setStep(currentStep + 1);
            }
        });

        setStep(0);
        updateTimerText(remainingMs);

        // NOWOŚĆ: Wyświetlamy okienko powitalne po załadowaniu ekranu
        showWelcomeDialog();
    }

    // --- NOWOŚĆ: Budowanie i wyświetlanie okienka powitalnego z kodu ---
    private void showWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Tworzenie kontenera dla nakładki
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(dp(24), dp(24), dp(24), dp(24));

        // Ustawianie zaokrąglonego tła w kolorze calmSurface
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dp(16));
        shape.setColor(calmSurface);
        dialogLayout.setBackground(shape);

        // Tytuł
        TextView title = new TextView(this);
        title.setText("Plan Treningu");
        title.setTextColor(calmAccent);
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(16));
        dialogLayout.addView(title);

        // Opis z listą ćwiczeń
        TextView desc = new TextView(this);
        desc.setText("Przed Tobą zestaw 5 ćwiczeń relaksacyjnych:\n\n" +
                "1. Przysiady\n" +
                "2. Pajacyki\n" +
                "3. Marsz w miejscu\n" +
                "4. Pompki\n" +
                "5. Krążenia ramion\n\n" +
                "Masz na nie łącznie 4 minuty. Sam decydujesz, ile czasu spędzisz na każdym z nich. Po prostu używaj przycisków Wstecz i Dalej, by przechodzić do kolejnych zadań we własnym tempie.");
        desc.setTextColor(textPrimary);
        desc.setTextSize(16);
        desc.setLineSpacing(dp(4), 1.0f);
        desc.setPadding(0, 0, 0, dp(24));
        dialogLayout.addView(desc);

        // Przycisk zamykający
        Button btnClose = createButton("Zrozumiałem, zaczynamy", calmAccent, calmBackground);
        btnClose.setTypeface(null, android.graphics.Typeface.BOLD);
        dialogLayout.addView(btnClose);

        builder.setView(dialogLayout);
        AlertDialog dialog = builder.create();

        // Przezroczyste tło systemowe, żeby było widać nasze zaokrąglone rogi
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        // Zamknięcie okienka
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    // --------------------------------------------------------------------

    private void createLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(calmBackground);
        scrollView.setFitsSystemWindows(true);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setGravity(Gravity.CENTER_HORIZONTAL);
        main.setPadding(dp(24), dp(24), dp(24), dp(24));

        scrollView.addView(main);

        txtStepCounter = new TextView(this);
        txtStepCounter.setTextColor(textHint);
        txtStepCounter.setTextSize(14);
        txtStepCounter.setGravity(Gravity.CENTER);
        main.addView(txtStepCounter, fullWidthWrap());

        txtStepTitle = new TextView(this);
        txtStepTitle.setTextColor(calmAccent);
        txtStepTitle.setTextSize(24);
        txtStepTitle.setGravity(Gravity.CENTER);
        txtStepTitle.setPadding(0, dp(10), 0, dp(12));
        txtStepTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        main.addView(txtStepTitle, fullWidthWrap());

        txtStepHint = new TextView(this);
        txtStepHint.setTextColor(textPrimary);
        txtStepHint.setTextSize(16);
        txtStepHint.setGravity(Gravity.CENTER);
        txtStepHint.setLineSpacing(dp(4), 1.0f);
        txtStepHint.setPadding(0, 0, 0, dp(16));
        txtStepHint.setLines(4);
        main.addView(txtStepHint, fullWidthWrap());

        videoPreview = new VideoView(this);

        LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(220)
        );
        videoParams.setMargins(0, 0, 0, dp(12));
        main.addView(videoPreview, videoParams);

        txtVideoHint = new TextView(this);
        txtVideoHint.setTextColor(textHint);
        txtVideoHint.setTextSize(13);
        txtVideoHint.setGravity(Gravity.CENTER);
        txtVideoHint.setPadding(0, 0, 0, dp(12));
        txtVideoHint.setText("Film uruchamia się automatycznie i działa w pętli.");
        main.addView(txtVideoHint, fullWidthWrap());

        txtStepTimer = new TextView(this);
        txtStepTimer.setTextColor(calmAccent);
        txtStepTimer.setTextSize(48);
        txtStepTimer.setGravity(Gravity.CENTER);
        txtStepTimer.setTypeface(null, android.graphics.Typeface.BOLD);
        txtStepTimer.setPadding(0, dp(10), 0, dp(16));
        main.addView(txtStepTimer, fullWidthWrap());

        btnStartPause = createButton("Start", calmAccent, calmBackground);
        btnStartPause.setTypeface(null, android.graphics.Typeface.BOLD);
        main.addView(btnStartPause, buttonParams());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        btnPrev = createButton("Wstecz", calmSurface, textPrimary);
        btnNext = createButton("Dalej", calmSurface, textPrimary);

        LinearLayout.LayoutParams halfLeft = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        halfLeft.setMargins(0, 0, dp(8), 0);

        LinearLayout.LayoutParams halfRight = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        halfRight.setMargins(dp(8), 0, 0, 0);

        row.addView(btnPrev, halfLeft);
        row.addView(btnNext, halfRight);

        main.addView(row, fullWidthWrap());

        Button btnFinish = createButton("Zakończ", calmBackground, textHint);
        btnFinish.setOnClickListener(v -> finish());

        LinearLayout.LayoutParams finishParams = buttonParams();
        finishParams.setMargins(0, dp(16), 0, dp(24));
        main.addView(btnFinish, finishParams);

        TextView txtTabsHeader = new TextView(this);
        txtTabsHeader.setText("Plan treningu:");
        txtTabsHeader.setTextColor(textPrimary);
        txtTabsHeader.setTextSize(16);
        txtTabsHeader.setPadding(0, 0, 0, dp(12));
        main.addView(txtTabsHeader, fullWidthWrap());

        LinearLayout tabsContainer = new LinearLayout(this);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setWeightSum(3f);
        main.addView(tabsContainer, fullWidthWrap());

        LinearLayout col1 = new LinearLayout(this); col1.setOrientation(LinearLayout.VERTICAL);
        LinearLayout col2 = new LinearLayout(this); col2.setOrientation(LinearLayout.VERTICAL);
        LinearLayout col3 = new LinearLayout(this); col3.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tabsContainer.addView(col1, colParams);
        tabsContainer.addView(col2, colParams);
        tabsContainer.addView(col3, colParams);

        stepTabs = new TextView[steps.length];
        for (int i = 0; i < steps.length; i++) {
            TextView tv = new TextView(this);
            tv.setText((i + 1) + ". " + steps[i].title);
            tv.setTextSize(13);
            tv.setPadding(0, dp(2), dp(4), dp(6));

            tv.setLines(2);

            stepTabs[i] = tv;

            if (i < 2) {
                col1.addView(tv);
            } else if (i < 4) {
                col2.addView(tv);
            } else {
                col3.addView(tv);
            }
        }

        setContentView(scrollView);
    }

    private Button createButton(String text, int backgroundColor, int textColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(textColor);
        button.setBackgroundColor(backgroundColor);
        return button;
    }

    private LinearLayout.LayoutParams fullWidthWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        return params;
    }

    private void setStep(int idx) {
        currentStep = idx;
        Step s = steps[currentStep];

        txtStepTitle.setText(s.title);
        txtStepHint.setText(s.hint);
        txtStepCounter.setText("Ćwiczenie " + (currentStep + 1) + " z " + steps.length);

        for (int i = 0; i < stepTabs.length; i++) {
            if (i == currentStep) {
                stepTabs[i].setTextColor(calmAccent);
                stepTabs[i].setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                stepTabs[i].setTextColor(textHint);
                stepTabs[i].setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }

        btnPrev.setEnabled(currentStep > 0);
        btnNext.setEnabled(currentStep < steps.length - 1);

        playExerciseVideo(s.videoName);
    }

    private void playExerciseVideo(String videoName) {
        int videoId = getResources().getIdentifier(videoName, "raw", getPackageName());

        if (videoId == 0) {
            txtVideoHint.setText("Brakuje filmu w folderze res/raw: " + videoName + ".mp4");
            videoPreview.stopPlayback();
            return;
        }

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + videoId);
        videoPreview.setVideoURI(videoUri);

        videoPreview.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0f, 0f);
            videoPreview.start();
        });

        videoPreview.setOnCompletionListener(MediaPlayer::start);
    }

    private void startTimer() {
        if (remainingMs <= 0) {
            remainingMs = TOTAL_TIME_SECONDS * 1000L;
        }

        running = true;
        btnStartPause.setText("Pauza");

        timer = new CountDownTimer(remainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                updateTimerText(remainingMs);
            }

            @Override
            public void onFinish() {
                remainingMs = 0;
                updateTimerText(0);
                running = false;
                btnStartPause.setText("Koniec!");
                txtStepHint.setText("Trening zakończony. Zrób jeszcze kilka spokojnych oddechów i powoli wróć do normalnej aktywności.");
            }
        }.start();
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        running = false;
        btnStartPause.setText("Start");
    }

    private void updateTimerText(long ms) {
        long totalSec = Math.max(0, ms) / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;

        String text = min + ":" + (sec < 10 ? "0" + sec : String.valueOf(sec));
        txtStepTimer.setText(text);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (running) {
            pauseTimer();
        }

        if (videoPreview != null) {
            videoPreview.pause();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (videoPreview != null && steps != null) {
            playExerciseVideo(steps[currentStep].videoName);
        }
    }
}