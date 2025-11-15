package edu.uiuc.cs427app.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.uiuc.cs427app.BuildConfig;
import edu.uiuc.cs427app.services.weather.WeatherSnapshot;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * WeatherAwareCityViewService generates a realistic city view image using a Gemini
 * image generation model that matches the current weather conditions for a given city.
 *
 * <p>The service uses the existing {@link WeatherService} implementation to retrieve
 * weather data, then builds a detailed prompt for a Gemini image generation model
 * to create a photo-realistic background image suitable for the weather details page.
 */
public class WeatherAwareCityViewService {

    private static final String TAG = "WeatherAwareCityView";

    private static final String MODEL_NAME = "gemini-2.5-flash-image";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ExecutorService executor;
    private final String apiKey;

    /**
     * Callback interface for receiving the generated city view image or an error.
     */
    public interface Callback {
        /**
         * Called when the Gemini API successfully returns an image for the requested city.
         *
         * @param bitmap The decoded bitmap for the weather-aware city view image
         */
        void onSuccess(Bitmap bitmap);

        /**
         * Called when an error occurs while generating the city view image.
         *
         * @param error The exception describing the failure (network, rate limit, parsing, etc.)
         */
        void onError(Exception error);
    }

    /**
     * Constructs a new WeatherAwareCityViewService with a default HTTP client and executor.
     *
     * @throws IllegalStateException if the Gemini API key is missing or empty
     */
    public WeatherAwareCityViewService() {
        this(buildDefaultClient(), Executors.newSingleThreadExecutor());
    }

