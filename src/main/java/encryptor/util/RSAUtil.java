package encryptor.util;

import encryptor.model.UserAccess;
import encryptor.model.UserKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Wojtek on 2015-03-14.
 */
public class RSAUtil {


    public static final String MODULUS = "d46f473a2d746537de2056ae3092c451";



    public static List<UserAccess> encryptSessionKey(byte[] sessionKey, List<UserKey> userKeys) {
        return userKeys.stream()
                .map(k -> new UserAccess(k.getIdentifier(), encryptSessionKey(sessionKey, k.getPublicKey())))
                .collect(Collectors.toList());
    }

    public static byte[] encryptSessionKey(byte[] sessionKey, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(sessionKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            MyLogger.log(e.toString());
            throw new RuntimeException("Wystąpił błąd przy szyfrowaniu klucza sesyjnego", e);
        }
    }

    public static byte[] decryptSessionKey(byte[] encryptedSessionKey, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedSessionKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            MyLogger.log(e.toString());
            throw new RuntimeException("Wystąpił błąd przy deszyfrowaniu klucza sesyjnego", e);
        }
    }
}
