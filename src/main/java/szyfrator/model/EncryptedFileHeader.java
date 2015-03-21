package szyfrator.model;

import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.List;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EncryptedFileHeader {

    @XmlElement
    public final String algorithmName = "Rijndael";
    protected EncryptionMode encryptionMode;
    protected int blockSize;
    @XmlElement(required = false, nillable = true)
    protected Integer segmentSize;
    protected byte[] initialVector;
    @XmlElementWrapper(name = "users")
    @XmlElement(name = "user")
    protected List<UserAccess> users;

    public EncryptedFileHeader() {}

    public EncryptedFileHeader(EncryptionMode encryptionMode, int blockSize, Integer segmentSize, byte[] initialVector, List<UserAccess> users) {
        this.encryptionMode = encryptionMode;
        this.blockSize = blockSize;
        this.segmentSize = segmentSize;
        this.initialVector = initialVector;
        this.users = users;
    }

    public List<UserAccess> getUsers() {
        return users;
    }
}
