package controller;

import com.mongodb.MongoException;
import interfaz.MessageBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.bson.Document;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;

public class ResumenController {

    @FXML private TextField partidoField;
    @FXML private TextArea resumenTexto;
    @FXML private Label equipo1Label;
    @FXML private Label equipo2Label;
    @FXML private VBox contenedorComentarios;
    @FXML private MediaView videoView;
    @FXML private Button reproducirBoton;

    private Document documento;
    private int indexActualVideo;
    private ArrayList<String> videos;
    private ArrayList<Integer> comentariosMostrados;

    public ResumenController(){
        videos = new ArrayList<>();
        comentariosMostrados = new ArrayList<>();
        indexActualVideo = -1;
    }

    public ResumenController(Document documento){
        this.documento = documento;
        comentariosMostrados = new ArrayList<>();
        videos = new ArrayList<>();
    }

    @FXML public void initialize(){

        if(documento != null){
            try{

                int numeroPartido = documento.getInteger("numero_partido");
                Pair<String, String> equipos = Controller.obtenerEquipos(numeroPartido);
                equipo1Label.setText(equipos.getKey());
                equipo2Label.setText(equipos.getValue());
                resumenTexto.setText(documento.getString("texto"));
                List<Document> comentarios = (List<Document>)documento.get("comentarios");

                if(comentarios != null)
                    for(Document comentario : comentarios)
                        popularComentario(comentario, 0, 0);

                inicializarURLS();

            }catch (Exception e){
                if(e instanceof SQLException)
                    Controller.manejarExcepcion(new Exception("NO PUEDE REGISTRAR EL RESUMEN PORQUE EL PARTIDO AÚN NO ESTÁ EN EL SISTEMA"));
                else
                    Controller.manejarExcepcion(e);
            }
        }
    }

    @FXML public void aceptarOnClick(){
        if(Controller.obtenerUsuario().equals("Administrador")){
            if(documento != null){
                String mensaje = resumenTexto.getText();
                int numero = documento.getInteger("numero_partido");

                try {
                    if (mensaje != null && !mensaje.equals("")) {
                        Controller.getDatabase().getCollection("resumenes").updateOne(
                                eq("numero_partido", numero),
                                set("texto", mensaje)
                        );
                        MessageBox.crearConfirmacion("Se ha actualizado con exito");
                    }
                }catch (Exception e){
                    MessageBox.crearAlerta("No se ha podido actualizar el resumen.\n Razon: " + e.getMessage());
                }
            }else{

                String mensaje = resumenTexto.getText();
                int numero = Integer.valueOf(partidoField.getText());

                ArrayList<Document> documentos = new ArrayList<>();
                for(String s : videos){
                    documentos.add(new Document("url", s));
                }

                try{
                    Controller.obtenerEquipos(numero);
                    Controller.getDatabase().getCollection("resumenes").insertOne(
                            new Document("numero_partido", numero).
                                    append("texto", mensaje).
                                    append("videos", documentos)
                    );
                    MessageBox.crearConfirmacion("Se creado con exito el resumen");
                }catch (Exception e){
                    if(e instanceof SQLException)
                        MessageBox.crearAlerta("NO PUEDE REGISTRAR EL RESUMEN PORQUE EL PARTIDO AÚN NO ESTÁ EN EL SISTEMA");
                    else
                        MessageBox.crearAlerta("No se ha podido crear el resumen en la base de datos." +
                                "\nRazon: " + e.getMessage());
                }
            }
        }else{
            MessageBox.crearAlerta("Debe iniciar sesion como administrador para hacer cambios o crear resumenes");
        }
    }

    @FXML public void buscarOnClick(){
        try{
            int numeroPartido = Integer.valueOf(partidoField.getText());

            Pair<String, String> equipos = Controller.obtenerEquipos(numeroPartido);

            if(equipos != null){
                equipo1Label.setText(equipos.getKey());
                equipo2Label.setText(equipos.getValue());
            }

            Document temp = Controller.getDatabase().getCollection("resumenes").
                    find(new Document("numero_partido", numeroPartido)).first();

            if(temp != null)
                documento = temp;

            initialize();

        }catch (Exception e){
            if(e instanceof SQLException) {
                MessageBox.crearAlerta("El partido no se encuentra registrado en el sistema SQL");
                e.printStackTrace();
            }else {
                MessageBox.crearAlerta("No se ha encontrado el partido en la base de datos local");
            }
        }
    }

