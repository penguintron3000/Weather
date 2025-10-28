# App Theme System Documentation

## Overview

This app now includes a comprehensive global theme system that allows for customizable UI theming across all activities. The theme system supports LLM-generated theme specifications and provides a consistent look and feel throughout the application.

## Architecture

### Core Components

1. **`AppTheme.java`** - Global theme manager class
2. **`ThemedActivity.java`** - Base activity class for themed activities
3. **All Activity Classes** - Modified to extend `ThemedActivity`

## Theme Specification

### Required Elements

The theme system requires at least these two elements (as specified in the assignment):

- **backgroundColor** (hex format): Main background color for activities
- **textColor** (hex format): Primary text color

### Additional Supported Elements

The theme system supports many additional personalized elements:

- **primaryColor**: Accent/primary color for important UI elements
- **secondaryColor**: Secondary color for cards, panels, and subtle elements
- **headerColor**: Color for headers and titles
- **buttonBackgroundColor**: Background color for buttons
- **buttonTextColor**: Text color for buttons
- **cardBackgroundColor**: Background color for card-like elements
- **borderColor**: Color for borders and dividers
- **errorColor**: Color for error messages
- **successColor**: Color for success messages
- **emoji**: Optional decorative emoji for the theme
- **themeName**: Name of the theme

## Theme JSON Format

Themes are stored as JSON strings in the user's database record. Here's an example:

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

### Minimal Theme Example

For a minimal theme (meeting assignment requirements):

```json
{
  "backgroundColor": "#FFFFFF",
  "textColor": "#000000"
}
```

## Usage

### For Developers

#### Creating a New Themed Activity

1. Extend `ThemedActivity` instead of `AppCompatActivity`:

```java
public class MyActivity extends ThemedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        
        // Apply theme to all UI components
        applyThemeToActivity();
        
        // Your initialization code...
    }
}
```

#### Accessing Theme Properties

```java
int backgroundColor = theme.getBackgroundColor();
int textColor = theme.getTextColor();
int primaryColor = theme.getPrimaryColor();
// ... etc
```

#### Applying Card Styling

```java
View myCard = findViewById(R.id.my_card);
applyCardStyle(myCard);
```

#### Adjusting Color Alpha

```java
int semiTransparentColor = adjustAlpha(theme.getPrimaryColor(), 0.5f);
```

### For LLM Integration

When generating themes via LLM, the response should be a JSON object with the theme properties. Example prompt:

```
Create a theme based on the description: "Dark forest with mystical vibes"

Return a JSON object with at least:
- backgroundColor (hex)
- textColor (hex)

And optionally include:
- primaryColor, secondaryColor, headerColor
- buttonBackgroundColor, buttonTextColor
- cardBackgroundColor, borderColor
- errorColor, successColor
- emoji (decorative emoji matching the theme)
- themeName
```

## Default Theme

If no theme is specified or if the theme JSON is empty/invalid, the system uses a clean, modern default theme:

- Background: White (#FFFFFF)
- Text: Dark Gray (#212121)
- Primary: Purple (#6200EE)
- Secondary: Light Gray (#F5F5F5)
- Headers: Black (#000000)
- Buttons: Purple background (#6200EE) with white text
- Cards: Off-white (#FAFAFA)
- Borders: Light gray (#E0E0E0)
- Error: Red (#FF0000)
- Success: Green (#4CAF50)

## Activities Using Theme System

All activities in the app now use the theme system:

1. **MainActivity** - Main city list screen
2. **DetailsActivity** - City details screen
3. **LoginActivity** - User login screen
4. **RegisterActivity** - User registration screen
5. **AddCityActivity** - Add new city screen

## Technical Details

### Theme Loading Process

1. User logs in via `LoginActivity`
2. User's theme JSON is loaded from the database
3. Theme JSON is stored in `User.getInstance().getThemeJson()`
4. Each activity creates an `AppTheme` instance from this JSON in `onCreate()`
5. Theme is applied programmatically to all UI components via `applyThemeToActivity()`

### Theme Application

The `ThemedActivity.applyThemeToView()` method recursively traverses the view hierarchy and:

- Sets background colors for container views (ConstraintLayout, LinearLayout)
- Sets text colors for TextViews (with special handling for headers)
- Applies theme colors to Buttons (background tint and text color)
- Styles EditText fields with text and hint colors
- Handles dynamically created views (like city list items)

### Color Parsing

Colors are parsed from hex strings (with or without `#` prefix) in both `#RRGGBB` and `#AARRGGBB` formats. Invalid colors default to black with a logged error.

## Testing

To test the theme system:

1. Create a test user in the database
2. Set their `theme_json` field to a custom theme JSON
3. Log in as that user
4. Observe that all screens now use the custom theme

### Sample Theme for Testing

Use `AppTheme.createSampleThemeJson()` to generate a sample "Ocean Breeze" theme for testing.

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

## Implementation Notes

- Theme is loaded once per activity in `onCreate()`
- Theme changes require activity restart to take effect
- Dynamically created views (e.g., city list items) have theme applied explicitly
- Error and success messages use theme colors for consistency
- Default fallback ensures app always has a working theme

