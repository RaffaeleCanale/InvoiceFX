package app.util.helpers;

import app.config.Config;
import app.util.adapter.DateConverter;
import javafx.beans.binding.NumberExpression;
import javafx.beans.binding.StringBinding;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDate;

import static app.config.preferences.SharedProperty.*;

/**
 * Utility methods for various actions related to invoices.
 * <p>
 * Created on 10/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class InvoiceHelper {

//    /**
//     * Create an invoice initialized with default values.
//     * <p>
//     * Requires the {@link Config} to be loaded (in order to select a unique id).
//     *
//     * @return An invoice with default values
//     */
//    public static Invoice createDefaultInvoice() {
//        return createDefaultInvoice(Config.sharedPreferences().getDoubleArrayProperty(SharedProperty.VAT)[0]);
//    }
//
//    /**
//     * Create an invoice initialized with default values except for the VAT.
//     *
//     * @param vat VAT to set
//     *
//     * @return An invoice with default values and custom VAT
//     */
//    public static Invoice createDefaultInvoice(double vat) {
//        Invoice invoice = new Invoice();
//        setDefaultValues(invoice, vat);
//
//        return invoice;
//    }
//
//    /**
//     * Set all the values of the given invoice to default, except for the VAT.
//     *
//     * @param invoice Invoice to set to default
//     * @param vat     VAT for this invoice
//     */
//    public static void setDefaultValues(Invoice invoice, double vat) {
//        invoice.setDate(LocalDate.now());
//        invoice.setId(Config.suggestId());
//        invoice.getItems().add(createDefaultClientItem(vat));
//    }
//
//    /**
//     * Create a client item with default values and given VAT.
//     *
//     * @param vat VAT to set
//     *
//     * @return A client item with default values
//     */
//    public static ClientItem createDefaultClientItem(double vat) {
//        ClientItem item = new ClientItem();
//        setDefaultValues(item, vat);
//
//        return item;
//    }
//
//    /**
//     * Set all the values of the given client item to default, except for the VAT.
//     *
//     * @param item Item to set to default
//     * @param vat  VAT for this item
//     */
//    public static void setDefaultValues(ClientItem item, double vat) {
//        item.setClientName("");
//        item.setFromDate(LocalDate.now());
//        item.setToDate(LocalDate.now().plusDays(1));
//        item.setItemCount(1);
//
//        ObservableList<ItemModel> itemModels = Config.getItemGroup(vat);
//        if (itemModels.isEmpty()) {
//            item.setItem(createDefaultItem(vat));
//        } else {
//            item.setItem(ItemModel.copyOf(itemModels.get(0)));
//        }
//
//        item.setDateEnabled(item.getItem().getDefaultDateEnabled());
//    }
//
//    /**
//     * Create an item with default values and given VAT.
//     *
//     * @param vat VAT to set
//     *
//     * @return A client item with default values
//     */
//    public static ItemModel createDefaultItem(double vat) {
//        ItemModel item = new ItemModel();
//        setDefaultValues(item, vat);
//
//        return item;
//    }
//
//    /**
//     * Set all the values of the given item to default, except for the VAT.
//     *
//     * @param model Item to set to default
//     * @param vat   VAT for this item
//     */
//    public static void setDefaultValues(Item model, double vat) {
//        model.setVat(vat);
//        model.setPrice(0.0);
//        model.setItemName("");
//        model.setDefaultDateEnabled(DateEnabled.BOTH);
//    }

//    /**
//     * Test if the given invoice has any field value that contains the queryIndexFirst.
//     *
//     * @param invoice Invoice to test
//     * @param query   Value to queryIndexFirst
//     *
//     * @return {@code true} if the invoice has a field containing the queryIndexFirst
//     */
//    public static boolean queryContains(Invoice invoice, String query) {
//        String[] words = query.split("\\s+");
//
//        for (String word : words) {
//            if (!containsWord(invoice, word)) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    private static boolean containsWord(Invoice invoice, String word) {
//        return contains(invoice.addressProperty().getValueSafe(), word)
//                || contains(dateConverter().toString(invoice.getDate()), word)
//                || contains(idFormat().format(invoice.getId()), word)
//                || invoice.getItems().stream().anyMatch(c -> containsWord(c, word));
//    }
//
//    private static boolean containsWord(PurchasedItem item, String query) {
//        StringConverter<LocalDate> dateConverter = dateConverter();
//        return contains(item.getItem().getItemName(), query)
//                || contains(item.getClientName(), query)
//                || (item.getDateEnabled() != DateEnabled.NONE && contains(dateConverter.toString(item.getFromDate()), query))
//                || (item.getDateEnabled() != DateEnabled.BOTH && contains(dateConverter.toString(item.getToDate()), query));
//    }
//
//    private static boolean containsWord(Client client, String query) {
//        return contains(client.getName(), query);
//    }
//

    private static boolean contains(String source, String word) {
        if (source == null || word == null) {
            return false;
        }

        return normalizeString(source).contains(normalizeString(word));
    }

    private static String normalizeString(String s) {
        // Removes all accents, capitals, etc...
        // It renders queries more flexible, so if we search 'muller', we may find 'Müller'

        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[^\\p{ASCII}]", "");

        return s.toLowerCase();
    }

    /**
     * Get the string converter for dates according the user preferences.
     *
     * @return A string converter for dates
     */
    public static StringConverter<LocalDate> dateConverter() {
        return new DateConverter(Config.sharedPreferences().getString(DATE_PATTERN));
    }

    /**
     * Get the ID format according to the user preferences.
     *
     * @return A formatter for an invoice ID
     */
    public static DecimalFormat idFormat() {
        return new DecimalFormat(Config.sharedPreferences().getString(ID_FORMAT));
    }

    /**
     * Create a string binding of a formatted (according to user preferences) price.
     *
     * @param price Expression for the price
     *
     * @return A formatted string binding of the price
     */
    public static StringBinding formattedPrice(NumberExpression price) {
        String format = Config.sharedPreferences().getString(MONEY_FORMAT);
        return price.asString(format);
    }

    /**
     * Get the money format to the user preferences.
     *
     * @return A formatter for money
     */
    public static NumberFormat moneyFormat() {
        return getNumberFormat(Config.sharedPreferences().getString(MONEY_DECIMAL_FORMAT));
    }

    /**
     * Get the VAT format according to user preferences.
     *
     * @return A formatter for VAT
     */
    public static NumberFormat vatFormat() {
        return getNumberFormat(Config.sharedPreferences().getString(VAT_DECIMAL_FORMAT));
    }

    /**
     * Get a formatter for the given pattern.
     *
     * @param pattern Format pattern
     *
     * @return A formatter for the given pattern
     */
    public static NumberFormat getNumberFormat(String pattern) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');

        return new DecimalFormat(pattern, symbols);
    }

//    public static String formatClientName(Invoice invoice) {
//        String name = invoice.getItems().stream()
//                .map(ClientItem::getClientName)
//                .filter(n -> !n.isEmpty())
//                .collect(Collectors.joining(", "));
//        if (name.isEmpty()) {
//            return " - ";
//        }
//        return name;
//    }
}
