package app.legacy.converter;

import app.legacy.model.invoice.InvoiceModel;
import app.legacy.model.item.ClientItem;
import app.model.DateEnabled;
import app.model.client.Client;
import app.model.client.PurchasedItem;
import app.model.invoice.Invoice;
import app.model.item.Item;
import com.wx.util.representables.TypeCaster;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 25.06.16.
 */
public class InvoiceConverter implements TypeCaster<Invoice, app.legacy.model.invoice.InvoiceModel> {

    private final TypeCaster<DateEnabled, app.legacy.model.DateEnabled> dateEnabledConverter;
    private final TypeCaster<Client, String> clientConverter;
    private final TypeCaster<Item, app.legacy.model.item.ItemModel> itemConverter;

    public InvoiceConverter() {
        dateEnabledConverter = new DateEnabledConverter();
        clientConverter = new ClientConverter();
        itemConverter = new ItemConverter(dateEnabledConverter);
    }

    @Override
    public Invoice castIn(InvoiceModel legacyInvoice) {
        Invoice invoice = new Invoice();
        invoice.setId(legacyInvoice.getId());
        invoice.setDate(legacyInvoice.getDate());
        invoice.setAddress(legacyInvoice.getAddress());
        invoice.setPdfFileName(legacyInvoice.getPdfFileName());

        Client previousClient = clientConverter.castIn(null);

        for (ClientItem legacyItem : legacyInvoice.getItems()) {
            Client client = legacyItem.getClientName() == null || legacyItem.getClientName().isEmpty() ?
                    previousClient :
                    clientConverter.castIn(legacyItem.getClientName());
            // TODO: 24.06.16 Test this!
            Item item = itemConverter.castIn(legacyItem.getItem());

            PurchasedItem purchase = new PurchasedItem(client, item);
            purchase.setItemCount(legacyItem.getItemCount());
            purchase.setFromDate(legacyItem.getFromDate());
            purchase.setToDate(legacyItem.getToDate());
            purchase.setDateEnabled(dateEnabledConverter.castIn(legacyItem.getDateEnabled()));

            invoice.getPurchases().add(purchase);
        }

        return invoice;
    }

    @Override
    public InvoiceModel castOut(Invoice invoice) {
        InvoiceModel legacyInvoice = new InvoiceModel();
        legacyInvoice.setId(Math.toIntExact(invoice.getId()));
        legacyInvoice.setDate(invoice.getDate());
        legacyInvoice.setAddress(invoice.getAddress());
        legacyInvoice.setPdfFileName(invoice.getPdfFileName());

        Client previousClient = clientConverter.castIn(null);
        for (PurchasedItem purchase : invoice.getPurchases()) {
            Client client = purchase.getClient();
            Item item = purchase.getItem();

            ClientItem legacyIem = new ClientItem();
            if (client.getId() == previousClient.getId()) {
                legacyIem.setClientName("");
            } else {
                legacyIem.setClientName(clientConverter.castOut(client));
            }
            legacyIem.setItem(itemConverter.castOut(item));
            legacyIem.setItemCount(purchase.getItemCount());
            legacyIem.setFromDate(purchase.getFromDate());
            legacyIem.setToDate(purchase.getToDate());
            legacyIem.setDateEnabled(dateEnabledConverter.castOut(purchase.getDateEnabled()));
        }

        return legacyInvoice;
    }
}
