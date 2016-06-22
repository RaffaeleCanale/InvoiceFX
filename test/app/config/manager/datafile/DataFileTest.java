package app.config.manager.datafile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static app.config.manager.DummyData.*;
import static app.config.manager.local.DirectoryStorage.PartitionFile;
import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.06.16.
 */
public abstract class DataFileTest {

    private DataFile getPartition() {
        return new PartitionFile(tmpFile, DUMMY_SERIALIZER);
    }

    @Before
    public void createTmpFile() throws IOException {
        Path path = Files.createTempFile("invoicefx_test_partition_file", ".part");
        tmpFile = path.toFile();

        assertEquals(0, tmpFile.length());
    }

    @After
    public void clearTmpFile() throws IOException {
        Files.delete(tmpFile.toPath());
    }

    private File tmpFile;

    @Test
    public void testReadNew() throws IOException {
        List<Object[]> read = getPartition().read();
        assertEquals(Collections.emptyList(), read);
    }


    @Test
    public void testWriteRead() throws IOException {
        List<Object[]> data = Collections.unmodifiableList(generateData(10));

        getPartition().write(data);


        List<Object[]> read = getPartition().read();
        assertDataEquals(data, read);
    }

    @Test
    public void testInsert() throws IOException {
        List<Object[]> data = generateData(10);
        getPartition().write(data);

        Object[] newRow = generateData(1).get(0);
        List<Object[]> expected = new ArrayList<>(data);
        expected.add(newRow);


        getPartition().append(data, newRow);
        assertDataEquals(expected, data);
        assertDataEquals(expected, getPartition().read());
    }

    @Test
    public void testInsertEmpty() throws IOException {
        List<Object[]> data = new ArrayList<>();

        Object[] newRow = generateData(1).get(0);
        List<Object[]> expected = new ArrayList<>(data);
        expected.add(newRow);


        getPartition().append(data, newRow);
        assertDataEquals(expected, data);
        assertDataEquals(expected, getPartition().read());
    }


}