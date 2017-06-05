package com.wx.invoicefx.sync;

import com.nitorcreations.junit.runners.NestedRunner;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.sync.repo.Local;
import com.wx.invoicefx.sync.repo.Remote;
import com.wx.invoicefx.util.InvalidDataException;
import com.wx.io.TextAccessor;
import com.wx.io.file.FileUtil;
import com.wx.properties.page.ResourcePage;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

import static com.wx.invoicefx.sync.SyncManager.Status.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
@RunWith(NestedRunner.class)
public class SyncManagerTest {

    private static final int LINES_COUNT = 2048;

    @BeforeClass
    public static void createTmpDir() throws IOException {
        Path path = Files.createTempDirectory("invoicefx_test_sync_manager_local");
        localTmpDir = path.toFile();

        path = Files.createTempDirectory("invoicefx_test_sync_manager_remote");
        remoteTmpDir = path.toFile();
    }

    @AfterClass
    public static void removeTmpDir() {
        FileUtil.deleteDir(localTmpDir);
        FileUtil.deleteDir(remoteTmpDir);
    }

    @After
    public void clearTmpDir() throws IOException {
        for (File file : localTmpDir.listFiles()) {
            Files.delete(file.toPath());
        }

        for (File file : remoteTmpDir.listFiles()) {
            Files.delete(file.toPath());
        }
    }


    private static File localTmpDir;
    private static File remoteTmpDir;


    private static ResourcePage createDummyPage() {
        return ResourcePage.builder()
                .fromLinks(null, ByteArrayOutputStream::new)
                .get();
    }

    private static class TestCase {

        final RepoData data1 = new RepoData(10, "variant 1");
        final RepoData data2 = new RepoData(15, "variant 2");

        SyncManager syncManager;
        Local local;
        TestRemote remote;

        Index localIndex, remoteIndex;


        public void initEmptyLocal() throws IOException {
            local = new Local(createDummyPage(), localTmpDir);
            localIndex = local.getIndex();

            tryInitSyncManager();
        }

        public void initLocal(RepoData data, double version, double branchVersion) throws IOException {
            local = data.initAtWithIndex(localTmpDir, Local::new);
            localIndex = local.getIndex();

            localIndex.setVersion(version);
            localIndex.setBranchVersion(branchVersion);

            tryInitSyncManager();
        }

        public void initEmptyRemote() throws IOException {
            remote = new TestRemote(createDummyPage(), remoteTmpDir);
            remoteIndex = remote.getIndex();

            tryInitSyncManager();
        }

        public void initRemote(RepoData data, double version) throws IOException {
            remote = data.initAtWithIndex(remoteTmpDir, TestRemote::new);
            remoteIndex = remote.getIndex();

            remoteIndex.setVersion(version);

            tryInitSyncManager();
        }

        private void tryInitSyncManager() throws IOException {
            if (local != null && remote != null) {
                syncManager = new SyncManager(local, remote);
            }
        }


    }


    public class NoRemote extends TestCase {

        @Before
        public void setUp() throws IOException {
            initLocal(data1, 10.0, 10.0);
            initEmptyRemote();

            remote.isReachable = false;
        }

        @Test
        public void statusTest() throws InvalidDataException {
            assertEquals(REMOTE_UNREACHABLE, syncManager.getStatus());
        }

        @Test(expected = IOException.class)
        public void pull() throws IOException, InvalidDataException {
            syncManager.pull();
        }

        @Test(expected = IOException.class)
        public void push() throws IOException, InvalidDataException {
            syncManager.push();
        }
    }

    public class EmptyRemote extends TestCase {

        @Before
        public void setUp() throws IOException {
            initLocal(data1, 10.0, 10.0);
            initEmptyRemote();
        }

        @Test
        public void statusTest() throws InvalidDataException {
            assertEquals(NEED_PUSH, syncManager.getStatus());
        }

        @Test
        public void pull() throws IOException, InvalidDataException {
            assertEquals(0, syncManager.pull());
        }

        @Test
        public void push() throws IOException, InvalidDataException {
            assertThat(syncManager.push(), greaterThan(0));
            assertEquals(10.0, remoteIndex.getVersion(), 0.0);

            data1.assertDirContent(remoteTmpDir);
        }
    }

    public class EmptyLocal extends TestCase {

        @Before
        public void setUp() throws IOException {
            initEmptyLocal();
            initRemote(data1, 10.0);
        }

        @Test
        public void statusTest() throws InvalidDataException {
            assertEquals(NEED_PULL, syncManager.getStatus());
        }

        @Test
        public void pull() throws IOException, InvalidDataException {
            assertThat(syncManager.pull(), greaterThan(0));

            assertEquals(10.0, localIndex.getVersion(), 0.0);
            assertEquals(10.0, localIndex.getBranchVersion(), 0.0);

            data1.assertDirContent(localTmpDir);
        }

