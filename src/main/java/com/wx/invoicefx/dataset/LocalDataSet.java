package com.wx.invoicefx.dataset;

import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.sync.AbstractFileSystem;
import com.wx.invoicefx.sync.index.FileInfo;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.util.io.InvalidDataException;
import com.wx.io.Accessor;
import com.wx.io.file.FileUtil;
import com.wx.properties.PropertiesManager;
import com.wx.properties.page.ResourcePage;
import com.wx.util.log.LogHelper;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 08.07.17.
 */
public abstract class LocalDataSet extends DataSet {

    private static final Logger LOG = LogHelper.getLogger(LocalDataSet.class);

    protected final File dataDirectory;

    public LocalDataSet(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public File getDataDirectory() {
        return dataDirectory;
    }

    @Override
    protected ResourcePage getIndexPage() {
        File indexFile = getFile(DEFAULT_INDEX_FILENAME);
        return ResourcePage.builder().fromFile(indexFile).get();
    }

    @Override
    public boolean isReachable() {
        return getIndex() != null;
    }

    @Override
    protected AbstractFileSystem accessFileSystem0() {
        return new LocalFileSystem();
    }


    protected File getFile(String filename) {
        return new File(dataDirectory, filename);
    }

    protected File[] getDataSetFiles() {
        return dataDirectory.listFiles((dir, name) -> !name.equals(DEFAULT_INDEX_FILENAME));
    }

    @Override
    protected void populateIndex(Index index) throws IOException {
        String author = AppResources.getComputerName();

        if (!index.isEmpty()) {
            throw new IllegalArgumentException("Index already exists");
        }

        File[] files = getDataSetFiles();
        if (files == null) {
            files = new File[0];
        }

        FileInfo[] infos = new FileInfo[files.length];

        for (int i = 0; i < files.length; i++) {
            infos[i] = new FileInfo(files[i]);
        }
        index.setFiles(infos);
        index.setVersion(0.0);

        if (infos.length > 0) {
            index.incrementVersion();
        }

        index.setLastModified(author, new Date());
    }

    public boolean updateIndex() throws IOException {
        String author = AppResources.getComputerName();

        boolean changed = false;
        Index index = getIndex();

        File[] files = getDataSetFiles();
        if (files == null) {
            files = new File[0];
        }

        final int newFilesCount = files.length;

        // STEP 1 - Gather file infos
        FileInfo[] infos = new FileInfo[newFilesCount];

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileInfo info = index.getFileInfo(file.getName());

            if (info == null || !info.matches(file)) {
                info = new FileInfo(file);
                changed = true;
            }

            infos[i] = info;
        }


        if (index.getFilesCount() != infos.length) {
            changed = true;
        }

        if (changed) {
            index.setFiles(infos);
            index.incrementVersion();
            index.setLastModified(author, new Date());
        }

        if (changed) {
            index.save();
        }

        return changed;
    }

    protected void testDataIntegrityThroughIndex() throws InvalidDataException {
        Index index = getIndex();

        File[] files = getDataSetFiles();
        if (files == null) {
            files = new File[0];
        }

        if (files.length != index.getFilesCount()) {
            throw new InvalidDataException("Expected " + index.getFilesCount() + " files but found " + files.length);
        }

        for (File file : files) {
            FileInfo info = index.getFileInfo(file.getName());
            if (info == null) {
                throw new InvalidDataException("File missing in index: " + file.getName());
            }

            if (info.getTimestamp() != file.lastModified()) {
                try {
                    byte[] checksum = FileUtil.checkSum(file);

                    if (!Arrays.equals(checksum, info.getCheckSum())) {
                        throw new InvalidDataException("File checksum does not match: " + file.getName());
                    }
                } catch (IOException e) {
                    throw new InvalidDataException(e);
                }
            }
        }
    }

    private class LocalFileSystem implements AbstractFileSystem {

        @Override
        public void clear() throws IOException {
            File[] files = dataDirectory.listFiles();

            if (files != null) {
                for (File file : files) {
                    boolean success = file.isDirectory() ?
                            FileUtil.deleteDir(file) :
                            file.delete();
                    if (!success) {
                        throw new IOException("Failed to remove " + file);
                    }
                }
            }
        }

        @Override
        public InputStream read(String filename) throws IOException {
            return new BufferedInputStream(new FileInputStream(getFile(filename)));
        }

        @Override
        public void write(String filename, InputStream input) throws IOException {
            try (Accessor accessor = new Accessor()
                    .setIn(input)
                    .setOut(getFile(filename))) {
                accessor.pourInOut();
            }
        }

        @Override
        public void remove(String filename) throws IOException {
            if (!getFile(filename).delete()) {
                throw new IOException("Failed to delete " + filename);
            }
        }

        @Override
        public void close() {
        }
    }

}
