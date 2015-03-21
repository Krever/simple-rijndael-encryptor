package szyfrator.controller;

import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import szyfrator.model.EncryptedFileHeader;
import szyfrator.model.UserAccess;
import szyfrator.util.MyLogger;
import szyfrator.util.RSAUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.security.PrivateKey;


public class DecryptController extends TabController {

    public ComboBox<UserAccess> identifierCombo;
    public TextField privateKeyFileField;
    public PasswordField passwordField;


    public void reloadIdentifiers() {

        File inputFile = new File(inputFilePathProperty.getValue());
        if (!inputFile.canRead()) {
            MyLogger.log("Plik wejściowy nie może być odczytany");
            return;
        }

        String xmlHeader;
        try {
            FileInputStream inputStream = new FileInputStream(inputFile);
            xmlHeader = readXmlHeader(inputStream);
        } catch (Exception e) {
            MyLogger.log("Wystąpił błąd podczas wczytywania nagłowka pliku wejściowego");
            MyLogger.log(e.toString());
            return;
        }

        EncryptedFileHeader encryptedFileHeader;
        try {
            encryptedFileHeader = parseEncryptedFileHeader(xmlHeader);
            System.out.println(encryptedFileHeader);
        } catch (Exception e) {
            MyLogger.log("Wystapił błąd przy wczytywaniu pliku wejściowego");
            MyLogger.log(e.toString());
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
            if (!inputFile.canRead()) {
                MyLogger.log("Plik wejściowy nie może być odczytany");
                return;
            }

            FileInputStream inputStream = new FileInputStream(inputFile);
            String xmlHeader = readXmlHeader(inputStream);

            PrivateKey privateKey = RSAUtil.loadPrivateKey(new File(privateKeyFileField.getText()), passwordField.getText());

            UserAccess userAccess = identifierCombo.getValue();
            byte[] sessionKey = RSAUtil.decryptSessionKey(userAccess.getSessionKey(), privateKey);

            EncryptedFileHeader encryptedFileHeader = parseEncryptedFileHeader(xmlHeader);
            //System.out.println(encryptedFileHeader);
            File outputFile = new File(outputFilePathProperty.getValue());
            rewriteData(inputFile, outputFile);

        } catch (Exception e) {
            MyLogger.handleException(e);
        }

    }

    private EncryptedFileHeader parseEncryptedFileHeader(String xmlHeader) {
        try {
            EncryptedFileHeader encryptedFileHeader;
            JAXBContext jaxbContext = JAXBContext.newInstance(EncryptedFileHeader.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            encryptedFileHeader = (EncryptedFileHeader) jaxbUnmarshaller.unmarshal(new StringReader(xmlHeader));
            return encryptedFileHeader;
        } catch (JAXBException e) {
            throw new RuntimeException("Problem z parsowaniem nagłówka", e);
        }
    }

    private void rewriteData(File inputFile, File outputFile) {
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
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Nie mozna odnaleźć pliku.", e);
        } catch (IOException e) {
            throw new RuntimeException("Problem z dostępem do pliku.", e);
        }
    }

    private String readXmlHeader(FileInputStream inputStream) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            StringBuilder buffer = new StringBuilder();
            while (true) {
                line = br.readLine();
                buffer.append(line).append("\n");
                if (line.startsWith("</encryptedFileHeader>")) break;
            }
            return buffer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Problem z odczytaniem pliku wejsciowego", e);
        }
    }
}
