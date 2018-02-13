package com.wx.invoicefx.ui.components.settings;

import com.wx.invoicefx.ui.components.RemoveableComponent;
import com.wx.invoicefx.ui.components.ToggleSwitch;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 18.06.17.
 */
public class PropertyTogglePane extends BorderPane implements RemoveableComponent {


    private final ToggleSwitch propertySwitch = new ToggleSwitch();
    private BooleanProperty boundProperty;

    public PropertyTogglePane(@NamedArg("text") String text) {
        setCursor(Cursor.HAND);

        propertySwitch.setMouseTransparent(true);
        setOnMouseClicked(propertySwitch.getOnMouseClicked());


        final Label propertyLabel = new Label(text);
        BorderPane.setAlignment(propertyLabel, Pos.CENTER_LEFT);

        this.setLeft(propertyLabel);
        this.setRight(propertySwitch);
    }

    public BooleanProperty getSwitchProperty() {
        return propertySwitch.switchedOnProperty();
    }

    protected ToggleSwitch getSwitchComponent() {
        return propertySwitch;
    }

    public void bindWith(BooleanProperty property) {
        boundProperty = property;
        propertySwitch.switchedOnProperty().bindBidirectional(boundProperty);
    }

    @Override
    public void onRemove() {
        if (boundProperty != null) {
            propertySwitch.switchedOnProperty().unbindBidirectional(boundProperty);
        }
    }
}
