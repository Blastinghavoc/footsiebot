package footsiebot.gui;

import footsiebot.Core;
import footsiebot.datagathering.Article;
import java.io.*;
import java.time.*;
import javafx.stage.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.TextAlignment;
import javafx.geometry.*;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.util.Duration;
import javafx.application.Platform;

public class GUIcore implements IGraphicalUserInterface {
    private String style;

    private Timeline newDataTimeline;
    private Timeline tradingHourTimeline;

    private Thread dataDownload;
    private volatile Boolean closing = false;

    private ListProperty<Node> messages;
    private ListProperty<Node> news;

    private Core core;
    private Stage stage;
    private Scene scene;
    private StackPane root;
    private StackPane chatPane;
    private StackPane sidePane;
    private StackPane topBar;
    private ScrollPane boardWrapper;
    private FlowPane messageBoard;
    private StackPane inputWrapper;
    private Rectangle inputVisual;
    private TextField input;
    private Button btnSend;
    private StackPane settingsPane;
    private StackPane newsPane;
    private ScrollPane newsWrapper;
    private FlowPane newsBoard;
    private ImageView settingsIcon;
    private FadeTransition settingsPaneTrans;
    private FadeTransition newsPaneTrans;
    private RotateTransition settingsIconTrans;
    private Label noNews;

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

        scene = new Scene(root, 800, 600);
        scene.getStylesheets().setAll("file:src/gui/css/" + style + ".css");

        messages = new SimpleListProperty<Node>();
        news = new SimpleListProperty<Node>();

        initChat();
        initSide();
        initTop();
        setupListeners();
        setupActions();
        startDataDownload();
        startNewDataTimeline();
        startTradingHourTimeline();

        root.getChildren().addAll(chatPane, sidePane, topBar);
        root.setAlignment(topBar, Pos.TOP_LEFT);
        root.setAlignment(chatPane, Pos.BOTTOM_LEFT);
        root.setAlignment(sidePane, Pos.BOTTOM_RIGHT);

        stage.setTitle("Footsiebot");
        stage.setScene(scene);
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
        newsWrapper.setFitToWidth(true);

        newsBoard = new FlowPane();
        newsBoard.setId("news-board");
        newsBoard.setVgap(1);

        Button btnStyle = new Button("Update style");
        btnStyle.setOnAction(e -> {
            updateStyle();
        });

        noNews = new Label("Oh no! It looks like we don't\nhave any news for you right now!");
        noNews.setId("no-news");
        noNews.setWrapText(true);
        noNews.setTextAlignment(TextAlignment.CENTER);

        Insets wrapperPadding = new Insets(0, 0, 0, -1);
        newsWrapper.setPadding(wrapperPadding);
        newsWrapper.setContent(newsBoard);

        settingsPane.getChildren().add(btnStyle);
        settingsPane.setAlignment(btnStyle, Pos.BOTTOM_CENTER);
        newsPane.getChildren().addAll(newsWrapper, noNews);
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

        Label name = new Label("Footsiebot");
        name.setId("name-label");

        settingsIcon = new ImageView("file:src/img/settings.png");
        settingsIcon.setPreserveRatio(true);
        settingsIcon.setFitWidth(27);
        settingsIcon.setId("settings-icon");
        topBar.getChildren().addAll(settingsIcon, name);
        topBar.setAlignment(settingsIcon, Pos.CENTER_RIGHT);
        topBar.setAlignment(name, Pos.BOTTOM_CENTER);
        Insets settingsIconMargin = new Insets(0, 10, 0, 0);
        topBar.setMargin(settingsIcon, settingsIconMargin);
        settingsIconTrans = new RotateTransition(Duration.millis(300), settingsIcon);
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
            if (newsBoard.getHeight() > newsWrapper.getHeight()) {
                newsBoard.setMinWidth(sidePane.getWidth() - 15);
                newsBoard.setMaxWidth(sidePane.getWidth() - 15);
            } else {
                newsBoard.setMinWidth(sidePane.getWidth());
                newsBoard.setMaxWidth(sidePane.getWidth());
            }
            resizeNews(newsBoard.getWidth());
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
            newsWrapper.setMinWidth(sidePane.getWidth());
            newsWrapper.setMaxWidth(sidePane.getWidth());
            if (newsBoard.getHeight() > newsWrapper.getHeight()) {
                newsBoard.setMinWidth(sidePane.getWidth() - 15);
                newsBoard.setMaxWidth(sidePane.getWidth() - 15);
            } else {
                newsBoard.setMinWidth(sidePane.getWidth());
                newsBoard.setMaxWidth(sidePane.getWidth());
            }

