package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import org.bson.Document;

import java.util.Map;

public class comentarioController {

    @FXML private Button boton;
    @FXML private Label labelCorreo;
    @FXML private Label labelNumero;
    @FXML private ImageView fotoView;
    @FXML private TextArea areaTexto;

    private Document documento;

    public comentarioController(){ documento = null; }

    public comentarioController(Document documento){ this.documento = documento; }

    @FXML public void initialize(){
        if(documento != null){
            boton.setText("Responder");
        }
        else{
            boton.setText("Aceptar");
        }
    }

    @FXML public void accionOnClick(){
        if(boton.getText().equals("Aceptar")){

        }
        else{

        }
    }
}
