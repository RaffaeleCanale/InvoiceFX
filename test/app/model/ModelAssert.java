package app.model;

import app.model.client.Client;
import app.model.client.PurchasedItem;
import app.model.invoice.Invoice;
import app.model.item.Item;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.16.
 */
public class ModelAssert {

    public static void assertInvoiceEquals(List<Invoice> expected, List<Invoice> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertInvoiceEquals(expected.get(i), actual.get(i));
        }
    }

    public static void assertInvoiceEquals(Invoice expected, Invoice actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAddress(), actual.getAddress());
        assertEquals(expected.getDate(), actual.getDate());
        assertEquals(expected.getPdfFileName(), actual.getPdfFileName());

        assertEquals(expected.getPurchases().size(), actual.getPurchases().size());
        for (PurchasedItem expectedPurchase : expected.getPurchases()) {
            actual.getPurchases().stream()
                    .filter(p -> purchaseEquals(expectedPurchase, p))
                    .findAny().orElseThrow(() -> new AssertionError("Expected purchase: " + expectedPurchase));
        }
    }


    private static boolean purchaseEquals(PurchasedItem p1, PurchasedItem p2) {
        return clientEquals(p1.getClient(), p2.getClient()) && itemEquals(p1.getItem(), p2.getItem())
                && p1.getItemCount() == p2.getItemCount() && Objects.equals(p1.getDateEnabled(), p2.getDateEnabled())
                && Objects.equals(p1.getFromDate(), p2.getFromDate()) && Objects.equals(p1.getToDate(), p2.getToDate());
    }

    private static boolean clientEquals(Client c1, Client c2) {
        return c1.getId() == c2.getId() && Objects.equals(c1.getName(), c2.getName());
    }

    private static boolean itemEquals(Item i1, Item i2) {
        return i1.getId() == i2.getId() && Objects.equals(i1.getName(), i2.getName()) && i1.getPrice() == i2.getPrice()
                && Objects.equals(i1.getDefaultDateEnabled(), i2.getDefaultDateEnabled()) && i1.getVat() == i2.getVat();
    }

    public static void assertClientEquals(Client expected, Client actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
    }

    public static void assertPurchaseEquals(PurchasedItem expected, PurchasedItem actual) {
        assertClientEquals(expected.getClient(), actual.getClient());
        assertItemEquals(expected.getItem(), actual.getItem());
        assertEquals(expected.getDateEnabled(), actual.getDateEnabled());
        assertEquals(expected.getFromDate(), actual.getFromDate());
        assertEquals(expected.getToDate(), actual.getToDate());
        assertEquals(expected.getItemCount(), actual.getItemCount());
    }

    public static void assertItemEquals(Item expected, Item actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDefaultDateEnabled(), actual.getDefaultDateEnabled());
        assertEquals(expected.getPrice(), actual.getPrice(), 0.0);
        assertEquals(expected.getVat(), actual.getVat(), 0.0);
    }
}
