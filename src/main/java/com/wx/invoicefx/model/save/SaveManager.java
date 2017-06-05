package com.wx.invoicefx.model.save;


import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.model.save.table.*;
import com.wx.properties.page.ResourcePage;
import com.wx.util.future.IoIterator;
import com.wx.util.pair.Pair;
import com.wx.util.representables.string.LongRepr;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wx.invoicefx.model.save.table.ClientsTable.getClientModel;
import static com.wx.invoicefx.model.save.table.InvoicesTable.getInvoiceModel;
import static com.wx.invoicefx.model.save.table.ItemsTable.getItemRecord;
import static com.wx.invoicefx.model.save.table.PurchasesTable.getPurchaseModel;
import static com.wx.invoicefx.model.save.table.RecordsHelper.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class SaveManager {


    private static final String INVOICES_PK_PROPERTY = "clustered_index.invoices.max_id";
    private static final String GROUPSS_PK_PROPERTY = "clustered_index.groups.max_id";

//    private static long idIndexedInsert(ClusteredIndex table, Object[] record) throws IOException {
//        assert record[0] == null;
//        assert table.getSortKey() == 0;
//
//        IoIterator<Object[]> it = table.iterator();
//        long newId = it.hasNext() ? (long) it.next()[0] + 1 : 1L;
//        record[0] = newId;
//
//        table.insertWithIndex(record);
//
//        return newId;
//    }
//
//    private static long genericInsert(ClusteredIndex table, Object[] record, Property<Long> lastId) throws IOException {
//        assert record[0] == null;
//
//        long newId = lastId.get().orElse(0L) + 1;
//        record[0] = newId;
//
//        table.insertWithIndex(record);
//        lastId.set(newId);
//
//        return newId;
//    }

    private final InvoicesTable invoicesTable;
    private final ClientsTable clientsTable;
    private final ItemsTable itemsTable;
    private final PurchasesTable purchasesTable;
    private final PurchaseGroupsTable purchaseGroupsTable;
    private final ClientGroupsRelationTable clientGroupsRelationTable;
//    private final ClientPurchaseRelationTable clientPurchaseRelationTable;

    public SaveManager(File dataDirectory, ResourcePage metadataPage) {
        LongRepr caster = new LongRepr();

        invoicesTable = new InvoicesTable(dataDirectory, metadataPage.getProperty(INVOICES_PK_PROPERTY, caster));
        clientsTable = new ClientsTable(dataDirectory);
        itemsTable = new ItemsTable(dataDirectory);
        purchasesTable = new PurchasesTable(dataDirectory);
        purchaseGroupsTable = new PurchaseGroupsTable(dataDirectory, metadataPage.getProperty(GROUPSS_PK_PROPERTY, caster));
        clientGroupsRelationTable = new ClientGroupsRelationTable(dataDirectory);
//        clientPurchaseRelationTable = new ClientPurchaseRelationTable(dataDirectory);
    }

//    SaveManager(ClusteredIndex invoicesTable, ClusteredIndex clientsTable, ClusteredIndex itemsTable, ClusteredIndex purchasesTable) {
//        this.invoicesTable = invoicesTable;
//        this.clientsTable = clientsTable;
//        this.itemsTable = itemsTable;
//        this.purchasesTable = purchasesTable;
//    }

    public IoIterator<Invoice> getAllInvoices() throws IOException {
        return new InvoicesIterator();
    }

    public IoIterator<Client> getAllClients() throws IOException {
        return new MappedIterator<>(clientsTable.iterator(), ClientsTable::getClientModel);
    }

    public IoIterator<Item> getAllItems() throws IOException {
        return new MappedIterator<>(itemsTable.iterator(), ItemsTable::getItemModel);
    }

//    public IoIterator<Item> getItemsWith(double tva) throws IOException {
//        IoIterator<Object[]> query = itemsTable.query(row -> (double) row[3] == tva);
//
//        return new MappedIterator<>(query, ItemsTable::getItemModel);
//    }

    public void cleanUnreferenced() throws IOException {
        IoIterator<Object[]> it = clientsTable.iterator();
        while (it.hasNext()) {
            long clientId = getLong(it.next(), ClientsTable.Cols.ID);

            boolean isReferenced = clientGroupsRelationTable.queryFirst((relation) -> {
                long relationClientId = getLong(relation, ClientGroupsRelationTable.Cols.CLIENT_ID);
                return relationClientId == clientId;
            }).isPresent();

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

        invoicesTable.insertWithIndex(InvoicesTable.getInvoiceRecord(invoice));

        int groupIndex = 0;
        for (PurchaseGroup purchaseGroup : invoice.getPurchaseGroups()) {
            for (Client client : purchaseGroup.getClients()) {
                getOrInsertClient(client);
            }
            for (Purchase purchase : purchaseGroup.getPurchases()) {
                getOrInsertItem(purchase.getItem());
            }

            long groupId = purchaseGroupsTable.assignUniqueIdAndInsert(PurchaseGroupsTable.getPurchaseGroupRecord(invoice, purchaseGroup, groupIndex));
            purchaseGroup.setId(groupId);

            for (int i = 1; i < purchaseGroup.getClients().size(); i++) {
                Client client = purchaseGroup.getClients().get(i);
                clientGroupsRelationTable.insertWithIndex(ClientGroupsRelationTable.getClientPurchaseRelationRecord(purchaseGroup, client, i));
            }

            int purchaseIndex = 0;
            for (Purchase purchase : purchaseGroup.getPurchases()) {
                purchasesTable.insertWithIndex(PurchasesTable.getPurchaseRecord(purchaseGroup, purchase, purchaseIndex));

                purchaseIndex++;
            }


            groupIndex++;
        }

//        int purchaseIndex = 0;
//        for (Purchase purchase : invoice.getPurchases()) {
//            List<Client> clients = purchase.getClients();
//            Item item = purchase.getItem();
//
//            getOrInsertItem(item);
//            for (Client client : clients) {
//                getOrInsertClient(client);
//            }
//
//            purchasesTable.assignUniqueIdAndInsert(PurchasesTable.getPurchaseRecord(invoice, purchase, purchaseIndex));
//
//            purchaseIndex++;
//        }
    }

    public void removeInvoices(Collection<Invoice> invoices) throws IOException {
        for (Invoice invoice : invoices) {
            removeInvoice_helper(invoice);
        }

        flush();
    }

    public void removeInvoice(Invoice invoice) throws IOException {
        removeInvoice_helper(invoice);

//        cleanUnreferenced();
        this.flush();
    }

    private void removeInvoice_helper(Invoice invoice) throws IOException {
        if (invoice.getId() <= 0) {
            throw new IllegalArgumentException("Invoice has no id!");
        }

        boolean removed = invoicesTable.removeFirst(r -> (long) r[0] == invoice.getId());
        if (!removed) {
            throw new IOException("Invoice not found"); // TODO: 24.06.16 Message
        }


        for (PurchaseGroup group : invoice.getPurchaseGroups()) {
            int purchasesCount = group.getPurchases().size();

            if (purchasesCount > 0) {
                IoIterator<Object[]> it = purchasesTable.queryIndex(group.getId());
                for (int i = 0; i < purchasesCount && it.hasNext(); i++) {
                    it.next();
                    it.remove();
                }
            }

            int clientsCount = group.getClients().size();

            if (clientsCount > 1) {
                IoIterator<Object[]> it = clientGroupsRelationTable.queryIndex(group.getId());
                for (int i = 1; i < clientsCount && it.hasNext(); i++) {
                    it.next();
                    it.remove();
                }
            }
        }

        IoIterator<Object[]> it = purchaseGroupsTable.queryIndex(invoice.getId());
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    private void flush() throws IOException {
        invoicesTable.flush();
        clientsTable.flush();
        itemsTable.flush();
        purchasesTable.flush();
        purchaseGroupsTable.flush();
        clientGroupsRelationTable.flush();
    }

    private void getOrInsertClient(Client client) throws IOException {
        if (client.getId() > 0) {
            return;
        }

        Optional<Object[]> existing = findClient(client.getName());

        long clientId = existing.isPresent() ?
                getLong(existing.get(), ClientsTable.Cols.ID) :
                clientsTable.assignUniqueIdAndInsert(ClientsTable.getClientRecord(client));

        client.setId(clientId);
    }

    private void getOrInsertItem(Item item) throws IOException {
        if (item.getId() > 0) {
            return;
        }

        Optional<Object[]> existing = findItem(item.getName(), item.getPrice());

        long itemId = existing.isPresent() ?
                getLong(existing.get(), ItemsTable.Cols.ID) :
                itemsTable.assignUniqueIdAndInsert(getItemRecord(item));

        item.setId(itemId);
    }


    private Optional<Object[]> findClient(String queryName) throws IOException {
        return clientsTable.queryFirst(clientRecord -> {
            String clientName = getString(clientRecord, ClientsTable.Cols.NAME);

            return queryName.equals(clientName);
        });
    }

    private Optional<Object[]> findItem(String queryName, double queryPrice) throws IOException {
        return itemsTable.queryFirst(itemRecord -> {
            String itemName = getString(itemRecord, ItemsTable.Cols.NAME);
            double itemPrice = getDouble(itemRecord, ItemsTable.Cols.PRICE);

            return queryName.equals(itemName) && queryPrice == itemPrice;
        });
    }

//    private Optional<Object[]> findPurchase(long queryInvoiceId, long queryClientId, long queryItemId) throws IOException {
//        return purchasesTable.queryFirst(purchaseRecord -> {
//            long invoiceId = getLong(purchaseRecord, PurchasesTable.Cols.INVOICE_ID);
//            long clientId = getLong(purchaseRecord, PurchasesTable.Cols.CLIENT_ID);
//            long itemId = getLong(purchaseRecord, PurchasesTable.Cols.ITEM_ID);
//
//            return  queryInvoiceId == invoiceId && queryClientId == clientId && queryItemId == queryItemId;
//        });
//    }

    public ClusteredIndex getPurchasesTable() {
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
                + purchasesTable.debugPrint()
                + "\nPurchasesGroups:\n"
                + purchaseGroupsTable.debugPrint()
                + "\nclientGroupsRelationTable\n"
                + clientGroupsRelationTable.debugPrint();
    }

    public ClusteredIndex getInvoicesTable() {
        return invoicesTable;
    }

    public ClusteredIndex getClientsTable() {
        return clientsTable;
    }

    public ClusteredIndex getItemsTable() {
        return itemsTable;
    }

    public ClientGroupsRelationTable getClientGroupsRelationTable() {
        return clientGroupsRelationTable;
    }

    public PurchaseGroupsTable getPurchaseGroupsTable() {
        return purchaseGroupsTable;
    }

    private static class MappedIterator<T> implements IoIterator<T> {

        private final IoIterator<Object[]> recordsIterator;
        private final Function<Object[], T> mapper;

        public MappedIterator(IoIterator<Object[]> it, Function<Object[], T> mapper) throws IOException {
            this.recordsIterator = it;
            this.mapper = mapper;
        }

        @Override
        public boolean hasNext() {
            return recordsIterator.hasNext();
        }

        @Override
        public T next() throws IOException {
            return mapper.apply(recordsIterator.next());
        }

    }

    private static <E> List<E> sortedByIndex(List<Pair<E, Integer>> listWithIndex) {
        return listWithIndex.stream()
                .sorted(Comparator.comparingInt(Pair::get2))
                .map(Pair::get1)
                .collect(Collectors.toList());
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
            Object[] invoiceRecord = invoicesIterator.next();
            final long invoiceId = getLong(invoiceRecord, InvoicesTable.Cols.ID);
            final int groupsCount = getInteger(invoiceRecord, InvoicesTable.Cols.GROUPS_COUNT);


            List<Pair<PurchaseGroup, Integer>> groupsWithIndex = new ArrayList<>(groupsCount);

            IoIterator<Object[]> groupsIt = purchaseGroupsTable.queryIndex(invoiceId);
            for (int i = 0; i < groupsCount; i++) {
                if (!groupsIt.hasNext()) {
                    throw new IOException("Missing groups");
                }

                Object[] groupRecord = groupsIt.next();

                groupsWithIndex.add(loadPurchaseGroup(groupRecord));
            }

            return InvoicesTable.getInvoiceModel(sortedByIndex(groupsWithIndex), invoiceRecord);
        }



        private Pair<PurchaseGroup, Integer> loadPurchaseGroup(Object[] groupRecord) throws IOException {
            final long groupId = getLong(groupRecord, PurchaseGroupsTable.Cols.ID);
            final int clientsCount = getInteger(groupRecord, PurchaseGroupsTable.Cols.CLIENTS_COUNT);
            final int purchasesCount = getInteger(groupRecord, PurchaseGroupsTable.Cols.PURCHASES_COUNT);
            final int groupIndex = getInteger(groupRecord, PurchaseGroupsTable.Cols.GROUP_INDEX);
            final long firstClientId = getLong(groupRecord, PurchaseGroupsTable.Cols.FIRST_CLIENT_ID);

            PurchaseGroup group = new PurchaseGroup();
            group.setId(groupId);

            if (clientsCount == 1) {
                group.getClients().add(getClient(firstClientId));
            } else if (clientsCount > 1) {
                List<Pair<Client, Integer>> clientsWithIndex = new ArrayList<>(clientsCount);
                clientsWithIndex.add(Pair.of(getClient(firstClientId), 0));

                IoIterator<Object[]> relationIt = clientGroupsRelationTable.queryIndex(groupId);
                for (int i = 1; i < clientsCount; i++) {
                    if (!relationIt.hasNext()) {
                        throw new IOException("Missing client relation");
                    }

                    Object[] relationRecord = relationIt.next();

                    long clientId = getLong(relationRecord, ClientGroupsRelationTable.Cols.CLIENT_ID);
                    int clientIndex = getInteger(relationRecord, ClientGroupsRelationTable.Cols.CLIENT_INDEX);

                    clientsWithIndex.add(Pair.of(getClient(clientId), clientIndex));
                }

                group.setClients(sortedByIndex(clientsWithIndex));
            }


            List<Pair<Purchase, Integer>> purchasesWithIndex = new ArrayList<>(purchasesCount);

            IoIterator<Object[]> purchasesIt = purchasesTable.queryIndex(groupId);
            for (int i = 0; i < purchasesCount; i++) {
                if (!purchasesIt.hasNext()) {
                    throw new IOException("Missing purchase");
                }

                Object[] purchaseRecord = purchasesIt.next();

                final long itemId = getLong(purchaseRecord, PurchasesTable.Cols.ITEM_ID);
                Item item = getItem(itemId);

                purchasesWithIndex.add(PurchasesTable.getPurchaseModel(item, purchaseRecord));
            }

            group.setPurchases(sortedByIndex(purchasesWithIndex));

            return Pair.of(group, groupIndex);
        }

        private Client getClient(long clientId) throws IOException {
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
                item = ItemsTable.getItemModel(itemsTable.queryIndexFirst(itemId).orElseThrow(() -> new IOException("Item not found for id " + itemId)));
                itemsBuffer.put(itemId, item);
            }

            return item;
        }
    }
}

