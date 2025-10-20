package edu.uiuc.cs427app.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class CityContentProvider extends ContentProvider {
    private static final int CITY = 200;
    private static final int CITY_ID = 201;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(CityContract.AUTHORITY, "city", CITY);
        sUriMatcher.addURI(CityContract.AUTHORITY, "city/#", CITY_ID);
    }

    private DatabaseHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return true;
    }

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
