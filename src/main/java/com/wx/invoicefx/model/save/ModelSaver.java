package com.wx.invoicefx.model.save;


import com.wx.invoicefx.io.file.ClusteredIndex;
import com.wx.invoicefx.model.ModelException;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.entities.item.Vats;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.model.save.table.*;
import com.wx.invoicefx.util.io.LimitIoIterator;
import com.wx.properties.page.ResourcePage;
import com.wx.util.future.IoIterator;
import com.wx.util.pair.Pair;
import com.wx.util.representables.DelimiterEncoder;
import com.wx.util.representables.string.LongRepr;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wx.invoicefx.model.save.ModelSaver.MetadataKeys.GROUPS_PK_PROPERTY;
import static com.wx.invoicefx.model.save.ModelSaver.MetadataKeys.INVOICES_COUNT;
import static com.wx.invoicefx.model.save.ModelSaver.MetadataKeys.INVOICES_PK_PROPERTY;
import static com.wx.invoicefx.model.save.table.ClientsTable.getClientModel;
import static com.wx.invoicefx.model.save.table.ItemsTable.getItemRecord;
import static com.wx.invoicefx.model.save.table.RecordsHelper.*;
import static java.util.Collections.singleton;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class ModelSaver {

    public static final String DEFAULT_METADATA_FILENAME = "md.properties";

    public enum MetadataKeys {
        INVOICES_PK_PROPERTY,
        GROUPS_PK_PROPERTY,
        INVOICES_COUNT;

        public String key() {
            return name().toLowerCase();
        }
    }


    private final InvoicesTable invoicesTable;
    private final ClientsTable clientsTable;
    private final ItemsTable itemsTable;
    private final PurchasesTable purchasesTable;
    private final PurchaseGroupsTable purchaseGroupsTable;
    private final ClientGroupsRelationTable clientGroupsRelationTable;

    private final ResourcePage metadataPage;

    public ModelSaver(File dataDirectory) throws IOException {
        this(dataDirectory, ResourcePage.builder().fromFile(new File(dataDirectory, DEFAULT_METADATA_FILENAME)).loadOrCreate());
    }

    public ModelSaver(File dataDirectory, ResourcePage metadataPage) throws IOException {
        LongRepr caster = new LongRepr();

        this.metadataPage = metadataPage;

        this.invoicesTable = new InvoicesTable(dataDirectory, metadataPage.getProperty(INVOICES_PK_PROPERTY.key(), caster));
        this.clientsTable = new ClientsTable(dataDirectory);
        this.itemsTable = new ItemsTable(dataDirectory);
        this.purchasesTable = new PurchasesTable(dataDirectory);
        this.purchaseGroupsTable = new PurchaseGroupsTable(dataDirectory, metadataPage.getProperty(GROUPS_PK_PROPERTY.key(), caster));
        this.clientGroupsRelationTable = new ClientGroupsRelationTable(dataDirectory);


        recoverProperties();
    }


    public boolean invoiceExists(long id) throws IOException {
        return invoicesTable.queryFirst((record) -> getLong(record, InvoicesTable.Cols.ID) == id).isPresent();
    }

    public long getNextInvoiceId() {
        Long maxId = invoicesTable.getMaxIdProperty().get();
        return maxId != null && maxId > 0 ?
                maxId + 1 :
                1;
    }

    public void testDataCorrupted() throws IOException {
        int invoicesCount = 0;
        long maxInvoiceId = 0;
        long maxGroupId = 0;

        IoIterator<Invoice> it = getAllInvoices();
        while (it.hasNext()) {
            invoicesCount++;

            Invoice next = it.next();
            maxInvoiceId = Math.max(maxInvoiceId, next.getId());
            maxGroupId = Math.max(maxGroupId, next.getPurchaseGroups().stream().mapToLong(PurchaseGroup::getId).max().orElse(0L));
        }

        if (metadataPage.getInt(MetadataKeys.INVOICES_COUNT.key()).orElse(0) != invoicesCount) {
            throw new IOException("Invoices count does not match");
        }
        if (metadataPage.getInt(MetadataKeys.INVOICES_PK_PROPERTY.key()).orElse(0) < maxInvoiceId) {
            throw new IOException("Max invoice id is invalid");
        }
        if (metadataPage.getInt(MetadataKeys.GROUPS_PK_PROPERTY.key()).orElse(0) < maxGroupId) {
            throw new IOException("Groups id is invalid");
        }
    }

    public void invalidate() throws IOException {
        invoicesTable.clearBuffer();
        clientsTable.clearBuffer();
        itemsTable.clearBuffer();
        purchasesTable.clearBuffer();
        purchaseGroupsTable.clearBuffer();
        clientGroupsRelationTable.clearBuffer();

        metadataPage.load();

        recoverProperties();
    }


    //<editor-fold desc="INVOICES">
    public IoIterator<Invoice> getAllInvoices() throws IOException {
        return new InvoicesIterator();
    }

    public void addInvoices(Collection<Invoice> invoices) throws IOException {
        for (Invoice invoice : invoices) {
            addInvoice_helper(invoice);
        }

        flush();
    }

    public final void addInvoice(Invoice invoice) throws IOException {
        addInvoices(singleton(invoice));
    }

    public boolean removeInvoicesById(Collection<Long> invoicesId) throws IOException {
        boolean changed = false;

        for (Long id : invoicesId) {
            if (removeInvoice_helper(id)) {
                changed = true;
            }
        }

        if (changed) {
            flush();
        }

        return changed;
    }

    public final boolean removeInvoices(Collection<Invoice> invoices) throws IOException {
        return removeInvoicesById(invoices.stream().map(Invoice::getId).collect(Collectors.toList()));
    }

    public final boolean removeInvoice(Invoice invoice) throws IOException {
        return removeInvoices(singleton(invoice));
    }

    public final boolean removeInvoice(long invoiceId) throws IOException {
        return removeInvoicesById(singleton(invoiceId));
    }

    //</editor-fold>


    //<editor-fold desc="CLIENTS">
    public IoIterator<Client> getAllClients() throws IOException {
        return new MappedIterator<>(clientsTable.iterator(), ClientsTable::getClientModel);
    }

    public IoIterator<Client> getAllClients(int limit) throws IOException {
        return new MappedIterator<>(new LimitIoIterator<>(clientsTable.iterator(), limit), ClientsTable::getClientModel);
    }
    //</editor-fold>


    //<editor-fold desc="ITEMS">

    // GET

    public IoIterator<String> getAllAddresses() throws IOException {
        return new MappedIterator<>(invoicesTable.iterator(), row -> getString(row, InvoicesTable.Cols.ADDRESS));
    }

    public IoIterator<Item> getAllActiveItems() throws IOException {
        return new MappedIterator<>(itemsTable.query((row) -> getBoolean(row, ItemsTable.Cols.IS_ACTIVE)), ItemsTable::getItemModel);
    }

    public IoIterator<Item> getAllItems() throws IOException {
        return new MappedIterator<>(itemsTable.iterator(), ItemsTable::getItemModel);
    }

    public IoIterator<Item> getAllItems(int limit) throws IOException {
        return new MappedIterator<>(new LimitIoIterator<>(itemsTable.iterator(), limit), ItemsTable::getItemModel);
    }

    public final boolean isReferenced(Item item) throws IOException {
        return isReferenced(item.getId());
    }

    public boolean isReferenced(long itemId) throws IOException {
        return purchasesTable.queryColumn(PurchasesTable.Cols.ITEM_ID, itemId).hasNext();
    }

    // ADD

    public void addItems(Collection<Item> items) throws IOException {
        for (Item item : items) {
            addItem_helper(item);
        }

        flush(itemsTable);
    }

    public final void addItem(Item item) throws IOException {
        addItems(singleton(item));
    }



    // UPDATE

    public boolean updateItems(Collection<Item> items) throws IOException {
        boolean changed = false;

        for (Item item : items) {
            if (updateItem_helper(item)) {
                changed = true;
            }
        }

        if (changed) {
            flush(itemsTable);
        }

        return changed;
    }


    public final boolean updateItem(Item item) throws IOException {
        return updateItems(singleton(item));
    }

    // REMOVE

    public boolean removeItemsById(Collection<Long> itemsId) throws IOException {
        boolean changed = false;

        for (Long id : itemsId) {
            if (removeItem_helper(id)) {
                changed = true;
            }
        }

        if (changed) {
            flush(itemsTable);
        }

        return changed;
    }

    public final boolean removeItems(Collection<Item> items) throws IOException {
        return removeItemsById(items.stream().map(Item::getId).collect(Collectors.toList()));
    }

    public final boolean removeItem(Item item) throws IOException {
        return removeItems(singleton(item));
    }

    public final boolean removeItem(long itemId) throws IOException {
        return removeItemsById(singleton(itemId));
    }

    public final boolean removeOrDisable(Collection<Item> items) throws IOException {
        List<Item> itemsToRemove = new LinkedList<>();
        List<Item> itemsToUpdate = new LinkedList<>();

        for (Item item : items) {
            if (isReferenced(item.getId())) {
                item.setActive(false);
                itemsToUpdate.add(item);
            } else {
                itemsToRemove.add(item);
            }
        }

        boolean anyRemoved = removeItems(itemsToRemove);
        boolean anyUpdated = updateItems(itemsToUpdate);

        return anyRemoved || anyUpdated;
    }

    public final boolean removeOrDisable(Item item) throws IOException {
        if (isReferenced(item.getId())) {
            item.setActive(false);
            return updateItem(item);
        } else {
            return removeItem(item);
        }
    }

    public final boolean cleanUpInactiveItems() throws IOException {
        IoIterator<Item> items = getAllItems();
        Set<Long> itemIdsToRemove = new HashSet<>();

        while (items.hasNext()) {
            Item item = items.next();

            if (!isReferenced(item.getId()) && !item.isActive()) {
                itemIdsToRemove.add(item.getId());
            }
        }

        return removeItemsById(itemIdsToRemove);
    }

    public final void duplicateActiveItems(Vats updatedVats) throws IOException {
        List<Item> activeItems = getAllActiveItems().collect();

        List<Item> itemsToUpdate = new ArrayList<>();
        List<Item> itemsToAdd = new ArrayList<>();


        for (Item item : activeItems) {
            Optional<Vat> newVat = updatedVats.getVat(item.getVat().getCategory());
            if (newVat.isPresent()) {
                if (isReferenced(item)) {
                    item = Item.copy(item);
                    item.setVat(newVat.get());

                    itemsToAdd.add(item);
                } else {
                    item.setVat(newVat.get());
                    itemsToUpdate.add(item);
                }
            }
        }

        addItems(itemsToAdd);
        updateItems(itemsToUpdate);
    }

    //</editor-fold>


    //<editor-fold desc="Getters">
    public ClusteredIndex getPurchasesTable() {
        return purchasesTable;
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

    public ResourcePage getMetadataPage() {
        return metadataPage;
    }
    //</editor-fold>

    protected void onFileChanged() {
    }

    private void flush() throws IOException {
        invoicesTable.flush();
        clientsTable.flush();
        itemsTable.flush();
        purchasesTable.flush();
        purchaseGroupsTable.flush();
        clientGroupsRelationTable.flush();

        metadataPage.save();

        onFileChanged();
    }

    private void flush(ClusteredIndex singleTable) throws IOException {
        singleTable.flush();

        onFileChanged();
    }


    //<editor-fold desc="Helpers">
    private void addInvoice_helper(Invoice invoice) throws IOException {
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

        int currentCount = metadataPage.getInt(INVOICES_COUNT.key()).orElse(0);
        metadataPage.setProperty(INVOICES_COUNT.key(), currentCount + 1);
    }

    private boolean removeInvoice_helper(long invoiceId) throws IOException {
        if (invoiceId <= 0) {
            throw new IllegalArgumentException("Invoice has no id!");
        }

        boolean removed = invoicesTable.removeFirst(r -> (long) r[0] == invoiceId);
        if (!removed) {
            return false;
        }

        IoIterator<Object[]> groupsIt = purchaseGroupsTable.queryIndex(invoiceId);
        while (groupsIt.hasNext()) {
            Object[] groupRecord = groupsIt.next();


            long groupId = getLong(groupRecord, PurchaseGroupsTable.Cols.ID);
            int purchasesCount = getInteger(groupRecord, PurchaseGroupsTable.Cols.PURCHASES_COUNT);
            int clientsCount = getInteger(groupRecord, PurchaseGroupsTable.Cols.CLIENTS_COUNT);

            if (purchasesCount > 0) {
                IoIterator<Object[]> it = purchasesTable.queryIndex(groupId);
                for (int i = 0; i < purchasesCount && it.hasNext(); i++) {
                    it.next();
                    it.remove();
                }
            }

            if (clientsCount > 1) {
                IoIterator<Object[]> it = clientGroupsRelationTable.queryIndex(groupId);
                for (int i = 1; i < clientsCount && it.hasNext(); i++) {
                    it.next();
                    it.remove();
                }
            }

            groupsIt.remove();
        }

        int currentCount = metadataPage.getInt(INVOICES_COUNT.key()).orElse(0);
        metadataPage.setProperty(INVOICES_COUNT.key(), currentCount - 1);

        return true;
    }

    private void addItem_helper(Item item) throws IOException {
        long id = itemsTable.assignUniqueIdAndInsert(ItemsTable.getItemRecord(item));
        item.setId(id);
    }

    private boolean updateItem_helper(Item item) throws IOException {
        Object[] itemRecord = ItemsTable.getItemRecord(item);

        return itemsTable.update(ItemsTable.Cols.ID, item.getId(), itemRecord);
    }

    private boolean removeItem_helper(long itemId) throws IOException {
        IoIterator<Object[]> it = itemsTable.iterator();

        while (it.hasNext()) {
            Long rowItemId = getLong(it.next(), ItemsTable.Cols.ID);
            if (rowItemId == itemId) {
                it.remove();
                return true;
            }
        }

        return false;
    }
    //</editor-fold>

    //<editor-fold desc="Util methods">
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

        Optional<Object[]> existing = findItem(item.getName(), item.getPrice(), item.getVat());

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

    private Optional<Object[]> findItem(String queryName, double queryPrice, Vat queryVat) throws IOException {
        return itemsTable.queryFirst(itemRecord -> {
            String itemName = getString(itemRecord, ItemsTable.Cols.NAME);
            double itemPrice = getDouble(itemRecord, ItemsTable.Cols.PRICE);
            double itemVat = getDouble(itemRecord, ItemsTable.Cols.VAT);
            byte itemVatCategory = getByte(itemRecord, ItemsTable.Cols.VAT_CATEGORY);

            return queryName.equals(itemName) && queryPrice == itemPrice && queryVat.getValue() == itemVat && queryVat.getCategory() == itemVatCategory;
        });
    }

    private void recoverInvoicesCount() throws IOException {
        IoIterator<Object[]> it = invoicesTable.iterator();
        int count = 0;

        while (it.hasNext()) {
            it.next();
            count++;
        }

        metadataPage.setProperty(INVOICES_COUNT.key(), count);
    }

    private void recoverProperties() throws IOException {
        boolean saveNeeded = false;
        if (!metadataPage.containsKey(INVOICES_PK_PROPERTY.key())) {
            invoicesTable.recoverMaxId();
            saveNeeded = true;
        }
        if (!metadataPage.containsKey(GROUPS_PK_PROPERTY.key())) {
            purchaseGroupsTable.recoverMaxId();
            saveNeeded = true;
        }
        if (!metadataPage.containsKey(INVOICES_COUNT.key())) {
            recoverInvoicesCount();
            saveNeeded = true;
        }

        if (saveNeeded) {
            metadataPage.save();
        }
    }
    //</editor-fold>



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

                groupsWithIndex.add(loadPurchaseGroup(groupRecord, invoiceId));
            }

            return InvoicesTable.getInvoiceModel(sortedByIndex(groupsWithIndex), invoiceRecord);
        }


        private Pair<PurchaseGroup, Integer> loadPurchaseGroup(Object[] groupRecord, long invoiceId) throws IOException {
            final long groupId = getLong(groupRecord, PurchaseGroupsTable.Cols.ID);
            final int clientsCount = getInteger(groupRecord, PurchaseGroupsTable.Cols.CLIENTS_COUNT);
            final int purchasesCount = getInteger(groupRecord, PurchaseGroupsTable.Cols.PURCHASES_COUNT);
            final int groupIndex = getInteger(groupRecord, PurchaseGroupsTable.Cols.GROUP_INDEX);
            final long firstClientId = getLong(groupRecord, PurchaseGroupsTable.Cols.FIRST_CLIENT_ID);
            final String encodedStopWords = getString(groupRecord, PurchaseGroupsTable.Cols.STOP_WORDS);

            PurchaseGroup group = new PurchaseGroup();
            group.setId(groupId);
            group.setStopWords(DelimiterEncoder.autoDecode(encodedStopWords));

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
                    System.out.println("groupId = " + groupId);
                    throw new ModelException("Missing purchase", invoiceId);
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
            if (clientId == Client.EMPTY_CLIENT.getId()) {
                return Client.EMPTY_CLIENT;
            }
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

