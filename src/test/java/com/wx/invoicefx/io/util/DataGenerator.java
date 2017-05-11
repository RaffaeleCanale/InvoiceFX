package com.wx.invoicefx.io.util;

import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.client.Purchase;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.util.math.GaussianRandom;
import com.wx.invoicefx.util.math.RightRandomGaussian;
import com.wx.io.TextAccessor;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wx.invoicefx.model.entities.ModelComparator.deepEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class DataGenerator {

    private static final long SEED = 24;

    private static final GaussianRandom CLIENTS_PER_INVOICE = new RightRandomGaussian(SEED, 1, 3);
    private static final GaussianRandom PURCHASES_PER_CLIENT = new RightRandomGaussian(SEED+1, 1, 3);
    private static final GaussianRandom COUNT_PER_PURCHASE = new RightRandomGaussian(SEED+2, 1, 3);
    private static final GaussianRandom ITEM_PRICE = new GaussianRandom(SEED+3, 100.0, 50.0);
    private static int counter = 0;

    private static final GaussianRandom DATE = new GaussianRandom(SEED+4, LocalDate.now().toEpochDay(), 50);

    private static final Random RANDOM = new Random(SEED+5);


    private static final List<String> CLIENT_NAMES = loadTestResources("/Clients.txt");
    private static final List<String> ADDRESSES_NAMES = loadTestResources("/Addresses.txt");
    private static final List<String> ITEMS_NAMES = loadTestResources("/Items.txt");

    private static List<String> loadTestResources(String name) {
        try (TextAccessor accessor = new TextAccessor().setIn(DataGenerator.class.getResourceAsStream(name))) {
            List<String> result = new ArrayList<>();
            accessor.read(result);

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalDate generateDate() {
        return LocalDate.ofEpochDay(DATE.nextLong());
    }

    public static String generateClientName() {
        return CLIENT_NAMES.get(RANDOM.nextInt(CLIENT_NAMES.size()));
    }

    public static String generateAddressName() {
        return ADDRESSES_NAMES.get(RANDOM.nextInt(ADDRESSES_NAMES.size()));
    }

    public static String generateItemName() {
        return ITEMS_NAMES.get(RANDOM.nextInt(ITEMS_NAMES.size()));
    }


    public static List<Invoice> generateInvoices(int n) {
        Set<LocalDate> uniqueDates = new HashSet<>();
        return Stream.generate(DataGenerator::generateInvoice)
                .filter(invoice -> uniqueDates.add(invoice.getDate()))
                .limit(n).collect(Collectors.toList());
    }

    public static Invoice generateInvoice() {
        Invoice invoice = new Invoice();
        invoice.setId(++counter);
        invoice.setAddress(generateAddressName());
        invoice.setDate(generateDate());

        int nbClients = CLIENTS_PER_INVOICE.nextInt();
        for (int j = 0; j < nbClients; j++) {
            Client client = new Client();
            client.setName(generateClientName());

            int nbPurchases = PURCHASES_PER_CLIENT.nextInt();
            for (int i = 0; i < nbPurchases; i++) {
                Item item = new Item();
                item.setDefaultDateEnabled(DateEnabled.values()[RANDOM.nextInt(DateEnabled.values().length)]);
                item.setName(generateItemName());
                item.setPrice(ITEM_PRICE.next());
                item.setVat(3.8);

                Purchase purchase = new Purchase(client, item);
                purchase.setDateEnabled(DateEnabled.values()[RANDOM.nextInt(DateEnabled.values().length)]);
                purchase.setToDate(generateDate());
                purchase.setFromDate(generateDate());
                purchase.setItemCount(COUNT_PER_PURCHASE.nextInt());

                invoice.getPurchases().add(purchase);
            }
        }

        return invoice;
    }


    public static void assertInvoicesEquals(List<Invoice> expectedInvoices, List<Invoice> actualInvoices) {
        assertEquals("Lists do not have the same number of invoices", expectedInvoices.size(), actualInvoices.size());

        for (int i = 0; i < expectedInvoices.size(); i++) {
            Invoice expected = expectedInvoices.get(i);
            Invoice actual = actualInvoices.get(i);

            if (!deepEquals(expected, actual)) {
                throw new AssertionError("Invoices differ.\nExpected:\n" + expected + "\n\nActual:\n" + actual);
            }
        }
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




//    private static final int ITEMS_COUNT = 100;
//    private static final int CLIENTS_COUNT = 100;
//
//    private static final int MAX_CLIENTS_PER_INVOICE = 20;
//    private static final int MAX_PURCHASES_PER_CLIENT = 20;
//
//    private static List<Item> randomItems;
//
//
//    public static List<Invoice> generateRandomInvoices(int count) {
//        List<Invoice> invoices = new ArrayList<>(count);
////        for (int i = 0; i < count; i++) {
////            Invoice invoice = new Invoice();
////            invoice.setId(count);
////            invoice.setAddress("Address " + i);
////            invoice.setDate(LocalDate.ofEpochDay(Math.abs(RANDOM.nextInt(999999999))));
////            invoice.setPdfFileName("PDF " + i);
////
////            int nbClients = RANDOM.nextInt(MAX_CLIENTS_PER_INVOICE);
////            for (int j = 0; j < nbClients; j++) {
////                Client client = getRandomClient();
////                int nbPurchases = RANDOM.nextInt(MAX_PURCHASES_PER_CLIENT);
////
////                for (int k = 0; k < nbPurchases; k++) {
////                    client.getPurchasedItems().add(createRandomPurchase());
////                }
////
////                invoice.getPurchases().add(client);
////            }
////
////            invoices.add(invoice);
////        }
//
//        return invoices;
//    }
//
//    public static PurchasedItem createRandomPurchase() {
//        PurchasedItem item = new PurchasedItem(null, getRandomItem());
//        item.setFromDate(LocalDate.ofEpochDay(Math.abs(RANDOM.nextInt(999999999))));
//        item.setToDate(LocalDate.ofEpochDay(Math.abs(RANDOM.nextInt(999999999))));
//        item.setDateEnabled(DateEnabled.values()[RANDOM.nextInt(DateEnabled.values().length)]);
//        item.setItemCount(Math.abs(RANDOM.nextInt()));
//        return item;
//    }
//
//    public static List<Item> getRandomItems(int count) {
//        return Stream.generate(DataGenerator::getRandomItem)
//                .limit(count)
//                .collect(Collectors.toList());
//    }
//
//
//    public static Item getRandomItem() {
//        if (randomItems == null) {
//            randomItems = new ArrayList<>(ITEMS_COUNT);
//
//            for (int i = 0; i < ITEMS_COUNT; i++) {
//                Item item = new Item();
//                item.setId(i);
//                item.setName("Item " + i);
//                item.setPrice(RANDOM.nextDouble() * Double.MAX_VALUE);
//                item.setVat(RANDOM.nextDouble() * 100.0);
//                item.setDefaultDateEnabled(DateEnabled.values()[RANDOM.nextInt(DateEnabled.values().length)]);
//
//                randomItems.add(item);
//            }
//        }
//
//        return randomItems.get(RANDOM.nextInt(randomItems.size()));
//    }
//
//    public static Client getRandomClient() {
//        int clientId = RANDOM.nextInt(CLIENTS_COUNT);
//
//        Client client = new Client();
//        client.setId(clientId);
//        client.setName("Client " + clientId);
//
//        return client;
//    }


}
