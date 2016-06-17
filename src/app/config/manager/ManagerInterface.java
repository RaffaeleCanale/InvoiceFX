package app.config.manager;

import app.model.invoice.Invoice;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public interface ManagerInterface {

    Stream<Invoice> getAllInvoices();

    void addNewInvoice(Invoice invoice) throws IOException;

}