    @FXML public void agregarComentarioOnClick(){

        HBox contenedor = new HBox();
        contenedor.setAlignment(Pos.CENTER_LEFT);
        contenedor.setSpacing(15);

        ComentarioController controller = new ComentarioController(documento.getInteger("numero_partido"), 0, 0);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../interfaz/comentarioCRUD.fxml"));
        loader.setController(controller);

        try {
            Node n = loader.load();
            contenedor.getChildren().add(n);
            contenedorComentarios.getChildren().add(contenedor);

            int numeroComentario = 0;

            for(Document resultado : Controller.getDatabase().getCollection("comentarios").
                    find(eq("numero_partido", documento.getInteger("numero_partido"))))

                numeroComentario = (numeroComentario > resultado.getInteger("numero_comentario")) ?
                        numeroComentario : resultado.getInteger("numero_comentario");

            numeroComentario++;

            comentariosMostrados.add(numeroComentario);

            Controller.getDatabase().getCollection("resumenes").updateOne(
                    eq("numero_partido", documento.getInteger("numero_partido")),
                    push("comentarios", new Document("id", numeroComentario))
            );

        }catch (Exception e){
            MessageBox.crearAlerta("No se ha podido crear el contenedor del comentario");
        }

    }

    @FXML public boolean anteriorOnClick(){
        if(indexActualVideo > 0 && videoView.getMediaPlayer() != null){
            indexActualVideo--;
            videoView.getMediaPlayer().stop();
            videoView.setMediaPlayer(null);
            videoView.setMediaPlayer(new MediaPlayer(new Media(videos.get(indexActualVideo))));
            return true;
        }else{
            MessageBox.crearAlerta("No hay videos anteriores");
            return false;
        }
    }

    @FXML public void reproducirOnClick(){
        if(reproducirBoton.getText().equals("Reproducir")){
            if(videoView.getMediaPlayer() != null) {
                videoView.getMediaPlayer().play();
                reproducirBoton.setText("Detener");
            }
            else
                MessageBox.crearAlerta("No hay video por reproducir");
        }
        else{
            if(videoView.getMediaPlayer() != null) {
                videoView.getMediaPlayer().stop();
                reproducirBoton.setText("Reproducir");
            }
            else
                MessageBox.crearAlerta("No hay video por detener");
        }
    }

    @FXML public boolean siguienteOnClick(){
        if(indexActualVideo < videos.size() && videoView.getMediaPlayer() != null){
            indexActualVideo++;
            videoView.getMediaPlayer().stop();
            videoView.setMediaPlayer(null);
            videoView.setMediaPlayer(new MediaPlayer(new Media(videos.get(indexActualVideo))));
            return true;
        }else{
            MessageBox.crearAlerta("No hay videos siguientes.");
            return false;
        }
    }

    @FXML public void eliminarOnClick(){
        int temp = indexActualVideo;

        try{
            if(Controller.obtenerUsuario().equals("Administrador")){
                if(indexActualVideo > -1) {

                    boolean unico = !siguienteOnClick();

                    if(unico)
                        unico = !anteriorOnClick();

                    if(unico)
                        videoView.setMediaPlayer(null);

                    String urlActual = videos.remove(temp);

                    if(documento != null)
                        Controller.getDatabase().getCollection("resumenes").updateOne(
                                eq("numero_partido", documento.getInteger("numero_partido")),
                                pull("videos", new Document("url", urlActual))
                        );
                }else{
                    MessageBox.crearAlerta("No hay videos por borrar");
                }
            }else {
                MessageBox.crearAlerta("Debe iniciar sesion como administrador para hacer cambios en el resumen");
            }
        }catch (Exception e){
            MessageBox.crearAlerta("No se ha podido eliminar el video");
        }
    }

