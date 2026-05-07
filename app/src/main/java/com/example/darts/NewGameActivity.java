package com.example.darts;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.*;

public class NewGameActivity extends AppCompatActivity {

    private int guestCount = 1;
    private int[] spinnerIds = {
            R.id.spinnerP1, R.id.spinnerP2, R.id.spinnerP3, R.id.spinnerP4,
            R.id.spinnerP5, R.id.spinnerP6, R.id.spinnerP7, R.id.spinnerP8
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_game);

        List<String> savedUsers = UserStorage.getUsers(this);

        for (int id : spinnerIds) {
            setupSpinner(findViewById(id), savedUsers);
        }

    }

    // https://www.geeksforgeeks.org/android/spinner-in-android-with-example/
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

    private void showGuestDialog(Spinner spinner, ArrayAdapter<String> adapter) {
        EditText input = new EditText(this);
        input.setText("Guest" + guestCount);
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("Guest name")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String name = input.getText().toString();
                    if (name.isEmpty()) name = "Guest" + guestCount;
                    if (adapter.getPosition(name) == -1) {
                        adapter.insert(name, adapter.getCount() - 1);
                        guestCount++;
                    }
                    spinner.setSelection(adapter.getPosition(name));

                })

                .setNegativeButton("Cancel", (dialog, which) -> {
                    spinner.setSelection(0);
                })
                .show();
    }


    public void onBackClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onStartGameClick(View view) {
        // get game mode
        RadioButton mode301 = findViewById(R.id.is301);
        RadioButton mode501 = findViewById(R.id.is501);

        // get out condition
        RadioButton isDoubleOut = findViewById(R.id.isDoubleOut);
        RadioButton isAnyOut = findViewById(R.id.isAnyOut);

        // get players to array
        ArrayList<String> players = new ArrayList<>();
        for (int id : spinnerIds) {
            Spinner spinner = findViewById(id);
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