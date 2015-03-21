package szyfrator.util;

import szyfrator.model.UserAccess;
import szyfrator.model.UserKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Wojtek on 2015-03-14.
 */
public class RSAUtil {


    public static final String MODULUS = "d46f473a2d746537de2056ae3092c451";

    public static PublicKey loadPublicKey(File file) throws InvalidKeySpecException {
        /*File f = new File(publicKeyFilePath);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int)f.length()];
        dis.readFully(keyBytes);
        dis.close();

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.publicKey = kf.generatePublic(spec);*/
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(
                    MODULUS, 16), new BigInteger("11", 16));
            return keyFactory.generatePublic(pubKeySpec);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            throw new RuntimeException("Nie znaleziono algorytmu RSA lub dostawcy BC.");
        }
    }

    public static PrivateKey loadPrivateKey(File file, String password) {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");

            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(new BigInteger(
                    MODULUS, 16), new BigInteger(MODULUS,
                    16));
            return keyFactory.generatePrivate(privKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Problem z wczytaniem klucza prywatnego", e);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Problem z dostępnymi algorytmami.", e);
        }

    }


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
