package edu.uiuc.cs427app;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import edu.uiuc.cs427app.db.CityContract;

/**
 * A well defined City object to translate to and from the database
 */
public class City implements Parcelable {

    private long id;
    private String userId;
    private String placeId;
    private String displayName;
    private String state;
    private String countryCode;
    private double lat;
    private double lon;

    /**
     * Constructor for preparsed data values
     * @param id cityId primary key from database
     * @param userId user ID associated with city record
     * @param placeId Google Map's unique place ID of city
     * @param displayName City display name
     * @param state City's state/province
     * @param countryCode Country code of city
     * @param lat city latitude
     * @param lon city longitude
      */
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

    /**
     * Constructor for creating and inserting newly added city to database
     * since id is assigned via autoincrement per DatabaseHelper SQL
     * @param userId user ID associated with city record
     * @param placeId Google Map's unique place ID of city
     * @param displayName City display name
     * @param state City's state/province
     * @param countryCode Country code of city
     * @param lat city latitude
     * @param lon city longitude
     */
    public City(String userId, String placeId, String displayName, String state, String countryCode, double lat, double lon) { //before inserting, we will not have id, until we replace id with places id
        this.userId = userId; //we may change this to long since that is how User class defines userId
        this.placeId = placeId;
        this.displayName = displayName;
        this.state = state;
        this.countryCode = countryCode;
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Constructor for City directly built out of Content Provider values
     * @param contentValues ContentValues containing mapped key value pairs of desired column name, desired value
      */
    public City(ContentValues contentValues) {
        this.id = contentValues.getAsInteger(CityContract.CityEntry.COLUMN_CITY_ID);
        this.userId = contentValues.getAsString(CityContract.CityEntry.COLUMN_USER_ID);
        this.displayName = contentValues.getAsString(CityContract.CityEntry.COLUMN_DISPLAY_NAME);
        this.countryCode = contentValues.getAsString(CityContract.CityEntry.COLUMN_COUNTRY_CODE);
        this.lat = contentValues.getAsDouble(CityContract.CityEntry.COLUMN_LAT);
        this.lon = contentValues.getAsDouble(CityContract.CityEntry.COLUMN_LON);
    }

    // Parcelable constructor
    public City(Parcel in) {
        id = in.readLong();
        userId = in.readString();
        placeId = in.readString();
        displayName = in.readString();
        state = in.readString();
        countryCode = in.readString();
        lat = in.readDouble();
        lon = in.readDouble();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(userId);
        parcel.writeString(placeId);
        parcel.writeString(displayName);
        parcel.writeString(state);
        parcel.writeString(countryCode);
        parcel.writeDouble(lat);
        parcel.writeDouble(lon);
    }

    //Allow Parcel to create a new city object using Parcel payload data
    public static final Creator<City> CREATOR = new Creator<City>() {

        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }

    };
}

