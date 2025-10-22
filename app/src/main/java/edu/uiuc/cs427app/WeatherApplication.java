package edu.uiuc.cs427app;

import android.app.Application;

/**
 * Custom Application class for the Weather app.
 * Responsible for initializing global singletons and application-wide configurations.
 */
public class WeatherApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize AppTheme singleton with application context
        AppTheme.initialize(this);
    }
}



