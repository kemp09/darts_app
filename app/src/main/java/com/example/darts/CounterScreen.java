package com.example.darts;

import android.content.Intent;
import android.graphics.Color;
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

public class CounterScreen extends AppCompatActivity {

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

        // intent adatok, atadott adatok
        Intent intent = getIntent();
        int maxScore = intent.getIntExtra("gameMode", 501);
        String endCondition = intent.getStringExtra("endCondition");
        players = intent.getStringArrayExtra("players");

        // pontszamok feltoltese alap ertekkel
        for (String player : players) {
            scores.put(player, maxScore);
        }

        // player es score ui elemek
        tvPlayer = findViewById(R.id.tvPlayer);
        tvScore = findViewById(R.id.tvScore);
        tvPlayer.setText(players[currentPlayerIndex]);
        tvScore.setText(String.valueOf(maxScore));

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
        });

        // enter es next gombok
        Button enterBtn = findViewById(R.id.enterBtn);
        Button nextBtn = findViewById(R.id.nextBtn);

        enterBtn.setOnClickListener(v -> {
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

            boolean isDoubleOut = endCondition.equals("doubleOut");

            // utolso dobas tarolasa
            String lastThrow = "";
            for (int i = throwsArr.length - 1; i >= 0; i--) {
                String val = throwsArr[i].getText().toString();
                if (!val.equals("0")) {
                    lastThrow = val;
                    break;
                }
            }

            // double out logika
            boolean validFinish = true;
            if (newScore == 0 && isDoubleOut) {
                // ha nem dupla az utolso, akkor nem vlaid
                if (!lastThrow.startsWith("D") && !lastThrow.equals("DB")) {
                    validFinish = false;
                }
            }

            if (newScore < 0) validFinish = false;

            if (!validFinish) {
                // tulment vagy nem valid finish -> pontok nem valtoznak
                tvScore.setText(String.valueOf(currentScore));
            } else {
                scores.put(currentPlayer, newScore);
                tvScore.setText(String.valueOf(newScore));
            }

            // enter es next vissza
            enterBtn.setEnabled(false);
            nextBtn.setEnabled(true);
        });

        nextBtn.setOnClickListener(v -> {
            // kovetkezo jatekos
            currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
            nextBtn.setEnabled(false);
            enterBtn.setEnabled(true);
            resetThrows();
            tvPlayer.setText(players[currentPlayerIndex]);
            tvScore.setText(String.valueOf(scores.get(players[currentPlayerIndex])));
        });

        // alapertelmezett: elso throw kivalasztva + 0zas
        resetThrows();
    }

    void resetThrows() {
        throwsLabel = new String[]{"0", "0", "0"};
        updateTextViews();
        selectedThrow = 0;
        highlightSelected(0);
        currentInput = "";
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
}