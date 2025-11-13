package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailsActivity extends ThemedActivity implements View.OnClickListener{

    private TextView timeText;
    private TextView dateText;
    private TextView weatherText;
    private TextView temperatureText;
    private TextView windsSpeedText;
    private TextView windsDirectionText;
    private TextView humidityText;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        timeText = findViewById(R.id.detailsTime);
        dateText = findViewById(R.id.detailsDate);
        weatherText = findViewById(R.id.weatherStatus);
        temperatureText = findViewById(R.id.temperature);
        windsSpeedText = findViewById(R.id.windsSpeed);
        windsDirectionText = findViewById(R.id.windsDirection);
        humidityText = findViewById(R.id.humidity);

        // Process the Intent payload that has opened this Activity and show the information accordingly
        City city = getIntent().getParcelableExtra("city");
        String title = city.getDisplayName() + "\n" + city.getState() + ", " + city.getCountryCode();

        int cityNameLength = city.getDisplayName().length();

        // Initializing the GUI elements
        TextView displayName = findViewById(R.id.detailsDisplayName);

        SpannableString spannableString = new SpannableString(title);

        spannableString.setSpan(new AbsoluteSizeSpan(35, true), 0, cityNameLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(20, true), cityNameLength + 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        displayName.setText(spannableString);
        // Get the weather information from a Service that connects to a weather server and show the results

        Button buttonMap = findViewById(R.id.detailsMapButton);
        buttonMap.setOnClickListener(v ->{
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("lat", city.getLat());
            intent.putExtra("lon", city.getLon());
            intent.putExtra("city", city.getDisplayName());
            startActivity(intent);
        });

        Button insightsButton = findViewById(R.id.detailsWeatherInsightsButton);
        // Provide a quick jump from the detail page into the Weather Insights flow.
        insightsButton.setOnClickListener(v -> openWeatherInsights(city));

        ConstraintLayout background =  findViewById(R.id.cityViewBackgroundImage);
        //TODO: use LLM to set background here, however you may even create your own runnable if you want to update the image as the weather updates, can use weatherservice api calls

        startRunnables();

    }

    /**
     * Routes the details screen Weather Insights button to the new activity.
     */
    private void openWeatherInsights(City city) {
        Intent intent = new Intent(this, WeatherInsightsActivity.class);
        intent.putExtra("city", city);
        startActivity(intent);
    }

    /**
     * Start all runnables that will update itself by x milliseconds to keep up to date(or minute) information as the page persists
     */
    private void startRunnables(){
        updateTimeAndDate();
        updateWeatherStatus();
        updateTemperature();
        updateWinds();
        updateHumidity();
        updateDummyAPI();
    }

    /**
     * Live updates to time and date text, checking every second
     * Text font size is also formatted here
     */
    private void updateTimeAndDate() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String currentDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

                timeText.setText(currentTime);
                dateText.setText(currentDate);

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    //DUMMY VALUES FOR TESTING. TO BE REPLACED WITH API CALLS
    private int count = 0;
    private int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    /**
     * Placeholder API method caller. Gives us placeholder values that update every x ms
     * @return placeholder value to be displayed on the phone
     */
    public int getValue(){
        return values[count];
    }

    /**
     * Live updates to weather text, checking every minute
     */
    private void updateWeatherStatus() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String weatherStatus = "Snowy"; //TODO: api call here

                weatherText.setText(weatherStatus);

                handler.postDelayed(this, 60000);
            }
        };
        handler.post(runnable);
    }

    /**
     * Live updates to our count value to change our placeholder values every minute
     */
    private void updateDummyAPI() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                count++;
                if(count > 9){
                    count = 0;
                }

                handler.postDelayed(this, 60000);
            }
        };
        handler.post(runnable);
    }

    /**
     * Live updates to temperature, checking every minute
     * Text font size is also formatted here
     */
    private void updateTemperature() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String temp = getValue() + "°F"; //TODO: api call here

                temperatureText.setText(temp);

                handler.postDelayed(this, 60000);
            }
        };
        handler.post(runnable);
    }

    /**
     * Live updates to wind information, checking every minute
     * Text font size is also formatted here
     */
    private void updateWinds() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String windsSpeed = getValue() + " mph"; //TODO: api call here
                String direction = "NW";

                windsSpeedText.setText(windsSpeed);
                windsDirectionText.setText(direction);

                handler.postDelayed(this, 60000);
            }
        };
        handler.post(runnable);
    }

    /**
     * Live updates to humidity, checking every minute
     * Text font size is also formatted here
     */
    private void updateHumidity() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String temp = getValue() + "%"; //TODO: api call here

                humidityText.setText(temp);

                handler.postDelayed(this, 60000);
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onClick(View view) {
        //Implement this (create an Intent that goes to a new Activity, which shows the map)
    }
}
