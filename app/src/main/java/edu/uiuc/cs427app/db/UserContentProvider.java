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
        // 设置通知 URI
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

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
