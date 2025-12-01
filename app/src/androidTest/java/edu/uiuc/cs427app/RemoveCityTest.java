package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
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
import android.view.View;
import android.view.ViewGroup;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

import edu.uiuc.cs427app.db.CityContract;
import edu.uiuc.cs427app.db.UserContract;

/**
 * Instrumentation tests for the "Remove City" feature.
 * This class verifies that cities can be correctly removed from the main list,
 * that the removal action persists after activity recreation, and that the
 * cancel operation works as expected.
 */
@RunWith(AndroidJUnit4.class)
public class RemoveCityTest {

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

        // Clear old data
        context.getContentResolver().delete(UserContract.CONTENT_URI, null, null);
        context.getContentResolver().delete(CityContract.CONTENT_URI, null, null);

        // Create test user and initialize the User singleton (bypass login UI)
        long newUserId = createTestUserInDb();
        User.getInstance().init(newUserId, USERNAME, "{}");
    }

    /**
     * Verifies that removing a city correctly updates the UI by removing the
     * corresponding city view and decrementing the list size.
     */
    @Test
    public void testRemoveCorrectCity_DecrementsListSize() {
        // add two cities for the current user
        addCityForCurrentUser("Chicago", "IL", "US");
        sleepForDemo(1000);
        addCityForCurrentUser("Seattle", "WA", "US");
        sleepForDemo(1000);
        // Now launch MainActivity and let it load the cities
        try (ActivityScenario<MainActivity> mainScenario =
                     ActivityScenario.launch(MainActivity.class)) {

            sleepForDemo(2000); // let UI render

            // Remove Chicago
            removeCityAtPosition(0, true);
            sleepForDemo(2000);
            // Assert ground truth:
            // - Chicago is no longer displayed
            // - Seattle is still displayed
            // - The list contains exactly 1 child view (1 city)
            onView(withText(containsString("Chicago"))).check(doesNotExist());
            onView(withText(containsString("Seattle"))).check(matches(isDisplayed()));
            onView(withId(R.id.cities_container)).check(matches(hasChildCount(1)));
        }
    }

    /**
     * Ensures that clicking "Cancel" in the remove confirmation dialog does not
     * remove the city from the list.
     */
    @Test
    public void testCancelRemove_DoesNotRemoveCity() {
        // Add one city
        addCityForCurrentUser("Chicago", "IL", "US");
        sleepForDemo(1000);
        try (ActivityScenario<MainActivity> mainScenario =
                     ActivityScenario.launch(MainActivity.class)) {

            sleepForDemo(2000);

            // Attempt to remove the only city (position 0), but press "Cancel"
            removeCityAtPosition(0, false);
            sleepForDemo(2000);
            // Assert: city is still there, list size unchanged
            onView(withText(containsString("Chicago"))).check(matches(isDisplayed()));
            onView(withId(R.id.cities_container)).check(matches(hasChildCount(1)));
        }
    }

    /**
     * Confirms that a removed city remains gone even after navigating to its
     * details page, verifying that the removal was persisted to the database.
     */
    @Test
    public void testRemovedCity_ListPersistsAfterNavigation() {
        // Add two cities
        addCityForCurrentUser("Chicago", "IL", "US");
        addCityForCurrentUser("Seattle", "WA", "US");

        try (ActivityScenario<MainActivity> mainScenario =
                     ActivityScenario.launch(MainActivity.class)) {

            sleepForDemo(2000);

            // Remove Chicago (position 0)
            removeCityAtPosition(0, true);
            sleepForDemo(1500);

            // Navigate to Seattle's details page
            onView(allOf(
                    withId(R.id.view_city_info_button),
                    withText(containsString("Seattle"))
            )).perform(click());

            sleepForDemo(2000);

            // Come back
            pressBack();
            sleepForDemo(1500);

            // Assert: Chicago stays gone, Seattle remains
            onView(withText(containsString("Chicago"))).check(doesNotExist());
            onView(withText(containsString("Seattle"))).check(matches(isDisplayed()));
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
     * Test helper to add a city for the logged-in user by launching AddCityActivity,
     * injecting a mock Place object, and simulating a click on the save button.
     *
     * @param cityName The name of the city to add.
     * @param stateName The state of the city.
     * @param countryCode The country code of the city.
     */
    private void addCityForCurrentUser(String cityName, String stateName, String countryCode) {
        Place fakePlace = createFakePlace(cityName, stateName, countryCode);

        try (ActivityScenario<AddCityActivity> scenario =
                     ActivityScenario.launch(AddCityActivity.class)) {

            scenario.onActivity(activity -> {
                activity.setFakePlaceForTesting(fakePlace);
            });
            sleepForDemo(2000);
            // Save the city
            onView(withId(R.id.button_save))
                    .check(matches(isDisplayed()))
                    .perform(click());

            sleepForDemo(2000);
        }
    }

    /**
     * Helper method to simulate the user interaction for removing a city from the list.
     * It finds the remove button associated with a city at a given position, clicks it, and then
     * interacts with the confirmation dialog.
     *
     * @param position The position of the city to be removed in the list.
     * @param confirm True to click "Yes" on the dialog, false to click "Cancel".
     */
    private void removeCityAtPosition(int position, boolean confirm) {
        // Perform a custom action on the cities_container to click that row's remove_button
        onView(withId(R.id.cities_container))
                .perform(clickRemoveButtonAtPosition(position));

        sleepForDemo(2000);

        String buttonText = confirm ? "Yes" : "Cancel";
        onView(withText(buttonText)).perform(click());
        sleepForDemo(2000);
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
     * Custom Hamcrest matcher that checks if a ViewGroup has a specific number of child views.
     *
     * @param count The expected number of children.
     * @return A matcher that verifies the child count of a ViewGroup.
     */
    public static Matcher<View> hasChildCount(final int count) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has " + count + " children");
            }

            @Override
            protected boolean matchesSafely(View view) {
                return view instanceof ViewGroup
                        && ((ViewGroup) view).getChildCount() == count;
            }
        };
    }

    /**
     * Returns a custom {@link ViewAction} that finds a child view at a specific position
     * within a ViewGroup, locates a 'remove_button' within that child, and clicks it.
     *
     * @param position The position of the child view in the ViewGroup.
     * @return A ViewAction to be performed on the ViewGroup.
     */
    private static ViewAction clickRemoveButtonAtPosition(final int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                // Must be a ViewGroup (LinearLayout)
                return isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "Click remove_button inside child at position " + position;
            }

            @Override
            public void perform(UiController uiController, View view) {
                ViewGroup container = (ViewGroup) view;
                if (container.getChildCount() <= position) {
                    throw new AssertionError("No child at position " + position +
                            ", childCount=" + container.getChildCount());
                }
                View child = container.getChildAt(position);
                View remove = child.findViewById(R.id.remove_button);
                if (remove == null) {
                    throw new AssertionError("No remove_button found in child at position " + position);
                }
                // Delegate to Espresso's built-in click action on that specific view
                click().perform(uiController, remove);
            }
        };
    }

    /**
     * Pauses the test execution for a specified duration.
     * This is primarily used for visual demonstration or to wait for UI animations.
     *
     * @param millis The duration to sleep in milliseconds.
     */
    private void sleepForDemo(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