    @FXML public void agregarOnClick(){
        if(indexActualVideo == -1){
            try {
                if(Controller.obtenerUsuario().equals("Administrador")) {
                    Stage stage = (Stage) partidoField.getScene().getWindow();
                    File video = new FileChooser().showOpenDialog(stage);

                    videos.add(video.toURI().toString());
                    videoView.setMediaPlayer(new MediaPlayer(new Media(video.toURI().toString())));
                    indexActualVideo = 0;

                    if (documento != null)
                        Controller.getDatabase().getCollection("resumenes").updateOne(
                                eq("numero_partido", documento.getInteger("numero_partido")),
                                push("videos", new Document("url", video.toURI().toString()))
                        );

                    MessageBox.crearConfirmacion("Se ha cargado con exito el video");
                }else {
                    MessageBox.crearAlerta("Debe iniciar sesion como administrador para hacer cambios en el resumen");
                }
            }catch (MongoException e){
                MessageBox.crearAlerta("No se ha podido agregar el video a la base de datos");
            }
            catch (Exception e){
                MessageBox.crearAlerta("No se ha podido cargar el video");
            }

        }else{

            try {
                if(Controller.obtenerUsuario().equals("Administrador")) {
                    Stage stage = (Stage) partidoField.getScene().getWindow();
                    File video = new FileChooser().showOpenDialog(stage);
                    videos.add(video.toURI().toString());

                    if (documento != null)
                        Controller.getDatabase().getCollection("resumenes").updateOne(
                                eq("numero_partido", documento.getInteger("numero_partido")),
                                push("videos", new Document("url", video.toURI().toString()))
                        );
                }else{
                    MessageBox.crearAlerta("Debe iniciar sesión como administrador primero");
                }
            }catch (MongoException e){
                MessageBox.crearAlerta("No se ha podido actualizar el resumen; no se ha podido agregar el video.");
            }
            catch (Exception e){
                MessageBox.crearAlerta("No se ha podido cargar el video");
            }
        }
    }

    private void inicializarURLS(){
        try{
            videos = new ArrayList<>();
            List<Document> urls = (List<Document>) documento.get("videos");

            if(urls != null) {

                for (Document video : urls) {
                    videos.add(video.getString("url"));
                }

                if (videos.size() > 0) {
                    videoView.setMediaPlayer(new MediaPlayer(new Media(videos.get(0))));
                    indexActualVideo = 0;
                }

            }
        }catch (Exception e){
            MessageBox.crearAlerta("Ha habido un error obtieniendo los URLS de los videos");
        }
    }

    private void popularComentario(Document comentario, int nivelesReply, int comentarioParent){

        if(!comentariosMostrados.contains(comentario.getInteger("numero_comentario"))){

            HBox contenedor = new HBox();
            contenedor.setAlignment(Pos.CENTER_LEFT);
            contenedor.setSpacing(15);

            for(int i = 0; i < nivelesReply; i++){
                contenedor.getChildren().add(new Separator(Orientation.VERTICAL));
            }

            if(comentario.size() == 1){
                comentario = Controller.getDatabase().getCollection("comentarios").
                        find(and(eq("numero_partido", documento.getInteger("numero_partido")),
                                eq("numero_comentario", comentario.getInteger("id")))).first();
            }

            ComentarioController controller = new ComentarioController(documento.getInteger("numero_partido"), comentario, nivelesReply, comentarioParent);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../interfaz/comentarioCRUD.fxml"));
            loader.setController(controller);

            try{

                Node n = loader.load();
                contenedor.getChildren().add(n);
                comentariosMostrados.add(comentario.getInteger("numero_comentario"));
                contenedorComentarios.getChildren().add(contenedor);

                List<Document> replies = (List<Document>) comentario.get("replies");
                int numeroPartido = documento.getInteger("numero_partido");

                if(replies != null) {
                    for (Document reply : replies) {
                        int numeroComentario = reply.getInteger("id");

                        Document replyDoc = Controller.getDatabase().getCollection("comentarios").find(
                                and(eq("numero_partido", numeroPartido), eq("numero_comentario", numeroComentario))).first();

                        if (replyDoc != null)
                            popularComentario(replyDoc, nivelesReply + 1, comentario.getInteger("numero_comentario"));
                    }
                }

            }catch (Exception e){
                new MessageBox(Alert.AlertType.ERROR, "No se puede mostrar el comentario");
                e.printStackTrace();
            }
        }
    }

}
