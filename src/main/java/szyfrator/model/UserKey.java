package szyfrator.model;

import szyfrator.util.RSAUtil;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by Wojtek on 2015-03-11.
 */
public class UserKey {

    protected String identifier;
    protected PublicKey publicKey;


    public UserKey(){
    }

    public UserKey(String identifier, String publicKeyFilePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.identifier = identifier;
        this.publicKey = RSAUtil.loadPublicKey(new File(publicKeyFilePath));
    }

    public String getIdentifier() {
        return identifier;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
