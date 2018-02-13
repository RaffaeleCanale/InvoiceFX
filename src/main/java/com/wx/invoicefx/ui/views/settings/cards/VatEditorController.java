package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.entities.item.Vats;
import com.wx.invoicefx.ui.components.NumberTextField;
import com.wx.invoicefx.ui.components.settings.ExpandPane;
import com.wx.invoicefx.util.string.DoubleArrayConverter;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wx.invoicefx.config.preferences.shared.SharedProperty.VAT;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 23.06.17.
 */
public class VatEditorController {

    @FXML
    private ExpandPane expandPane;
    @FXML
    private NumberTextField vatField1;
    @FXML
    private NumberTextField vatField2;

    @FXML
    private void initialize() {
        Vats vats = AppResources.getAllVats();
        NumberTextField[] fields = {vatField1, vatField2};

        if (vats.values().size() != fields.length) {
            expandPane.setDisable(true);
            return;
        }

        ChangeListener<Number> updateListener = (observable, oldValue, newValue) -> updateVats(fields);
        NumberFormat vatFormat = InvoiceFormats.vatFormat();

        int i = 0;
        for (Vat vat : vats.values()) {
            fields[i].setNumber(vat.getValue());
            fields[i].setUserData(vat.getCategory());
            fields[i].numberProperty().addListener(updateListener);
            fields[i].setNumberFormat(vatFormat);
            fields[i].setNumberPredicate(v -> v >= 0.0 && v <= 100.0);

            i++;
        }

        updateSubLabel(vats);
    }

    private void updateVats(NumberTextField[] fields) {
        Vat[] vatsArray = new Vat[fields.length];

        for (int i = 0; i < vatsArray.length; i++) {
            double value = fields[i].getNumber().doubleValue();
            int category = (int) fields[i].getUserData();
            vatsArray[i] = new Vat(value, category);
        }

        Vats vats = new Vats(vatsArray);

        try {
            AppResources.setAllVats(vats);
        } catch (IOException e) {
            ExceptionLogger.logException(e);
        }
        updateSubLabel(vats);
    }

    private void updateSubLabel(Vats vats) {
        NumberFormat vatFormat = InvoiceFormats.vatFormat();

        String label = vats.values().stream()
                .map(v -> vatFormat.format(v.getValue()))
                .collect(Collectors.joining(" "));
        expandPane.setSubLabel(label);
    }
}
