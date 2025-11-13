package edu.uiuc.cs427app;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * MapActivity displays a Google Map focused on a specific city.
 * It receives the city's latitude, longitude, and name via an Intent
 * and displays the city name, coordinates, and a map with a marker at that location.
 * The map is zoomed to a street-level view.
 */
public class MapActivity extends ThemedActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double lat;
    private double lon;
    private String cityName;

    /**
     * Called when the activity is first created.
     * Retrieves city data from the Intent and initializes the UI components.
     *
     * @param savedInstanceState A bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get the location data and city name from the intent
        lat = getIntent().getDoubleExtra("lat", 0);
        lon = getIntent().getDoubleExtra("lon", 0);
        cityName = getIntent().getStringExtra("city");

        // Display the city name
        TextView cityNameView = findViewById(R.id.city_name);
        if (cityName != null && !cityName.isEmpty()) {
            cityNameView.setText(cityName);
        } else {
            cityNameView.setText("Unknown City");
        }

        // Display the latitude and longitude
        TextView coordinatesView = findViewById(R.id.coordinates);
        coordinatesView.setText(String.format("Latitude: %.6f, Longitude: %.6f", lat, lon));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Callback triggered when the Google Map is ready to be used.
     * Configures the map with a marker at the city's location and zooms to a street-level view.
     *
     * @param googleMap The GoogleMap instance that is ready to be used
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Create a LatLng object for the city's location
        LatLng cityLocation = new LatLng(lat, lon);

        // Add a marker at the city's location with the city name as the title
        mMap.addMarker(new MarkerOptions().position(cityLocation).title(cityName));

        // Zoom to a street-level view of the city (zoom level 14 provides good detail)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 14f));
    }
}
