package encryptor.util;

import encryptor.model.EncryptedFileHeader;
import encryptor.model.EncryptionMode;
import javafx.concurrent.Task;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.input.ReaderInputStream;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.io.*;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
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
        byte[] key = new byte[length / 8];
        random.nextBytes(key);
        return key;

    }

    public static byte[] generateInitialVector(Integer length) {
        SecureRandom random = new SecureRandom(String.valueOf(System.nanoTime()).getBytes());
        byte[] initialVector = new byte[length / 8];
        random.nextBytes(initialVector);
        return initialVector;
    }


    public static Task<Void> encryptTask(File inputFile, File outputFile, EncryptedFileHeader header, byte[] sessionKeyBytes) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (OutputStream outputStream = new FileOutputStream(outputFile, true);
                     FileInputStream inputStream = new FileInputStream(inputFile)) {

                    EncryptionMode mode = header.getEncryptionMode();

                    PaddedBufferedBlockCipher blockCipher = createCipher(header.getBlockSize(), header.getEncryptionMode(), header.getSegmentSize());
                    CipherParameters parameters;
                    if (EncryptionMode.ECB.equals(mode))
                        parameters = new KeyParameter(sessionKeyBytes);
                    else
                        parameters = new ParametersWithIV(new KeyParameter(sessionKeyBytes), header.getInitialVector());
                    blockCipher.init(true, parameters);

                    CipherOutputStream cipherOutputStream = new CipherOutputStream(new Base64OutputStream(outputStream), blockCipher);
                    byte[] buf = new byte[1024];
                    int bytesRead;
                    int bytesReadSum = 0;
                    long fileLength = inputFile.length();
                    while ((bytesRead = inputStream.read(buf)) >= 0) {
                        cipherOutputStream.write(buf, 0, bytesRead);
                        bytesReadSum += bytesRead;
                        updateProgress(bytesReadSum, fileLength);
                    }

                    cipherOutputStream.close();

                }

                return null;
            }
        };
    }

    public static Task<Void> decryptTask(File inputFile, File outputFile, byte[] sessionKeyBytes, EncryptedFileHeader header) throws IOException {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (FileOutputStream outputStream = new FileOutputStream(outputFile);
                     FileInputStream inputStream = new FileInputStream(inputFile);
                     BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while (true) {
                        line = br.readLine();
                        if(line == null)
                            throw new RuntimeException("Invalid file header. Header should end with "+EncryptedFileHeader.END_TAG);
                        if (line.startsWith(EncryptedFileHeader.END_TAG))
                            break;
                    }
                    EncryptionMode mode = header.getEncryptionMode();

                    PaddedBufferedBlockCipher blockCipher = createCipher(header.getBlockSize(), header.getEncryptionMode(), header.getSegmentSize());
                    CipherParameters parameters;
                    if (EncryptionMode.ECB.equals(mode))
                        parameters = new KeyParameter(sessionKeyBytes);
                    else
                        parameters = new ParametersWithIV(new KeyParameter(sessionKeyBytes), header.getInitialVector());
                    blockCipher.init(false, parameters);


                    CipherInputStream cipherInputStream = new CipherInputStream(new Base64InputStream(new ReaderInputStream(br)), blockCipher);
                    byte[] buf = new byte[1024];
                    int bytesRead;
                    int bytesReadSum = 0;
                    long fileLength = (long) (inputFile.length()/1.37); //base64 size growth multiplier
                    while ((bytesRead = cipherInputStream.read(buf)) >= 0) {
                        outputStream.write(buf, 0, bytesRead);
                        bytesReadSum += bytesRead;
                        updateProgress(bytesReadSum, fileLength);
                    }
                    cipherInputStream.close();
                }
                return null;
            }
        };
    }

    private static PaddedBufferedBlockCipher createCipher(int blockSize, EncryptionMode encryptionMode, Integer segmentSize) {
        RijndaelEngine engine = new RijndaelEngine(blockSize);
        PKCS7Padding padding = new PKCS7Padding();

        switch (encryptionMode) {
            case ECB:
                return new PaddedBufferedBlockCipher(engine, padding);
            case CBC:
                return new PaddedBufferedBlockCipher(new CBCBlockCipher(engine), padding);
            case CFB:
                return new PaddedBufferedBlockCipher(new CFBBlockCipher(engine, segmentSize), padding);
            case OFB:
                return new PaddedBufferedBlockCipher(new OFBBlockCipher(engine, segmentSize), padding);
            default:
                throw new RuntimeException("Wrong encryption mode: "+encryptionMode);
        }
    }

    public static byte[] encryptPrivateKey(PrivateKey key, String password) {
        try {
            Cipher cipher = Cipher.getInstance("Rijndael/ECB/PKCS7Padding");
            SecretKeySpec passwordKey = createKey(password);
            cipher.init(Cipher.ENCRYPT_MODE, passwordKey);

            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(key.getEncoded());

            return cipher.doFinal(pkcs8EncodedKeySpec.getEncoded());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.error("Cipher could not be acquired", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Cipher wrongly used", e);
            throw new RuntimeException(e);
        }
    }

    public static byte[] decryptPrivateKey(byte[] encryptedKey, String password) throws InvalidKeySpecException {
        try {
            Cipher cipher = Cipher.getInstance("Rijndael/ECB/PKCS7Padding");
            SecretKeySpec passwordKey = createKey(password);
            cipher.init(Cipher.DECRYPT_MODE, passwordKey);
            return cipher.doFinal(encryptedKey);
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
