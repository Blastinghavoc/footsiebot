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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

public class SummaryMessage extends VBox {
    private Label pre;
    // private TableView table;
    private Label post;

    public SummaryMessage(String pre, String[] data, String post) {
        // super();
        // this.pre = new Label(pre);
        // table = new TableView();
        // TableColumn code = new TableColumn("Code");
        // TableColumn spot = new TableColumn("Spot");
        // TableColumn abs = new TableColumn("Abs");
        // TableColumn perc = new TableColumn("Perc");
        // table.getColumns().addAll(code, spot, abs, perc);
        // this.post = new Label(post);
        //
        // // final ObservableList<String> dat = FXCollections.observableArrayList();
        // // for (int i = 0; i < data.length; i++) {
        // //     dat.add(data[i]);
        // // }
        // //
        // // table.setItems(dat);
        //
        // getChildren().addAll(this.pre, table, this.post);
    }
}
