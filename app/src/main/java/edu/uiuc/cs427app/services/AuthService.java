package edu.uiuc.cs427app.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import java.time.DateTimeException;
import java.time.LocalDateTime;

import edu.uiuc.cs427app.db.UserContract;
import edu.uiuc.cs427app.model.AuthResult;
import edu.uiuc.cs427app.User;
import edu.uiuc.cs427app.PasswordHasher;

/**
 * AuthService provides methods for user authentication.
 */
public class AuthService {

    private static final String TAG = "AuthService";
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 5;
    private static final String GENERIC_CREDENTIALS_ERROR = "Your username and/or password was incorrect.";

    private final Context context;

    /**
     * Constructor for the AuthService.
     * @param context The application context.
     */
    public AuthService(Context context) {
        this.context = context;
    }

    /**
     * Authenticates a user based on the provided username and password.
     * This method checks for account locks, verifies the password, and handles failed login attempts.
     *
     * @param username The user's username.
     * @param password The user's password.
     * @return An AuthResult object indicating the result of the authentication attempt.
     */
    public AuthResult login(String username, String password) {
        Cursor cursor = context.getContentResolver().query(
                UserContract.CONTENT_URI,
                null,
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null);

        if (cursor == null) {
            return new AuthResult(AuthResult.Status.FAILURE, null, "Unable to process your request at this time.");
        }

        try {
            if (!cursor.moveToFirst()) {
                return new AuthResult(AuthResult.Status.INVALID_CREDENTIALS, null, GENERIC_CREDENTIALS_ERROR);
            }

            int isLockedIndex = cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_IS_LOCKED);
            int lockedAtIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_LOCKED_AT);
            int lockedUntilIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_LOCKED_UNTIL);

            boolean isLocked = cursor.getInt(isLockedIndex) == 1;
            LocalDateTime lockedAt = parseDateTime(cursor, lockedAtIndex);
            LocalDateTime lockedUntil = parseDateTime(cursor, lockedUntilIndex);

            if (isLocked) {
                if (shouldRemainLocked(lockedAt, lockedUntil)) {
                    return new AuthResult(AuthResult.Status.ACCOUNT_LOCKED, null, "Account is temporarily locked.");
                } else {
                    unlockAccount(username);
                }
            }

            int passwordHashIndex = cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_PASSWORD_HASH);
            String passwordHash = cursor.getString(passwordHashIndex);

            if (PasswordHasher.verify(password, passwordHash)) {
                resetFailedAttempts(username);

                int userIdIndex = cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_USER_ID);
                int themeJsonIndex = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_THEME_JSON);

                long userId = cursor.getLong(userIdIndex);
                String themeJson = cursor.getString(themeJsonIndex);

                User user = User.getInstance();
                user.init(userId, username, themeJson);

                return new AuthResult(AuthResult.Status.SUCCESS, user, "Login successful");
            }

            int failedAttemptsIndex = cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_FAILED_ATTEMPTS);
            int failedAttempts = cursor.getInt(failedAttemptsIndex) + 1;

            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                lockAccount(username, failedAttempts);
                return new AuthResult(AuthResult.Status.ACCOUNT_LOCKED, null, "Account is temporarily locked due to too many failed attempts.");
            }

            updateFailedAttempts(username, failedAttempts);
            return new AuthResult(AuthResult.Status.INVALID_CREDENTIALS, null, GENERIC_CREDENTIALS_ERROR);
        } finally {
            cursor.close();
        }
    }

    /**
     * Parses a LocalDateTime string from the current cursor row.
     *
     * @param cursor cursor positioned on the target row
     * @param columnIndex index of the column storing the datetime string
     * @return parsed LocalDateTime or null if parsing fails
     */
    private LocalDateTime parseDateTime(Cursor cursor, int columnIndex) {
        if (columnIndex < 0) {
            return null;
        }
        String value = cursor.getString(columnIndex);
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeException ex) {
            Log.w(TAG, "Unable to parse stored LocalDateTime: " + value, ex);
            return null;
        }
    }

    /**
     * Returns whether the account should remain locked given the stored lock timestamps.
     *
     * @param lockedAt time when the account was locked
     * @param lockedUntil time when the account should be unlocked
     * @return true if the lock is still in effect
     */
    private boolean shouldRemainLocked(LocalDateTime lockedAt, LocalDateTime lockedUntil) {
        LocalDateTime now = LocalDateTime.now();
        if (lockedUntil != null) {
            return now.isBefore(lockedUntil);
        }
        if (lockedAt != null) {
            return now.isBefore(lockedAt.plusMinutes(LOCK_DURATION_MINUTES));
        }
        return false;
    }

    /**
     * Clears lock-related fields and failed attempt counters for the specified username.
     *
     * @param username account identifier to unlock
     */
    private void unlockAccount(String username) {
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_IS_LOCKED, 0);
        values.put(UserContract.UserEntry.COLUMN_FAILED_ATTEMPTS, 0);
        values.putNull(UserContract.UserEntry.COLUMN_LOCKED_AT);
        values.putNull(UserContract.UserEntry.COLUMN_LOCKED_UNTIL);
        context.getContentResolver().update(
                UserContract.CONTENT_URI,
                values,
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username});
    }

    /**
     * Resets failed attempt counters and clears lock timestamps.
     *
     * @param username username whose counters should be reset
     */
    private void resetFailedAttempts(String username) {
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_FAILED_ATTEMPTS, 0);
        values.putNull(UserContract.UserEntry.COLUMN_LOCKED_AT);
        values.putNull(UserContract.UserEntry.COLUMN_LOCKED_UNTIL);
        context.getContentResolver().update(
                UserContract.CONTENT_URI,
                values,
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username});
    }

    /**
     * Updates the failed attempt counter without locking the account.
     *
     * @param username username being updated
     * @param failedAttempts updated failed attempt count
     */
    private void updateFailedAttempts(String username, int failedAttempts) {
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_FAILED_ATTEMPTS, failedAttempts);
        context.getContentResolver().update(
                UserContract.CONTENT_URI,
                values,
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username});
    }

    /**
     * Locks the account for the configured duration and records timestamps.
     *
     * @param username username being locked
     * @param failedAttempts failed attempt count at time of locking
     */
    private void lockAccount(String username, int failedAttempts) {
        LocalDateTime lockedAt = LocalDateTime.now();
        LocalDateTime lockedUntil = lockedAt.plusMinutes(LOCK_DURATION_MINUTES);

        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_FAILED_ATTEMPTS, failedAttempts);
        values.put(UserContract.UserEntry.COLUMN_IS_LOCKED, 1);
        values.put(UserContract.UserEntry.COLUMN_LOCKED_AT, lockedAt.toString());
        values.put(UserContract.UserEntry.COLUMN_LOCKED_UNTIL, lockedUntil.toString());

        context.getContentResolver().update(
                UserContract.CONTENT_URI,
                values,
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username});
    }
}
