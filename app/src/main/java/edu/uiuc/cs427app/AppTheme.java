package edu.uiuc.cs427app;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * AppTheme is a global singleton theme manager class that provides consistent theming across the entire app.
 * It parses theme specifications from JSON (which can be LLM-generated) and provides easy access
 * to theme properties throughout the application.
 * 
 * This singleton class ensures that only one theme instance exists at a time and that all UI components
 * reference the same theme settings.
 * 
 * The theme supports the following elements (all required):
 * - backgroundColor: Main background color for activities (hex format)
 * - textColor: Primary text color (hex format)
 * - primaryColor: Accent/primary color for buttons and important UI elements (hex format)
 * - secondaryColor: Secondary color for cards, panels, and subtle elements (hex format)
 * - headerColor: Color for headers and titles (hex format)
 * - buttonBackgroundColor: Background color for buttons (hex format)
 * - buttonTextColor: Text color for buttons (hex format)
 * - cardBackgroundColor: Background color for card-like elements (hex format)
 * - borderColor: Color for borders and dividers (hex format)
 * - errorColor: Color for error messages (hex format)
 * - successColor: Color for success messages (hex format)
 * - emoji: Optional decorative emoji for the theme
 * - themeName: Name of the theme
 * 
 * If the provided JSON is missing any required fields or contains incorrectly formatted values,
 * the class will fall back to the default theme JSON file (default_theme.json) in the assets folder.
 */
public class AppTheme {
    private static final String TAG = "AppTheme";
    private static final String DEFAULT_THEME_FILE = "default_theme.json";
    
    // Singleton instance
    private static AppTheme instance;
    
    // Application context for accessing assets
    private static Context appContext;
    
    // Required theme fields
    private static final String[] REQUIRED_FIELDS = {
        "themeName", "backgroundColor", "textColor", "primaryColor", 
        "secondaryColor", "headerColor", "buttonBackgroundColor", 
        "buttonTextColor", "cardBackgroundColor", "borderColor", 
        "errorColor", "successColor", "emoji"
    };
    
    // Theme properties (all private with getters only)
    private int backgroundColor;
    private int textColor;
    private int primaryColor;
    private int secondaryColor;
    private int headerColor;
    private int buttonBackgroundColor;
    private int buttonTextColor;
    private int cardBackgroundColor;
    private int borderColor;
    private int errorColor;
    private int successColor;
    private String emoji;
    private String themeName;
    
    /**
     * Private constructor to enforce singleton pattern.
     * Accepts a JSONObject containing theme specifications.
     * If the JSONObject is invalid, missing required fields, or contains incorrectly formatted values,
     * this constructor will load the default theme from the backup JSON file.
     * 
     * @param themeJson JSONObject containing theme specifications
     * @throws IllegalStateException if the backup default theme file is also invalid
     */
    private AppTheme(JSONObject themeJson) {
        parseThemeJson(themeJson);
    }
    
