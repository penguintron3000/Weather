package edu.uiuc.cs427app;

import android.app.Application;
import android.util.Log;

import edu.uiuc.cs427app.services.GeminiWeatherInsightsRepository;
import edu.uiuc.cs427app.services.WeatherInsightsRepositoryProvider;

/**
 * Custom Application class for the Weather app.
 * Responsible for initializing global singletons and application-wide configurations.
 */
public class WeatherApplication extends Application {

    private static final String TAG = "WeatherApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize AppTheme singleton with application context
        AppTheme.initialize(this);

        configureWeatherInsights();
    }

    /**
     * Configures the Weather Insights repository to use Gemini backend.
     * Falls back to mock repository if Gemini API key is missing.
     */
    private void configureWeatherInsights() {
        try {
            WeatherInsightsRepositoryProvider.setRepository(new GeminiWeatherInsightsRepository());
            Log.i(TAG, "Gemini Weather Insights repository configured.");
        } catch (IllegalStateException ex) {
            Log.w(TAG, "Gemini API key missing; continuing with mock WeatherInsightsRepository.", ex);
        }
    }
}




