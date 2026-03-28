package com.example.darts;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class UserStorage {
    private static final String PREFS_NAME = "darts_prefs";
    private static final String KEY_USERS = "users";

    public static List<String> getUsers(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_USERS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            List<String> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) list.add(arr.getString(i));
            return list;
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    public static void saveUsers(Context ctx, List<String> users) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USERS, new JSONArray(users).toString()).apply();
    }
}