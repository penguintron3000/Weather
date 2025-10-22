package edu.uiuc.cs427app.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ThemeManager applies LLM-generated theme descriptors across the application.
 */
public final class ThemeManager {

    private static final String TAG = "ThemeManager";
    private static final String PREFS_NAME = "llm_theme_store";
    private static final String PREF_KEY_THEME_JSON = "theme_json";

    private ThemeManager() {
        // Utility class
    }

    /**
     * Persists the LLM theme JSON so downstream screens can render it.
     *
     * @param context   context used to access persistent storage
     * @param themeJson JSON definition for the theme supplied by the LLM
     */
    public static void applyLlmTheme(Context context, String themeJson) {
        if (themeJson == null || themeJson.trim().isEmpty()) {
            Log.d(TAG, "No LLM theme data available to apply");
            return;
        }
        if (!isValidJson(themeJson)) {
            Log.w(TAG, "Ignoring invalid theme data returned by the LLM");
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putString(PREF_KEY_THEME_JSON, themeJson)
                .apply();
        Log.d(TAG, "Stored LLM theme data for later consumption");
    }

    /**
     * Performs a basic JSON validation to guard against malformed content.
     *
     * @param themeJson raw JSON string supplied by the LLM
     * @return true if the string is valid JSON
     */
    private static boolean isValidJson(String themeJson) {
        try {
            new JSONObject(themeJson);
            return true;
        } catch (JSONException exception) {
            return false;
        }
    }
}
