package edu.uiuc.cs427app;

/**
 * Global singleton User class.
 * This class maintains the current logged-in user's state throughout the app
 * lifecycle.
 * LoginActivity fills this class with data after successful authentication.
 * MainActivity references this class to display username in the header.
 */
public final class User {
    private static final User INSTANCE = new User();

    private Long userId;
    private String username;
    private String themeJson;
    private boolean isLoggedIn;

    /**
     * Private constructor to ensure only one instance exists.
     */
    private User() {
        this.isLoggedIn = false;
    }

    /**
     * Returns the single instance of User class.
     * 
     * @return Global User instance
     */
    public static User getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes user data after successful login.
     * 
     * @param userId    User's database ID
     * @param username  User's username
     * @param themeJson User's theme preferences (JSON string format)
     */
    public void init(long userId, String username, String themeJson) {
        this.userId = userId;
        this.username = username;
        this.themeJson = themeJson;
        this.isLoggedIn = true;
    }

    /**
     * Clears user data (for logout).
     */
    public void clear() {
        this.userId = null;
        this.username = null;
        this.themeJson = null;
        this.isLoggedIn = false;
    }

    /**
     * Checks if user is currently logged in.
     * 
     * @return true if user is logged in, false o.w.
     */
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    /**
     * Gets the current user's ID.
     * 
     * @return User ID or null if not logged in
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Gets the current user's username.
     * 
     * @return Username or null if not logged in
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the current user's theme preferences.
     * 
     * @return Theme JSON string or null if not logged in
     */
    public String getThemeJson() {
        return themeJson;
    }
}
