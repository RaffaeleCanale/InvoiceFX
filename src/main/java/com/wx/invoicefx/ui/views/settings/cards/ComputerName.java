package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.ui.components.settings.TextFieldPane;
import javafx.beans.NamedArg;

import static com.wx.invoicefx.config.preferences.local.LocalProperty.COMPUTER_NAME;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class ComputerName extends TextFieldPane {

    public ComputerName(@NamedArg("text") String text) {
        super(text);

        bindWith(AppResources.localPreferences().stringProperty(COMPUTER_NAME));
    }

    @Override
    protected boolean accept(String newValue) {
        return !newValue.isEmpty();
    }
}
