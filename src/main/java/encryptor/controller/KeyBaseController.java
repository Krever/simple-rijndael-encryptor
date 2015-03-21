package encryptor.controller;

import encryptor.model.UserKey;
import encryptor.util.I18n;
import encryptor.util.KeyBaseDao;
import javafx.collections.SetChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by krever on 3/21/15.
 */
public class KeyBaseController implements Initializable {
    private final KeyBaseDao keyBaseDao = new KeyBaseDao();
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public TextField identifierField;
    public TextField publicKeyFileField;
    public ListView<UserKey> keyList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        keyBaseDao.reloadKeyBase();
        keyList.getItems().addAll(keyBaseDao.getKeys());
        keyBaseDao.getKeys().addListener((SetChangeListener<UserKey>) change -> {
            if (change.wasAdded())
                keyList.getItems().add(change.getElementAdded());
            if (change.wasRemoved())
                keyList.getItems().remove(change.getElementRemoved());
        });
    }

    public void removeKey() {
        val selectedKeys = keyList.getSelectionModel().getSelectedItems();
        keyBaseDao.removeKeys(selectedKeys);
    }

    public void selectKeyFile() {
        File file = (new FileChooser()).showOpenDialog(null);
        publicKeyFileField.setText(file.getAbsolutePath());
    }

    public void addKey() {
        final String identifier = identifierField.getText();
        boolean keyExists = keyList.getItems().filtered(k -> k.getIdentifier().equals(identifier)).size() > 0;
        if (keyExists) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(I18n.resourceBundle.getString("keybase.add.confirm.title"));
            alert.setHeaderText(null);
            alert.setContentText(I18n.resourceBundle.getString("keybase.add.confirm.text"));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK) {
                return;
            }
        }


        try {
            UserKey userKey = new UserKey(identifier, publicKeyFileField.getText());
            keyBaseDao.addKey(userKey);
        } catch (Exception e) {
            log.debug("Error during adding key to keybase", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(I18n.resourceBundle.getString("general.error.title"));
            alert.setContentText(I18n.resourceBundle.getString("keybase.add.error.text"));
            alert.showAndWait();
        }
    }
}