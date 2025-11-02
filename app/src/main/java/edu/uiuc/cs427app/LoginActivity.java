package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import org.json.JSONObject;

import edu.uiuc.cs427app.model.AuthResult;
import edu.uiuc.cs427app.services.AuthService;
import edu.uiuc.cs427app.services.GeminiThemeService;
import edu.uiuc.cs427app.services.TokenManager;
import edu.uiuc.cs427app.views.LoginFormView;

/**
 * LoginActivity is the entry point shown on app launch.
 * Handles user authentication by verifying credentials against the database.
 * This activity always uses the default theme - custom themes are applied after login.
 */
public class LoginActivity extends AppCompatActivity {

    private LoginFormView loginFormView;
    private AuthService authService;
    private TokenManager tokenManager;
    private final GeminiThemeService geminiThemeService = new GeminiThemeService();
    private static final String TAG = "LoginActivity";

    /**
     * This method is called when the activity is first created.
     * It is used to initialize the UI components and set up the event listeners.
     * @param savedInstanceState A bundle containing the activity's previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService(this);
        tokenManager = new TokenManager(this);

        loginFormView = findViewById(R.id.login_form_view);
        loginFormView.setActionListener(new LoginFormView.ActionListener() {
            /**
             * Callback invoked when the user requests to sign in.
             * Initiates the login process by calling handleLogin().
             */
            @Override
            public void onSignInRequested() {
                handleLogin();
            }

            /**
             * Callback invoked when the user requests to sign up.
             * Navigates to the RegisterActivity to allow new user registration.
             */
            @Override
            public void onSignUpRequested() {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Handles the login process by getting the user input and calling the AuthService.
     * It then processes the result from the AuthService and navigates to the MainActivity on success.
     */
    private void handleLogin() {
        String username = loginFormView.getUsername();
        String password = loginFormView.getPassword();
        String themeDescription = loginFormView.getThemeDescription();

        // Clear previous error messages
        loginFormView.clearError();

        if (username.isEmpty()) {
            loginFormView.showError("Please enter a username");
            loginFormView.requestUsernameFocus();
            return;
        }

        if (password.isEmpty()) {
            loginFormView.showError("Please enter a password");
            loginFormView.requestPasswordFocus();
            return;
        }

        AuthResult authResult = authService.login(username, password);

        switch (authResult.getStatus()) {
            case SUCCESS:
                // password correct, initialize user session
                String authToken = tokenManager.generateToken();
                tokenManager.persistSession(username, authToken);

                if (themeDescription != null && !themeDescription.trim().isEmpty()) {
                    Log.i(TAG, "Theme description entered: " + themeDescription);
                    if (!isNetworkAvailable()) {
                        Toast.makeText(this, "No internet connection. Skipping theme generation.", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                        return;
                    }
                    Toast.makeText(this, "Generating theme...", Toast.LENGTH_SHORT).show();
                    // Generate theme asynchronously, then proceed to main
                    geminiThemeService.generateThemeFromDescription(this, themeDescription, new GeminiThemeService.ThemeCallback() {
                        /**
                         * Callback invoked when theme generation succeeds.
                         * Applies the generated theme JSON to the app and navigates to MainActivity.
                         * If theme application fails, logs the error and proceeds to MainActivity anyway.
                         *
                         * @param themeJson The JSON object containing the generated theme data
                         */
                        @Override
                        public void onSuccess(JSONObject themeJson) {
                            runOnUiThread(() -> {
                                try {
                                    Log.i(TAG, "Applying theme JSON: " + themeJson.toString());
                                    AppTheme.updateTheme(themeJson);
                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to apply theme JSON", e);
                                    // If update fails, proceed without blocking
                                }
                                navigateToMain();
                            });
                        }

                        /**
                         * Callback invoked when theme generation fails.
                         * Displays an error toast message and navigates to MainActivity using the current theme.
                         *
                         * @param error The exception that occurred during theme generation
                         */
                        @Override
                        public void onError(Exception error) {
                            runOnUiThread(() -> {
                                Log.e(TAG, "Theme generation failed", error);
                                Toast.makeText(LoginActivity.this, "Theme generation failed. Using current theme.", Toast.LENGTH_SHORT).show();
                                navigateToMain();
                            });
                        }
                    });
                } else {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                }
                break;
            case INVALID_CREDENTIALS:
            case FAILURE:
                loginFormView.showError(authResult.getMessage());
                loginFormView.requestPasswordFocus();
                break;
            case ACCOUNT_LOCKED:
                loginFormView.showError(authResult.getMessage());
                break;
        }
    }

    /**
     * Navigates to the MainActivity and finishes this login activity.
     * This is called after successful login, whether or not theme generation occurs.
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Checks if the device has an active network connection available.
     * Verifies that the network has internet capability and uses WiFi, cellular, or ethernet transport.
     *
     * @return true if a valid network connection with internet capability is available, false otherwise
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
