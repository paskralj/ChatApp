package encryptdecrypt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class CryptoUtils {

    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    public static void setPrivateKey(PrivateKey privateKey) {
        CryptoUtils.privateKey = privateKey;
    }

    public static void setPublicKey(PublicKey publicKey) {
        CryptoUtils.publicKey = publicKey;
    }

    public static String decryptMessage(byte[] encryptedMessage) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        if (privateKey == null) {
            throw new IllegalStateException("Private key nije inicijaliziran.");
        }
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(encryptedMessage));
    }

    public static byte[] encryptMessage(String message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (publicKey == null) {
            throw new IllegalStateException("Public key nije inicijaliziran.");
        }
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes());
    }
}
