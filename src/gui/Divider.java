package footsiebot.gui;

import java.time.LocalDateTime;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.scene.text.Font;

public class Divider extends StackPane {
    private Rectangle ffs;

    public Divider(Stage stage) {
        super();
        setPrefWidth(stage.getScene().getWidth() - 36);
        setMaxWidth(stage.getScene().getWidth() - 36);
        ffs = new Rectangle(20, 3, Color.WHITE);
        getChildren().add(ffs);
    }
}
