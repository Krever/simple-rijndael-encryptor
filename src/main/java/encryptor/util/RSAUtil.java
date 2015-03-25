package encryptor.util;

import encryptor.model.UserAccess;
import encryptor.model.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(RSAUtil.class);

    public static KeyPair generateKeyPair() {
        try {
            SecureRandom random = new SecureRandom();
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
            generator.initialize(2048, random);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Algorith could not be acquired", e);
        }
    }

    public static List<UserAccess> encryptSessionKey(byte[] sessionKey, List<UserKey> userKeys) {
        return userKeys.stream()
                .map(k ->  {
                    try { return new UserAccess(k.getIdentifier(), encryptSessionKey(sessionKey, k.getPublicKey())); }
                    catch (Exception e) {log.error("Error during session key encryption", e);throw new RuntimeException(e); }
                })
                .collect(Collectors.toList());
    }

    public static byte[] encryptSessionKey(byte[] sessionKey, PublicKey publicKey) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(sessionKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            log.error("Error during acquiring cipher", e);
            throw new RuntimeException(e);
        }
    }

    public static byte[] decryptSessionKey(byte[] encryptedSessionKey, PrivateKey privateKey) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedSessionKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            log.error("Error during acquiring cipher", e);
            throw new RuntimeException(e);
        }
    }
}
