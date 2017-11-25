//2015145533
//Gerardo Monge Corella

package interfaz;

import javafx.scene.control.Alert;

public class MessageBox {

    private String respuesta;
    private Alert alerta;
    
    public MessageBox(Alert.AlertType tipo, String mensaje, javafx.scene.control.ButtonType... botones)
    {
        alerta = new Alert(tipo, mensaje, botones);
        java.util.Optional<javafx.scene.control.ButtonType> botonClick = alerta.showAndWait();
        if(botonClick.isPresent())
        {
            respuesta = botonClick.get().getText();
        }
    }
    
    public String getRespuesta()
    {
        alerta.close();
        return respuesta;
    }
}
