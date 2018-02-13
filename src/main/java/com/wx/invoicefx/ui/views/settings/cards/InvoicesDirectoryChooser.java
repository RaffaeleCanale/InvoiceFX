package com.wx.invoicefx.ui.views.settings.cards;

import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.ui.components.settings.FilePane;
import javafx.beans.NamedArg;

import static com.wx.invoicefx.config.preferences.local.LocalProperty.INVOICE_DIRECTORY;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class InvoicesDirectoryChooser extends FilePane {

    public InvoicesDirectoryChooser(@NamedArg("text") String text) {
        super(text);

        setDirectory(true);
        setOfferToTransferFiles(true);
        bindWith(AppResources.localPreferences().stringProperty(INVOICE_DIRECTORY));
    }
}
