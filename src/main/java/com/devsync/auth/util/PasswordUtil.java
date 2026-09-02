package com.devsync.auth.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;

public class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private PasswordUtil() {
    }

    /**
     * Hashes a plain text password using PBKDF2 with HMAC-SHA256 and a random salt.
     * Output format: {saltHex}:{hashHex}
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        byte[] hash = generateHash(password.toCharArray(), salt);

        HexFormat hex = HexFormat.of();
        return hex.formatHex(salt) + ":" + hex.formatHex(hash);
    }

    /**
     * Verifies a plain text password against a stored PBKDF2 hash.
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }

        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        try {
            HexFormat hex = HexFormat.of();
            byte[] salt = hex.parseHex(parts[0]);
            byte[] expectedHash = hex.parseHex(parts[1]);

            byte[] actualHash = generateHash(password.toCharArray(), salt);

            // Constant-time comparison to prevent timing attacks
            return java.security.MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] generateHash(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Failed to hash password", e);
        }
    }
}
