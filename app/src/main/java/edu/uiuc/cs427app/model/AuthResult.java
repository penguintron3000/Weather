package edu.uiuc.cs427app.model;

import edu.uiuc.cs427app.User;

/**
 * AuthResult represents the result of an authentication attempt.
 * It includes a status, a User object on success, and a message.
 */
public class AuthResult {

    /**
     * The status of the authentication attempt.
     */
    public enum Status {
        SUCCESS,
        INVALID_CREDENTIALS,
        ACCOUNT_LOCKED,
        FAILURE
    }

    private final Status status;
    private final User user;
    private final String message;

    /**
     * Constructor for the AuthResult.
     * @param status The status of the authentication attempt.
     * @param user The User object on success, or null otherwise.
     * @param message A message associated with the result.
     */
    public AuthResult(Status status, User user, String message) {
        this.status = status;
        this.user = user;
        this.message = message;
    }

    /**
     * Gets the status of the authentication attempt.
     * @return The status of the authentication attempt.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the User object.
     * @return The User object on success, or null otherwise.
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the message associated with the result.
     * @return The message associated with the result.
     */
    public String getMessage() {
        return message;
    }
}
