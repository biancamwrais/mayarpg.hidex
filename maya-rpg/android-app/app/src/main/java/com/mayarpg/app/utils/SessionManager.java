package com.mayarpg.app.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SessionManager {

    private static final String PREF = "maya_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_PACIENTE_ID = "paciente_id";
    private static final String KEY_DARK_MODE = "dark_mode";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context ctx) {
        if (instance == null) instance = new SessionManager(ctx);
        return instance;
    }

    public void salvarLogin(String token, int userId, String nome, String email, Integer pacienteId) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString(KEY_TOKEN, token);
        e.putInt(KEY_USER_ID, userId);
        e.putString(KEY_USER_NAME, nome);
        e.putString(KEY_USER_EMAIL, email);
        if (pacienteId != null) e.putInt(KEY_PACIENTE_ID, pacienteId);
        e.apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public int getUserId() { return prefs.getInt(KEY_USER_ID, -1); }
    public String getUserName() { return prefs.getString(KEY_USER_NAME, ""); }
    public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, ""); }
    public int getPacienteId() { return prefs.getInt(KEY_PACIENTE_ID, -1); }

    public boolean isLogado() { return getToken() != null; }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public boolean isDarkMode() { return prefs.getBoolean(KEY_DARK_MODE, false); }
    public void setDarkMode(boolean dark) { prefs.edit().putBoolean(KEY_DARK_MODE, dark).apply(); }
}