            resizeMessages();
            resizeNews(newsBoard.getWidth());
            stage.setScene(scene);
            messageBoard.applyCss();
            messageBoard.layout();
        });

        messageBoard.heightProperty().addListener((obs, oldVal, newVal) -> {
            boardWrapper.setVvalue(1);
        });

        messages.addListener((obs, oldVal, newVal) -> {
            resizeMessages();
        });

        news.addListener((obs, oldVal, newVal) -> {
            if (news.size() == 0)
                noNews.setVisible(true);
            else
                noNews.setVisible(false);
            resizeNews(sidePane.getWidth());
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
            settingsIconTrans.setFromAngle(0);
            settingsIconTrans.setToAngle(45);
            settingsIconTrans.play();
        });

        settingsIcon.setOnMouseExited(e -> {
            settingsIconTrans.stop();
            settingsIconTrans.setFromAngle(45);
            settingsIconTrans.setToAngle(0);
            settingsIconTrans.play();
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
    * Creation of a background thread to scrape the LSE website
    * regularly, so that data is available when needed.
    */
    private void startDataDownload(){
        dataDownload = new Thread(() -> {
            try {
                while(!closing){
                    core.downloadNewData();
                    Thread.sleep(core.DOWNLOAD_RATE);
                }
            }
            catch (InterruptedException e){
                closing = true;
            }
            catch (Exception e) {
                // should not be able to get here...
                System.out.println("Error in thread");
                e.printStackTrace();
            }
        });
        dataDownload.start();
    }

   /**
    * Stops the data gathering background thread when the application is closing
    */
    public void stopDataDownload(){
        closing = true;
        try {
            dataDownload.interrupt();
            dataDownload.join();
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("Stopped download thread");
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
        newDataTimeline.playFrom(Duration.millis(core.DATA_REFRESH_RATE - core.DOWNLOAD_RATE)); //Running the core function at regular times, but starting soon after program startup
    }

   /**
    * Starts the tradingHourTimeline to run the core action regularly
    */
    private void startTradingHourTimeline() {
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
    * Displays a child message from the system to the user
    *
    * @param msg a string containing the message to be displayed
    * @param isAI a boolean representing whether the message was sent by the AI
    * @param parent the parent message
    */
    public void displayMessage(String msg, boolean isAI, Message parent) {
        if (msg != null) {
            if (parent != null)
                messageBoard.getChildren().add(new Message(msg, LocalDateTime.now(), this, parent));
            else
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
            for (Article a : news) {
                if (a != null)
                    newsBoard.getChildren().add(new NewsBlock(a, (sidePane.getWidth() - 15), core, this));
            }
            this.news.setValue(newsBoard.getChildren());
        }

    }

   /**
    * Resizes the messages displayed
    */
    private void resizeMessages() {
        for (int i = 0; i < messages.size(); i++) {
            if (messageBoard.getChildren().get(i) instanceof Message) {
                Message tmp = (Message) messageBoard.getChildren().get(i);
                System.out.println("Message: " + tmp.getText());
                System.out.println("Initial visual: " + tmp.getVisual().getHeight());
                System.out.println("Initial label: " + tmp.getLabel().getHeight());
                tmp.resize(stage);
                System.out.println("Updated visual: " + tmp.getVisual().getHeight());
                System.out.println("Updated label: " + tmp.getLabel().getHeight());
                System.out.println("=======================================");
            }
        }
        System.out.println("//////////////////////////////");
    }

   /**
    * Resizes news blocks in the news pane
    */
    private void resizeNews(double width) {
        for (int i = 0; i < news.size(); i++) {
            if (newsBoard.getChildren().get(i) instanceof NewsBlock) {
                NewsBlock tmp = (NewsBlock) newsBoard.getChildren().get(i);
                tmp.resize(width);
            }
        }
    }

   /**
    * Verifies the input
    *
    * @return true if the input is valid, false if not
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
    * Accessor for the news board
    *
    * @return the news board of the GUI
    */
    public FlowPane getNewsBoard() {
        return newsBoard;
    }

   /**
    * Accessor for stage
    *
    * @return the stage used by the GUI
    */
    public Stage getStage() {
        return stage;
    }
}
