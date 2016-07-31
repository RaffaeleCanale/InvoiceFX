package app.config.manager;

import app.model.invoice.Invoice;
import app.model.item.Item;
import com.wx.util.future.IoIterator;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public interface ManagerInterface {

    IoIterator<Invoice> getAllInvoices() throws IOException;

    void addNewInvoice(Invoice invoice) throws IOException;

    IoIterator<Item> getAllItems() throws IOException;
}
