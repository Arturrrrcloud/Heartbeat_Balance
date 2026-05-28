package com.example.heartrateapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RelaxGameActivity extends AppCompatActivity {

    private TextView txtTitle;
    private TextView txtProgress;
    private TextView txtStats;
    private TextView txtHint;
    private TreeGardenView treeGardenView;

    private final int calmBackground = Color.parseColor("#102A36");
    private final int calmSurface = Color.parseColor("#1D5663");
    private final int calmAccent = Color.parseColor("#64FFDA");
    private final int textPrimary = Color.parseColor("#FFFFFF");
    private final int textHint = Color.parseColor("#B0BEC5");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLayout();
    }

    private void createLayout() {
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setGravity(Gravity.CENTER_HORIZONTAL);
        main.setPadding(dp(18), dp(18), dp(18), dp(18));
        main.setBackgroundColor(calmBackground);

        txtTitle = new TextView(this);
        txtTitle.setText("Sadzenie drzewa");
        txtTitle.setTextColor(calmAccent);
        txtTitle.setTextSize(28);
        txtTitle.setTypeface(null, Typeface.BOLD);
        txtTitle.setGravity(Gravity.CENTER);
        main.addView(txtTitle, fullWidthWrap());

        txtProgress = new TextView(this);
        txtProgress.setText("Wzrost: 0%");
        txtProgress.setTextColor(textPrimary);
        txtProgress.setTextSize(16);
        txtProgress.setGravity(Gravity.CENTER);
        txtProgress.setPadding(0, dp(6), 0, dp(2));
        main.addView(txtProgress, fullWidthWrap());

        txtStats = new TextView(this);
        txtStats.setText("Woda: 0   Słońce: 0   Opieka: 0");
        txtStats.setTextColor(textHint);
        txtStats.setTextSize(14);
        txtStats.setGravity(Gravity.CENTER);
        main.addView(txtStats, fullWidthWrap());

        txtHint = new TextView(this);
        txtHint.setText("Zbieraj wodę, słońce i liście. Omijaj chwasty.");
        txtHint.setTextColor(textHint);
        txtHint.setTextSize(13);
        txtHint.setGravity(Gravity.CENTER);
        txtHint.setPadding(0, dp(8), 0, dp(12));
        main.addView(txtHint, fullWidthWrap());

        treeGardenView = new TreeGardenView(this);
        treeGardenView.setTreeListener((growth, water, sun, care, hint) -> {
            txtProgress.setText("Wzrost: " + growth + "%");
            txtStats.setText("Woda: " + water + "   Słońce: " + sun + "   Opieka: " + care);
            txtHint.setText(hint);
        });

        LinearLayout.LayoutParams gameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        );
        gameParams.setMargins(0, dp(8), 0, dp(14));
        main.addView(treeGardenView, gameParams);

        Button btnReset = createButton("Zasadź od nowa", calmAccent, calmBackground);
        btnReset.setTypeface(null, Typeface.BOLD);
        btnReset.setOnClickListener(v -> treeGardenView.resetGame());
        main.addView(btnReset, buttonParams());

        Button btnBack = createButton("Wróć", calmBackground, textHint);
        btnBack.setOnClickListener(v -> finish());
        main.addView(btnBack, buttonParams());

        setContentView(main);
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
        params.setMargins(0, 0, 0, dp(10));
        return params;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    public static class TreeGardenView extends View {

        interface TreeListener {
            void onTreeChanged(int growth, int water, int sun, int care, String hint);
        }

        private static class GardenItem {
            float x;
            float y;
            float vx;
            float vy;
            float radius;
            int type;
            String symbol;
            int color;

            GardenItem(float x, float y, float vx, float vy, float radius, int type, String symbol, int color) {
                this.x = x;
                this.y = y;
                this.vx = vx;
                this.vy = vy;
                this.radius = radius;
                this.type = type;
                this.symbol = symbol;
                this.color = color;
            }
        }

        private static class FloatingText {
            float x;
            float y;
            String text;
            int alpha;

            FloatingText(float x, float y, String text) {
                this.x = x;
                this.y = y;
                this.text = text;
                this.alpha = 230;
            }
        }

        private final Random random = new Random();
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final List<GardenItem> items = new ArrayList<>();
        private final List<FloatingText> floatingTexts = new ArrayList<>();

        private TreeListener listener;

        private int growth = 0;
        private int water = 0;
        private int sun = 0;
        private int care = 0;

        private final int backgroundTop = Color.parseColor("#143B48");
        private final int backgroundBottom = Color.parseColor("#081E26");
        private final int grassColor = Color.parseColor("#2E7D32");
        private final int trunkColor = Color.parseColor("#8D5A2B");
        private final int leafColor = Color.parseColor("#66BB6A");
        private final int leafDarkColor = Color.parseColor("#2E7D32");
        private final int waterColor = Color.parseColor("#4FC3F7");
        private final int sunColor = Color.parseColor("#FFD54F");
        private final int careColor = Color.parseColor("#81C784");
        private final int weedColor = Color.parseColor("#B0BEC5");

        public TreeGardenView(Context context) {
            super(context);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            resetGame();
        }

        public void setTreeListener(TreeListener listener) {
            this.listener = listener;
            notifyChange("Zbieraj wodę, słońce i liście, aby drzewo rosło.");
        }

        public void resetGame() {
            growth = 0;
            water = 0;
            sun = 0;
            care = 0;
            items.clear();
            floatingTexts.clear();

            if (getWidth() > 0 && getHeight() > 0) {
                createStartItems();
            }

            notifyChange("Zasadzone nasiono. Zbieraj elementy, żeby wyrosło drzewo.");
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            if (items.isEmpty()) {
                createStartItems();
            }
        }

        private void createStartItems() {
            items.clear();

            for (int i = 0; i < 10; i++) {
                addRandomItem();
            }
        }

        private void addRandomItem() {
            int width = Math.max(getWidth(), 1);
            int height = Math.max(getHeight(), 1);

            int chance = random.nextInt(100);
            int type;

            if (chance < 32) {
                type = 0; // woda
            } else if (chance < 62) {
                type = 1; // słońce
            } else if (chance < 88) {
                type = 2; // liść/opieka
            } else {
                type = 3; // chwast
            }

            String symbol;
            int color;
            float radius;

            if (type == 0) {
                symbol = "💧";
                color = waterColor;
                radius = 38;
            } else if (type == 1) {
                symbol = "☀";
                color = sunColor;
                radius = 40;
            } else if (type == 2) {
                symbol = "🍃";
                color = careColor;
                radius = 38;
            } else {
                symbol = "!";
                color = weedColor;
                radius = 36;
            }

            float x = radius + random.nextInt(Math.max(1, width - (int) (radius * 2)));
            float y = radius + random.nextInt(Math.max(1, height - (int) (radius * 2) - 80));

            float vx = randomSpeed();
            float vy = randomSpeed();

            items.add(new GardenItem(x, y, vx, vy, radius, type, symbol, color));
        }

        private float randomSpeed() {
            float speed = 0.35f + random.nextFloat() * 0.65f;
            return random.nextBoolean() ? speed : -speed;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            drawBackground(canvas);
            updateItems();
            drawTree(canvas);
            drawItems(canvas);
            drawFloatingTexts(canvas);

            postInvalidateOnAnimation();
        }

        private void drawBackground(Canvas canvas) {
            LinearGradient gradient = new LinearGradient(
                    0,
                    0,
                    0,
                    getHeight(),
                    backgroundTop,
                    backgroundBottom,
                    Shader.TileMode.CLAMP
            );

            paint.setShader(gradient);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            paint.setShader(null);

            paint.setColor(Color.argb(55, 46, 125, 50));
            canvas.drawOval(-80, getHeight() - 120, getWidth() + 80, getHeight() + 80, paint);

            paint.setColor(grassColor);
            canvas.drawRect(0, getHeight() - 65, getWidth(), getHeight(), paint);

            paint.setColor(Color.argb(70, 100, 255, 218));
            canvas.drawCircle(getWidth() - 60, 70, 45, paint);
        }

        private void updateItems() {
            int w = getWidth();
            int h = getHeight();

            while (items.size() < 10) {
                addRandomItem();
            }

            for (GardenItem item : items) {
                item.x += item.vx;
                item.y += item.vy;

                if (item.x < item.radius || item.x > w - item.radius) {
                    item.vx *= -1;
                }

                if (item.y < item.radius || item.y > h - item.radius - 70) {
                    item.vy *= -1;
                }
            }

            for (int i = floatingTexts.size() - 1; i >= 0; i--) {
                FloatingText ft = floatingTexts.get(i);
                ft.y -= 1.5f;
                ft.alpha -= 5;

                if (ft.alpha <= 0) {
                    floatingTexts.remove(i);
                }
            }
        }

        private void drawTree(Canvas canvas) {
            float cx = getWidth() / 2f;
            float groundY = getHeight() - 65;

            paint.setStyle(Paint.Style.FILL);

            if (growth < 15) {
                drawSeed(canvas, cx, groundY);
            } else if (growth < 35) {
                drawSprout(canvas, cx, groundY);
            } else if (growth < 70) {
                drawSmallTree(canvas, cx, groundY);
            } else {
                drawBigTree(canvas, cx, groundY);
            }

            paint.setStyle(Paint.Style.FILL);
        }

        private void drawSeed(Canvas canvas, float cx, float groundY) {
            paint.setColor(Color.parseColor("#A1887F"));
            canvas.drawOval(cx - 18, groundY - 18, cx + 18, groundY + 8, paint);

            textPaint.setTextSize(20);
            textPaint.setColor(Color.WHITE);
            canvas.drawText("nasiono", cx, groundY + 35, textPaint);
        }

        private void drawSprout(Canvas canvas, float cx, float groundY) {
            paint.setStrokeWidth(8);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(leafDarkColor);
            canvas.drawLine(cx, groundY, cx, groundY - 70, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(leafColor);
            canvas.drawOval(cx - 55, groundY - 80, cx + 5, groundY - 35, paint);
            canvas.drawOval(cx - 5, groundY - 82, cx + 55, groundY - 37, paint);
        }

        private void drawSmallTree(Canvas canvas, float cx, float groundY) {
            paint.setColor(trunkColor);
            canvas.drawRect(cx - 14, groundY - 120, cx + 14, groundY, paint);

            paint.setColor(leafDarkColor);
            canvas.drawCircle(cx, groundY - 150, 65, paint);

            paint.setColor(leafColor);
            canvas.drawCircle(cx - 35, groundY - 140, 48, paint);
            canvas.drawCircle(cx + 35, groundY - 140, 48, paint);
            canvas.drawCircle(cx, groundY - 190, 52, paint);
        }

        private void drawBigTree(Canvas canvas, float cx, float groundY) {
            paint.setColor(trunkColor);
            canvas.drawRect(cx - 20, groundY - 160, cx + 20, groundY, paint);

            paint.setStrokeWidth(12);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(trunkColor);
            canvas.drawLine(cx, groundY - 110, cx - 60, groundY - 190, paint);
            canvas.drawLine(cx, groundY - 105, cx + 65, groundY - 185, paint);

            paint.setStyle(Paint.Style.FILL);

            RadialGradient glow = new RadialGradient(
                    cx,
                    groundY - 205,
                    150,
                    Color.argb(100, 129, 199, 132),
                    Color.argb(0, 129, 199, 132),
                    Shader.TileMode.CLAMP
            );
            paint.setShader(glow);
            canvas.drawCircle(cx, groundY - 205, 150, paint);
            paint.setShader(null);

            paint.setColor(leafDarkColor);
            canvas.drawCircle(cx, groundY - 215, 95, paint);

            paint.setColor(leafColor);
            canvas.drawCircle(cx - 70, groundY - 190, 70, paint);
            canvas.drawCircle(cx + 70, groundY - 190, 70, paint);
            canvas.drawCircle(cx, groundY - 265, 75, paint);
            canvas.drawCircle(cx, groundY - 175, 70, paint);

            if (growth >= 90) {
                paint.setColor(Color.parseColor("#FF80AB"));
                canvas.drawCircle(cx - 55, groundY - 230, 8, paint);
                canvas.drawCircle(cx + 45, groundY - 255, 8, paint);
                canvas.drawCircle(cx + 70, groundY - 175, 8, paint);
                canvas.drawCircle(cx - 20, groundY - 285, 8, paint);
            }
        }

        private void drawItems(Canvas canvas) {
            for (GardenItem item : items) {
                RadialGradient glow = new RadialGradient(
                        item.x,
                        item.y,
                        item.radius * 1.7f,
                        Color.argb(90, Color.red(item.color), Color.green(item.color), Color.blue(item.color)),
                        Color.argb(0, Color.red(item.color), Color.green(item.color), Color.blue(item.color)),
                        Shader.TileMode.CLAMP
                );

                paint.setShader(glow);
                canvas.drawCircle(item.x, item.y, item.radius * 1.7f, paint);
                paint.setShader(null);

                paint.setColor(Color.argb(80, 255, 255, 255));
                canvas.drawCircle(item.x, item.y, item.radius, paint);

                paint.setColor(Color.argb(150, Color.red(item.color), Color.green(item.color), Color.blue(item.color)));
                canvas.drawCircle(item.x, item.y, item.radius * 0.78f, paint);

                textPaint.setTextSize(item.type == 3 ? 30 : 34);
                textPaint.setColor(Color.WHITE);

                Paint.FontMetrics fm = textPaint.getFontMetrics();
                float textY = item.y - (fm.ascent + fm.descent) / 2;
                canvas.drawText(item.symbol, item.x, textY, textPaint);
            }
        }

        private void drawFloatingTexts(Canvas canvas) {
            for (FloatingText ft : floatingTexts) {
                textPaint.setTextSize(24);
                textPaint.setColor(Color.argb(ft.alpha, 255, 255, 255));
                canvas.drawText(ft.text, ft.x, ft.y, textPaint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_DOWN) {
                return true;
            }

            float touchX = event.getX();
            float touchY = event.getY();

            for (int i = items.size() - 1; i >= 0; i--) {
                GardenItem item = items.get(i);

                float dx = touchX - item.x;
                float dy = touchY - item.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance <= item.radius + 16) {
                    handleItemTouch(i, item);
                    return true;
                }
            }

            care = clamp(care + 1);
            growth = clamp(growth + 1);
            floatingTexts.add(new FloatingText(touchX, touchY, "+ opieka"));
            notifyChange("Dotknąłeś ziemi i zadbałeś o roślinę. Zbieraj też wodę i słońce.");
            return true;
        }

        private void handleItemTouch(int index, GardenItem item) {
            if (item.type == 0) {
                water = clamp(water + 12);
                growth = clamp(growth + 5);
                floatingTexts.add(new FloatingText(item.x, item.y, "+ woda"));
                notifyChange("Drzewo zostało podlane. Zbieraj dalej, żeby szybciej rosło.");
            } else if (item.type == 1) {
                sun = clamp(sun + 12);
                growth = clamp(growth + 5);
                floatingTexts.add(new FloatingText(item.x, item.y, "+ słońce"));
                notifyChange("Drzewo dostało światło. To pomaga mu rosnąć.");
            } else if (item.type == 2) {
                care = clamp(care + 12);
                growth = clamp(growth + 6);
                floatingTexts.add(new FloatingText(item.x, item.y, "+ opieka"));
                notifyChange("Dobra opieka. Drzewo robi się coraz większe.");
            } else {
                growth = Math.max(0, growth - 6);
                care = Math.max(0, care - 5);
                floatingTexts.add(new FloatingText(item.x, item.y, "- chwast"));
                notifyChange("To był chwast. Usuń go spokojnie i wróć do podlewania drzewa.");
            }

            items.remove(index);
            addRandomItem();

            if (growth >= 100) {
                notifyChange("Drzewo w pełni urosło. Udało się stworzyć spokojny ogród.");
            }
        }

        private int clamp(int value) {
            return Math.max(0, Math.min(100, value));
        }

        private void notifyChange(String hint) {
            if (listener != null) {
                listener.onTreeChanged(growth, water, sun, care, hint);
            }
        }
    }
}