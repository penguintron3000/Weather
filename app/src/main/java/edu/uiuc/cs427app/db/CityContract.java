package edu.uiuc.cs427app.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for the city table in the app’s database.
 * Defines the table name, column names, content authority and content URI
 * that other components (ContentProviders, UI, etc.) will use to interact
 * with the city data.
 */
public final class CityContract {
    private CityContract() { }

    public static final String AUTHORITY = "edu.uiuc.cs427app.cityprovider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/city");

    /**
     * Inner class that defines the contents of the city table.
     */
    public static final class CityEntry implements BaseColumns {
        public static final String TABLE_NAME = "city";

        public static final String COLUMN_CITY_ID = "city_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_PLACE_ID = "place_id";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_COUNTRY_CODE = "country_code";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LON = "lon";
    }
}
