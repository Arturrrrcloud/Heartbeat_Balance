package com.example.heartrateapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
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

        // NOWOŚĆ: Dopasowanie do pasków systemowych telefonu (przeciwdziała obcinaniu trawy)
        main.setFitsSystemWindows(true);

        // --- PASEK GÓRNY (Nawigacja i Tytuł) ---
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(fullWidthWrap());

        Button btnBack = new Button(this);
        btnBack.setText("←");
        btnBack.setTextColor(calmAccent);
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setTextSize(26);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setPadding(0, 0, dp(10), 0);
        btnBack.setOnClickListener(v -> finish());
        topBar.addView(btnBack);

        txtTitle = new TextView(this);
        txtTitle.setText("Ogród Zen");
        txtTitle.setTextColor(calmAccent);
        txtTitle.setTextSize(24);
        txtTitle.setTypeface(null, Typeface.BOLD);
        txtTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        topBar.addView(txtTitle, titleParams);

        Button btnReset = new Button(this);
        btnReset.setText("⟳");
        btnReset.setTextColor(calmAccent);
        btnReset.setBackgroundColor(Color.TRANSPARENT);
        btnReset.setTextSize(26);
        btnReset.setTypeface(null, Typeface.BOLD);
        btnReset.setPadding(dp(10), 0, 0, 0);
        btnReset.setOnClickListener(v -> treeGardenView.resetGame());
        topBar.addView(btnReset);

        main.addView(topBar);
        // --------------------------------------------

        txtStats = new TextView(this);
        txtStats.setText("Zasoby: 0 / 2");
        txtStats.setTextColor(textPrimary);
        txtStats.setTextSize(16);
        txtStats.setGravity(Gravity.CENTER);
        txtStats.setPadding(0, dp(12), 0, dp(4));
        main.addView(txtStats, fullWidthWrap());

        txtHint = new TextView(this);
        txtHint.setText("Przeciągnij wodę i słońce do nasiona, aby wykiełkowało.");
        txtHint.setTextColor(textHint);
        txtHint.setTextSize(14);
        txtHint.setGravity(Gravity.CENTER);
        txtHint.setPadding(0, 0, 0, dp(12));
        txtHint.setLines(2);
        main.addView(txtHint, fullWidthWrap());

        treeGardenView = new TreeGardenView(this);
        treeGardenView.setGameListener((resources, hint) -> {
            if (treeGardenView.currentPhase == TreeGardenView.PHASE_SEED) {
                txtStats.setText("Zasoby: " + resources + " / 2");
            } else {
                txtStats.setText("Zasoby: " + resources);
            }
            txtHint.setText(hint);
        });

        LinearLayout.LayoutParams gameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1
        );
        gameParams.setMargins(0, dp(8), 0, dp(4));
        main.addView(treeGardenView, gameParams);

        setContentView(main);
    }

    private LinearLayout.LayoutParams fullWidthWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    // =================================================================
    // GŁÓWNY WIDOK GRY
    // =================================================================
    public static class TreeGardenView extends View {

        interface GameListener {
            void onStateChanged(int resources, String hint);
        }

        public static final int PHASE_SEED = 0;
        public static final int PHASE_TRUNK = 1;

        private static class DraggableOrb {
            float x, y;
            float vx, vy;
            float targetVx, targetVy;
            float radius;
            float slowDownY;
            int type;
            boolean isDragged;
            int fruitSubType;

            DraggableOrb(float x, float y, int type) {
                this.x = x; this.y = y; this.type = type;
                this.radius = 50;
                this.isDragged = false;

                Random rnd = new Random();
                this.targetVx = (rnd.nextFloat() * 1.0f - 0.5f);
                this.targetVy = (rnd.nextFloat() * 0.4f + 0.3f);

                this.vx = 0f;
                this.vy = 2.5f + rnd.nextFloat() * 2.0f;

                this.slowDownY = 50f + rnd.nextFloat() * 300f;

                if (type == 4) {
                    this.fruitSubType = rnd.nextInt(3);
                } else {
                    this.fruitSubType = -1;
                }
            }
        }

        private static class TreeNode {
            float startX, startY, endX, endY, thickness;
            float angle;
            float length;

            TreeNode parent;
            TreeNode[] slots = new TreeNode[3];

            float relAngle;
            float attachT;
            float relLengthMulti = 0.65f;
            float relThickMulti = 0.70f;

            TreeNode() {}

            TreeNode(TreeNode parent, int slotIndex) {
                this.parent = parent;

                if (slotIndex == 0)      { attachT = 1.0f; relAngle = 0f; }
                else if (slotIndex == 1) { attachT = 0.7f; relAngle = (float)Math.toRadians(-60); }
                else if (slotIndex == 2) { attachT = 0.7f; relAngle = (float)Math.toRadians(60); }
            }

            void updateAbsolute() {
                if (parent != null) {
                    startX = parent.startX + (parent.endX - parent.startX) * attachT;
                    startY = parent.startY + (parent.endY - parent.startY) * attachT;
                    angle = parent.angle + relAngle;
                    length = parent.length * relLengthMulti;
                    thickness = parent.thickness * relThickMulti;

                    endX = startX + (float)Math.cos(angle) * length;
                    endY = startY + (float)Math.sin(angle) * length;
                }
                for (int i=0; i<3; i++) {
                    if (slots[i] != null) slots[i].updateAbsolute();
                }
            }

            float getSlotStartX(int slotIndex) {
                float t = (slotIndex == 0) ? 1.0f : 0.7f;
                return startX + (endX - startX) * t;
            }
            float getSlotStartY(int slotIndex) {
                float t = (slotIndex == 0) ? 1.0f : 0.7f;
                return startY + (endY - startY) * t;
            }
        }

        private static class Fruit {
            float factorX, factorY;
            int subType;

            Fruit(float fx, float fy, int subType) {
                this.factorX = fx;
                this.factorY = fy;
                this.subType = subType;
            }
        }

        private static class Leaf {
            TreeNode node;
            float t;
            float offsetX, offsetY;
            List<Fruit> fruits = new ArrayList<>();

            Leaf(TreeNode node, float t, float offsetX, float offsetY) {
                this.node = node;
                this.t = t;
                this.offsetX = offsetX;
                this.offsetY = offsetY;
            }
        }

        private TreeNode trunkNode;
        private final List<TreeNode> allNodes = new ArrayList<>();
        private float trunkHeight = 60f;
        private float trunkWidth = 24f;

        private final List<Leaf> leaves = new ArrayList<>();
        private final List<DraggableOrb> orbs = new ArrayList<>();
        private DraggableOrb currentlyDraggedOrb = null;

        private final Random random = new Random();
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private GameListener listener;
        public int currentPhase = PHASE_SEED;
        private int collectedResources = 0;

        private float rootX = 0;
        private float rootY = 0;
        private final float rootRadius = 60;

        private final int colorWater = Color.parseColor("#4FC3F7");
        private final int colorSun = Color.parseColor("#FFD54F");
        private final int colorSeed = Color.parseColor("#A1887F");
        private final int colorTrunk = Color.parseColor("#8D5A2B");
        private final int colorLeaf = Color.parseColor("#66BB6A");
        private final int colorFruitRed = Color.parseColor("#E53935");
        private final int colorFruitOrange = Color.parseColor("#FB8C00");
        private final int colorFruitPurple = Color.parseColor("#8E24AA");

        public TreeGardenView(Context context) {
            super(context);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        public void setGameListener(GameListener listener) {
            this.listener = listener;
            resetGame();
        }

        public void resetGame() {
            currentPhase = PHASE_SEED;
            collectedResources = 0;
            trunkHeight = 60f;
            trunkWidth = 24f;
            allNodes.clear();
            trunkNode = null;
            leaves.clear();
            orbs.clear();
            currentlyDraggedOrb = null;
            notifyChange("Przeciągnij wodę lub słońce na nasiono.");
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            rootX = w / 2f;
            rootY = h - 100f;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (trunkNode != null) {
                trunkNode.startX = rootX;
                trunkNode.startY = rootY;
                trunkNode.angle = (float)Math.toRadians(-90);
                trunkNode.length = trunkHeight;
                trunkNode.thickness = trunkWidth;
                trunkNode.endX = trunkNode.startX + (float)Math.cos(trunkNode.angle) * trunkNode.length;
                trunkNode.endY = trunkNode.startY + (float)Math.sin(trunkNode.angle) * trunkNode.length;
                trunkNode.updateAbsolute();
            }

            drawBackground(canvas);
            updatePhysics();
            drawTreeBase(canvas);
            drawLeavesAndFruits(canvas);
            drawOrbs(canvas);
            postInvalidateOnAnimation();
        }

        private void drawBackground(Canvas canvas) {
            LinearGradient gradient = new LinearGradient(0, 0, 0, getHeight(),
                    Color.parseColor("#A2C2E0"), // Góra: Poranny błękit
                    Color.parseColor("#D8C9B9"), // Dół: Ciepły beż
                    Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            paint.setShader(null);

            paint.setColor(Color.parseColor("#2E7D32"));
            canvas.drawRect(0, rootY + 20, getWidth(), getHeight(), paint);

            paint.setColor(Color.parseColor("#1B5E20"));
            canvas.drawOval(rootX - 50, rootY + 10, rootX + 50, rootY + 30, paint);
        }

        private void drawTreeBase(Canvas canvas) {
            if (currentPhase == PHASE_SEED) {
                paint.setColor(colorSeed);
                canvas.drawOval(rootX - 25, rootY - 15, rootX + 25, rootY + 15, paint);

                paint.setColor(Color.argb(40, 255, 255, 255));
                float pulse = 10f * (float) Math.sin(System.currentTimeMillis() / 300.0);
                canvas.drawOval(rootX - 30 - pulse, rootY - 20 - pulse, rootX + 30 + pulse, rootY + 20 + pulse, paint);

            } else if (currentPhase == PHASE_TRUNK) {
                paint.setColor(colorTrunk);
                paint.setStrokeCap(Paint.Cap.ROUND);
                for (TreeNode n : allNodes) {
                    paint.setStrokeWidth(n.thickness);
                    canvas.drawLine(n.startX, n.startY, n.endX, n.endY, paint);
                }
            }
        }

        private void drawLeavesAndFruits(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);

            for (Leaf leaf : leaves) {
                float lx = leaf.node.startX + (leaf.node.endX - leaf.node.startX) * leaf.t + leaf.offsetX;
                float ly = leaf.node.startY + (leaf.node.endY - leaf.node.startY) * leaf.t + leaf.offsetY;

                float currentRadius = 40f;

                paint.setColor(colorLeaf);
                canvas.drawCircle(lx, ly, currentRadius, paint);

                if (!leaf.fruits.isEmpty()) {
                    for (Fruit f : leaf.fruits) {
                        float fx = lx + (f.factorX * currentRadius);
                        float fy = ly + (f.factorY * currentRadius);

                        if (f.subType == 0) paint.setColor(colorFruitRed);
                        else if (f.subType == 1) paint.setColor(colorFruitOrange);
                        else paint.setColor(colorFruitPurple);

                        float fruitRadius = 10f;
                        canvas.drawCircle(fx, fy, fruitRadius, paint);
                    }
                }
            }
        }

        private void drawOrbs(Canvas canvas) {
            for (DraggableOrb orb : orbs) {
                int color;
                String symbol;

                if (orb.type == 0) { color = colorWater; symbol = "💧"; }
                else if (orb.type == 1) { color = colorSun; symbol = "☀"; }
                else if (orb.type == 2) { color = colorTrunk; symbol = "Y"; }
                else if (orb.type == 3) { color = colorLeaf; symbol = "🍃"; }
                else {
                    if (orb.fruitSubType == 0) { color = colorFruitRed; symbol = "🍎"; }
                    else if (orb.fruitSubType == 1) { color = colorFruitOrange; symbol = "🍊"; }
                    else { color = colorFruitPurple; symbol = "🍇"; }
                }

                RadialGradient glow = new RadialGradient(orb.x, orb.y, orb.radius * 1.5f,
                        Color.argb(100, Color.red(color), Color.green(color), Color.blue(color)),
                        Color.TRANSPARENT, Shader.TileMode.CLAMP);
                paint.setShader(glow);
                canvas.drawCircle(orb.x, orb.y, orb.radius * 1.5f, paint);
                paint.setShader(null);

                paint.setColor(color);
                if (orb.isDragged) paint.setAlpha(180);
                canvas.drawCircle(orb.x, orb.y, orb.radius, paint);
                paint.setAlpha(255);

                textPaint.setTextSize(40);
                textPaint.setColor(Color.WHITE);
                Paint.FontMetrics fm = textPaint.getFontMetrics();
                canvas.drawText(symbol, orb.x, orb.y - (fm.ascent + fm.descent) / 2, textPaint);
            }
        }

        private void updatePhysics() {
            if (orbs.size() < 7) {
                if (random.nextInt(100) < 5) {
                    int type;
                    if (currentPhase == PHASE_SEED) {
                        type = random.nextBoolean() ? 0 : 1;
                    } else {
                        int r = random.nextInt(100);
                        if (r < 17) type = 0;
                        else if (r < 34) type = 1;
                        else if (r < 70) type = 2;
                        else if (r < 91) type = 3;
                        else type = 4;
                    }
                    float startX = 60 + random.nextInt(getWidth() - 120);
                    orbs.add(new DraggableOrb(startX, -150, type));
                }
            }

            for (int i = orbs.size() - 1; i >= 0; i--) {
                DraggableOrb orb = orbs.get(i);
                if (!orb.isDragged) {

                    if (orb.y > orb.slowDownY) {
                        orb.vx += (orb.targetVx - orb.vx) * 0.015f;
                        orb.vy += (orb.targetVy - orb.vy) * 0.015f;
                    }

                    orb.x += orb.vx;
                    orb.y += orb.vy;

                    if (orb.y > getHeight() + orb.radius) {
                        orbs.remove(i);
                        continue;
                    }

                    if (orb.x < orb.radius) {
                        orb.x = orb.radius;
                        orb.vx = 0.5f + random.nextFloat() * 1.5f;
                        orb.vy = (random.nextFloat() * 2f - 1f);
                    } else if (orb.x > getWidth() - orb.radius) {
                        orb.x = getWidth() - orb.radius;
                        orb.vx = -0.5f - random.nextFloat() * 1.5f;
                        orb.vy = (random.nextFloat() * 2f - 1f);
                    }
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float tx = event.getX();
            float ty = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for (int i = orbs.size() - 1; i >= 0; i--) {
                        DraggableOrb orb = orbs.get(i);
                        float dx = tx - orb.x;
                        float dy = ty - orb.y;
                        if (dx * dx + dy * dy <= orb.radius * orb.radius * 1.5f) {
                            currentlyDraggedOrb = orb;
                            orb.isDragged = true;
                            orbs.remove(i);
                            orbs.add(orb);
                            return true;
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (currentlyDraggedOrb != null) {
                        currentlyDraggedOrb.x = tx;
                        currentlyDraggedOrb.y = ty;
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (currentlyDraggedOrb != null) {
                        boolean orbAbsorbed = false;

                        if (currentlyDraggedOrb.type == 0 || currentlyDraggedOrb.type == 1) {
                            if (isNearTree(currentlyDraggedOrb.x, currentlyDraggedOrb.y)) {
                                orbAbsorbed = true;
                                collectedResources++;

                                if (currentPhase == PHASE_SEED) {
                                    if (collectedResources >= 2) {
                                        currentPhase = PHASE_TRUNK;
                                        trunkNode = new TreeNode();
                                        allNodes.add(trunkNode);
                                        notifyChange("Drzewo rośnie! Układaj gałęzie i doczepiaj do nich liście.");
                                    } else {
                                        notifyChange("Nasiono wchłania zasoby. Zbieraj dalej.");
                                    }
                                } else {
                                    handleGrowth();
                                    notifyChange("Cała roślina naturalnie się powiększa.");
                                }
                            }
                        } else if (currentlyDraggedOrb.type == 2) {
                            if (isNearTree(currentlyDraggedOrb.x, currentlyDraggedOrb.y)) {
                                boolean attached = addFractalBranch(currentlyDraggedOrb.x, currentlyDraggedOrb.y);
                                if (attached) {
                                    orbAbsorbed = true;
                                    notifyChange("Nowa gałąź znalazła swoje miejsce.");
                                } else {
                                    notifyChange("Brak wolnych miejsc w tej okolicy drzewa.");
                                }
                            }
                        } else if (currentlyDraggedOrb.type == 3) {
                            if (currentPhase == PHASE_TRUNK && isNearTree(currentlyDraggedOrb.x, currentlyDraggedOrb.y)) {
                                boolean attached = addLeafAt(currentlyDraggedOrb.x, currentlyDraggedOrb.y);
                                if (attached) {
                                    orbAbsorbed = true;
                                    notifyChange("Liście ubarwiły drzewo.");
                                } else {
                                    notifyChange("Nie możesz sadzić liści tak nisko na głównym pniu.");
                                }
                            } else if (allNodes.size() == 0 && currentPhase == PHASE_SEED) {
                                notifyChange("Najpierw musisz poczekać na wykiełkowanie!");
                            }
                        } else if (currentlyDraggedOrb.type == 4) {
                            if (leaves.size() > 0) {
                                boolean added = addFruitAt(currentlyDraggedOrb.x, currentlyDraggedOrb.y);
                                if (added) {
                                    orbAbsorbed = true;
                                    notifyChange("Owoc dojrzał na liściu.");
                                } else {
                                    notifyChange("Upuść owoc bezpośrednio na zielony liść (max 6 na liść).");
                                }
                            } else {
                                notifyChange("Najpierw musisz posadzić liście!");
                            }
                        }

                        if (orbAbsorbed) {
                            orbs.remove(currentlyDraggedOrb);
                        }

                        currentlyDraggedOrb.isDragged = false;
                        currentlyDraggedOrb = null;
                        return true;
                    }
                    break;
            }
            return true;
        }

        private void handleGrowth() {
            float margin = 200f;
            float growthMulti = 1.0f;

            for (TreeNode n : allNodes) {
                float distTop = n.endY - margin;
                float distLeft = n.endX - 100f;
                float distRight = getWidth() - 100f - n.endX;

                float minD = Math.min(distTop, Math.min(distLeft, distRight));
                if (minD < margin) {
                    float m = Math.max(0f, minD / margin);
                    if (m < growthMulti) growthMulti = m;
                }
            }

            // ZMIANA: Przyspieszony wzrost po zjedzeniu zasobu
            trunkHeight += 20f * growthMulti;
            trunkWidth += 2.5f * growthMulti;
        }

        private boolean isNearTree(float tx, float ty) {
            float dx = tx - rootX;
            float dy = ty - rootY;
            if (dx * dx + dy * dy <= rootRadius * rootRadius * 2) return true;

            if (currentPhase == PHASE_TRUNK) {
                for (TreeNode n : allNodes) {
                    float bdx = tx - n.endX;
                    float bdy = ty - n.endY;
                    if (bdx * bdx + bdy * bdy <= 16000) return true;

                    float sx = tx - n.startX;
                    float sy = ty - n.startY;
                    if (sx * sx + sy * sy <= 16000) return true;
                }
            }
            return false;
        }

        private boolean addFractalBranch(float dropX, float dropY) {
            float minDist = Float.MAX_VALUE;
            TreeNode bestNode = null;
            int bestSlot = -1;

            for (TreeNode node : allNodes) {
                for (int i = 0; i < 3; i++) {
                    if (node.slots[i] == null) {
                        float slotX = node.getSlotStartX(i);
                        float slotY = node.getSlotStartY(i);
                        float dist = (float) Math.hypot(dropX - slotX, dropY - slotY);

                        if (dist < minDist) {
                            minDist = dist;
                            bestNode = node;
                            bestSlot = i;
                        }
                    }
                }
            }

            if (bestNode != null && minDist < 200f) {
                TreeNode newNode = new TreeNode(bestNode, bestSlot);
                bestNode.slots[bestSlot] = newNode;
                allNodes.add(newNode);

                if (trunkNode != null) trunkNode.updateAbsolute();
                return true;
            }
            return false;
        }

        private boolean addLeafAt(float dropX, float dropY) {
            TreeNode bestNode = null;
            float minDist = Float.MAX_VALUE;
            float bestT = 0f;
            float bestAttachX = 0f, bestAttachY = 0f;

            for (TreeNode n : allNodes) {
                float l2 = (n.endX - n.startX)*(n.endX - n.startX) + (n.endY - n.startY)*(n.endY - n.startY);
                float t = 0f;
                if (l2 != 0) {
                    t = ((dropX - n.startX)*(n.endX - n.startX) + (dropY - n.startY)*(n.endY - n.startY)) / l2;
                    t = Math.max(0, Math.min(1, t));
                }
                float attachX = n.startX + t * (n.endX - n.startX);
                float attachY = n.startY + t * (n.endY - n.startY);
                float dist = (float) Math.hypot(dropX - attachX, dropY - attachY);

                if (dist < minDist) {
                    minDist = dist;
                    bestNode = n;
                    bestT = t;
                    bestAttachX = attachX; bestAttachY = attachY;
                }
            }

            if (bestNode != null) {
                if (bestNode == allNodes.get(0) && bestT < 0.5f) {
                    return false;
                }

                float offsetX = (dropX - bestAttachX) * 0.6f;
                float offsetY = (dropY - bestAttachY) * 0.6f;
                leaves.add(new Leaf(bestNode, bestT, offsetX, offsetY));
                return true;
            }
            return false;
        }

        private boolean addFruitAt(float dropX, float dropY) {
            Leaf closestLeaf = null;
            float minDist = Float.MAX_VALUE;
            float leafCurrentX = 0f;
            float leafCurrentY = 0f;

            for (Leaf l : leaves) {
                float lx = l.node.startX + (l.node.endX - l.node.startX) * l.t + l.offsetX;
                float ly = l.node.startY + (l.node.endY - l.node.startY) * l.t + l.offsetY;

                float dist = (float) Math.hypot(dropX - lx, dropY - ly);
                float currentRadius = 40f;

                if (dist < currentRadius * 1.5f && dist < minDist) {
                    minDist = dist;
                    closestLeaf = l;
                    leafCurrentX = lx;
                    leafCurrentY = ly;
                }
            }

            if (closestLeaf != null && closestLeaf.fruits.size() < 6) {
                float maxOffset = 0.6f;
                float dx = dropX - leafCurrentX;
                float dy = dropY - leafCurrentY;
                float currentRadius = 40f;

                float factorX = dx / currentRadius;
                float factorY = dy / currentRadius;

                if (factorX > maxOffset) factorX = maxOffset;
                if (factorX < -maxOffset) factorX = -maxOffset;
                if (factorY > maxOffset) factorY = maxOffset;
                if (factorY < -maxOffset) factorY = -maxOffset;

                closestLeaf.fruits.add(new Fruit(factorX, factorY, currentlyDraggedOrb.fruitSubType));
                return true;
            }
            return false;
        }

        private void notifyChange(String hint) {
            if (listener != null) {
                listener.onStateChanged(collectedResources, hint);
            }
        }
    }
}