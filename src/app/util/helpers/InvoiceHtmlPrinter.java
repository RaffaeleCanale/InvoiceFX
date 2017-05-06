package app.util.helpers;

import app.model.DateEnabled;
import app.model.invoice.InvoiceModel;
import app.model.item.ClientItem;
import app.util.ExceptionLogger;
import com.wx.fx.Lang;
import com.wx.io.TextAccessor;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static app.util.helpers.KeyWordHelper.keyPattern;
import static app.util.helpers.KeyWordHelper.keyWordsIn;

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
    private static String clientModel;
    private static String itemModel;

    /**
     * Print an invoice using the HTML format.
     *
     * @param invoice Invoice to print
     *
     * @return A formatted view of the invoice in HTML code
     */
    public static String print(InvoiceModel invoice) {
        if (invoiceModel == null) {
            invoiceModel = loadModel("/html_model/InvoiceHtmlModel.html");
        }
        if (clientModel == null) {
            clientModel = loadModel("/html_model/ClientHtmlModel.html");
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


    private static String generateInvoice(InvoiceModel invoice) {
        String clients = invoice.getItems().stream()
                .map(InvoiceHtmlPrinter::generateClientItem)
                .collect(Collectors.joining("<br>"));

        return invoiceModel
                .replace(keyPattern("id"), InvoiceHelper.idFormat().format(invoice.getId()))
                .replace(keyPattern("address"), invoice.addressProperty().getValueSafe().replaceAll("\n", "<br>"))
                .replace(keyPattern("date"), formattedDate(invoice.getDate()))
                .replace(keyPattern("clients"), clients)
                .replace(keyPattern("total"), getTotalPrice(invoice));
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

    private static String generateClientItem(ClientItem item) {
        return itemModel
                .replace(keyPattern("client"), item.clientNameProperty().getValueSafe())
                .replace(keyPattern("count"), String.valueOf(item.getItemCount()))
                .replace(keyPattern("item"), item.getItem().itemNameProperty().getValueSafe())
                .replace(keyPattern("price"), getPrice(item))
                .replace(keyPattern("from"), item.getDateEnabled() != DateEnabled.NONE ? formattedDate(item.getFromDate()) : "-")
                .replace(keyPattern("to"), item.getDateEnabled() == DateEnabled.BOTH ? formattedDate(item.getToDate()) : "-");
    }

    private static String getPrice(ClientItem item) {
        return InvoiceHelper.formattedPrice(item.getItem().priceProperty()).getValueSafe();
    }

    private static String getTotalPrice(InvoiceModel invoice) {
        NumberFormat format = InvoiceHelper.moneyFormat();

        return format.format(invoice.sumProperty().get());
    }

    private static String formattedDate(LocalDate date) {
        return InvoiceHelper.dateConverter().toString(date);
    }


}
