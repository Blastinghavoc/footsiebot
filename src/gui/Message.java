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
import javafx.scene.text.Text;
import java.lang.Math;
import javafx.animation.*;
import javafx.util.Duration;

public class Message extends FlowPane {
    private Label msg;
    private LocalDateTime timestamp;
    private boolean sent;
    private Rectangle visual;
    private StackPane msgWrapper;
    private StackPane btnWrapper;
    private double width;
    private double height;
    private GUIcore ui;
    private boolean isAI;
    private Message parent;

    public Message(String text, LocalDateTime timestamp, boolean sent, boolean isAI, GUIcore ui) {
        super();
        double chatPaneWidth = ui.getStage().getScene().getWidth() * 0.6875;

        this.timestamp = timestamp;
        this.sent = sent;
        this.ui = ui;
        this.isAI = isAI;
        parent = null;
        setPrefWidth(chatPaneWidth - 36);
        setMaxWidth(chatPaneWidth - 36);

        final double maxWidth = (chatPaneWidth - 36) * 0.55;
        Text sizing = new Text(text);
        if (Math.ceil(sizing.getLayoutBounds().getWidth()) > maxWidth)
            sizing.setWrappingWidth(maxWidth);

        width = Math.ceil(sizing.getLayoutBounds().getWidth());
        height = Math.ceil(sizing.getLayoutBounds().getHeight());
        msg = new Label(text);
        msg.setMaxWidth(width);
        msg.setMinHeight(height);
        msg.setWrapText(true);

        visual = new Rectangle((width + 14), (height + 8));
        msgWrapper = new StackPane();
        msgWrapper.setMinWidth(visual.getWidth());
        msgWrapper.setMaxWidth(visual.getWidth());
        msgWrapper.getChildren().addAll(visual, msg);

        Tooltip aiNote = new Tooltip("Why am I seeing this?");
        Tooltip removeNote = new Tooltip("Remove message");

        if (isAI) {
            btnWrapper = new StackPane();
            Insets btnPadding = new Insets(0, 5, 2, 5);
            btnWrapper.setPadding(btnPadding);
            Label close = new Label("x");
            close.getStyleClass().add("remove-message");
            close.setTooltip(removeNote);
            Text lblSize = new Text("?");
            Label why = new Label("?");
            why.setTooltip(aiNote);
            why.setAlignment(Pos.CENTER);
            why.setMinWidth(lblSize.getLayoutBounds().getHeight());
            why.getStyleClass().add("options-button");
            btnWrapper.getChildren().addAll(why, close);
            btnWrapper.setAlignment(close, Pos.TOP_CENTER);
            btnWrapper.setAlignment(why, Pos.BOTTOM_CENTER);

            why.setOnMouseClicked(e -> {
                ui.displayMessage("This is why", false, this);
            });

            close.setOnMouseClicked(e -> {
                for (int i = 0; i < ui.getMessageBoard().getChildren().size(); i++) {
                    if (ui.getMessageBoard().getChildren().get(i) instanceof Message) {
                        Message tmp = (Message) ui.getMessageBoard().getChildren().get(i);
                        if (tmp.getParentMsg() == this)
                            ui.getMessageBoard().getChildren().removeAll(tmp);
                    }
                }
                ui.getMessageBoard().getChildren().removeAll(this);
            });
        }

        if (sent) {
            if (isAI)
                getChildren().add(btnWrapper);
            getChildren().add(msgWrapper);
            msg.getStyleClass().add("user-label");
            visual.getStyleClass().add("user-visual");
            getStyleClass().add("user-message");
            msg.setAlignment(Pos.CENTER_RIGHT);
            setAlignment(Pos.CENTER_RIGHT);
        } else {
            getChildren().addAll(msgWrapper);
            if (isAI)
                getChildren().add(btnWrapper);
            msg.getStyleClass().add("system-label");
            visual.getStyleClass().add("system-visual");
            getStyleClass().add("system-message");
            msg.setAlignment(Pos.CENTER_LEFT);
            setAlignment(Pos.CENTER_LEFT);
        }
    }

