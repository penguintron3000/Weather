package edu.uiuc.cs427app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.uiuc.cs427app.db.UserContract;

/**
 * LoginActivity is the entry point shown on app launch.
 * Handles user authentication by verifying credentials against the database.
 */
public class LoginActivity extends ThemedActivity implements View.OnClickListener {

    private EditText usernameInput;
    private EditText passwordInput;
    private Spinner layoutSelector;
    private Button signInButton;
    private Button signUpButton;
    private TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Apply the global theme to all UI components
        applyThemeToActivity();

        usernameInput = findViewById(R.id.inputUsername);
        passwordInput = findViewById(R.id.inputPassword);
        layoutSelector = findViewById(R.id.spinnerLayout);
        signInButton = findViewById(R.id.buttonSignIn);
        signUpButton = findViewById(R.id.buttonSignUp);
        errorMessage = findViewById(R.id.errorMessage);

        // basic layouts list placeholder, we can add actual options later
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.layout_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layoutSelector.setAdapter(adapter);

        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.buttonSignIn) {
            handleLogin();
        } else if (id == R.id.buttonSignUp) {
            // navigate to RegisterActivity
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Handles the login process by verifying credentials against the database.
     */
    private void handleLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Clear previous error messages
        clearError();

        if (username.isEmpty()) {
            showError("Please enter a username");
            usernameInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter a password");
            passwordInput.requestFocus();
            return;
        }

        // query database for user
        Cursor cursor = getContentResolver().query(
                UserContract.CONTENT_URI,
                null,
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[] { username },
                null);

        if (cursor == null) {
            showError("Login failed. Please try again.");
            return;
        }

        if (cursor.moveToFirst()) {
            // user found, now verify password
            int idxPassword = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_PASSWORD_HASH);
            int idxUserId = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_USER_ID);
            int idxUsername = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_USERNAME);
            int idxTheme = cursor.getColumnIndex(UserContract.UserEntry.COLUMN_THEME_JSON);

            if (idxPassword >= 0 && idxUserId >= 0 && idxUsername >= 0 && idxTheme >= 0) {
                String storedHash = cursor.getString(idxPassword);
                long userId = cursor.getLong(idxUserId);
                String dbUsername = cursor.getString(idxUsername);
                String themeJson = cursor.getString(idxTheme);

                if (PasswordHasher.verify(password, storedHash)) {
                    // password correct, initialize user session
                    User.getInstance().init(userId, dbUsername, themeJson);

                    // navigate to MainActivity
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    showError("Invalid username or password");
                    passwordInput.requestFocus();
                }

            } else {
                showError("Login failed. Please try again.");
            }
        } else {
            // user not found
            showError("Invalid username or password");
            usernameInput.requestFocus();
        }

        cursor.close();
    }

    /**
     * Shows an error message to the user.
     * 
     * @param message The error message to display
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setTextColor(theme.getErrorColor());
        errorMessage.setBackgroundColor(adjustAlpha(theme.getErrorColor(), 0.1f));
        errorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Clears any error messages.
     */
    private void clearError() {
        errorMessage.setVisibility(View.GONE);
        errorMessage.setText("");
    }
}
