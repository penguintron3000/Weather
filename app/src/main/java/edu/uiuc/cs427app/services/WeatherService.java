package edu.uiuc.cs427app.services;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uiuc.cs427app.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * WeatherService provides weather data for a specific city.
 * The weather data is fetched once when the service is initialized and cached
 * to avoid multiple API calls. This class serves as the weather API for the rest
 * of the team to use.
 * 
 * Usage:
 *   WeatherService weather = new WeatherService("Chicago");
 *   weather.fetchWeatherData(() -> {
 *       // Weather data is now available
 *       double temp = weather.getTemperature();
 *       String condition = weather.getWeatherCondition();
 *       int humidity = weather.getHumidity();
 *       double windSpeed = weather.getWindSpeed();
 *       String windDir = weather.getWindDirection();
 *   });
 */
public class WeatherService {
    private static final String TAG = "WeatherService";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    
    private final String city;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    // Cached weather data
    private Double temperature = null;
    private String weatherCondition = null;
    private Integer humidity = null;
    private String windCondition = null; // Combined format for backward compatibility
    private Double windSpeed = null; // Wind speed in mph
    private String windDirection = null; // Wind direction (e.g., "NW", "SW")
    private boolean dataFetched = false;
    private Exception fetchError = null;
    
    /**
     * Callback interface for handling weather data fetch results.
     * Methods are invoked on a background thread; UI updates must be done on the UI thread.
     */
    public interface WeatherCallback {
        /**
         * Invoked when weather data fetch succeeds.
         * All getter methods will now return valid data.
         */
        void onSuccess();
        
        /**
         * Invoked when weather data fetch fails.
         * 
         * @param error The exception that occurred during the fetch
         */
        void onError(Exception error);
    }
    
