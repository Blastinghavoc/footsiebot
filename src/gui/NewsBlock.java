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

public class NewsBlock extends BorderPane {
    private Label headline;
    private Label digest;
    private Label url;

    public NewsBlock(Article a, double width, Core core) {
        super();
        headline = new Label(a.getHeadline());
        headline.setWrapText(true);
        headline.getStyleClass().add("headline");

        digest = new Label(a.getDigest());
        digest.getStyleClass().add("digest");
        digest.setWrapText(true);

        url = new Label(a.getUrl());
        url.getStyleClass().add("url");
        url.setUnderline(true);

        getStyleClass().add("news-block");

        Insets padding = new Insets(7);
        setPadding(padding);

        setTop(headline);
        setLeft(digest);
        setBottom(url);

        setMinHeight(100);
        setMaxHeight(100);
        setMinWidth(width);
        setMaxWidth(width);

        setOnMouseClicked(e -> {
            core.openWebpage(url.getText());
        });
    }

    public void resize(double width) {
        setMaxWidth(width);
        setMinWidth(width);
    }
}
