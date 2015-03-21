package encryptor.util;

import java.security.SecureRandom;

/**
 * Created by Wojtek on 2015-03-08.
 */
public class Rijndael {


    public static byte[] generateSessionKey(Integer value) {
        SecureRandom random = new SecureRandom(String.valueOf(System.nanoTime()).getBytes());
        byte[] key = new byte[value/8];
        random.nextBytes(key);
        return key;

    }

    public static byte[] generateInitialVector(Integer value) {
        return new byte[]{0, 1, 1, 1, 1, 0};
    }

}
