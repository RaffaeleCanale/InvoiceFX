package com.wx.invoicefx.ui.views.viewer;

import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.ui.views.Stages;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class InvoiceViewerController implements StageController{

    public GridPane outerPane;
    @FXML
    private ImageView imageView;

    private double zoom = 1.0;

    @Override
    public void setArguments(Object... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Must be exactly one argument");
        }

        if (args[0] instanceof Image) {
            imageView.setImage((Image) args[0]);
        } else {
            throw new IllegalArgumentException("Invalid argument type");
        }


    }

    @FXML
    public void initialize() {
        imageView.setOnScroll(this::onScroll);
        imageView.setPreserveRatio(true);


        RowConstraints row = new RowConstraints();
        row.setPercentHeight(100);
        row.setFillHeight(false);
        row.setValignment(VPos.CENTER);
        outerPane.getRowConstraints().add(row);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(100);
        col.setFillWidth(false);
        col.setHalignment(HPos.CENTER);
        outerPane.getColumnConstraints().add(col);
    }

    @Override
    public void setContext(Stage stage) {
//        stage.getScene().setOnScroll(this::onScroll);
    }



    private void onScroll(ScrollEvent event) {
        if (event.isControlDown()) {
            System.out.println(zoom);
            if (event.getDeltaY() > 0) {
                zoom *= 1.1;
            } else if (event.getDeltaY() < 0) {
                zoom /= 1.1;
            }

            zoom = Math.max(0.2, Math.min(3, zoom));

            imageView.setFitWidth(zoom * imageView.getImage().getWidth());
            imageView.setFitHeight(zoom * imageView.getImage().getHeight());
        }
    }

    public void onClose() {
        StageManager.close(Stages.INVOICE_VIEWER);
    }
}
