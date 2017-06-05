package com.wx.invoicefx.model.save;

import com.nitorcreations.junit.runners.NestedRunner;
import com.wx.invoicefx.io.util.data.DummyModels;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.model.save.table.ClientGroupsRelationTable;
import com.wx.invoicefx.model.save.table.PurchaseGroupsTable;
import com.wx.invoicefx.model.save.table.PurchasesTable;
import com.wx.invoicefx.model.save.table.RecordsHelper;
import com.wx.io.file.FileUtil;
import com.wx.properties.page.ResourcePage;
import com.wx.util.future.IoIterator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wx.invoicefx.io.util.ModelAssert.*;
import static com.wx.invoicefx.io.util.data.DummyModels.generateInvoice;
import static com.wx.invoicefx.model.entities.ModelComparator.deepEquals;
import static com.wx.invoicefx.model.entities.ModelComparator.shallowEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 09.05.17.
 */
@RunWith(NestedRunner.class)
public class SaveManagerTest {

    private static final int INVOICES_TEST_COUNT = 10;

    @BeforeClass
    public static void createTmpDir() throws IOException {
        Path path = Files.createTempDirectory("invoicefx_test_save_manager");
        tmpDir = path.toFile();
    }

    @AfterClass
    public static void removeTmpDir() {
        FileUtil.deleteDir(tmpDir);
    }

    @Before
    public void clearTmpDir() throws IOException {
        for (File file : tmpDir.listFiles()) {
            Files.delete(file.toPath());
        }
    }

    private static File tmpDir;


    private static class TestCase {

        static SaveManager loadSaveManager() throws IOException {
            File mdFile = new File(tmpDir, "tmp.properties");

            ResourcePage page = ResourcePage.builder().fromFile(mdFile).loadOrCreate();
            return new SaveManager(tmpDir, page);
        }

        static List<Invoice> readInvoices() throws IOException {
            return loadSaveManager().getAllInvoices().collect();
        }

        static void write(Invoice invoice) throws IOException {
            write(Collections.singletonList(invoice));
        }

        static void write(List<Invoice> invoices) throws IOException {
            SaveManager s = loadSaveManager();

            for (Invoice invoice : invoices) {
                s.addNewInvoice(invoice);
            }
        }

        static List<Client> readClients() throws IOException {
            return loadSaveManager().getAllClients().collect();
        }

        static List<Item> readItems() throws IOException {
            return loadSaveManager().getAllItems().collect();
        }

        static List<Invoice> generateInvoices() {
            return DummyModels.generateInvoices(INVOICES_TEST_COUNT);
        }

        static Set<Client> getClientsIn(Collection<Invoice> invoices) {
            return new HashSet<>(invoices.stream()
                    .flatMap(i -> i.getPurchaseGroups().stream())
                    .flatMap(g -> g.getClients().stream())
                    .collect(Collectors.toMap(Client::getId, Function.identity(), (c1, c2) -> c1))
                    .values());
        }

        static Set<Item> getItemsIn(Collection<Invoice> invoices) {
            return new HashSet<>(invoices.stream()
                    .flatMap(i -> i.getPurchaseGroups().stream())
                    .flatMap(g -> g.getPurchases().stream())
                    .map(Purchase::getItem)
                    .collect(Collectors.toMap(Item::getId, Function.identity(), (c1, c2) -> c1))
                    .values());
        }

        static List<Client> getClientsInSorted(Collection<Invoice> invoices) {
            List<Client> clients = new ArrayList<>(getClientsIn(invoices));
            Collections.sort(clients, Comparator.comparingLong(Client::getId).reversed());

            return clients;
        }

        static List<Item> getItemsInSorted(Collection<Invoice> invoices) {
            List<Item> items = new ArrayList<>(getItemsIn(invoices));
            Collections.sort(items, Comparator.comparingLong(Item::getId).reversed());

            return items;
        }

        static void readAndAssertEquals(List<Invoice> invoices) throws IOException {
            Collections.sort(invoices, Comparator.comparing(Invoice::getDate).reversed());
            assertInvoicesEquals(invoices, readInvoices());
        }

        static Invoice removeSome(List<Invoice> invoices) {
            return invoices.remove(invoices.size() / 2);
        }
    }


    public class ClientsRead extends TestCase {

        @Test
        public void getAllClients() throws IOException {
            List<Invoice> invoices = generateInvoices();

            write(invoices);

            List<Client> clients = getClientsInSorted(invoices);

            assertClientsEquals(clients, readClients());
        }

    }


    public class InvoiceRead extends TestCase {

        @Test
        public void getAllInvoicesEmpty() throws IOException {
            assertEquals(Collections.emptyList(), readInvoices());
        }

        @Test
        public void getAllInvoices() throws IOException {
            List<Invoice> invoices = generateInvoices();

            write(invoices);

            readAndAssertEquals(invoices);
        }

    }

    public class InvoiceWrite extends TestCase {

        @Test(expected = IllegalArgumentException.class)
        public void insertWithoutId() throws IOException {
            Invoice invoice = generateInvoice();

            invoice.setId(-1);
            write(invoice);
        }

        @Test(expected = IllegalArgumentException.class)
        public void insertInvoiceDup() throws IOException {
            Invoice invoice = generateInvoice();

            write(invoice);
            write(invoice);
        }

    }

    public class InvoiceRemove extends TestCase {

        @Test
        public void removeInvoice() throws IOException {
            List<Invoice> invoices = DummyModels.generateInvoices(2);

            write(invoices);

            Invoice removed = removeSome(invoices);
            loadSaveManager().removeInvoice(removed);

            readAndAssertEquals(invoices);
        }


