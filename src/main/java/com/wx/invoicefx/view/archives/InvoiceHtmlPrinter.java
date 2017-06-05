package com.wx.invoicefx.view.archives;

import com.wx.fx.Lang;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.io.TextAccessor;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static com.wx.invoicefx.model.entities.ModelComparator.deepEquals;
import static com.wx.invoicefx.util.string.KeyWordHelper.keyPattern;
import static com.wx.invoicefx.util.string.KeyWordHelper.keyWordsIn;
import static com.wx.invoicefx.util.string.KeyWordHelper.replace;

/**
 * Utility class that "pretty prints" an invoice using HTML tags.
 * <p>
 * Created on 10/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class InvoiceHtmlPrinter {

    /*
    Here the 'models' refer to the HTML format models.
    More specifically, theses models are specified in the resources.
     */
    private static String invoiceModel;
    private static String itemModel;

    /**
     * Print an invoice using the HTML format.
     *
     * @param invoice Invoice to print
     *
     * @return A formatted view of the invoice in HTML code
     */
    public static String print(Invoice invoice) {
        if (invoiceModel == null) {
            invoiceModel = loadModel("/html_model/InvoiceHtmlModel.html");
        }
        if (itemModel == null) {
            itemModel = loadModel("/html_model/ItemHtmlModel.html");
        }

        return generateInvoice(invoice);
    }

    private static String loadModel(String name) {

        try (TextAccessor accessor = new TextAccessor().setIn(InvoiceHtmlPrinter.class.getResourceAsStream(name))) {

            String model = accessor.readText();

            for (String keyWord : keyWordsIn(model)) {
                String value = null;
                if (keyWord.startsWith("/")) {
                    value = InvoiceHtmlPrinter.class.getResource(keyWord).toString();
                } else if (keyWord.contains(".")) {
                    value = Lang.getString(keyWord);
                }

                if (value != null) {
                    model = model.replace(keyPattern(keyWord), value);
                }
            }

            return model;
        } catch (IOException e) {
            ExceptionLogger.logException(e);
            return "An error occurred: " + e.getMessage();
        }
    }


    private static String generateInvoice(Invoice invoice) {
        return "TODO";


//        Property<Client> lastClient = new SimpleObjectProperty<>();
//        String clients = invoice.getPurchases().stream()
//                .map(purchase -> {
//                        boolean ignoreClient = deepEquals(lastClient.getValue(), purchase.getClient());
//                        lastClient.setValue(purchase.getClient());
//
//                        return generatePurchase(purchase, ignoreClient);
//                })
//                .collect(Collectors.joining("<br>"));
//
//        return invoiceModel
//                .replace(keyPattern("id"), InvoiceFormats.idFormat().format(invoice.getId()))
//                .replace(keyPattern("address"), invoice.addressProperty().getValueSafe().replaceAll("\n", "<br>"))
//                .replace(keyPattern("date"), formattedDate(invoice.getDate()))
//                .replace(keyPattern("clients"), clients)
//                .replace(keyPattern("total"), getTotalPrice(invoice));
    }

//    private static String generateClient(ClientModel client) {
//        String items = client.getItems().stream()
//                .map(InvoiceHtmlPrinter::generateClientItem)
//                .collect(Collectors.joining("<br>"));
//
//        return clientModel
//                .replace(keyPattern("name"), client.nameProperty().getValueSafe())
//                .replace(keyPattern("items"), items);
//
//    }

    private static String generatePurchase(Purchase purchase, boolean ignoreClient) {
        return "TODO";
//
//        return itemModel
//                .replace(keyPattern("client"), purchase.getClient() == null || ignoreClient ? "" : purchase.getClient().nameProperty().getValueSafe())
//                .replace(keyPattern("count"), String.valueOf(purchase.getItemCount()))
//                .replace(keyPattern("item"), purchase.getItem() == null ? "" : purchase.getItem().nameProperty().getValueSafe())
//                .replace(keyPattern("price"), purchase.getItem() == null ? "" : getPrice(purchase.getItem()))
//                .replace(keyPattern("from"), purchase.getDateEnabled() != DateEnabled.NONE ? formattedDate(purchase.getFromDate()) : "-")
//                .replace(keyPattern("to"), purchase.getDateEnabled() == DateEnabled.BOTH ? formattedDate(purchase.getToDate()) : "-");
    }

    private static String getPrice(Item item) {
        return InvoiceFormats.moneyFormat().format(item.getPrice());
//        return InvoiceFormats.formattedPrice(item.getPrice());
    }

    private static String getTotalPrice(Invoice invoice) {
        NumberFormat format = InvoiceFormats.moneyFormat();

        return format.format(invoice.getSum());
    }

    private static String formattedDate(LocalDate date) {
        return InvoiceFormats.dateConverter().toString(date);
    }


}
