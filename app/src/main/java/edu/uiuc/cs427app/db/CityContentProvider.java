package edu.uiuc.cs427app.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * ContentProvider for the city table.
 * Provides CRUD operations for the city table, allowing access to the city data
 * stored in the SQLite database via {@link DatabaseHelper}. Supports both
 * querying/updating the full city table (URI: content://AUTHORITY/city)
 * and a single city by ID (URI: content://AUTHORITY/city/#).
 */
public class CityContentProvider extends ContentProvider {
    private static final int CITY = 200;
    private static final int CITY_ID = 201;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(CityContract.AUTHORITY, "city", CITY);
        sUriMatcher.addURI(CityContract.AUTHORITY, "city/#", CITY_ID);
    }

    private DatabaseHelper mHelper;

    /**
     * Called when the provider is being created.
     * Initializes the DatabaseHelper instance for access to the database.
     *
     * @return true if the provider was successfully loaded, false otherwise.
     */
    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Handle query requests from clients.
     * If URI matches "city", returns a Cursor for all cities (optionally filtered by selection).
     * If URI matches "city/#", returns a Cursor for the specific city ID.
     *
     * @param uri           The URI to query.
     * @param projection    The list of columns to include (null = all columns).
     * @param selection     SQL WHERE clause (omit ID when URI provides it).
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
            case CITY:
                cursor = db.query(CityContract.CityEntry.TABLE_NAME,
                        projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CITY_ID:
                long id = ContentUris.parseId(uri);
                cursor = db.query(CityContract.CityEntry.TABLE_NAME,
                        projection,
                        CityContract.CityEntry.COLUMN_CITY_ID + " = ?",
                        new String[]{ String.valueOf(id) },
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Handle requests to insert a new city.
     * Only URI matching "city" (the full table) is supported for insert.
     * The inserted row’s URI (with generated ID) is returned on success.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values A set of column_name/value pairs to add.
     * @return Uri of the newly inserted row, or null if the insert fails or conflict occurs.
     * @throws SQLException If the insert fails unexpectedly.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Uri resultUri;
        switch (sUriMatcher.match(uri)) {
            case CITY:
                long id = db.insertWithOnConflict(CityContract.CityEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (id > 0) {
                    resultUri = ContentUris.withAppendedId(CityContract.CONTENT_URI, id);
                    getContext().getContentResolver().notifyChange(resultUri, null);
                    return resultUri;
                }
                return null;
            default:
                throw new UnsupportedOperationException("Unknown URI for insert: " + uri);
        }
    }

    /**
     * Handle requests to update existing cities.
     * Supports updating either multiple rows via "city" URI and selection,
     * or a single row via "city/#" URI identifying the city ID.
     *
     * @param uri           The URI to update.
     * @param values        A map from columns to update values.
     * @param selection     SQL WHERE clause if selecting multiple rows.
     * @param selectionArgs Arguments for WHERE placeholders.
     * @return The number of rows updated.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case CITY:
                count = db.update(CityContract.CityEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case CITY_ID:
                long id = ContentUris.parseId(uri);
                count = db.update(CityContract.CityEntry.TABLE_NAME,
                        values,
                        CityContract.CityEntry.COLUMN_CITY_ID + " = ?",
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
     * Handle requests to delete cities.
     * Supports deletion either of multiple rows via "city" URI and selection,
     * or single row via "city/#" URI identifying the city ID.
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
            case CITY:
                count = db.delete(CityContract.CityEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CITY_ID:
                long id = ContentUris.parseId(uri);
                count = db.delete(CityContract.CityEntry.TABLE_NAME,
                        CityContract.CityEntry.COLUMN_CITY_ID + " = ?",
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
     * For URI matching the entire table ("city") returns a directory MIME type,
     * for URI matching a single row ("city/#") returns the single-item MIME type.
     *
     * @param uri The URI to query.
     * @return A MIME type string corresponding to the URI.
     */
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CITY:
                return "vnd.android.cursor.dir/vnd." + CityContract.AUTHORITY + ".city";
            case CITY_ID:
                return "vnd.android.cursor.item/vnd." + CityContract.AUTHORITY + ".city";
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }
}
