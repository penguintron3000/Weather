package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

import edu.uiuc.cs427app.db.CityContract;
import edu.uiuc.cs427app.db.UserContract;


/**
 * Instrumentation tests for the "Add City" feature.
 * This class covers UI navigation, adding multiple cities,
 * and ensuring duplicate cities are handled gracefully.
 */
@RunWith(AndroidJUnit4.class)
public class AddCityTest {

    private static final String USERNAME = "testuser";
    private Context context;

    /**
     * Sets up the test environment before each test.
     * This involves clearing the database of any existing users and cities,
     * and then creating and logging in a standard test user.
     */
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Clear any old data from previous test runs
        context.getContentResolver().delete(UserContract.CONTENT_URI, null, null);
        context.getContentResolver().delete(CityContract.CONTENT_URI, null, null);

        // Create a test user in the database and get the newly generated ID
        long newUserId = createTestUserInDb();
        // Bypass the login UI by initializing the User singleton with the correct dynamic ID
        User.getInstance().init(newUserId, USERNAME, "{}");
    }

    /**
     * Verifies that clicking the "Add Location" button on MainActivity correctly
     * launches the AddCityActivity, displaying the Google Places search UI.
     */
    @Test
    public void testClickAddCity_launchesSearchUI() {
        // Start on MainActivity
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            sleepForDemo(2000); // Wait for UI to load

            // Click the "Add Location" button
            onView(withId(R.id.buttonAddLocation)).perform(click());
            sleepForDemo(3000);

            // Assert: Verify the Autocomplete fragment from AddCityActivity is now displayed.
            // This confirms we have navigated to the search screen.
            onView(withId(R.id.autocomplete_fragment)).check(matches(isDisplayed()));

        }
    }

    /**
     * Tests the process of adding multiple cities and verifies they are displayed
     * in the correct alphabetical order on the MainActivity.
     */
    @Test
    public void testAddMultipleCities_showsAlphabeticalOrderInMainActivity() {
        // Ground truth: Alphabetical order is "Chicago" then "Manhattan"
        Place manhattan = createFakePlace("Manhattan", "NY", "US");
        Place chicago = createFakePlace("Chicago", "IL", "US");

        // 1) Add Manhattan
        try (ActivityScenario<AddCityActivity> scenario = ActivityScenario.launch(AddCityActivity.class)) {
            scenario.onActivity(activity -> {
                activity.setFakePlaceForTesting(manhattan);
            });
            sleepForDemo(2500);
            onView(withId(R.id.button_save))
                    .check(matches(isDisplayed()))
                    .perform(click());
            sleepForDemo(2500);
        }

        // 2) Add Chicago
        try (ActivityScenario<AddCityActivity> scenario2 = ActivityScenario.launch(AddCityActivity.class)) {
            scenario2.onActivity(activity -> {
                activity.setFakePlaceForTesting(chicago);
            });
            sleepForDemo(2500);
            onView(withId(R.id.button_save))
                    .check(matches(isDisplayed()))
                    .perform(click());
            sleepForDemo(2500);
        }

        // 3) Launch MainActivity to show the city list
        android.content.Intent intent = new android.content.Intent(context, MainActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Give MainActivity time to load and render the list
        sleepForDemo(3000);

        // 4) Assert ground truth UI ordering:
        onView(withId(R.id.cities_container))
                .check(matches(withViewAtPosition(0,
                        hasDescendant(withText(containsString("Chicago")))
                )));

        onView(withId(R.id.cities_container))
                .check(matches(withViewAtPosition(1,
                        hasDescendant(withText(containsString("Manhattan")))
                )));
    }

    /**
     * Verifies that attempting to add the same city twice does not create a duplicate entry.
     * This test checks both the database and the main UI list to ensure only one instance of the city exists.
     */
    @Test
    public void testAddDuplicateCity_doesNotCreateDuplicate() {
        // Create a Place for Chicago
        Place chicago = createFakePlace("Chicago", "IL", "US");

        // Add Chicago for the first time. This should succeed and close the activity.
        try (ActivityScenario<AddCityActivity> scenario1 =
                     ActivityScenario.launch(AddCityActivity.class)) {

            scenario1.onActivity(activity -> activity.setFakePlaceForTesting(chicago));
            sleepForDemo(2500);
            onView(withId(R.id.button_save)).perform(click());
            sleepForDemo(2500);
        }

        // Attempt to add Chicago again in a new activity
        try (ActivityScenario<AddCityActivity> scenario2 =
                     ActivityScenario.launch(AddCityActivity.class)) {

            scenario2.onActivity(activity -> activity.setFakePlaceForTesting(chicago));
            sleepForDemo(2500);
            onView(withId(R.id.button_save)).perform(click());
            sleepForDemo(2500);
        }

        // Assert: Database still contains only ONE row for this place ID
        android.database.Cursor c = context.getContentResolver().query(
                CityContract.CONTENT_URI,
                null,
                CityContract.CityEntry.COLUMN_PLACE_ID + " = ?",
                new String[]{"fake-id-chicago"},
                null
        );
        assertNotNull("Cursor should not be null.", c);
        org.junit.Assert.assertEquals(
                "Database should contain exactly one row for the duplicate city.",
                1,
                c.getCount()
        );
        c.close();

        // Assert : UI also shows only one city entry when we open MainActivity
        android.content.Intent intent = new android.content.Intent(context, MainActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        sleepForDemo(2500);

        // "Chicago" is shown
        onView(withText(containsString("Chicago"))).check(matches(isDisplayed()));
        // Additionally, check if there is only one child in the cities_container
        onView(withId(R.id.cities_container)).check(matches(hasChildCount(1)));
    }

    /**
     * Verifies that the list of cities on the main screen persists after navigating
     * to a different activity (the Details screen) and then returning.
     */
    @Test
    public void testCityList_persistsAfterNavigatingAwayAndBack() {
        // Add one city, "Chicago"
        Place chicago = createFakePlace("Chicago", "IL", "US");
        try (ActivityScenario<AddCityActivity> scenario =
                     ActivityScenario.launch(AddCityActivity.class)) {

            scenario.onActivity(activity -> activity.setFakePlaceForTesting(chicago));
            sleepForDemo(2500);
            onView(withId(R.id.button_save))
                    .check(matches(isDisplayed()))
                    .perform(click());
            sleepForDemo(2500); // Wait for save and activity to finish
        }

        // Launch MainActivity and verify the city is there.
        try (ActivityScenario<MainActivity> mainScenario =
                     ActivityScenario.launch(MainActivity.class)) {

            sleepForDemo(2500); // Wait for UI to load
            onView(withText(containsString("Chicago")))
                    .check(matches(isDisplayed()));

            // Navigate to the Details screen for Chicago.
            onView(allOf(
                    withId(R.id.view_city_info_button),
                    withText(containsString("Chicago"))
            )).perform(click());

            sleepForDemo(1000); // Let DetailsActivity open

            // Press the back button to return to MainActivity.
            pressBack();
            sleepForDemo(2500); // Wait for MainActivity to be visible again

            // Assert: Check that Chicago is still displayed on the list.
            onView(withText(containsString("Chicago")))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.cities_container))
                    .check(matches(hasChildCount(1)));
        }
    }

    /**
     * Pauses the test execution for a specified duration.
     * This is primarily used for visual demonstration or to wait for UI animations.
     *
     * @param millis The duration to sleep in milliseconds.
     */
    private void sleepForDemo(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to insert a standardized test user into the database.
     *
     * @return The database ID of the newly created user.
     */
    private long createTestUserInDb() {
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_USERNAME, USERNAME);
        values.put(UserContract.UserEntry.COLUMN_PASSWORD_HASH, "password");
        values.put(UserContract.UserEntry.COLUMN_FAILED_ATTEMPTS, 0);
        values.put(UserContract.UserEntry.COLUMN_IS_LOCKED, 0);

        Uri newUri = context.getContentResolver().insert(UserContract.CONTENT_URI, values);
        assertNotNull("Failed to insert new test user.", newUri);
        return Long.parseLong(newUri.getLastPathSegment());
    }


    /**
     * Creates a mock {@link Place} object with predefined properties using Mockito.
     * This is a test helper used to simulate a user selecting a place from the Google Places API,
     * bypassing the actual network calls and UI interaction.
     *
     * @param cityName The name of the city for the mock Place.
     * @param stateName The state of the city.
     * @param countryCode The two-letter country code.
     * @return A mocked Place object.
     */
    private Place createFakePlace(String cityName, String stateName, String countryCode) {
        Place place = mock(Place.class);
        when(place.getId()).thenReturn("fake-id-" + cityName.toLowerCase());
        when(place.getName()).thenReturn(cityName);

        LatLng fakeLatLng = new LatLng(41.8781, -87.6298);
        when(place.getLatLng()).thenReturn(fakeLatLng);

        AddressComponent state = mock(AddressComponent.class);
        when(state.getName()).thenReturn(stateName);
        when(state.getTypes()).thenReturn(Collections.singletonList("administrative_area_level_1"));

        AddressComponent country = mock(AddressComponent.class);
        when(country.getShortName()).thenReturn(countryCode);
        when(country.getTypes()).thenReturn(Collections.singletonList("country"));

        AddressComponents components = mock(AddressComponents.class);
        when(components.asList()).thenReturn(Arrays.asList(state, country));
        when(place.getAddressComponents()).thenReturn(components);

        return place;
    }

    /**
     * Custom Hamcrest matcher to assert properties of a view at a specific position within a ViewGroup.
     *
     * @param position The position of the child view to match.
     * @param itemMatcher The matcher to apply to the child view at the given position.
     * @return A matcher that checks a view at a specific position in a ViewGroup.
     */
    public static org.hamcrest.Matcher<android.view.View> withViewAtPosition(
            final int position,
            final org.hamcrest.Matcher<android.view.View> itemMatcher) {

        return new androidx.test.espresso.matcher.BoundedMatcher<android.view.View, android.widget.LinearLayout>(android.widget.LinearLayout.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(android.widget.LinearLayout view) {
                if (view.getChildCount() <= position) return false;
                android.view.View child = view.getChildAt(position);
                return itemMatcher.matches(child);
            }
        };
    }

    /**
     * Custom Hamcrest matcher that checks if a ViewGroup has a specific number of child views.
     *
     * @param count The expected number of children.
     * @return A matcher that verifies the child count of a ViewGroup.
     */
    public static org.hamcrest.Matcher<android.view.View> hasChildCount(final int count) {
        return new TypeSafeMatcher<android.view.View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has " + count + " children");
            }

            @Override
            protected boolean matchesSafely(android.view.View view) {
                return view instanceof android.view.ViewGroup
                        && ((android.view.ViewGroup) view).getChildCount() == count;
            }
        };
    }

}
