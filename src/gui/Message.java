package footsiebot.gui;

import footsiebot.ai.Suggestion;
import java.time.LocalDateTime;
import javafx.animation.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.text.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Message extends FlowPane {
    private boolean sent;
    private boolean isAI;
    private Message parent;
    private Suggestion sugg;

    private StackPane msgWrapper;
    private Label msg;
    private Rectangle visual;
    private StackPane btnWrapper;

    private Tooltip aiNote;
    private Tooltip removeNote;

    private double width;
    private double height;
    private GUIcore ui;

   /**
    * Constructor for a Message with no parent - could be a suggestion from the AI
    *
    * @param text the content of the Message
    * @param sent true if the Message was sent by the user, false if sent by the system
    * @param s the Suggestion object if a suggestion from the AI - can be null
    * @param ui the instance of GUIcore the Message was sent from
    */
    public Message(String text, boolean sent, Suggestion s, GUIcore ui) {
        super();
        double chatPaneWidth = ui.getStage().getScene().getWidth() * 0.6875;

        this.sent = sent;
        this.ui = ui;
        this.isAI = (s!= null);
        sugg = s;
        parent = null;
        setPrefWidth(chatPaneWidth - 36);
        setMaxWidth(chatPaneWidth - 36);

        final double maxWidth = (chatPaneWidth - 36) * 0.55;
        Text sizing = new Text(text);
        sizing.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
        if (Math.ceil(sizing.getLayoutBounds().getWidth()) > maxWidth)
            sizing.setWrappingWidth(maxWidth);

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

   /**
    * Constructor for a Message with a parent
    *
    * @param text the content of the Message
    * @param ui the instance of GUIcore the Message was sent from
    * @param parent the parent Message
    */
    public Message(String text, GUIcore ui, Message parent) {
        super();
        double chatPaneWidth = ui.getStage().getScene().getWidth() * 0.6875;

        sent = false;
        this.ui = ui;
        isAI = false;
        this.parent = parent;
        setPrefWidth(chatPaneWidth - 36);
        setMaxWidth(chatPaneWidth - 36);

        final double maxWidth = (chatPaneWidth - 36) * 0.55;
        Text sizing = new Text(text);
        sizing.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
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

   /**
    * Starts the construction of the Message
    *
    * @param text the content of the Message
    */
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

   /**
    * If the Message was sent by the AI, this method creates the required features
    */
    private void setupAI() {
        btnWrapper = new StackPane();
        Insets btnPadding = new Insets(3, 5, 2, 2);
        btnWrapper.setPadding(btnPadding);
        ImageView close = new ImageView("file:src/img/msg-close2.png");
        close.setPreserveRatio(true);
        close.setFitWidth(12);
        close.getStyleClass().add("remove-message");
        Tooltip.install(close, removeNote);

        ImageView why = new ImageView("file:src/img/reason.png");
        why.setPreserveRatio(true);
        why.setFitWidth(15);
        Tooltip.install(why, aiNote);
        why.getStyleClass().add("options-button");

        btnWrapper.getChildren().addAll(why, close);
        btnWrapper.setAlignment(close, Pos.TOP_CENTER);
        btnWrapper.setAlignment(why, Pos.BOTTOM_CENTER);

        btnWrapper.heightProperty().addListener((obs, oldVal, newVal) -> {
            double buttonsHeight = close.getLayoutBounds().getHeight() + why.getLayoutBounds().getHeight() + 10;
            double buttonsWidth = close.getLayoutBounds().getWidth() + why.getLayoutBounds().getWidth() + 10;
            if (newVal.doubleValue() < buttonsHeight) {
                btnWrapper.setMinWidth(buttonsWidth);
                btnWrapper.setMaxWidth(buttonsWidth);
                btnWrapper.setAlignment(close, Pos.CENTER_RIGHT);
                btnWrapper.setAlignment(why, Pos.CENTER_LEFT);
            } else {
                buttonsWidth = why.getLayoutBounds().getWidth();
                if (close.getLayoutBounds().getWidth() > why.getLayoutBounds().getWidth())
                    buttonsWidth = close.getLayoutBounds().getWidth();
                btnWrapper.setMinWidth(buttonsWidth + 8);
                btnWrapper.setMaxWidth(buttonsWidth + 8);
                btnWrapper.setAlignment(close, Pos.TOP_CENTER);
                btnWrapper.setAlignment(why, Pos.BOTTOM_CENTER);
            }
        });

        why.setOnMouseEntered(e -> {
            why.setImage(new Image("file:src/img/reason-hover.png"));
        });

        why.setOnMouseExited(e -> {
            why.setImage(new Image("file:src/img/reason.png"));
        });

        close.setOnMouseEntered(e -> {
            close.setImage(new Image("file:src/img/msg-close-hover2.png"));
        });

        close.setOnMouseExited(e -> {
            close.setImage(new Image("file:src/img/msg-close2.png"));
        });

        why.setOnMouseClicked(e -> {
            ui.displayMessage(sugg.getReason(), this);
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

   /**
    * Finishes the construction of the Message
    */
    private void finishSetup(String sender) {
        if (msg.getText().contains("it's time for your daily summary")) {
            msg.getStyleClass().add(sender + "-label");
            visual.getStyleClass().add("summary-visual");
        } else if (msg.getText().contains("WARNING")) {
            msg.getStyleClass().add("warning-label");
            visual.getStyleClass().add("warning-visual");
        } else if ((sugg != null) || (parent != null)) {
            msg.getStyleClass().add("suggestion-label");
            visual.getStyleClass().add("suggestion-visual");
        } else {
            msg.getStyleClass().add(sender + "-label");
            visual.getStyleClass().add(sender + "-visual");
        }
        getStyleClass().add(sender + "-message");
        msg.setAlignment(Pos.CENTER_LEFT);
    }

   /**
    * Resizes the visual parts of the Message
    *
    * @param chatPaneWidth the width of the chatPane in the instance of GUIcore
    */
    public void resize(Double chatPaneWidth) {
        setMinWidth(chatPaneWidth - 34);
        setMaxWidth(chatPaneWidth - 34);

        final double maxWidth = (chatPaneWidth - 34) * 0.55;
        Text sizing = new Text(msg.getText());
        sizing.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
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
}
