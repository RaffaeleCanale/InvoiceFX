package com.wx.invoicefx.model.save;

import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.client.Purchase;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.io.file.FileUtil;
import com.wx.util.future.IoIterator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wx.invoicefx.io.util.DataGenerator.*;
import static com.wx.invoicefx.model.entities.ModelComparator.deepEquals;
import static com.wx.invoicefx.model.entities.ModelComparator.shallowEquals;
import static org.junit.Assert.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 09.05.17.
 */
public class SaveManagerTest {

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

    private static List<Invoice> readInvoices() throws IOException {
        SaveManager s = new SaveManager(tmpDir);

        IoIterator<Invoice> it = s.getAllInvoices();

        List<Invoice> result = new ArrayList<>();
        it.forEachRemaining(result::add);

        return result;
    }

    private static void write(Invoice invoice) throws IOException {
        write(Collections.singletonList(invoice));
    }

    private static void write(List<Invoice> invoices) throws IOException {
        SaveManager s = new SaveManager(tmpDir);

        for (Invoice invoice : invoices) {
            s.addNewInvoice(invoice);
        }
    }

    private static List<Client> readClients() throws IOException {
        SaveManager s = new SaveManager(tmpDir);

        IoIterator<Client> it = s.getAllClients();

        List<Client> result = new ArrayList<>();
        it.forEachRemaining(result::add);

        return result;
    }

    private static File tmpDir;

    @Test
    public void getAllInvoicesEmpty() throws IOException {
        assertEquals(Collections.emptyList(), readInvoices());
    }

    @Test
    public void getAllInvoices() throws IOException {
        List<Invoice> invoices = generateInvoices(10);

        write(invoices);

        Collections.sort(invoices, Comparator.comparing(Invoice::getDate).reversed());
        assertInvoicesEquals(invoices, readInvoices());
    }

    @Test
    public void getAllClients() throws IOException {
        List<Invoice> invoices = generateInvoices(10);

        write(invoices);

        Map<Long, Client> clients = invoices.stream().flatMap(i -> i.getPurchases().stream().map(Purchase::getClient))
                .collect(Collectors.toMap(Client::getId, Function.identity(), (c1, c2) -> c1));

        List<Client> clientsList = new ArrayList<>(clients.values());
        Collections.sort(clientsList, Comparator.comparingLong(Client::getId).reversed());

        assertClientsEquals(clientsList, readClients());
    }

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

    @Test
    public void removeInvoice() throws IOException {
        List<Invoice> invoices = generateInvoices(10);

        write(invoices);

        Invoice removed = invoices.remove(5);
        new SaveManager(tmpDir).removeInvoice(removed);

        Collections.sort(invoices, Comparator.comparing(Invoice::getDate).reversed());
        assertInvoicesEquals(invoices, readInvoices());
    }

    @Test
    public void removeInvoiceLast() throws IOException {
        List<Invoice> invoices = generateInvoices(10);
        Collections.sort(invoices, Comparator.comparing(Invoice::getDate).reversed());

        write(invoices);

        Invoice removed = invoices.remove(9);
        new SaveManager(tmpDir).removeInvoice(removed);

        assertInvoicesEquals(invoices, readInvoices());
    }

    @Test
    public void removeCascadesOnPurchases() throws IOException {
        List<Invoice> invoices = generateInvoices(10);

        write(invoices);

        Invoice removed = invoices.remove(5);

        IoIterator<Object[]> it = new SaveManager(tmpDir).getPurchasesTable().queryIndex(removed.getId());
        assertTrue(it.hasNext());

        new SaveManager(tmpDir).removeInvoice(removed);

        it = new SaveManager(tmpDir).getPurchasesTable().queryIndex(removed.getId());
        assertFalse(it.hasNext());
//        Collections.sort(invoices, Comparator.comparing(Invoice::getDate).reversed());
//        assertInvoicesEquals(invoices, readInvoices());
    }

    @Test(expected = IOException.class)
    public void removeInvoiceNotFound() throws IOException {
        List<Invoice> invoices = generateInvoices(10);

        List<Invoice> subList = invoices.subList(1, invoices.size());
        write(subList);

        new SaveManager(tmpDir).removeInvoice(invoices.get(0));
    }

    @Test
    public void cleanUnreferenced() throws IOException {
        List<Invoice> invoices = generateInvoices(10);

        write(invoices);

        Client someClient = invoices.get(0).getPurchases().get(0).getClient();

        SaveManager s = new SaveManager(tmpDir);
        for (Invoice invoice : invoices) {
            if (hasClient(invoice, someClient)) {
                s.removeInvoice(invoice);
            }
        }


        List<Client> clients = readClients();
        assertTrue(hasClientShallow(clients, someClient));

        s.cleanUnreferenced();

        clients = readClients();
        assertFalse(hasClientShallow(clients, someClient));
    }

    private boolean hasClient(Invoice invoice, Client client) {
        for (Purchase purchase : invoice.getPurchases()) {
            if (deepEquals(purchase.getClient(), client)) {
                return true;
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