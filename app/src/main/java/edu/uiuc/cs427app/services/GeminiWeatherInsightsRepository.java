package edu.uiuc.cs427app.services;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.uiuc.cs427app.BuildConfig;
import edu.uiuc.cs427app.City;
import edu.uiuc.cs427app.services.weather.WeatherSnapshot;
import edu.uiuc.cs427app.services.WeatherService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Implementation of {@link WeatherInsightsRepository} backed by Gemini.
 */
public class GeminiWeatherInsightsRepository implements WeatherInsightsRepository {

    private static final String TAG = "GeminiWeatherInsights";
    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ExecutorService executor;
    private final String apiKey;

    public GeminiWeatherInsightsRepository() {
        this(buildDefaultClient(), Executors.newSingleThreadExecutor());
    }

    GeminiWeatherInsightsRepository(OkHttpClient httpClient, ExecutorService executor) {
        this.httpClient = httpClient;
        this.executor = executor;
        this.apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Gemini API key missing. Define GEMINI_API_KEY in local.properties.");
        }
    }

    /**
     * Builds a default OkHttpClient with appropriate timeouts for Gemini API requests.
     * 
     * @return Configured OkHttpClient instance with 30s connect/write timeouts and 60s read timeout
     */
    private static OkHttpClient buildDefaultClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Override
    public void fetchInitialQuestions(City city, QuestionsCallback callback) {
        executor.execute(() -> {
            try {
                WeatherSnapshot snapshot = fetchWeatherSnapshot(city);
                JSONObject requestBody = buildRequestBody(buildInitialPrompt(city, snapshot));
                JSONObject payload = executeGeminiRequest(requestBody);
                List<String> questions = parseQuestions(payload);
                callback.onSuccess(questions);
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch initial questions", e);
                callback.onError(e);
            }
        });
    }

    @Override
    public void askQuestion(City city, String prompt, ResponseCallback callback) {
        executor.execute(() -> {
            try {
                WeatherSnapshot snapshot = fetchWeatherSnapshot(city);
                JSONObject requestBody = buildRequestBody(buildFollowUpPrompt(city, snapshot, prompt));
                JSONObject payload = executeGeminiRequest(requestBody);
                WeatherInsightsResponse response = parseResponse(payload);
                callback.onSuccess(response);
            } catch (Exception e) {
                Log.e(TAG, "Failed to ask weather question", e);
                callback.onError(e);
            }
        });
    }

    /**
     * Fetches weather data and converts it to WeatherSnapshot.
     * Falls back to hardcoded data if API is unavailable (e.g., API key not activated yet).
     * 
     * @param city The city to fetch weather data for
     * @return WeatherSnapshot containing current weather conditions
     * @throws IllegalArgumentException if city is null
     * @throws Exception if weather data cannot be fetched (fallback data will be used instead)
     */
    private WeatherSnapshot fetchWeatherSnapshot(City city) throws Exception {
        if (city == null) {
            throw new IllegalArgumentException("city must not be null");
        }

        WeatherService weatherService = new WeatherService(city.getDisplayName());
        
        try {
            weatherService.fetchWeatherDataSync();
            
            String summary = String.format("Weather data for %s via OpenWeatherMap API", city.getDisplayName());
            
            return new WeatherSnapshot(
                weatherService.getTemperature(),
                weatherService.getWeatherCondition(),
                weatherService.getHumidity(),
                weatherService.getWindSpeed(),
                weatherService.getWindDirection(),
                summary
            );
        } catch (Exception e) {
            // API failed (key maybe not activated yet), use hardcoded fallback data
            Log.w(TAG, "Weather API unavailable for " + city.getDisplayName() + ", using fallback data. Error: " + e.getMessage());
            
            String summary = String.format("Using fallback weather data for %s (API may not be activated yet)", city.getDisplayName());
            
            return new WeatherSnapshot(
                72.0,           // temperature
                "Partly Cloudy", // condition
                55,             // humidity
                6.0,            // wind speed
                "NW",           // wind direction
                summary
            );
        } finally {
            weatherService.shutdown();
        }
    }

    /**
     * Builds the JSON request body for Gemini API calls.
     * Configures the request to return JSON format with a temperature setting for response variability.
     * 
     * @param promptText The text prompt to send to Gemini
     * @return JSONObject formatted according to Gemini API specification
     * @throws JSONException if JSON construction fails
     */
    private JSONObject buildRequestBody(String promptText) throws JSONException {
        JSONArray parts = new JSONArray().put(new JSONObject().put("text", promptText));
        JSONObject content = new JSONObject().put("parts", parts);
        JSONArray contents = new JSONArray().put(content);

        JSONObject generationConfig = new JSONObject()
                .put("responseMimeType", "application/json")
                .put("temperature", 0.7);

        return new JSONObject()
                .put("contents", contents)
                .put("generationConfig", generationConfig);
    }

    /**
     * Executes a Gemini API request with rate limiting and retry logic.
     * Enforces rate limiting before making the request to prevent quota exhaustion.
     * 
     * @param requestBody The JSON request body to send to Gemini
     * @return JSONObject containing the parsed response from Gemini
     * @throws IOException if rate limit is exceeded, network error occurs, or API returns an error
     * @throws JSONException if the response cannot be parsed
     */
    private JSONObject executeGeminiRequest(JSONObject requestBody) throws IOException, JSONException {
        // Enforce rate limiting (shared across all Gemini services using the same API key)
        GeminiRateLimiter.enforceRateLimit();
        return executeGeminiRequestWithRetry(requestBody, 0);
    }

    /**
     * Executes a Gemini API request with retry logic for transient failures.
     * Retries once on 503 (Service Unavailable) errors but never retries on quota/rate limit errors.
     * 
     * @param requestBody The JSON request body to send to Gemini
     * @param attempt The current retry attempt number (0 = first attempt, 1 = retry)
     * @return JSONObject containing the parsed response from Gemini
     * @throws IOException if the API request fails, quota is exceeded, or response is invalid
     * @throws JSONException if the response cannot be parsed
     */
    private JSONObject executeGeminiRequestWithRetry(JSONObject requestBody, int attempt) throws IOException, JSONException {
        RequestBody body = RequestBody.create(requestBody.toString(), JSON_MEDIA);
        String url = BASE_URL + MODEL_NAME + ":generateContent?key=" + apiKey;
        String maskedKey = apiKey.length() > 8 ? apiKey.substring(0, 8) + "…" : "(set)";
        Log.d(TAG, "Calling Gemini (attempt " + (attempt + 1) + "): " + BASE_URL + MODEL_NAME + ":generateContent?key=" + maskedKey);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String err = response.body() != null ? response.body().string() : "";
                Log.e(TAG, "=== Gemini API Error ===");
                Log.e(TAG, "Status Code: " + response.code());
                Log.e(TAG, "Status Message: " + response.message());
                Log.e(TAG, "Model: " + MODEL_NAME);
                Log.e(TAG, "Error Body: " + err);
                Log.e(TAG, "======================");
                
                String errorMessage = extractErrorMessage(err);
                
                // NEVER retry on 429 (quota exceeded) or resource exhausted errors - this wastes quota
                if (response.code() == 429 || (errorMessage != null && 
                    (errorMessage.toLowerCase().contains("resource exhausted") || 
                     errorMessage.toLowerCase().contains("quota") ||
                     errorMessage.toLowerCase().contains("rate limit")))) {
                    Log.e(TAG, "Quota/rate limit exceeded - NOT retrying to avoid wasting quota");
                    Log.e(TAG, "Full error details logged above. Check Google AI Studio for quota status.");
                    if (errorMessage != null && !errorMessage.isEmpty()) {
                        throw new IOException("Gemini API error: " + errorMessage);
                    }
                    throw new IOException("Gemini API error: Quota exceeded. Please try again later.");
                }
                
                // Only retry on 503 (Service Unavailable), not on quota errors
                if (response.code() == 503 && attempt < 1) {
                    Log.w(TAG, "Gemini service temporarily unavailable, retrying once... " + errorMessage);
                    
                    // Wait 2 seconds before retry
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Retry interrupted", e);
                    }
                    
                    return executeGeminiRequestWithRetry(requestBody, attempt + 1);
                }
                
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    throw new IOException("Gemini API error: " + errorMessage);
                }
                throw new IOException("Gemini API error: " + response.code() + " - " + response.message());
            }
            String responseStr = response.body() != null ? response.body().string() : "";
            if (responseStr.isEmpty()) {
                throw new IOException("Gemini returned empty response");
            }
            JSONObject root = new JSONObject(responseStr);
            JSONArray candidates = root.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) {
                throw new IOException("Gemini response missing candidates");
            }
            JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            if (parts.length() == 0) {
                throw new IOException("Gemini response missing parts");
            }
            String text = parts.getJSONObject(0).optString("text", "").trim();
            if (text.isEmpty()) {
                throw new IOException("Gemini returned empty text payload");
            }
            String json = extractJson(text);
            return new JSONObject(json);
        }
    }

    /**
     * Extracts a error message from Gemini API error response JSON.
     * Returns null if the error body cannot be parsed.
     */
    private String extractErrorMessage(String errorBody) {
        if (errorBody == null || errorBody.trim().isEmpty()) {
            return null;
        }
        try {
            JSONObject errorJson = new JSONObject(errorBody);
            JSONObject error = errorJson.optJSONObject("error");
            if (error != null) {
                String message = error.optString("message", "");
                if (!message.isEmpty()) {
                    return message;
                }
            }
        } catch (JSONException e) {
            // If parsing fails, return null and fall back to generic error
            Log.d(TAG, "Could not parse error response JSON", e);
        }
        return null;
    }

    /**
     * Extracts JSON from text that may contain markdown code fences or extra content.
     * Handles cases where Gemini wraps JSON in ```json``` blocks or includes explanatory text.
     * 
     * @param text The raw text response from Gemini that may contain JSON
     * @return The extracted JSON string, or the original text if no JSON structure is found
     */
    private String extractJson(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            trimmed = trimmed.replaceFirst("^```[a-zA-Z]*\\n", "");
            trimmed = trimmed.replaceFirst("\\n```$", "");
            return trimmed.trim();
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    /**
     * Parses the questions array from Gemini's initial questions response.
     * Validates that at least two questions are present.
     * 
     * @param payload The JSON response from Gemini containing a "questions" array
     * @return List of question strings (at least 2 questions)
     * @throws JSONException if the payload structure is invalid
     * @throws IllegalStateException if fewer than 2 questions are found
     */
    private List<String> parseQuestions(JSONObject payload) throws JSONException {
        JSONArray array = payload.optJSONArray("questions");
        if (array == null || array.length() < 2) {
            throw new IllegalStateException("LLM did not return at least two questions");
        }
        List<String> results = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            String question = array.optString(i, "").trim();
            if (!question.isEmpty()) {
                results.add(question);
            }
        }
        if (results.size() < 2) {
            throw new IllegalStateException("LLM did not return at least two valid questions");
        }
        return results;
    }

    /**
     * Parses the reply and follow-up questions from Gemini's question response.
     * Validates that both reply and at least two follow-up questions are present.
     * 
     * @param payload The JSON response from Gemini containing "reply" and "followUpQuestions"
     * @return WeatherInsightsResponse containing the answer and follow-up questions
     * @throws JSONException if the payload structure is invalid
     * @throws IllegalStateException if reply is missing or fewer than 2 follow-up questions are found
     */
    private WeatherInsightsResponse parseResponse(JSONObject payload) throws JSONException {
        String reply = payload.optString("reply", "").trim();
        if (reply.isEmpty()) {
            throw new IllegalStateException("LLM response missing reply");
        }
        JSONArray followUps = payload.optJSONArray("followUpQuestions");
        if (followUps == null || followUps.length() < 2) {
            throw new IllegalStateException("LLM response missing follow-up questions");
        }
        List<String> questions = new ArrayList<>();
        for (int i = 0; i < followUps.length(); i++) {
            String question = followUps.optString(i, "").trim();
            if (!question.isEmpty()) {
                questions.add(question);
            }
        }
        if (questions.size() < 2) {
            throw new IllegalStateException("LLM follow-up questions invalid");
        }
        return new WeatherInsightsResponse(reply, questions);
    }

    /**
     * Builds the initial prompt sent to Gemini for generating weather-related questions.
     * The prompt instructs Gemini to generate at least three context-relevant questions
     * based on the current weather conditions for the specified city.
     * 
     * @param city The city for which to generate questions
     * @param snapshot The current weather conditions for the city
     * @return Formatted prompt string to send to Gemini API
     */
    private String buildInitialPrompt(City city, WeatherSnapshot snapshot) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a proactive weather assistant for a mobile weather app.\n");
        builder.append("Generate at least three concise, decision-focused questions that a user in ")
                .append(city.getDisplayName());
        if (city.getState() != null && !city.getState().isEmpty()) {
            builder.append(", ").append(city.getState());
        }
        if (city.getCountryCode() != null && !city.getCountryCode().isEmpty()) {
            builder.append(" (").append(city.getCountryCode()).append(")");
        }
        builder.append(" might ask based on the current conditions.\n");
        builder.append("Each question should be unique, under 120 characters, and helpful for planning daily activities.\n");
        builder.append("Use this weather summary:\n");
        builder.append(formatWeatherSummary(snapshot));
        builder.append("\nRespond ONLY with valid JSON in this exact shape:\n");
        builder.append("{\"questions\": [\"Question one\", \"Question two\", \"Question three\"]}\n");
        builder.append("Do not include any additional fields or explanations.");
        return builder.toString();
    }

    /**
     * Builds the follow-up prompt sent to Gemini when a user selects a question.
     * The prompt includes the user's question and current weather conditions, and instructs
     * Gemini to provide an answer plus at least two follow-up questions.
     * 
     * @param city The city for which the question is being asked
     * @param snapshot The current weather conditions for the city
     * @param userPrompt The question the user selected
     * @return Formatted prompt string to send to Gemini API
     */
    private String buildFollowUpPrompt(City city, WeatherSnapshot snapshot, String userPrompt) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are assisting a user of a weather insights feature for ")
                .append(city.getDisplayName());
        if (city.getState() != null && !city.getState().isEmpty()) {
            builder.append(", ").append(city.getState());
        }
        if (city.getCountryCode() != null && !city.getCountryCode().isEmpty()) {
            builder.append(" (").append(city.getCountryCode()).append(")");
        }
        builder.append(".\n");
        builder.append("Current conditions:\n");
        builder.append(formatWeatherSummary(snapshot));
        builder.append("\nThe user asked: \"").append(userPrompt).append("\".\n");
        builder.append("Provide a clear, practical answer and suggest at least two follow-up questions that keep the conversation going.\n");
        builder.append("Respond ONLY with JSON shaped like this:\n");
        builder.append("{\"reply\": \"Your answer...\", \"followUpQuestions\": [\"Next question 1\", \"Next question 2\"]}\n");
        builder.append("Follow-up questions must be specific to the current conditions and under 120 characters.");
        return builder.toString();
    }

    /**
     * Formats weather data into a human-readable summary string for inclusion in Gemini prompts.
     * Includes condition, temperature, humidity, wind speed/direction, and optional observation notes.
     * 
     * @param snapshot The weather data to format
     * @return Formatted multi-line string describing the weather conditions
     */
    private String formatWeatherSummary(WeatherSnapshot snapshot) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.getDefault(), "- Condition: %s%n", snapshot.getConditionDescription()));
        builder.append(String.format(Locale.getDefault(), "- Temperature: %.1f°F%n", snapshot.getTemperatureFahrenheit()));
        builder.append(String.format(Locale.getDefault(), "- Humidity: %d%%%n", snapshot.getHumidityPercentage()));
        builder.append(String.format(Locale.getDefault(), "- Wind: %.1f mph", snapshot.getWindSpeedMph()));
        if (snapshot.getWindDirectionCardinal() != null) {
            builder.append(String.format(Locale.getDefault(), " from %s", snapshot.getWindDirectionCardinal()));
        }
        builder.append("\n");
        if (snapshot.getObservationSummary() != null) {
            builder.append(String.format(Locale.getDefault(), "- Note: %s%n", snapshot.getObservationSummary()));
        }
        return builder.toString();
    }
}


