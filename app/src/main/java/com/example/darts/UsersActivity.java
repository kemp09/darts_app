package com.example.darts;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class UsersActivity extends AppCompatActivity {

    private List<String> users;
    private LinearLayout llUserList;
    private EditText etUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        etUserName = findViewById(R.id.etUserName);
        llUserList = findViewById(R.id.llUserList);
        Button btnAdd = findViewById(R.id.btnAddUser);
        Button btnRandom = findViewById(R.id.btnRandomUser);

        users = UserStorage.getUsers(this);
        refreshList();

        btnAdd.setOnClickListener(v -> {
            String name = etUserName.getText().toString().trim();
            if (name.isEmpty()) return;
            users.add(name);
            UserStorage.saveUsers(this, users);
            etUserName.setText("");
            refreshList();
        });

        btnRandom.setOnClickListener(v -> {
            btnRandom.setEnabled(false);
            NameGenerator.getRandomName(name -> {
                etUserName.setText(name);
                btnRandom.setEnabled(true);
            });
        });
    }

    private void refreshList() {
        llUserList.removeAllViews();
        for (String name : users) {
            LinearLayout row = new LinearLayout(this);

            TextView tv = new TextView(this);
            tv.setText(name);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            Button btnDelete = new Button(this);
            btnDelete.setText("X");
            btnDelete.setOnClickListener(v -> {
                users.remove(name);
                UserStorage.saveUsers(this, users);
                refreshList();
            });

            row.addView(tv);
            row.addView(btnDelete);
            llUserList.addView(row);
        }
    }
}