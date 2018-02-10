package footsiebot.guicore;
import footsiebot.Core;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.scene.text.Font;
import java.time.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import java.io.*;
import javafx.animation.*;
import javafx.util.Duration;

public class GUIcore implements IGraphicalUserInterface {
    private Stage stage;
    private String style;
    private StackPane root;
    private Scene scene;
    private ScrollPane boardWrapper;
    private FlowPane messageBoard;
    private StackPane inputWrapper;
    private Rectangle inputVisual;
    private TextField input;
    private ListProperty<Node> messages;
    private Core core;
    private Timeline newDataTimeline;
    private Timeline tradingHourTimeline;

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
            inputVisual.setWidth(scene.getWidth() - 10);
            input.setMinWidth(scene.getWidth() - 20);
            input.setMaxWidth(scene.getWidth() - 20);
            //resize all messages on the board
            // for (int i = 0; i < messages.size(); i++) {
            //     if (messageBoard.getChildren().get(i) instanceof Message) {
            //         // System.out.println("Resizing msg(" + i + ")");
            //         Message tmp = (Message) messageBoard.getChildren().get(i);
            //         tmp.getLabel().setMaxWidth(stage.getWidth() * 0.55);
            //         tmp.getVisual().setHeight(tmp.getLabel().getHeight() * 1.5);
            //         tmp.getVisual().setWidth(tmp.getLabel().getWidth() + 10);
            //         tmp.setMaxWidth(stage.getWidth() - 36);
            //         tmp.setPrefWidth(stage.getWidth() - 36);
            //     }
            // }
            // resizeMessages();
            stage.setScene(scene);
        });

        //send user input
        input.setOnAction((event) -> {
            onUserInput();
            // resizeMessages();
        });

        messageBoard.heightProperty().addListener((obs, oldVal, newVal) -> {
            boardWrapper.setVvalue(1);
        });

        messages = new SimpleListProperty<Node>();

        messages.addListener((obs, oldVal, newVal) -> {
            // for (int i = 0; i < messages.size(); i++) {
            //     if (messageBoard.getChildren().get(i) instanceof Message) {
            //         Message tmp = (Message) messageBoard.getChildren().get(i);
            //         tmp.getVisual().setHeight(tmp.getLabel().getHeight() * 1.5);
            //         tmp.getVisual().setWidth(tmp.getLabel().getWidth() + 10);
            //         tmp.setMinHeight(tmp.getVisual().getHeight() + 10);
            //         tmp.setMaxHeight(tmp.getVisual().getHeight() + 10);
            //     }
            //     // stage.setScene(null);
            //     stage.setScene(stage.getScene());
            // }
            resizeMessages();
        });

        messages.setValue(messageBoard.getChildren());

        startNewDataTimeline();//Starts up the timeline for regular data updates
        startNewTradingHourTimeline();//Starts timeline for trading hour

        inputWrapper.getChildren().addAll(inputVisual, input);
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
    * Starts the newDataTimeline.
    * Simple Timeline to run the core action regularly
    * REF: http://tomasmikula.github.io/blog/2014/06/04/timers-in-javafx-and-reactfx.html
    */
    private void startNewDataTimeline(){
        newDataTimeline = new Timeline(new KeyFrame(
            Duration.millis(core.DATA_REFRESH_RATE),
            ae -> core.onNewDataAvailable()));
        newDataTimeline.setCycleCount(Animation.INDEFINITE);
        newDataTimeline.play();//Running the core function at regular times.
    }

    private void startNewTradingHourTimeline(){
        tradingHourTimeline = new Timeline(new KeyFrame(
            Duration.millis(86400000),//24 hour refresh time
            ae -> core.onTradingHour()));
        newDataTimeline.setCycleCount(Animation.INDEFINITE);
        Long timeOfDayInMillis = ((Instant.now().toEpochMilli())%86400000);
        Long targetTimeOfDay = core.TRADING_TIME;
        Long startDuration;
        if(timeOfDayInMillis > targetTimeOfDay){
            startDuration = (timeOfDayInMillis - targetTimeOfDay);
            //System.out.println("time of day is later than target.\nStart duration is "+startDuration);//DEBUG
        }
        else{
            startDuration = 86400000-(targetTimeOfDay - timeOfDayInMillis);
            //System.out.println("time of day is before target.\nStart duration is "+startDuration);//DEBUG
        }
        tradingHourTimeline.playFrom(Duration.millis(startDuration));
        System.out.println("will call onTradingHour in " + (86400000 - startDuration) + " milliseconds");//DEBUG
        //Skips forward by the current time of day + the trading hour time.
    }

    /**
    * Manages input from the user
    */
    private void onUserInput() {
        if (checkInput()) {
            messageBoard.getChildren().add(new Message(input.getText(), LocalDateTime.now(), stage, true));
            messages.setValue(messageBoard.getChildren());
            // messageBoard.getChildren().add(new Divider(stage));
            /*
            * send string to core
            */
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
        // scene.getStylesheets().setAll("file:///" + styleFile.getAbsolutePath().replace("\\", "/"));
        stage.setScene(scene);
    }

    /**
    * Displays a message from the system to the user
    *
    * @param msg a string containing the message to be displayed
    */
    public void displayMessage(String msg) {
        messageBoard.getChildren().add(new Message(msg, LocalDateTime.now(), stage, false));
        messages.setValue(messageBoard.getChildren());
    }


    public void displayResults(String data, boolean isAI) {

    }

    public void displayResults(String[] news, boolean isAI) {

    }

    /**
    * Resizes the messages displayed
    */
    private void resizeMessages() {
        System.out.println("=========================================");
        for (int i = 0; i < messages.size(); i++) {
            if (messageBoard.getChildren().get(i) instanceof Message) {
                Message tmp = (Message) messageBoard.getChildren().get(i);
                System.out.println("Resizing msg[" + i + "] with height and width: " + tmp.getLabel().getHeight() + ", " + tmp.getLabel().getWidth());
                tmp.getLabel().setMaxWidth(stage.getWidth() * 0.55);
                tmp.getVisual().setHeight(tmp.getLabel().getHeight() + 8);
                tmp.getVisual().setWidth(tmp.getLabel().getWidth() + 10);
                tmp.setMaxWidth(stage.getWidth() - 36);
                tmp.setPrefWidth(stage.getWidth() - 36);
                tmp.setMinHeight(tmp.getVisual().getHeight() + 10);
                tmp.setMaxHeight(tmp.getVisual().getHeight() + 10);
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
}
