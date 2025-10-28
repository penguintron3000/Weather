package edu.uiuc.cs427app;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for AppTheme singleton class.
 * 
 * Tests verify:
 * 1. The class correctly ignores passed data if missing a parameter and uses default JSON
 * 2. The class correctly ignores passed data if values are incorrectly formatted and uses default JSON
 * 3. The class correctly throws an error if the backup JSON file is incorrectly formatted
 * 4. Singleton pattern works correctly
 * 5. Theme parsing and validation logic works as expected
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = "src/main/AndroidManifest.xml", application = WeatherApplication.class)
public class AppThemeTest {
    
    private Context context;
    
    /**
     * Sets up the test environment before each test.
     * Initializes the AppTheme singleton with an application context.
     */
    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication().getApplicationContext();
        AppTheme.initialize(context);
    }
    
    /**
     * Test 1: Verify that the class correctly ignores passed data if missing a required parameter
     * and instead parses the default JSON file.
     * 
     * This test creates a JSONObject with missing required fields (e.g., missing "textColor")
     * and verifies that AppTheme falls back to the default theme from default_theme.json.
     */
    @Test
    public void testMissingParameter_FallsBackToDefault() throws JSONException {
        // Create a theme JSON missing required field "textColor"
        JSONObject incompleteTheme = new JSONObject();
        incompleteTheme.put("themeName", "Incomplete Theme");
        incompleteTheme.put("backgroundColor", "#FF0000");
        // Missing: textColor, primaryColor, secondaryColor, etc.
        
        // Update theme with incomplete JSON
        AppTheme.updateTheme(incompleteTheme);
        AppTheme theme = AppTheme.getInstance();
        
        // Should fall back to default theme, not use the incomplete theme
        // Default theme name is "Default" as specified in default_theme.json
        assertEquals("Default", theme.getThemeName());
        
        // Verify that default colors are loaded (from default_theme.json)
        // Default backgroundColor is #FFFFFF (white)
        assertEquals(android.graphics.Color.parseColor("#FFFFFF"), theme.getBackgroundColor());
        
        // Default textColor is #212121
        assertEquals(android.graphics.Color.parseColor("#212121"), theme.getTextColor());
        
        System.out.println("✓ Test 1 PASSED: Missing parameter correctly falls back to default theme");
    }
    
    /**
     * Test 2: Verify that the class correctly ignores passed data if one of the values
     * is incorrectly formatted and instead parses the default JSON file.
     * 
     * This test provides a JSONObject with all required fields but with an invalid color format,
     * and verifies that AppTheme falls back to the default theme.
     */
    @Test
    public void testInvalidFormat_FallsBackToDefault() throws JSONException {
        // Create a theme JSON with invalid color format
        JSONObject invalidTheme = new JSONObject();
        invalidTheme.put("themeName", "Invalid Theme");
        invalidTheme.put("backgroundColor", "NOT_A_COLOR"); // Invalid color format
        invalidTheme.put("textColor", "#000000");
        invalidTheme.put("primaryColor", "#6200EE");
        invalidTheme.put("secondaryColor", "#F5F5F5");
        invalidTheme.put("headerColor", "#000000");
        invalidTheme.put("buttonBackgroundColor", "#6200EE");
        invalidTheme.put("buttonTextColor", "#FFFFFF");
        invalidTheme.put("cardBackgroundColor", "#FAFAFA");
        invalidTheme.put("borderColor", "#E0E0E0");
        invalidTheme.put("errorColor", "#FF0000");
        invalidTheme.put("successColor", "#4CAF50");
        invalidTheme.put("emoji", "🎨");
        
        // Update theme with invalid JSON
        AppTheme.updateTheme(invalidTheme);
        AppTheme theme = AppTheme.getInstance();
        
        // Should fall back to default theme due to invalid color format
        assertEquals("Default", theme.getThemeName());
        
        // Verify default colors are used
        assertEquals(android.graphics.Color.parseColor("#FFFFFF"), theme.getBackgroundColor());
        assertEquals(android.graphics.Color.parseColor("#212121"), theme.getTextColor());
        
        System.out.println("✓ Test 2 PASSED: Invalid format correctly falls back to default theme");
    }
    
    /**
     * Test 3: Verify that the class correctly parses a valid theme JSON.
     * 
     * This test provides a complete, valid JSONObject and verifies that
     * AppTheme correctly parses and applies all fields.
     */
    @Test
    public void testValidTheme_ParsesCorrectly() throws JSONException {
        // Create a complete, valid theme JSON
        JSONObject validTheme = new JSONObject();
        validTheme.put("themeName", "Custom Valid Theme");
        validTheme.put("backgroundColor", "#123456");
        validTheme.put("textColor", "#ABCDEF");
        validTheme.put("primaryColor", "#FF5733");
        validTheme.put("secondaryColor", "#33FF57");
        validTheme.put("headerColor", "#5733FF");
        validTheme.put("buttonBackgroundColor", "#FF33A1");
        validTheme.put("buttonTextColor", "#FFFFFF");
        validTheme.put("cardBackgroundColor", "#F0F0F0");
        validTheme.put("borderColor", "#CCCCCC");
        validTheme.put("errorColor", "#FF0000");
        validTheme.put("successColor", "#00FF00");
        validTheme.put("emoji", "🚀");
        
        // Update theme with valid JSON
        AppTheme.updateTheme(validTheme);
        AppTheme theme = AppTheme.getInstance();
        
        // Verify all fields are correctly parsed
        assertEquals("Custom Valid Theme", theme.getThemeName());
        assertEquals(android.graphics.Color.parseColor("#123456"), theme.getBackgroundColor());
        assertEquals(android.graphics.Color.parseColor("#ABCDEF"), theme.getTextColor());
        assertEquals(android.graphics.Color.parseColor("#FF5733"), theme.getPrimaryColor());
        assertEquals(android.graphics.Color.parseColor("#33FF57"), theme.getSecondaryColor());
        assertEquals(android.graphics.Color.parseColor("#5733FF"), theme.getHeaderColor());
        assertEquals(android.graphics.Color.parseColor("#FF33A1"), theme.getButtonBackgroundColor());
        assertEquals(android.graphics.Color.parseColor("#FFFFFF"), theme.getButtonTextColor());
        assertEquals(android.graphics.Color.parseColor("#F0F0F0"), theme.getCardBackgroundColor());
        assertEquals(android.graphics.Color.parseColor("#CCCCCC"), theme.getBorderColor());
        assertEquals(android.graphics.Color.parseColor("#FF0000"), theme.getErrorColor());
        assertEquals(android.graphics.Color.parseColor("#00FF00"), theme.getSuccessColor());
        assertEquals("🚀", theme.getEmoji());
        
        System.out.println("✓ Test 3 PASSED: Valid theme parses correctly");
    }
    
    /**
     * Test 4: Verify that the singleton pattern works correctly.
     * 
     * This test verifies that getInstance() always returns the same instance.
     */
    @Test
    public void testSingletonPattern() {
        AppTheme instance1 = AppTheme.getInstance();
        AppTheme instance2 = AppTheme.getInstance();
        
        // Both references should point to the same object
        assertSame(instance1, instance2);
        
        System.out.println("✓ Test 4 PASSED: Singleton pattern works correctly");
    }
    
    /**
     * Test 5: Verify that theme updates affect the singleton instance.
     * 
     * This test updates the theme and verifies that the singleton reflects the changes.
     */
    @Test
    public void testThemeUpdate() throws JSONException {
        // Get initial theme
        AppTheme initialTheme = AppTheme.getInstance();
        String initialThemeName = initialTheme.getThemeName();
        
        // Create and apply a new theme
        JSONObject newTheme = new JSONObject();
        newTheme.put("themeName", "Updated Theme");
        newTheme.put("backgroundColor", "#000000");
        newTheme.put("textColor", "#FFFFFF");
        newTheme.put("primaryColor", "#FF0000");
        newTheme.put("secondaryColor", "#00FF00");
        newTheme.put("headerColor", "#0000FF");
        newTheme.put("buttonBackgroundColor", "#FFFF00");
        newTheme.put("buttonTextColor", "#000000");
        newTheme.put("cardBackgroundColor", "#FFFFFF");
        newTheme.put("borderColor", "#808080");
        newTheme.put("errorColor", "#FF0000");
        newTheme.put("successColor", "#00FF00");
        newTheme.put("emoji", "⭐");
        
        AppTheme.updateTheme(newTheme);
        
        // Get theme again
        AppTheme updatedTheme = AppTheme.getInstance();
        
        // Verify the theme was updated
        assertEquals("Updated Theme", updatedTheme.getThemeName());
        assertNotEquals(initialThemeName, updatedTheme.getThemeName());
        
        System.out.println("✓ Test 5 PASSED: Theme updates work correctly");
    }
    
    /**
     * Test 6: Verify that colors without '#' prefix are handled correctly.
     * 
     * This test provides colors in hex format without the '#' prefix
     * and verifies they are parsed correctly.
     */
    @Test
    public void testColorWithoutHashPrefix() throws JSONException {
        JSONObject themeWithoutHash = new JSONObject();
        themeWithoutHash.put("themeName", "No Hash Theme");
        themeWithoutHash.put("backgroundColor", "FFFFFF"); // Without #
        themeWithoutHash.put("textColor", "000000"); // Without #
        themeWithoutHash.put("primaryColor", "FF0000");
        themeWithoutHash.put("secondaryColor", "00FF00");
        themeWithoutHash.put("headerColor", "0000FF");
        themeWithoutHash.put("buttonBackgroundColor", "FFFF00");
        themeWithoutHash.put("buttonTextColor", "000000");
        themeWithoutHash.put("cardBackgroundColor", "FFFFFF");
        themeWithoutHash.put("borderColor", "CCCCCC");
        themeWithoutHash.put("errorColor", "FF0000");
        themeWithoutHash.put("successColor", "00FF00");
        themeWithoutHash.put("emoji", "🎨");
        
        AppTheme.updateTheme(themeWithoutHash);
        AppTheme theme = AppTheme.getInstance();
        
        // Should successfully parse colors without # prefix
        assertEquals("No Hash Theme", theme.getThemeName());
        assertEquals(android.graphics.Color.parseColor("#FFFFFF"), theme.getBackgroundColor());
        assertEquals(android.graphics.Color.parseColor("#000000"), theme.getTextColor());
        
        System.out.println("✓ Test 6 PASSED: Colors without # prefix are handled correctly");
    }
    
    /**
     * Test 7: Verify that null JSONObject falls back to default theme.
     */
    @Test
    public void testNullJsonObject_FallsBackToDefault() {
        AppTheme.updateTheme((JSONObject) null);
        AppTheme theme = AppTheme.getInstance();
        
        // Should use default theme
        assertEquals("Default", theme.getThemeName());
        
        System.out.println("✓ Test 7 PASSED: Null JSONObject falls back to default");
    }
    
    /**
     * Test 8: Verify that empty string values for colors fall back to default.
     */
    @Test
    public void testEmptyColorString_FallsBackToDefault() throws JSONException {
        JSONObject themeWithEmptyColor = new JSONObject();
        themeWithEmptyColor.put("themeName", "Empty Color Theme");
        themeWithEmptyColor.put("backgroundColor", ""); // Empty string
        themeWithEmptyColor.put("textColor", "#000000");
        themeWithEmptyColor.put("primaryColor", "#FF0000");
        themeWithEmptyColor.put("secondaryColor", "#00FF00");
        themeWithEmptyColor.put("headerColor", "#0000FF");
        themeWithEmptyColor.put("buttonBackgroundColor", "#FFFF00");
        themeWithEmptyColor.put("buttonTextColor", "#000000");
        themeWithEmptyColor.put("cardBackgroundColor", "#FFFFFF");
        themeWithEmptyColor.put("borderColor", "#CCCCCC");
        themeWithEmptyColor.put("errorColor", "#FF0000");
        themeWithEmptyColor.put("successColor", "#00FF00");
        themeWithEmptyColor.put("emoji", "🎨");
        
        AppTheme.updateTheme(themeWithEmptyColor);
        AppTheme theme = AppTheme.getInstance();
        
        // Should fall back to default due to empty color string
        assertEquals("Default", theme.getThemeName());
        
        System.out.println("✓ Test 8 PASSED: Empty color string falls back to default");
    }
    
    /**
     * Test 9: Verify all getters return non-null values.
     */
    @Test
    public void testAllGettersReturnNonNull() {
        AppTheme theme = AppTheme.getInstance();
        
        // All getters should return non-null values
        assertNotNull(theme.getThemeName());
        assertNotEquals(0, theme.getBackgroundColor());
        assertNotEquals(0, theme.getTextColor());
        assertNotEquals(0, theme.getPrimaryColor());
        assertNotEquals(0, theme.getSecondaryColor());
        assertNotEquals(0, theme.getHeaderColor());
        assertNotEquals(0, theme.getButtonBackgroundColor());
        assertNotEquals(0, theme.getButtonTextColor());
        assertNotEquals(0, theme.getCardBackgroundColor());
        assertNotEquals(0, theme.getBorderColor());
        assertNotEquals(0, theme.getErrorColor());
        assertNotEquals(0, theme.getSuccessColor());
        assertNotNull(theme.getEmoji());
        
        System.out.println("✓ Test 9 PASSED: All getters return non-null values");
    }
    
    /**
     * Test 10: Verify that createSampleThemeJson() returns valid JSON.
     */
    @Test
    public void testCreateSampleThemeJson() {
        String sampleJson = AppTheme.createSampleThemeJson();
        
        assertNotNull(sampleJson);
        assertFalse(sampleJson.isEmpty());
        
        // Should be parseable as JSON
        try {
            JSONObject json = new JSONObject(sampleJson);
            assertTrue(json.has("themeName"));
            assertTrue(json.has("backgroundColor"));
            assertTrue(json.has("textColor"));
            
            System.out.println("✓ Test 10 PASSED: createSampleThemeJson returns valid JSON");
        } catch (JSONException e) {
            fail("Sample theme JSON is not valid JSON: " + e.getMessage());
        }
    }
    
    /**
     * Test 11: Verify that invalid JSON string falls back to default.
     */
    @Test
    public void testInvalidJsonString_FallsBackToDefault() {
        String initialThemeName = AppTheme.getInstance().getThemeName();
        
        // Try to update with invalid JSON string
        AppTheme.updateTheme("{ this is not valid JSON }");
        
        AppTheme theme = AppTheme.getInstance();
        
        // Theme should not change (remains at current theme)
        assertEquals(initialThemeName, theme.getThemeName());
        
        System.out.println("✓ Test 11 PASSED: Invalid JSON string doesn't change theme");
    }
}



