package edu.uiuc.cs427app.services;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import edu.uiuc.cs427app.City;

/**
 * Lightweight mock implementation so the UI can run before the real LLM hook exists.
 */
public class MockWeatherInsightsRepository implements WeatherInsightsRepository {

    private static final long SIMULATED_DELAY_MS = 600L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    /**
     * Simulates downloading the first batch of questions.
     */
    @Override
    public void fetchInitialQuestions(City city, QuestionsCallback callback) {
        handler.postDelayed(() -> callback.onSuccess(buildInitialQuestions(city)), SIMULATED_DELAY_MS);
    }

    /**
     * Simulates the LLM answering a picked question with new follow-ups.
     */
    @Override
    public void askQuestion(City city, String prompt, ResponseCallback callback) {
        handler.postDelayed(() -> callback.onSuccess(
                new WeatherInsightsResponse(
                        buildResponse(city, prompt),
                        buildFollowUpQuestions(city)
                )
        ), SIMULATED_DELAY_MS);
    }

    /**
     * Crafts a deterministic starter set of questions for demo purposes.
     */
    private List<String> buildInitialQuestions(City city) {
        String cityName = city != null ? city.getDisplayName() : "your city";
        return Arrays.asList(
                String.format(Locale.getDefault(), "What should I wear in %s today?", cityName),
                String.format(Locale.getDefault(), "Is it a good day for outdoor plans in %s?", cityName),
                String.format(Locale.getDefault(), "How should I prepare for the commute in %s?", cityName)
        );
    }

    /**
     * Crafts a pseudo-response string mimicking an LLM answer.
     */
    private String buildResponse(City city, String prompt) {
        String cityName = city != null ? city.getDisplayName() : "your city";
        return String.format(
                Locale.getDefault(),
                "Here's a quick tip for %s: %s. Expect mild temperatures with a light breeze, so layer accordingly and keep an eye out for quick showers later today.",
                cityName,
                prompt
        );
    }

    /**
     * Builds new follow-up questions so the UI always has fresh prompts.
     */
    private List<String> buildFollowUpQuestions(City city) {
        String cityName = city != null ? city.getDisplayName() : "your city";
        List<String> pool = new ArrayList<>(Arrays.asList(
                String.format(Locale.getDefault(), "Will rain affect evening plans in %s?", cityName),
                String.format(Locale.getDefault(), "Do I need sunscreen around noon in %s?", cityName),
                String.format(Locale.getDefault(), "Should commuters expect delays in %s?", cityName),
                String.format(Locale.getDefault(), "What should I pack for a workout in %s?", cityName),
                String.format(Locale.getDefault(), "Is it safe for outdoor events in %s tonight?", cityName)
        ));

        List<String> followUps = new ArrayList<>();
        while (followUps.size() < 3 && !pool.isEmpty()) {
            int index = random.nextInt(pool.size());
            followUps.add(pool.remove(index));
        }

        if (followUps.size() < 2) {
            followUps.clear();
            followUps.add(String.format(Locale.getDefault(), "How windy will it get in %s?", cityName));
            followUps.add(String.format(Locale.getDefault(), "Do kids need rain gear in %s?", cityName));
        }

        return followUps;
    }
}
