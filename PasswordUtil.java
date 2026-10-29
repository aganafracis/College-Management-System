import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Provides PBKDF2 password hashing with per-password random salts and constant-time verification.
 * Adheres to secure credential handling standards, avoiding plaintext storage and zeroing sensitive arrays.
 */
public class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final int SALT_BYTES = 16;  // 128 bits
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a password char array using PBKDF2WithHmacSHA256 with a unique random salt.
     * Format: PBKDF2$<iterations>$<base64_salt>$<base64_hash>
     */
    public static String hashPassword(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(password, salt, ITERATIONS, KEY_LENGTH);

        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashB64 = Base64.getEncoder().encodeToString(hash);

        // Zero out derived hash bytes
        Arrays.fill(hash, (byte) 0);

        return "PBKDF2$" + ITERATIONS + "$" + saltB64 + "$" + hashB64;
    }

    /**
     * Verifies a candidate password char array against a stored PBKDF2 formatted hash.
     * Uses constant-time comparison to prevent timing attacks.
     */
    public static boolean verifyPassword(char[] candidatePassword, String storedHash) {
        if (candidatePassword == null || storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !parts[0].equals("PBKDF2")) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] candidateHash = pbkdf2(candidatePassword, salt, iterations, expectedHash.length * 8);

            boolean match = MessageDigest.isEqual(candidateHash, expectedHash);

            // Zero out sensitive byte arrays
            Arrays.fill(candidateHash, (byte) 0);

            return match;
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error computing PBKDF2 hash: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Utility method to safely clear a password char array in memory.
     */
    public static void clearArray(char[] chars) {
        if (chars != null) {
            Arrays.fill(chars, '\0');
        }
    }
}