    public Message(String text, LocalDateTime timestamp, GUIcore ui, Message parent) {
        super();
        double chatPaneWidth = ui.getStage().getScene().getWidth() * 0.6875;

        this.timestamp = timestamp;
        sent = false;
        this.ui = ui;
        isAI = false;
        this.parent = parent;
        setPrefWidth(chatPaneWidth - 36);
        setMaxWidth(chatPaneWidth - 36);

        final double maxWidth = (chatPaneWidth - 36) * 0.55;
        Text sizing = new Text(text);
        if (Math.ceil(sizing.getLayoutBounds().getWidth()) > maxWidth)
            sizing.setWrappingWidth(maxWidth);

        width = Math.ceil(sizing.getLayoutBounds().getWidth());
        height = Math.ceil(sizing.getLayoutBounds().getHeight());
        msg = new Label(text);
        msg.setMaxWidth(width);
        msg.setMinHeight(height);
        msg.setWrapText(true);

        visual = new Rectangle((width + 14), (height + 8));
        msgWrapper = new StackPane();
        msgWrapper.setMinWidth(visual.getWidth());
        msgWrapper.setMaxWidth(visual.getWidth());
        msgWrapper.getChildren().addAll(visual, msg);

        Tooltip aiNote = new Tooltip("Why am I seeing this?");
        Tooltip removeNote = new Tooltip("Remove message");

        if (isAI) {
            btnWrapper = new StackPane();
            Insets btnPadding = new Insets(0, 5, 2, 5);
            btnWrapper.setPadding(btnPadding);
            Label close = new Label("x");
            close.getStyleClass().add("remove-message");
            close.setTooltip(removeNote);
            Text lblSize = new Text("?");
            Label why = new Label("?");
            why.setTooltip(aiNote);
            why.setAlignment(Pos.CENTER);
            why.setMinWidth(lblSize.getLayoutBounds().getHeight());
            why.getStyleClass().add("options-button");
            btnWrapper.getChildren().addAll(why, close);
            btnWrapper.setAlignment(close, Pos.TOP_CENTER);
            btnWrapper.setAlignment(why, Pos.BOTTOM_CENTER);

            why.setOnMouseClicked(e -> {
                ui.displayMessage("This is why", false, this);
            });

            close.setOnMouseClicked(e -> {
                for (int i = 0; i < ui.getMessageBoard().getChildren().size(); i++) {
                    if (ui.getMessageBoard().getChildren().get(i) instanceof Message) {
                        Message tmp = (Message) ui.getMessageBoard().getChildren().get(i);

                        if (tmp.getParentMsg() == this)
                            ui.getMessageBoard().getChildren().removeAll(tmp);
                    }
                }
                ui.getMessageBoard().getChildren().removeAll(this);
            });
        }

        if (sent) {
            if (isAI)
                getChildren().add(btnWrapper);
            getChildren().add(msgWrapper);
            msg.getStyleClass().add("user-label");
            visual.getStyleClass().add("user-visual");
            getStyleClass().add("user-message");
            msg.setAlignment(Pos.CENTER_RIGHT);
            setAlignment(Pos.CENTER_RIGHT);
        } else {
            getChildren().addAll(msgWrapper);
            if (isAI)
                getChildren().add(btnWrapper);
            msg.getStyleClass().add("system-label");
            visual.getStyleClass().add("system-visual");
            getStyleClass().add("system-message");
            msg.setAlignment(Pos.CENTER_LEFT);
            setAlignment(Pos.CENTER_LEFT);
        }
    }

   /**
    * Resizes the visual parts of the Message
    *
    * @param stage the stage used to calculate widths
    */
    public void resize(Stage stage) {
        double chatPaneWidth = stage.getScene().getWidth() * 0.6875;
        setMinWidth(chatPaneWidth - 36);
        setMaxWidth(chatPaneWidth - 36);

        final double maxWidth = (chatPaneWidth - 36) * 0.55;
        Text sizing = new Text(msg.getText());
        if (Math.ceil(sizing.getLayoutBounds().getWidth()) > maxWidth)
            sizing.setWrappingWidth(maxWidth);

        width = Math.ceil(sizing.getLayoutBounds().getWidth());
        height = Math.ceil(sizing.getLayoutBounds().getHeight());
        msgWrapper.setMinWidth(visual.getWidth());
        msgWrapper.setMaxWidth(visual.getWidth());
        msg.setMaxWidth(width);
        visual.setWidth(width + 14);
        visual.setHeight(height + 8);

        if (isAI)
            btnWrapper.setMaxHeight(visual.getHeight());

        setMinHeight(visual.getHeight() + 10);
        setMaxHeight(visual.getHeight() + 10);
    }

   /**
    * Accessor for parent
    *
    * @return the parent of this Message
    */
    public Message getParentMsg() {
        return parent;
    }

   /**
    * Accessor for msg
    *
    * @return returns msg
    */
    public Label getLabel() {
        return msg;
    }

   /**
    * Accessor for the text of msg
    *
    * @return returns a String representation of msg
    */
    public String getText() {
        return msg.getText();
    }

   /**
    * Accessor for sent
    *
    * @return returns sent
    */
    public boolean getSent() {
        return sent;
    }

   /**
    * Accessor for visual
    *
    * @return returns visual
    */
    public Rectangle getVisual() {
        return visual;
    }

   /**
    * Accessor for timestamp
    *
    * @return returns timestamp
    */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }


}
