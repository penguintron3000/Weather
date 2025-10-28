# LLM Theme Integration Example

## Overview

This document provides practical examples of how to integrate LLM-generated themes into the app's theme system.

## Integration Flow

```
User Description → LLM API → JSON Response → Parse & Validate → Store in DB → Apply Theme
```

## Example LLM Prompts

### Basic Prompt
```
I need a theme for a weather app. The user described their desired theme as: "{USER_DESCRIPTION}"

Generate a JSON theme with these properties (all colors in hex format):
- backgroundColor: Main screen background color
- textColor: Primary text color
- primaryColor: Accent color for important elements
- buttonBackgroundColor: Button background color
- buttonTextColor: Button text color
- headerColor: Header/title color
- emoji: A single emoji that represents the theme

Ensure colors have good contrast and are visually appealing.
Return ONLY valid JSON, no additional text.
```

### Advanced Prompt
```
Create a comprehensive theme for a weather application based on this description: "{USER_DESCRIPTION}"

Generate a JSON object with these properties:

Required (hex format):
- backgroundColor: Main background color
- textColor: Primary text color

Recommended (hex format):
- primaryColor: Accent/primary UI color
- secondaryColor: Secondary UI elements color
- headerColor: Headers and titles color
- buttonBackgroundColor: Button background
- buttonTextColor: Button text
- cardBackgroundColor: Card/panel background
- borderColor: Borders and dividers
- errorColor: Error messages
- successColor: Success messages

Optional:
- emoji: Single emoji representing the theme
- themeName: Short descriptive name

Guidelines:
1. Ensure good contrast ratios (WCAG AA minimum)
2. Colors should be cohesive and harmonious
3. Consider the weather app context
4. Make it visually appealing

Return ONLY valid JSON.
```

## Example User Descriptions → Expected Themes

### Example 1: "Dark ocean waves at midnight"

**Expected LLM Response:**
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

### Example 2: "Warm sunny day in the desert"

**Expected LLM Response:**
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

### Example 3: "Northern lights in winter"

**Expected LLM Response:**
```json
{
  "themeName": "Aurora Borealis",
  "backgroundColor": "#121826",
  "textColor": "#E8EAF6",
  "primaryColor": "#7E57C2",
  "secondaryColor": "#1E2839",
  "headerColor": "#B39DDB",
  "buttonBackgroundColor": "#5E35B1",
  "buttonTextColor": "#FFFFFF",
  "cardBackgroundColor": "#1A2332",
  "borderColor": "#4527A0",
  "errorColor": "#EF5350",
  "successColor": "#26C6DA",
  "emoji": "🌌"
}
```

### Example 4: "Cherry blossom spring"

**Expected LLM Response:**
```json
{
  "themeName": "Cherry Blossom",
  "backgroundColor": "#FFF0F5",
  "textColor": "#4A148C",
  "primaryColor": "#EC407A",
  "secondaryColor": "#FCE4EC",
  "headerColor": "#880E4F",
  "buttonBackgroundColor": "#D81B60",
  "buttonTextColor": "#FFFFFF",
  "cardBackgroundColor": "#FFFFFF",
  "borderColor": "#F8BBD0",
  "errorColor": "#D32F2F",
  "successColor": "#66BB6A",
  "emoji": "🌸"
}
```

## Implementation Example (Pseudocode)

### Java/Android Implementation

```java
public class ThemeGenerator {
    
    /**
     * Generates a theme from user description using LLM
     */
    public void generateAndApplyTheme(String userDescription, User user) {
        // 1. Call LLM API
        String llmResponse = callLLMAPI(userDescription);
        
        // 2. Validate JSON
        if (isValidThemeJson(llmResponse)) {
            // 3. Store in database
            updateUserTheme(user.getUserId(), llmResponse);
            
            // 4. Update user singleton
            user.setThemeJson(llmResponse);
            
            // 5. Restart activity to apply theme
            restartCurrentActivity();
        } else {
            showError("Failed to generate theme. Using default.");
        }
    }
    
    private String callLLMAPI(String userDescription) {
        // Example using OpenAI API
        String apiKey = "your-api-key";
        String model = "gpt-4";
        
        String prompt = String.format(
            "Create a theme for a weather app based on: \"%s\"\n\n" +
            "Return JSON with backgroundColor, textColor, primaryColor, " +
            "buttonBackgroundColor, buttonTextColor, headerColor, emoji, and themeName. " +
            "All colors in hex format. Return ONLY valid JSON.",
            userDescription
        );
        
        // Make API call
        // ... (implementation depends on HTTP library used)
        
        return jsonResponse;
    }
    
    private boolean isValidThemeJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            // Check required fields
            return obj.has("backgroundColor") && obj.has("textColor");
        } catch (JSONException e) {
            return false;
        }
    }
    
    private void updateUserTheme(long userId, String themeJson) {
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_THEME_JSON, themeJson);
        
        getContentResolver().update(
            UserContract.CONTENT_URI,
            values,
            UserContract.UserEntry.COLUMN_USER_ID + "=?",
            new String[]{String.valueOf(userId)}
        );
    }
}
```

### Theme Settings Activity Example

