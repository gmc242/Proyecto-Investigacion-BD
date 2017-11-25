package controller;

import Funcionalidades.ValidacionPassword;
import com.mongodb.MongoException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;
import org.bson.Document;

public class aficionadoCrudController {

    @FXML private Label usuarioField;
    @FXML private PasswordField passField;
    @FXML private TextField emailField;
    @FXML private ImageView fotoView;
    @FXML private RadioButton siCorreo;
    @FXML private RadioButton noCorreo;
    @FXML private RadioButton siFoto;
    @FXML private RadioButton noFoto;

    private Document documento;

    @FXML public void initialize(){
        if(documento != null){

            usuarioField.setText(documento.getString("usuario"));
            emailField.setText(documento.getString("correo"));
            siCorreo.setSelected(documento.getBoolean("correo_presente"));
            noCorreo.setSelected(!siCorreo.isSelected());
            fotoView.setImage(new Image(documento.getString("foto")));
            siFoto.setSelected(documento.getBoolean("foto_presente"));
            noFoto.setSelected(!siFoto.isSelected());

        }
    }

    @FXML public void aceptarOnClick(){

        String correo = emailField.getText();

        if(documento != null) {


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

                }catch (MongoException e){
                    // Maneja excepcion Mongo
                }
                catch(Exception e){
                    // Manejar excepcion
                }
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
