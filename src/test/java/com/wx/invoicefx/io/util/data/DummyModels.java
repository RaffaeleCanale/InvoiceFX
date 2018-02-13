package com.wx.invoicefx.io.util.data;

import com.wx.invoicefx.model.InvoiceFormats;
import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.util.math.GaussianRandom;
import com.wx.invoicefx.util.math.RightRandomGaussian;
import com.wx.invoicefx.util.string.SentenceItemsParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wx.invoicefx.io.util.data.DummyData.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.05.17.
 */
public class DummyModels {

    private static final long SEED = 26541;

    private static final Vat[] VATS = {new Vat(3.8, 1), new Vat(8, 2)};
    private static final GaussianRandom GROUPS_PER_INVOICE = new RightRandomGaussian(SEED+1, 1, 3);
    private static final GaussianRandom CLIENTS_PER_GROUP = new RightRandomGaussian(SEED+2, 1, 3);
    private static final GaussianRandom PURCHASES_PER_GROUP = new RightRandomGaussian(SEED+3, 1, 3);
    private static final Random RANDOM = new Random(SEED + 4);

//    private static final GaussianRandom CLIENTS_PER_INVOICE = new RightRandomGaussian(SEED, 1, 3);
//    private static final GaussianRandom PURCHASES_PER_CLIENT = new RightRandomGaussian(SEED+1, 1, 3);
//    private static final GaussianRandom CLIENTS_PER_PURCHASE = new RightRandomGaussian(SEED+2, 1, 3);
    private static final GaussianRandom COUNT_PER_PURCHASE = new RightRandomGaussian(SEED+4, 1, 3);
    private static final GaussianRandom ITEM_PRICE = new GaussianRandom(SEED+5, 100.0, 50.0);
    private static int invoiceIdCounter = 0;
//    private static int groupIdCounter = 0;

    public static List<Invoice> generateInvoices(int n) {
        return generateList(DummyModels::generateInvoice, n);
    }

    public static Invoice generateInvoice() {
        int nbGroups = GROUPS_PER_INVOICE.nextInt();

        Invoice invoice = new Invoice();
        invoice.setId(++invoiceIdCounter);
        invoice.setAddress(generateAddressName());
        invoice.setDate(generateDate());
        invoice.setPurchaseGroups(generateList(DummyModels::generatePurchaseGroup, nbGroups));
        invoice.setPdfFilename(invoice.getId() + ".pdf");

        return invoice;
    }

    public static Client generateClient() {
        Client client = new Client();
        client.setName(DummyData.generateClientName());

        return client;
    }

    public static Purchase generatePurchase() {
        Purchase purchase = new Purchase();
        Item item = new Item();

        item.setDefaultDateEnabled(DateEnabled.values()[RANDOM.nextInt(DateEnabled.values().length)]);
        item.setName(DummyData.generateItemName());
        item.setPrice(ITEM_PRICE.next());
        item.setVat(VATS[RANDOM.nextInt(VATS.length)]);
        item.setActive(true);

        purchase.setItem(item);
        purchase.setDateEnabled(DateEnabled.values()[RANDOM.nextInt(DateEnabled.values().length)]);
        purchase.setToDate(generateDate());
        purchase.setFromDate(generateDate());
        purchase.setItemCount(COUNT_PER_PURCHASE.nextInt());

        return purchase;
    }

    public static PurchaseGroup generatePurchaseGroup() {
        int nbClients = CLIENTS_PER_GROUP.nextInt();
        int nbPurchases = PURCHASES_PER_GROUP.nextInt();

        PurchaseGroup group = new PurchaseGroup();
//            group.setId(++groupIdCounter);
        group.setClients(generateList(DummyModels::generateClient, nbClients));
        group.setPurchases(generateList(DummyModels::generatePurchase, nbPurchases));

        List<String> stopWords = new ArrayList<>(group.getClients().size() - 1);
        for (int i = 0; i < group.getClients().size() - 1; i++) {
            stopWords.add(SentenceItemsParser.STOP_WORD[RANDOM.nextInt(SentenceItemsParser.STOP_WORD.length)]);
        }
        group.setStopWords(stopWords);

        return group;
    }

    private static <E> List<E> generateList(Supplier<E> supplier, int count) {
        return Stream.generate(supplier).limit(count).collect(Collectors.toList());
    }

}
