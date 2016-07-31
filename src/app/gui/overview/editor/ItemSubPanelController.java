package app.gui.overview.editor;

import app.config.Config;
import app.config.preferences.SharedProperty;
import app.model.item.Item;
import app.util.bindings.FormElement;
import app.util.gui.components.NumberTextField;
import app.util.helpers.Common;
import app.util.helpers.InvoiceHelper;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.converter.FormatStringConverter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.DoubleStream;

/**
 * Created on 07/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class ItemSubPanelController {

    @FXML
    private TextField nameField;

    @FXML
    private NumberTextField priceField;

    @FXML
    private ChoiceBox<Double> vatField;


    private EventHandler<ActionEvent> onClose;

    public void initialize() {
        vatField.setConverter(new FormatStringConverter<>(InvoiceHelper.vatFormat()));

        double[] vats = Config.sharedPreferences().get(SharedProperty.VAT, Common::decodeDoubleArray);
        vatField.getItems().setAll(
                DoubleStream.of(vats).boxed().toArray(Double[]::new)
        );
    }

    public void setOnClose(EventHandler<ActionEvent> onClose) {
        this.onClose = onClose;
    }

    public void remove() {
        if (onClose != null) {
            onClose.handle(new ActionEvent());
        }
    }


    public Set<FormElement> bind(Item item) {
        Set<FormElement> forms = new HashSet<>();

        // NAME
        nameField.textProperty().bindBidirectional(item.nameProperty());
        forms.add(FormElement.simple(item.nameValidityProperty(), nameField));

        // PRICE
        priceField.setNumberFormat(InvoiceHelper.moneyFormat());
        priceField.numberProperty().bindBidirectional(item.priceProperty());
        forms.add(FormElement.simple(item.priceValidityProperty(), priceField));

        // VAT
        DoubleProperty vatValue = DoubleProperty.doubleProperty(vatField.valueProperty());
        vatValue.bindBidirectional(item.vatProperty());
        forms.add(FormElement.simple(item.vatValidityProperty(), vatField));

        return forms;
    }


}
