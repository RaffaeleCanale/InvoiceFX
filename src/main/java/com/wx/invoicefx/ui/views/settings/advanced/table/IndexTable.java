package com.wx.invoicefx.ui.views.settings.advanced.table;

import com.wx.fx.Lang;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.dataset.LocalDataSet;
import com.wx.invoicefx.util.io.InvalidDataException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.06.17.
 */
public class IndexTable extends ResourcePageTable {

    public void setIndex(DataSet dataSet) {
        super.setPage(dataSet.getIndex().getPage());



        Label infoLabel = new Label("");
        Button integrityTestButton = new Button(Lang.getString("stage.settings.advanced.button.test_integrity"));
        integrityTestButton.getStyleClass().add("custom-button");

        HBox pane = new HBox(10, infoLabel, integrityTestButton);
        pane.setPadding(new Insets(10));
        pane.setAlignment(Pos.CENTER_RIGHT);

        setBottom(pane);





        integrityTestButton.setOnAction(event -> {
            try {
                dataSet.getIndex().testIntegrity();

                infoLabel.setText(Lang.getString("stage.settings.advanced.label.integrity_success"));
            } catch (InvalidDataException e) {
                infoLabel.setText(Lang.getString("stage.settings.advanced.label.integrity_failed", e.getMessage()));
            }
        });


//        if (dataSet instanceof LocalDataSet) {
//            Button createIndexButton = new Button(Lang.getString("stage.settings.advanced.button.create_index"));
//            createIndexButton.getStyleClass().add("custom-button");
//
//            createIndexButton.setOnAction(event -> {
//                try {
//                    dataSet.getIndex().clear();
//                } catch (IOException e) {
//                    infoLabel.setText(e.getMessage());
//                }
//
//                super.setItems(dataSet.getIndex().getPage().keySet());
//            });
//
//            pane.getChildren().add(createIndexButton);
//        }
    }
}
