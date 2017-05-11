package com.wx.invoicefx.model.save;


import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.io.file.DirectoryStorage;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.client.Purchase;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.properties.property.Property;
import com.wx.util.future.IoIterator;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.wx.invoicefx.model.save.RelationalModelHelper.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class SaveManager {

    private static final int DEFAULT_PARTITION_SIZE = 100;

    private static long idIndexedInsert(ClusteredIndex table, Object[] record) throws IOException {
        assert record[0] == null;
        assert table.getSortKey() == 0;

        IoIterator<Object[]> it = table.iterator();
        long newId = it.hasNext() ? (long) it.next()[0] + 1 : 1L;
        record[0] = newId;

        table.insert(record);

        return newId;
    }

    private static long genericInsert(ClusteredIndex table, Object[] record, Property<Long> lastId) throws IOException {
        assert record[0] == null;

        long newId = lastId.get().orElse(0L) + 1;
        record[0] = newId;

        table.insert(record);
        lastId.set(newId);

        return newId;
    }

    private final ClusteredIndex invoicesTable;
    private final ClusteredIndex clientsTable;
    private final ClusteredIndex itemsTable;
    private final ClusteredIndex purchasesTable;

    public SaveManager(File dataDirectory) {
        invoicesTable = new ClusteredIndex(new DirectoryStorage(INVOICE_SERIALIZER, dataDirectory, "invoices"), DEFAULT_PARTITION_SIZE, 2);
        clientsTable = new ClusteredIndex(new DirectoryStorage(CLIENT_SERIALIZER, dataDirectory, "clients"), DEFAULT_PARTITION_SIZE, 0);
        itemsTable = new ClusteredIndex(new DirectoryStorage(ITEM_SERIALIZER, dataDirectory, "items"), DEFAULT_PARTITION_SIZE, 0);
        purchasesTable = new ClusteredIndex(new DirectoryStorage(PURCHASE_SERIALIZER, dataDirectory, "purchases"), DEFAULT_PARTITION_SIZE, 0);
    }

    public SaveManager(ClusteredIndex invoicesTable, ClusteredIndex clientsTable, ClusteredIndex itemsTable, ClusteredIndex purchasesTable) {
        this.invoicesTable = invoicesTable;
        this.clientsTable = clientsTable;
        this.itemsTable = itemsTable;
        this.purchasesTable = purchasesTable;
    }

    public IoIterator<Invoice> getAllInvoices() throws IOException {
        return new InvoicesIterator();
    }

    public IoIterator<Client> getAllClients() throws IOException {
        return new ClientsIterator();
    }

    public void cleanUnreferenced() throws IOException {
        IoIterator<Object[]> it = clientsTable.iterator();
        while (it.hasNext()) {
            Object clientId = it.next()[0];

            boolean isReferenced = purchasesTable
                    .queryFirst(p -> clientId.equals(p[1]))
                    .isPresent();

            if (!isReferenced) {
                it.remove();
            }
        }

        this.flush();
    }

    public void addNewInvoice(Invoice invoice) throws IOException {
        // TODO: 17.06.16 Begin transaction (Actually, maybe not to be done here...)

        if (invoice.getId() <= 0) {
            throw new IllegalArgumentException("Invoice has no id!");
        }

        invoicesTable.insertUnique(getInvoiceRecord(invoice));

        for (Purchase purchase : invoice.getPurchases()) {
            Client client = purchase.getClient();
            Item item = purchase.getItem();

            getOrInsertClient(client);
            getOrInsertItem(item);

            purchasesTable.insert(getPurchaseRecord(invoice, client, purchase));

        }
    }

    public void removeInvoice(Invoice invoice) throws IOException {
        if (invoice.getId() <= 0) {
            throw new IllegalArgumentException("Invoice has no id!");
        }

        boolean removed = invoicesTable.removeFirst(r -> (long) r[0] == invoice.getId());
        if (!removed) {
            throw new IOException("Invoice not found"); // TODO: 24.06.16 Message
        }

        IoIterator<Object[]> it = purchasesTable.queryIndex(invoice.getId());
        while (it.hasNext()) {
            it.next();
            it.remove();
        }

//        cleanUnreferenced();
        this.flush();
    }

    private void flush() throws IOException {
        invoicesTable.flush();
        clientsTable.flush();
        itemsTable.flush();
        purchasesTable.flush();
    }

    private void getOrInsertClient(Client client) throws IOException {
        if (client.getId() > 0) {
            return;
        }

        Optional<Object[]> existing = findClient(client.getName());

        long id = existing.isPresent() ?
                (long) existing.get()[0] :
                idIndexedInsert(clientsTable, getClientRecord(client));

        client.setId(id);
    }

    private void getOrInsertItem(Item item) throws IOException {
        if (item.getId() > 0) {
            return;
        }

        Optional<Object[]> existing = findItem(item.getName(), item.getPrice());

        long id = existing.isPresent() ?
                (long) existing.get()[0] :
                idIndexedInsert(itemsTable, getItemRecord(item));

        item.setId(id);
    }


    private Optional<Object[]> findClient(String clientName) throws IOException {
        return clientsTable.queryFirst(r -> clientName.equals(r[1]));
    }

    private Optional<Object[]> findItem(String itemName, double itemPrice) throws IOException {
        return itemsTable.queryFirst(r -> itemName.equals(r[1]) && itemPrice == (double) r[2]);
    }

    private Optional<Object[]> findPurchase(long invoiceId, long clientId, long itemId) throws IOException {
        return purchasesTable.queryFirst(r -> invoiceId == (long) r[0] && clientId == (long) r[1] && itemId == (long) r[2]);
    }

    // For tests
    ClusteredIndex getPurchasesTable() {
        return purchasesTable;
    }

    public String debugPrint() throws IOException {
        return "Invoices:\n"
                + invoicesTable.debugPrint()
                + "\nClients:\n"
                + clientsTable.debugPrint()
                + "\nItems:\n"
                + itemsTable.debugPrint()
                + "\nPurchases:\n"
                + purchasesTable.debugPrint();
    }

    public void debugDisplay() throws IOException {
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setLayout(new GridLayout(1,4,10,10));

        add(dialog, invoicesTable, "Invoices");
        add(dialog, clientsTable, "Clients");
        add(dialog, itemsTable, "Items");
        add(dialog, purchasesTable, "Purchases");

        dialog.pack();
        dialog.setVisible(true);
    }

    private void add(JDialog parent, ClusteredIndex table, String name) throws IOException {
        JPanel panel = table.debugDisplay();
        panel.add(new JLabel(name), BorderLayout.SOUTH);
        parent.add(panel);
    }

    private class ClientsIterator implements IoIterator<Client> {

        private final IoIterator<Object[]> clientsIterator;

        public ClientsIterator() throws IOException {
            this.clientsIterator = clientsTable.iterator();
        }

        @Override
        public boolean hasNext() {
            return clientsIterator.hasNext();
        }

        @Override
        public Client next() throws IOException {
            return getClientModel(clientsIterator.next());
        }
    }

    private class InvoicesIterator implements IoIterator<Invoice> {

        private final IoIterator<Object[]> invoicesIterator;

        private final Map<Long, Item> itemsBuffer;
        private final Map<Long, Client> clientsBuffer;

        private InvoicesIterator() throws IOException {
            invoicesIterator = invoicesTable.iterator();
            itemsBuffer = new HashMap<>();
            clientsBuffer = new HashMap<>();
        }

        @Override
        public boolean hasNext() {
            return invoicesIterator.hasNext();
        }

        @Override
        public Invoice next() throws IOException {
            Object[] nextInvoice = invoicesIterator.next();
            long invoiceId = (long) nextInvoice[0];
            List<Purchase> purchases = new ArrayList<>();

            IoIterator<Object[]> it = purchasesTable.queryIndex(invoiceId);
            while (it.hasNext()) {
                Object[] purchase = it.next();
                Client client = loadClient((long) purchase[1]);
                Item item = getItem((long) purchase[2]);

                purchases.add(getPurchaseModel(client, item, purchase));
            }

            return getInvoiceModel(purchases, nextInvoice);
        }

        private Client loadClient(long clientId) throws IOException {
            Client client = clientsBuffer.get(clientId);
            if (client == null) {
                client = getClientModel(clientsTable.queryIndexFirst(clientId)
                        .orElseThrow(() -> new IOException("Client not found for id " + clientId)));
                clientsBuffer.put(clientId, client);
            }

            return client;
        }

        private Item getItem(long itemId) throws IOException {
            Item item = itemsBuffer.get(itemId);
            if (item == null) {
                item = getItemModel(itemsTable.queryIndexFirst(itemId).orElseThrow(() -> new IOException("Item not found for id " + itemId)));
                itemsBuffer.put(itemId, item);
            }

            return item;
        }
    }
}

