package encryptor.util;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by Wojtek on 2015-03-15.
 */
public class RSAKeyFilesUtil {

    public static void savePublicKey(PublicKey publicKey, String filePath) throws IOException {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
            fos.write(x509EncodedKeySpec.getEncoded());
            fos.close();
    }

    public static void savePrivateKey(PrivateKey privateKey, String password, String filePath) throws IOException {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);

            byte[] encryptedKey = Rijndael.encryptPrivateKey(privateKey, password);

            fos.write(encryptedKey);
            fos.close();
    }

    public static PublicKey loadPublicKey(String filePath) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
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
    }

    public static PrivateKey loadPrivateKey(String filePath, String password) {
        try {
            File f = new File(filePath);
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int)f.length()];
            dis.readFully(keyBytes);
            dis.close();

            byte[] decryptedKeyBytes = Rijndael.decryptPrivateKey(keyBytes, password);

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decryptedKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Wystapił błąd przy wczytywaniu klucza prywatnego.", e);
        }
    }
}
