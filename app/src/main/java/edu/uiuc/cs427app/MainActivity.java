package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.ui.AppBarConfiguration;

import edu.uiuc.cs427app.databinding.ActivityMainBinding;

import android.widget.Button;

import edu.uiuc.cs427app.db.DatabaseHelper;

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
        edu.uiuc.cs427app.db.DatabaseImporter.importFromAssets(this);

        // Run the test /example code
        edu.uiuc.cs427app.db.DatabaseTester.runTests(this);

        // update header to display "Team 413 - <username>"
        updateHeader();

        // Initializing the UI components
        // The list of locations should be customized per user (change the
        // implementation so that
        // buttons are added to layout programmatically
        Button buttonChampaign = findViewById(R.id.buttonChampaign);
        Button buttonChicago = findViewById(R.id.buttonChicago);
        Button buttonLA = findViewById(R.id.buttonLA);
        Button buttonNew = findViewById(R.id.buttonAddLocation);

        buttonChampaign.setOnClickListener(this);
        buttonChicago.setOnClickListener(this);
        buttonLA.setOnClickListener(this);
        buttonNew.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.buttonChampaign:
                intent = new Intent(this, DetailsActivity.class);
                intent.putExtra("city", "Champaign");
                startActivity(intent);
                break;
            case R.id.buttonChicago:
                intent = new Intent(this, DetailsActivity.class);
                intent.putExtra("city", "Chicago");
                startActivity(intent);
                break;
            case R.id.buttonLA:
                intent = new Intent(this, DetailsActivity.class);
                intent.putExtra("city", "Los Angeles");
                startActivity(intent);
                break;
            case R.id.buttonAddLocation:
                // Implement this action to add a new location to the list of locations
                break;
        }
    }
}
