package edu.uiuc.cs427app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

import edu.uiuc.cs427app.db.CityContract;

/**
 * AddCityActivity provides a user interface for searching and adding new cities to the user's list.
 * It uses the Google Places API's Autocomplete feature to allow users to easily find and select a city.
 * Once a city is selected, its details are saved to the database via the CityContentProvider.
 */
public class AddCityActivity extends ThemedActivity {
    private static final String TAG = "AddCityActivity";

    //UI
    private TextView selectedCityText;
    private Button buttonSave;
    private Button buttonCancel;
    private Place selectedPlace = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        
        // Apply the global theme to all UI components
        applyThemeToActivity();

        selectedCityText = findViewById(R.id.selected_city_text);
        buttonSave = findViewById(R.id.button_save);
        buttonCancel = findViewById(R.id.button_cancel);

        // Save button hidden until a city is picked
        buttonSave.setVisibility(View.GONE);

        //Autocomplete fragment
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment == null) {
            Log.e(TAG, "Autocomplete fragment not found. Check activity_add_city.xml (id=autocomplete_fragment).");
            Toast.makeText(this, "Internal error: autocomplete not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Requesting fields for DB: name, lat/lng, country (from address components), id
        autocompleteFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
        ));

        autocompleteFragment.setTypeFilter(TypeFilter.CITIES);

        //Autocomplete search bar
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                selectedPlace = place;
                String display = place.getName() != null ? place.getName() : "(unknown)";
                selectedCityText.setText("Selected City: " + display);
                buttonSave.setVisibility(View.VISIBLE);
                Log.i(TAG, "Place selected: name=" + place.getName()
                        + ", id=" + place.getId()
                        + ", latLng=" + (place.getLatLng() != null ? place.getLatLng().toString() : "null"));
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e(TAG, "Place selection error: " + status);
                Toast.makeText(AddCityActivity.this, "Error selecting place: " + status, Toast.LENGTH_SHORT).show();
            }
        });

        buttonSave.setOnClickListener(v -> {
            if (selectedPlace == null) {
                Toast.makeText(this, "Please select a city first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get current user's ID from the singleton
            Long userIdLong = User.getInstance().getUserId();
            if (userIdLong == null) {
                Toast.makeText(this, "Error: No logged-in user.", Toast.LENGTH_SHORT).show();
                return;
            }
            int userId = userIdLong.intValue();

            //Extract selected city info
            String placeId = selectedPlace.getId();
            String name = selectedPlace.getName();
            String state = getState(selectedPlace);
            Double lat = selectedPlace.getLatLng() != null ? selectedPlace.getLatLng().latitude : null;
            Double lon = selectedPlace.getLatLng() != null ? selectedPlace.getLatLng().longitude : null;
            String country = getCountryCode(selectedPlace);

            if (placeId == null || name == null || lat == null || lon == null) {
                Toast.makeText(this, "Missing place data. Try another city.", Toast.LENGTH_SHORT).show();
                return;
            }

            //Insert selected City
            Uri inserted = cityService.addCity(String.valueOf(userId), placeId, name, state, country, lat, lon);
            if (inserted != null) {
                Toast.makeText(this, "Saved " + name, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Inserted city URI: " + inserted);
                finish();
            } else {
                //City already in list (duplicate)
                Toast.makeText(this, "City already in list.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> finish());
    }

    /**
     * Extracts the two-letter country code from a Place object.
     * It iterates through the address components of the place to find the one corresponding to the country.
     *
     * @param place The Place object from the Google Places API.
     * @return A String representing the short name of the country, or null if not found.
     */
    private String getCountryCode(Place place) {
        if (place.getAddressComponents() == null) return null;
        for (AddressComponent c : place.getAddressComponents().asList()) {
            if (c.getTypes().contains("country")) {
                return c.getShortName();
            }
        }
        return null;
    }

    /**
     * Extracts the state (administrative area level 1) from a Place object.
     *
     * @param place The Place object from the Google Places API.
     * @return The name of the state, or null if not found.
     */
    private String getState(Place place) {
        if (place.getAddressComponents() == null) return null;
        for (AddressComponent c : place.getAddressComponents().asList()) {
            if (c.getTypes().contains("administrative_area_level_1")) {
                return c.getName();
            }
        }
        return null;
    }

    private boolean bound = false;
    private CityService cityService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CityService.CityBinder binder = (CityService.CityBinder) service;
            cityService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            cityService = null;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, CityService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }
}
