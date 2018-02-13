package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.ui.components.settings.PropertyTogglePane;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;

import static com.wx.invoicefx.config.preferences.shared.SharedProperty.AUTO_UPDATE_CURRENCY;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class AutoUpdateCurrencyToggle extends PropertyTogglePane {

    public AutoUpdateCurrencyToggle(@NamedArg("text") String text) {
        super(text);

        BooleanProperty property = AppResources.sharedPreferences().booleanProperty(AUTO_UPDATE_CURRENCY);
        bindWith(property);
        getSwitchProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                AppResources.updateCurrencyRate();
            }
        });
    }
}
