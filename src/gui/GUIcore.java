package footsiebot.gui;

import footsiebot.Core;
import footsiebot.datagathering.Article;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.geometry.*;
import java.time.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import java.io.*;
import javafx.animation.*;
import javafx.util.Duration;

public class GUIcore implements IGraphicalUserInterface {
    private Core core;
    private Timeline newDataTimeline;
    private Timeline tradingHourTimeline;
    private Stage stage;
    private String style;
    private StackPane root;
    private Scene scene;
    private ScrollPane boardWrapper;
    private FlowPane messageBoard;
    private StackPane inputWrapper;
    private Rectangle inputVisual;
    private TextField input;
    private Button btnSend;
    private ListProperty<Node> messages;

   /**
    * Constructor for the user interface using default styling
    *
    * @param primaryStage the initial stage of the application
    */
    public GUIcore(Stage primaryStage, Core core) {
        stage = primaryStage;
        style = "main";
        this.core = core;
        setup();
    }

   /**
    * Constructor for the user interface using custom styling
    *
    * @param primaryStage the initial stage of the application
    * @param style the name of the css file used for styling
    */
    public GUIcore(Stage primaryStage, String style, Core core) {
        stage = primaryStage;
        this.style = style;
        this.core = core;
        setup();
    }

   /**
    * Builds the user interface on the initial stage of the application
    */
    private void setup() {
        // System.out.println(LocalDateTime.now());
        // System.out.println(Instant.now());
        stage.setMinWidth(250);
        stage.setMinHeight(200);

        root = new StackPane();
        root.setId("root");

        scene = new Scene(root, 550, 700);
        String styleFilePath = "src/gui/css/" + style + ".css";
        File styleFile = new File(styleFilePath);
        scene.getStylesheets().add("file:///" + styleFile.getAbsolutePath().replace("\\", "/"));

        boardWrapper = new ScrollPane();
        boardWrapper.setId("board-wrapper");
        boardWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        messageBoard = new FlowPane();
        Insets boardPadding = new Insets(0, 0, 0, 16);
        messageBoard.setPadding(boardPadding);
        messageBoard.setId("message-board");
        // messageBoard.setVgap(3);

        inputWrapper = new StackPane();
        Insets inputPadding = new Insets(0, 5, 0, 5);
        Insets inputMargin = new Insets(0, 0, 0, 5);
        inputWrapper.setPadding(inputPadding);
        inputWrapper.setId("input-wrapper");
        inputWrapper.setMaxHeight(45);
        inputWrapper.setMinHeight(45);

        inputVisual = new Rectangle();
        inputVisual.setHeight(35);
        inputVisual.setId("input-visual");

        input = new TextField();
        input.setId("input");
        input.setMinHeight(25);
        input.setMaxHeight(25);
        input.setPromptText("Type something here...");

        btnSend = new Button("Send");
        btnSend.setId("send-button");

        //resize nodes to conform to layout
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            boardWrapper.setMaxHeight(scene.getHeight() - 45);
            boardWrapper.setMinHeight(scene.getHeight() - 45);
            stage.setScene(scene);
        });

        //resizes nodes to conform to layout
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            inputWrapper.setMaxWidth(scene.getWidth());
            inputWrapper.setMinWidth(scene.getWidth());
            inputVisual.setWidth(scene.getWidth() - 60);
            input.setMinWidth(scene.getWidth() - 65);
            input.setMaxWidth(scene.getWidth() - 65);
            boardWrapper.setMaxWidth(scene.getWidth());
            boardWrapper.setMinWidth(scene.getWidth());
            messageBoard.setMaxWidth(scene.getWidth());
            messageBoard.setMinWidth(scene.getWidth());
            resizeMessages();
            stage.setScene(scene);
        });

        //send user input
        input.setOnAction(e -> {
            onUserInput();
        });

        //send user input
        btnSend.setOnAction(e -> {
            onUserInput();
        });

        messageBoard.heightProperty().addListener((obs, oldVal, newVal) -> {
            boardWrapper.setVvalue(1);
        });

        messages = new SimpleListProperty<Node>();

        messages.addListener((obs, oldVal, newVal) -> {
            resizeMessages();
        });

        startNewDataTimeline(); //Starts up the timeline for regular data updates
        startNewTradingHourTimeline(); //Starts timeline for trading hour

        inputWrapper.getChildren().addAll(inputVisual, input, btnSend);
        inputWrapper.setAlignment(Pos.CENTER_LEFT);
        inputWrapper.setMargin(input, inputMargin);

        inputWrapper.setAlignment(btnSend, Pos.CENTER_RIGHT);
        boardWrapper.setContent(messageBoard);
        root.getChildren().addAll(inputWrapper, boardWrapper);
        root.setAlignment(inputWrapper, Pos.BOTTOM_LEFT);
        root.setAlignment(boardWrapper, Pos.TOP_LEFT);

        stage.setTitle("Footsiebot");
        stage.setScene(scene);
        stage.hide();
        stage.show();
    }

   /**
    * Starts the newDataTimeline to run the core action regularly
    * REF: http://tomasmikula.github.io/blog/2014/06/04/timers-in-javafx-and-reactfx.html
    */
    private void startNewDataTimeline() {
        newDataTimeline = new Timeline(new KeyFrame(
            Duration.millis(core.DATA_REFRESH_RATE),
            ae -> core.onNewDataAvailable()));
        newDataTimeline.setCycleCount(Animation.INDEFINITE);
        newDataTimeline.play(); //Running the core function at regular times.
    }

   /**
    * Starts the tradingHourTimeline to run the core action regularly
    */
    private void startNewTradingHourTimeline() {
        tradingHourTimeline = new Timeline(new KeyFrame(
            Duration.millis(86400000),//24 hour refresh time
            ae -> core.onTradingHour()));

        newDataTimeline.setCycleCount(Animation.INDEFINITE);
        Long timeOfDayInMillis = ((Instant.now().toEpochMilli())%86400000);
        Long targetTimeOfDay = core.TRADING_TIME;
        Long startDuration;

        if (timeOfDayInMillis > targetTimeOfDay) {
            startDuration = (timeOfDayInMillis - targetTimeOfDay);
            //System.out.println("time of day is later than target.\nStart duration is "+startDuration); //DEBUG
        } else {
            startDuration = 86400000-(targetTimeOfDay - timeOfDayInMillis);
            //System.out.println("time of day is before target.\nStart duration is "+startDuration); //DEBUG
        }

        tradingHourTimeline.playFrom(Duration.millis(startDuration));
        // System.out.println("will call onTradingHour in " + (86400000 - startDuration) + " milliseconds"); //DEBUG
        //Skips forward by the current time of day + the trading hour time.
    }

   /**
    * Manages input from the user
    */
    private void onUserInput() {
        if (checkInput()) {
            messageBoard.getChildren().add(new Message(input.getText().trim(), LocalDateTime.now(), stage, true, false, this));
            core.onUserInput(input.getText().trim());
            messages.setValue(messageBoard.getChildren());
            input.clear();
        }
    }

   /**
    * Sets the css used for the application
    *
    * @param style the name of the css file to be used
    */
    public void setStyle(String style) {
        this.style = style;
        String styleFilePath = "src/gui/css/" + style + ".css";
        File styleFile = new File(styleFilePath);
        scene.getStylesheets().setAll("file:///" + styleFile.getAbsolutePath().replace("\\", "/"));
        stage.setScene(scene);
    }

   /**
    * Displays a message from the system to the user
    *
    * @param msg a string containing the message to be displayed
    * @param isAI a boolean representing whether the message was sent by the AI
    */
    public void displayMessage(String msg, boolean isAI) {
        if(msg != null){
            messageBoard.getChildren().add(new Message(msg, LocalDateTime.now(), stage, false, isAI, this));
            messages.setValue(messageBoard.getChildren());
        }
    }


    public void displayResults(String data, boolean isAI) {

    }

   /**
    * Displays news results
    *
    * @param news the array of Articles to displayed
    * @param isAI a boolean representing whether the message was sent by the AI
    */
    public void displayResults(Article[] news, boolean isAI) {
        if(news != null){
            for (Article a: news) {
                String msg = a.getHeadline() + "\n" + a.getDigest() + "\n" + a.getUrl();
                displayMessage(msg, isAI);
            }
        }

    }

   /**
    * Resizes the messages displayed
    */
    private void resizeMessages() {
        for (int i = 0; i < messages.size(); i++) {
            if (messageBoard.getChildren().get(i) instanceof Message) {
                Message tmp = (Message) messageBoard.getChildren().get(i);
                tmp.resize(stage);
            }
        }
    }

   /**
    * Verifies the input
    */
    private boolean checkInput() {
        String in = input.getText();
        boolean flag = false;
        for (int i = 0; i < in.length(); i++) {
            if (in.charAt(i) != ' ')
                return true;
        }
        input.clear();
        return false;
    }

   /**
    * Getter for the message board
    *
    * @return the message board of the GUI
    */
    public FlowPane getMessageBoard() {
        return messageBoard;
    }
}
