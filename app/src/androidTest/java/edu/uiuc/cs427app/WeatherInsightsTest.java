package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.uiuc.cs427app.db.CityContract;

/**
 * Tests for complete WeatherInsightsActivity navigation and functionality.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WeatherInsightsTest extends BaseAndroidTest {

    public final int EXPECTED_INITIAL_QUESTION_COUNT = 3; //according to prompt "at least 3" from GeminiWeatherInsightsRepository
    public final int EXPECTED_QUESTION_COUNT = 2;//according to prompt "at least 2"
    //random user id, make sure to change it along with USER_ID_STR or just dynamically do it
    public static final long USER_ID = 57364L;
    public static final String USER_ID_STR = "57364";
    public static final City EXAMPLE_CITY = new City(USER_ID,USER_ID_STR, "ChIJOwg_06VPwokRYv534QaPC8g", "New York City", "New York",
            "USA", 40.7128, -74.0060);
    public static final int timeToWait = 15000; // api waiting time, recommend minimum 10 seconds unless you got fast internet
    public static final int transitionTime = 4000; //transition time between tests

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
     */
    @Test
    public void testQuestionsAppearAndAreUnique() {
        try(ActivityScenario<WeatherInsightsActivity> scenario = ActivityScenario.launch(createWeatherInsightsActivityIntent())) {
            sleep(timeToWait);
            List<String> questions = getTextsFromContainer(scenario, R.id.weatherInsightsQuestionsContainer);

            assertTrue(EXPECTED_INITIAL_QUESTION_COUNT <= questions.size()); //at least 3 according to prompt
            assertEquals(questions.size(), new HashSet<>(questions).size());

            sleep(transitionTime);
        }
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
            sleep(timeToWait);
            List<String> initialQuestions = getTextsFromContainer(scenario, R.id.weatherInsightsQuestionsContainer);

            assertEquals(EXPECTED_INITIAL_QUESTION_COUNT, initialQuestions.size());

            String questionToClick = initialQuestions.get(0);

            onView(ViewMatchers.withText(questionToClick)).perform(click());

            sleep(timeToWait);

            List<String> newQuestions = getTextsFromContainer(scenario, R.id.weatherInsightsQuestionsContainer);

            //checks for response and that it's not the sample response
            String response = getText(scenario, R.id.weatherInsightsResponse);
            assertNotEquals(sampleResponse, response);

            assertTrue(EXPECTED_QUESTION_COUNT <= newQuestions.size()); //at least 2 according to prompt

            HashSet<String> combinedSet = new HashSet<>(initialQuestions);
            combinedSet.addAll(newQuestions);

            //checks for dupes by checking dynamic sizes between sets and arrays
            assertEquals(combinedSet.size(), initialQuestions.size() + newQuestions.size());

            //repeating the test once more by clicking the "question button" and applying assertions again
            String anotherQuestionToClick = newQuestions.get(0);

            onView(ViewMatchers.withText(anotherQuestionToClick)).perform(click());

            sleep(timeToWait);

            List<String> moreNewQuestions = getTextsFromContainer(scenario, R.id.weatherInsightsQuestionsContainer);

            response = getText(scenario, R.id.weatherInsightsResponse);
            assertNotEquals(sampleResponse, response);

            assertTrue(EXPECTED_QUESTION_COUNT <= moreNewQuestions.size());

            combinedSet.addAll(moreNewQuestions);
            assertEquals(combinedSet.size(), initialQuestions.size() + newQuestions.size() + moreNewQuestions.size());

            sleep(transitionTime);
        }
    }

    /**
     * Tests for navigation from MainActivity to WeatherInsightsActivity. Uses actual database but
     * removes record right after test
     */
    @Test
    public void mainActivity_to_weatherInsightsActivity() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(CityContract.CityEntry.COLUMN_CITY_ID, EXAMPLE_CITY.getId());
        values.put(CityContract.CityEntry.COLUMN_USER_ID, USER_ID_STR);
        values.put(CityContract.CityEntry.COLUMN_PLACE_ID, EXAMPLE_CITY.getPlaceId());
        values.put(CityContract.CityEntry.COLUMN_DISPLAY_NAME, EXAMPLE_CITY.getDisplayName());
        values.put(CityContract.CityEntry.COLUMN_STATE, EXAMPLE_CITY.getState());
        values.put(CityContract.CityEntry.COLUMN_COUNTRY_CODE, EXAMPLE_CITY.getCountryCode());
        values.put(CityContract.CityEntry.COLUMN_LAT, EXAMPLE_CITY.getLat());
        values.put(CityContract.CityEntry.COLUMN_LON, EXAMPLE_CITY.getLon());

        Uri uri = resolver.insert(CityContract.CONTENT_URI, values);

        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), MainActivity.class);
        intent.putExtra("city", EXAMPLE_CITY);
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)){
            sleep(4000);
            onView(withId(R.id.weather_insights_button)).perform(click());

            sleep(4000);

            Intents.intended(
                    allOf(
                            IntentMatchers.hasComponent(WeatherInsightsActivity.class.getName()),
                            IntentMatchers.hasExtraWithKey("city")
                    )
            );
            onView(withId(R.id.weather_insights_root)).check(matches(isDisplayed()));
        }

        resolver.delete(uri, null, null);

        sleep(transitionTime);
    }

    /**
     * Tests for navigation from DetailsActivity to WeatherInsightsActivity.
     */
    @Test
    public void detailsActivity_to_weatherInsightsActivity() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), DetailsActivity.class);
        intent.putExtra("city", EXAMPLE_CITY);

        try(ActivityScenario<DetailsActivity> scenario = ActivityScenario.launch(intent)){
            sleep(4000);
            onView(withId(R.id.detailsWeatherInsightsButton)).perform(click());

            sleep(4000);

            Intents.intended(
                    allOf(
                            IntentMatchers.hasComponent(WeatherInsightsActivity.class.getName()),
                            IntentMatchers.hasExtraWithKey("city")
                    )
            );
            onView(withId(R.id.weather_insights_root)).check(matches(isDisplayed()));
        }

        sleep(transitionTime);
    }

    /**
     * Extracts from text of children from container
     * @param scenario Scenario layout to scour element id's for
     * @param containerId element id to scour for
     * @return Texts from each child of the parent containerId
     */
    public static List<String> getTextsFromContainer(ActivityScenario<?> scenario, int containerId) {
        List<String> texts = new ArrayList<>();

        /**
         * Extracts text from element
         * @param activity Activity to find parent element with corresponding containerId
         */
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

        /**
         * Extracts text from element
         * @param activity Activity to extract text from
         */
        scenario.onActivity(activity -> {
            View view = activity.findViewById(viewId);
            if (view instanceof TextView) {
                text[0] = ((TextView) view).getText().toString();
            }
        });

        return text[0];
    }

}
