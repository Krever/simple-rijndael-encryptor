package encryptor.controller;

import encryptor.util.I18n;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    public EncryptController encryptTabController;
    @FXML
    public DecryptController decryptTabController;
    @FXML
    public TextField inputFileField;
    @FXML
    public TextField outputFileField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Arrays.asList(encryptTabController, decryptTabController).stream().forEach(c -> {
            c.bindInputFile(inputFileField.textProperty());
            c.bindOutputFile(outputFileField.textProperty());
        });
    }

    public void selectInputFile() {
        File file = (new FileChooser()).showOpenDialog(null);
        inputFileField.setText(file.getAbsolutePath());
    }

    public void selectOutputFile() {
        File file = (new FileChooser()).showSaveDialog(null);
        outputFileField.setText(file.getAbsolutePath());
    }

    public void switchFiles() {
        String inputFilePath = inputFileField.getText();
        inputFileField.setText(outputFileField.getText());
        outputFileField.setText(inputFilePath);
    }

    public void generateKeys() throws IOException {
        final Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("keygen.fxml"), I18n.resourceBundle);
        stage.setScene(new Scene(root));
        stage.setTitle(I18n.resourceBundle.getString("keygen.title"));
        stage.show();
    }

    public void showKeybaseWindow() throws IOException {
        final Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("key-base.fxml"), I18n.resourceBundle);
        stage.setScene(new Scene(root));
        stage.setTitle(I18n.resourceBundle.getString("keybase.title"));
        stage.show();
    }
}
