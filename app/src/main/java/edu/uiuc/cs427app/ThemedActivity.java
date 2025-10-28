package edu.uiuc.cs427app;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import android.widget.ScrollView;

/**
 * ThemedActivity is a base class for all activities that need to apply the global theme.
 * It automatically applies theme colors to common UI elements based on the user's theme preferences.
 * Activities should extend this class. The theme is applied automatically after setContentView() is called.
 */
public abstract class ThemedActivity extends AppCompatActivity {
    
    protected AppTheme theme;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the user's theme JSON and update the singleton if user has a custom theme
        String themeJson = User.getInstance().getThemeJson();
        if (themeJson != null && !themeJson.trim().isEmpty() && !themeJson.equals("{}")) {
            // User has a custom theme, update the singleton
            AppTheme.updateTheme(themeJson);
        }
        
        // Get the singleton instance
        theme = AppTheme.getInstance();
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
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            applyThemeToView(rootView);
        }
    }
    
    /**
     * Recursively applies theme to a view and all its children.
     * 
     * @param view The view to apply the theme to
     */
    protected void applyThemeToView(View view) {
        // Apply background color to container views
        if (view instanceof ConstraintLayout || view instanceof LinearLayout) {
            view.setBackgroundColor(theme.getBackgroundColor());
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
        if (view instanceof ScrollView || view instanceof NestedScrollView) {
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

