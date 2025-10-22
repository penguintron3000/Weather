package edu.uiuc.cs427app.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * ContentProvider for the user table.
 * Provides CRUD operations for the user table, allowing access to the user data
 * stored in the SQLite database via {@link DatabaseHelper}.
 * The provider supports both querying/updating the full user table
 * (URI: content://AUTHORITY/user) and a single user by ID
 * (URI: content://AUTHORITY/user/#).
 *
 */
public class UserContentProvider extends ContentProvider {
    private static final String TAG = "UserContentProvider";

    private static final int USER = 100;
    private static final int USER_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(UserContract.AUTHORITY, "user", USER);
        sUriMatcher.addURI(UserContract.AUTHORITY, "user/#", USER_ID);
    }

    private DatabaseHelper mHelper;

    /**
     * Called when the provider is being created.
     * Initializes the DatabaseHelper instance.
     *
     * @return true if provider was successfully loaded, false otherwise.
     */
    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Handle query requests from clients.
     * If URI matches "user", returns a cursor for all users (optionally filtered by selection).
     * If URI matches "user/#", returns a cursor for the specific user ID.
     *
     * @param uri           The URI to query.
     * @param projection    The list of columns to include (null = all).
     * @param selection     SQL WHERE clause (omit user id when path provides it).
     * @param selectionArgs Arguments for selection placeholders.
     * @param sortOrder     The sort order for the returned rows.
     * @return Cursor over the result set.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case USER:
                cursor = db.query(UserContract.UserEntry.TABLE_NAME,
                        projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case USER_ID:
                long id = ContentUris.parseId(uri);
                cursor = db.query(UserContract.UserEntry.TABLE_NAME,
                        projection,
                        UserContract.UserEntry.COLUMN_USER_ID + " = ?",
                        new String[]{ String.valueOf(id) },
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        // Set notification URI so ContentResolver can observe changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Handle requests to insert a new user.
     * Only URIs matching "user" (the whole table) are supported for insert.
     * The inserted row’s URI (with generated ID) is returned on success.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values A set of column_name/value pairs to add.
     * @return Uri of the newly inserted row.
     * @throws SQLException if the insert fails.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Uri resultUri;
        switch (sUriMatcher.match(uri)) {
            case USER:
                long id = db.insert(UserContract.UserEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    resultUri = ContentUris.withAppendedId(UserContract.CONTENT_URI, id);
                    getContext().getContentResolver().notifyChange(resultUri, null);
                    return resultUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new UnsupportedOperationException("Unknown URI for insert: " + uri);
        }
    }

    /**
     * Handle requests to update existing users.
     * Supports updating either multiple rows via "user" URI with selection,
     * or a single row via "user/#" URI identifying ID.
     *
     * @param uri           The URI to update.
     * @param values        A map from columns to update values.
     * @param selection     SQL WHERE clause (used when table-level URI).
     * @param selectionArgs Arguments for selection placeholders.
     * @return The number of rows updated.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case USER:
                count = db.update(UserContract.UserEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case USER_ID:
                long id = ContentUris.parseId(uri);
                count = db.update(UserContract.UserEntry.TABLE_NAME,
                        values,
                        UserContract.UserEntry.COLUMN_USER_ID + " = ?",
                        new String[]{ String.valueOf(id) });
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI for update: " + uri);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    /**
     * Handle requests to delete users.
     * Supports deletion either of multiple rows via "user", or single row via "user/#".
     *
     * @param uri           The full URI to query, including row ID if applicable.
     * @param selection     Optional SQL WHERE clause.
     * @param selectionArgs Arguments for WHERE placeholders.
     * @return The number of rows deleted.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case USER:
                count = db.delete(UserContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_ID:
                long id = ContentUris.parseId(uri);
                count = db.delete(UserContract.UserEntry.TABLE_NAME,
                        UserContract.UserEntry.COLUMN_USER_ID + " = ?",
                        new String[]{ String.valueOf(id) });
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI for delete: " + uri);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    /**
     * Returns the MIME type of data for the given URI.
     * For "user" URI returns directory type,
     * for "user/#" URI returns single item type.
     *
     * @param uri The URI to query.
     * @return A MIME type string.
     */
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case USER:
                return "vnd.android.cursor.dir/vnd." + UserContract.AUTHORITY + ".user";
            case USER_ID:
                return "vnd.android.cursor.item/vnd." + UserContract.AUTHORITY + ".user";
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }
}
