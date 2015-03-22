package encryptor.controller;

import encryptor.model.EncryptedFileHeader;
import encryptor.model.UserAccess;
import encryptor.util.AlertUtil;
import encryptor.util.RSAKeyFilesUtil;
import encryptor.util.RSAUtil;
import encryptor.util.Rijndael;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.security.PrivateKey;
import java.util.Optional;


public class DecryptController extends TabController {

    public ComboBox<UserAccess> identifierCombo;
    public TextField privateKeyFileField;
    public PasswordField passwordField;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void reloadIdentifiers() {
        File inputFile = new File(inputFilePathProperty.getValue());
        EncryptedFileHeader encryptedFileHeader;
        try {
            encryptedFileHeader = readHeader(inputFile);
            System.out.println(encryptedFileHeader);
        } catch (Exception e) {
            log.error("Error occured during reading file header", e);
            AlertUtil.showGenericError();
            return;
        }

        identifierCombo.getItems().clear();
        identifierCombo.getItems().addAll(encryptedFileHeader.getUsers());
        if (!identifierCombo.getItems().isEmpty())
            identifierCombo.getSelectionModel().select(0);
    }

    public void decrypt() {
        try {
            File inputFile = new File(inputFilePathProperty.getValue());

            EncryptedFileHeader header = readHeader(inputFile);
            PrivateKey privateKey = RSAKeyFilesUtil.loadPrivateKey(privateKeyFileField.getText(), passwordField.getText());
            UserAccess userAccess = identifierCombo.getValue();
            byte[] sessionKey = RSAUtil.decryptSessionKey(userAccess.getSessionKey(), privateKey);

            File outputFile = new File(outputFilePathProperty.getValue());
            Rijndael.decrypt(inputFile, outputFile, sessionKey, header);

        } catch (Exception e) {
            log.error("Error occured during decryption", e);
            AlertUtil.showGenericError();
        }

    }

    private EncryptedFileHeader readHeader(File inputFile) throws IOException, JAXBException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)))) {
            String line;
            StringBuilder buffer = new StringBuilder();
            while (true) {
                line = br.readLine();
                buffer.append(line).append("\n");
                if (line.startsWith(EncryptedFileHeader.END_TAG)) break;
            }
            return parseHeader(buffer.toString());
        }
    }

    private EncryptedFileHeader parseHeader(String xmlHeader) throws JAXBException {
        EncryptedFileHeader encryptedFileHeader;
        JAXBContext jaxbContext = JAXBContext.newInstance(EncryptedFileHeader.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        encryptedFileHeader = (EncryptedFileHeader) jaxbUnmarshaller.unmarshal(new StringReader(xmlHeader));
        return encryptedFileHeader;
    }
}
