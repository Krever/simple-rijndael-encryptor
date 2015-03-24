package encryptor.controller;

import encryptor.util.AlertUtil;
import encryptor.util.I18n;
import encryptor.util.RSAKeyFilesUtil;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Optional;

/**
 * Created by Wojtek on 2015-03-15.
 */
public class KeygenController {

    public TextField publicKeyFileField;
    public TextField privateKeyFileField;
    public ProgressIndicator progressIndicator;
    public Button generateButton;
    public PasswordField passwordField;
    public PasswordField passwordConfirmField;

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    public void selectPublicKeyFile() {
        File file = (new FileChooser()).showSaveDialog(null);
        publicKeyFileField.setText(file.getAbsolutePath());
    }


    public void selectPrivateKeyFile() {
        File file = (new FileChooser()).showSaveDialog(null);
        privateKeyFileField.setText(file.getAbsolutePath());
    }

    public void generate() {

        if(! passwordField.getText().equals(passwordConfirmField.getText())){
            AlertUtil.showErrorI18n(Optional.<String>empty(), Optional.of("generate.passwordMatch.error.text"));
        }

        lockControls(true);

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws IOException {
                KeyPair keyPair = RSAKeyFilesUtil.generateKeyPair();
                RSAKeyFilesUtil.savePublicKey(keyPair.getPublic(), publicKeyFileField.getText());
                RSAKeyFilesUtil.savePrivateKey(keyPair.getPrivate(), passwordField.getText(), privateKeyFileField.getText());
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            lockControls(false);
            showOkDialog();
        });
        task.setOnFailed(event -> {
            lockControls(false);
            log.error("Error occured during creation of key pari", event.getSource().getException());
            AlertUtil.showGenericError();
        });
        new Thread(task).start();

    }

    private void lockControls(boolean lock){
        generateButton.setDisable(lock);
        privateKeyFileField.setDisable(lock);
        publicKeyFileField.setDisable(lock);
        passwordField.setDisable(lock);
        passwordConfirmField.setDisable(lock);
        progressIndicator.setVisible(lock);
    }

    private void showOkDialog()  {
        AlertUtil.showInfoI18n(Optional.<String>empty(), "keygen.generate.ok.text");
    }
}
