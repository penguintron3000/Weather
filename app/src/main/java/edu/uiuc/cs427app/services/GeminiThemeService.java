package edu.uiuc.cs427app.services;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uiuc.cs427app.BuildConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * GeminiThemeService generates an AppTheme JSON specification from a natural language description
 * using Google's Gemini model. The API key is read from BuildConfig.GEMINI_API_KEY which is populated
 * from local.properties and MUST NOT be hardcoded.
 */
public class GeminiThemeService {
    private static final String TAG = "GeminiThemeService";

    /**
     * Callback interface for handling theme generation results.
     * Methods are invoked on a background thread; UI updates must be done on the UI thread.
     */
    public interface ThemeCallback {
        /**
         * Invoked when theme generation succeeds.
         * The theme JSON object is guaranteed to have all required fields and is ready for use.
         *
         * @param themeJson The generated theme JSON object with all required fields
         */
        void onSuccess(JSONObject themeJson);

        /**
         * Invoked when theme generation fails.
         *
         * @param error The exception that occurred during theme generation
         */
        void onError(Exception error);
    }

    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Generates a theme configuration based on the user's natural language description.
     * This method sends the description to Google's Gemini LLM via HTTP API asynchronously
     * and processes the response to produce a valid AppTheme JSON object.
     *
     * @param context The Android context for logging and resource access
     * @param description The user's natural language description of the desired theme
     * @param callback The callback to handle success or error results (invoked on background thread)
     */
    public void generateThemeFromDescription(Context context, String description, ThemeCallback callback) {
        final String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError(new IllegalStateException("Gemini API key missing. Define GEMINI_API_KEY in local.properties."));
            return;
        }

        final String prompt = buildPrompt(description);

        // Execute theme generation asynchronously on background thread
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
            try {
                // Build request JSON per Gemini REST API
                JSONObject textPart = new JSONObject().put("text", prompt);
                JSONObject part = new JSONObject().put("parts", new JSONArray().put(textPart));
                JSONObject contents = new JSONObject().put("contents", new JSONArray().put(part));
                JSONObject generationConfig = new JSONObject().put("responseMimeType", "application/json");
                JSONObject requestBody = new JSONObject()
                        .put("generationConfig", generationConfig)
                        .put("contents", contents.getJSONArray("contents"));

                MediaType jsonMedia = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(requestBody.toString(), jsonMedia);

                String url = BASE_URL + MODEL_NAME + ":generateContent?key=" + apiKey;
                String maskedKey = apiKey.length() > 8 ? (apiKey.substring(0, 8) + "…") : "(set)";
                Log.i(TAG, "Gemini request URL: " + BASE_URL + MODEL_NAME + ":generateContent?key=" + maskedKey);
                Log.i(TAG, "Gemini prompt: " + (prompt.length() > 500 ? prompt.substring(0, 500) + "…" : prompt));
                Log.i(TAG, "Gemini request body: " + requestBody.toString());
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String err = response.body() != null ? response.body().string() : "";
                        Log.e(TAG, "Gemini non-200 response: code=" + response.code() + ", message=" + response.message());
                        Log.e(TAG, "Gemini error body: " + err);
                        throw new IOException("Gemini API error: " + response.code() + " - " + response.message());
                    }
                    String responseStr = response.body() != null ? response.body().string() : "";
                    Log.i(TAG, "Gemini REST raw response: " + responseStr);
                    if (responseStr.isEmpty()) {
                        throw new IllegalStateException("Empty response from LLM");
                    }

