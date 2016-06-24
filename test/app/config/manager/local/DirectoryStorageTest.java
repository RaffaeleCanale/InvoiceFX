package app.config.manager.local;

import app.config.manager.storage.PartitionedStorage;
import app.config.manager.storage.PartitionedStorageTest;
import com.wx.io.file.FileUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.16.
 */
public class DirectoryStorageTest extends PartitionedStorageTest {

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


    @Override
    protected PartitionedStorage getPartitionedStorage(RecordSerializer serializer) {
        return new DirectoryStorage(serializer, tmpDir, "test");
    }
}
