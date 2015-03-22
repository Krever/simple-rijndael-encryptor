package encryptor.model;

import encryptor.util.RSAKeyFilesUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**
 * Created by Wojtek on 2015-03-11.
 */
public class UserKey {

    protected String identifier;
    protected PublicKey publicKey;


    public UserKey(){
    }

    public UserKey(String identifier, String publicKeyFilePath) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        this.identifier = identifier;
        this.publicKey = RSAKeyFilesUtil.loadPublicKey(publicKeyFilePath);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserKey)) return false;
        UserKey userKey = (UserKey) o;
        return Objects.equals(getIdentifier(), userKey.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier());
    }
}
