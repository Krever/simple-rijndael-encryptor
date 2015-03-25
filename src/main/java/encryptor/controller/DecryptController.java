package encryptor.controller;

import encryptor.model.EncryptedFileHeader;
import encryptor.model.UserAccess;
import encryptor.util.AlertUtil;
import encryptor.util.RSAKeyFilesUtil;
import encryptor.util.RSAUtil;
import encryptor.util.Rijndael;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;


public class DecryptController extends TabController implements Initializable{

    public ComboBox<UserAccess> identifierCombo;
    public TextField privateKeyFileField;
    public PasswordField passwordField;
    public TextField passwordTextField;
    public ProgressBar progressBar;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());
        passwordTextField.setVisible(false);
        inputFilePathProperty.addListener((observable, oldValue, newValue) -> identifierCombo.getItems().clear());
    }

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
            System.out.println(Arrays.toString(sessionKey));


            Task<Void> decryptTask = Rijndael.decryptTask(inputFile, outputFile, sessionKey, header);
            decryptTask.setOnSucceeded(event -> AlertUtil.showInfoI18n(Optional.<String>empty(), "decrypt.ok.text"));
            decryptTask.setOnFailed(event -> {
                log.error("Error occured durring decryption", event.getSource().getException());
                AlertUtil.showGenericError();
            });
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(decryptTask.progressProperty());

            new Thread(decryptTask).start();

        } catch (Exception e) {
            log.error("Error occured before decryption", e);
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

    public void showPassword() {
        boolean visible = passwordTextField.isVisible();
        passwordTextField.setVisible(!visible);
        passwordField.setVisible(visible);
    }

    public void selectKeyFile() {
        File file = (new FileChooser()).showOpenDialog(null);
        privateKeyFileField.setText(file.getAbsolutePath());
    }
}
