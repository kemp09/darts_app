package com.example.darts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

public class CounterScreen extends AppCompatActivity implements SensorEventListener {

    int selectedThrow = -1;
    String[] throwsLabel = {"0", "0", "0"};
    boolean doubleActive = false;
    boolean tripleActive = false;
    TextView throw1, throw2, throw3;
    TextView[] throwsArr = new TextView[3];
    String currentInput = "";
    int currentPlayerIndex = 0;
    Map<String, Integer> scores = new LinkedHashMap<>();
    String[] players;
    TextView tvScore;
    TextView tvPlayer;
    Button enterBtn;
    Button nextBtn;
    Button undoBtn;
    String endCondition;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;

    class GameState {
        int playerIndex;
        Map<String, Integer> scores;
        GameState(int pIdx, Map<String, Integer> s) {
            playerIndex = pIdx;
            scores = new LinkedHashMap<>(s);
        }
    }

    Stack<GameState> history = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_counter_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // intent adatok, atadott adatok
        Intent intent = getIntent();
        int maxScore = intent.getIntExtra("gameMode", 501);
        endCondition = intent.getStringExtra("endCondition");
        players = intent.getStringArrayExtra("players");

        // pontszamok feltoltese alap ertekkel
        for (String player : players) {
            scores.put(player, maxScore);
        }

        // player es score ui elemek
        tvPlayer = findViewById(R.id.tvPlayer);
        tvScore = findViewById(R.id.tvScore);

        // throw textviewk
        throw1 = findViewById(R.id.throw1);
        throw2 = findViewById(R.id.throw2);
        throw3 = findViewById(R.id.throw3);
        throwsArr[0] = throw1;
        throwsArr[1] = throw2;
        throwsArr[2] = throw3;

        // toggle gombok
        ToggleButton btnDouble = findViewById(R.id.toggleDouble);
        ToggleButton btnTriple = findViewById(R.id.toggleTriple);

        // throw listnererek
        for (int i = 0; i < throwsArr.length; i++) {
            final int index = i;
            throwsArr[i].setOnClickListener(v -> {
                selectedThrow = index;
                highlightSelected(index);
                btnTriple.setChecked(false);
                btnDouble.setChecked(false);
                doubleActive = false;
                tripleActive = false;
            });
        }

        // szamgombok
        Button[] numBtns = new Button[10];
        int[] btnIds = {R.id.input0, R.id.input1, R.id.input2, R.id.input3, R.id.input4,
                R.id.input5, R.id.input6, R.id.input7, R.id.input8, R.id.input9};

        for (int i = 0; i < btnIds.length; i++) {
            numBtns[i] = findViewById(btnIds[i]);
            final String num = String.valueOf(i);
            numBtns[i].setOnClickListener(v -> appendInput(num));
        }

        // egyeb gombok (C es B)
        Button btnC = findViewById(R.id.inputC);
        Button btnB = findViewById(R.id.inputB);

        btnDouble.setOnClickListener(v -> {
            doubleActive = !doubleActive;
            btnTriple.setChecked(false);
            tripleActive = false;
            if (selectedThrow != -1) updateThrow();
        });

        btnTriple.setOnClickListener(v -> {
            tripleActive = !tripleActive;
            btnDouble.setChecked(false);
            doubleActive = false;
            if (selectedThrow != -1) updateThrow();
        });

        btnC.setOnClickListener(v -> {
            currentInput = "";
            if (selectedThrow != -1) {
                throwsLabel[selectedThrow] = "0";
                updateTextViews();
            }
        });

        btnB.setOnClickListener(v -> {
            if (selectedThrow == -1) return;
            String label = doubleActive ? "DB" : "B";
            throwsLabel[selectedThrow] = label;
            currentInput = "";
            updateTextViews();
            autoAdvance();
        });

        // enter es next gombok
        enterBtn = findViewById(R.id.enterBtn);
        nextBtn = findViewById(R.id.nextBtn);
        undoBtn = findViewById(R.id.undoBtn);

