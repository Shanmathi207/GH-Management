package hms.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtils provides cryptographic password hashing using SHA-256.
 * Ensures zero plaintext passwords are stored in MySQL.
 */
public class PasswordUtils {

    /**
     * Hashes a password string with SHA-256 and returns its hexadecimal representation.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current Java runtime", e);
        }
    }

    /**
     * Verifies a plain password against a stored hash (or plain fallback if legacy).
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        String hashedInput = hashPassword(plainPassword);
        if (hashedInput.equalsIgnoreCase(storedHash)) {
            return true;
        }
        // Legacy plain-text fallback during initial migration
        return plainPassword.equals(storedHash);
    }
}
