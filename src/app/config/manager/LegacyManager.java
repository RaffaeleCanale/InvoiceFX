package app.config.manager;

import app.legacy.Config;
import app.legacy.converter.InvoiceConverter;
import app.legacy.model.invoice.InvoiceModel;
import app.model.invoice.Invoice;
import com.wx.util.future.IoIterator;
import com.wx.util.representables.TypeCaster;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.06.16.
 */
public class LegacyManager implements ManagerInterface {

    private final TypeCaster<Invoice, InvoiceModel> legacyConverter = new InvoiceConverter();



    public void load() throws IOException {
        app.legacy.Config.loadManagers();
    }

    @Override
    public IoIterator<Invoice> getAllInvoices() throws IOException {
        Iterator<Invoice> it = Config.invoicesManager().get()
                .stream()
                .map(legacyConverter::castIn)
                .iterator();
        return IoIterator.from(it);
    }

    @Override
    public void addNewInvoice(Invoice invoice) throws IOException {
        InvoiceModel legacyInvoice = legacyConverter.castOut(invoice);

        Config.invoicesManager().get().add(legacyInvoice);
    }



}
