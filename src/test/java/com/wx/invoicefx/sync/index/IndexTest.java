package com.wx.invoicefx.sync.index;

import com.wx.invoicefx.util.io.InvalidDataException;
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

    private static Index sampleIndex(int filesCount) {
        ResourcePage page = new PageBuilder().get();

        page.setProperty("index.version", 2.0);
        page.setProperty("index.base_version", 1.0);
        page.setProperty("index.files_count", filesCount);

        for (int i = 0; i < filesCount; i++) {
            page.setProperty("index.files." + i + ".filename", "File " + i);
            page.setProperty("index.files." + i + ".timestamp", i);
            page.setProperty("index.files." + i + ".check_sum", new byte[]{(byte) i});
        }

        page.setProperty("unrelatedProperty", filesCount);

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
    public void getSetBaseVersion() {
        Index index = sampleIndex(0);

        assertEquals(1.0, index.getBaseVersion(), 0.0);

        index.setBaseVersion(1.5);
        assertEquals(1.5, index.getBaseVersion(), 0.0);

        index.setBaseVersion(2.0);
        assertEquals(2.0, index.getBaseVersion(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidBranchVersion() {
        Index index = sampleIndex(0);

        index.setBaseVersion(2.1);
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
            newFiles[i] = new FileInfo(String.valueOf(i), -i, new byte[]{(byte) i, (byte) i}, (long) i);
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
        copy.setBaseVersion(77);

        index.copyTo(copy);

        copy.testIntegrity();

        assertEquals(index.getVersion(), copy.getVersion(), 0.0);
        assertEquals(0.0, copy.getBaseVersion(), 0.0);
        assertEquals(index.getFilesCount(), copy.getFilesCount());

        for (int i = 0; i < index.getFilesCount(); i++) {
            assertEquals(index.getFileInfo(i), copy.getFileInfo(i));
        }

        assertEquals(10, index.getPage().getInt("unrelatedProperty").get().intValue());
        assertEquals(101, copy.getPage().getInt("unrelatedProperty").get().intValue());
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
        index.getPage().removeProperty("index.base_version");

        index.testIntegrity();
    }


    @Test
    public void integrityFailByMissingKey() {
        String[] keysToTest = {"index.version", "index.files_count",
                "index.files.3.filename", "index.files.4.timestamp", "index.files.7.check_sum"};

        for (String key : keysToTest) {
            Index index = sampleIndex(10);
            index.getPage().removeProperty(key);

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
                Pair.of("index.version", "hello"),
                Pair.of("index.base_version", "hello"),
                Pair.of("index.files_count", "hello"),
                Pair.of("index.files.4.timestamp", "hello"),
                Pair.of("index.files.2.check_sum", "hello"),
                Pair.of("index.files_count", "11"),
//                Pair.of("index.files_count", "9"),
//                Pair.of("index.files_count", "-1"),
                Pair.of("index.base_version", "99")
        };


        for (Pair value : values) {
            Index index = sampleIndex(10);
            index.getPage().setProperty((String) value.get1(), (String) value.get2());

            try {
                index.testIntegrity();
                throw new AssertionError("Expected a failure when setting: " + value);
            } catch (InvalidDataException e) {
            }
        }

        // Same tests but without base_version
        for (Pair value : values) {
            Index index = sampleIndex(10);
            index.getPage().removeProperty("index.base_version");
            index.getPage().setProperty((String) value.get1(), (String) value.get2());

            try {
                index.testIntegrity();
                throw new AssertionError("Expected a failure when setting: " + value);
            } catch (InvalidDataException e) {
            }
        }
    }


}