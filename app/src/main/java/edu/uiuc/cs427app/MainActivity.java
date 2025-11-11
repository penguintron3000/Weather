package edu.uiuc.cs427app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.view.View;

import androidx.navigation.ui.AppBarConfiguration;

import edu.uiuc.cs427app.databinding.ActivityMainBinding;

import android.widget.Button;

import com.google.android.libraries.places.api.Places;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import edu.uiuc.cs427app.db.DatabaseHelper;
import edu.uiuc.cs427app.db.CityContract;


public class MainActivity extends ThemedActivity implements View.OnClickListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    /**
     * Initializes the main screen, ensures the user is authenticated, and wires up UI listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if user is logged in, o.w. redirect to LoginActivity
        if (!User.getInstance().isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // initialize the Places SDK
        if (!Places.isInitialized()) {
            // Read the API key from the build configuration
            String apiKey = BuildConfig.MAPS_API_KEY;
            Places.initialize(getApplicationContext(), apiKey);
        }

        // update header to display "Team 413 - <username>"
        updateHeader();

        // initialize ONLY the "Add Location" button
        Button buttonNew = findViewById(R.id.buttonAddLocation);
        buttonNew.setOnClickListener(this);

        Button logOutButton = findViewById(R.id.buttonLogOut);
        logOutButton.setOnClickListener(this);
    }

    /**
     * Refreshes the list of city buttons.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (bound && cityService != null) {
            populateCityList();
        }
    }

    /**
     * Updates the header textview to display "Team 413 - <username>"
     */
    private void updateHeader() {
        TextView headerTextView = findViewById(R.id.textView3);
        String teamName = getString(R.string.app_name);
        String username = User.getInstance().getUsername();

        if (username != null) {
            headerTextView.setText(teamName + " - " + username);
        } else {
            headerTextView.setText(teamName + " - Guest");
        }
    }

    /**
     * Fetches the user's saved cities from the database
     * and dynamically creates a view for each one using the city_list_item layout.
     */
    private void populateCityList() {
        LinearLayout container = findViewById(R.id.cities_container);
        container.removeAllViews(); // Clear old views to prevent duplicates

        List<City> cities = cityService.getCities();

        for (City city : cities) {
            String cityName = city.getDisplayName();
            String countryCode = city.getCountryCode();
            String state = city.getState();
            double lat = city.getLat();
            double lon = city.getLon();

            // Inflate the city_list_item layout
            LayoutInflater inflater = LayoutInflater.from(this);
            View cityView = inflater.inflate(R.layout.city_list_item, container, false);

            Button viewInfoButton = cityView.findViewById(R.id.view_city_info_button);
            Button mapButton = cityView.findViewById(R.id.map_button);
            Button removeButton = cityView.findViewById(R.id.remove_button);

            // Build the display text, including state if available
            StringBuilder displayText = new StringBuilder(cityName);
            if (state != null && !state.isEmpty()) {
                displayText.append(", ").append(state);
            }
            displayText.append(", ").append(countryCode);

            viewInfoButton.setText(displayText.toString());

            // Apply theme to the dynamically created city list item
            applyThemeToView(cityView);

            viewInfoButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, DetailsActivity.class);
                intent.putExtra("city", city);
                startActivity(intent);
            });

            // Launches the MapActivity with the city's location details
            mapButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("city", cityName);
                startActivity(intent);
            });

            removeButton.setOnClickListener(v -> {
                showRemoveCityDialog(city, cityView);
            });

            container.addView(cityView);
        }
    }


    /**
     * Asks user for removal confirmation of selected city
     *
     * @param city city in question for the dialog
     * @param cityView cityView of the city in question for the dialog
     */
    private void showRemoveCityDialog(City city, View cityView) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to remove this city from your saved cities?")
                .setTitle("Remove " + city.getDisplayName())
                .setPositiveButton("Yes", (dialog, id) -> {
                    Snackbar snackbar;
                    LinearLayout container = findViewById(R.id.cities_container);
                    if(cityService.removeCity(city.getId())){
                        container.removeView(cityView);
                        snackbar = Snackbar.make(container, "City removed successfully!", Snackbar.LENGTH_SHORT);
                    }
                    else{
                        snackbar = Snackbar.make(container, "Failed to remove city. Please try again later.", Snackbar.LENGTH_LONG);
                    }
                    snackbar.getView().setTranslationY(-150);
                    snackbar.show();
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Apply theme colors to dialog buttons
        android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        android.widget.Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(theme.getPrimaryColor());
        }
        if (negativeButton != null) {
            negativeButton.setTextColor(theme.getPrimaryColor());
        }
    }


    /**
     * Handles onClick callbacks for actions initiated from the main screen buttons.
     *
     * @param view View that was activated
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonAddLocation) {
            Intent intent = new Intent(this, AddCityActivity.class);
            startActivity(intent);
            return;
        }

        if (view.getId() == R.id.buttonLogOut) {
            handleLogOut();
        }
    }

    /**
     * Performs the log out workflow by clearing session state and and returning to LoginActivity.
     */
    private void handleLogOut() {
        User.getInstance().clear();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean bound = false;
    private CityService cityService;

    /**
     *  Overrides onServiceConnected and onServiceDisconnected, methods that are called asynchronously upon successfully binding/unbinding to CityService service
     */
    private ServiceConnection connection = new ServiceConnection() {

        /**
         * Allows access to CityService via IBinder, immediately loads cities from the database for the current user
         * @param name name of service
         * @param service service accessible via service implemented IBinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CityService.CityBinder binder = (CityService.CityBinder) service;
            cityService = binder.getService();
            bound = true;

            try{
                cityService.loadCitiesForUser(User.getInstance().getUserId().intValue());
            }
            catch(Exception e){
                Log.e("MainActivity", "Could not find user ID for username: " + User.getInstance().getUsername());
            }

            populateCityList();
        }

        /**
         * Ensures security upon service disconnect
         * @param name name of service
         */
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