        @Test
        public void removeInvoiceLast() throws IOException {
            List<Invoice> invoices = generateInvoices();
            Collections.sort(invoices, Comparator.comparing(Invoice::getDate).reversed());

            write(invoices);

            Invoice removed = invoices.remove(invoices.size() - 1);
            loadSaveManager().removeInvoice(removed);

            assertInvoicesEquals(invoices, readInvoices());
        }

        @Test(expected = IOException.class)
        public void removeInvoiceNotFound() throws IOException {
            List<Invoice> invoices = generateInvoices();

            List<Invoice> subList = invoices.subList(1, invoices.size());
            write(subList);

            loadSaveManager().removeInvoice(invoices.get(0));
        }

        public class RemoveCascade extends TestCase {

            private Invoice removed;
            private Set<Long> groupIds;

            @Before
            public void removeSomeInvoice() throws IOException {
                List<Invoice> invoices = generateInvoices();

                write(invoices);

                removed = removeSome(invoices);
                groupIds = removed.getPurchaseGroups().stream().map(PurchaseGroup::getId).collect(Collectors.toSet());

                loadSaveManager().removeInvoice(removed);

                readAndAssertEquals(invoices);
            }


            @Test
            public void cascadePurchaseGroups() throws IOException {
                IoIterator<Object[]> it = loadSaveManager().getPurchaseGroupsTable()
                        .queryColumn(PurchaseGroupsTable.Cols.INVOICE_ID.ordinal(), removed.getId());
                assertFalse(it.hasNext());
            }

            @Test
            public void cascadePurchases() throws IOException {
                IoIterator<Object[]> it = loadSaveManager().getPurchasesTable()
                        .query((row) -> {
                            Long groupId = RecordsHelper.getLong(row, PurchasesTable.Cols.GROUP_ID);
                            return groupIds.contains(groupId);
                        });
                assertFalse(it.hasNext());
            }

            @Test
            public void cascadeClientGroupsRelations() throws IOException {
                IoIterator<Object[]> it = loadSaveManager().getClientGroupsRelationTable()
                        .query((row) -> {
                            Long groupId = RecordsHelper.getLong(row, ClientGroupsRelationTable.Cols.GROUP_ID);
                            return groupIds.contains(groupId);
                        });
                assertFalse(it.hasNext());
            }
        }

        public class RemovePreserve extends TestCase {

            private List<Client> clients;
            private List<Item> items;

            @Before
            public void removeSomeInvoice() throws IOException {
                List<Invoice> invoices = generateInvoices();

                write(invoices);

                clients = getClientsInSorted(invoices);
                items = getItemsInSorted(invoices);

                SaveManager s = loadSaveManager();
                for (Invoice invoice : invoices) {
                    s.removeInvoice(invoice);
                }
            }

            @Test
            public void preserveClients() throws IOException {
                assertClientsEquals(clients, readClients());
            }

            @Test
            public void preserveItems() throws IOException {
                assertItemsEquals(items, readItems());
            }
        }
    }


//    @Test
//    public void removeCascadesOnPurchaseGroups() throws IOException {
//
//    }
//
//    @Test
//    public void removeCascadesOnPurchases() throws IOException {
//
//    }
//
//    @Test
//    public void removeCascadesOnClientGroupRelation() throws IOException {
//
//    }

    @Test
    public void removeNotCascadesOnClient() throws IOException {
//        List<Invoice> invoices = generateInvoices(1);
//
//        write(invoices);
//
//        Invoice removed = invoices.remove(0);
//        Set<Long> clientIds = removed.getPurchaseGroups().stream()
//                .flatMap(g -> g.getClients().stream().map(Client::getId)).collect(Collectors.toSet());
//
//        IoIterator<Object[]> it = initSaveManager().getCLi()
//                .query((row) -> {
//                    Long groupId = RecordsHelper.getLong(row, ClientGroupsRelationTable.Cols.GROUP_ID);
//                    return groupIds.contains(groupId);
//                });
//        assertTrue(it.hasNext());
//
//
//        initSaveManager().removeInvoice(removed);
//
//        it = initSaveManager().getClientGroupsRelationTable()
//                .query((row) -> {
//                    Long groupId = RecordsHelper.getLong(row, ClientGroupsRelationTable.Cols.GROUP_ID);
//                    return groupIds.contains(groupId);
//                });
//        assertFalse(it.hasNext());
    }


//    @Test
//    public void cleanUnreferenced() throws IOException {
//        List<Invoice> invoices = generateInvoices(10);
//
//        write(invoices);
//
//        Client someClient = invoices.get(0).getPurchaseGroups().get(0).getClients().get(0);
//
//        SaveManager s = initSaveManager();
//        for (Invoice invoice : invoices) {
//            if (hasClient(invoice, someClient)) {
//                s.removeInvoice(invoice);
//            }
//        }
//
//
//        List<Client> clients = readClients();
//        assertTrue(hasClientShallow(clients, someClient));
//
//        s.cleanUnreferenced();
//
//        clients = readClients();
//        assertFalse(hasClientShallow(clients, someClient));
//    }

    private boolean hasClient(Invoice invoice, Client client) {
        for (PurchaseGroup group : invoice.getPurchaseGroups()) {
            for (Client otherClient : group.getClients()) {
                if (deepEquals(otherClient, client)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasClientShallow(List<Client> clients, Client client) {
        for (Client c : clients) {
            if (shallowEquals(c, client)) {
                return true;
            }
        }

        return false;
    }

}