package app.config.manager;

import app.model.DateEnabled;
import app.model.client.Client;
import app.model.client.PurchasedItem;
import app.model.invoice.Invoice;
import app.model.item.Item;
import app.util.GaussianRandom;
import app.util.RightRandomGaussian;
import com.wx.io.TextAccessor;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

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


    public static List<Invoice> generateInvoice(int i) {
        Set<LocalDate> uniqueDates = new HashSet<>();
        return Stream.generate(DataGenerator::generateInvoice)
                .filter(invoice -> uniqueDates.add(invoice.getDate()))
                .limit(i).collect(Collectors.toList());
    }

    public static Invoice generateInvoice() {
        Invoice invoice = new Invoice();
        invoice.setId(++counter);
        invoice.setAddress(generateItemName());
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

                PurchasedItem purchase = new PurchasedItem(client, item);
                purchase.setDateEnabled(DateEnabled.values()[RANDOM.nextInt(DateEnabled.values().length)]);
                purchase.setToDate(generateDate());
                purchase.setFromDate(generateDate());
                purchase.setItemCount(COUNT_PER_PURCHASE.nextInt());

                invoice.getPurchases().add(purchase);
            }
        }

        return invoice;
    }





    private static final int ITEMS_COUNT = 100;
    private static final int CLIENTS_COUNT = 100;

    private static final int MAX_CLIENTS_PER_INVOICE = 20;
    private static final int MAX_PURCHASES_PER_CLIENT = 20;

    private static List<Item> randomItems;


    public static List<Invoice> generateRandomInvoices(int count) {
        List<Invoice> invoices = new ArrayList<>(count);
//        for (int i = 0; i < count; i++) {
//            Invoice invoice = new Invoice();
//            invoice.setId(count);
//            invoice.setAddress("Address " + i);
//            invoice.setDate(LocalDate.ofEpochDay(Math.abs(RANDOM.nextInt(999999999))));
//            invoice.setPdfFileName("PDF " + i);
//
//            int nbClients = RANDOM.nextInt(MAX_CLIENTS_PER_INVOICE);
//            for (int j = 0; j < nbClients; j++) {
//                Client client = getRandomClient();
//                int nbPurchases = RANDOM.nextInt(MAX_PURCHASES_PER_CLIENT);
//
//                for (int k = 0; k < nbPurchases; k++) {
//                    client.getPurchasedItems().add(createRandomPurchase());
//                }
//
//                invoice.getPurchases().add(client);
//            }
//
//            invoices.add(invoice);
//        }

        return invoices;
    }

    public static PurchasedItem createRandomPurchase() {
        PurchasedItem item = new PurchasedItem(null, getRandomItem());
        item.setFromDate(LocalDate.ofEpochDay(Math.abs(RANDOM.nextInt(999999999))));
        item.setToDate(LocalDate.ofEpochDay(Math.abs(RANDOM.nextInt(999999999))));
        item.setDateEnabled(DateEnabled.values()[RANDOM.nextInt(DateEnabled.values().length)]);
        item.setItemCount(Math.abs(RANDOM.nextInt()));
        return item;
    }

    public static List<Item> getRandomItems(int count) {
        return Stream.generate(DataGenerator::getRandomItem)
                .limit(count)
                .collect(Collectors.toList());
    }


    public static Item getRandomItem() {
        if (randomItems == null) {
            randomItems = new ArrayList<>(ITEMS_COUNT);

            for (int i = 0; i < ITEMS_COUNT; i++) {
                Item item = new Item();
                item.setId(i);
                item.setName("Item " + i);
                item.setPrice(RANDOM.nextDouble() * Double.MAX_VALUE);
                item.setVat(RANDOM.nextDouble() * 100.0);
                item.setDefaultDateEnabled(DateEnabled.values()[RANDOM.nextInt(DateEnabled.values().length)]);

                randomItems.add(item);
            }
        }

        return randomItems.get(RANDOM.nextInt(randomItems.size()));
    }

    public static Client getRandomClient() {
        int clientId = RANDOM.nextInt(CLIENTS_COUNT);

        Client client = new Client();
        client.setId(clientId);
        client.setName("Client " + clientId);

        return client;
    }


}