                    // Parse: candidates[0].content.parts[*].text
                    JSONObject root = new JSONObject(responseStr);
                    JSONArray candidates = root.optJSONArray("candidates");
                    if (candidates == null || candidates.length() == 0) {
                        throw new IllegalStateException("No candidates in response");
                    }
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    JSONObject candidateContent = firstCandidate.getJSONObject("content");
                    JSONArray parts = candidateContent.getJSONArray("parts");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.length(); i++) {
                        JSONObject p = parts.getJSONObject(i);
                        sb.append(p.optString("text", ""));
                    }
                    String text = sb.toString();
                    Log.i(TAG, "Gemini extracted text: " + text);
                    if (text.trim().isEmpty()) {
                        throw new IllegalStateException("Empty text in candidate parts");
                    }

                    String jsonCandidate = extractJson(text);
                    Log.i(TAG, "Extracted JSON candidate: " + jsonCandidate);
                    JSONObject themeJson = new JSONObject(jsonCandidate);
                    // Attempt to normalize and map schema variations before strict validation
                    normalizeThemeSchema(themeJson, description);
                    mapFromColorsObject(themeJson);
                    Log.i(TAG, "Normalized theme JSON: " + themeJson.toString());
                    validateRequiredFields(themeJson);
                    callback.onSuccess(themeJson);
                }
            } catch (Exception e) {
                Log.e(TAG, "Theme generation error: " + e.getMessage(), e);
                callback.onError(e);
            }
        });
    }

    /**
     * Builds the prompt string sent to Gemini LLM for theme generation.
     * The prompt instructs the model to return a JSON object with specific required fields.
     *
     * @param description The user's natural language theme description
     * @return The formatted prompt string for Gemini API
     */
    private String buildPrompt(String description) {
        // Prompt aligned with DEVELOPER_GUIDE_APPTHEME.md → LLM Integration requirements
        return "Create a theme for a weather app based on: '" + description + "'.\n" +
                "Respond with ONLY a valid JSON object (no markdown, no extra keys) with EXACTLY these fields: " +
                "themeName, backgroundColor, textColor, primaryColor, secondaryColor, headerColor, " +
                "buttonBackgroundColor, buttonTextColor, cardBackgroundColor, borderColor, errorColor, successColor, emoji.\n" +
                "- All colors must be hex (#RRGGBB or #AARRGGBB).\n" +
                "- Ensure sufficient contrast (WCAG AA).\n" +
                "- 'emoji' must be a single emoji character.\n" +
                "Output JSON only.";
    }

    /**
     * Extracts a JSON string from text that may contain markdown code fences or extra content.
     * Handles cases where the LLM wraps JSON in ```json``` blocks or includes extra text.
     *
     * @param text The raw text response from the LLM that may contain JSON
     * @return The extracted JSON string, or the original text if no JSON structure is found
     */
    private String extractJson(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            // Remove fences
            String withoutFences = trimmed.replaceFirst("^```[a-zA-Z]*\\n", "").replaceFirst("\\n```$", "");
            return withoutFences.trim();
        }
        // Try to locate first '{' and last '}'
        int first = trimmed.indexOf('{');
        int last = trimmed.lastIndexOf('}');
        if (first != -1 && last != -1 && last > first) {
            return trimmed.substring(first, last + 1);
        }
        return trimmed;
    }

    /**
     * Validates that a JSON object contains all required theme fields.
     * Throws JSONException if any required field is missing.
     *
     * @param obj The JSON object to validate
     * @throws JSONException If any required field is missing
     */
    private void validateRequiredFields(JSONObject obj) throws JSONException {
        String[] required = new String[] {
                "themeName",
                "backgroundColor",
                "textColor",
                "primaryColor",
                "secondaryColor",
                "headerColor",
                "buttonBackgroundColor",
                "buttonTextColor",
                "cardBackgroundColor",
                "borderColor",
                "errorColor",
                "successColor",
                "emoji"
        };
        for (String key : required) {
            if (!obj.has(key)) {
                throw new JSONException("Missing required field: " + key);
            }
        }
    }

    /**
     * Normalizes a theme JSON object by mapping common alias keys to required schema keys.
     * Also generates a default themeName if missing by using the description.
     * This handles variations in LLM output format to match the expected schema.
     *
     * @param obj The JSON object to normalize (modified in place)
     * @param description The original theme description, used to generate themeName if missing
     */
    private void normalizeThemeSchema(JSONObject obj, String description) {
        // Map common alias keys from LLM outputs to required schema keys
        renameKey(obj, "name", "themeName");
        renameKey(obj, "title", "themeName");
        renameKey(obj, "theme_name", "themeName");

        renameKey(obj, "background", "backgroundColor");
        renameKey(obj, "bgColor", "backgroundColor");

        renameKey(obj, "text", "textColor");
        renameKey(obj, "text_colour", "textColor");

        renameKey(obj, "primary", "primaryColor");
        renameKey(obj, "secondary", "secondaryColor");
        renameKey(obj, "header", "headerColor");

        renameKey(obj, "buttonBackground", "buttonBackgroundColor");
        renameKey(obj, "buttonBgColor", "buttonBackgroundColor");
        renameKey(obj, "buttonText", "buttonTextColor");

        renameKey(obj, "cardBackground", "cardBackgroundColor");
        renameKey(obj, "border", "borderColor");
        renameKey(obj, "error", "errorColor");
        renameKey(obj, "success", "successColor");

        renameKey(obj, "icon", "emoji");
        renameKey(obj, "symbol", "emoji");

        // If still missing themeName, derive a short one from the description
        if (!obj.has("themeName") && description != null) {
            String trimmed = description.trim();
            if (trimmed.length() > 32) trimmed = trimmed.substring(0, 32);
            try {
                obj.put("themeName", trimmed.isEmpty() ? "Generated Theme" : trimmed);
            } catch (JSONException ignored) {
            }
        }
    }

    /**
     * Renames a key in a JSON object if the source key exists and the target key doesn't.
     * This is used to map common LLM output variations to the required schema keys.
     *
     * @param obj The JSON object to modify
     * @param from The source key name to rename from
     * @param to The target key name to rename to
     */
    private void renameKey(JSONObject obj, String from, String to) {
        if (obj.has(to)) return;
        if (obj.has(from)) {
            Object value = obj.opt(from);
            obj.remove(from);
            try {
                obj.put(to, value);
            } catch (JSONException ignored) {
            }
        }
    }

    /**
     * Maps color values from a nested "colors" object to the top-level theme JSON object.
     * This handles LLM responses that nest colors in a "colors" sub-object instead of at the root level.
     * Uses fallback logic for button colors when explicit values are not present.
     *
     * @param obj The theme JSON object that may contain a "colors" sub-object (modified in place)
     */
    private void mapFromColorsObject(JSONObject obj) {
        JSONObject colors = obj.optJSONObject("colors");
        if (colors == null) return;

        copyIfPresent(colors, obj, "background", "backgroundColor");
        copyIfPresent(colors, obj, "backgroundColor", "backgroundColor");
        copyIfPresent(colors, obj, "on_background", "textColor");
        copyIfPresent(colors, obj, "text", "textColor");

        copyIfPresent(colors, obj, "primary", "primaryColor");
        copyIfPresent(colors, obj, "secondary", "secondaryColor");
        copyIfPresent(colors, obj, "accent", "secondaryColor");
        copyIfPresent(colors, obj, "header", "headerColor");

        // Buttons: prefer explicit, fall back to primary
        if (!obj.has("buttonBackgroundColor")) {
            if (colors.has("buttonBackground")) {
                copyIfPresent(colors, obj, "buttonBackground", "buttonBackgroundColor");
            } else if (colors.has("primary")) {
                copyIfPresent(colors, obj, "primary", "buttonBackgroundColor");
            }
        }
        if (!obj.has("buttonTextColor")) {
            if (colors.has("buttonText")) {
                copyIfPresent(colors, obj, "buttonText", "buttonTextColor");
            } else if (colors.has("on_primary")) {
                copyIfPresent(colors, obj, "on_primary", "buttonTextColor");
            } else if (colors.has("onPrimary")) {
                copyIfPresent(colors, obj, "onPrimary", "buttonTextColor");
            }
        }

        copyIfPresent(colors, obj, "surface", "cardBackgroundColor");
        copyIfPresent(colors, obj, "card", "cardBackgroundColor");
        copyIfPresent(colors, obj, "border", "borderColor");
        copyIfPresent(colors, obj, "error", "errorColor");
        copyIfPresent(colors, obj, "success", "successColor");
    }

    /**
     * Copies a value from one JSON object to another if the source key exists and target key doesn't.
     * This is a helper method for mapping color values between JSON structures.
     *
     * @param from The source JSON object to read from
     * @param to The target JSON object to write to (modified in place)
     * @param fromKey The source key name
     * @param toKey The target key name
     */
    private void copyIfPresent(JSONObject from, JSONObject to, String fromKey, String toKey) {
        if (!to.has(toKey) && from.has(fromKey)) {
            try {
                to.put(toKey, from.opt(fromKey));
            } catch (JSONException ignored) {
            }
        }
    }
}


