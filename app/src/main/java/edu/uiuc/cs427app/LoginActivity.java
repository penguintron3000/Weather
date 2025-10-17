package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * LoginActivity is the entry point shown on app launch.
 * For now sign in button navigates to MainActivity without real auth.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText usernameInput;
    private EditText passwordInput;
    private Spinner layoutSelector;
    private Button signInButton;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.inputUsername);
        passwordInput = findViewById(R.id.inputPassword);
        layoutSelector = findViewById(R.id.spinnerLayout);
        signInButton = findViewById(R.id.buttonSignIn);
        signUpButton = findViewById(R.id.buttonSignUp);

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
            // temporary navigation to MainActivity for manual testing
            String username = usernameInput.getText().toString().trim();

            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.buttonSignUp) {
            // placeholder - should navigate to a RegisterActivity page in the
            // future.
            Toast.makeText(this, "Sign Up not implemented yet", Toast.LENGTH_SHORT).show();
        }

    }
}
