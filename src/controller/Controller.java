package controller;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import interfaz.MessageBox;
import javafx.scene.control.Alert;
import org.bson.Document;

import java.util.Arrays;

public class Controller {

    private static MongoClient conexion;
    private static MongoDatabase db;

    public static void inicializar() {
        conexion = new MongoClient();
        db = conexion.getDatabase("bases");
    }

    public static MongoDatabase getDatabase(){
        return db;
    }

    public static MongoClient getConexion(){ return conexion; }

    public static void manejarExcepcion(Exception e){
        String mensaje = "Ha ocurrido un problema inesperado.";
        if(e instanceof MongoException){
            mensaje += "\nEl problema tiene código Mongo - " + ((MongoException)e).getCode();
        }
        mensaje += "\nMensaje de error: " + e.getMessage();
        MessageBox alerta = new MessageBox(Alert.AlertType.ERROR, mensaje);
    }

}
