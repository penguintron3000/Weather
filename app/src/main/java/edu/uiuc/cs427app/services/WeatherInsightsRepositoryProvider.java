package edu.uiuc.cs427app.services;

/**
 * Central place to grab or override the active WeatherInsightsRepository implementation.
 */
public final class WeatherInsightsRepositoryProvider {

    private static WeatherInsightsRepository repository;

    private WeatherInsightsRepositoryProvider() {
        // Utility holder
    }

    /**
     * Supplies the current repository, defaulting to the mock if nothing is registered.
     */
    public static WeatherInsightsRepository getRepository() {
        if (repository == null) {
            repository = new MockWeatherInsightsRepository();
        }
        return repository;
    }

    /**
     * Allows the LLM team to swap in their concrete repository at runtime.
     */
    public static void setRepository(WeatherInsightsRepository customRepository) {
        repository = customRepository;
    }
}
