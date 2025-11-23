package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Tests the real API with activity-bound functionality, only navigation is excludedgit 
 */
public class WeatherInsightsAPITest {

    public final int EXPECTED_QUESTION_COUNT = 3;
    //random user id, make sure to change it along with USER_ID_STR or just dynamically do it
    public static final long USER_ID = 57364L;
    public static final String USER_ID_STR = "57364";
    public static final City EXAMPLE_CITY = new City(USER_ID,USER_ID_STR, "ChIJOwg_06VPwokRYv534QaPC8g", "New York City", "New York",
            "USA", 40.7128, -74.0060);
    public static final int timeToWait = 10000;

    /**
     * Creates weatherInsightsActivity intent
     * @return intent containing city for weatherInsightsActivity
     */
    private static Intent createWeatherInsightsActivityIntent() {
        // Get the context of the application under test.
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();


        // Create the intent and put the City object as an extra.
        Intent intent = new Intent(targetContext, WeatherInsightsActivity.class);
        intent.putExtra("city", EXAMPLE_CITY);
        return intent;
    }

    @Before
    public void setUp() {
        User.getInstance().clear();
        User.getInstance().init(USER_ID, "testuser", null);
        Intents.init();

        //WeatherInsightsRepositoryProvider.setRepository(new MockWeatherInsightsRepository());
    }

    @After
    public void tearDown() {
        Intents.release();
        User.getInstance().clear();

        //WeatherInsightsRepositoryProvider.setRepository(null);
    }


    /**
     * Tests for initial 3+ questions.
     * Tests for uniqueness between all questions and responses
     * Tests for 2+ questions and a response whenever a question button is pressed.
     */
    @Test
    public void testClickingQuestionGeneratesNewQuestions() {
        String sampleResponse = "Sample response from the Weather Insights LLM will appear here."; //make sure new responses aren't this
        try(ActivityScenario<WeatherInsightsActivity> scenario = ActivityScenario.launch(createWeatherInsightsActivityIntent())) {
            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException e) {
                //ignore
            }
            List<String> initialQuestions = getTextsFromContainer(scenario, R.id.weatherInsightsQuestionsContainer);

            assertEquals(EXPECTED_QUESTION_COUNT, initialQuestions.size());

            String questionToClick = initialQuestions.get(0);

            onView(ViewMatchers.withText(questionToClick)).perform(click());

            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException ie) {
                // ignore
            }

            List<String> newQuestions = getTextsFromContainer(scenario, R.id.weatherInsightsQuestionsContainer);
            String response = getText(scenario, R.id.weatherInsightsResponse);
            assertNotEquals(sampleResponse, response);

            assertTrue(EXPECTED_QUESTION_COUNT - 1 <= newQuestions.size()); //at least 2 according to prompt

            HashSet<String> combinedSet = new HashSet<>(initialQuestions);
            combinedSet.addAll(newQuestions);

            assertEquals(combinedSet.size(), initialQuestions.size() + newQuestions.size());

            //repeating the test once more by clicking the "question button" and applying assertions again
            String anotherQuestionToClick = newQuestions.get(0);

            onView(ViewMatchers.withText(anotherQuestionToClick)).perform(click());

            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException ie) {
                // ignore
            }

            List<String> moreNewQuestions = getTextsFromContainer(scenario, R.id.weatherInsightsQuestionsContainer);
            response = getText(scenario, R.id.weatherInsightsResponse);
            assertNotEquals(sampleResponse, response);

            assertTrue(EXPECTED_QUESTION_COUNT - 1 <= moreNewQuestions.size());

            combinedSet.addAll(moreNewQuestions);
            assertEquals(combinedSet.size(), initialQuestions.size() + newQuestions.size() + moreNewQuestions.size());

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                // ignore
            }
        }
    }

    /**
     * Extracts from text of children from container
     * @param scenario Scenario layout to scour element id's for
     * @param containerId element id to scour for
     * @return Texts from each child of the parent containerId
     */
    public static List<String> getTextsFromContainer(ActivityScenario<?> scenario, int containerId) {
        List<String> texts = new ArrayList<>();

        scenario.onActivity(activity -> {
            View root = activity.findViewById(containerId);
            if (root instanceof ViewGroup) {
                extractTexts((ViewGroup) root, texts);
            }
        });

        return texts;
    }

    /**
     * Extracts text from element
     * @param group ViewGroup object to extract text from
     * @param texts Extracted text from curent ViewGroup
     */
    private static void extractTexts(ViewGroup group, List<String> texts) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View v = group.getChildAt(i);

            if (v instanceof TextView) {
                texts.add(((TextView) v).getText().toString());
            }
            else if (v instanceof ViewGroup) {
                extractTexts((ViewGroup) v, texts);
            }
        }
    }

    /**
     * Extracts text from element
     * @param scenario Scenario layout to scour element id's for
     * @param viewId element id to extract text from
     * @return Text from element
     */
    public static String getText(ActivityScenario<?> scenario, int viewId) {
        final String[] text = {null}; //awkward but has to be instantiated this way as text has to be declared final

        scenario.onActivity(activity -> {
            View view = activity.findViewById(viewId);
            if (view instanceof TextView) {
                text[0] = ((TextView) view).getText().toString();
            }
        });

        return text[0];
    }
}
