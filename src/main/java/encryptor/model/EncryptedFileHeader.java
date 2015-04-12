package encryptor.model;

import javax.xml.bind.annotation.*;
import java.util.List;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EncryptedFileHeader {

    public static final String END_TAG = "</encryptedFileHeader>";

    @XmlElement
    public final String algorithmName = "Rijndael";
    protected EncryptionMode encryptionMode;
    protected int keyLength;
    protected int blockSize;
    @XmlElement(required = false, nillable = true)
    protected Integer segmentSize;
    protected byte[] initialVector;
    @XmlElementWrapper(name = "users")
    @XmlElement(name = "user")
    protected List<UserAccess> users;

    public EncryptedFileHeader() {}

    public EncryptedFileHeader(EncryptionMode encryptionMode, int blockSize, Integer segmentSize, byte[] initialVector, List<UserAccess> users, int keyLength) {
        this.encryptionMode = encryptionMode;
        this.blockSize = blockSize;
        this.segmentSize = segmentSize;
        this.initialVector = initialVector;
        this.users = users;
        this.keyLength = keyLength;
    }

    public List<UserAccess> getUsers() {
        return users;
    }

    public EncryptionMode getEncryptionMode() {
        return encryptionMode;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public Integer getSegmentSize() {
        return segmentSize;
    }

    public byte[] getInitialVector() {
        return initialVector;
    }

    public int getKeyLength() {
        return keyLength;
    }
}
