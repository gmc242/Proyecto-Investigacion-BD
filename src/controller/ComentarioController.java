package controller;

import com.mongodb.MongoException;
import interfaz.MessageBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;

public class ComentarioController {

    @FXML private Button botonResponder;
    @FXML private Button botonAceptar;
    @FXML private Label labelCorreo;
    @FXML private Label labelNumero;
    @FXML private Label labelUsuario;
    @FXML private ImageView fotoView;
    @FXML private TextArea areaTexto;
    @FXML private Label labelFecha;
    @FXML private VBox parent;

    private Document documento;
    private boolean comentarioPropio;
    private int numeroPartido;
    private int nivelesReply;
    private int comentarioParent;

    public ComentarioController(int numeroPartido, int nivelesReply, int comentarioParent){
        comentarioPropio = true;
        documento = null;
        this.numeroPartido = numeroPartido;
    }

    public ComentarioController(int numeroPartido, Document documento, int nivelesReply, int comentarioParent){
        this.documento = documento;
        this.nivelesReply = nivelesReply;
        this.comentarioParent = comentarioParent;
        this.numeroPartido = numeroPartido;
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

                if(!Controller.obtenerUsuario().equals("")) {

                    int numeroComentario = 0;

                    for (Document resultado : Controller.getDatabase().getCollection("comentarios").
                            find(eq("numero_partido", numeroPartido)))

                        numeroComentario = (numeroComentario > resultado.getInteger("numero_comentario")) ?
                                numeroComentario : resultado.getInteger("numero_comentario");

                    numeroComentario++;

                    Controller.getDatabase().getCollection("comentarios").
                            insertOne(new Document("numero_partido", numeroPartido).append
                                    ("numero_comentario", numeroComentario).append //
                                    ("mensaje", areaTexto.getText()).append
                                    ("usuario", Controller.obtenerUsuario()).append
                                    ("fecha", Date.from(Instant.now())));

                    if (comentarioParent > 0)
                        Controller.getDatabase().getCollection("comentarios").updateOne(
                                and(eq("numero_partido", numeroPartido),
                                        eq("numero_comentario", comentarioParent)),
                                push("replies", new Document("id", numeroComentario))
                        );

                    MessageBox.crearConfirmacion("Se creado el comentario con exito");

                }else{
                    MessageBox.crearAlerta("Debe iniciar sesión primero");
                }
            }else
                new MessageBox(Alert.AlertType.ERROR, "El cuerpo del comentario no puede estar vacio");
        }
        else{

            if(areaTexto != null) {

                if(!Controller.obtenerUsuario().equals("")) {

                    Controller.getDatabase().getCollection("comentarios").
                            updateOne(and(
                                    eq("numero_comentario", Integer.valueOf(labelNumero.getText())),
                                    eq("numero_partido", numeroPartido)), //Filtro
                                    set("mensaje", areaTexto.getText()));

                    MessageBox.crearConfirmacion("Se ha actualizado la informacion con exito");

                }else{
                    MessageBox.crearAlerta("Debe iniciar sesión primero");
                }
            }

            else
                new MessageBox(Alert.AlertType.ERROR, "El cuerpo del comentario no puede estar vacio");
        }
    }

    @FXML public void responderOnClick(){
        if(!Controller.obtenerUsuario().equals("")){

            HBox contenedor = new HBox();
            contenedor.setSpacing(15);
            contenedor.setAlignment(Pos.CENTER);

            for(int i = 0; i < nivelesReply; i++){
                contenedor.getChildren().add(new Separator(Orientation.VERTICAL));
            }

            ComentarioController controllerInterno = new ComentarioController(
                    numeroPartido, nivelesReply + 1, Integer.valueOf(labelNumero.getText()));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../interfaz/comentarioCrud.fxml"));
            loader.setController(controllerInterno);

            try{
                Node n = loader.load();
                contenedor.getChildren().add(n);
                VBox parentExterno = (VBox)parent.getParent().getParent();
                parentExterno.getChildren().add(parentExterno.getChildren().indexOf(parent.getParent()) + 1,
                        contenedor);

                int numeroComentarioNuevo = 0;

                for (Document resultado : Controller.getDatabase().getCollection("comentarios").
                        find(eq("numero_partido", numeroPartido)))

                    numeroComentarioNuevo = (numeroComentarioNuevo > resultado.getInteger("numero_comentario")) ?
                            numeroComentarioNuevo : resultado.getInteger("numero_comentario");

                numeroComentarioNuevo++;

                Controller.getDatabase().getCollection("comentarios").updateOne(
                        and(eq("numero_partido", documento.getInteger("numero_partido")),
                        eq("numero_comentario", documento.getInteger("numero_comentario"))),
                        push("replies", new Document("id", numeroComentarioNuevo))
                );

            }catch (Exception e){
                MessageBox.crearAlerta("No se puede mostrar el nuevo comentario ya que no se encuentra el archivo de interfaz");
                e.printStackTrace();
            }
        }else{
            MessageBox.crearAlerta("Debe iniciar sesion primero");
        }
    }

    private void determinarSiEsPropio(){
        String usuarioActual = Controller.obtenerUsuario();
        String usuarioDocumento = documento.getString("usuario");
        comentarioPropio = (usuarioActual.equals(usuarioDocumento));
    }

    private void popularDatosExistente(){

        String usuario = documento.getString("usuario");
        int numero = documento.getInteger("numero_comentario");

        labelNumero.setText(String.valueOf(numero));
        labelUsuario.setText(usuario);
        areaTexto.setText(documento.getString("mensaje"));
        labelFecha.setText(documento.getDate("fecha").toString());

        Document usuarioDoc = Controller.getDatabase().getCollection("aficionados").find(new Document("usuario", usuario)).first();

        if(usuarioDoc != null && usuarioDoc.getBoolean("correo_presente"))
            labelCorreo.setText(usuarioDoc.getString("correo"));
        else
            labelCorreo.setText("");

        if(usuarioDoc != null && usuarioDoc.getBoolean("foto_presente"))
            fotoView.setImage(new Image(usuarioDoc.getString("foto")));

        if(usuarioDoc.getString("correo").equals("BORRADO"))
            labelUsuario.setText("");

    }

    private void popularDatosNoExistente(){

        String usuario = Controller.obtenerUsuario();

        Document usuarioDoc = Controller.getDatabase().getCollection("aficionados").find(new Document("usuario", usuario)).first();
        labelUsuario.setText(usuario);
        labelFecha.setText(Date.from(Instant.now()).toString());

        if(usuarioDoc != null && usuarioDoc.getBoolean("correo_presente"))
            labelCorreo.setText(usuarioDoc.getString("correo"));
        else
            labelCorreo.setText("");

        if(usuarioDoc != null && usuarioDoc.getBoolean("foto_presente"))
            fotoView.setImage(new Image(usuarioDoc.getString("foto")));
    }
}
