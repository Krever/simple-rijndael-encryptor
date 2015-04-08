package encryptor.controller;

import encryptor.model.EncryptedFileHeader;
import encryptor.model.EncryptionMode;
import encryptor.model.UserAccess;
import encryptor.model.UserKey;
import encryptor.util.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.util.stream.Collectors.toList;

/**
 * Created by Wojtek on 2015-03-07.
 */
public class EncryptController extends TabController implements Initializable {

    public ComboBox<EncryptionMode> encryptionModeCombo;
    public ComboBox<Integer> keyLengthCombo;
    public ComboBox<Integer> blockSizeCombo;
    public ComboBox<Integer> segmentSizeCombo;
    public ListView<UserKey> receiverList;
    public ListView<UserKey> keyList;
    public ProgressBar progressBar;
    public Button encryptButton;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final KeyBaseDao keyBaseDao = new KeyBaseDao();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        encryptionModeCombo.valueProperty().addListener(new ChangeListener<EncryptionMode>() {
            @Override
            public void changed(ObservableValue<? extends EncryptionMode> observable, EncryptionMode oldValue, EncryptionMode newValue) {
                segmentSizeCombo.getItems().clear();
                if (EncryptionMode.OFB.equals(newValue) || EncryptionMode.CFB.equals(newValue)) {
                    segmentSizeCombo.setDisable(false);
                    for(int i = 8; i<=blockSizeCombo.getValue();i+=8){
                        segmentSizeCombo.getItems().add(i);
                    }
                    segmentSizeCombo.getSelectionModel().select(0);
                } else {
                    segmentSizeCombo.setDisable(true);
                }
            }
        });
        blockSizeCombo.valueProperty().addListener((observable, oldValue, newValue) ->  {
            segmentSizeCombo.getItems().clear();
            if (EncryptionMode.OFB.equals(encryptionModeCombo.getValue()) || EncryptionMode.CFB.equals(encryptionModeCombo.getValue())) {
                for(int i = 8; i<=newValue;i+=8){
                    segmentSizeCombo.getItems().add(i);
                }
                segmentSizeCombo.getSelectionModel().select(0);
            }
        });

        encryptionModeCombo.getItems().addAll(EncryptionMode.values());
        encryptionModeCombo.getSelectionModel().select(0);
        keyLengthCombo.getItems().addAll(128, 160, 192, 224, 256);
        keyLengthCombo.getSelectionModel().select(0);
        blockSizeCombo.getItems().addAll(128, 160, 192, 224, 256);
        blockSizeCombo.getSelectionModel().select(0);

        reloadKeys();
    }


    public void encrypt() {
        File inputFile = new File(inputFilePathProperty.getValue());
        File outputFile = new File(outputFilePathProperty.getValue());

        byte[] sessionKey = Rijndael.generateSessionKey(keyLengthCombo.getValue());
        byte[] initialVector = Rijndael.generateInitialVector(blockSizeCombo.getValue());
        List<UserAccess> accesses = RSAUtil.encryptSessionKey(sessionKey, receiverList.getItems());
        EncryptedFileHeader header =
                new EncryptedFileHeader(encryptionModeCombo.getValue(), blockSizeCombo.getValue(), segmentSizeCombo.getValue(), initialVector, accesses, keyLengthCombo.getValue());

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EncryptedFileHeader.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(header, outputFile);
        } catch (Exception e) {
            log.error("Error occured during writing file header.", e);
            AlertUtil.showGenericError();
            return;
        }


        Task<Void> encryptTask = Rijndael.encryptTask(inputFile, outputFile, header, sessionKey);

        encryptTask.setOnSucceeded(event -> {
            AlertUtil.showInfoI18n(Optional.<String>empty(), "encrypt.ok.text");
            encryptButton.setDisable(false);
        });
        encryptTask.setOnFailed(event -> {
            log.error("Error occured durring encryption.", event.getSource().getException());
            AlertUtil.showGenericError();
            encryptButton.setDisable(false);
        });

        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(encryptTask.progressProperty());
        encryptButton.setDisable(true);

        new Thread(encryptTask).start();


    }

    public void reloadKeys() {
        keyBaseDao.refreshKeys();
        ObservableSet<UserKey> keys = keyBaseDao.getKeys();
        ArrayList<UserKey> receivers = new ArrayList<>(receiverList.getItems().filtered(keys::contains));
        List<UserKey> notReceivers = keys.stream().filter(k -> !receivers.contains(k)).collect(toList());

        keyList.getItems().clear();
        keyList.getItems().addAll(notReceivers);
        keyList.getItems().sort((o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
        receiverList.getItems().clear();
        receiverList.getItems().sort((o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
    }

    public void addKeyAsReceiver() {
        List<UserKey> selectedKeys = new ArrayList<UserKey>(keyList.getSelectionModel().getSelectedItems());
        receiverList.getItems().addAll(selectedKeys);
        receiverList.getItems().sort((o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
        keyList.getItems().removeAll(selectedKeys);
    }

    public void addAllKeysAsReceivers() {
        receiverList.getItems().addAll(keyList.getItems());
        receiverList.getItems().sort((o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
        keyList.getItems().clear();
    }

    public void removeReceiver() {
        List<UserKey> selectedKeys = new ArrayList<UserKey>(receiverList.getSelectionModel().getSelectedItems());
        keyList.getItems().addAll(selectedKeys);
        keyList.getItems().sort((o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
        receiverList.getItems().removeAll(selectedKeys);
    }

    public void removeAllReceivers() {
        keyList.getItems().addAll(receiverList.getItems());
        keyList.getItems().sort((o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
        receiverList.getItems().clear();
    }

}
