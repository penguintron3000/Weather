package edu.uiuc.cs427app.services;

import java.util.List;

import edu.uiuc.cs427app.City;

/**
 * Contract that the Weather Insights front-end relies on for LLM interactions.
 */
public interface WeatherInsightsRepository {

    /**
     * Callback for receiving initial LLM-generated questions.
     */
    interface QuestionsCallback {
        void onSuccess(List<String> questions);
        void onError(Exception exception);
    }

    /**
     * Callback for receiving the LLM response plus refreshed questions.
     */
    interface ResponseCallback {
        void onSuccess(WeatherInsightsResponse response);
        void onError(Exception exception);
    }

    /**
     * Requests the starting set of suggested questions for the given city.
     */
    void fetchInitialQuestions(City city, QuestionsCallback callback);

    /**
     * Sends the selected question and returns an LLM reply plus follow-ups.
     */
    void askQuestion(City city, String prompt, ResponseCallback callback);
}