        @Test(expected = IllegalArgumentException.class)
        public void push() throws IOException, InvalidDataException {
            syncManager.push();
        }

        @Test
        public void pushForce() throws IOException, InvalidDataException {
            assertThat(syncManager.pushForce(), greaterThan(0));
            double incrementVersion = 10.0 + Index.VERSION_INCREMENT;

            assertEquals(incrementVersion, localIndex.getVersion(), 0.0);
            assertEquals(incrementVersion, localIndex.getBranchVersion(), 0.0);
            assertEquals(incrementVersion, remoteIndex.getVersion(), 0.0);

            assertEquals(0, localTmpDir.list().length);
            assertEquals(0, remoteTmpDir.list().length);
        }
    }

    public class SameVersion extends TestCase {

        @Before
        public void setUp() throws IOException {
            initLocal(data1, 10.0, 10.0);
            initRemote(data1, 10.0);
        }

        @Test
        public void statusTest() throws InvalidDataException {
            assertEquals(IS_UP_TO_DATE, syncManager.getStatus());
        }

        @Test
        public void pull() throws IOException, InvalidDataException {
            assertEquals(0, syncManager.pull());
        }

        @Test
        public void push() throws IOException, InvalidDataException {
            assertEquals(0, syncManager.pull());
        }

    }

    public class LocalAhead extends TestCase {

        @Before
        public void setUp() throws IOException {
            initLocal(data1, 11.0, 10.0);
            initRemote(data2, 10.0);
        }

        @Test
        public void statusTest() throws InvalidDataException {
            assertEquals(NEED_PUSH, syncManager.getStatus());
        }

        @Test
        public void pull() throws IOException, InvalidDataException {
            assertThat(syncManager.pull(), greaterThan(0));
            assertEquals(10.0, localIndex.getVersion(), 0.0);

            data2.assertDirContent(localTmpDir);
            data2.assertDirContent(remoteTmpDir);

            assertEquals(IS_UP_TO_DATE, syncManager.getStatus());
        }

        @Test
        public void push() throws IOException, InvalidDataException {
            assertThat(syncManager.push(), greaterThan(0));
            assertEquals(11.0, localIndex.getVersion(), 0.0);

            data1.assertDirContent(localTmpDir);
            data1.assertDirContent(remoteTmpDir);

            assertEquals(IS_UP_TO_DATE, syncManager.getStatus());
        }
    }

    public class RemoteAhead extends TestCase {

        @Before
        public void setUp() throws IOException {
            initLocal(data1, 11.0, 11.0);
            initRemote(data2, 16.0);
        }

        @Test
        public void statusTest() throws InvalidDataException {
            assertEquals(NEED_PULL, syncManager.getStatus());
        }

        @Test
        public void pull() throws IOException, InvalidDataException {
            assertThat(syncManager.pull(), greaterThan(0));
            assertEquals(16.0, localIndex.getVersion(), 0.0);

            data2.assertDirContent(localTmpDir);
            data2.assertDirContent(remoteTmpDir);

            assertEquals(IS_UP_TO_DATE, syncManager.getStatus());
        }

        @Test(expected = IllegalArgumentException.class)
        public void push() throws IOException, InvalidDataException {
            syncManager.push();
        }

        @Test
        public void pushForce() throws IOException, InvalidDataException {
            assertThat(syncManager.pushForce(), greaterThan(0));
            double incrementVersion = 16.0 + Index.VERSION_INCREMENT;

            assertEquals(incrementVersion, localIndex.getVersion(), 0.0);
            assertEquals(incrementVersion, localIndex.getBranchVersion(), 0.0);
            assertEquals(incrementVersion, remoteIndex.getVersion(), 0.0);

            data1.assertDirContent(localTmpDir);
            data1.assertDirContent(remoteTmpDir);
        }

    }

    public class VersionConflict extends TestCase {

        @Before
        public void setUp() throws IOException {
            initLocal(data1, 12.0, 11.0);
            initRemote(data2, 15.0);
        }

        @Test
        public void statusTest() throws InvalidDataException {
            assertEquals(VERSION_CONFLICT, syncManager.getStatus());
        }

        @Test
        public void pull() throws IOException, InvalidDataException {
            assertThat(syncManager.pull(), greaterThan(0));
            assertEquals(15.0, localIndex.getVersion(), 0.0);
            assertEquals(15.0, localIndex.getBranchVersion(), 0.0);

            data2.assertDirContent(localTmpDir);
            data2.assertDirContent(remoteTmpDir);

            assertEquals(IS_UP_TO_DATE, syncManager.getStatus());
        }

