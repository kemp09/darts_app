package com.example.darts;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.*;

public class NewGameActivity extends AppCompatActivity {

    private int guestCount = 1;

    private List<Spinner> activeSpinners = new ArrayList<>();
    private List<String> savedUsers;
    private LinearLayout playersContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        savedUsers = UserStorage.getUsers(this);
        playersContainer = findViewById(R.id.playersContainer);
        Button btnAddPlayer = findViewById(R.id.btnAddPlayer);

        addPlayerField();
        addPlayerField();

        btnAddPlayer.setOnClickListener(v -> {
            if (activeSpinners.size() < 8) {
                addPlayerField();
            }
        });
    }

    private void setupSpinner(Spinner spinner, List<String> savedUsers) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("Select Player");

        for (String user : savedUsers) {
            adapter.add(user);
        }

        adapter.add("Guest");
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == adapter.getCount() - 1) {
                    showGuestDialog(spinner, adapter);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void addPlayerField() {
        int playerNumber = activeSpinners.size() + 1;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView tv = new TextView(this);
        tv.setText("P" + playerNumber + ":");
        tv.setTextSize(18);
        tv.setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT));

        Spinner spinner = new Spinner(this);
        spinner.setPopupBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#0d0d1a")));
        spinner.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        setupSpinner(spinner, savedUsers);

        row.addView(tv);
        row.addView(spinner);
        playersContainer.addView(row);
        activeSpinners.add(spinner);
    }

    private void showGuestDialog(Spinner spinner, ArrayAdapter<String> adapter) {
        EditText input = new EditText(this);
        input.setText("Guest" + guestCount);
        input.setSingleLine(true);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Guest name")
                .setView(input)
                .setPositiveButton("OK", (d, which) -> {
                    String name = input.getText().toString();
                    if (name.isEmpty()) name = "Guest" + guestCount;
                    if (adapter.getPosition(name) == -1) {
                        adapter.insert(name, adapter.getCount() - 1);
                        guestCount++;
                    }
                    spinner.setSelection(adapter.getPosition(name));
                })
                .setNeutralButton("Random", null)
                .setNegativeButton("Cancel", (d, which) -> spinner.setSelection(0))
                .create();

        dialog.setOnShowListener(d -> {
            Button btnRandom = dialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL);
            btnRandom.setOnClickListener(v -> {
                btnRandom.setEnabled(false);
                NameGenerator.getRandomName(name -> {
                    input.setText(name);
                    btnRandom.setEnabled(true);
                });
            });
        });
        dialog.show();
    }


    public void onBackClick(View view) {
        finish();
    }

    public void onStartGameClick(View view) {
        RadioButton mode301 = findViewById(R.id.is301);
        RadioButton isDoubleOut = findViewById(R.id.isDoubleOut);

        ArrayList<String> players = new ArrayList<>();
        for (Spinner spinner : activeSpinners) {
            String selected = spinner.getSelectedItem().toString();
            if (!selected.equals("Select Player")) {
                players.add(selected);
            }
        }

        if (!players.isEmpty()) {
            Intent intent = new Intent(this, CounterScreen.class);
            intent.putExtra("gameMode", mode301.isChecked() ? 301 : 501);
            intent.putExtra("endCondition", isDoubleOut.isChecked() ? "Double Out" : "Any out");
            intent.putExtra("players", players.toArray(new String[0]));
            startActivity(intent);
        }
    }
}