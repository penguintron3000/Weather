package edu.uiuc.cs427app;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * AppTheme is a global theme manager class that provides consistent theming across the entire app.
 * It parses theme specifications from JSON (which can be LLM-generated) and provides easy access
 * to theme properties throughout the application.
 * 
 * The theme supports the following elements:
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
 * 
 * All colors default to a clean, modern theme if not specified.
 */
public class AppTheme {
    private static final String TAG = "AppTheme";
    
    // Default theme colors (clean, modern look)
    private static final String DEFAULT_BACKGROUND_COLOR = "#FFFFFF";
    private static final String DEFAULT_TEXT_COLOR = "#212121";
    private static final String DEFAULT_PRIMARY_COLOR = "#6200EE";
    private static final String DEFAULT_SECONDARY_COLOR = "#F5F5F5";
    private static final String DEFAULT_HEADER_COLOR = "#000000";
    private static final String DEFAULT_BUTTON_BACKGROUND_COLOR = "#6200EE";
    private static final String DEFAULT_BUTTON_TEXT_COLOR = "#FFFFFF";
    private static final String DEFAULT_CARD_BACKGROUND_COLOR = "#FAFAFA";
    private static final String DEFAULT_BORDER_COLOR = "#E0E0E0";
    private static final String DEFAULT_ERROR_COLOR = "#FF0000";
    private static final String DEFAULT_SUCCESS_COLOR = "#4CAF50";
    
    // Theme properties
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
     * Creates an AppTheme instance from a JSON string.
     * If the JSON is null, empty, or invalid, defaults to the standard theme.
     * 
     * @param themeJson JSON string containing theme specifications
     */
    public AppTheme(String themeJson) {
        loadTheme(themeJson);
    }
    
    /**
     * Creates an AppTheme instance with default theme.
     */
    public AppTheme() {
        this(null);
    }
    
    /**
     * Parses the theme JSON and loads theme properties.
     * Falls back to defaults for any missing or invalid properties.
     * 
     * @param themeJson JSON string containing theme specifications
     */
    private void loadTheme(String themeJson) {
        // Set defaults first
        backgroundColor = parseColor(DEFAULT_BACKGROUND_COLOR);
        textColor = parseColor(DEFAULT_TEXT_COLOR);
        primaryColor = parseColor(DEFAULT_PRIMARY_COLOR);
        secondaryColor = parseColor(DEFAULT_SECONDARY_COLOR);
        headerColor = parseColor(DEFAULT_HEADER_COLOR);
        buttonBackgroundColor = parseColor(DEFAULT_BUTTON_BACKGROUND_COLOR);
        buttonTextColor = parseColor(DEFAULT_BUTTON_TEXT_COLOR);
        cardBackgroundColor = parseColor(DEFAULT_CARD_BACKGROUND_COLOR);
        borderColor = parseColor(DEFAULT_BORDER_COLOR);
        errorColor = parseColor(DEFAULT_ERROR_COLOR);
        successColor = parseColor(DEFAULT_SUCCESS_COLOR);
        emoji = "";
        themeName = "Default";
        
        // If no theme JSON provided, use defaults
        if (themeJson == null || themeJson.trim().isEmpty() || themeJson.equals("{}")) {
            Log.i(TAG, "Using default theme");
            return;
        }
        
        // Parse JSON and override defaults
        try {
            JSONObject json = new JSONObject(themeJson);
            
            // Parse each theme property, keeping defaults if not present
            if (json.has("backgroundColor")) {
                backgroundColor = parseColor(json.getString("backgroundColor"));
            }
            if (json.has("textColor")) {
                textColor = parseColor(json.getString("textColor"));
            }
            if (json.has("primaryColor")) {
                primaryColor = parseColor(json.getString("primaryColor"));
            }
            if (json.has("secondaryColor")) {
                secondaryColor = parseColor(json.getString("secondaryColor"));
            }
            if (json.has("headerColor")) {
                headerColor = parseColor(json.getString("headerColor"));
            }
            if (json.has("buttonBackgroundColor")) {
                buttonBackgroundColor = parseColor(json.getString("buttonBackgroundColor"));
            }
            if (json.has("buttonTextColor")) {
                buttonTextColor = parseColor(json.getString("buttonTextColor"));
            }
            if (json.has("cardBackgroundColor")) {
                cardBackgroundColor = parseColor(json.getString("cardBackgroundColor"));
            }
            if (json.has("borderColor")) {
                borderColor = parseColor(json.getString("borderColor"));
            }
            if (json.has("errorColor")) {
                errorColor = parseColor(json.getString("errorColor"));
            }
            if (json.has("successColor")) {
                successColor = parseColor(json.getString("successColor"));
            }
            if (json.has("emoji")) {
                emoji = json.getString("emoji");
            }
            if (json.has("themeName")) {
                themeName = json.getString("themeName");
            }
            
            Log.i(TAG, "Loaded custom theme: " + themeName);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing theme JSON, using defaults: " + e.getMessage());
        }
    }
    
    /**
     * Parses a hex color string to an Android color integer.
     * Supports both #RRGGBB and #AARRGGBB formats.
     * Returns black if parsing fails.
     * 
     * @param hexColor Hex color string (e.g., "#FF5733")
     * @return Android color integer
     */
    private int parseColor(String hexColor) {
        try {
            // Ensure the color starts with #
            if (!hexColor.startsWith("#")) {
                hexColor = "#" + hexColor;
            }
            return Color.parseColor(hexColor);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid color format: " + hexColor + ", using black");
            return Color.BLACK;
        }
    }
    
    // Getter methods for all theme properties
    
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

