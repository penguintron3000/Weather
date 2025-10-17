package edu.uiuc.cs427app.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DatabaseImporter {
    private static final String TAG = "DatabaseImporter";

    public static void importFromAssets(Context context) {
        try {
            AssetManager am = context.getAssets();
            importUsers(context, "users.csv");
            importCities(context, "cities.csv");
        } catch (Exception e) {
            Log.e(TAG, "importFromAssets failed", e);
        }
    }

    private static void importUsers(Context context, String filename) {
        SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
        try (InputStream is = context.getAssets().open(filename);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",", -1);
                if (tokens.length < 6) continue;
                ContentValues cv = new ContentValues();
                cv.put(UserContract.UserEntry.COLUMN_USERNAME, tokens[0]);
                cv.put(UserContract.UserEntry.COLUMN_PASSWORD_HASH, tokens[1]);
                cv.put(UserContract.UserEntry.COLUMN_IS_LOCKED, Integer.parseInt(tokens[2]));
                if (!tokens[3].isEmpty()) cv.put(UserContract.UserEntry.COLUMN_LOCKED_AT, Long.parseLong(tokens[3]));
                if (!tokens[4].isEmpty()) cv.put(UserContract.UserEntry.COLUMN_LOCKED_UNTIL, Long.parseLong(tokens[4]));
                cv.put(UserContract.UserEntry.COLUMN_THEME_JSON, tokens[5]);
                long id = db.insert(UserContract.UserEntry.TABLE_NAME, null, cv);
                Log.d(TAG, "Inserted user id=" + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error importing users", e);
        }
    }

    private static void importCities(Context context, String filename) {
        SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
        try (InputStream is = context.getAssets().open(filename);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",", -1);
                if (tokens.length < 5) continue;
                ContentValues cv = new ContentValues();
                cv.put(CityContract.CityEntry.COLUMN_USER_ID, Integer.parseInt(tokens[0]));
                cv.put(CityContract.CityEntry.COLUMN_DISPLAY_NAME, tokens[1]);
                cv.put(CityContract.CityEntry.COLUMN_COUNTRY_CODE, tokens[2]);
                cv.put(CityContract.CityEntry.COLUMN_LAT, Double.parseDouble(tokens[3]));
                cv.put(CityContract.CityEntry.COLUMN_LON, Double.parseDouble(tokens[4]));
                long id = db.insert(CityContract.CityEntry.TABLE_NAME, null, cv);
                Log.d(TAG, "Inserted city id=" + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error importing cities", e);
        }
    }
}
