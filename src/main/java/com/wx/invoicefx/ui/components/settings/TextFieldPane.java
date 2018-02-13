package com.wx.invoicefx.ui.components.settings;

import com.wx.fx.Lang;
import javafx.beans.NamedArg;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class TextFieldPane extends BorderPane {

    private final TextField textField;
    private StringProperty property;

    public TextFieldPane(@NamedArg("text") String text) {
        Label label = new Label(text);
        textField = new TextField();
        Button changeButton = new Button(Lang.getString("stage.settings.components.button.change"));
        VBox vBox = new VBox(10.0, label, textField);

        vBox.setPadding(new Insets(0.0, 10.0, 0.0, 0.0));
        textField.setDisable(true);
        textField.setOnAction(this::validateAndSet);
        changeButton.setPrefSize(75, 50);
        changeButton.setFocusTraversable(false);
        changeButton.setOnAction(this::handleChange);
        changeButton.getStyleClass().add("right-button");

        setCenter(vBox);
        setRight(changeButton);
    }

    public void bindWith(StringProperty property) {
        this.property = property;
        this.textField.setText(property.get());
    }

    protected boolean accept(String newValue) {
        return true;
    }

    private void handleChange(ActionEvent actionEvent) {
        if (textField.isDisable()) {
            textField.setDisable(false);
            textField.requestFocus();
        } else {
            validateAndSet(actionEvent);
        }
    }

    private void validateAndSet(ActionEvent actionEvent) {
        String newValue = textField.getText().trim();

        if (accept(newValue)) {
            property.set(newValue);
        } else {
            textField.setText(property.get());
        }

        textField.setDisable(true);
    }

}
