package footsiebot.gui;

import footsiebot.Core;
import footsiebot.datagathering.Article;
import java.time.LocalDateTime;
import javafx.application.*;
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
import javafx.scene.image.*;

public class NewsBlock extends BorderPane {
    private Label headline;
    private Label digest;
    private Label url;

   /**
    * Constructor for a new news item
    *
    * @param article the article to be presented to the user
    * @param width the width available to the news block
    * @param core the core of the application
    * @param ui the GUI of the application
    */
    public NewsBlock(Article article, double width, Core core, GUIcore ui) {
        super();
        headline = new Label(article.getHeadline());
        headline.setWrapText(true);
        headline.getStyleClass().add("headline");

        digest = new Label(article.getDigest());
        digest.getStyleClass().add("digest");
        digest.setWrapText(true);

        url = new Label(article.getUrl());
        url.getStyleClass().add("url");
        url.setUnderline(true);

        getStyleClass().add("news-block");

        Insets padding = new Insets(5, 7, 7, 7);
        setPadding(padding);

        ImageView close = new ImageView("file:src/img/close.png");
        close.getStyleClass().add("remove-news");
        close.setPreserveRatio(true);
        close.setFitWidth(10);

        close.setOnMouseEntered(e -> {
            close.setImage(new Image("file:src/img/close-hover.png"));
        });

        close.setOnMouseExited(e -> {
            close.setImage(new Image("file:src/img/close.png"));
        });

        close.setOnMouseClicked(e -> {
            ui.getNewsBoard().getChildren().removeAll(this);
        });

        StackPane topPane = new StackPane();
        topPane.getChildren().addAll(headline, close);
        topPane.setAlignment(headline, Pos.TOP_LEFT);
        Insets closeMargins = new Insets(2, 0, 0, 0);
        topPane.setMargin(close, closeMargins);
        topPane.setAlignment(close, Pos.TOP_RIGHT);

        setTop(topPane);
        setLeft(digest);
        setBottom(url);

        setMinHeight(100);
        setMaxHeight(100);
        setMinWidth(width);
        setMaxWidth(width);

        url.setOnMouseClicked(e -> {
            core.openWebpage(url.getText());
        });
    }

   /**
    * Resizes the news blocks to the width provided
    *
    * @param width the new width of the news blocks
    */
    public void resize(double width) {
        setMaxWidth(width);
        setMinWidth(width);
    }
}
