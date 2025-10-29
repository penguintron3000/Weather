package edu.uiuc.cs427app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;

import edu.uiuc.cs427app.R;

/**
 * LoginFormView encapsulates the login form UI and dispatches user actions to an attached listener.
 */
public class LoginFormView extends LinearLayout {

    /**
     * ActionListener receives callbacks when the user presses sign-in or sign-up.
     */
    public interface ActionListener {
        /**
         * Called when the user taps the sign-in button.
         */
        void onSignInRequested();

        /**
         * Called when the user taps the sign-up button.
         */
        void onSignUpRequested();
    }

    private EditText usernameInput;
    private EditText passwordInput;
    private Spinner layoutSelector;
    private EditText themeDescriptionInput;
    private TextView errorMessage;
    private ActionListener actionListener;

    /**
     * Creates the login form when instantiated directly in code.
     *
     * @param context host context supplying theme and resources
     */
    public LoginFormView(Context context) {
        super(context);
        init(context);
    }

    /**
     * Creates the login form from XML inflation, applying attribute configuration.
     *
     * @param context host context supplying theme and resources
     * @param attrs   attribute set inflated from XML
     */
    public LoginFormView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Creates the login form with an explicit default style applied.
     *
     * @param context      host context supplying theme and resources
     * @param attrs        attribute set inflated from XML
     * @param defStyleAttr default style resource to apply
     */
    public LoginFormView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Inflates the layout and wires up view interactions.
     *
     * @param context host context used to inflate the layout
     */
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_login_form, this, true);
        setOrientation(VERTICAL);

        usernameInput = findViewById(R.id.inputUsername);
        passwordInput = findViewById(R.id.inputPassword);
        layoutSelector = findViewById(R.id.spinner_layout);
        themeDescriptionInput = findViewById(R.id.inputThemeDescription);
        Button signInButton = findViewById(R.id.buttonSignIn);
        Button signUpButton = findViewById(R.id.buttonSignUp);
        errorMessage = findViewById(R.id.errorMessage);

        signInButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onSignInRequested();
            }
        });

        signUpButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onSignUpRequested();
            }
        });

        passwordInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    /**
     * Initializes the layout selector spinner with the provided string array resource.
     *
     * @param arrayResId resource id for the entries to display in the spinner
     */
    public void initializeLayoutOptions(@ArrayRes int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                arrayResId,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layoutSelector.setAdapter(adapter);
    }

    /**
     * Sets the listener that receives sign-in and sign-up events.
     *
     * @param listener callback invoked for login form actions
     */
    public void setActionListener(@Nullable ActionListener listener) {
        this.actionListener = listener;
    }

    /**
     * Returns the text entered in the username field.
     *
     * @return trimmed username text
     */
    public String getUsername() {
        return usernameInput.getText().toString().trim();
    }

    /**
     * Returns the text entered in the password field.
     *
     * @return raw password text
     */
    public String getPassword() {
        return passwordInput.getText().toString();
    }

    /**
     * Returns the text entered in the theme description field.
     *
     * @return trimmed theme description text
     */
    public String getThemeDescription() {
        return themeDescriptionInput.getText().toString().trim();
    }

    /**
     * Requests focus for the username input field.
     */
    public void requestUsernameFocus() {
        usernameInput.requestFocus();
    }

    /**
     * Requests focus for the password input field.
     */
    public void requestPasswordFocus() {
        passwordInput.requestFocus();
    }

    /**
     * Displays the supplied error message and makes the error label visible.
     *
     * @param message user-facing error message to display
     */
    public void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisibility(VISIBLE);
    }

    /**
     * Clears any currently displayed error message.
     */
    public void clearError() {
        errorMessage.setText("");
        errorMessage.setVisibility(GONE);
    }
}
