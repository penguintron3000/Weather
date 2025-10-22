package edu.uiuc.cs427app;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test class specifically for testing AppTheme behavior when the backup default_theme.json
 * file is corrupted or invalid.
 * 
 * This test verifies that the class correctly throws an IllegalStateException when
 * the backup JSON file is incorrectly formatted, as required by the acceptance criteria.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class AppThemeCorruptBackupTest {
    
    private Context context;
    
    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication().getApplicationContext();
    }
    
    /**
     * Test: Verify that if the backup default_theme.json file is corrupted (invalid JSON),
     * the AppTheme class throws an IllegalStateException.
     * 
     * This test uses a mock context to simulate a corrupted default_theme.json file
     * and verifies that the appropriate exception is thrown.
     */
    @Test(expected = IllegalStateException.class)
    public void testCorruptBackupJson_ThrowsIllegalStateException() throws IOException {
        // Create a mock context with a corrupted default_theme.json
        Context mockContext = Mockito.spy(context);
        AssetManager mockAssetManager = Mockito.mock(AssetManager.class);
        
        // Simulate corrupted JSON file (invalid JSON syntax)
        String corruptJson = "{ this is not valid JSON }";
        InputStream corruptStream = new ByteArrayInputStream(corruptJson.getBytes());
        
        when(mockAssetManager.open(anyString())).thenReturn(corruptStream);
        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        
        // Initialize AppTheme with mock context
        AppTheme.initialize(mockContext);
        
        // Create an incomplete theme that will trigger fallback to default
        JSONObject incompleteTheme = new JSONObject();
        try {
            incompleteTheme.put("themeName", "Incomplete");
            // Missing required fields to trigger fallback
        } catch (JSONException e) {
            fail("Failed to create test JSON");
        }
        
        // This should throw IllegalStateException because the backup file is corrupt
        AppTheme.updateTheme(incompleteTheme);
        
        // If we reach here, the test fails (exception should have been thrown)
        fail("Expected IllegalStateException was not thrown for corrupt backup JSON");
    }
    
    /**
     * Test: Verify that if the backup default_theme.json file is missing required fields,
     * the AppTheme class throws an IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void testBackupJsonMissingFields_ThrowsIllegalStateException() throws IOException {
        // Create a mock context with an incomplete default_theme.json
        Context mockContext = Mockito.spy(context);
        AssetManager mockAssetManager = Mockito.mock(AssetManager.class);
        
        // Create JSON missing required fields
        String incompleteJson = "{\"themeName\": \"Incomplete\", \"backgroundColor\": \"#FFFFFF\"}";
        InputStream incompleteStream = new ByteArrayInputStream(incompleteJson.getBytes());
        
        when(mockAssetManager.open(anyString())).thenReturn(incompleteStream);
        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        
        // Initialize AppTheme with mock context
        AppTheme.initialize(mockContext);
        
        // Create an incomplete theme that will trigger fallback to default
        JSONObject incompleteTheme = new JSONObject();
        try {
            incompleteTheme.put("themeName", "Test");
            // Missing required fields
        } catch (JSONException e) {
            fail("Failed to create test JSON");
        }
        
        // This should throw IllegalStateException because the backup file is incomplete
        AppTheme.updateTheme(incompleteTheme);
        
        fail("Expected IllegalStateException was not thrown for incomplete backup JSON");
    }
    
    /**
     * Test: Verify that if the backup default_theme.json has invalid color formats,
     * the AppTheme class throws an IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void testBackupJsonInvalidColorFormat_ThrowsIllegalStateException() throws IOException {
        // Create a mock context with default_theme.json containing invalid color format
        Context mockContext = Mockito.spy(context);
        AssetManager mockAssetManager = Mockito.mock(AssetManager.class);
        
        // Create JSON with all fields but invalid color format
        String invalidColorJson = "{"
            + "\"themeName\": \"Invalid\","
            + "\"backgroundColor\": \"NOT_A_COLOR\","  // Invalid color
            + "\"textColor\": \"#000000\","
            + "\"primaryColor\": \"#6200EE\","
            + "\"secondaryColor\": \"#F5F5F5\","
            + "\"headerColor\": \"#000000\","
            + "\"buttonBackgroundColor\": \"#6200EE\","
            + "\"buttonTextColor\": \"#FFFFFF\","
            + "\"cardBackgroundColor\": \"#FAFAFA\","
            + "\"borderColor\": \"#E0E0E0\","
            + "\"errorColor\": \"#FF0000\","
            + "\"successColor\": \"#4CAF50\","
            + "\"emoji\": \"🎨\""
            + "}";
        InputStream invalidStream = new ByteArrayInputStream(invalidColorJson.getBytes());
        
        when(mockAssetManager.open(anyString())).thenReturn(invalidStream);
        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        
        // Initialize AppTheme with mock context
        AppTheme.initialize(mockContext);
        
        // Create an incomplete theme that will trigger fallback to default
        JSONObject incompleteTheme = new JSONObject();
        try {
            incompleteTheme.put("themeName", "Test");
            // Missing required fields to trigger fallback
        } catch (JSONException e) {
            fail("Failed to create test JSON");
        }
        
        // This should throw IllegalStateException because the backup file has invalid colors
        AppTheme.updateTheme(incompleteTheme);
        
        fail("Expected IllegalStateException was not thrown for invalid color format in backup JSON");
    }
    
    /**
     * Test: Verify that the error message is informative when backup JSON is corrupt.
     */
    @Test
    public void testCorruptBackupJson_HasInformativeErrorMessage() {
        Context mockContext = Mockito.spy(context);
        AssetManager mockAssetManager = Mockito.mock(AssetManager.class);
        
        try {
            // Simulate corrupted JSON file
            String corruptJson = "{ invalid json here }";
            InputStream corruptStream = new ByteArrayInputStream(corruptJson.getBytes());
            
            when(mockAssetManager.open(anyString())).thenReturn(corruptStream);
            when(mockContext.getAssets()).thenReturn(mockAssetManager);
            
            // Initialize AppTheme with mock context
            AppTheme.initialize(mockContext);
            
            // Create incomplete theme to trigger fallback
            JSONObject incompleteTheme = new JSONObject();
            incompleteTheme.put("themeName", "Test");
            
            // Try to update theme (should throw exception)
            AppTheme.updateTheme(incompleteTheme);
            
            fail("Should have thrown IllegalStateException");
            
        } catch (IllegalStateException e) {
            // Verify the error message is informative
            String message = e.getMessage();
            assertNotNull("Error message should not be null", message);
            assertTrue("Error message should mention 'CRITICAL ERROR'", 
                      message.contains("CRITICAL ERROR"));
            assertTrue("Error message should mention default theme file", 
                      message.toLowerCase().contains("default_theme.json"));
            
            System.out.println("✓ Test PASSED: Corrupt backup JSON throws informative error: " + message);
            
        } catch (Exception e) {
            fail("Wrong exception type thrown: " + e.getClass().getName());
        }
    }
    
    /**
     * Documentation test: Print summary of what happens when backup JSON is corrupt.
     */
    @Test
    public void testDocumentation_BackupJsonErrorHandling() {
        System.out.println("\n========================================");
        System.out.println("AppTheme Backup JSON Error Handling");
        System.out.println("========================================");
        System.out.println("When the backup default_theme.json file is corrupt or invalid:");
        System.out.println("1. AppTheme attempts to load it when user theme is invalid");
        System.out.println("2. Validates all required fields are present");
        System.out.println("3. Attempts to parse all color values");
        System.out.println("4. If ANY validation fails, throws IllegalStateException");
        System.out.println("5. Error message includes 'CRITICAL ERROR' and file name");
        System.out.println("6. Application cannot start without valid default theme");
        System.out.println("========================================\n");
        
        // This is a documentation test, always passes
        assertTrue(true);
    }
}



