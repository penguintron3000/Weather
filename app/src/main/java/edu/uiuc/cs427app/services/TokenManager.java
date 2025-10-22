package edu.uiuc.cs427app.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.UUID;

/**
 * TokenManager coordinates session token storage using Android's AccountManager when possible,
 * falling back to SharedPreferences when account APIs are unavailable.
 */
public class TokenManager {

    private static final String TAG = "TokenManager";
    private static final String ACCOUNT_TYPE = "edu.uiuc.cs427app.ACCOUNT";
    private static final String AUTH_TOKEN_TYPE = "FULL_ACCESS";
    private static final String PREFS_NAME = "token_manager_store";
    private static final String PREF_KEY_PREFIX = "token_for_";

    private final AccountManager accountManager;
    private final SharedPreferences sharedPreferences;

    /**
     * Creates a TokenManager that will leverage the provided context for storage.
     *
     * @param context application or activity context used for storage
     */
    public TokenManager(Context context) {
        this.accountManager = AccountManager.get(context);
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Generates a new opaque session token.
     *
     * @return newly generated token string
     */
    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Persists the session token for the supplied username using the best available storage.
     *
     * @param username  username associated with the session
     * @param authToken opaque session token value
     */
    public void persistSession(String username, String authToken) {
        boolean storedWithAccountManager = persistViaAccountManager(username, authToken);
        if (!storedWithAccountManager) {
            persistViaPreferences(username, authToken);
        }
    }

    /**
     * Clears any stored session token for the supplied username.
     *
     * @param username username whose token should be cleared
     */
    public void clearSession(String username) {
        clearWithAccountManager(username);
        sharedPreferences.edit().remove(prefKey(username)).apply();
    }

    /**
     * Attempts to persist the token using AccountManager APIs.
     *
     * @param username  username to bind the token to
     * @param authToken opaque session token
     * @return true if storage succeeded, false otherwise
     */
    private boolean persistViaAccountManager(String username, String authToken) {
        try {
            Account account = ensureAccount(username);
            if (account == null) {
                return false;
            }
            accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, authToken);
            accountManager.setUserData(account, "last_login", String.valueOf(System.currentTimeMillis()));
            return true;
        } catch (SecurityException ex) {
            Log.w(TAG, "Falling back to SharedPreferences due to AccountManager restrictions", ex);
            return false;
        }
    }

    /**
     * Removes the stored auth token from AccountManager if present.
     *
     * @param username username whose token should be removed
     */
    private void clearWithAccountManager(String username) {
        try {
            Account account = findAccount(username);
            if (account != null) {
                accountManager.invalidateAuthToken(ACCOUNT_TYPE, accountManager.peekAuthToken(account, AUTH_TOKEN_TYPE));
                accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, null);
            }
        } catch (SecurityException ex) {
            Log.w(TAG, "Unable to clear auth token from AccountManager", ex);
        }
    }

    /**
     * Stores the token in SharedPreferences as a compatibility fallback.
     *
     * @param username username associated with the token
     * @param authToken session token to persist
     */
    private void persistViaPreferences(String username, String authToken) {
        sharedPreferences.edit()
                .putString(prefKey(username), authToken)
                .apply();
    }

    /**
     * Returns or creates the AccountManager Account for the supplied username.
     *
     * @param username username to locate inside AccountManager
     * @return account instance or null if creation failed
     */
    private Account ensureAccount(String username) {
        Account existing = findAccount(username);
        if (existing != null) {
            return existing;
        }
        Account newAccount = new Account(username, ACCOUNT_TYPE);
        boolean added;
        try {
            added = accountManager.addAccountExplicitly(newAccount, null, null);
        } catch (SecurityException ex) {
            Log.w(TAG, "Unable to add account to AccountManager", ex);
            return null;
        }
        return added ? newAccount : null;
    }

    /**
     * Looks up an existing AccountManager account by username.
     *
     * @param username username to search for
     * @return matching account or null if none is registered
     */
    private Account findAccount(String username) {
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (account.name.equals(username)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Builds the SharedPreferences key for a username.
     *
     * @param username username to encode
     * @return preference key for storing the token
     */
    private String prefKey(String username) {
        return PREF_KEY_PREFIX + username;
    }
}
