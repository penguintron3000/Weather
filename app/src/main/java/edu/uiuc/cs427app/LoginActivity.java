package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import edu.uiuc.cs427app.model.AuthResult;
import edu.uiuc.cs427app.services.AuthService;
import edu.uiuc.cs427app.services.TokenManager;
import edu.uiuc.cs427app.views.LoginFormView;

/**
 * LoginActivity is the entry point shown on app launch.
 * Handles user authentication by verifying credentials against the database.
 */
public class LoginActivity extends ThemedActivity {

    private LoginFormView loginFormView;
    private AuthService authService;
    private TokenManager tokenManager;

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
        loginFormView.initializeLayoutOptions(R.array.layout_options);
        loginFormView.setActionListener(new LoginFormView.ActionListener() {
            @Override
            public void onSignInRequested() {
                handleLogin();
            }

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
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                // navigate to MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
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
}