        @Test(expected = IllegalArgumentException.class)
        public void push() throws IOException, InvalidDataException {
            syncManager.push();
        }

        @Test
        public void pushForce() throws IOException, InvalidDataException {
            assertThat(syncManager.pushForce(), greaterThan(0));
            double incrementVersion = 15.0 + Index.VERSION_INCREMENT;

            assertEquals(incrementVersion, localIndex.getVersion(), 0.0);
            assertEquals(incrementVersion, localIndex.getBranchVersion(), 0.0);
            assertEquals(incrementVersion, remoteIndex.getVersion(), 0.0);

            data1.assertDirContent(localTmpDir);
            data1.assertDirContent(remoteTmpDir);
        }
    }

    public class VersionConflict2 extends TestCase {

        @Before
        public void setUp() throws IOException {
            initLocal(data1, 19.0, 11.0);
            initRemote(data2, 15.0);
        }

        @Test
        public void statusTest() throws InvalidDataException {
            assertEquals(VERSION_CONFLICT, syncManager.getStatus());
        }

        @Test
        public void pull() throws IOException, InvalidDataException {
            assertThat(syncManager.pull(), greaterThan(0));
            assertEquals(15.0, localIndex.getVersion(), 0.0);
            assertEquals(15.0, localIndex.getBranchVersion(), 0.0);

            data2.assertDirContent(localTmpDir);
            data2.assertDirContent(remoteTmpDir);
        }

        @Test(expected = IllegalArgumentException.class)
        public void push() throws IOException, InvalidDataException {
            syncManager.push();
        }

        @Test
        public void pushForce() throws IOException, InvalidDataException {
            assertThat(syncManager.pushForce(), greaterThan(0));

            assertEquals(19.0, localIndex.getVersion(), 0.0);
            assertEquals(19.0, localIndex.getBranchVersion(), 0.0);
            assertEquals(19.0, remoteIndex.getVersion(), 0.0);

            data1.assertDirContent(localTmpDir);
            data1.assertDirContent(remoteTmpDir);
        }
    }

    public class InvalidVersions extends TestCase {

        @Before
        public void setUp() throws IOException {
            initLocal(data1, 12.0, 12.0);
            initRemote(data2, 10.0);
        }

        @Test(expected = InvalidDataException.class)
        public void statusTest() throws InvalidDataException {
            assertEquals(VERSION_CONFLICT, syncManager.getStatus());
        }

        @Test(expected = InvalidDataException.class)
        public void pull() throws IOException, InvalidDataException {
            syncManager.pull();
        }

        @Test(expected = InvalidDataException.class)
        public void push() throws IOException, InvalidDataException {
            syncManager.push();
        }

    }

    private static class TestRemote extends Remote {

        private final File dir;
        private boolean isReachable = true;

        public TestRemote(ResourcePage index, File dir) {
            super(index);
            this.dir = dir;
        }

        @Override
        public void downloadFile(String filename, File destination) throws IOException {
            FileUtil.copyFile(new File(dir, filename), destination);
        }

        @Override
        public void uploadFile(String filename, File file) throws IOException {
            FileUtil.copyFile(file, new File(dir, filename));
        }

        @Override
        public void removeFile(String filename) throws IOException {
            new File(dir, filename).delete();
        }

        @Override
        public boolean isReachable() {
            return isReachable;
        }
    }

    private static class RepoData {

        private final int numberOfFiles;
        private final String variant;

        private RepoData(int numberOfFiles, String variant) {
            this.numberOfFiles = numberOfFiles;
            this.variant = variant;
        }

        void initAt(File dir) throws IOException {
            for (int i = 0; i < numberOfFiles; i++) {
                File file = new File(dir, "file_" + i + ".txt");

                try (TextAccessor accessor = new TextAccessor().setOut(file, false)) {
                    for (int j = 0; j < LINES_COUNT; j++) {
                        accessor.write(variant + j);
                    }
                }
            }
        }

        <T> T initAtWithIndex(File dir, BiFunction<ResourcePage, File, T> repo) throws IOException {
            initAt(dir);

            ResourcePage page = createDummyPage();
            Local local = new Local(page, dir);
            local.createIndex();

            return repo.apply(page, dir);
        }

        void assertDirContent(File dir) throws IOException {
            File[] files = dir.listFiles();
            assertEquals(numberOfFiles, files.length);


            for (int i = 0; i < numberOfFiles; i++) {
                File file = new File(dir, "file_" + i + ".txt");

                assertTrue(file.exists());
                try (TextAccessor accessor = new TextAccessor().setIn(file)) {
                    for (int j = 0; j < LINES_COUNT; j++) {
                        assertEquals(variant + j, accessor.readLine());
                    }
                    assertNull(accessor.readLine());
                }
            }
        }

    }
}