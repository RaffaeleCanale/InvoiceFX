package app.io.local;

import app.io.datafile.DataFile;
import app.io.datafile.DataFileTest;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.config.manager.DummyData.DUMMY_SERIALIZER;
import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.16.
 */
public class PartitionFileTest extends DataFileTest {

    @Before
    public void createTmpFile() throws IOException {
        Path path = Files.createTempFile("invoicefx_test_partition_file", ".part");
        tmpFile = path.toFile();

        assertEquals(0, tmpFile.length());
    }

    @After
    public void clearTmpFile() throws IOException {
        if (tmpFile.exists()) {
            Files.delete(tmpFile.toPath());
        }
    }

    private File tmpFile;

    @Override
    protected DataFile getDataFile() {
        return new DirectoryStorage.PartitionFile(tmpFile, DUMMY_SERIALIZER);
    }

}
