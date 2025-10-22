package edu.uiuc.cs427app.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for the user table in the app’s database.
 * Defines the table name, column names, content authority and content URI
 * that other components (ContentProviders, UI, etc.) will use to interact
 * with the user data.
 */
public final class UserContract {
    private UserContract() { }

    public static final String AUTHORITY = "edu.uiuc.cs427app.userprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/user");

    /**
     * Inner class that defines the contents of the user table.
     */
    public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "user";

        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD_HASH = "password_hash";
        public static final String COLUMN_IS_LOCKED = "is_locked";
        public static final String COLUMN_LOCKED_AT = "locked_at";
        public static final String COLUMN_LOCKED_UNTIL = "locked_until";
        public static final String COLUMN_THEME_JSON = "theme_json";
    }
}
