package encryptor.util;

import javafx.scene.control.Alert;

import java.util.Optional;

/**
 * Created by krever on 3/22/15.
 */
public class AlertUtil {


    public static void showError(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showErrorI18n(Optional<String> headerProperty, Optional<String> messageProperty) {
        String title = I18n.resourceBundle.getString("general.error.title");
        String header = headerProperty.isPresent() ? I18n.resourceBundle.getString(headerProperty.get()) : null;
        String content = messageProperty.isPresent() ? I18n.resourceBundle.getString(messageProperty.get()) : I18n.resourceBundle.getString("general.error.text");
        showError(title, header, content);
    }

    public static void showGenericError() {
        showErrorI18n(Optional.<String>empty(), Optional.<String>empty());
    }

    public static void showInfoI18n(Optional<String> headerProperty,String messageProperty) {
        String header = headerProperty.isPresent() ? I18n.resourceBundle.getString(headerProperty.get()) : null;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.resourceBundle.getString("general.info.title"));
        alert.setHeaderText(header);
        alert.setContentText(I18n.resourceBundle.getString(messageProperty));
        alert.show();
    }

}
