package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.uiuc.cs427app.util.MockLocationProvider;

/**
 * Validates that the map UI responds to mocked location changes instead of relying on real GPS.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocationMockingTest {

    private static final double CHICAGO_LAT = 41.8781;
    private static final double CHICAGO_LON = -87.6298;
    private static final double CHAMPAIGN_LAT = 40.1164;
    private static final double CHAMPAIGN_LON = -88.2434;
    private static final double SEATTLE_LAT = 47.6062;
    private static final double SEATTLE_LON = -122.3321;

    private ActivityScenario<MapActivity> launchMapFor(MockLocationProvider.MockLocation location) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra("lat", location.latitude);
        intent.putExtra("lon", location.longitude);
        intent.putExtra("city", location.label);
        return ActivityScenario.launch(intent);
    }

    /**
     * Starts a map for Chicago, then swaps to Champaign via the mock provider and verifies the UI updates.
     */
    @Test
    public void testMockLocationSwitchesMapBetweenCities() {
        MockLocationProvider locationProvider = new MockLocationProvider();
        MockLocationProvider.MockLocation chicago = locationProvider.setLocation("Chicago", CHICAGO_LAT, CHICAGO_LON);

        try (ActivityScenario<MapActivity> scenario = launchMapFor(chicago)) {
            // Allow the map to load for clarity in the demo video
            Thread.sleep(1000);

            onView(withId(R.id.map)).check(matches(isDisplayed()));
            onView(withId(R.id.city_name)).check(matches(withText("Chicago")));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", CHICAGO_LAT)))));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", CHICAGO_LON)))));

            Thread.sleep(500);

            // Push a new mock location and update the running activity without recreating it
            MockLocationProvider.MockLocation champaign = locationProvider.setLocation("Champaign", CHAMPAIGN_LAT, CHAMPAIGN_LON);
            scenario.onActivity(activity -> activity.updateLocationForTesting(
                    champaign.latitude,
                    champaign.longitude,
                    champaign.label
            ));

            // Small pause so the TA can see the transition in the recording
            Thread.sleep(500);

            onView(withId(R.id.city_name)).check(matches(withText("Champaign")));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", CHAMPAIGN_LAT)))));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", CHAMPAIGN_LON)))));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Applies multiple mock updates in sequence and verifies each transition renders the new location.
     */
    @Test
    public void testMockLocationSupportsMultipleCityTransitions() {
        MockLocationProvider provider = new MockLocationProvider();
        MockLocationProvider.MockLocation chicago = provider.setLocation("Chicago", CHICAGO_LAT, CHICAGO_LON);

        try (ActivityScenario<MapActivity> scenario = launchMapFor(chicago)) {
            Thread.sleep(800);

            onView(withId(R.id.city_name)).check(matches(withText("Chicago")));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", CHICAGO_LON)))));

            MockLocationProvider.MockLocation champaign = provider.setLocation("Champaign", CHAMPAIGN_LAT, CHAMPAIGN_LON);
            scenario.onActivity(activity -> activity.updateLocationForTesting(
                    champaign.latitude, champaign.longitude, champaign.label));
            Thread.sleep(500);

            onView(withId(R.id.city_name)).check(matches(withText("Champaign")));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", CHAMPAIGN_LAT)))));

            MockLocationProvider.MockLocation seattle = provider.setLocation("Seattle", SEATTLE_LAT, SEATTLE_LON);
            scenario.onActivity(activity -> activity.updateLocationForTesting(
                    seattle.latitude, seattle.longitude, seattle.label));
            Thread.sleep(500);

            onView(withId(R.id.city_name)).check(matches(withText("Seattle")));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", SEATTLE_LAT)))));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", SEATTLE_LON)))));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Ensures mock updates with empty labels gracefully show the fallback city name text.
     */
    @Test
    public void testMockLocationWithUnknownCityNameShowsFallback() {
        MockLocationProvider provider = new MockLocationProvider();
        MockLocationProvider.MockLocation mock = provider.setLocation("", CHAMPAIGN_LAT, CHAMPAIGN_LON);

        try (ActivityScenario<MapActivity> scenario = launchMapFor(mock)) {
            Thread.sleep(500);

            // Blank city name should render the activity fallback string.
            onView(withId(R.id.city_name)).check(matches(withText("Unknown City")));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", CHAMPAIGN_LAT)))));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", CHAMPAIGN_LON)))));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
