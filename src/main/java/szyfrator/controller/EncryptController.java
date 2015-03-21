package szyfrator.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import szyfrator.model.EncryptedFileHeader;
import szyfrator.model.EncryptionMode;
import szyfrator.model.UserAccess;
import szyfrator.model.UserKey;
import szyfrator.util.MyLogger;
import szyfrator.util.RSAUtil;
import szyfrator.util.Rijndael;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Created by Wojtek on 2015-03-07.
 */
public class EncryptController  extends TabController implements Initializable {

    public ComboBox<EncryptionMode> encryptionModeCombo;
    public ComboBox<Integer> keyLengthCombo;
    public ComboBox<Integer> blockLengthCombo;
    public TextField segmentLengthField;
    public ListView<UserKey> userList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        encryptionModeCombo.valueProperty().addListener(new ChangeListener<EncryptionMode>() {
            @Override
            public void changed(ObservableValue<? extends EncryptionMode> observable, EncryptionMode oldValue, EncryptionMode newValue) {
                if(EncryptionMode.OFB.equals(newValue) || EncryptionMode.CFB.equals(newValue)){
                    segmentLengthField.setDisable(false);
                } else {
                    segmentLengthField.setDisable(true);
                }
            }
        });

        encryptionModeCombo.getItems().addAll(EncryptionMode.values());
        encryptionModeCombo.getSelectionModel().select(0);
        keyLengthCombo.getItems().addAll(128, 192, 256);
        keyLengthCombo.getSelectionModel().select(0);
        blockLengthCombo.getItems().addAll(128, 192, 256);
        blockLengthCombo.getSelectionModel().select(0);
    }

    public void addUser(){
        try {
            Optional<Pair<String, String>> result = showAddUserDialog();
            if(result.isPresent()) {
                userList.getItems().addAll(new UserKey(result.get().getKey(), result.get().getValue()));
            }
        } catch (IOException e) {
            MyLogger.log("Wystapił problem przy dostępie do pliku klucza publicznego");
            MyLogger.log(e.toString());
        } catch (NoSuchAlgorithmException e) {
            MyLogger.log("Bardzo nieoczekiwany błąd");
            MyLogger.log(e.toString());
        } catch (InvalidKeySpecException e) {
            MyLogger.log("Wystapił problem przy wczytywaniu klucz publicznego. Klucz powinien być zakodowany przy pomocy X509");
            MyLogger.log(e.toString());
        }
    }

    public void removeUser(){
        try {
            userList.getItems().remove(userList.getSelectionModel().getSelectedItem());
        } catch (Exception e){
            MyLogger.log("Zaznacz element, który chcesz usunąć");
        }
    }

    public void encrypt() {
        MyLogger.clear();
        File inputFile = new File(inputFilePathProperty.getValue());
        if(!inputFile.canRead()) {
            MyLogger.log("Plik wejściowy nie może być odczytany");
            return;
        }
        File outputFile = new File(outputFilePathProperty.getValue());


        byte[] sessionKey = Rijndael.generateSessionKey(keyLengthCombo.getValue());
        byte[] initialVector = Rijndael.generateInitialVector(blockLengthCombo.getValue());
        List<UserAccess> accesses = RSAUtil.encryptSessionKey(sessionKey, userList.getItems());
        EncryptedFileHeader header =
                new EncryptedFileHeader(encryptionModeCombo.getValue(),blockLengthCombo.getValue(), null, initialVector, accesses);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EncryptedFileHeader.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(header, outputFile);
            jaxbMarshaller.marshal(header, System.out);
        }
        catch (Exception e){
            MyLogger.log("Wystąpił bład podczas zapisu do xml");
            MyLogger.log(e.toString());
        }

        try(BufferedWriter out = new BufferedWriter(new FileWriter(outputFile, true));
            FileInputStream in = new FileInputStream(inputFile)) {
            int bite;
            while((bite = in.read())!= -1){
                out.write(bite);
            }
        }catch (IOException e) {
            MyLogger.log(e.toString());
        }

    }

    private Optional<Pair<String, String>> showAddUserDialog(){
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Dodaj użytkownika");
        dialog.setHeaderText("Podaj dane użytkownik,a który ma mieć dostęp do szyfrowanego pliku.");

// Set the button types.
        ButtonType loginButtonType = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, cancelButtonType);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField identifierField = new TextField();
        identifierField.setPromptText("Identyfikator");
        TextField publicKeyField = new TextField();
        publicKeyField.setPromptText("Klucz publiczny");

        Button selectFileButton = new Button("Wybierz");
        selectFileButton.setOnAction(e -> {
            File file = (new FileChooser()).showSaveDialog(null);
            publicKeyField.setText(file.getAbsolutePath());
        });

        grid.add(new Label("Identyfikator:"), 0, 0);
        grid.add(identifierField, 1, 0);
        grid.add(new Label("Klucz publiczny:"), 0, 1);
        grid.add(publicKeyField, 1, 1);
        grid.add(selectFileButton, 2, 1);

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(identifierField::requestFocus);

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(identifierField.getText(), publicKeyField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        return result;
    }
}
