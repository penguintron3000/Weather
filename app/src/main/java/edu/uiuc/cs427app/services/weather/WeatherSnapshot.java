package edu.uiuc.cs427app.services.weather;

import androidx.annotation.Nullable;

/**
 * Immutable snapshot of the current weather conditions used to seed Gemini prompts.
 */
public class WeatherSnapshot {

    private final double temperatureFahrenheit;
    private final String conditionDescription;
    private final int humidityPercentage;
    private final double windSpeedMph;
    @Nullable
    private final String windDirectionCardinal;
    @Nullable
    private final String observationSummary;

    /**
     * Constructs a new WeatherSnapshot with the specified weather conditions.
     * 
     * @param temperatureFahrenheit The current temperature in degrees Fahrenheit
     * @param conditionDescription A human-readable description of the weather condition (i.e. "Partly Cloudy", "Sunny", "Rainy")
     * @param humidityPercentage The relative humidity as a percentage (0-100)
     * @param windSpeedMph The wind speed in miles per hour
     * @param windDirectionCardinal The wind direction in cardinal format (i.e. "N", "NE", "SW"), or null if not available
     * @param observationSummary Optional summary or notes about the weather observation, or null if not available
     */
    public WeatherSnapshot(
            double temperatureFahrenheit,
            String conditionDescription,
            int humidityPercentage,
            double windSpeedMph,
            @Nullable String windDirectionCardinal,
            @Nullable String observationSummary
    ) {
        this.temperatureFahrenheit = temperatureFahrenheit;
        this.conditionDescription = conditionDescription;
        this.humidityPercentage = humidityPercentage;
        this.windSpeedMph = windSpeedMph;
        this.windDirectionCardinal = windDirectionCardinal;
        this.observationSummary = observationSummary;
    }

    /**
     * Returns the current temperature in degrees Fahrenheit.
     * 
     * @return The temperature in Fahrenheit
     */
    public double getTemperatureFahrenheit() {
        return temperatureFahrenheit;
    }

    /**
     * Returns a human-readable description of the current weather condition.
     * 
     * @return The weather condition description (i.e. "Partly Cloudy", "Sunny", "Rainy")
     */
    public String getConditionDescription() {
        return conditionDescription;
    }

    /**
     * Returns the relative humidity as a percentage.
     * 
     * @return The humidity percentage (0-100)
     */
    public int getHumidityPercentage() {
        return humidityPercentage;
    }

    /**
     * Returns the wind speed in miles per hour.
     * 
     * @return The wind speed in mph
     */
    public double getWindSpeedMph() {
        return windSpeedMph;
    }

    /**
     * Returns the wind direction in cardinal format.
     * 
     * @return The wind direction (i.e. "N", "NE", "SW"), or null if not available
     */
    @Nullable
    public String getWindDirectionCardinal() {
        return windDirectionCardinal;
    }

    /**
     * Returns optional summary or notes about the weather observation.
     * This may include information about the data source or any additional context.
     * 
     * @return The observation summary, or null if not available
     */
    @Nullable
    public String getObservationSummary() {
        return observationSummary;
    }
}

