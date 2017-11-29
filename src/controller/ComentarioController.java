package controller;

import com.mongodb.MongoException;
import interfaz.MessageBox;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.Map;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class ComentarioController {

    @FXML private Button botonResponder;
    @FXML private Button botonAceptar;
    @FXML private Label labelCorreo;
    @FXML private Label labelNumero;
    @FXML private Label labelUsuario;
    @FXML private ImageView fotoView;
    @FXML private TextArea areaTexto;
    @FXML private VBox parent;

    private Document documento;
    private boolean comentarioPropio;
    private int numeroPartido;

    public ComentarioController(int numeroPartido){
        comentarioPropio = true;
        documento = null;
        numeroPartido = numeroPartido;
    }

    public ComentarioController(int numeroPartido, Document documento){
        this.documento = documento;
        numeroPartido = numeroPartido;
        determinarSiEsPropio();
    }

    @FXML public void initialize(){
        if(documento != null && comentarioPropio){
            botonAceptar.setText("Editar");
            botonResponder.setVisible(false);

            popularDatosExistente();
        }
        else if(documento != null){
            botonAceptar.setVisible(false);
            botonResponder.setVisible(true);

            popularDatosExistente();
        }
        else{
            botonAceptar.setText("Aceptar");
            botonResponder.setVisible(false);

            popularDatosNoExistente();
        }
    }

    @FXML public void aceptarOnClick(){
        if(botonAceptar.getText().equals("Aceptar")){

            if(areaTexto != null){

                Controller.getDatabase().getCollection("comentarios").
                        insertOne(new Document("numero_partido", numeroPartido).append
                                ("numero_comentario", Integer.valueOf(labelNumero.getText())).append
                                ("mensaje", areaTexto.getText()).append
                                ("usuario", Controller.obtenerUsuario()));

            }else
                new MessageBox(Alert.AlertType.ERROR, "El cuerpo del comentario no puede estar vacio");
        }
        else{

            if(areaTexto != null)

                Controller.getDatabase().getCollection("comentarios").
                        updateOne(and(
                                eq("numero_comentario", Integer.valueOf(labelNumero.getText())),
                                eq("numero_partido", numeroPartido)), //Filtro
                                set("mensaje", areaTexto.getText()));

            else
                new MessageBox(Alert.AlertType.ERROR, "El cuerpo del comentario no puede estar vacio");


        }
    }

    @FXML public void responderOnClick(){

    }

    private void determinarSiEsPropio(){
        String usuarioActual = Controller.obtenerUsuario();
        String usuarioDocumento = documento.getString("usuario");
        comentarioPropio = (usuarioActual.equals(usuarioDocumento));
    }

    private void popularDatosExistente(){

        String usuario = documento.getString("usuario");

        labelNumero.setText(documento.getLong("numero_comentario").toString());
        labelUsuario.setText(usuario);
        areaTexto.setText(documento.getString("mensaje"));

        Document usuarioDoc = Controller.getDatabase().getCollection("aficionados").find(new Document("usuario", usuario)).first();

        if(usuarioDoc != null && usuarioDoc.getBoolean("correo_presente"))
            labelCorreo.setText(usuarioDoc.getString("correo"));

        if(usuarioDoc != null && usuarioDoc.getBoolean("foto_presente"))
            fotoView.setImage(new Image(usuarioDoc.getString("foto")));

    }

    private void popularDatosNoExistente(){

        String usuario = Controller.obtenerUsuario();

        Document usuarioDoc = Controller.getDatabase().getCollection("aficionados").find(new Document("usuario", usuario)).first();
        labelUsuario.setText(usuario);

        if(usuarioDoc != null && usuarioDoc.getBoolean("correo_presente"))
            labelCorreo.setText(usuarioDoc.getString("correo"));

        if(usuarioDoc != null && usuarioDoc.getBoolean("foto_presente"))
            fotoView.setImage(new Image(usuarioDoc.getString("foto")));
    }
}
