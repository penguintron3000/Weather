package edu.uiuc.cs427app;
import android.content.ContentValues;
import edu.uiuc.cs427app.db.CityContract;

/**
 * A well defined City object to translate to and from the database
 */
public class City {

    private long id;
    private String userId;
    private String displayName;
    private String countryCode;
    private double lat;
    private double lon;

    // Constructor for preparsed data values
    public City(long id, String userId, String displayName, String countryCode, double lat, double lon) {
        this.id = id;
        this.userId = userId; //we may change this to long since that is how User class defines userId
        this.displayName = displayName;
        this.countryCode = countryCode;
        this.lat = lat;
        this.lon = lon;
    }

    public City(String userId, String displayName, String countryCode, double lat, double lon) { //before inserting, we will not have id, until we replace id with places id
        this.userId = userId; //we may change this to long since that is how User class defines userId
        this.displayName = displayName;
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
     * Sets the ID of the city.
     *
     * @param id The unique ID to set for the city.
     */
    public void setId(long id) {
        this.id = id;
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
     * Sets the user ID associated with the city.
     *
     * @param userId The user ID to set for the city.
     */
    public void setUserId(String userId) {
        this.userId = userId;
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
     * Sets the display name of the city.
     *
     * @param displayName The display name to set for the city.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
     * Sets the country code of the city.
     *
     * @param countryCode The country code to set for the city.
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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
     * Sets the latitude of the city.
     *
     * @param lat The latitude to set for the city.
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * Gets the longitude of the city.
     *
     * @return The longitude of the city.
     */
    public double getLon() {
        return lon;
    }

    /**
     * Sets the longitude of the city.
     *
     * @param lon The longitude to set for the city.
     */
    public void setLon(double lon) {
        this.lon = lon;
    }
}

