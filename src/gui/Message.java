package footsiebot.gui;

import java.time.LocalDateTime;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import javafx.geometry.*;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.text.Font;

import footsiebot.ai.Suggestion;

public class Message extends FlowPane {
    private LocalDateTime timestamp;
    private boolean sent;
    private double width;
    private double height;
    private GUIcore ui;
    private boolean isAI;
    private Message parent;
    private boolean isSummary;

    private StackPane msgWrapper;
    private Label msg;
    private Rectangle visual;

    private StackPane btnWrapper;

    private Tooltip aiNote;
    private Tooltip removeNote;

    private Suggestion sugg;


    public Message(String text, LocalDateTime timestamp, boolean sent, Suggestion s, GUIcore ui) {
        super();
        double chatPaneWidth = ui.getStage().getScene().getWidth() * 0.6875;

        this.timestamp = timestamp;
        this.sent = sent;
        this.ui = ui;
        this.isAI = (s!= null);
        isSummary = false;
        sugg = s;
        parent = null;
        setPrefWidth(chatPaneWidth - 36);
        setMaxWidth(chatPaneWidth - 36);

        final double maxWidth = (chatPaneWidth - 36) * 0.55;
        Text sizing = new Text(text);
        if (Math.ceil(sizing.getLayoutBounds().getWidth()) > maxWidth)
            sizing.setWrappingWidth(maxWidth);

        // System.out.println(sizing.getFont());

        width = Math.ceil(sizing.getLayoutBounds().getWidth());
        height = Math.ceil(sizing.getLayoutBounds().getHeight() + 200);

        initMessage(text);

        aiNote = new Tooltip("Why am I seeing this?");
        removeNote = new Tooltip("This isn't important to me");

        if (isAI)
            setupAI();

        if (sent) {
            if (isAI)
                getChildren().add(btnWrapper);
            getChildren().add(msgWrapper);
            finishSetup("user");
            setAlignment(Pos.CENTER_RIGHT);
        } else {
            getChildren().addAll(msgWrapper);
            if (isAI)
                getChildren().add(btnWrapper);
            finishSetup("system");
            setAlignment(Pos.CENTER_LEFT);
        }
    }

    public Message(String text, LocalDateTime timestamp, boolean sent, Suggestion s, GUIcore ui, boolean isSummary) {
        super();
        double chatPaneWidth = ui.getStage().getScene().getWidth() * 0.6875;

        this.timestamp = timestamp;
        this.sent = sent;
        this.ui = ui;
        this.isAI = (s!= null);
        sugg = s;
        parent = null;
        setPrefWidth(chatPaneWidth - 36);
        setMaxWidth(chatPaneWidth - 36);

        final double maxWidth = (chatPaneWidth - 36) * 0.55;
        Text sizing = new Text(text);
        if (Math.ceil(sizing.getLayoutBounds().getWidth()) > maxWidth)
            sizing.setWrappingWidth(maxWidth);

        // System.out.println(sizing.getFont());

        width = Math.ceil(sizing.getLayoutBounds().getWidth());
        height = Math.ceil(sizing.getLayoutBounds().getHeight());

        initMessage(text);

        aiNote = new Tooltip("Why am I seeing this?");
        removeNote = new Tooltip("This isn't important to me");

        if (isAI)
            setupAI();

        if (sent) {
            if (isAI)
                getChildren().add(btnWrapper);
            getChildren().add(msgWrapper);
            finishSetup("user");
            setAlignment(Pos.CENTER_RIGHT);
        } else {
            getChildren().addAll(msgWrapper);
            if (isAI)
                getChildren().add(btnWrapper);
            finishSetup("system");
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
        isSummary = false;
        this.parent = parent;
        setPrefWidth(chatPaneWidth - 36);
        setMaxWidth(chatPaneWidth - 36);

        final double maxWidth = (chatPaneWidth - 36) * 0.55;
        Text sizing = new Text(text);
        if (Math.ceil(sizing.getLayoutBounds().getWidth()) > maxWidth)
            sizing.setWrappingWidth(maxWidth);

        width = Math.ceil(sizing.getLayoutBounds().getWidth());
        height = Math.ceil(sizing.getLayoutBounds().getHeight());
        initMessage(text);

        if (sent) {
            getChildren().addAll(msgWrapper);
            finishSetup("user");
            setAlignment(Pos.CENTER_RIGHT);
        } else {
            getChildren().addAll(msgWrapper);
            finishSetup("system");
            setAlignment(Pos.CENTER_LEFT);
        }
    }

    private void initMessage(String text) {
        msg = new Label(text);
        msg.setMaxWidth(width);
        msg.setMinHeight(height);
        msg.setWrapText(true);

        visual = new Rectangle((width + 14), (height + 8));
        msgWrapper = new StackPane();
        msgWrapper.setMinWidth(visual.getWidth());
        msgWrapper.setMaxWidth(visual.getWidth());
        msgWrapper.getChildren().addAll(visual, msg);
    }

    private void setupAI() {
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
            ui.displayMessage(sugg.getReason(), false, this);
            why.setVisible(false);
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
            if(isAI){
                ui.suggestionIrrelevant(sugg);
            }
        });
    }

    private void finishSetup(String sender) {
        msg.getStyleClass().add(sender + "-label");
        visual.getStyleClass().add(sender + "-visual");
        getStyleClass().add(sender + "-message");
        msg.setAlignment(Pos.CENTER_LEFT);
        // setMinHeight(height);
        // setMaxHeight(height);
        // System.out.println(msg.getFont());
    }

   /**
    * Resizes the visual parts of the Message
    *
    * @param stage the stage used to calculate widths
    */
    public void resize(Double chatPaneWidth) {
        setMinWidth(chatPaneWidth - 34);
        setMaxWidth(chatPaneWidth - 34);

        final double maxWidth = (chatPaneWidth - 34) * 0.55;
        Text sizing = new Text(msg.getText());
        if (Math.ceil(sizing.getLayoutBounds().getWidth()) > maxWidth)
            sizing.setWrappingWidth(maxWidth);

        width = sizing.getLayoutBounds().getWidth();
        height = sizing.getLayoutBounds().getHeight();

        msg.setMaxWidth(width);
        visual.setWidth(width + 14);
        visual.setHeight(height + 8);

        if (isAI)
            btnWrapper.setMaxHeight(visual.getHeight());

        msgWrapper.setMinWidth(visual.getWidth());
        msgWrapper.setMaxWidth(visual.getWidth());
        msgWrapper.setMinHeight(visual.getHeight() + 10);
        msgWrapper.setMaxHeight(visual.getHeight() + 10);
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

    public boolean isSummary() {
        return isSummary;
    }

}
