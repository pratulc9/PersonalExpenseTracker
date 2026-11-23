
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    // Hash password during registration
    public static String hashPassword(char[] password) throws Exception {

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        int iterations = 600000;

        PBEKeySpec spec = new PBEKeySpec(
                password, salt, iterations, 256);

        try {
            byte[] hash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

            return iterations + ":" +
                    Base64.getEncoder().encodeToString(salt) + ":" +
                    Base64.getEncoder().encodeToString(hash);

        } finally {
            spec.clearPassword();
        }
    }

    // Verify password during login
    public static boolean verifyPassword(
            char[] password, String storedHash) throws Exception {

        if (storedHash == null)
            return false;

        String[] parts = storedHash.split(":");

        if (parts.length != 3)
            return false;

        int iterations = Integer.parseInt(parts[0]);

        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(
                password, salt, iterations, expectedHash.length * 8);

        try {
            byte[] actualHash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

            return MessageDigest.isEqual(expectedHash, actualHash);

        } finally {
            spec.clearPassword();
        }
    }
}
