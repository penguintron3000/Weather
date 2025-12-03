package edu.uiuc.cs427app.util;

import android.location.Location;

/**
 * Lightweight mock location source for instrumentation tests. It avoids hitting
 * real location hardware or network-backed APIs by returning deterministic coordinates.
 */
public class MockLocationProvider {

    private MockLocation currentLocation;

    /**
     * Represents a captured mock location with an optional human readable label.
     */
    public static class MockLocation {
        public final String label;
        public final double latitude;
        public final double longitude;

        MockLocation(String label, double latitude, double longitude) {
            this.label = label;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        /**
         * Converts this mock location into an Android {@link Location} instance.
         */
        public Location toLocation() {
            Location location = new Location("mock");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAccuracy(1f);
            location.setTime(System.currentTimeMillis());
            return location;
        }
    }

    /**
     * Updates the active mock location and returns the stored representation.
     */
    public MockLocation setLocation(String label, double latitude, double longitude) {
        currentLocation = new MockLocation(label, latitude, longitude);
        return currentLocation;
    }

    /**
     * Gets the last mock location that was provided (or null if none).
     */
    public MockLocation getCurrentLocation() {
        return currentLocation;
    }
}
