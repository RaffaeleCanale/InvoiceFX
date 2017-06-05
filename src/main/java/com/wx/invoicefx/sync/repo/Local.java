package com.wx.invoicefx.sync.repo;

import com.wx.invoicefx.sync.index.FileInfo;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.util.InvalidDataException;
import com.wx.io.file.FileUtil;
import com.wx.properties.page.ResourcePage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.wx.invoicefx.sync.index.Index.VERSION_INCREMENT;
import static java.awt.SystemColor.info;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public class Local {


    private final File dataDirectory;
    private final Index index;

    public Local(ResourcePage index, File dataDirectory) {
        this.index = new Index(index);
        this.dataDirectory = dataDirectory;
    }

    public Index getIndex() {
        return index;
    }

    public boolean hasUncommittedChanges() {
        return index.getVersion() != index.getBranchVersion();
    }


    public File getFile(String filename) {
        return new File(dataDirectory, filename);
    }



    public void createIndex() throws IOException {
        if (!index.isEmpty()) {
            throw new IllegalArgumentException("Index already exists");
        }

        File[] files = dataDirectory.listFiles();
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
    }

    public void dataIntegrityTest() throws InvalidDataException {
        if (index.isEmpty()) return;

        File[] files = dataDirectory.listFiles();
        if (files == null) {
            files = new File[0];
        }

        if (files.length != index.getFilesCount()) {
            throw new InvalidDataException("Too many or too few files found");
        }

        for (File file : files) {
            FileInfo info = index.getFileInfo(file.getName());
            if (info == null) {
                throw new InvalidDataException("File info missing");
            }

            if (info.getTimestamp() != file.lastModified()) {
                try {
                    byte[] checksum = FileUtil.checkSum(file);

                    if (!Arrays.equals(checksum, info.getCheckSum())) {
                        throw new InvalidDataException("File is different: " + file.getName());
                    }
                } catch (IOException e) {
                    throw new InvalidDataException(e);
                }
            }
        }
    }


    private void updateIndex() throws IOException {
        File[] files = dataDirectory.listFiles();
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
            }

            infos[i] = info;
        }



        index.setFiles(infos);
        index.incrementVersion();
    }
}
