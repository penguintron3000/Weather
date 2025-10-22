package edu.uiuc.cs427app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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

import edu.uiuc.cs427app.db.DatabaseHelper;
import edu.uiuc.cs427app.db.CityContract;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

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

        // create db without imported csv
        // DatabaseHelper dbHelper = new DatabaseHelper(this);
        // dbHelper.getWritableDatabase();

        // create db with imported csv
        //edu.uiuc.cs427app.db.DatabaseImporter.importFromAssets(this);

        // Run the test /example code
        //edu.uiuc.cs427app.db.DatabaseTester.runTests(this);


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
    }

    /**
     * Refreshes the list of city buttons.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the city list when returning to this screen
        populateCityList();
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

        Long userIdLong = User.getInstance().getUserId();
        if (userIdLong == null) {
            Log.e("MainActivity", "Could not find user ID for username: " + User.getInstance().getUsername());
            return;
        }
        int userId = userIdLong.intValue();

        // Query the CityContentProvider for the user's saved cities
        String[] projection = { CityContract.CityEntry.COLUMN_DISPLAY_NAME, CityContract.CityEntry.COLUMN_COUNTRY_CODE, CityContract.CityEntry.COLUMN_CITY_ID };
        String selection = CityContract.CityEntry.COLUMN_USER_ID + "=?";
        String[] selectionArgs = { String.valueOf(userId) };
        String sortOrder = CityContract.CityEntry.COLUMN_DISPLAY_NAME + " ASC"; // Sort cities alphabetically

        try (Cursor cursor = getContentResolver().query(CityContract.CONTENT_URI, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_DISPLAY_NAME);
                int countryIndex = cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_COUNTRY_CODE);
                int idIndex = cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_CITY_ID);

                while (cursor.moveToNext()) {
                    String cityName = cursor.getString(nameIndex);
                    String countryCode = cursor.getString(countryIndex);
                    long cityId = cursor.getLong(idIndex);

                    // Inflate the city_list_item layout
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View cityView = inflater.inflate(R.layout.city_list_item, container, false);

                    // Get the TextView and Button from the layout
                    TextView cityNameText = cityView.findViewById(R.id.city_name_text);
                    Button viewInfoButton = cityView.findViewById(R.id.view_city_info_button);

                    Button removeButton = cityView.findViewById(R.id.remove_button);

                    // Set the city name and country code
                    cityNameText.setText(cityName + ", " + countryCode);

                    viewInfoButton.setOnClickListener(v -> {
                        Intent intent = new Intent(this, DetailsActivity.class);
                        intent.putExtra("city", cityName);
                        startActivity(intent);
                    });

                    removeButton.setOnClickListener(v -> {
                        showRemoveCityDialog(cityId, cityView);
                    });


                    container.addView(cityView);
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error populating city buttons", e);
        }
    }

    /**
     * Asks user for removal confirmation of selected city
     *
     * @param cityId cityId of the city in question for the dialog
     * @param cityView cityView of the city in question for the dialog
     */
    private void showRemoveCityDialog(long cityId, View cityView) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Really remove this city?")
                .setTitle("Remove City")
                .setPositiveButton("Yes", (dialog, id) -> {
                    Toast t;
                    if(cityService.removeCity(cityId)){
                        LinearLayout container = findViewById(R.id.cities_container);
                        container.removeView(cityView);
                        t = Toast.makeText(this, "City removed successfully!", Toast.LENGTH_SHORT);
                    }
                    else{
                        t = Toast.makeText(this, "Failed to remove city. Please try again later.", Toast.LENGTH_LONG);
                    }
                    t.show();
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonAddLocation) {
            Intent intent = new Intent(this, AddCityActivity.class);
            startActivity(intent);
        }
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
