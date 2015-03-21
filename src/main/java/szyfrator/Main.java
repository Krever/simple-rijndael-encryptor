package szyfrator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.security.Security;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
        primaryStage.setTitle("De/Szyfrator");
        primaryStage.setScene(new Scene(root, 430, 300));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}