    /**
     * Initializes the AppTheme singleton with an application context.
     * This must be called before getInstance() can be used.
     * 
     * @param context Application context
     */
    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
    }
    
    /**
     * Returns the singleton instance of AppTheme.
     * If the instance doesn't exist, creates one with the default theme.
     * 
     * @return AppTheme singleton instance
     * @throws IllegalStateException if initialize() hasn't been called first
     */
    public static AppTheme getInstance() {
        if (appContext == null) {
            throw new IllegalStateException("AppTheme must be initialized with a Context before use. Call AppTheme.initialize(context) first.");
        }
        
        if (instance == null) {
            // Load default theme from backup file
            try {
                JSONObject defaultTheme = loadDefaultThemeFromAssets();
                instance = new AppTheme(defaultTheme);
            } catch (Exception e) {
                Log.e(TAG, "Critical error: Cannot load default theme", e);
                throw new IllegalStateException("Failed to initialize AppTheme with default theme", e);
            }
        }
        return instance;
    }
    
    /**
     * Updates the singleton instance with a new theme from a JSONObject.
     * This method is used to change the app's theme at runtime.
     * 
     * @param themeJson JSONObject containing new theme specifications
     */
    public static void updateTheme(JSONObject themeJson) {
        if (appContext == null) {
            throw new IllegalStateException("AppTheme must be initialized with a Context before use.");
        }
        
        instance = new AppTheme(themeJson);
    }
    
    /**
     * Updates the singleton instance with a new theme from a JSON string.
     * This is a convenience method that parses the string to JSONObject first.
     * 
     * @param themeJsonString JSON string containing new theme specifications
     */
    public static void updateTheme(String themeJsonString) {
        try {
            JSONObject themeJson = new JSONObject(themeJsonString);
            updateTheme(themeJson);
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON string provided, keeping current theme", e);
            notifyUser("Invalid theme format, keeping current theme");
        }
    }
    
    /**
     * Public method to parse and apply theme specifications from a JSONObject.
     * 
     * This method validates that the JSONObject contains all required fields and that
     * all values are correctly formatted. If validation fails, it loads the backup
     * default theme from assets/default_theme.json instead.
     * 
     * Validation steps:
     * 1. Check that all required fields are present in the JSONObject
     * 2. Attempt to parse each field to its expected type (color hex strings, etc.)
     * 3. If any validation fails, load the backup default theme
     * 4. If the backup theme is also invalid, throw an IllegalStateException
     * 
     * @param themeJson JSONObject containing theme specifications
     * @throws IllegalStateException if both the provided JSON and backup default theme are invalid
     */
    public void parseThemeJson(JSONObject themeJson) {
        // First, validate that the JSONObject has all required fields
        if (!validateRequiredFields(themeJson)) {
            Log.w(TAG, "Provided JSONObject is missing required fields. Loading default theme from backup file.");
            notifyUser("Theme missing required fields. Loading default theme.");
            loadDefaultTheme();
            return;
        }
        
        // Try to parse all fields from the JSONObject
        try {
            // Extract and validate all fields
            String bgColor = themeJson.getString("backgroundColor");
            String txtColor = themeJson.getString("textColor");
            String primColor = themeJson.getString("primaryColor");
            String secColor = themeJson.getString("secondaryColor");
            String hdrColor = themeJson.getString("headerColor");
            String btnBgColor = themeJson.getString("buttonBackgroundColor");
            String btnTxtColor = themeJson.getString("buttonTextColor");
            String cardBgColor = themeJson.getString("cardBackgroundColor");
            String brdColor = themeJson.getString("borderColor");
            String errColor = themeJson.getString("errorColor");
            String succColor = themeJson.getString("successColor");
            String emojiStr = themeJson.getString("emoji");
            String themeNameStr = themeJson.getString("themeName");
            
            // Attempt to parse all colors - if any fail, the exception will be caught
            int bgColorInt = parseColorWithValidation(bgColor);
            int txtColorInt = parseColorWithValidation(txtColor);
            int primColorInt = parseColorWithValidation(primColor);
            int secColorInt = parseColorWithValidation(secColor);
            int hdrColorInt = parseColorWithValidation(hdrColor);
            int btnBgColorInt = parseColorWithValidation(btnBgColor);
            int btnTxtColorInt = parseColorWithValidation(btnTxtColor);
            int cardBgColorInt = parseColorWithValidation(cardBgColor);
            int brdColorInt = parseColorWithValidation(brdColor);
            int errColorInt = parseColorWithValidation(errColor);
            int succColorInt = parseColorWithValidation(succColor);
            
            // If all parsing succeeded, apply the theme
            this.backgroundColor = bgColorInt;
            this.textColor = txtColorInt;
            this.primaryColor = primColorInt;
            this.secondaryColor = secColorInt;
            this.headerColor = hdrColorInt;
            this.buttonBackgroundColor = btnBgColorInt;
            this.buttonTextColor = btnTxtColorInt;
            this.cardBackgroundColor = cardBgColorInt;
            this.borderColor = brdColorInt;
            this.errorColor = errColorInt;
            this.successColor = succColorInt;
            this.emoji = emojiStr;
            this.themeName = themeNameStr;
            
            Log.i(TAG, "Successfully loaded custom theme: " + themeName);
            
        } catch (JSONException | IllegalArgumentException e) {
            // If any field is missing or incorrectly formatted, load default theme
            Log.w(TAG, "Error parsing theme values: " + e.getMessage() + ". Loading default theme from backup file.");
            notifyUser("Theme has incorrect format. Loading default theme.");
            loadDefaultTheme();
        }
    }
    
    /**
     * Validates that the provided JSONObject contains all required fields.
     * 
     * @param json JSONObject to validate
     * @return true if all required fields are present, false otherwise
     */
    private boolean validateRequiredFields(JSONObject json) {
        if (json == null) {
            return false;
        }
        
        for (String field : REQUIRED_FIELDS) {
            if (!json.has(field)) {
                Log.w(TAG, "Missing required field: " + field);
                return false;
            }
        }
        return true;
    }
    
    /**
     * Parses a hex color string and validates it.
     * Throws an IllegalArgumentException if the color format is invalid.
     * 
     * @param hexColor Hex color string (e.g., "#FF5733" or "FF5733")
     * @return Android color integer
     * @throws IllegalArgumentException if the color format is invalid
     */
    private int parseColorWithValidation(String hexColor) throws IllegalArgumentException {
        if (hexColor == null || hexColor.trim().isEmpty()) {
            throw new IllegalArgumentException("Color string is null or empty");
        }
        
        // Ensure the color starts with #
        if (!hexColor.startsWith("#")) {
            hexColor = "#" + hexColor;
        }
        
        // This will throw IllegalArgumentException if the format is invalid
        return Color.parseColor(hexColor);
    }
    
    /**
     * Loads the default theme from the backup JSON file in assets.
     * If the backup file is also invalid, throws an IllegalStateException.
     * 
     * @throws IllegalStateException if the backup default theme file is invalid or cannot be loaded
     */
    private void loadDefaultTheme() {
        try {
            JSONObject defaultTheme = loadDefaultThemeFromAssets();
            
            // Validate the default theme - if it's invalid, this is a critical error
            if (!validateRequiredFields(defaultTheme)) {
                throw new IllegalStateException("Default theme file is missing required fields");
            }
            
            // Parse the default theme
            // We're not calling parseThemeJson here to avoid infinite recursion
            this.backgroundColor = parseColorWithValidation(defaultTheme.getString("backgroundColor"));
            this.textColor = parseColorWithValidation(defaultTheme.getString("textColor"));
            this.primaryColor = parseColorWithValidation(defaultTheme.getString("primaryColor"));
            this.secondaryColor = parseColorWithValidation(defaultTheme.getString("secondaryColor"));
            this.headerColor = parseColorWithValidation(defaultTheme.getString("headerColor"));
            this.buttonBackgroundColor = parseColorWithValidation(defaultTheme.getString("buttonBackgroundColor"));
            this.buttonTextColor = parseColorWithValidation(defaultTheme.getString("buttonTextColor"));
            this.cardBackgroundColor = parseColorWithValidation(defaultTheme.getString("cardBackgroundColor"));
            this.borderColor = parseColorWithValidation(defaultTheme.getString("borderColor"));
            this.errorColor = parseColorWithValidation(defaultTheme.getString("errorColor"));
            this.successColor = parseColorWithValidation(defaultTheme.getString("successColor"));
            this.emoji = defaultTheme.getString("emoji");
            this.themeName = defaultTheme.getString("themeName");
            
            Log.i(TAG, "Successfully loaded default theme from backup file");
            
        } catch (JSONException | IllegalArgumentException | IOException e) {
            // This is a critical error - the default theme file is corrupted or missing
            String errorMsg = "CRITICAL ERROR: Default theme file (default_theme.json) is invalid or missing. " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
    }
    
    /**
     * Loads and parses the default theme JSON file from the assets folder.
     * 
     * @return JSONObject containing the default theme
     * @throws IOException if the file cannot be read
     * @throws JSONException if the file contents are not valid JSON
     */
    private static JSONObject loadDefaultThemeFromAssets() throws IOException, JSONException {
        AssetManager assetManager = appContext.getAssets();
        InputStream inputStream = assetManager.open(DEFAULT_THEME_FILE);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        
        reader.close();
        inputStream.close();
        
        String jsonString = jsonBuilder.toString();
        return new JSONObject(jsonString);
    }
    
    /**
     * Notifies the user via Toast and logs a message.
     * This is called when the theme parser falls back to the default theme.
     * 
     * @param message Message to display to the user
     */
    private static void notifyUser(String message) {
        Log.w(TAG, "User notification: " + message);
        
        // Try to show a toast on the UI thread
        if (appContext != null) {
            android.os.Handler mainHandler = new android.os.Handler(appContext.getMainLooper());
            mainHandler.post(() -> {
                Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
            });
        }
    }
    
    // ==================== Getter methods for all theme properties ====================
    // All fields are private and only accessible through getters (no setters)
    
    public int getBackgroundColor() {
        return backgroundColor;
    }
    
    public int getTextColor() {
        return textColor;
    }
    
    public int getPrimaryColor() {
        return primaryColor;
    }
    
    public int getSecondaryColor() {
        return secondaryColor;
    }
    
    public int getHeaderColor() {
        return headerColor;
    }
    
    public int getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }
    
    public int getButtonTextColor() {
        return buttonTextColor;
    }
    
    public int getCardBackgroundColor() {
        return cardBackgroundColor;
    }
    
    public int getBorderColor() {
        return borderColor;
    }
    
    public int getErrorColor() {
        return errorColor;
    }
    
    public int getSuccessColor() {
        return successColor;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getThemeName() {
        return themeName;
    }
    
    // ==================== Utility methods ====================
    
    /**
     * Creates a sample theme JSON for testing or as a template.
     * 
     * @return JSON string with a complete theme specification
     */
    public static String createSampleThemeJson() {
        JSONObject theme = new JSONObject();
        try {
            theme.put("themeName", "Ocean Breeze");
            theme.put("backgroundColor", "#E3F2FD");
            theme.put("textColor", "#0D47A1");
            theme.put("primaryColor", "#1976D2");
            theme.put("secondaryColor", "#BBDEFB");
            theme.put("headerColor", "#0D47A1");
            theme.put("buttonBackgroundColor", "#1976D2");
            theme.put("buttonTextColor", "#FFFFFF");
            theme.put("cardBackgroundColor", "#FFFFFF");
            theme.put("borderColor", "#90CAF9");
            theme.put("errorColor", "#D32F2F");
            theme.put("successColor", "#388E3C");
            theme.put("emoji", "🌊");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating sample theme JSON");
        }
        return theme.toString();
    }
}

