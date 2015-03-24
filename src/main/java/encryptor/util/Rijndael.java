package encryptor.util;

import encryptor.model.EncryptedFileHeader;
import encryptor.model.EncryptionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

/**
 * Created by Wojtek on 2015-03-08.
 */
public class Rijndael {

    private static final Logger log = LoggerFactory.getLogger(Rijndael.class);


    public static byte[] generateSessionKey(Integer length) {
        SecureRandom random = new SecureRandom(String.valueOf(System.nanoTime()).getBytes());
        byte[] key = new byte[length/8];
        random.nextBytes(key);
        return key;

    }

    public static byte[] generateInitialVector(Integer length) {
        SecureRandom random = new SecureRandom(String.valueOf(System.nanoTime()).getBytes());
        byte[] initialVector = new byte[length/8];
        random.nextBytes(initialVector);
        return initialVector;
    }


    public static void encryptFile(File inputFile, File outputFile, EncryptedFileHeader header) throws IOException {
        //TODO
        try (BufferedWriter out = new BufferedWriter(new FileWriter(outputFile, true));
             FileInputStream in = new FileInputStream(inputFile)) {
            int bite;
            while ((bite = in.read()) != -1) {
                out.write(bite);
            }
        }
    }

    public static void decrypt(File inputFile, File outputFile, byte[] sessionKey, EncryptedFileHeader header) throws IOException {
        //TODO
        try (FileOutputStream outputStream = new FileOutputStream(outputFile);
             FileInputStream inputStream = new FileInputStream(inputFile);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            int bite;
            String line;
            boolean headerPassed = false;
            while (true) {
                if (!headerPassed) {
                    line = br.readLine();
                    if (line.startsWith(EncryptedFileHeader.END_TAG)) headerPassed = true;
                } else {
                    bite = br.read();
                    if (bite == -1) break;
                    outputStream.write(bite);
                }

            }
        }
    }

    public static byte[] encryptPrivateKey(PrivateKey key, String password) {
        try {
            Cipher cipher = Cipher.getInstance("Rijndael/ECB/PKCS7Padding");
            SecretKeySpec passwordKey = createKey(password);
            cipher.init(Cipher.ENCRYPT_MODE, passwordKey);

            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(key.getEncoded());
            int keyLength = key.getEncoded().length;

            return cipher.doFinal(pkcs8EncodedKeySpec.getEncoded());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.error("Cipher could not be acquired", e);
            throw new RuntimeException(e);
        } catch ( Exception e) {
            log.error("Cipher wrongly used", e);
            throw new RuntimeException(e);
        }
    }

    public static byte[] decryptPrivateKey(byte[] encryptedKey, String password) throws InvalidKeySpecException {
        try {
            Cipher cipher = Cipher.getInstance("Rijndael/ECB/PKCS7Padding");
            SecretKeySpec passwordKey = createKey(password);
            cipher.init(Cipher.DECRYPT_MODE, passwordKey);
            byte[] decryptedKey = cipher.doFinal(encryptedKey);
            return decryptedKey;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.error("Cipher could not be acquired", e);
            throw new RuntimeException(e);
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            log.error("Cipher wrongly used", e);
            throw new RuntimeException(e);
        }
    }

    private static SecretKeySpec createKey(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte[] paswordHash = md.digest();
        byte[] first128bytes = Arrays.copyOfRange(paswordHash, 0, 16);
        return new SecretKeySpec(first128bytes, "Rijndael");
    }


}
