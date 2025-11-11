package edu.uiuc.cs427app;

import android.content.res.AssetManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import android.widget.ScrollView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ThemedActivity is a base class for all activities that need to apply the global theme.
 * It automatically applies theme colors to common UI elements based on the user's theme preferences.
 * Activities should extend this class. The theme is applied automatically after setContentView() is called.
 */
public abstract class ThemedActivity extends AppCompatActivity {

    private static final String TAG = "ThemedActivity";
    private static final String DEFAULT_THEME_FILE = "default_theme.json";

    protected AppTheme theme;

    /**
     * Called when the activity is first created.
     * Loads and applies the user's theme, or applies the default theme if none exists.
     * This ensures that users without a custom theme always get the default theme,
     * preventing the app from using a previous user's theme.
     *
     * @param savedInstanceState A bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the user's theme JSON and update the singleton if user has a custom theme
        String themeJson = User.getInstance().getThemeJson();
        if (themeJson != null && !themeJson.trim().isEmpty() && !themeJson.equals("{}")) {
            // User has a custom theme, update the singleton
            AppTheme.updateTheme(themeJson);
        } else {
            // User has no theme or empty theme, explicitly load the default theme
            // This ensures a default theme is always applied instead of using a previous user's theme
            loadAndApplyDefaultTheme();
        }

        // Get the singleton instance
        theme = AppTheme.getInstance();
    }

    /**
     * Loads the default theme from assets and applies it to AppTheme singleton.
     * This method reads the default_theme.json file from assets, parses it as JSON,
     * and updates the AppTheme singleton with the default theme values.
     * This is called when a user has no theme or an empty theme to ensure a default theme is always applied.
     * If loading fails, the AppTheme singleton will fall back to its own default theme loading mechanism.
     *
     * @throws IOException If the default_theme.json file cannot be read from assets (handled internally)
     */
    private void loadAndApplyDefaultTheme() {
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open(DEFAULT_THEME_FILE);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            reader.close();
            inputStream.close();

            String defaultThemeJson = jsonBuilder.toString();
            AppTheme.updateTheme(defaultThemeJson);
            Log.i(TAG, "Applied default theme for user with no custom theme");

        } catch (IOException e) {
            Log.e(TAG, "Failed to load default theme from assets", e);
            // If loading fails, AppTheme.getInstance() will still provide a default theme
        }
    }

    /**
     * Override setContentView to automatically apply the theme after the content is set.
     * This eliminates the need for each activity to manually call applyThemeToActivity().
     */
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applyThemeToActivity();
    }

    /**
     * Override setContentView to automatically apply the theme after the content is set.
     * This eliminates the need for each activity to manually call applyThemeToActivity().
     */
    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applyThemeToActivity();
    }

    /**
     * Override setContentView to automatically apply the theme after the content is set.
     * This eliminates the need for each activity to manually call applyThemeToActivity().
     */
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        applyThemeToActivity();
    }

    /**
     * Applies the global theme to the activity's root view and all its children.
     * This is now called automatically by setContentView(), but can still be called manually if needed.
     */
    protected void applyThemeToActivity() {
        // Apply theme to status bar (Android 5.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(theme.getPrimaryColor());
        }

        // Apply theme to ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(theme.getPrimaryColor()));
        }

        // Apply theme to root view and all its children
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            applyThemeToView(rootView);
        }
    }

    /**
     * Allows the use of android:tags="tag1,tag2, ..." by checking all possible tags in a View
     * @param view View object to check tags
     * @param tagToCheck the desired tag to check for
     * @return true if desired tag is found, false if not
     */
    private boolean checkTags(View view, String tagToCheck){
        String tags[] = ((String)view.getTag()).split(",");
        for(int i = 0; i < tags.length; i ++){
            if(tags[i].equals(tagToCheck)){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if xml override_background tag exists on view object. Leave it as is (as specified in the xml) if it exists. Returns true if no background tag
     * @param view View object to check tag with
     * @return True if no background tag, false if otherwise
     */
    private boolean noBackgroundTag(View view){
        return view.getTag() == null || !checkTags(view, "override_background");
    }

    /**
     * Apply rounded corners to xml objects with android:tag="corners"
     * Currently only works on AppThemes with a background color, could probably be made more robust
     * @param view View object to create corners with
     */
    private void roundedCorners(View view){
        if(view.getTag() == null || !checkTags(view, "corners")){
            return;
        }

        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setColor(theme.getBackgroundColor());

        float cornerRadiusDp = 12f;
        float cornerRadiusPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                cornerRadiusDp,
                view.getResources().getDisplayMetrics()
        );
        backgroundDrawable.setCornerRadius(cornerRadiusPx);

        view.setBackground(backgroundDrawable);
    }

    /**
     * Recursively applies theme to a view and all its children.
     *
     * @param view The view to apply the theme to
     */
    protected void applyThemeToView(View view) {
        // Apply background color to container views
        if ((view instanceof ConstraintLayout || view instanceof LinearLayout || view instanceof FrameLayout) && noBackgroundTag(view)) {
            view.setBackgroundColor(theme.getBackgroundColor());
            roundedCorners(view);
        }

        // Apply theme to TextViews
        if (view instanceof TextView && !(view instanceof Button) && !(view instanceof EditText)) {
            TextView textView = (TextView) view;

            // Check if this is a header/title (larger text size or specific IDs)
            if (textView.getTextSize() >= 60 || isHeaderView(textView)) { // 60px ~ 20sp
                textView.setTextColor(theme.getHeaderColor());
            } else {
                textView.setTextColor(theme.getTextColor());
            }
        }

        // Apply theme to Buttons
        if (view instanceof Button) {
            Button button = (Button) view;
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(theme.getButtonBackgroundColor()));
            button.setTextColor(theme.getButtonTextColor());
        }

        // Apply theme to EditText
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            editText.setTextColor(theme.getTextColor());
            editText.setHintTextColor(adjustAlpha(theme.getTextColor(), 0.5f));
        }

        // Apply theme to ScrollView backgrounds
        if ((view instanceof ScrollView || view instanceof NestedScrollView) && noBackgroundTag(view)) {
            view.setBackgroundColor(theme.getBackgroundColor());
        }

        // Recursively apply to children
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyThemeToView(viewGroup.getChildAt(i));
            }
        }
    }

    /**
     * Checks if a TextView is likely a header based on its ID or properties.
     *
     * @param textView The TextView to check
     * @return true if it appears to be a header, false otherwise
     */
    private boolean isHeaderView(TextView textView) {
        int id = textView.getId();
        String resourceName = "";

        try {
            resourceName = getResources().getResourceEntryName(id).toLowerCase();
        } catch (Exception e) {
            return false;
        }

        return resourceName.contains("title") ||
                resourceName.contains("header") ||
                resourceName.contains("welcome") ||
                resourceName.equals("textview3"); // Main screen header
    }

    /**
     * Adjusts the alpha (transparency) of a color.
     *
     * @param color The original color
     * @param factor Alpha factor (0.0 to 1.0)
     * @return Color with adjusted alpha
     */
    protected int adjustAlpha(int color, float factor) {
        int alpha = Math.round(android.graphics.Color.alpha(color) * factor);
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        return android.graphics.Color.argb(alpha, red, green, blue);
    }

    /**
     * Creates a styled card background drawable with the theme's card color.
     * Useful for creating card-like UI elements.
     *
     * @return GradientDrawable configured as a card
     */
    protected GradientDrawable createCardBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(theme.getCardBackgroundColor());
        drawable.setCornerRadius(8 * getResources().getDisplayMetrics().density); // 8dp corners
        drawable.setStroke(
                (int) (1 * getResources().getDisplayMetrics().density),
                theme.getBorderColor()
        );
        return drawable;
    }

    /**
     * Applies card styling to a view.
     *
     * @param view The view to style as a card
     */
    protected void applyCardStyle(View view) {
        view.setBackground(createCardBackground());
        int padding = (int) (12 * getResources().getDisplayMetrics().density); // 12dp padding
        view.setPadding(padding, padding, padding, padding);
    }

    /**
     * Gets the current app theme instance.
     *
     * @return The AppTheme instance for this activity
     */
    public AppTheme getAppTheme() {
        return theme;
    }
}

