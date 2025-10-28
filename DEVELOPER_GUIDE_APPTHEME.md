# AppTheme System - Developer Guide

## 📋 Table of Contents

- [Quick Start](#quick-start)
- [Overview](#overview)
- [Using AppTheme in Your Activities](#using-apptheme-in-your-activities)
- [Theme Specification](#theme-specification)
- [Creating Custom Themes](#creating-custom-themes)
- [LLM Integration](#llm-integration)
- [Testing](#testing)
- [API Reference](#api-reference)
- [Examples](#examples)
- [Troubleshooting](#troubleshooting)

---

## Quick Start

### 1. Run the Tests

```bash
./gradlew test
```

### 2. Build the Project

```bash
./gradlew clean build
```

### 3. Use in Your Activity

```java
public class MyActivity extends ThemedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        
        // Apply theme to all UI components
        applyThemeToActivity();
        
        // Access theme colors
        int bgColor = theme.getBackgroundColor();
        int textColor = theme.getTextColor();
    }
}
```

---

## Overview

The **AppTheme system** provides a centralized, singleton-based theme management solution for the entire application. It supports:

- ✅ Global theme configuration via JSON
- ✅ LLM-generated theme specifications
- ✅ Automatic UI theming across all activities
- ✅ Fallback to default theme on invalid data
- ✅ 13+ customizable theme properties
- ✅ Comprehensive validation and error handling

### Architecture

```
┌─────────────────────┐
│  AppTheme.java      │ ← Global singleton theme manager
└──────────┬──────────┘
           │
           ├─────────────────────────────────┐
           │                                 │
┌──────────▼──────────┐          ┌──────────▼──────────┐
│ ThemedActivity.java │          │  default_theme.json │
│ (Base class)        │          │  (Backup theme)     │
└──────────┬──────────┘          └─────────────────────┘
           │
    ┌──────┴──────┐
    ▼             ▼
MainActivity  DetailsActivity  (and all other activities)
```

### Core Components

| Component | Purpose |
|-----------|---------|
| `AppTheme.java` | Global theme manager (singleton pattern) |
| `ThemedActivity.java` | Base class for all themed activities |
| `WeatherApplication.java` | Initializes theme singleton on app startup |
| `default_theme.json` | Backup theme used when user theme is invalid |

---

## Using AppTheme in Your Activities

### Step 1: Extend ThemedActivity

Instead of extending `AppCompatActivity`, extend `ThemedActivity`:

```java
public class MyActivity extends ThemedActivity {
    // Your activity code
}
```

### Step 2: Apply Theme in onCreate

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my);
    
    // This applies the theme to all views in the layout
    applyThemeToActivity();
    
    // Your initialization code...
}
```

### Step 3: Access Theme Properties

```java
// Get the singleton instance
AppTheme theme = AppTheme.getInstance();

// Use theme colors
view.setBackgroundColor(theme.getBackgroundColor());
textView.setTextColor(theme.getTextColor());
button.setBackgroundTintList(ColorStateList.valueOf(theme.getButtonBackgroundColor()));
```

### Available Helper Methods (from ThemedActivity)

```java
// Apply card-like styling to a view
applyCardStyle(myCardView);

// Adjust alpha/transparency of a color
int semiTransparent = adjustAlpha(theme.getPrimaryColor(), 0.5f);

// Apply theme to a specific view and its children
applyThemeToView(myViewGroup);
```

---

## Theme Specification

### Required Fields

Every theme **must** include these 13 fields:

| Field | Type | Description |
|-------|------|-------------|
| `themeName` | String | Name of the theme |
| `backgroundColor` | Hex Color | Main background color for activities |
| `textColor` | Hex Color | Primary text color |
| `primaryColor` | Hex Color | Accent/primary color for important UI elements |
| `secondaryColor` | Hex Color | Secondary color for cards, panels |
| `headerColor` | Hex Color | Color for headers and titles |
| `buttonBackgroundColor` | Hex Color | Background color for buttons |
| `buttonTextColor` | Hex Color | Text color for buttons |
| `cardBackgroundColor` | Hex Color | Background color for card-like elements |
| `borderColor` | Hex Color | Color for borders and dividers |
| `errorColor` | Hex Color | Color for error messages |
| `successColor` | Hex Color | Color for success messages |
| `emoji` | String | Decorative emoji matching the theme |

### Color Format

Colors must be in **hexadecimal format**:
- With hash: `#RRGGBB` or `#AARRGGBB`
- Without hash: `RRGGBB` or `AARRGGBB`

Examples: `#FF5733`, `#80FF5733`, `FF5733`

---

## Creating Custom Themes

### JSON Format

```json
{
  "themeName": "Ocean Breeze",
  "backgroundColor": "#E3F2FD",
  "textColor": "#0D47A1",
  "primaryColor": "#1976D2",
  "secondaryColor": "#BBDEFB",
  "headerColor": "#0D47A1",
  "buttonBackgroundColor": "#1976D2",
  "buttonTextColor": "#FFFFFF",
  "cardBackgroundColor": "#FFFFFF",
  "borderColor": "#90CAF9",
  "errorColor": "#D32F2F",
  "successColor": "#388E3C",
  "emoji": "🌊"
}
```

### Programmatically Update Theme

```java
// Create theme JSON
JSONObject newTheme = new JSONObject();
newTheme.put("themeName", "My Custom Theme");
newTheme.put("backgroundColor", "#FFFFFF");
newTheme.put("textColor", "#000000");
// ... add other fields

// Update the global theme
AppTheme.updateTheme(newTheme);

// Restart activity to apply changes
recreate();
```

### Sample Theme for Testing

```java
String sampleJson = AppTheme.createSampleThemeJson();
// This creates an "Ocean Breeze" theme for testing
```

---

## LLM Integration

The theme system is designed for **LLM-generated themes**. Here's how to integrate:

### Step 1: Create a Prompt

```
Create a theme for a weather app based on: "Tropical sunset with warm, vibrant colors"

Return a JSON object with these properties (all colors in hex format):
- themeName: Short descriptive name
- backgroundColor: Main screen background
- textColor: Primary text color
- primaryColor: Accent color
- secondaryColor: Secondary UI color
- headerColor: Headers and titles
- buttonBackgroundColor: Button background
- buttonTextColor: Button text
- cardBackgroundColor: Card backgrounds
- borderColor: Borders and dividers
- errorColor: Error messages
- successColor: Success messages
- emoji: Single emoji representing the theme

Ensure colors have good contrast (WCAG AA). Return ONLY valid JSON.
```

### Step 2: Parse LLM Response

```java
// Get response from LLM API
String llmResponse = callLLMAPI(userDescription);

try {
    // Parse and validate
    JSONObject themeJson = new JSONObject(llmResponse);
    
    // Update theme
    AppTheme.updateTheme(themeJson);
    
    // Store in database
    updateUserThemeInDatabase(userId, llmResponse);
    
    // Apply to UI
    recreate(); // Restart activity
    
} catch (JSONException e) {
    Toast.makeText(this, "Invalid theme generated", Toast.LENGTH_SHORT).show();
}
```

### Example LLM Responses

#### Dark Ocean Theme
```json
{
  "themeName": "Midnight Ocean",
  "backgroundColor": "#0A1929",
  "textColor": "#E3F2FD",
  "primaryColor": "#1E88E5",
  "secondaryColor": "#1C2C3F",
  "headerColor": "#90CAF9",
  "buttonBackgroundColor": "#1565C0",
  "buttonTextColor": "#FFFFFF",
  "cardBackgroundColor": "#13263A",
  "borderColor": "#1E3A5F",
  "errorColor": "#EF5350",
  "successColor": "#66BB6A",
  "emoji": "🌊"
}
```

#### Desert Sun Theme
```json
{
  "themeName": "Desert Sun",
  "backgroundColor": "#FFF8E1",
  "textColor": "#5D4037",
  "primaryColor": "#FF6F00",
  "secondaryColor": "#FFECB3",
  "headerColor": "#E65100",
  "buttonBackgroundColor": "#FB8C00",
  "buttonTextColor": "#FFFFFF",
  "cardBackgroundColor": "#FFFDE7",
  "borderColor": "#FFE082",
  "errorColor": "#D32F2F",
  "successColor": "#7CB342",
  "emoji": "☀️"
}
```

### Best Practices for LLM Integration

1. **Validate Output**: Always check if JSON is valid before applying
2. **Contrast Checking**: Ensure text is readable on backgrounds (WCAG AA: 4.5:1)
3. **Error Handling**: Fall back to default theme if generation fails
4. **User Feedback**: Show loading states during generation
5. **Rate Limiting**: Implement rate limits for API calls
6. **Async Operations**: Generate themes off the main UI thread

---

## Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Tests

```bash
# Test missing parameter fallback
./gradlew test --tests AppThemeTest.testMissingParameter_FallsBackToDefault

# Test invalid format fallback
./gradlew test --tests AppThemeTest.testInvalidFormat_FallsBackToDefault

# Test corrupt backup error
./gradlew test --tests AppThemeCorruptBackupTest.testCorruptBackupJson_ThrowsIllegalStateException
```

### Test Coverage

- **Total Tests**: 16
- **Test Files**: 2
  - `AppThemeTest.java` (11 tests)
  - `AppThemeCorruptBackupTest.java` (5 tests)
- **Coverage**: 100% of AppTheme methods

### Test Acceptance Criteria ✅

| Criterion | Status |
|-----------|--------|
| Class ignores data if missing parameter → Uses default | ✅ PASS |
| Class ignores data if incorrect format → Uses default | ✅ PASS |
| Class throws error if backup is corrupt → IllegalStateException | ✅ PASS |

### Manual Testing

#### Test 1: App with Default Theme
1. Clear app data
2. Launch app
3. Verify white background with dark text
4. Check logcat: `"Successfully loaded default theme"`

#### Test 2: Custom Theme Applied
1. Update user's `theme_json` in database
2. Login as that user
3. Verify custom colors are applied

#### Test 3: Invalid Theme Fallback
1. Set user's `theme_json` to invalid JSON
2. Login
3. Verify default theme is applied
4. Check for Toast notification

---

## API Reference

### AppTheme Class

#### Singleton Access
```java
AppTheme theme = AppTheme.getInstance();
```

#### Update Theme
```java
AppTheme.updateTheme(JSONObject themeJson);
```

#### Getters
```java
String getThemeName()
int getBackgroundColor()
int getTextColor()
int getPrimaryColor()
int getSecondaryColor()
int getHeaderColor()
int getButtonBackgroundColor()
int getButtonTextColor()
int getCardBackgroundColor()
int getBorderColor()
int getErrorColor()
int getSuccessColor()
String getEmoji()
```

#### Utility Methods
```java
static String createSampleThemeJson()  // Returns sample "Ocean Breeze" theme
```

### ThemedActivity Class

#### Methods Available in Your Activities

```java
// Apply theme to the entire activity
protected void applyThemeToActivity()

// Apply theme to a specific view hierarchy
protected void applyThemeToView(View view)

// Apply card-like styling to a view
protected void applyCardStyle(View view)

// Adjust alpha/transparency of a color
protected int adjustAlpha(int color, float factor)
```

---

## Examples

### Example 1: Simple Themed Activity

```java
public class MyActivity extends ThemedActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        applyThemeToActivity();
    }
}
```

### Example 2: Custom Button Styling

```java
Button myButton = findViewById(R.id.my_button);
myButton.setBackgroundTintList(ColorStateList.valueOf(theme.getPrimaryColor()));
myButton.setTextColor(theme.getButtonTextColor());
```

### Example 3: Card with Theme Colors

```java
CardView card = findViewById(R.id.my_card);
applyCardStyle(card); // Applies card background and border colors
```

### Example 4: Error Message with Theme Color

```java
TextView errorText = findViewById(R.id.error_message);
errorText.setTextColor(theme.getErrorColor());
errorText.setText("An error occurred!");
```

### Example 5: Dynamic View Creation

```java
TextView dynamicText = new TextView(this);
dynamicText.setTextColor(theme.getTextColor());
dynamicText.setBackgroundColor(theme.getCardBackgroundColor());
container.addView(dynamicText);
```

### Example 6: Theme Preview Before Applying

```java
// Preview theme colors before applying
private void showThemePreview(JSONObject themeJson) {
    AppTheme previewTheme = new AppTheme(themeJson, this);
    
    previewBox.setBackgroundColor(previewTheme.getBackgroundColor());
    previewText.setTextColor(previewTheme.getTextColor());
    previewButton.setBackgroundTintList(
        ColorStateList.valueOf(previewTheme.getPrimaryColor())
    );
}
```

---

## Troubleshooting

### Tests Won't Run

```bash
./gradlew clean
./gradlew build
./gradlew test
```

### Build Fails

**Check if `default_theme.json` exists:**
```bash
cat app/src/main/assets/default_theme.json
```

**Validate JSON:**
```bash
cat app/src/main/assets/default_theme.json | python -m json.tool
```

### App Crashes on Start

**Check logcat for AppTheme errors:**
```bash
adb logcat | grep AppTheme
```

**Common issues:**
- Missing `default_theme.json` in assets
- Corrupt backup JSON file
- Invalid JSON format in user theme

### Theme Not Applying

1. Ensure activity extends `ThemedActivity`
2. Call `applyThemeToActivity()` in `onCreate()`
3. Verify theme is loaded (check `User.getInstance().getThemeJson()`)
4. Try restarting the activity: `recreate()`

### Colors Look Wrong

- Check hex color format (should be `#RRGGBB`)
- Verify contrast ratio (use online WCAG checker)
- Check if `applyThemeToView()` was called on dynamic views
- Look for hardcoded colors in XML layouts

### Validation Logic

The theme system validates themes in this order:

1. **Check all required fields present** → If missing, load default theme
2. **Validate color formats** → If invalid, load default theme
3. **Load default theme from assets** → If default is corrupt, throw IllegalStateException
4. **Notify user via Toast** → When falling back to default

---

## File Locations

### Implementation Files

| File | Location |
|------|----------|
| AppTheme.java | `app/src/main/java/edu/uiuc/cs427app/AppTheme.java` |
| ThemedActivity.java | `app/src/main/java/edu/uiuc/cs427app/ThemedActivity.java` |
| WeatherApplication.java | `app/src/main/java/edu/uiuc/cs427app/WeatherApplication.java` |
| default_theme.json | `app/src/main/assets/default_theme.json` |

### Test Files

| File | Location |
|------|----------|
| AppThemeTest.java | `app/src/test/java/edu/uiuc/cs427app/AppThemeTest.java` |
| AppThemeCorruptBackupTest.java | `app/src/test/java/edu/uiuc/cs427app/AppThemeCorruptBackupTest.java` |

### Documentation

| File | Location |
|------|----------|
| Implementation Summary | `docs/yuhaoc7/IMPLEMENTATION_SUMMARY.md` |
| Singleton Guide | `docs/yuhaoc7/SINGLETON_IMPLEMENTATION.md` |
| Theme Documentation | `docs/yuhaoc7/THEME_DOCUMENTATION.md` |
| LLM Integration Examples | `docs/yuhaoc7/LLM_THEME_INTEGRATION_EXAMPLE.md` |
| Test Execution Guide | `docs/yuhaoc7/TEST_EXECUTION_GUIDE.md` |

---

## Activities Using Theme System

All activities in the app use the theme system:

- ✅ **MainActivity** - City list screen
- ✅ **DetailsActivity** - City weather details
- ✅ **LoginActivity** - User login
- ✅ **RegisterActivity** - User registration
- ✅ **AddCityActivity** - Add new city

---

## Future Enhancements

Potential improvements for the theme system:

- Live theme preview in settings
- Multiple pre-defined themes to choose from
- Theme editor UI
- Dark mode toggle
- Per-screen theme overrides
- Animation support for theme changes
- Font customization
- Icon set customization
- Theme sharing between users
- Theme marketplace

---

## Support and Resources

### Quick Reference

- **Task**: SCRUM-15 - Create Global Singleton Theme Class
- **Status**: ✅ COMPLETED
- **Tests**: 16/16 passing
- **Coverage**: 100%

### Documentation

For more detailed information, see:

- `docs/yuhaoc7/SINGLETON_IMPLEMENTATION.md` - Complete implementation details
- `docs/yuhaoc7/TEST_EXECUTION_GUIDE.md` - Comprehensive testing guide
- `docs/yuhaoc7/LLM_THEME_INTEGRATION_EXAMPLE.md` - LLM integration examples
- `docs/yuhaoc7/SCRUM-15_COMPLETION_SUMMARY.md` - Acceptance criteria verification

### Getting Help

If you encounter issues:

1. Check this guide's troubleshooting section
2. Review the detailed documentation in `docs/yuhaoc7/`
3. Run the test suite to verify implementation
4. Check logcat output for error messages

---

## Contributing

When adding new activities or UI components:

1. **Extend ThemedActivity** instead of AppCompatActivity
2. **Call applyThemeToActivity()** in onCreate()
3. **Use theme colors** instead of hardcoded values
4. **Test with multiple themes** to ensure compatibility
5. **Update tests** if you modify AppTheme or ThemedActivity

### Code Style

```java
// ✅ GOOD: Using theme colors
view.setBackgroundColor(theme.getBackgroundColor());

// ❌ BAD: Hardcoded colors
view.setBackgroundColor(Color.WHITE);
```

```java
// ✅ GOOD: Dynamic theming
textView.setTextColor(theme.getTextColor());

// ❌ BAD: Hardcoded in XML
android:textColor="#000000"  <!-- Avoid this -->
```

---

## Summary

The AppTheme system provides a robust, centralized theme management solution that:

- ✅ Supports LLM-generated themes
- ✅ Provides graceful fallbacks for invalid data
- ✅ Offers 13+ customizable properties
- ✅ Applies themes automatically across all activities
- ✅ Includes comprehensive validation and error handling
- ✅ Has 100% test coverage

**Get started in 3 steps:**
1. Extend `ThemedActivity`
2. Call `applyThemeToActivity()`
3. Use `theme.get*()` methods for colors

Happy theming! 🎨

---

**Last Updated**: October 2025  
**Version**: 1.0  
**Branch**: yuhaoc7/AppTheme

