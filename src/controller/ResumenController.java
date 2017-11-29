package controller;

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

    ResumenController(){
        comentariosMostrados = new ArrayList<>();
    }

    ResumenController(Document documento){
        this.documento = documento;
        comentariosMostrados = new ArrayList<>();
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

                for(Document comentario : comentarios)
                    popularComentario(comentario, 0);

            }catch (Exception e){
                if(e instanceof SQLException)
                    Controller.manejarExcepcion(new Exception("NO PUEDE REGISTRAR EL RESUMEN PORQUE EL PARTIDO AÚN NO ESTÁ EN EL SISTEMA"));
                else
                    Controller.manejarExcepcion(e);
            }
        }
    }

    @FXML public void aceptarOnClick(){
        if(documento != null){
            String mensaje = resumenTexto.getText();
            int numero = documento.getInteger("numero");
            if(mensaje != null && !mensaje.equals("")){
                Controller.getDatabase().getCollection("resumenes").updateOne(
                        eq("numero_partido", numero),
                        set("texto", mensaje)
                );
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
            }catch (Exception e){
                if(e instanceof SQLException)
                    MessageBox.crearAlerta("NO PUEDE REGISTRAR EL RESUMEN PORQUE EL PARTIDO AÚN NO ESTÁ EN EL SISTEMA");
            }
        }
    }

    @FXML public void buscarOnClick(){
        try{
            int numeroPartido = Integer.valueOf(partidoField.getText());
            Document temp = Controller.getDatabase().getCollection("resumenes").
                    find(new Document("numero_partido", numeroPartido)).first();
            if(temp != null)
                documento = temp;
            initialize();
        }catch (Exception e){
            MessageBox.crearAlerta("No se ha encontrado el partido en la base de datos local");
        }
    }

    @FXML public void anteriorOnClick(){
        if(indexActualVideo > 0){
            indexActualVideo--;
            videoView.getMediaPlayer().stop();
            videoView.setMediaPlayer(new MediaPlayer(new Media(videos.get(indexActualVideo))));
        }
    }

    @FXML public void reproducirOnClick(){
        if(reproducirBoton.getText().equals("Reproducir")){
            reproducirBoton.setText("Detener");
            if(videoView.getMediaPlayer() != null)
                videoView.getMediaPlayer().play();
        }
        else{
            reproducirBoton.setText("Reproducir");
            if(videoView.getMediaPlayer() != null)
                videoView.getMediaPlayer().stop();
        }
    }

    @FXML public void siguienteOnClick(){
        if(indexActualVideo < videos.size()){
            indexActualVideo++;
            videoView.getMediaPlayer().stop();
            videoView.setMediaPlayer(new MediaPlayer(new Media(videos.get(indexActualVideo))));
        }
    }

    @FXML public void eliminarOnClick(){
        int temp = indexActualVideo;
        siguienteOnClick();
        String urlActual = videos.get(temp);

        Controller.getDatabase().getCollection("resumenes").updateOne(
                eq("numero_partido", documento.getInteger("numero_partido")),
                pull("videos", new Document("url", urlActual))
        );
    }

    @FXML public void agregarOnClick(){
        if(indexActualVideo == -1){

            indexActualVideo = 0;
            Stage stage = (Stage) partidoField.getScene().getWindow();
            File video = new FileChooser().showOpenDialog(stage);
            videos.add(video.getAbsolutePath());

            Controller.getDatabase().getCollection("resumenes").updateOne(
                    eq("numero_partido", documento.getInteger("numero_partido")),
                    push("videos", new Document("url", video.getAbsolutePath()))
            );

        }else{
            Stage stage = (Stage) partidoField.getScene().getWindow();
            File video = new FileChooser().showOpenDialog(stage);
            videos.add(video.getAbsolutePath());
        }
    }

    private void inicializarURLS(){
        try{
            videos = new ArrayList<>();
            List<Document> urls = (List<Document>) documento.get("videos");
            for(Document video : urls){
                videos.add(video.getString("url"));
            }
        }catch (Exception e){
            MessageBox.crearAlerta("Ha habido un error obtieniendo los URLS de los videos");
        }
    }

    private void popularComentario(Document comentario, int nivelesReply){

        if(!comentariosMostrados.contains(comentario.getInteger("numero_comentario"))){

            HBox contenedor = new HBox();
            contenedor.setAlignment(Pos.CENTER_LEFT);
            contenedor.setSpacing(15);

            for(int i = 0; i < nivelesReply; i++){
                contenedor.getChildren().add(new Separator(Orientation.VERTICAL));
            }

            ComentarioController controller = new ComentarioController(documento.getInteger("numero_partido"), comentario);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../interfaz/comentarioCRUD.fxml"));
            loader.setController(controller);

            try{

                Node n = loader.load();
                contenedor.getChildren().add(n);
                comentariosMostrados.add(comentario.getInteger("numero_comentario"));

                List<Document> replies = (List<Document>) comentario.get("replies");
                int numeroPartido = documento.getInteger("numero_partido");

                for(Document reply : replies){
                    int numeroComentario = reply.getInteger("id");

                    Document replyDoc = Controller.getDatabase().getCollection("comentarios").find(
                            and(eq("numero_partido", numeroPartido), eq("numero_comentario", numeroComentario))).first();

                    if(replyDoc != null)
                        popularComentario(replyDoc, nivelesReply+1);
                }

            }catch (Exception e){
                new MessageBox(Alert.AlertType.ERROR, "No se puede mostrar el comentario");
            }
        }
    }

}