    /**
     * Creates a WeatherService for the specified city.
     * Note: The weather data is not automatically fetched. Call fetchWeatherData()
     * to retrieve and cache the weather data.
     * 
     * @param city The name of the city to get weather data for
     */
    public WeatherService(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }
        this.city = city.trim();
    }
    
    /**
     * Ensures the executor service is available. If it has been shut down,
     * creates a new executor instance. This allows the service to be reused
     * even after shutdown() has been called.
     */
    private void ensureExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
    }
    
    /**
     * Fetches weather data from the OpenWeatherMap API asynchronously.
     * The data is cached after a successful fetch, so subsequent calls to getter
     * methods will return the cached data without making additional API calls.
     * 
     * <p>This method uses a background executor thread to perform the network request
     * asynchronously. The callback methods (onSuccess/onError) are invoked on the
     * background thread, so any UI updates must be posted to the UI thread.
     * 
     * <p>If weather data has already been successfully fetched and cached, the callback
     * is invoked immediately without making another API call. If a previous fetch failed,
     * a new fetch attempt will be made.
     * 
     * @param callback The callback to handle success or error results (invoked on background thread)
     * 
     * @see WeatherCallback#onSuccess() Called when weather data is successfully fetched
     * @see WeatherCallback#onError(Exception) Called when an error occurs during fetch
     */
    public void fetchWeatherData(WeatherCallback callback) {
        // Ensure executor is available (recreate if it was shut down)
        ensureExecutor();
        
        // Early return optimization: If data was already successfully fetched and cached,
        // invoke the success callback immediately on the executor thread to maintain
        // consistent threading behavior, avoiding unnecessary API calls.
        if (dataFetched && fetchError == null) {
            // Data already fetched successfully, invoke callback immediately
            executor.execute(() -> callback.onSuccess());
            return;
        }
        
        // Validate that the API key is configured before attempting the network request.
        // The API key should be defined in local.properties and will be available via BuildConfig.
        final String apiKey = BuildConfig.WEATHER_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            // Create an IllegalStateException with a clear error message indicating
            // that the API key needs to be configured in local.properties.
            Exception error = new IllegalStateException(
                "Weather API key missing. Define WEATHER_API_KEY in local.properties.");
            // Store the error so it can be retrieved later via getLastError()
            fetchError = error;
            // Execute the error callback on the background thread to maintain
            // consistent threading behavior with the success path.
            // Note: ensureExecutor() was already called above, so executor is available
            executor.execute(() -> callback.onError(error));
            return;
        }
        
        // Execute the actual network request on a background thread to avoid blocking
        // the calling thread. This lambda runs asynchronously and handles both success
        // and error cases, updating the internal state accordingly.
        executor.execute(() -> {
            try {
                // Fetch weather data synchronously (this is safe because we're already
                // on a background thread). This will parse and cache the weather data.
                fetchWeatherDataSync();
                // Clear any previous error state since the fetch succeeded
                fetchError = null;
                // Notify the callback that the data is now available
                callback.onSuccess();
            } catch (Exception e) {
                // Store the exception for later retrieval and logging
                fetchError = e;
                Log.e(TAG, "Weather fetch error: " + e.getMessage(), e);
                // Notify the callback about the error
                callback.onError(e);
            }
        });
    }
    
    /**
     * Fetches weather data synchronously. This method should be called from a background thread.
     * The data is cached after a successful fetch.
     * 
     * <p>This method performs the actual HTTP request to the OpenWeatherMap API and parses
     * the JSON response. It should only be called from a background thread as it performs
     * blocking network I/O operations.
     * 
     * <p>If weather data has already been successfully fetched, this method returns immediately
     * without making another API call, using the cached data.
     * 
     * @throws IOException If the network request fails or the API returns a non-successful response
     * @throws JSONException If the response cannot be parsed or required fields are missing
     * @throws IllegalStateException If the API key is missing or the response is invalid/empty
     */
    public void fetchWeatherDataSync() throws IOException, JSONException {
        // Early return optimization: If data was already successfully fetched and cached,
        // skip the network request and return immediately. This prevents redundant API calls
        // and reduces unnecessary network traffic and API quota usage.
        if (dataFetched && fetchError == null) {
            // Data already fetched successfully, return immediately
            return;
        }
        
        // Validate that the API key is configured. The API key is required for all
        // OpenWeatherMap API requests. It should be defined in local.properties as
        // WEATHER_API_KEY and will be available via BuildConfig at build time.
        final String apiKey = BuildConfig.WEATHER_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            // Throw an IllegalStateException with a descriptive error message that
            // guides developers to configure the API key in local.properties.
            throw new IllegalStateException(
                "Weather API key missing. Define WEATHER_API_KEY in local.properties.");
        }
        
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        
        // Build URL with query parameters
        String url = BASE_URL + "?q=" + city + "&appid=" + apiKey + "&units=imperial";
        String maskedKey = apiKey.length() > 8 ? (apiKey.substring(0, 8) + "…") : "(set)";
        Log.i(TAG, "Weather request URL: " + BASE_URL + "?q=" + city + "&appid=" + maskedKey + "&units=imperial");
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String err = response.body() != null ? response.body().string() : "";
                Log.e(TAG, "Weather API non-200 response: code=" + response.code() + ", message=" + response.message());
                Log.e(TAG, "Weather API error body: " + err);
                throw new IOException("Weather API error: " + response.code() + " - " + response.message());
            }
            
            String responseStr = response.body() != null ? response.body().string() : "";
            Log.i(TAG, "Weather API raw response: " + responseStr);
            
            if (responseStr.isEmpty()) {
                throw new IllegalStateException("Empty response from Weather API");
            }
            
            // Parse JSON response
            JSONObject root = new JSONObject(responseStr);
            parseWeatherData(root);
            dataFetched = true;
            Log.i(TAG, "Weather data fetched and cached for city: " + city);
        }
    }
    
    /**
     * Parses the weather data from the OpenWeatherMap API JSON response.
     * 
     * @param root The root JSON object from the API response
     * @throws JSONException If required fields are missing from the response
     */
    private void parseWeatherData(JSONObject root) throws JSONException {
        // Parse temperature (in Fahrenheit, since we use units=imperial)
        if (root.has("main") && root.getJSONObject("main").has("temp")) {
            this.temperature = root.getJSONObject("main").getDouble("temp");
        } else {
            throw new JSONException("Missing temperature data in API response");
        }
        
        // Parse weather condition (description)
        if (root.has("weather") && root.getJSONArray("weather").length() > 0) {
            JSONObject weatherObj = root.getJSONArray("weather").getJSONObject(0);
            if (weatherObj.has("main")) {
                this.weatherCondition = weatherObj.getString("main");
            } else if (weatherObj.has("description")) {
                this.weatherCondition = weatherObj.getString("description");
            } else {
                throw new JSONException("Missing weather condition data in API response");
            }
        } else {
            throw new JSONException("Missing weather condition data in API response");
        }
        
        // Parse humidity
        if (root.has("main") && root.getJSONObject("main").has("humidity")) {
            this.humidity = root.getJSONObject("main").getInt("humidity");
        } else {
            throw new JSONException("Missing humidity data in API response");
        }
        
        // Parse wind condition (speed and direction)
        StringBuilder windBuilder = new StringBuilder();
        if (root.has("wind")) {
            JSONObject windObj = root.getJSONObject("wind");
            if (windObj.has("speed")) {
                this.windSpeed = windObj.getDouble("speed");
                windBuilder.append(String.format("%.1f mph", this.windSpeed));
            }
            if (windObj.has("deg")) {
                int deg = windObj.getInt("deg");
                this.windDirection = getWindDirection(deg);
                if (windBuilder.length() > 0) {
                    windBuilder.append(", ");
                }
                windBuilder.append(this.windDirection);
            }
        }
        
        if (windBuilder.length() > 0) {
            this.windCondition = windBuilder.toString();
        } else {
            // Fallback if wind data is missing
            this.windCondition = "No wind data available";
            this.windSpeed = 0.0;
            this.windDirection = "N/A";
        }
    }
    
    /**
     * Converts wind direction in degrees to a cardinal direction string.
     * 
     * @param degrees Wind direction in degrees (0-360)
     * @return A string representing the cardinal direction
     */
    private String getWindDirection(int degrees) {
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) Math.round(degrees / 22.5) % 16;
        return directions[index];
    }
    
    /**
     * Gets the current temperature in Fahrenheit.
     * 
     * @return The temperature
     * @throws IllegalStateException If weather data has not been fetched successfully
     */
    public double getTemperature() {
        if (!dataFetched || temperature == null) {
            throw new IllegalStateException(
                "Weather data not available. Call fetchWeatherData() first.");
        }
        return temperature;
    }
    
    /**
     * Gets the current weather condition (e.g., "Clear", "Clouds", "Rain", etc.).
     * 
     * @return The weather condition
     * @throws IllegalStateException If weather data has not been fetched successfully
     */
    public String getWeatherCondition() {
        if (!dataFetched || weatherCondition == null) {
            throw new IllegalStateException(
                "Weather data not available. Call fetchWeatherData() first.");
        }
        return weatherCondition;
    }
    
    /**
     * Gets the current humidity percentage.
     * 
     * @return The humidity percentage (0-100)
     * @throws IllegalStateException If weather data has not been fetched successfully
     */
    public int getHumidity() {
        if (!dataFetched || humidity == null) {
            throw new IllegalStateException(
                "Weather data not available. Call fetchWeatherData() first.");
        }
        return humidity;
    }
    
    /**
     * Gets the current wind condition (speed and direction).
     * 
     * @return A string describing the wind condition (e.g., "10.5 mph, SW")
     * @throws IllegalStateException If weather data has not been fetched successfully
     */
    public String getWindCondition() {
        if (!dataFetched || windCondition == null) {
            throw new IllegalStateException(
                "Weather data not available. Call fetchWeatherData() first.");
        }
        return windCondition;
    }
    
    /**
     * Gets the current wind speed in miles per hour.
     * 
     * @return The wind speed in mph
     * @throws IllegalStateException If weather data has not been fetched successfully
     */
    public double getWindSpeed() {
        if (!dataFetched || windSpeed == null) {
            throw new IllegalStateException(
                "Weather data not available. Call fetchWeatherData() first.");
        }
        return windSpeed;
    }
    
    /**
     * Gets the current wind direction as a cardinal direction (e.g., "NW", "SW", "E").
     * 
     * @return The wind direction
     * @throws IllegalStateException If weather data has not been fetched successfully
     */
    public String getWindDirection() {
        if (!dataFetched || windDirection == null) {
            throw new IllegalStateException(
                "Weather data not available. Call fetchWeatherData() first.");
        }
        return windDirection;
    }
    
    /**
     * Gets the city name this service is configured for.
     * 
     * @return The city name
     */
    public String getCity() {
        return city;
    }
    
    /**
     * Checks if weather data has been successfully fetched and cached.
     * 
     * @return true if data is available, false otherwise
     */
    public boolean isDataAvailable() {
        return dataFetched && fetchError == null;
    }
    
    /**
     * Gets the error that occurred during the last fetch attempt, if any.
     * 
     * @return The exception, or null if no error occurred
     */
    public Exception getLastError() {
        return fetchError;
    }
    
    /**
     * Forces a refresh of the weather data by clearing the cache and fetching again.
     * 
     * @param callback The callback to handle success or error results
     */
    public void refreshWeatherData(WeatherCallback callback) {
        dataFetched = false;
        temperature = null;
        weatherCondition = null;
        humidity = null;
        windCondition = null;
        windSpeed = null;
        windDirection = null;
        fetchError = null;
        fetchWeatherData(callback);
    }
    
    /**
     * Shuts down the executor service. Call this when the service is no longer needed
     * to free up resources. Note that the executor will be automatically recreated
     * if fetchWeatherData() or refreshWeatherData() is called after shutdown.
     */
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}

