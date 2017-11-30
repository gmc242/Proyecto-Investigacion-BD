package controller;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import interfaz.MessageBox;
import javafx.scene.control.Alert;
import javafx.util.Pair;
import java.sql.*;

public class Controller {

    private static MongoClient conexion;
    private static MongoDatabase db;
    private static String usuario;

    public static void inicializar() {
        if(conexion != null)
            cerrarConexion();

        conexion = new MongoClient();
        db = conexion.getDatabase("bases");

        usuario = "";
    }

    public static MongoDatabase getDatabase(){
        return db;
    }

    public static MongoClient getConexion(){ return conexion; }

    public static void registrarUsuario(String usuario){
        Controller.usuario = usuario;
    }

    public static String obtenerUsuario(){ return usuario; }

    public static Pair<String, String> obtenerEquipos(int numeroPartido) throws Exception{
        try{

            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection conexionSQL = DriverManager.getConnection("jdbc:oracle:thin:dbaMundial/dba123456789@localhost");

            if(!conexionSQL.isValid(1000)){
                throw new Exception("No se ha podido establecer la conexion a la base de datos");
            }

            String query = "SELECT equipo1, equipo2 FROM partido WHERE numero_partido = ? ";
            PreparedStatement statement = conexionSQL.prepareStatement(query);
            statement.setInt(1, numeroPartido);

            ResultSet resultados = statement.executeQuery();
            resultados.next();

            String equipo1 = resultados.getString("equipo1");
            String equipo2 = resultados.getString("equipo2");

            return new Pair<>(equipo1, equipo2);

        }catch (SQLException e){
            throw e;
        }catch (Exception e){
            throw e;
        }
    }

    public static void manejarExcepcion(Exception e){
        String mensaje = "Ha ocurrido un problema inesperado.";
        if(e instanceof MongoException){
            mensaje += "\nEl problema tiene código Mongo - " + ((MongoException)e).getCode();
        }
        mensaje += "\nMensaje de error: " + e.getMessage();
        MessageBox alerta = new MessageBox(Alert.AlertType.ERROR, mensaje);
    }

    public static void cerrarConexion(){
        conexion.close();
        conexion = null;
    }

}
