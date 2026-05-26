package com.example.federatedsearch.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    
    /**
     * Generates a hash for a search result to detect duplicates
     * Based on title and URL similarity
     */
    public static String generateHash(String title, String url) {
        try {
            String input = normalizeString(title) + "|" + normalizeString(url);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash if SHA-256 is not available
            String input = normalizeString(title) + "|" + normalizeString(url);
            return String.valueOf(input.hashCode());
        }
    }
    
    /**
     * Normalizes string for better duplicate detection
     */
    private static String normalizeString(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase().trim().replaceAll("\\s+", " ");
    }
}
