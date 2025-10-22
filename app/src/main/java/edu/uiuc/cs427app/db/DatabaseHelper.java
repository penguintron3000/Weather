package edu.uiuc.cs427app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DatabaseHelper is responsible for creating and upgrading the app's SQLite database.
 * It defines the schema for the user and city tables and handles version upgrades.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "app_db.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a DatabaseHelper to manage database creation and version management.
     *
     * @param context The application context.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * This is where the creation of tables and initial schema should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlUser = "CREATE TABLE user (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password_hash TEXT NOT NULL," +
                "is_locked INTEGER DEFAULT 0," +
                "locked_at INTEGER," +
                "locked_until INTEGER," +
                "theme_json TEXT" +
                ");";
        String sqlCity = "CREATE TABLE city (" +
                "city_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "display_name TEXT NOT NULL COLLATE NOCASE," +
                "country_code TEXT," +
                "lat REAL," +
                "lon REAL," +
                "FOREIGN KEY(user_id) REFERENCES user(user_id) ON DELETE CASCADE," +
                "UNIQUE(user_id, display_name, country_code)" +
                ");";
        db.execSQL(sqlUser);
        db.execSQL(sqlCity);

        Log.d(TAG, "Database tables created");
    }

    /**
     * Called when the database needs to be upgraded.
     * This implementation drops existing tables and recreates them — all data will be lost.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion +
                ". Existing data will be dropped");
        db.execSQL("DROP TABLE IF EXISTS city");
        db.execSQL("DROP TABLE IF EXISTS user");
        onCreate(db);
    }
}
