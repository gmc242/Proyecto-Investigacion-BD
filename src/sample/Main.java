package sample;

import controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../interfaz/principal.fxml"));
        primaryStage.setTitle("Proyecto de Investigación - MongoDB");
        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.show();

        primaryStage.setOnCloseRequest((WindowEvent)->{
            Controller.cerrarConexion();
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
