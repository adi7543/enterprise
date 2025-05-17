package com.mycompany.enterprise;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import java.util.prefs.Preferences;

public class LicenseValidator {

    private static final String SECRET_KEY = "!B2#fT9x@Y7$pLr8*Qz&uX4w+NsMvKd%";
    private static final Preferences prefs = Preferences.userRoot().node("com.mycompany.enterprise");

    // Check if license key and User ID are stored in the registry
    public static boolean isLicenseInfoInRegistry() {
        return prefs.get("licenseKey", null) != null && prefs.get("userId", null) != null;
    }

    // Retrieve license key from the registry
    public static String getLicenseKeyFromRegistry() {
        return prefs.get("licenseKey", null);
    }

    // Retrieve User ID from the registry
    public static String getUserIdFromRegistry() {
        return prefs.get("userId", null);
    }

    // Save new license key and User ID to the registry
    public static void saveLicenseInfoToRegistry(String userId, String licenseKey) {
        prefs.put("licenseKey", licenseKey);
        prefs.put("userId", userId);
        System.out.println("License key and User ID saved to registry.");
    }

    // Validate the license key with User ID
    public static boolean validateLicense(String userId, String licenseKey) throws Exception {
        String[] parts = licenseKey.split(":");
        if (parts.length != 2) {
            JOptionPane.showMessageDialog(null, "Invalid license format. Please enter a valid license key.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;  // Invalid license format
        }

        String expirationDateString = parts[0];
        String hashedKey = parts[1];

        // Prepare the data for validation
        String data = userId + ":" + expirationDateString;

        // Perform hash validation using HMAC
        Mac hasher = Mac.getInstance("HmacSHA256");
        hasher.init(new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256"));
        byte[] hash = hasher.doFinal(data.getBytes());

        String expectedHash = Base64.getEncoder().encodeToString(hash);

        // Check if the generated hash matches the provided hash
        if (!expectedHash.equals(hashedKey)) {
            JOptionPane.showMessageDialog(null, "Invalid license key. Please check your User ID and license key.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if the license has expired
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date expirationDate = sdf.parse(expirationDateString);
        if (expirationDate.before(new Date())) {
            JOptionPane.showMessageDialog(null, "License has expired.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    // Check license and prompt user if missing or invalid
    public static void checkLicense() {
        try {
            // Check if license information is stored in the registry
            if (!isLicenseInfoInRegistry()) {
                System.out.println("License information is missing from registry.");
                String userId = JOptionPane.showInputDialog("Please enter your User ID:");
                String licenseKey = JOptionPane.showInputDialog("Please enter your license key:");

                if (userId != null && !userId.isEmpty() && licenseKey != null && !licenseKey.isEmpty()) {
                    // Validate the User ID and license key
                    if (validateLicense(userId, licenseKey)) {
                        System.out.println("License key and User ID accepted.");
                        // Save to registry and continue
                        saveLicenseInfoToRegistry(userId, licenseKey);
                    } else {
                        System.out.println("Invalid license information.");
                        System.exit(0);  // Exit the application if invalid
                    }
                } else {
                    System.out.println("No license information entered. Exiting...");
                    System.exit(0);  // Exit if no input is given
                }
            } else {
                // Retrieve stored information and validate
                String userId = getUserIdFromRegistry();
                String licenseKey = getLicenseKeyFromRegistry();
                
                if (validateLicense(userId, licenseKey)) {
                    System.out.println("License is valid.");
                    // Proceed with main application logic
                } else {
                    System.out.println("License expired or invalid.");
                    String newUserId = JOptionPane.showInputDialog("Please enter your User ID:");
                    String newLicenseKey = JOptionPane.showInputDialog("Please enter a new license key:");

                    if (newUserId != null && !newUserId.isEmpty() && newLicenseKey != null && !newLicenseKey.isEmpty()) {
                        if (validateLicense(newUserId, newLicenseKey)) {
                            System.out.println("New license information accepted.");
                            saveLicenseInfoToRegistry(newUserId, newLicenseKey);
                        } else {
                            System.out.println("Invalid license information.");
                            System.exit(0);  // Exit if invalid
                        }
                    } else {
                        System.out.println("No license information entered. Exiting...");
                        System.exit(0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
