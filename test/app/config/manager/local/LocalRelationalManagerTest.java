package app.config.manager.local;

import app.config.manager.DummyData;
import app.config.manager.DataGenerator;
import app.config.manager.datafile.ClusteredIndex;
import app.model.invoice.Invoice;
import com.wx.io.file.FileUtil;
import com.wx.util.future.IoIterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static app.model.ModelAssert.assertInvoiceEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class LocalRelationalManagerTest {

    private static <T> List<T> collect(IoIterator<T> it) throws IOException {
        List<T> result = new ArrayList<>();
        it.forEachRemaining(result::add);

        return result;
    }

    @BeforeClass
    public static void createTmpDir() throws IOException {
        Path path = Files.createTempDirectory("invoicefx_test_local_relational_manager");
        tmpDir = path.toFile();
    }

    @AfterClass
    public static void removeTmpDir() {
        FileUtil.deleteDir(tmpDir);
    }

    @After
    public void clearTmpDir() throws IOException {
        for (File file : tmpDir.listFiles()) {
            Files.delete(file.toPath());
        }
    }

    private static File tmpDir;


    protected LocalRelationalManager createManager() {
        return new LocalRelationalManager(
                new ClusteredIndex(new DummyData.DummyPartitionedStorage(), 10, 2),
                new ClusteredIndex(new DummyData.DummyPartitionedStorage(), 10, 0),
                new ClusteredIndex(new DummyData.DummyPartitionedStorage(), 10, 0),
                new ClusteredIndex(new DummyData.DummyPartitionedStorage(), 10, 0)
        );
    }

    @Test
    public void test1() throws IOException {
        LocalRelationalManager manager = createManager();

        List<Invoice> invoices = DataGenerator.generateInvoice(200);
        Collections.sort(invoices, Comparator.comparing(Invoice::getDate));

        for (Invoice invoice : invoices) {
            manager.addNewInvoice(invoice);
        }


        List<Invoice> read = collect(manager.getAllInvoices());
        Collections.reverse(invoices);


        assertInvoiceEquals(invoices, read);
    }
}