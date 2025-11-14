package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.uiuc.cs427app.services.WeatherService;

public class DetailsActivity extends ThemedActivity implements View.OnClickListener{

    private TextView timeText;
    private TextView dateText;
    private TextView weatherText;
    private TextView temperatureText;
    private TextView windsSpeedText;
    private TextView windsDirectionText;
    private TextView humidityText;
    private Handler handler = new Handler(Looper.getMainLooper());
    private WeatherService weatherService;
    private City currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        timeText = findViewById(R.id.detailsTime);
        dateText = findViewById(R.id.detailsDate);
        weatherText = findViewById(R.id.weatherStatus);
        temperatureText = findViewById(R.id.temperature);
        windsSpeedText = findViewById(R.id.windsSpeed);
        windsDirectionText = findViewById(R.id.windsDirection);
        humidityText = findViewById(R.id.humidity);

        // Process the Intent payload that has opened this Activity and show the information accordingly
        currentCity = getIntent().getParcelableExtra("city");
        String title = currentCity.getDisplayName() + "\n" + currentCity.getState() + ", " + currentCity.getCountryCode();

        int cityNameLength = currentCity.getDisplayName().length();

        // Initializing the GUI elements
        TextView displayName = findViewById(R.id.detailsDisplayName);

        SpannableString spannableString = new SpannableString(title);

        spannableString.setSpan(new AbsoluteSizeSpan(35, true), 0, cityNameLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(20, true), cityNameLength + 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        displayName.setText(spannableString);
        
        // Initialize WeatherService for the city
        weatherService = new WeatherService(currentCity.getDisplayName());
        // Fetch initial weather data
        fetchWeatherData();

        Button buttonMap = findViewById(R.id.detailsMapButton);

        // Sets an onClickListener to launch the MapActivity with the city's location details passed via Intent
        buttonMap.setOnClickListener(v ->{
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("lat", currentCity.getLat());
            intent.putExtra("lon", currentCity.getLon());
            intent.putExtra("city", currentCity.getDisplayName());
            startActivity(intent);
        });

        Button insightsButton = findViewById(R.id.detailsWeatherInsightsButton);
        // Provide a quick jump from the detail page into the Weather Insights flow.
        insightsButton.setOnClickListener(v -> openWeatherInsights(city));

        ConstraintLayout background =  findViewById(R.id.cityViewBackgroundImage);
        //TODO: use LLM to set background here, however you may even create your own runnable if you want to update the image as the weather updates, can use weatherservice api calls

        startRunnables();

    }

    /**
     * Routes the details screen Weather Insights button to the new activity.
     */
    private void openWeatherInsights(City city) {
        Intent intent = new Intent(this, WeatherInsightsActivity.class);
        intent.putExtra("city", city);
        startActivity(intent);
    }

    /**
     * Start all runnables that will update itself by x milliseconds to keep up to date(or minute) information as the page persists
     */
    private void startRunnables(){
        updateTimeAndDate();
        updateWeatherData(); // Single runnable to update all weather data
    }

    /**
     * Live updates to time and date text, checking every second
     * Text font size is also formatted here
     */
    private void updateTimeAndDate() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String currentDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

                timeText.setText(currentTime);
                dateText.setText(currentDate);

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    /**
     * Fetches weather data from the WeatherService API.
     * This method is called initially and can be called to refresh the data.
     * 
     * <p>This method initiates an asynchronous weather data fetch for the current city.
     * The WeatherService will make an API call to OpenWeatherMap and cache the results.
     * The callback methods (onSuccess/onError) are invoked on a background thread,
     * so UI updates must be posted to the main thread using the Handler.
     * 
     * <p>If the WeatherService is not initialized, this method logs an error and returns
     * early to prevent a NullPointerException.
     */
    private void fetchWeatherData() {
        // Early return: Validate that WeatherService is initialized before attempting
        // to fetch weather data. This prevents NullPointerException if the service
        // was not properly set up in onCreate().
        if (weatherService == null) {
            Log.e("DetailsActivity", "WeatherService is null");
            return;
        }
        
        // Initiate asynchronous weather data fetch. The WeatherService.fetchWeatherData()
        // method will handle the network request on a background thread and invoke
        // the appropriate callback method when complete.
        weatherService.fetchWeatherData(new WeatherService.WeatherCallback() {
            /**
             * Called when weather data is successfully fetched and cached by WeatherService.
             * This callback is invoked on a background thread, so UI updates must be
             * posted to the main thread using the Handler to avoid threading violations.
             */
            @Override
            public void onSuccess() {
                // Post UI update to the main thread. The WeatherCallback.onSuccess() is
                // invoked on a background thread, but Android requires all UI operations
                // to be performed on the main/UI thread. The Handler ensures thread safety.
                handler.post(() -> {
                    updateWeatherUI();
                });
            }
            
            /**
             * Called when an error occurs during the weather data fetch.
             * This callback is invoked on a background thread, so UI updates must be
             * posted to the main thread using the Handler.
             * 
             * @param error The exception that occurred during the fetch operation.
             *              Common causes include network failures, API key issues, or
             *              invalid city names.
             */
            @Override
            public void onError(Exception error) {
                // Log the error for debugging purposes. The error message will help
                // identify issues such as missing API keys, network problems, or invalid responses.
                Log.e("DetailsActivity", "Failed to fetch weather data: " + error.getMessage());
                // Update UI with error state on main thread. Display "N/A" for all
                // weather fields to indicate that data is unavailable, providing clear
                // feedback to the user that the weather information could not be loaded.
                handler.post(() -> {
                    weatherText.setText("N/A");
                    temperatureText.setText("N/A");
                    windsSpeedText.setText("N/A");
                    windsDirectionText.setText("N/A");
                    humidityText.setText("N/A");
                });
            }
        });
    }
    
    /**
     * Updates the weather UI elements with data from WeatherService.
     * This should be called on the main thread.
     */
    private void updateWeatherUI() {
        try {
            if (weatherService.isDataAvailable()) {
                // Update weather condition
                String condition = weatherService.getWeatherCondition();
                weatherText.setText(condition);
                
                // Update temperature
                double temp = weatherService.getTemperature();
                temperatureText.setText(String.format("%.0f°F", temp));
                
                // Update wind speed and direction
                double windSpeed = weatherService.getWindSpeed();
                String windDir = weatherService.getWindDirection();
                windsSpeedText.setText(String.format("%.1f mph", windSpeed));
                windsDirectionText.setText(windDir);
                
                // Update humidity
                int humidity = weatherService.getHumidity();
                humidityText.setText(humidity + "%");
            }
        } catch (IllegalStateException e) {
            Log.e("DetailsActivity", "Weather data not available: " + e.getMessage());
        }
    }

    /**
     * Live updates to all weather data, checking every minute.
     * This method refreshes the weather data from the API and updates all weather-related UI elements.
     */
    private void updateWeatherData() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Refresh weather data from API (only one API call per minute)
                if (weatherService != null) {
                    weatherService.refreshWeatherData(new WeatherService.WeatherCallback() {
                        @Override
                        public void onSuccess() {
                            // Update all weather UI elements on main thread
                            handler.post(() -> {
                                updateWeatherUI();
                            });
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            Log.e("DetailsActivity", "Failed to refresh weather data: " + error.getMessage());
                            // Optionally show error state in UI
                        }
                    });
                }

                handler.postDelayed(this, 60000); // Update every minute
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onClick(View view) {
        //Implement this (create an Intent that goes to a new Activity, which shows the map)
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up WeatherService resources
        if (weatherService != null) {
            weatherService.shutdown();
        }
    }
}
