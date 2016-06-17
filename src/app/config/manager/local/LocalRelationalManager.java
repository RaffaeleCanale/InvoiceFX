package app.config.manager.local;


import app.config.manager.ManagerInterface;
import app.config.manager.datafile.ClusteredIndex;
import app.model.DateEnabled;
import app.model.client.Client;
import app.model.client.PurchasedItem;
import app.model.invoice.Invoice;
import app.model.item.Item;
import com.wx.properties.property.Property;
import com.wx.util.future.IoIterator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class LocalRelationalManager implements ManagerInterface {

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
    private final ClusteredIndex purchasedItemsTable;

    public LocalRelationalManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Invoice> getAllInvoices() {



        throw new UnsupportedOperationException();
    }

    public void garbageCollectUnreferenced() throws IOException {
        IoIterator<Object[]> it = clientsTable.iterator();
        while (it.hasNext()) {
            Object clientId = it.next()[0];

            boolean isReferenced = purchasedItemsTable
                    .queryFirst(p -> clientId.equals(p[1]))
                    .isPresent();

            if (!isReferenced) {
                // TODO: 17.06.16 Add remove to IoIterator
                // it.remove();
            }
        }
    }

    @Override
    public void addNewInvoice(Invoice invoice) throws IOException {
        // TODO: 17.06.16 Begin transaction!

        // TODO: 17.06.16 Somehow check the assumption that invoice.getId() is unique!

        invoicesTable.insert(getInvoiceRecord(invoice));

        for (Client client : invoice.getClients()) {
            getOrInsertClient(client);

            for (PurchasedItem purchase : client.getPurchasedItems()) {
                Item item = purchase.getItem();

                getOrInsertItem(item);

                purchasedItemsTable.insert(getPurchaseRecord(invoice, client, purchase));
            }
        }
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
        return purchasedItemsTable.queryFirst(r -> invoiceId == (long) r[0] && clientId == (long) r[1] && itemId == (long) r[2]);
    }

    private static Object[] getInvoiceRecord(Invoice invoice) {
        return new Object[] {
                idOrNull(invoice.getId()),
                invoice.getAddress(),
                invoice.getDate(),
                invoice.getPdfFileName()
        };
    }

    private static Invoice getInvoiceModel(List<Client> clients, Object[] record) {
        Invoice invoice = new Invoice();
        invoice.setId((long) record[0]);
        invoice.setAddress((String) record[1]);
        invoice.setDate((LocalDate) record[2]);
        invoice.setPdfFileName((String) record[3]);
        invoice.getClients().setAll(clients);
        return invoice;
    }

    private static Object[] getClientRecord(Client client) {
        return new Object[] {
                idOrNull(client.getId()),
                client.getName()
        };
    }

    private static Client getClientModel(List<PurchasedItem> purchases, Object[] record) {
        Client client = new Client();
        client.setId((long) record[0]);
        client.setName((String) record[1]);
        client.getPurchasedItems().setAll(purchases);
        return client;
    }

    private static Object[] getItemRecord(Item item) {
        return new Object[] {
                idOrNull(item.getId()),
                item.getName(),
                item.getPrice(),
                item.getVat(),
                item.getDefaultDateEnabled()
        };
    }

    private static Item getItemModel(Object[] record) {
        Item item = new Item();
        item.setId((long) record[0]);
        item.setName((String) record[1]);
        item.setPrice((double) record[2]);
        item.setVat((double) record[3]);
        item.setDefaultDateEnabled((DateEnabled) record[4]);
        return item;
    }

    private static Object[] getPurchaseRecord(Invoice invoice, Client client, PurchasedItem purchase) {
        return new Object[] {
                invoice.getId(),
                client.getId(),
                purchase.getItem().getId(),
                purchase.getItemCount(),
                purchase.getFromDate(),
                purchase.getToDate(),
                purchase.getDateEnabled()
        };
    }

    private static PurchasedItem getPurchaseModel(Item item, Object[] record) {
        PurchasedItem purchase = new PurchasedItem();
        purchase.setItem(item);
        purchase.setItemCount((int) record[3]);
        purchase.setFromDate((LocalDate) record[4]);
        purchase.setToDate((LocalDate) record[5]);
        purchase.setDateEnabled((DateEnabled) record[6]);
        return purchase;
    }

    private static Object idOrNull(long id) {
        return id > 0 ? id : null;
    }

    private class InvoicesIterator implements IoIterator<Invoice> {

        private Map<Long, Item> itemsBuffer;


        private Iterator<Object[]> invoicesIterator;

        @Override
        public boolean hasNext() {
            return invoicesIterator.hasNext();
        }

        @Override
        public Invoice next() throws IOException {
            Object[] nextInvoice = invoicesIterator.next();

            long invoiceId = (long) nextInvoice[0];

            Map<Long, List<PurchasedItem>> purchasesPerClient = new HashMap<>();

            IoIterator<Object[]> it = purchasedItemsTable.queryIndex(invoiceId);
            while (it.hasNext()) {
                Object[] purchase = it.next();
                long clientId = (long) purchase[1];
                Item item = getItem((long) purchase[2]);

                purchasesPerClient.computeIfAbsent(clientId, id -> new ArrayList<>())
                        .add(getPurchaseModel(item, purchase));
            }


            List<Client> clients = new ArrayList<>(purchasesPerClient.size());
            for (Map.Entry<Long, List<PurchasedItem>> entry : purchasesPerClient.entrySet()) {
                clients.add(loadClient(entry.getKey(), entry.getValue()));
            }

            return getInvoiceModel(clients, nextInvoice);
        }

        private Client loadClient(long clientId, List<PurchasedItem> purchasedItems) throws IOException {
            Object[] record = clientsTable.queryIndexFirst(clientId).orElseThrow(() -> new IOException()); // TODO: 17.06.16 MESSAGE
            return getClientModel(purchasedItems, record);
        }

        private Item getItem(long itemId) throws IOException {
            Item item = itemsBuffer.get(itemId);
            if (item == null) {
                item = getItemModel(itemsTable.queryIndexFirst(itemId).orElseThrow(() -> new IOException())); // TODO: 17.06.16 MESSAGE
                itemsBuffer.put(itemId, item);
            }

            return item;
        }
    }
}
