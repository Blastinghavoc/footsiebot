package footsiebot.guicore;

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

public class Message extends StackPane {
    private Label msg;
    private LocalDateTime timestamp;
    private boolean sent;
    private Rectangle visual;

    public Message(String text, LocalDateTime timestamp, Stage stage, boolean sent) {
        super();
        visual = new Rectangle();
        // visual.setFill(Color.LIME);
        // visual.getStyleClass().add("message-visual");
        setPrefWidth(stage.getScene().getWidth() - 36);
        setMaxWidth(stage.getScene().getWidth() - 36);
        msg = new Label(text);
        msg.setMaxWidth(stage.getScene().getWidth() * 0.55);
        msg.setWrapText(true);
        this.timestamp = timestamp;
        this.sent = sent;
        getChildren().addAll(visual, msg);
        if (sent) {
            msg.getStyleClass().add("user-label");
            visual.getStyleClass().add("user-visual");
            getStyleClass().add("user-message");
            msg.setAlignment(Pos.CENTER_RIGHT);
            setAlignment(Pos.CENTER_RIGHT);
            // setAlignment(visual, Pos.CENTER_LEFT);
        } else {
            msg.getStyleClass().add("system-label");
            visual.getStyleClass().add("system-visual");
            getStyleClass().add("system-message");
            msg.setAlignment(Pos.CENTER_LEFT);
            setAlignment(Pos.CENTER_LEFT);
        }
    }

    /**
    * Getter for msg
    *
    * @return returns msg
    */
    public Label getLabel() {
        return msg;
    }

    /**
    * Getter for the text of msg
    *
    * @return returns a String representation of msg
    */
    public String getText() {
        return msg.getText();
    }

    /**
    * Getter for sent
    *
    * @return returns sent
    */
    public boolean getSent() {
        return sent;
    }

    /**
    * Getter for visual
    *
    * @return returns visual
    */
    public Rectangle getVisual() {
        return visual;
    }

    /**
    * Getter for timestamp
    *
    * @return returns timestamp
    */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }


}