    /**
     * Constructs a new WeatherAwareCityViewService with the specified HTTP client and executor.
     * Intended for testing.
     *
     * @param httpClient The OkHttpClient instance to use
     * @param executor   The ExecutorService for async operations
     * @throws IllegalStateException if the Gemini API key is missing or empty
     */
    WeatherAwareCityViewService(OkHttpClient httpClient, ExecutorService executor) {
        this.httpClient = httpClient;
        this.executor = executor;
        this.apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Gemini API key missing. Define GEMINI_API_KEY in local.properties.");
        }
    }

    private static OkHttpClient buildDefaultClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * Generates a weather-aware city view image for the given city.
     *
     * <p>This method:
     * <ol>
     *     <li>Fetches current weather data via {@link WeatherService}.</li>
     *     <li>Computes the city's local time-of-day label (morning/afternoon/evening/night).</li>
     *     <li>Builds a detailed, weather-aware prompt describing the city scene.</li>
     *     <li>Calls the Gemini image model to create a realistic image.</li>
     * </ol>
     *
     * @param cityName Name of the city (e.g., "Champaign")
     * @param callback Callback to receive the generated Bitmap or an error
     */
    public void generateCityViewImage(String cityName, Callback callback) {
        if (cityName == null || cityName.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("cityName must not be null or empty"));
            return;
        }

        executor.execute(() -> {
            String trimmedName = cityName.trim();
            try {
                WeatherService weatherService = new WeatherService(trimmedName);
                WeatherSnapshot snapshot;
                String timeOfDayLabel;
                try {
                    weatherService.fetchWeatherDataSync();

                    String summary = String.format(
                            Locale.US,
                            "Weather data for %s via OpenWeatherMap API used to seed image prompt.",
                            trimmedName
                    );

                    snapshot = new WeatherSnapshot(
                            weatherService.getTemperature(),
                            weatherService.getWeatherCondition(),
                            weatherService.getHumidity(),
                            weatherService.getWindSpeed(),
                            weatherService.getWindDirection(),
                            summary
                    );

                    // Derive time-of-day based on the city's local hour from the weather API.
                    int localHour = weatherService.getLocalHourOfDay();
                    timeOfDayLabel = deriveTimeOfDayForHour(localHour);
                } catch (Exception e) {
                    Log.w(TAG, "Weather API unavailable for " + trimmedName + ", using fallback data. Error: " + e.getMessage());

                    String summary = String.format(
                            Locale.US,
                            "Using fallback weather data for %s (API may not be activated yet)",
                            trimmedName
                    );

                    snapshot = new WeatherSnapshot(
                            72.0,
                            "Partly Cloudy",
                            55,
                            6.0,
                            "NW",
                            summary
                    );

                    // Fallback: derive time-of-day from the device clock if API time fields are unavailable.
                    timeOfDayLabel = deriveTimeOfDayFromDeviceClock();
                } finally {
                    weatherService.shutdown();
                }

                String prompt = buildImagePrompt(trimmedName, snapshot, timeOfDayLabel);
                JSONObject requestBody = buildRequestBody(prompt);
                Bitmap bitmap = executeGeminiImageRequest(requestBody);
                if (bitmap == null) {
                    throw new IOException("Gemini returned no image data");
                }
                callback.onSuccess(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Failed to generate city view image", e);
                callback.onError(e);
            }
        });
    }

    /**
     * Designs the Gemini image generation prompt using the city name, weather
     * data, and local time of day.
     *
     * <p>Example prompt (conceptual):
     * "Create a realistic image of Paris that matches this weather:
     *  Cloudy, 60°F, light wind from the NW in the evening. Show a city street
     *  scene with wet pavement, soft reflections, and dim streetlights. The
     *  style should be photo-realistic, 16:9 aspect ratio, no text or watermarks."
     *
     * @param cityName The display name of the city for which to generate the image
     * @param snapshot Snapshot of current (or fallback) weather conditions
     * @param timeOfDayLabel A label describing the city's local time of day (e.g., "morning", "evening")
     * @return A detailed, weather-aware prompt string for the Gemini model
     */
    private String buildImagePrompt(String cityName, WeatherSnapshot snapshot, String timeOfDayLabel) {
        String windDirection = snapshot.getWindDirectionCardinal() != null
                ? snapshot.getWindDirectionCardinal()
                : "variable";

        String observationSummary = snapshot.getObservationSummary() != null
                ? snapshot.getObservationSummary()
                : "Live weather data describing current conditions.";

        return String.format(
                Locale.US,
                "Create a single, photo-realistic image of the city of %s that visually matches "
                        + "these current weather conditions:\n"
                        + "- Condition: %s\n"
                        + "- Temperature: %.0f°F\n"
                        + "- Humidity: %d%%\n"
                        + "- Wind: %.1f mph %s\n"
                        + "- Time of day: %s.\n\n"
                        + "Show a recognizable but generic city view (street-level or skyline) for %s. "
                        + "Emphasize lighting, sky, and atmosphere that fit the time of day and weather "
                        + "(for example, wet sidewalks and reflections for rain, bright clear skies for "
                        + "sunny noon, long warm shadows at sunset, or hazy air for humid conditions). "
                        + "The image must look like a high-quality photograph, suitable as a background "
                        + "for a mobile weather app. Use a widescreen 16:9 aspect ratio, avoid any text, "
                        + "logos, or watermarks, and focus on the overall mood described above.\n\n"
                        + "Additional context: %s",
                cityName,
                snapshot.getConditionDescription(),
                snapshot.getTemperatureFahrenheit(),
                snapshot.getHumidityPercentage(),
                snapshot.getWindSpeedMph(),
                windDirection,
                timeOfDayLabel,
                cityName,
                observationSummary
        );
    }

    /**
     * Derives a coarse time-of-day label from an hour-of-day value (0-23).
     */
    private String deriveTimeOfDayForHour(int hour) {
        if (hour >= 5 && hour < 12) {
            return "morning";
        } else if (hour >= 12 && hour < 17) {
            return "afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "evening";
        } else {
            return "night";
        }
    }

    /**
     * Fallback: derives a time-of-day label for the prompt based on the local device time.
     *
     * @return A human-readable time-of-day label: "morning", "afternoon", "evening", or "night"
     */
    private String deriveTimeOfDayFromDeviceClock() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);

        return deriveTimeOfDayForHour(hour);
    }

    /**
     * Builds the JSON request body for the Gemini image generation API.
     *
     * <p>Note: The image data is returned via {@code inlineData} inside the response
     * {@code parts} array; we do not set {@code generationConfig.responseMimeType} here
     * because only text mime types are allowed for that field on this endpoint.
     *
     * @param promptText The fully formatted text prompt to send to Gemini
     * @return JSONObject representing the request body for {@code :generateContent}
     * @throws JSONException If an error occurs while constructing the JSON payload
     */
    private JSONObject buildRequestBody(String promptText) throws JSONException {
        JSONArray parts = new JSONArray().put(new JSONObject().put("text", promptText));
        JSONObject content = new JSONObject().put("parts", parts);
        JSONArray contents = new JSONArray().put(content);

        return new JSONObject().put("contents", contents);
    }

    /**
     * Executes the Gemini image generation request and returns a Bitmap decoded
     * from the {@code inlineData} field of the response.
     *
     * @param requestBody The JSON request body to send to the Gemini API
     * @return A decoded Bitmap representing the generated city view image
     * @throws IOException   If the HTTP request fails or the API returns an error status
     * @throws JSONException If the response body cannot be parsed as valid JSON
     */
    private Bitmap executeGeminiImageRequest(JSONObject requestBody) throws IOException, JSONException {
        // Enforce shared Gemini rate limiting
        GeminiRateLimiter.enforceRateLimit();

        RequestBody body = RequestBody.create(requestBody.toString(), JSON_MEDIA);
        String url = BASE_URL + MODEL_NAME + ":generateContent?key=" + apiKey;
        String maskedKey = apiKey.length() > 8 ? apiKey.substring(0, 8) + "…" : "(set)";
        Log.d(TAG, "Calling Gemini image model: " + BASE_URL + MODEL_NAME + ":generateContent?key=" + maskedKey);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String err = response.body() != null ? response.body().string() : "";
                Log.e(TAG, "=== Gemini Image API Error ===");
                Log.e(TAG, "Status Code: " + response.code());
                Log.e(TAG, "Status Message: " + response.message());
                Log.e(TAG, "Model: " + MODEL_NAME);
                Log.e(TAG, "Error Body: " + err);
                Log.e(TAG, "================================");

                String errorMessage = extractErrorMessage(err);

                if (response.code() == 429 || (errorMessage != null
                        && (errorMessage.toLowerCase().contains("resource exhausted")
                        || errorMessage.toLowerCase().contains("quota")
                        || errorMessage.toLowerCase().contains("rate limit")))) {
                    if (errorMessage != null && !errorMessage.isEmpty()) {
                        throw new IOException("Gemini image API error: " + errorMessage);
                    }
                    throw new IOException("Gemini image API error: Quota exceeded. Please try again later.");
                }

                if (errorMessage != null && !errorMessage.isEmpty()) {
                    throw new IOException("Gemini image API error: " + errorMessage);
                }

                throw new IOException("Gemini image API error: " + response.code() + " - " + response.message());
            }

            String responseStr = response.body() != null ? response.body().string() : "";
            if (responseStr.isEmpty()) {
                throw new IOException("Gemini returned empty image response");
            }

            JSONObject root = new JSONObject(responseStr);
            JSONArray candidates = root.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) {
                throw new IOException("Gemini image response missing candidates");
            }

            JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
            if (content == null) {
                throw new IOException("Gemini image response missing content");
            }

            JSONArray parts = content.optJSONArray("parts");
            if (parts == null || parts.length() == 0) {
                throw new IOException("Gemini image response missing parts");
            }

            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                JSONObject inlineData = part.optJSONObject("inlineData");
                if (inlineData != null) {
                    String mimeType = inlineData.optString("mimeType", "");
                    String data = inlineData.optString("data", "");
                    if (!data.isEmpty() && mimeType.startsWith("image/")) {
                        byte[] bytes = Base64.decode(data, Base64.DEFAULT);
                        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }
                }
            }

            throw new IOException("Gemini image payload missing inline image data");
        }
    }

    /**
     * Extracts a user-friendly error message from Gemini API error response JSON.
     *
     * @param errorBody The raw error response body returned by Gemini
     * @return A user-friendly error message string, or {@code null} if one cannot be extracted
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
            Log.d(TAG, "Could not parse image error response JSON", e);
        }
        return null;
    }
}

