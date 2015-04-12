package encryptor.util;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by Wojtek on 2015-03-15.
 */
public class RSAKeyFilesUtil {

    private static final String PUBLIC_KEY_HEADER = "-----BEGIN RSA PUBLIC KEY-----\n";
    private static final String PUBLIC_KEY_FOOTER = "-----END RSA PUBLIC KEY-----";

    public static void savePublicKey(PublicKey publicKey, String filePath) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        Writer writer = new OutputStreamWriter(fos);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        writer.write(PUBLIC_KEY_HEADER);
        writer.write(Base64.encodeBase64String(x509EncodedKeySpec.getEncoded()));
        writer.write(PUBLIC_KEY_FOOTER);
        writer.close();
    }

    public static PublicKey loadPublicKey(String filePath) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String content = new String(encoded, Charset.defaultCharset());
        String base64Key = content.replace(PUBLIC_KEY_HEADER, "").replace(PUBLIC_KEY_FOOTER, "");
        byte[] keyBytes = Base64.decodeBase64(base64Key);

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static void savePrivateKey(PrivateKey privateKey, String password, String filePath) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);

        byte[] encryptedKey = Rijndael.encryptPrivateKey(privateKey, password);

        fos.write(encryptedKey);
        fos.close();
    }

    public static PrivateKey loadPrivateKey(String filePath, String password) {
        try {
            File f = new File(filePath);
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int) f.length()];
            dis.readFully(keyBytes);
            dis.close();

            byte[] decryptedKeyBytes = Rijndael.decryptPrivateKey(keyBytes, password);

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decryptedKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            //throw new RuntimeException("Error during private key loading", e);
            return RSAUtil.generateKeyPair().getPrivate();
        }
    }
}
