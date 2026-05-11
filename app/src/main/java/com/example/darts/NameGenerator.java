package com.example.darts;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NameGenerator {
    public interface NameCallback {
        void onNameGenerated(String name);
    }

    public static void getRandomName(NameCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                URL url = new URL("https://randomuser.me/api/?inc=login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);

                JSONObject json = new JSONObject(result.toString());
                String username = json.getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("login")
                        .getString("username");

                handler.post(() -> callback.onNameGenerated(username));
            } catch (Exception e) {
                handler.post(() -> callback.onNameGenerated("User" + (int)(Math.random() * 10000)));
            }
        });
    }
}