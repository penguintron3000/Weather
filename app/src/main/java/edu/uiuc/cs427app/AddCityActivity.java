package edu.uiuc.cs427app;

import android.net.Uri;
import android.os.Bundle;
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

import edu.uiuc.cs427app.db.CityContract;

public class AddCityActivity extends AppCompatActivity {
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
            String name = selectedPlace.getName();
            Double lat = selectedPlace.getLatLng() != null ? selectedPlace.getLatLng().latitude : null;
            Double lon = selectedPlace.getLatLng() != null ? selectedPlace.getLatLng().longitude : null;
            String country = getCountryCode(selectedPlace);

            if (name == null || lat == null || lon == null) {
                Toast.makeText(this, "Missing place data. Try another city.", Toast.LENGTH_SHORT).show();
                return;
            }

            //Insert selected City
            Uri inserted = saveCityViaProvider(userId, name, country, lat, lon);
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

    /** Extract country code (e.g., "US") from AddressComponents, if present. */
    private String getCountryCode(Place place) {
        if (place.getAddressComponents() == null) return null;
        for (AddressComponent c : place.getAddressComponents().asList()) {
            if (c.getTypes().contains("country")) {
                return c.getShortName();
            }
        }
        return null;
    }

    /** Insert a city row via CityContentProvider. */
    private Uri saveCityViaProvider(int userId, String name, String country, Double lat, Double lon) {
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(CityContract.CityEntry.COLUMN_USER_ID, userId);
        values.put(CityContract.CityEntry.COLUMN_DISPLAY_NAME, name);
        values.put(CityContract.CityEntry.COLUMN_COUNTRY_CODE, country);
        values.put(CityContract.CityEntry.COLUMN_LAT, lat);
        values.put(CityContract.CityEntry.COLUMN_LON, lon);

        try {
            return getContentResolver().insert(CityContract.CONTENT_URI, values);
        } catch (Exception e) {
            Log.e(TAG, "Insert failed: " + e.getMessage(), e);
            return null;
        }
    }
}
