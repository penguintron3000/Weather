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

    public double getTemperatureFahrenheit() {
        return temperatureFahrenheit;
    }

    public String getConditionDescription() {
        return conditionDescription;
    }

    public int getHumidityPercentage() {
        return humidityPercentage;
    }

    public double getWindSpeedMph() {
        return windSpeedMph;
    }

    @Nullable
    public String getWindDirectionCardinal() {
        return windDirectionCardinal;
    }

    @Nullable
    public String getObservationSummary() {
        return observationSummary;
    }
}

