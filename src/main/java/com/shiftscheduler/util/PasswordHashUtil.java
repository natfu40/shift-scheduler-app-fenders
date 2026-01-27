package com.shiftscheduler.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHashUtil {

    public static String hashWithSHA256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean verifySHA256Hash(String password, String storedHash) {
        String hashedPassword = hashWithSHA256(password);
        return hashedPassword.equals(storedHash);
    }
}
