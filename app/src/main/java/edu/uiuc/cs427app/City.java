package edu.uiuc.cs427app;
import android.content.ContentValues;
import edu.uiuc.cs427app.db.CityContract;

/**
 * A well defined City object to translate to and from the database
 */
public class City {

    private long id;
    private String userId;
    private String placeId;
    private String displayName;
    private String state;
    private String countryCode;
    private double lat;
    private double lon;

    // Constructor for preparsed data values
    public City(long id, String userId, String placeId, String displayName, String state, String countryCode, double lat, double lon) {
        this.id = id;
        this.userId = userId; //we may change this to long since that is how User class defines userId
        this.placeId = placeId;
        this.displayName = displayName;
        this.state = state;
        this.countryCode = countryCode;
        this.lat = lat;
        this.lon = lon;
    }

    public City(String userId, String placeId, String displayName, String state, String countryCode, double lat, double lon) { //before inserting, we will not have id, until we replace id with places id
        this.userId = userId; //we may change this to long since that is how User class defines userId
        this.placeId = placeId;
        this.displayName = displayName;
        this.state = state;
        this.countryCode = countryCode;
        this.lat = lat;
        this.lon = lon;
    }

    // Constructor for City directly built out of Content Provider values
    public City(ContentValues contentValues) {
        this.id = contentValues.getAsInteger(CityContract.CityEntry.COLUMN_CITY_ID);
        this.userId = contentValues.getAsString(CityContract.CityEntry.COLUMN_USER_ID);
        this.displayName = contentValues.getAsString(CityContract.CityEntry.COLUMN_DISPLAY_NAME);
        this.countryCode = contentValues.getAsString(CityContract.CityEntry.COLUMN_COUNTRY_CODE);
        this.lat = contentValues.getAsDouble(CityContract.CityEntry.COLUMN_LAT);
        this.lon = contentValues.getAsDouble(CityContract.CityEntry.COLUMN_LON);
    }

    /**
     * Gets the ID of the city.
     *
     * @return The ID of the city.
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the user ID associated with the city.
     *
     * @return The user ID of the city.
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Gets the place ID associated with the city.
     *
     * @return The place ID of the city.
     */
    public String getPlaceId() {
        return placeId;
    }

    /**
     * Gets the display name of the city.
     *
     * @return The display name of the city.
     */
    public String getDisplayName() {
        return displayName;
    }
    /**
     * Gets the state associated with the city.
     *
     * @return The state of the city.
     */
    public String getState() {
        return state;
    }
    /**
     * Gets the country code of the city.
     *
     * @return The country code of the city.
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Gets the latitude of the city.
     *
     * @return The latitude of the city.
     */
    public double getLat() {
        return lat;
    }

    /**
     * Gets the longitude of the city.
     *
     * @return The longitude of the city.
     */
    public double getLon() {
        return lon;
    }
}

