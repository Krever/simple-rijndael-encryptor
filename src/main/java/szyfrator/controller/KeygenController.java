package szyfrator.controller;

import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import szyfrator.util.RSAKeyUtil;

import java.io.File;
import java.security.KeyPair;

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


    public void selectPublicKeyFile() {
        File file = (new FileChooser()).showSaveDialog(null);
        publicKeyFileField.setText(file.getAbsolutePath());
    }


    public void selectPrivateKeyFile() {
        File file = (new FileChooser()).showSaveDialog(null);
        privateKeyFileField.setText(file.getAbsolutePath());
    }

    public void generate() {

        lockControls(true);

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call(){
                KeyPair keyPair = RSAKeyUtil.generateKeyPair();
                RSAKeyUtil.savePublicKey(keyPair.getPublic(), publicKeyFileField.getText());
                RSAKeyUtil.savePrivateKey(keyPair.getPrivate(), privateKeyFileField.getText());
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            lockControls(false);
            showOkDialog();
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

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacja");
        alert.setHeaderText(null);
        alert.setContentText("Poprawnie wygenerowano parę kluczy RSA.");

        alert.show();
    }
}
