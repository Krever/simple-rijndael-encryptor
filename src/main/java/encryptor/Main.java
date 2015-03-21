package encryptor;

import encryptor.util.I18n;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.security.Security;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"), I18n.resourceBundle);
        primaryStage.setTitle("De/Szyfrator");
        primaryStage.setScene(new Scene(root, 650, 380));
        primaryStage.show();
    }
}