package edu.uiuc.cs427app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.List;

import edu.uiuc.cs427app.services.WeatherInsightsRepository;
import edu.uiuc.cs427app.services.WeatherInsightsRepositoryProvider;
import edu.uiuc.cs427app.services.WeatherInsightsResponse;

/**
 * WeatherInsightsActivity displays dynamic LLM questions and responses for a selected city.
 */
public class WeatherInsightsActivity extends ThemedActivity {

    private City city;
    private WeatherInsightsRepository repository;

    private LinearLayout questionsContainer;
    private ProgressBar progressBar;
    private TextView responseLabel;
    private TextView responseText;
    private TextView cityNameText;

    /**
     * Sets up the Weather Insights screen and kicks off the initial question fetch.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_insights);

        city = getIntent().getParcelableExtra("city");
        if (city == null) {
            Toast.makeText(this, R.string.weather_insights_missing_city, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        repository = WeatherInsightsRepositoryProvider.getRepository();

        questionsContainer = findViewById(R.id.weatherInsightsQuestionsContainer);
        progressBar = findViewById(R.id.weatherInsightsProgress);
        responseLabel = findViewById(R.id.weatherInsightsResponseLabel);
        responseText = findViewById(R.id.weatherInsightsResponse);
        cityNameText = findViewById(R.id.weatherInsightsCityName);

        cityNameText.setText(buildCityLabel());

        loadInitialQuestions();
    }

    /**
     * Builds the formatted city label shown underneath the header.
     */
    private String buildCityLabel() {
        StringBuilder builder = new StringBuilder(city.getDisplayName());
        if (city.getState() != null && !city.getState().trim().isEmpty()) {
            builder.append(", ").append(city.getState());
        }
        if (city.getCountryCode() != null && !city.getCountryCode().trim().isEmpty()) {
            builder.append(", ").append(city.getCountryCode());
        }
        return builder.toString();
    }

    /**
     * Requests the first set of questions from the repository and renders them.
     */
    private void loadInitialQuestions() {
        showLoading(true);
        repository.fetchInitialQuestions(city, new WeatherInsightsRepository.QuestionsCallback() {
            @Override
            public void onSuccess(List<String> questions) {
                runOnUiThread(() -> {
                    showLoading(false);
                    renderQuestions(questions);
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> handleError(exception));
            }
        });
    }

    /**
     * Handles the tap on a question button by asking the repository for a response.
     */
    private void onQuestionSelected(String question) {
        showLoading(true);
        repository.askQuestion(city, question, new WeatherInsightsRepository.ResponseCallback() {
            @Override
            public void onSuccess(WeatherInsightsResponse response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showResponse(response.getReply());
                    renderQuestions(response.getFollowUpQuestions());
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> handleError(exception));
            }
        });
    }

    /**
     * Displays the latest response text and ensures the label is visible.
     */
    private void showResponse(String reply) {
        responseLabel.setVisibility(View.VISIBLE);
        responseText.setVisibility(View.VISIBLE);
        responseText.setText(reply);
    }

    /**
     * Regenerates the question buttons to match the latest suggestions.
     */
    private void renderQuestions(List<String> questions) {
        questionsContainer.removeAllViews();
        if (questions == null || questions.isEmpty()) {
            TextView placeholder = new TextView(this);
            placeholder.setText(R.string.weather_insights_loading_error);
            questionsContainer.addView(placeholder);
            applyThemeToView(placeholder);
            return;
        }

        int margin = getResources().getDimensionPixelSize(R.dimen.weather_insights_button_margin_vertical);

        for (String question : questions) {
            Button button = new Button(this);
            button.setText(question);
            button.setAllCaps(false);
            button.setOnClickListener(v -> onQuestionSelected(question));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = margin;
            button.setLayoutParams(params);

            questionsContainer.addView(button);
            applyThemeToView(button);
        }
    }

    /**
     * Shows or hides the loading indicator and disables question buttons while loading.
     */
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        setQuestionButtonsEnabled(!isLoading);
    }

    /**
     * Enables/disables all current question buttons to avoid duplicate taps.
     */
    private void setQuestionButtonsEnabled(boolean enabled) {
        for (int i = 0; i < questionsContainer.getChildCount(); i++) {
            View child = questionsContainer.getChildAt(i);
            child.setEnabled(enabled);
            child.setAlpha(enabled ? 1f : 0.6f);
        }
    }

    /**
     * Handles any repository errors by notifying the user and clearing the spinner.
     */
    private void handleError(Exception exception) {
        showLoading(false);
        Toast.makeText(this, R.string.weather_insights_loading_error, Toast.LENGTH_SHORT).show();
    }
}
