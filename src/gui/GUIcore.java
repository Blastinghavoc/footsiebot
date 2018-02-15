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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GUIcore implements IGraphicalUserInterface {
    private Core core;
    private Timeline newDataTimeline;
    private Timeline tradingHourTimeline;
    private Stage stage;
    private String style;
    private StackPane root;
    private StackPane chatPane;
    private StackPane sidePane;
    private StackPane topBar;
    private Scene scene;
    private ScrollPane boardWrapper;
    private FlowPane messageBoard;
    private StackPane inputWrapper;
    private Rectangle inputVisual;
    private TextField input;
    private Button btnSend;
    private ListProperty<Node> messages;
    private StackPane settingsPane;
    private StackPane newsPane;
    private FlowPane newsBoard;
    private ScrollPane newsWrapper;
    private FadeTransition settingsPaneTrans;
    private FadeTransition newsPaneTrans;
    private RotateTransition settingsTrans;
    private ImageView settingsIcon;


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
        stage.setMinWidth(350);
        stage.setMinHeight(250);

        root = new StackPane();
        root.setId("root");

        scene = new Scene(root, 800, 700);
        scene.getStylesheets().setAll("file:src/gui/css/" + style + ".css");

        messages = new SimpleListProperty<Node>();

        initChat();
        initSide();
        initTop();
        setupListeners();
        setupActions();
        startNewDataTimeline(); //Starts up the timeline for regular data updates
        startNewTradingHourTimeline(); //Starts timeline for trading hour

        root.getChildren().addAll(chatPane, sidePane, topBar);
        root.setAlignment(topBar, Pos.TOP_LEFT);
        root.setAlignment(chatPane, Pos.BOTTOM_LEFT);
        root.setAlignment(sidePane, Pos.BOTTOM_RIGHT);

        stage.setTitle("Footsiebot");
        stage.setScene(scene);
        stage.getIcons().add(new Image("file:src/img/home-icon.png"));
        stage.hide();
        stage.show();
    }

   /**
    * Initialises the chat
    */
    private void initChat() {
        chatPane = new StackPane();
        chatPane.setId("chat-pane");
        chatPane.setMinWidth(scene.getWidth() * 0.6875);
        chatPane.setMaxWidth(scene.getWidth() * 0.6875);

        boardWrapper = new ScrollPane();
        boardWrapper.setId("board-wrapper");
        boardWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Insets boardWrapperPadding = new Insets(7, 0, 0, 0);
        boardWrapper.setPadding(boardWrapperPadding);

        messageBoard = new FlowPane();
        Insets boardPadding = new Insets(0, 0, 0, 16);
        messageBoard.setPadding(boardPadding);
        messageBoard.setId("message-board");

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

        inputWrapper.getChildren().addAll(inputVisual, input, btnSend);
        inputWrapper.setAlignment(Pos.CENTER_LEFT);
        inputWrapper.setMargin(input, inputMargin);

        inputWrapper.setAlignment(btnSend, Pos.CENTER_RIGHT);
        boardWrapper.setContent(messageBoard);
        chatPane.getChildren().addAll(inputWrapper, boardWrapper);
        chatPane.setAlignment(inputWrapper, Pos.BOTTOM_LEFT);
        chatPane.setAlignment(boardWrapper, Pos.TOP_LEFT);
        Insets boardWrapperMargins = new Insets(0, 0, 45, 0);
        chatPane.setMargin(boardWrapper, boardWrapperMargins);
    }

   /**
    * Initialises the sidepane
    */
    private void initSide() {
        sidePane = new StackPane();
        sidePane.setId("side-pane");
        sidePane.setMinWidth(scene.getWidth() * 0.3125);
        sidePane.setMaxWidth(scene.getWidth() * 0.3125);
        sidePane.setMinHeight(scene.getHeight() - 45);
        sidePane.setMaxHeight(scene.getHeight() - 45);

        settingsPane = new StackPane();
        settingsPane.setId("settings-pane");
        settingsPane.setVisible(false);
        Insets settingsPadding = new Insets(10, 10, 10, 10);
        settingsPane.setPadding(settingsPadding);
        settingsPaneTrans = new FadeTransition(Duration.millis(500), settingsPane);

        newsPane = new StackPane();
        newsPane.setId("news-pane");
        newsPane.setVisible(true);
        newsPaneTrans = new FadeTransition(Duration.millis(500), newsPane);

        newsWrapper = new ScrollPane();
        newsWrapper.setId("news-wrapper");

        newsBoard = new FlowPane(Orientation.VERTICAL);
        newsBoard.setId("news-board");

        Button hello = new Button("Say hello!");
        hello.setOnAction(e -> {
            System.out.println("Hello world!");
        });

        Button btnStyle = new Button("Update style");
        btnStyle.setOnAction(e -> {
            updateStyle();
        });

        Insets wrapperPadding = new Insets(0, 0, 0, -1);
        newsWrapper.setPadding(wrapperPadding);
        newsWrapper.setContent(newsBoard);
        settingsPane.getChildren().add(btnStyle);
        settingsPane.setAlignment(btnStyle, Pos.BOTTOM_CENTER);
        newsPane.getChildren().add(newsWrapper);

        sidePane.getChildren().addAll(settingsPane, newsPane);
    }

   /**
    * Initialises the topbar
    */
    private void initTop() {
        topBar = new StackPane();
        topBar.setId("top-bar");
        topBar.setMinWidth(scene.getWidth());
        topBar.setMaxWidth(scene.getWidth());
        topBar.setMinHeight(45);
        topBar.setMaxHeight(45);

        settingsIcon = new ImageView("file:src/img/settings.png");
        settingsIcon.setPreserveRatio(true);
        settingsIcon.setFitWidth(27);
        settingsIcon.setId("settings-icon");
        topBar.getChildren().add(settingsIcon);
        topBar.setAlignment(settingsIcon, Pos.CENTER_RIGHT);
        Insets settingsIconMargin = new Insets(0, 10, 0, 0);
        topBar.setMargin(settingsIcon, settingsIconMargin);
        settingsTrans = new RotateTransition(Duration.millis(300), settingsIcon);
    }

   /**
    * Sets the listeners for Nodes in the Stage
    */
    private void setupListeners() {
        //resize nodes to conform to layout
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            chatPane.setMaxHeight(scene.getHeight() - 45);
            chatPane.setMinHeight(scene.getHeight() - 45);
            sidePane.setMaxHeight(scene.getHeight() - 45);
            sidePane.setMinHeight(scene.getHeight() - 45);
            stage.setScene(scene);
        });

        //resizes nodes to conform to layout
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            chatPane.setMinWidth(scene.getWidth() * 0.6875);
            chatPane.setMaxWidth(scene.getWidth() * 0.6875);
            sidePane.setMinWidth(scene.getWidth() * 0.3125);
            sidePane.setMaxWidth(scene.getWidth() * 0.3125);
            topBar.setMinWidth(scene.getWidth());
            topBar.setMaxWidth(scene.getWidth());
            inputWrapper.setMaxWidth(chatPane.getWidth());
            inputWrapper.setMinWidth(chatPane.getWidth());
            inputVisual.setWidth(chatPane.getWidth() - 60);
            input.setMinWidth(chatPane.getWidth() - 65);
            input.setMaxWidth(chatPane.getWidth() - 65);
            boardWrapper.setMaxWidth(chatPane.getWidth());
            boardWrapper.setMinWidth(chatPane.getWidth());
            messageBoard.setMaxWidth(chatPane.getWidth() - 36);
            messageBoard.setMinWidth(chatPane.getWidth() - 36);
            newsPane.setMinWidth(sidePane.getWidth());
            newsPane.setMaxWidth(sidePane.getWidth());
            newsBoard.setMinWidth(sidePane.getWidth());
            newsBoard.setMaxWidth(sidePane.getWidth());
            newsWrapper.setMinWidth(sidePane.getWidth());
            newsWrapper.setMaxWidth(sidePane.getWidth());
            resizeMessages();
            resizeNews(sidePane.getWidth());
            stage.setScene(scene);
        });

        messageBoard.heightProperty().addListener((obs, oldVal, newVal) -> {
            boardWrapper.setVvalue(1);
        });

        messages.addListener((obs, oldVal, newVal) -> {
            resizeMessages();
        });
    }

   /**
    * Sets the actions performed by Nodes in the Stage
    */
    private void setupActions() {
        //send user input
        input.setOnAction(e -> {
            onUserInput();
        });

        //send user input
        btnSend.setOnAction(e -> {
            onUserInput();
        });

        settingsIcon.setOnMouseEntered(e -> {
            settingsTrans.setFromAngle(0);
            settingsTrans.setToAngle(45);
            settingsTrans.play();
        });

        settingsIcon.setOnMouseExited(e -> {
            settingsTrans.stop();
            settingsTrans.setFromAngle(45);
            settingsTrans.setToAngle(0);
            settingsTrans.play();
        });

        settingsIcon.setOnMouseClicked(e -> {
            if (settingsPane.visibleProperty().getValue() == Boolean.FALSE) {
                newsPaneTrans.setFromValue(1);
                newsPaneTrans.setToValue(0);
                newsPaneTrans.setOnFinished(event -> {
                    newsPane.setVisible(false);
                });

                settingsPane.setVisible(true);
                settingsPaneTrans.setFromValue(0);
                settingsPaneTrans.setToValue(1);
                settingsPaneTrans.setOnFinished(event -> {
                    settingsPane.setVisible(true);
                });
                newsPane.setDisable(true);
                newsPaneTrans.play();
                settingsPaneTrans.play();
                settingsPane.setDisable(false);
            } else {
                newsPane.setVisible(true);
                newsPaneTrans.setFromValue(0);
                newsPaneTrans.setToValue(1);
                newsPaneTrans.setOnFinished(event -> {
                    newsPane.setVisible(true);
                });

                settingsPane.setVisible(true);
                settingsPaneTrans.setFromValue(1);
                settingsPaneTrans.setToValue(0);
                settingsPaneTrans.setOnFinished(event -> {
                    settingsPane.setVisible(false);
                });

                settingsPane.setDisable(true);
                newsPaneTrans.play();
                settingsPaneTrans.play();
                newsPane.setDisable(false);
            }
        });
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
            messageBoard.getChildren().add(new Message(input.getText().trim(), LocalDateTime.now(), true, false, this));
            core.onUserInput(input.getText().trim());
            messages.setValue(messageBoard.getChildren());
            input.clear();
        }
    }

   /**
    * Updates the current styling
    */
    private void updateStyle() {
        scene.getStylesheets().setAll("file:src/gui/css/" + style + ".css");
    }

   /**
    * Sets the css used for the application
    *
    * @param style the name of the css file to be used
    */
    public void setStyle(String style) {
        this.style = style;
        scene.getStylesheets().setAll("file:src/gui/css/" + style + ".css");
        stage.setScene(scene);
    }

   /**
    * Displays a message from the system to the user
    *
    * @param msg a string containing the message to be displayed
    * @param isAI a boolean representing whether the message was sent by the AI
    */
    public void displayMessage(String msg, boolean isAI) {
        if (msg != null) {
            messageBoard.getChildren().add(new Message(msg, LocalDateTime.now(), false, isAI, this));
            messages.setValue(messageBoard.getChildren());
        }
    }

   /**
    * Displays news results
    *
    * @param news the array of Articles to displayed
    * @param isAI a boolean representing whether the message was sent by the AI
    */
    public void displayResults(Article[] news, boolean isAI) {
        if (news != null) {
            for (Article a: news) {
                newsBoard.getChildren().add(new NewsBlock(a, sidePane.getWidth(), core));
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

    private void resizeNews(double width) {
        for (int i = 0; i < newsBoard.getChildren().size(); i++) {
            if (newsBoard.getChildren().get(i) instanceof NewsBlock) {
                NewsBlock tmp = (NewsBlock) newsBoard.getChildren().get(i);
                tmp.resize(width);
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
    * Accessor for the message board
    *
    * @return the message board of the GUI
    */
    public FlowPane getMessageBoard() {
        return messageBoard;
    }

   /**
    * Accessor for stage
    */
    public Stage getStage() {
        return stage;
    }
}
