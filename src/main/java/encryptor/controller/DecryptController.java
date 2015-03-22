package encryptor.controller;

import encryptor.model.EncryptedFileHeader;
import encryptor.model.UserAccess;
import encryptor.util.AlertUtil;
import encryptor.util.I18n;
import encryptor.util.RSAKeyFilesUtil;
import encryptor.util.RSAUtil;
import javafx.scene.control.Alert;
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

        String xmlHeader;
        try {
            FileInputStream inputStream = new FileInputStream(inputFile);
            xmlHeader = readXmlHeader(inputStream);
        } catch (Exception e) {
            log.error("Error occured durring reading file header.", e);
            AlertUtil.showErrorI18n(Optional.<String>empty(), Optional.<String>empty());
            return;
        }

        EncryptedFileHeader encryptedFileHeader;
        try {
            encryptedFileHeader = parseEncryptedFileHeader(xmlHeader);
            System.out.println(encryptedFileHeader);
        } catch (Exception e) {
            log.error("Error occured during parsing of file header", e);
            AlertUtil.showErrorI18n(Optional.<String>empty(), Optional.<String>empty());
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

            FileInputStream inputStream = new FileInputStream(inputFile);
            String xmlHeader = readXmlHeader(inputStream);

            PrivateKey privateKey = RSAKeyFilesUtil.loadPrivateKey(privateKeyFileField.getText(), passwordField.getText());

            UserAccess userAccess = identifierCombo.getValue();
            byte[] sessionKey = RSAUtil.decryptSessionKey(userAccess.getSessionKey(), privateKey);

            EncryptedFileHeader encryptedFileHeader = parseEncryptedFileHeader(xmlHeader);
            File outputFile = new File(outputFilePathProperty.getValue());
            rewriteData(inputFile, outputFile); //TODO

        } catch (Exception e) {
            log.error("Error occured during decryption", e);
            AlertUtil.showErrorI18n(Optional.<String>empty(), Optional.<String>empty());
        }

    }

    private EncryptedFileHeader parseEncryptedFileHeader(String xmlHeader) throws JAXBException {

        EncryptedFileHeader encryptedFileHeader;
        JAXBContext jaxbContext = JAXBContext.newInstance(EncryptedFileHeader.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        encryptedFileHeader = (EncryptedFileHeader) jaxbUnmarshaller.unmarshal(new StringReader(xmlHeader));
        return encryptedFileHeader;
    }

    private void rewriteData(File inputFile, File outputFile) throws IOException {
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

    private String readXmlHeader(FileInputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            StringBuilder buffer = new StringBuilder();
            while (true) {
                line = br.readLine();
                buffer.append(line).append("\n");
                if (line.startsWith("</encryptedFileHeader>")) break;
            }
            return buffer.toString();
        }
    }
}
