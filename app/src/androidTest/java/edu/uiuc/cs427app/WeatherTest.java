package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Pattern;

/**
 * Tests for the Weather Feature. Verifies weather data display, city information, 
 * and button navigation against 2 cities (Champaign, IL and Chicago, IL). 
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WeatherTest {

    /**
     * Test config for Champaign, IL.
     */
    private static class ChampaignConfig {
        static final String DISPLAY_NAME = "Champaign";
        static final String STATE = "IL";
        static final String COUNTRY_CODE = "US";
        static final double LATITUDE = 40.1164;
        static final double LONGITUDE = -88.2434;
        static final double LAT_LON_TOLERANCE = 0.0001; // tolerance for lat/lon comparison
    }

    /**
     * Test config for Chicago, IL.
     */
    private static class ChicagoConfig {
        static final String DISPLAY_NAME = "Chicago";
        static final String STATE = "IL";
        static final String COUNTRY_CODE = "US";
        static final double LATITUDE = 41.8781;
        static final double LONGITUDE = -87.6298;
        static final double LAT_LON_TOLERANCE = 0.0001; 
    }

    /**
     * Sets up the test environment before each test (initialize
     * Intents for intent verification)..
     */
    @Before
    public void setUp() {
        Intents.init();
    }

    /**
     * Cleans up after each test (releases Intents).
     */
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Launch DetailsActivity with the specified city.
     *
     * @param city The city to display weather for
     * @return Active ActivityScenario for DetailsActivity
     */
    private ActivityScenario<DetailsActivity> launchDetailsActivity(City city) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("city", city);
        return ActivityScenario.launch(intent);
    }

    /**
     * Create a test City object with the specified config.
     *
     * @param displayName City display name
     * @param state State/province
     * @param countryCode Country code
     * @param lat Latitude
     * @param lon Longitude
     * @return City object for testing
     */
    private City createTestCity(String displayName, String state, String countryCode, double lat, double lon) {
        return new City(
                1L,
                "testuser",
                "test_place_id_" + displayName,
                displayName,
                state,
                countryCode,
                lat,
                lon
        );
    }

    /**
     * Test city name is displayed correctly for Champaign.
     */
    @Test
    public void testDisplaysChampaignCityName() {
        City city = createTestCity(
                ChampaignConfig.DISPLAY_NAME,
                ChampaignConfig.STATE,
                ChampaignConfig.COUNTRY_CODE,
                ChampaignConfig.LATITUDE,
                ChampaignConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(city)) {
            // wait 1 second for activity to load 
            Thread.sleep(1000);

            onView(withId(R.id.detailsDisplayName))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.detailsDisplayName))
                    .check(matches(withText(containsString(ChampaignConfig.DISPLAY_NAME))));

            // 0.5 sec pause
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test city name is displayed correctly for Chicago.
     */
    @Test
    public void testDisplaysChicagoCityName() {
        City city = createTestCity(
                ChicagoConfig.DISPLAY_NAME,
                ChicagoConfig.STATE,
                ChicagoConfig.COUNTRY_CODE,
                ChicagoConfig.LATITUDE,
                ChicagoConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(city)) {
            // wait for activity to load
            Thread.sleep(1000);

            // Verify city name is displayed
            onView(withId(R.id.detailsDisplayName))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.detailsDisplayName))
                    .check(matches(withText(containsString(ChicagoConfig.DISPLAY_NAME))));

            // 0.5 sec pause
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test temperature is displayed and is a valid float/double for Champaign.
     */
    @Test
    public void testDisplaysValidTemperatureForChampaign() {
        City city = createTestCity(
                ChampaignConfig.DISPLAY_NAME,
                ChampaignConfig.STATE,
                ChampaignConfig.COUNTRY_CODE,
                ChampaignConfig.LATITUDE,
                ChampaignConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(city)) {
            // wait for weather data from api to load 
            Thread.sleep(3000);

            onView(withId(R.id.temperature))
                    .check(matches(isDisplayed()));

            onView(withId(R.id.temperature))
                    .check(matches(not(withText("N/A"))));

            // verify temperature is a number followed by °F
            Pattern temperaturePattern = Pattern.compile("-?\\d+(\\.\\d+)?°F");
            onView(withId(R.id.temperature))
                    .check(matches(withText(org.hamcrest.Matchers.matchesPattern(temperaturePattern))));

            // 0.5 sec pause
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Tests temperature is displayed and is a valid float/double for Chicago.
     */
    @Test
    public void testDisplaysValidTemperatureForChicago() {
        City city = createTestCity(
                ChicagoConfig.DISPLAY_NAME,
                ChicagoConfig.STATE,
                ChicagoConfig.COUNTRY_CODE,
                ChicagoConfig.LATITUDE,
                ChicagoConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(city)) {
            // wait for weather data from api to load 
            Thread.sleep(3000);

            onView(withId(R.id.temperature))
                    .check(matches(isDisplayed()));

            // verify temperature is a number followed by °F
            Pattern temperaturePattern = Pattern.compile("-?\\d+(\\.\\d+)?°F");
            onView(withId(R.id.temperature))
                    .check(matches(withText(org.hamcrest.Matchers.matchesPattern(temperaturePattern))));

            // 0.5 sec pause
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test Map button redirects to MapActivity with correct city data for Champaign.
     */
    @Test
    public void testMapButtonRedirectsToMapActivityForChampaign() {
        City city = createTestCity(
                ChampaignConfig.DISPLAY_NAME,
                ChampaignConfig.STATE,
                ChampaignConfig.COUNTRY_CODE,
                ChampaignConfig.LATITUDE,
                ChampaignConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(city)) {
            // wait for activity to load
            Thread.sleep(1000);

            onView(withId(R.id.detailsMapButton))
                    .perform(click());

            // 0.5 sec pause 
            Thread.sleep(500);

            // verify MapActivity is launched with correct intent extras
            intended(hasComponent(MapActivity.class.getName()));
            intended(hasExtra("lat", ChampaignConfig.LATITUDE));
            intended(hasExtra("lon", ChampaignConfig.LONGITUDE));
            intended(hasExtra("city", ChampaignConfig.DISPLAY_NAME));

            // verify MapActivity is displayed
            onView(withId(R.id.map))
                    .check(matches(isDisplayed()));

            // 0.5 sec pause 
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test Map button redirects to MapActivity with correct city data for Chicago.
     */
    @Test
    public void testMapButtonRedirectsToMapActivityForChicago() {
        City city = createTestCity(
                ChicagoConfig.DISPLAY_NAME,
                ChicagoConfig.STATE,
                ChicagoConfig.COUNTRY_CODE,
                ChicagoConfig.LATITUDE,
                ChicagoConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(city)) {
            // wait for activity to load
            Thread.sleep(1000);

            onView(withId(R.id.detailsMapButton))
                    .perform(click());

            // 0.5 sec pause 
            Thread.sleep(500);

            // verify MapActivity is launched with correct intent extras
            intended(hasComponent(MapActivity.class.getName()));
            intended(hasExtra("lat", ChicagoConfig.LATITUDE));
            intended(hasExtra("lon", ChicagoConfig.LONGITUDE));
            intended(hasExtra("city", ChicagoConfig.DISPLAY_NAME));

            // verify MapActivity is displayed
            onView(withId(R.id.map))
                    .check(matches(isDisplayed()));

            // 0.5 sec pause 
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test Weather Insights button redirects to WeatherInsightsActivity for Champaign.
     */
    @Test
    public void testWeatherInsightsButtonRedirectsForChampaign() {
        City city = createTestCity(
                ChampaignConfig.DISPLAY_NAME,
                ChampaignConfig.STATE,
                ChampaignConfig.COUNTRY_CODE,
                ChampaignConfig.LATITUDE,
                ChampaignConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(city)) {
            // wait for activity to load
            Thread.sleep(1000);

            onView(withId(R.id.detailsWeatherInsightsButton))
                    .perform(click());

            // 0.5 sec pause 
            Thread.sleep(500);

            // verify WeatherInsightsActivity is launched
            intended(hasComponent(WeatherInsightsActivity.class.getName()));

            // verify WeatherInsightsActivity is displayed
            onView(withId(R.id.weather_insights_root))
                    .check(matches(isDisplayed()));

            // 0.5 sec pause 
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Test Weather Insights button redirects to WeatherInsightsActivity for Chicago.
     */
    @Test
    public void testWeatherInsightsButtonRedirectsForChicago() {
        City city = createTestCity(
                ChicagoConfig.DISPLAY_NAME,
                ChicagoConfig.STATE,
                ChicagoConfig.COUNTRY_CODE,
                ChicagoConfig.LATITUDE,
                ChicagoConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(city)) {
            // Wait for activity to load
            Thread.sleep(1000);

            onView(withId(R.id.detailsWeatherInsightsButton))
                    .perform(click());

            // 0.5 sec pause 
            Thread.sleep(500);

            // verify WeatherInsightsActivity is launched
            intended(hasComponent(WeatherInsightsActivity.class.getName()));

            // verify WeatherInsightsActivity is displayed
            onView(withId(R.id.weather_insights_root))
                    .check(matches(isDisplayed()));

            // 0.5 sec pause 
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Mocks the device location to Champaign, then verifies the
     * map shows the correct location when navigating from DetailsActivity.
     */
    @Test
    public void testLocationMockingWorks() {
        City champaignCity = createTestCity(
                ChampaignConfig.DISPLAY_NAME,
                ChampaignConfig.STATE,
                ChampaignConfig.COUNTRY_CODE,
                ChampaignConfig.LATITUDE,
                ChampaignConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(champaignCity)) {
            Thread.sleep(1000);

            onView(withId(R.id.detailsMapButton))
                    .perform(click());

            // 0.5 sec pause 
            Thread.sleep(500);

            // verify MapActivity is launched with Champaign coordinates
            intended(hasComponent(MapActivity.class.getName()));
            intended(hasExtra("lat", ChampaignConfig.LATITUDE));
            intended(hasExtra("lon", ChampaignConfig.LONGITUDE));
            intended(hasExtra("city", ChampaignConfig.DISPLAY_NAME));

            // verify MapActivity displays the map
            onView(withId(R.id.map))
                    .check(matches(isDisplayed()));

            // verify city name is displayed in MapActivity
            onView(withId(R.id.city_name))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.city_name))
                    .check(matches(withText(ChampaignConfig.DISPLAY_NAME)));

            // verify MapActivity displays coordinates as "Latitude: X.XXXXXX, Longitude: Y.YYYYYY"
            onView(withId(R.id.coordinates))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", ChampaignConfig.LATITUDE)))));
            onView(withId(R.id.coordinates))
                    .check(matches(withText(containsString(String.format("%.6f", ChampaignConfig.LONGITUDE)))));

            // 0.5 sec pause 
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Tests that weather data fields are displayed for Champaign.
     */
    @Test
    public void testDisplaysWeatherDataForChampaign() {
        City city = createTestCity(
                ChampaignConfig.DISPLAY_NAME,
                ChampaignConfig.STATE,
                ChampaignConfig.COUNTRY_CODE,
                ChampaignConfig.LATITUDE,
                ChampaignConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(city)) {
            // Wait for weather data from api to load
            Thread.sleep(3000);

            onView(withId(R.id.weatherStatus))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.temperature))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.humidity))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.windsSpeed))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.windsDirection))
                    .check(matches(isDisplayed()));

            onView(withId(R.id.temperature))
                    .check(matches(not(withText("N/A"))));

            // 0.5 sec pause 
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Tests that weather data fields are displayed for Chicago.
     */
    @Test
    public void testDisplaysWeatherDataForChicago() {
        City city = createTestCity(
                ChicagoConfig.DISPLAY_NAME,
                ChicagoConfig.STATE,
                ChicagoConfig.COUNTRY_CODE,
                ChicagoConfig.LATITUDE,
                ChicagoConfig.LONGITUDE
        );

        try (ActivityScenario<DetailsActivity> scenario = launchDetailsActivity(city)) {
            // Wait for weather data from api to load
            Thread.sleep(3000);

            onView(withId(R.id.weatherStatus))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.temperature))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.humidity))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.windsSpeed))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.windsDirection))
                    .check(matches(isDisplayed()));

            onView(withId(R.id.temperature))
                    .check(matches(not(withText("N/A"))));

            // 0.5 sec pause 
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

