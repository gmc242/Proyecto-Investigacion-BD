package controller;

import interfaz.MessageBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class PrincipalController {
    @FXML private ScrollPane contenedorPrincipal;

    @FXML public void resumenOnClick(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../interfaz/aficionadoCrud.fxml"));
        try{
            Node n = loader.load();
            contenedorPrincipal.setContent(n);
        }catch (Exception e){
            MessageBox.crearAlerta("No se puede encontrar el archivo que contiene la interfaz");
        }
    }

    @FXML public void aficionadoOnClick(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../interfaz/aficionadoCrud.fxml"));
        try{
            Node n = loader.load();
            contenedorPrincipal.setContent(n);
        }catch (Exception e){
            MessageBox.crearAlerta("No se puede encontrar el archivo que contiene la interfaz");
        }
    }

    @FXML public void inicioSesionOnClick(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../interfaz/inicioSesion.fxml"));
        try{
            Parent parent = (Parent) loader.load();
            Stage ventana = new Stage();
            ventana.setScene(new Scene(parent));
            ventana.showAndWait();
        }catch (Exception e){
            MessageBox.crearAlerta("No se puede encontrar el archivo que contiene la interfaz.");
        }
    }
}
