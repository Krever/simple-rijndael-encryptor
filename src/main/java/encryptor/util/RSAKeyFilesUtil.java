package encryptor.util;

import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by Wojtek on 2015-03-15.
 */
public class RSAKeyFilesUtil {

    public static KeyPair generateKeyPair() {
        try {
            SecureRandom random = new SecureRandom();
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
            generator.initialize(2048, random);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Dziwny błąd.", e);
        }
    }

    public static void savePublicKey(PublicKey publicKey, String filePath) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
            fos.write(x509EncodedKeySpec.getEncoded());
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas zapisu klucza publicznego.", e);
        }
    }

    public static void savePrivateKey(PrivateKey privateKey, String filePath) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                    privateKey.getEncoded());
            fos.write(pkcs8EncodedKeySpec.getEncoded());
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException("Błąd przy zapisie klucz praywatnego", e);
        }
    }

    public static PublicKey loadPublicKey(String filePath) {
        try {
            File f = new File(filePath);
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int)f.length()];
            dis.readFully(keyBytes);
            dis.close();

            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Wystapił błąd przy wczytywaniu klucza publicznego.", e);
        }
    }

    public static PrivateKey loadPrivateKey(String filePath, String password) {
        try {
            File f = new File(filePath);
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int)f.length()];
            dis.readFully(keyBytes);
            dis.close();

            PKCS8EncodedKeySpec spec =
                    new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Wystapił błąd przy wczytywaniu klucza prywatnego.", e);
        }
    }
}