        enterBtn.setOnClickListener(v -> {
            history.push(new GameState(currentPlayerIndex, scores));

            // dobott pontok osszeszamolasa
            int sumOfThrows = 0;
            for (TextView t : throwsArr) {
                String val = t.getText().toString();
                if (val.equals("0")) continue;

                if (val.contains("B")) {
                    sumOfThrows += val.equals("DB") ? 50 : 25;
                } else if (val.startsWith("T")) {
                    sumOfThrows += Integer.parseInt(val.replace("T", "")) * 3;
                } else if (val.startsWith("D")) {
                    sumOfThrows += Integer.parseInt(val.replace("D", "")) * 2;
                } else {
                    sumOfThrows += Integer.parseInt(val);
                }
            }

            // pontok ellenorzese
            String currentPlayer = players[currentPlayerIndex];
            int currentScore = scores.get(currentPlayer);
            int newScore = currentScore - sumOfThrows;

            boolean isDoubleOut = endCondition.equals("Double out");

            // utolso dobas tarolasa
            String lastThrow = "";
            for (int i = throwsArr.length - 1; i >= 0; i--) {
                String val = throwsArr[i].getText().toString();
                if (!val.equals("0")) {
                    lastThrow = val;
                    break;
                }
            }

            boolean bust = newScore < 0;
            boolean isLastThrowDouble = lastThrow.startsWith("D") || lastThrow.equals("DB");
            boolean validFinish = !bust && (!isDoubleOut || (newScore != 1 && (newScore != 0 || isLastThrowDouble)));

            if (!validFinish) {
                // tulment vagy nem valid finish -> pontok nem valtoznak
                tvScore.setText(String.valueOf(currentScore));
            } else {
                scores.put(currentPlayer, newScore);
                tvScore.setText(String.valueOf(newScore));

                if (newScore == 0) {
                    showWinnerDialog(currentPlayer);
                    return;
                }
            }

            // enter es next vissza
            enterBtn.setEnabled(false);
            nextBtn.setEnabled(true);
            undoBtn.setEnabled(true);
            updateUI();
        });

        nextBtn.setOnClickListener(v -> {
            // kovetkezo jatekos
            history.push(new GameState(currentPlayerIndex, scores));
            currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
            nextBtn.setEnabled(false);
            enterBtn.setEnabled(true);
            undoBtn.setEnabled(true);
            resetThrows();
            updateUI();
        });

        undoBtn.setOnClickListener(v -> performUndo());

        // alapertelmezett: elso throw kivalasztva + 0zas
        resetThrows();
        updateUI();
        undoBtn.setEnabled(false);
    }

    private void performUndo() {
        if (!history.isEmpty()) {
            GameState prevState = history.pop();
            currentPlayerIndex = prevState.playerIndex;
            scores = prevState.scores;
            resetThrows();
            updateUI();
            enterBtn.setEnabled(true);
            nextBtn.setEnabled(false);
            if (history.isEmpty()) {
                undoBtn.setEnabled(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > 1.5f) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > 1000) {
                    lastShakeTime = currentTime;
                    performUndo();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void updateUI() {
        String p = players[currentPlayerIndex];
        tvPlayer.setText(p);
        int sc = scores.get(p);
        tvScore.setText(String.valueOf(sc));
    }

    void resetThrows() {
        throwsLabel = new String[]{"0", "0", "0"};
        updateTextViews();
        selectedThrow = 0;
        highlightSelected(0);
        currentInput = "";
        ToggleButton btnDouble = findViewById(R.id.toggleDouble);
        ToggleButton btnTriple = findViewById(R.id.toggleTriple);
        btnDouble.setChecked(false);
        btnTriple.setChecked(false);
        doubleActive = false;
        tripleActive = false;
    }

    void highlightSelected(int index) {
        for (int i = 0; i < throwsArr.length; i++) {
            throwsArr[i].setBackgroundColor(i == index ? Color.parseColor("#8B0000") : Color.TRANSPARENT);
        }
        currentInput = "";
    }

    void appendInput(String num) {
        if (selectedThrow == -1) return;
        String newInput = currentInput + num;
        if (Integer.parseInt(newInput) > 20) return;
        currentInput = newInput;
        updateThrow();

        if (Integer.parseInt(newInput) >= 3 || currentInput.length() == 2) {
            autoAdvance();
        }
    }

    void updateThrow() {
        if (selectedThrow == -1 || currentInput.isEmpty()) return;
        String label;
        if (tripleActive) label = "T" + currentInput;
        else if (doubleActive) label = "D" + currentInput;
        else label = currentInput;
        throwsLabel[selectedThrow] = label;
        updateTextViews();
    }

    void updateTextViews() {
        throw1.setText(throwsLabel[0]);
        throw2.setText(throwsLabel[1]);
        throw3.setText(throwsLabel[2]);
    }

    void autoAdvance() {
        if (selectedThrow < 2) {
            selectedThrow++;
            highlightSelected(selectedThrow);
            ToggleButton btnDouble = findViewById(R.id.toggleDouble);
            ToggleButton btnTriple = findViewById(R.id.toggleTriple);
            btnDouble.setChecked(false);
            btnTriple.setChecked(false);
            doubleActive = false;
            tripleActive = false;
        }
    }

    private void showWinnerDialog(String winner) {
        new android.app.AlertDialog.Builder(this, android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setTitle("Game Over!")
                .setMessage("Winner: " + winner)
                .setCancelable(false)
                .setPositiveButton("Main Menu", (dialog, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Play Again", (dialog, which) -> {
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                })
                .show();
    }
}