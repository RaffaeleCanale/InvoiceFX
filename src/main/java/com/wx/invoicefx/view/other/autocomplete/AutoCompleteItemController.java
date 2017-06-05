package com.wx.invoicefx.view.other.autocomplete;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 13.05.17.
 */
public class AutoCompleteItemController {


    @FXML
    private Label closeLabel;
    @FXML
    private Label itemNameLabel;
    @FXML
    HBox contentPane;

    private Runnable onRemove;

    public void onRemove(Runnable onRemove) {
        this.onRemove = onRemove;
    }

    public void setItemName(String value) {
        itemNameLabel.setText(value);

        double textWidth = computeWidth(value);
        textWidth = Math.min(textWidth, 400);

        contentPane.setPrefWidth(textWidth + 30);
    }

    private double computeWidth(String value) {
        final Text text = new Text(value);
        new Scene(new Group(text));

        // java 7 =>
        //    text.snapshot(null, null);
        // java 8 =>
        text.applyCss();

        return text.getLayoutBounds().getWidth();
    }

    public void removeItem() {
        if (onRemove != null) {
            onRemove.run();
        }
    }

    public void onMouseExited() {
        closeLabel.setEffect(null);
    }

    public void onMouseEntered() {
        closeLabel.setEffect(new DropShadow());
    }
}
