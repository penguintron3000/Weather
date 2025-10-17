package edu.uiuc.cs427app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Arrays;


public class DatabaseTester {
    private static final String TAG = "DbTestHelper";

    public static void runTests(Context context) {
        testInsertUser(context);
        testQueryUserByName(context);
        testQueryUserById(context);
        testUpdateUserByName(context);
        testUpdateUserById(context);
        testInsertCity(context);
        testQueryCityByName(context);
        testQueryCityById(context);
        testUpdateCityByName(context);
        testUpdateCityById(context);
//        testDeleteUserByName(context);
//        testDeleteUserById(context);
//        testInsertUser(context);
//        testDeleteCityByName(context);
//        testDeleteCityById(context);
    }

    private static void testInsertUser(Context context) {
        try {
            ContentValues cvUser = new ContentValues();
            cvUser.put(UserContract.UserEntry.COLUMN_USERNAME, "testuser");
            cvUser.put(UserContract.UserEntry.COLUMN_PASSWORD_HASH, "hash_test");
            cvUser.put(UserContract.UserEntry.COLUMN_IS_LOCKED, 0);
            cvUser.put(UserContract.UserEntry.COLUMN_THEME_JSON,
                    "{\"bgColor\":\"#FFFFFF\",\"textColor\":\"#000000\"}");
            Uri newUserUri = context.getContentResolver().insert(UserContract.CONTENT_URI, cvUser);
            Log.d(TAG, "Inserted user URI: " + newUserUri);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting test user", e);
        }
    }

    // Query user by username (name)
    private static void testQueryUserByName(Context context) {
        Cursor cursor = context.getContentResolver().query(
                UserContract.CONTENT_URI,
                null,
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{"testuser"},
                null
        );
        logUserCursor(cursor, "byName");
    }

