package encryptor.controller;

import encryptor.model.EncryptedFileHeader;
import encryptor.model.EncryptionMode;
import encryptor.model.UserAccess;
import encryptor.model.UserKey;
import encryptor.util.KeyBaseDao;
import encryptor.util.MyLogger;
import encryptor.util.RSAUtil;
import encryptor.util.Rijndael;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.val;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.stream.Collectors.toList;

/**
 * Created by Wojtek on 2015-03-07.
 */
public class EncryptController extends TabController implements Initializable {

    private final KeyBaseDao keyBaseDao = new KeyBaseDao();
    public ComboBox<EncryptionMode> encryptionModeCombo;
    public ComboBox<Integer> keyLengthCombo;
    public ComboBox<Integer> blockLengthCombo;
    public TextField segmentLengthField;
    public ListView<UserKey> receiverList;
    public ListView<UserKey> keyList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        encryptionModeCombo.valueProperty().addListener(new ChangeListener<EncryptionMode>() {
            @Override
            public void changed(ObservableValue<? extends EncryptionMode> observable, EncryptionMode oldValue, EncryptionMode newValue) {
                if (EncryptionMode.OFB.equals(newValue) || EncryptionMode.CFB.equals(newValue)) {
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

        reloadKeys();
    }


    public void encrypt() {
        MyLogger.clear();
        File inputFile = new File(inputFilePathProperty.getValue());
        if (!inputFile.canRead()) {
            MyLogger.log("Plik wejściowy nie może być odczytany");
            return;
        }
        File outputFile = new File(outputFilePathProperty.getValue());


        byte[] sessionKey = Rijndael.generateSessionKey(keyLengthCombo.getValue());
        byte[] initialVector = Rijndael.generateInitialVector(blockLengthCombo.getValue());
        List<UserAccess> accesses = RSAUtil.encryptSessionKey(sessionKey, receiverList.getItems());
        EncryptedFileHeader header =
                new EncryptedFileHeader(encryptionModeCombo.getValue(), blockLengthCombo.getValue(), null, initialVector, accesses);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EncryptedFileHeader.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(header, outputFile);
            jaxbMarshaller.marshal(header, System.out);
        } catch (Exception e) {
            MyLogger.log("Wystąpił bład podczas zapisu do xml");
            MyLogger.log(e.toString());
        }

        try (BufferedWriter out = new BufferedWriter(new FileWriter(outputFile, true));
             FileInputStream in = new FileInputStream(inputFile)) {
            int bite;
            while ((bite = in.read()) != -1) {
                out.write(bite);
            }
        } catch (IOException e) {
            MyLogger.log(e.toString());
        }

    }

    public void reloadKeys() {
        keyBaseDao.reloadKeyBase();
        val keys = keyBaseDao.getKeys();
        ArrayList<UserKey> receivers = new ArrayList<>(receiverList.getItems().filtered(keys::contains));
        val notReceivers = keys.stream().filter(k -> !receivers.contains(k)).collect(toList());

        keyList.getItems().clear();
        keyList.getItems().addAll(notReceivers);
        receiverList.getItems().clear();
        receiverList.getItems().addAll(receivers);
    }

    public void addKeyAsReceiver() {
        val selectedKeys = new ArrayList<UserKey>(keyList.getSelectionModel().getSelectedItems());
        receiverList.getItems().addAll(selectedKeys);
        keyList.getItems().removeAll(selectedKeys);
    }

    public void addAllKeysAsReceivers() {
        receiverList.getItems().addAll(keyList.getItems());
        keyList.getItems().clear();
    }

    public void removeReceiver() {
        val selectedKeys = new ArrayList<UserKey>(receiverList.getSelectionModel().getSelectedItems());
        keyList.getItems().addAll(selectedKeys);
        receiverList.getItems().removeAll(selectedKeys);
    }

    public void removeAllReceivers() {
        keyList.getItems().addAll(receiverList.getItems());
        receiverList.getItems().clear();
    }

}