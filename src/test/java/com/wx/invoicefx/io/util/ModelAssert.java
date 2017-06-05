package com.wx.invoicefx.io.util;

import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wx.invoicefx.model.entities.ModelComparator.deepEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.05.17.
 */
public class ModelAssert {

    public static void assertInvoicesEquals(List<Invoice> expectedInvoices, List<Invoice> actualInvoices) {
        assertEquals("Lists do not have the same number of invoices", expectedInvoices.size(), actualInvoices.size());

        List<Set<Invoice>> expectedInvoicesByDate = groupByDate(expectedInvoices);
        List<Set<Invoice>> actualInvoicesByDate = groupByDate(actualInvoices);


        for (int i = 0; i < expectedInvoicesByDate.size(); i++) {
            Set<Invoice> expected = expectedInvoicesByDate.get(i);
            Set<Invoice> actual = actualInvoicesByDate.get(i);

            assertInvoicesSetEquals(expected, actual);
//
//            if (!deepEquals(expected, actual)) {
//                throw new AssertionError("Invoices differ.\nExpected:\n" + expected + "\n\nActual:\n" + actual);
//            }
        }
    }

    private static void assertInvoicesSetEquals(Set<Invoice> expectedInvoice, Set<Invoice> actualInvoices) {
        assertEquals("", expectedInvoice.size(), actualInvoices.size());

        Map<Long, Invoice> expectedMap = toMap(expectedInvoice);
        Map<Long, Invoice> actualMap = toMap(actualInvoices);

        for (Long id : expectedMap.keySet()) {
            Invoice expected = expectedMap.get(id);
            Invoice actual = actualMap.get(id);

            if (actual == null || !deepEquals(expected, actual)) {
                throw new AssertionError("Invoices differ.\nExpected:\n" + expected + "\n\nActual:\n" + actual);
            }
        }
    }

    private static Map<Long, Invoice> toMap(Set<Invoice> invoices) {
        return invoices.stream().collect(Collectors.toMap(Invoice::getId, Function.identity(), (BinaryOperator<Invoice>) (a, b) -> {
            throw new AssertionError("Multiple invoices with the same ID");
        }));
    }

    private static List<Set<Invoice>> groupByDate(List<Invoice> invoices) {
        if (invoices.isEmpty()) return Collections.emptyList();

        List<Set<Invoice>> result = new ArrayList<>();
        Set<Invoice> lastSet = null;
        LocalDate lastDate = null;


        for (Invoice invoice : invoices) {
            if (lastDate == null || !invoice.getDate().isEqual(lastDate)) {
                lastSet = new HashSet<>();
                result.add(lastSet);
            }

            lastSet.add(invoice);
            lastDate = invoice.getDate();
        }

        return result;
    }

    public static void assertClientsEquals(List<Client> expectedClients, List<Client> actualClients) {
        assertEquals("Lists do not have the same number of clients", expectedClients.size(), actualClients.size());

        for (int i = 0; i < expectedClients.size(); i++) {
            Client expected = expectedClients.get(i);
            Client actual = actualClients.get(i);

            if (!deepEquals(expected, actual)) {
                throw new AssertionError("Clients differ.\nExpected:\n" + expected + "\n\nActual:\n" + actual);
            }
        }
    }

    public static void assertItemsEquals(List<Item> expectedItems, List<Item> actualItems) {
        assertEquals("Lists do not have the same number of items", expectedItems.size(), actualItems.size());

        for (int i = 0; i < expectedItems.size(); i++) {
            Item expected = expectedItems.get(i);
            Item actual = actualItems.get(i);

            if (!deepEquals(expected, actual)) {
                throw new AssertionError("Items differ.\nExpected:\n" + expected + "\n\nActual:\n" + actual);
            }
        }
    }

    public static void assertClientsEquals(Set<Client> expectedClients, Set<Client> actualClients) {
        throw new AssertionError();
    }

}