    // Query user by ID
    private static void testQueryUserById(Context context) {
        long userId = 1;
        Uri uri = Uri.withAppendedPath(UserContract.CONTENT_URI, String.valueOf(userId));
        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null
        );
        logUserCursor(cursor, "byId (" + userId + ")");
    }

    private static void logUserCursor(Cursor cursor, String tagSuffix) {
        if (cursor != null) {
            String[] cols = cursor.getColumnNames();
            Log.d(TAG, "User Cursor columns (" + tagSuffix + "): " + Arrays.toString(cols));

            if (cursor.moveToFirst()) {
                int idxUserId = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_USER_ID);
                int idxUsername = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_USERNAME);
                int idxTheme = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_THEME_JSON);

                if (idxUserId >= 0 && idxUsername >= 0 && idxTheme >= 0) {
                    long uid = cursor.getLong(idxUserId);
                    String uname = cursor.getString(idxUsername);
                    String themeJson = cursor.getString(idxTheme);
                    Log.d(TAG, "Queried user (" + tagSuffix + "): id=" + uid
                            + ", username=" + uname + ", theme=" + themeJson);
                } else {
                    Log.e(TAG, "Some user columns not found (" + tagSuffix + "): "
                            + "idxUserId=" + idxUserId
                            + ", idxUsername=" + idxUsername
                            + ", idxTheme=" + idxTheme);
                }
            } else {
                Log.d(TAG, "User Cursor is empty — no matching user (" + tagSuffix + ")");
            }
            cursor.close();
        } else {
            Log.e(TAG, "User query returned null cursor (" + tagSuffix + ")");
        }
    }

    public static void testUpdateUserByName(Context context) {
        ContentValues up = new ContentValues();
        up.put(UserContract.UserEntry.COLUMN_THEME_JSON, "{\"bgColor\":\"#000000\",\"textColor\":\"#FFFFFF\"}");
        up.put(UserContract.UserEntry.COLUMN_PASSWORD_HASH, "password2");
        int count = context.getContentResolver().update(
                UserContract.CONTENT_URI,
                up,
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{"testuser"}
        );
        Log.d(TAG, "Updated user byName count: " + count);
    }

    public static void testUpdateUserById(Context context) {
        long userId = 1;
        Uri uri = Uri.withAppendedPath(UserContract.CONTENT_URI, String.valueOf(userId));
        ContentValues up = new ContentValues();
        up.put(UserContract.UserEntry.COLUMN_THEME_JSON, "{\"bgColor\":\"#333333\",\"textColor\":\"#EEEEEE\"}");
        int count = context.getContentResolver().update(
                uri,
                up,
                null,
                null
        );
        Log.d(TAG, "Updated user byId count: " + count);
    }

    private static void testInsertCity(Context context) {
        try {
            ContentValues cvCity = new ContentValues();
            cvCity.put(CityContract.CityEntry.COLUMN_USER_ID, 1);
            cvCity.put(CityContract.CityEntry.COLUMN_DISPLAY_NAME, "SampleCity");
            cvCity.put(CityContract.CityEntry.COLUMN_COUNTRY_CODE, "SC");
            cvCity.put(CityContract.CityEntry.COLUMN_LAT, 12.345678);
            cvCity.put(CityContract.CityEntry.COLUMN_LON, 98.765432);
            Uri newCityUri = context.getContentResolver().insert(CityContract.CONTENT_URI, cvCity);
            Log.d(TAG, "Inserted city URI: " + newCityUri);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting test city", e);
        }
    }

    // Query city by name
    private static void testQueryCityByName(Context context) {
        Cursor c2 = context.getContentResolver().query(
                CityContract.CONTENT_URI,
                null,
                CityContract.CityEntry.COLUMN_DISPLAY_NAME + " = ?",
                new String[]{"SampleCity"},
                null
        );
        logCityCursor(c2, "byName");
    }

    // Query city by ID
    private static void testQueryCityById(Context context) {
        long cityId = 1;
        Uri uri = Uri.withAppendedPath(CityContract.CONTENT_URI, String.valueOf(cityId));
        Cursor c2 = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null
        );
        logCityCursor(c2, "byId (" + cityId + ")");
    }

    private static void logCityCursor(Cursor c2, String tagSuffix) {
        if (c2 != null) {
            String[] cols2 = c2.getColumnNames();
            Log.d(TAG, "City Cursor columns (" + tagSuffix + "): " + Arrays.toString(cols2));

            if (c2.moveToFirst()) {
                int idxCityId = c2.getColumnIndex(CityContract.CityEntry.COLUMN_CITY_ID);
                int idxUserId = c2.getColumnIndex(CityContract.CityEntry.COLUMN_USER_ID);
                int idxName = c2.getColumnIndex(CityContract.CityEntry.COLUMN_DISPLAY_NAME);
                int idxLat = c2.getColumnIndex(CityContract.CityEntry.COLUMN_LAT);
                int idxLon = c2.getColumnIndex(CityContract.CityEntry.COLUMN_LON);

                if (idxCityId >= 0 && idxUserId >= 0 && idxName >= 0 && idxLat >= 0 && idxLon >= 0) {
                    long cid = c2.getLong(idxCityId);
                    long uid = c2.getLong(idxUserId);
                    String name = c2.getString(idxName);
                    double lat = c2.getDouble(idxLat);
                    double lon = c2.getDouble(idxLon);
                    Log.d(TAG, "Queried city (" + tagSuffix + "): id=" + cid
                            + ", user_id=" + uid
                            + ", name=" + name
                            + ", lat=" + lat + ", lon=" + lon);
                } else {
                    Log.e(TAG, "Some city columns not found (" + tagSuffix + "): "
                            + "idxCityId=" + idxCityId
                            + ", idxUserId=" + idxUserId
                            + ", idxName=" + idxName
                            + ", idxLat=" + idxLat
                            + ", idxLon=" + idxLon);
                }
            } else {
                Log.d(TAG, "City Cursor is empty — no matching city (" + tagSuffix + ")");
            }
            c2.close();
        } else {
            Log.e(TAG, "City query returned null cursor (" + tagSuffix + ")");
        }
    }

    public static void testUpdateCityByName(Context context) {
        ContentValues up = new ContentValues();
        up.put(CityContract.CityEntry.COLUMN_DISPLAY_NAME, "SampleCityUpdated");
        int count = context.getContentResolver().update(
                CityContract.CONTENT_URI,
                up,
                CityContract.CityEntry.COLUMN_DISPLAY_NAME + " = ?",
                new String[]{"SampleCity"}
        );
        Log.d(TAG, "Updated city byName count: " + count);
    }

    public static void testUpdateCityById(Context context) {
        long cityId = 1;
        Uri uri = Uri.withAppendedPath(CityContract.CONTENT_URI, String.valueOf(cityId));
        ContentValues up = new ContentValues();
        up.put(CityContract.CityEntry.COLUMN_DISPLAY_NAME, "UpdatedCityName");
        int count = context.getContentResolver().update(
                uri,
                up,
                null,
                null
        );
        Log.d(TAG, "Updated city byId count: " + count);
    }

    // (Optional) delete examples
    public static void testDeleteUserByName(Context context) {
        int del = context.getContentResolver().delete(
                UserContract.CONTENT_URI,
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{"testuser"}
        );
        Log.d(TAG, "Deleted user byName count: " + del);
    }

    public static void testDeleteUserById(Context context) {
        long userId = 2;
        Uri uri = Uri.withAppendedPath(UserContract.CONTENT_URI, String.valueOf(userId));
        int del = context.getContentResolver().delete(uri, null, null);
        Log.d(TAG, "Deleted user byId count: " + del);
    }

    public static void testDeleteCityByName(Context context) {
        int del = context.getContentResolver().delete(
                CityContract.CONTENT_URI,
                CityContract.CityEntry.COLUMN_DISPLAY_NAME + " = ?",
                new String[]{"SampleCityUpdated"}
        );
        Log.d(TAG, "Deleted city byName count: " + del);
    }

    public static void testDeleteCityById(Context context) {
        long cityId = 1;
        Uri uri = Uri.withAppendedPath(CityContract.CONTENT_URI, String.valueOf(cityId));
        int del = context.getContentResolver().delete(uri, null, null);
        Log.d(TAG, "Deleted city byId count: " + del);
    }
}
