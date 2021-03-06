package controller;

import Funcionalidades.ValidacionPassword;
import com.mongodb.client.MongoIterable;
import interfaz.MessageBox;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.Binary;

public class InicioSesionController {

    @FXML private TextField usuarioField;
    @FXML private PasswordField passField;

    private boolean valido;

    @FXML public void aceptarOnClick(){
        String usuario = usuarioField.getText();

        if(!usuario.equals("") && !passField.getText().equals("")){
            try {

                MongoIterable<Document> encontrados = Controller.getDatabase().
                        getCollection("aficionados").find(new Document("usuario", usuario));

                Document resultado = encontrados.first();

                if (resultado != null) {

                    byte[] sal = ((Binary) resultado.get("sal")).getData();
                    byte[] passEncriptado = ((Binary) resultado.get("pass")).getData();
                    valido = ValidacionPassword.esPasswordValido(passEncriptado, sal, passField.getText());

                    if(valido){
                        Controller.registrarUsuario(usuario);
                        MessageBox.crearConfirmacion("Ha iniciado sesión con exito");
                    }else{
                        MessageBox.crearAlerta("No se ha iniciado sesión, los datos no corresponden.");
                        Controller.registrarUsuario("");
                    }

                    // Cierra ventana
                    Stage stage = (Stage) usuarioField.getScene().getWindow();
                    stage.close();
                }
                else{
                    // Manda alerta no hay resultados
                    MessageBox alerta = new MessageBox(Alert.AlertType.ERROR, "No se han encontrado aficionados con ese nombre de usuario");
                    Controller.registrarUsuario("");
                }
            }catch (Exception e){
                // Maneja Excepcion
                Controller.manejarExcepcion(e);
                Controller.registrarUsuario("");
            }
        }
        else{
            // Manda alerta, los campos no pueden estar vacios
            MessageBox alerta = new MessageBox(Alert.AlertType.ERROR, "Ninguno de los campos pueden estar vacios");
            Controller.registrarUsuario("");
        }
    }

    @FXML public void cancelarOnClick(){
        //Cierra ventana
        Controller.registrarUsuario("");
        Stage stage = (Stage) usuarioField.getScene().getWindow();
        stage.close();
    }

    public boolean isValido(){
        return valido;
    }
}
