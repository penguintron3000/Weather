package edu.uiuc.cs427app;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing using salted SHA-256.
 */
public class PasswordHasher {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    /**
     * Hashes a password with random salt.
     * 
     * @param password The plain text password to hash
     * @return A string containing the salt and hash separated by ":"
     */
    public static String hash(String password) {
        try {
            // generate salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // hash pass with salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // join salt and hash
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hashedPassword);

            return saltBase64 + ":" + hashBase64;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 hash failed", e);
        }
    }

    /**
     * Verifies a password against a stored hash.
     * 
     * @param password   Plain text password to verify
     * @param storedHash Stored hash containing salt and hash
     * @return true if password matches, false o.w.
     */
    public static boolean verify(String password, String storedHash) {
        try {
            // split salt and hash
            String[] parts = storedHash.split(":");

            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedPasswordHash = Base64.getDecoder().decode(parts[1]);

            // hash the input password with the stored salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // compare hashes
            return MessageDigest.isEqual(storedPasswordHash, hashedPassword);

        } catch (Exception e) {
            return false;
        }
    }
}
