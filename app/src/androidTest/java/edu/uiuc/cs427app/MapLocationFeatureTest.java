package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Instrumented tests for the Google Maps based location feature.
 * Verifies map rendering, coordinate display, and zoom interactions across two cities.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapLocationFeatureTest {

    private static final double COORD_TOLERANCE = 0.001; // ~100m tolerance for camera target
    private static final float ZOOM_DELTA_MIN = 0.5f;

    /**
     * Simple holder for a city's display name and coordinates.
     */
    private static class TestCity {
        final String name;
        final double lat;
        final double lon;

        TestCity(String name, double lat, double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }

    /**
     * Immutable config holder for a test city with coordinates and name.
     */
    private static final TestCity CHAMPAIGN = new TestCity("Champaign", 40.1164, -88.2434);
    private static final TestCity CHICAGO = new TestCity("Chicago", 41.8781, -87.6298);

    /**
     * Safely fetches the current camera position from the UI thread.
     */
    private CameraPosition getCameraPositionOnUiThread(ActivityScenario<MapActivity> scenario, GoogleMap map) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<CameraPosition> cameraRef = new AtomicReference<>();
        scenario.onActivity(activity -> {
            cameraRef.set(map.getCameraPosition());
            latch.countDown();
        });
        assertTrue("Camera position not available", latch.await(5, TimeUnit.SECONDS));
        return cameraRef.get();
    }

    /**
     * Builds an intent to launch MapActivity with the required extras for a city.
     *
     * @param city target city configuration
     * @return Intent populated with lat/lon/city name extras
     */
    private Intent createMapIntent(TestCity city) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MapActivity.class);
        intent.putExtra("lat", city.lat);
        intent.putExtra("lon", city.lon);
        intent.putExtra("city", city.name);
        return intent;
    }

    /**
     * Waits for the map fragment to initialize and returns the GoogleMap instance.
     *
     * @param scenario active MapActivity scenario
     * @return initialized GoogleMap
     * @throws InterruptedException if waiting is interrupted
     */
    private GoogleMap waitForMap(ActivityScenario<MapActivity> scenario) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<GoogleMap> mapRef = new AtomicReference<>();

        scenario.onActivity(activity -> {
            SupportMapFragment fragment = (SupportMapFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (fragment != null) {
                // Callback: invoked when the GoogleMap is ready to use.
                fragment.getMapAsync(map -> {
                    mapRef.set(map);
                    latch.countDown();
                });
            } else {
                latch.countDown();
            }
        });

        assertTrue("Google Map did not initialize in time", latch.await(10, TimeUnit.SECONDS));
        GoogleMap map = mapRef.get();
        assertNotNull("GoogleMap reference is null", map);
        return map;
    }

    /**
     * Asserts that the camera target matches the expected city coordinates within tolerance.
     *
     * @param map  GoogleMap instance with current camera position
     * @param city expected city configuration
     */
    private void assertMapCenteredOnCity(ActivityScenario<MapActivity> scenario, GoogleMap map, TestCity city) throws InterruptedException {
        CameraPosition camera = getCameraPositionOnUiThread(scenario, map);
        LatLng target = camera.target;
        assertTrue(Math.abs(target.latitude - city.lat) < COORD_TOLERANCE);
        assertTrue(Math.abs(target.longitude - city.lon) < COORD_TOLERANCE);
    }

    /**
     * Asserts that UI labels reflect the current city name and coordinates.
     *
     * @param city expected city configuration
     */
    private void assertUiLabelsMatchCity(TestCity city) {
        onView(withId(R.id.city_name))
                .check(matches(withText(containsString(city.name))));
        onView(withId(R.id.coordinates))
                .check(matches(withText(containsString(String.format("%.6f", city.lat)))));
        onView(withId(R.id.coordinates))
                .check(matches(withText(containsString(String.format("%.6f", city.lon)))));
    }

    /**
     * Shared flow exercising map render, correct coordinates, labels, and zoom in/out.
     *
     * @param city The city configuration whose map state and UI are validated
     */
    private void runLocationAssertions(TestCity city) throws Exception {
        try (ActivityScenario<MapActivity> scenario = ActivityScenario.launch(createMapIntent(city))) {
            GoogleMap map = waitForMap(scenario);

            onView(withId(R.id.map)).perform(click());
            onView(withId(R.id.map)).check(matches(isDisplayed()));

            assertMapCenteredOnCity(scenario, map, city);
            assertUiLabelsMatchCity(city);

            CameraPosition initialCamera = getCameraPositionOnUiThread(scenario, map);
            scenario.onActivity(activity -> map.moveCamera(CameraUpdateFactory.zoomIn()));
            Thread.sleep(500);
            float zoomedIn = getCameraPositionOnUiThread(scenario, map).zoom;
            assertTrue("Zoom in should increase zoom level", zoomedIn - initialCamera.zoom >= ZOOM_DELTA_MIN);

            scenario.onActivity(activity -> map.moveCamera(CameraUpdateFactory.zoomOut()));
            Thread.sleep(500);
            float zoomedOut = getCameraPositionOnUiThread(scenario, map).zoom;
            assertTrue("Zoom out should decrease zoom level", zoomedIn - zoomedOut >= ZOOM_DELTA_MIN);
        }
    }

    /**
     * Verifies Champaign map view: correct city/coordinate display, map centered on the expected
     * location, and zoom in/out interaction changes the zoom level.
     */
    @Test
    public void testChampaignLocationDetailsAndZoom() throws Exception {
        runLocationAssertions(CHAMPAIGN);
    }

    /**
     * Verifies Chicago map view: correct city/coordinate display, map centered on the expected
     * location, and zoom in/out interaction changes the zoom level.
     */
    @Test
    public void testChicagoLocationDetailsAndZoom() throws Exception {
        runLocationAssertions(CHICAGO);
    }
}
