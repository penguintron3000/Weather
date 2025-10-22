package edu.uiuc.cs427app;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.uiuc.cs427app.City;
import edu.uiuc.cs427app.db.CityContract;

/**
 * Bound service class, accessible by other classes to access user's cities
 */
public class CityService extends Service {

    private final IBinder binder = new CityBinder();
    private final List<City> cachedCities = new ArrayList<>();

    private int currentUserId = -1;

    public class CityBinder extends Binder {
        /**
         * Allows binding Activity class to access our CityService service
         * @return CityService access to our singleton CityService
         */
        public CityService getService() {
            return CityService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * Creates a cache of cities for a specific user according to their userId
     * @param userId userId to match query
     */
    public void loadCitiesForUser(int userId) {
        currentUserId = userId;
        cachedCities.clear();
        cachedCities.addAll(fetchCitiesFromDatabase(userId));
    }
    /**
     * Returns a list of cached cities
     * @return List of cached cities
     */
    public List<City> getCities() {
        return new ArrayList<>(cachedCities);
    }
    /**
     * Find city by its unique ID
     * @param id The ID of the city
     * @return The City object corresponding to the ID, null if not found
     */
    public City getCityById(long id) {
        for (City c : cachedCities) {
            if (c.getId() == id) return c;
        }
        return fetchCityFromDatabase(id);
    }
    /**
     * Adds a city to the cached cities and database
     * @param city The city to be added
     * @return Uri of the newly inserted city, null if the insertion failed
     */
    public Uri addCity(City city) {
        Uri result = insertCity(city);
        if(result != null){
            cachedCities.add(city);
        }

        return result;
    }
    /**
     * Inserts a new city into the database using the CityContentProvider.
     * It constructs a ContentValues object and uses the ContentResolver to perform the insertion.
     *
     * @param userId The ID of the user for whom the city is being saved.
     * @param name The name of the city.
     * @param country The two-letter country code.
     * @param lat The latitude of the city.
     * @param lon The longitude of the city.
     * @return The Uri of the newly inserted row, or null if the insertion fails (e.g., due to a duplicate).
     */
    public Uri addCity(String userId, String name, String country, double lat, double lon) {
        City city = new City(userId, name, country, lat, lon);
        return addCity(city);
    }
    /**
     * Remove city from the cached cities and database. Only remove from cache if successfully removed in database
     * @param cityId The ID of the city to remove
     * @return True if successfully removed, false if not
     */
    public boolean removeCity(long cityId) {
        int deleted = deleteCityFromDatabase(cityId);
        if (deleted == 1) {
            cachedCities.removeIf(c -> c.getId() == cityId);
            return true;
        }
        return false;
    }
    /**
     * Fetches a list of cities from the database for a specific user in alphabetical order of city names
     * @param userId The ID of the user whose cities are being fetched
     * @return List of cities for the given user
     */
    private List<City> fetchCitiesFromDatabase(int userId) {
        List<City> cities = new ArrayList<>();
        String selection = CityContract.CityEntry.COLUMN_USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId)};
        String sortOrder = CityContract.CityEntry.COLUMN_DISPLAY_NAME + " ASC";
        try (Cursor cursor = getContentResolver().query(
                CityContract.CONTENT_URI,
                null, selection, selectionArgs, sortOrder)) {

            if (cursor != null) {
                int idIndex = cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_CITY_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_DISPLAY_NAME);
                int countryIndex = cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_COUNTRY_CODE);
                int latIndex = cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_LAT);
                int lonIndex = cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_LON);

                while (cursor.moveToNext()) {
                    cities.add(new City(
                            cursor.getLong(idIndex),
                            String.valueOf(userId),
                            cursor.getString(nameIndex),
                            cursor.getString(countryIndex),
                            cursor.getDouble(latIndex),
                            cursor.getDouble(lonIndex)
                    ));
                }
            }
        }
        return cities;
    }
    /**
     * Fetches a city from the database by its city ID
     * @param cityId The ID of the city to fetch
     * @return City object corresponding to Id, null if not found
     */
    private City fetchCityFromDatabase(long cityId) {
        String selection = CityContract.CityEntry.COLUMN_CITY_ID + "=?";
        String[] args = {String.valueOf(cityId)};
        try (Cursor cursor = getContentResolver().query(CityContract.CONTENT_URI, null, selection, args, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return new City(
                        cityId,
                        String.valueOf(currentUserId),
                        cursor.getString(cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_COUNTRY_CODE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_LAT)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(CityContract.CityEntry.COLUMN_LON))
                );
            }
        }
        return null;
    }
    /**
     * Inserts a new city into the database
     * @param city The city object to insert
     * @return Uri of the newly inserted city, null if the insertion error
     */
    private Uri insertCity(City city) {
        ContentValues values = new ContentValues();
        values.put(CityContract.CityEntry.COLUMN_USER_ID, city.getUserId());
        values.put(CityContract.CityEntry.COLUMN_DISPLAY_NAME, city.getDisplayName());
        values.put(CityContract.CityEntry.COLUMN_COUNTRY_CODE, city.getCountryCode());
        values.put(CityContract.CityEntry.COLUMN_LAT, city.getLat());
        values.put(CityContract.CityEntry.COLUMN_LON, city.getLon());
        try {
            return getContentResolver().insert(CityContract.CONTENT_URI, values);
        } catch (Exception e) {
            Log.e("CityServices", "Insert failed: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deletes a city from the database by its city ID
     * @param cityId The ID of the city to delete
     * @return Number of successful deletions. Since these cities are supposedly unique, 1 for successful deletion, 0 for fail.
     */
    private int deleteCityFromDatabase(long cityId) {
        String selection = CityContract.CityEntry.COLUMN_CITY_ID + "=?";
        String[] args = {String.valueOf(cityId)};
        return getContentResolver().delete(CityContract.CONTENT_URI, selection, args);
    }
}
