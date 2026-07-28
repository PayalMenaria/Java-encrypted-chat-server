import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CryptoUtils {
    // Standard 16-Byte (128-bit) Secret Key for AES
    private static final String SECRET_KEY = "MySuperSecretKey"; // Exactly 16 characters!

    // Message Encrypt karne ke liye
    public static String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes); // String representation
        } catch (Exception e) {
            System.err.println("[CRYPTO ERROR] Encryption failed: " + e.getMessage());
            return plainText; // Fallback
        }
    }

    // Message Decrypt karne ke liye
    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Notifications ya plain messages agar decrypt na ho sakein
            return encryptedText;
        }
    }
}