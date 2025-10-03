package com.example.user;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages the user's session, primarily by handling the authentication token.
 */
public class SessionManager {

    private static final String PREF_NAME = "UtsavAppSession";
    private static final String KEY_AUTH_TOKEN = "authToken";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Saves the authentication token to SharedPreferences.
     * @param token The JWT received from the server.
     */
    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    /**
     * Retrieves the authentication token from SharedPreferences.
     * @return The saved token, or null if it doesn't exist.
     */
    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Clears the session by removing the authentication token.
     */
    public void clearSession() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.apply();
    }
}