```java
public class ThemeSettingsActivity extends ThemedActivity {
    
    private EditText themeDescriptionInput;
    private Button generateButton;
    private ProgressBar loadingIndicator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_settings);
        applyThemeToActivity();
        
        themeDescriptionInput = findViewById(R.id.theme_description);
        generateButton = findViewById(R.id.generate_theme_button);
        loadingIndicator = findViewById(R.id.loading);
        
        generateButton.setOnClickListener(v -> generateTheme());
    }
    
    private void generateTheme() {
        String description = themeDescriptionInput.getText().toString().trim();
        
        if (description.isEmpty()) {
            Toast.makeText(this, "Please describe your desired theme", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        loadingIndicator.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);
        
        // Generate theme asynchronously
        new Thread(() -> {
            try {
                String themeJson = generateThemeFromLLM(description);
                
                // Update on UI thread
                runOnUiThread(() -> {
                    handleThemeGenerated(themeJson);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    handleThemeError(e);
                });
            }
        }).start();
    }
    
    private String generateThemeFromLLM(String description) throws Exception {
        // Call LLM API (implementation depends on chosen LLM service)
        // Return JSON string
        return llmApiCall(description);
    }
    
    private void handleThemeGenerated(String themeJson) {
        loadingIndicator.setVisibility(View.GONE);
        generateButton.setEnabled(true);
        
        // Validate and apply
        if (isValidThemeJson(themeJson)) {
            // Update database
            updateUserTheme(User.getInstance().getUserId(), themeJson);
            
            // Show success and restart
            Toast.makeText(this, "Theme generated! Restarting...", Toast.LENGTH_SHORT).show();
            
            // Restart app to apply theme
            new Handler().postDelayed(() -> {
                restartApp();
            }, 1000);
        } else {
            Toast.makeText(this, "Invalid theme generated. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handleThemeError(Exception e) {
        loadingIndicator.setVisibility(View.GONE);
        generateButton.setEnabled(true);
        Toast.makeText(this, "Error generating theme: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
    
    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
```

## Testing Without LLM

For testing the system without implementing full LLM integration:

```java
// Create a mock LLM response generator for testing
public class MockThemeGenerator {
    
    public static String generateMockTheme(String description) {
        // Simple keyword-based theme generation for testing
        JSONObject theme = new JSONObject();
        
        try {
            String lower = description.toLowerCase();
            
            if (lower.contains("dark") || lower.contains("night")) {
                theme.put("backgroundColor", "#121212");
                theme.put("textColor", "#FFFFFF");
                theme.put("primaryColor", "#BB86FC");
                theme.put("emoji", "🌙");
            } else if (lower.contains("ocean") || lower.contains("sea") || lower.contains("water")) {
                theme.put("backgroundColor", "#E3F2FD");
                theme.put("textColor", "#0D47A1");
                theme.put("primaryColor", "#1976D2");
                theme.put("emoji", "🌊");
            } else if (lower.contains("forest") || lower.contains("nature") || lower.contains("green")) {
                theme.put("backgroundColor", "#E8F5E9");
                theme.put("textColor", "#1B5E20");
                theme.put("primaryColor", "#4CAF50");
                theme.put("emoji", "🌲");
            } else {
                // Default bright theme
                theme.put("backgroundColor", "#FFFFFF");
                theme.put("textColor", "#000000");
                theme.put("primaryColor", "#6200EE");
                theme.put("emoji", "☀️");
            }
            
            // Add common properties
            theme.put("themeName", "Custom Theme");
            theme.put("buttonBackgroundColor", theme.getString("primaryColor"));
            theme.put("buttonTextColor", "#FFFFFF");
            theme.put("headerColor", theme.getString("textColor"));
            
        } catch (JSONException e) {
            Log.e("MockThemeGenerator", "Error creating theme", e);
        }
        
        return theme.toString();
    }
}
```

## API Integration Examples

### OpenAI GPT API
```java
// Using OkHttp for HTTP requests
public String callOpenAI(String userDescription) throws IOException {
    OkHttpClient client = new OkHttpClient();
    MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    String prompt = String.format(
        "Create a theme JSON for: %s. " +
        "Include backgroundColor, textColor, primaryColor (hex format).",
        userDescription
    );
    
    JSONObject requestBody = new JSONObject();
    requestBody.put("model", "gpt-4");
    requestBody.put("messages", new JSONArray()
        .put(new JSONObject()
            .put("role", "system")
            .put("content", "You are a UI theme generator. Return only valid JSON."))
        .put(new JSONObject()
            .put("role", "user")
            .put("content", prompt)));
    
    Request request = new Request.Builder()
        .url("https://api.openai.com/v1/chat/completions")
        .addHeader("Authorization", "Bearer " + API_KEY)
        .post(RequestBody.create(requestBody.toString(), JSON))
        .build();
    
    Response response = client.newCall(request).execute();
    JSONObject responseJson = new JSONObject(response.body().string());
    
    return responseJson
        .getJSONArray("choices")
        .getJSONObject(0)
        .getJSONObject("message")
        .getString("content");
}
```

## Best Practices

1. **Validate LLM Output**: Always validate JSON before storing
2. **Contrast Checking**: Ensure text is readable on backgrounds
3. **Error Handling**: Provide fallback to default theme
4. **User Feedback**: Show loading states during generation
5. **Theme Preview**: Let users preview before applying
6. **Async Operations**: Generate themes off the main thread
7. **Rate Limiting**: Implement rate limiting for API calls
8. **Caching**: Cache successful themes to reduce API calls

## Security Considerations

- Never expose API keys in client code
- Use a backend service to proxy LLM API calls
- Validate and sanitize user input
- Set reasonable rate limits
- Implement request timeouts

## Cost Optimization

- Cache common themes
- Implement theme templates
- Allow users to save/share themes
- Batch requests when possible
- Use cheaper models for simple themes

## Accessibility

When generating themes via LLM, ensure:
- Minimum contrast ratio of 4.5:1 for normal text
- Minimum contrast ratio of 3:1 for large text
- Color is not the only means of conveying information
- Themes work for colorblind users

Example prompt addition:
```
Ensure all color combinations meet WCAG AA contrast requirements (4.5:1 for normal text).
```

