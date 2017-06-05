package com.wx.invoicefx.sync.index;

import com.wx.invoicefx.util.InvalidDataException;
import com.wx.properties.page.PageBuilder;
import com.wx.properties.page.ResourcePage;
import com.wx.util.pair.Pair;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public class IndexTest {

    private static Index sampleIndex(int filesCoount) {
        ResourcePage page = new PageBuilder().get();

        page.setProperty("version", 2.0);
        page.setProperty("branch_version", 1.0);
        page.setProperty("files_count", filesCoount);

        for (int i = 0; i < filesCoount; i++) {
            page.setProperty("files." + i + ".filename", "File " + i);
            page.setProperty("files." + i + ".timestamp", i);
            page.setProperty("files." + i + ".check_sum", new byte[]{(byte) i});
        }

//        page.setProperty("unrelatedProperty", filesCoount);

        return new Index(page);
    }


    @Test
    public void isEmpty() {
        assertTrue(new Index(new PageBuilder().get()).isEmpty());
        assertFalse(sampleIndex(0).isEmpty());

//        ResourcePage page = new PageBuilder().get();
//        page.setProperty("unrelatedProperty", "foo bar");
//        assertTrue(new Index(page).isEmpty());
    }

    @Test
    public void getSetVersion() {
        Index index = sampleIndex(0);

        assertEquals(2.0, index.getVersion(), 0.0);

        index.setVersion(99.0);
        assertEquals(99.0, index.getVersion(), 0.0);

        index.setVersion(1.0);
        assertEquals(1.0, index.getVersion(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidVersion() {
        Index index = sampleIndex(0);

        index.setVersion(0.0);
    }

    @Test
    public void getSetBranchVersion() {
        Index index = sampleIndex(0);

        assertEquals(1.0, index.getBranchVersion(), 0.0);

        index.setBranchVersion(1.5);
        assertEquals(1.5, index.getBranchVersion(), 0.0);

        index.setBranchVersion(2.0);
        assertEquals(2.0, index.getBranchVersion(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidBranchVersion() {
        Index index = sampleIndex(0);

        index.setBranchVersion(2.1);
    }

    @Test
    public void getSetFiles() {
        final int n = 10;
        Index index = sampleIndex(n);

        assertEquals(n, index.getFilesCount());
        for (int i = 0; i < n; i++) {
            FileInfo info = index.getFileInfo(i);

            assertEquals(info, index.getFileInfo(info.getFilename()));

            assertEquals(info.getFilename(), "File " + i);
            assertEquals(info.getTimestamp(), i);
            assertArrayEquals(info.getCheckSum(), new byte[]{(byte) i});
        }

        FileInfo[] newFiles = new FileInfo[n / 2];
        for (int i = 0; i < newFiles.length; i++) {
            newFiles[i] = new FileInfo(String.valueOf(i), -i, new byte[]{(byte) i, (byte) i});
        }

        index.setFiles(newFiles);
        assertEquals(newFiles.length, index.getFilesCount());

        for (int i = 0; i < newFiles.length; i++) {
            FileInfo info = index.getFileInfo(i);
            assertEquals(info, index.getFileInfo(info.getFilename()));

            assertEquals(newFiles[i], info);
        }


        index = new Index(ResourcePage.builder().get());
        index.setFiles(newFiles);
        assertEquals(newFiles.length, index.getFilesCount());

        for (int i = 0; i < newFiles.length; i++) {
            FileInfo info = index.getFileInfo(i);
            assertEquals(info, index.getFileInfo(info.getFilename()));

            assertEquals(newFiles[i], info);
        }
    }

    @Test
    public void copyTest() throws InvalidDataException {
        Index index = sampleIndex(10);
        Index copy = sampleIndex(101);

        copy.setVersion(77);
        copy.setBranchVersion(77);

        index.copyTo(copy);

        copy.testIntegrity();

        assertEquals(index.getVersion(), copy.getVersion(), 0.0);
        assertEquals(0.0, copy.getBranchVersion(), 0.0);
        assertEquals(index.getFilesCount(), copy.getFilesCount());

        for (int i = 0; i < index.getFilesCount(); i++) {
            assertEquals(index.getFileInfo(i), copy.getFileInfo(i));
        }

//        assertEquals(10, index.page.getInt("unrelatedProperty"));
//        assertEquals(101, copy.page.getInt("unrelatedProperty"));
    }

    @Test
    public void integrityTestEmpty() throws InvalidDataException {
        new Index(ResourcePage.builder().get()).testIntegrity();
    }

    @Test
    public void integrityTest() throws InvalidDataException {
        sampleIndex(10).testIntegrity();
    }

    @Test
    public void integrityTest2() throws InvalidDataException {
        Index index = sampleIndex(10);
        index.page.removeProperty("branch_version");

        index.testIntegrity();
    }


    @Test
    public void integrityFailByMissingKey() {
        String[] keysToTest = {"version", "files_count",
                "files.3.filename", "files.4.timestamp", "files.7.check_sum"};

        for (String key : keysToTest) {
            Index index = sampleIndex(10);
            index.page.removeProperty(key);

            try {
                index.testIntegrity();
                throw new AssertionError("Expected a failure when removing: " + key);
            } catch (InvalidDataException e) {
            }
        }
    }

    @Test
    public void integrityFailByInvalidCast() {
        Pair[] values = {
                Pair.of("version", "hello"),
                Pair.of("branch_version", "hello"),
                Pair.of("files_count", "hello"),
                Pair.of("files.4.timestamp", "hello"),
                Pair.of("files.2.check_sum", "hello"),
                Pair.of("files_count", "11"),
                Pair.of("files_count", "9"),
                Pair.of("files_count", "-1"),
                Pair.of("branch_version", "99")
        };


        for (Pair value : values) {
            Index index = sampleIndex(10);
            index.page.setProperty((String) value.get1(), (String) value.get2());

            try {
                index.testIntegrity();
                throw new AssertionError("Expected a failure when setting: " + value);
            } catch (InvalidDataException e) {
            }
        }

        // Same tests but without branch_version
        for (Pair value : values) {
            Index index = sampleIndex(10);
            index.page.removeProperty("branch_version");
            index.page.setProperty((String) value.get1(), (String) value.get2());

            try {
                index.testIntegrity();
                throw new AssertionError("Expected a failure when setting: " + value);
            } catch (InvalidDataException e) {
            }
        }
    }


}