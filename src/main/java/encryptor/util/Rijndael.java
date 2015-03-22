package encryptor.util;

import encryptor.model.EncryptedFileHeader;
import encryptor.model.EncryptionMode;

import java.io.*;
import java.security.SecureRandom;

/**
 * Created by Wojtek on 2015-03-08.
 */
public class Rijndael {


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
                    if (line.startsWith("</encryptedFile>")) headerPassed = true;
                } else {
                    bite = br.read();
                    if (bite == -1) break;
                    outputStream.write(bite);
                }

            }
        }
    }
}
