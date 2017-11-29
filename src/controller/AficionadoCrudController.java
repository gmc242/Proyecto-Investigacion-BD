package controller;

import Funcionalidades.ValidacionPassword;
import com.mongodb.MongoException;
import interfaz.MessageBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.bson.Document;

import java.io.File;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class AficionadoCrudController {

    @FXML private TextField usuarioField;
    @FXML private PasswordField passField;
    @FXML private TextField emailField;
    @FXML private ImageView fotoView;
    @FXML private RadioButton siCorreo;
    @FXML private RadioButton noCorreo;
    @FXML private RadioButton siFoto;
    @FXML private RadioButton noFoto;
    @FXML private RadioButton modificarSi;
    @FXML private HBox panelModificar;

    private Document documento;

    @FXML public void initialize(){
        if(documento != null){
            panelModificar.setVisible(true);
            usuarioField.setText(documento.getString("usuario"));
            emailField.setText(documento.getString("correo"));
            siCorreo.setSelected(documento.getBoolean("correo_presente"));
            noCorreo.setSelected(!siCorreo.isSelected());
            fotoView.setImage(new Image(documento.getString("foto")));
            siFoto.setSelected(documento.getBoolean("foto_presente"));
            noFoto.setSelected(!siFoto.isSelected());
        }
        else
            panelModificar.setVisible(false);
    }

    @FXML public void aceptarOnClick(){

        String correo = emailField.getText();

        if(documento != null) {

            String usuarioAntesIS = Controller.obtenerUsuario();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../interfaz/inicioSesion.fxml"));
            InicioSesionController controllerInicio = loader.getController();

            try {

                Parent n = loader.load();
                Stage ventana = new Stage();
                ventana.setScene(new Scene(n, 400, 200));
                ventana.showAndWait();

                if(controllerInicio.isValido() && usuarioAntesIS.equals(Controller.obtenerUsuario())){

                    Pair<byte[], byte[]> resultadoDeEncriptacion = ValidacionPassword.generarEncriptado(passField.getText());
                    byte[] sal = resultadoDeEncriptacion.getKey();
                    byte[] encriptado = resultadoDeEncriptacion.getValue();

                    Document query = new Document("correo", correo).
                            append("correo_presente", siCorreo.isSelected()).append("foto", fotoView.getImage().getUrl()).
                            append("foto_presente", siFoto.isSelected()).append("sal", sal).append("pass", encriptado);

                    Controller.getDatabase().getCollection("aficionados").
                            updateOne(eq("usuario", usuarioField.getText()),
                                    combine(set("correo", correo),
                                            set("correo_presente", siCorreo.isSelected()),
                                            set("foto", fotoView.getImage().getUrl()),
                                            set("foto_presente", siFoto.isSelected()),
                                            set("sal", sal),
                                            set("pass", encriptado)));
                }
                else{
                    MessageBox alerta = new MessageBox(Alert.AlertType.ERROR, "El password o usuario ingresado no es correcto");
                }
            }catch (Exception e){
                Controller.manejarExcepcion(e);
            }
        }
        else{
            if(!passField.getText().equals("") && validarCorreo(correo)){
                try {

                    Pair<byte[], byte[]> resultadoDeEncriptacion = ValidacionPassword.generarEncriptado(passField.getText());
                    byte[] sal = resultadoDeEncriptacion.getKey();
                    byte[] encriptado = resultadoDeEncriptacion.getValue();

                    Document query = new Document("usuario", usuarioField.getText()).append("correo", correo).
                            append("correo_presente", siCorreo.isSelected()).append("foto", fotoView.getImage().getUrl()).
                            append("foto_presente", siFoto.isSelected()).append("sal", sal).append("pass", encriptado);

                    Controller.getDatabase().getCollection("aficionados").insertOne(query);

                }catch(Exception e){
                    Controller.manejarExcepcion(e);
                }
            }
        }
    }

    @FXML
    public void modificarOnClick(){
        passField.setDisable(!modificarSi.isSelected());
    }

    @FXML
    public void cargarFotoOnClick(){
        Stage stage = (Stage) usuarioField.getScene().getWindow();
        File imagen = new FileChooser().showOpenDialog(stage);
        if(imagen != null){
            try{
                fotoView.setImage(new Image(imagen.toURI().toString()));
            }catch (Exception e){
                MessageBox.crearAlerta("No se ha podido cargar la foto. \nRazon: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void setDocumento(Document documento){
        this.documento = documento;
        initialize();
    }

    private boolean validarCorreo(String email){
        if(email.matches("([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)"))
            return true;
        return false;
    }


}
