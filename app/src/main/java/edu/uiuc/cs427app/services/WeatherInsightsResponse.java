package edu.uiuc.cs427app.services;

import java.util.List;

/**
 * Represents a single Weather Insights exchange response plus follow-up questions.
 */
public class WeatherInsightsResponse {

    private final String reply;
    private final List<String> followUpQuestions;

    /**
     * Creates a response snapshot returned by the LLM service.
     */
    public WeatherInsightsResponse(String reply, List<String> followUpQuestions) {
        this.reply = reply;
        this.followUpQuestions = followUpQuestions;
    }

    /**
     * Provides the textual guidance the LLM returned for the selected question.
     */
    public String getReply() {
        return reply;
    }

    /**
     * Provides the follow-up questions the LLM suggested after the answer.
     */
    public List<String> getFollowUpQuestions() {
        return followUpQuestions;
    }
}
