package szyfrator.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by Wojtek on 2015-03-11.
 */
@XmlType
public class UserAccess {
    @XmlElement
    protected String identifier;
    @XmlElement
    protected byte[] sessionKey;

    public UserAccess() {

    }

    public UserAccess(String identifier, byte[] sessionKey) {
        this.identifier = identifier;
        this.sessionKey = sessionKey;
    }

    @Override
    public String toString() {
        return identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }
}
