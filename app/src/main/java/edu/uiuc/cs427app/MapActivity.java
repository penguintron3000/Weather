package edu.uiuc.cs427app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * MapActivity displays a Google Map focused on a specific city.
 * It receives the city's latitude, longitude, and name via an Intent
 * and displays a marker at that location, zoomed to a street-level view.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double lat;
    private double lon;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get the location data and city name from the intent
        lat = getIntent().getDoubleExtra("lat", 0);
        lon = getIntent().getDoubleExtra("lon", 0);
        cityName = getIntent().getStringExtra("city");

        // Set the title of the activity to the city name
        TextView mapTitle = findViewById(R.id.map_title);
        if (cityName != null && !cityName.isEmpty()) {
            mapTitle.setText(cityName);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * This callback is triggered when the map is ready to be used.
     * It adds a marker at the city's location and zooms the camera to a street-level view.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Create a LatLng object for the city's location
        LatLng cityLocation = new LatLng(lat, lon);

        // Add a marker at the city's location
        mMap.addMarker(new MarkerOptions().position(cityLocation).title(cityName));

        // Zoom to a street-level view of the city
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 14f));
    }
}
