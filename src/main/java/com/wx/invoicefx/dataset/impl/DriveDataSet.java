package com.wx.invoicefx.dataset.impl;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.model.File;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.google.DriveManager;
import com.wx.invoicefx.model.save.ModelSaver;
import com.wx.invoicefx.sync.AbstractFileSystem;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.util.io.InvalidDataException;
import com.wx.properties.PropertiesManager;
import com.wx.properties.page.InputLink;
import com.wx.properties.page.OutputLink;
import com.wx.properties.page.ResourcePage;

import java.io.*;
import java.util.Optional;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 11.07.17.
 */
public class DriveDataSet extends DataSet {

    private ResourcePage metadataPage;

    @Override
    public Optional<String> getProperty(String key) {
        if (key.equals("type")) {
            return Optional.of("remote");
        }

        return metadataPage.getString(key);
    }

    @Override
    protected void loadDataSetContent() throws IOException {
        if (metadataPage == null) {
            metadataPage = ResourcePage.builder().fromLinks(
                    new DriveInputLink(ModelSaver.DEFAULT_METADATA_FILENAME),
                    new ReadOnlyOutputLink()
            ).load();
        }
    }

    @Override
    public void testDataSetContent() throws InvalidDataException {
        /* no-op */
    }

    @Override
    protected ResourcePage getIndexPage() {
        return ResourcePage.builder().fromLinks(
                new DriveInputLink(DEFAULT_INDEX_FILENAME),
                new DriveOutputLink(DEFAULT_INDEX_FILENAME)
        ).get();
    }

    @Override
    protected void populateIndex(Index index) throws IOException {
        /* no-op */
    }

    @Override
    protected boolean hasContent() {
        return true;
    }

    @Override
    public boolean isReachable() {
        return DriveManager.isInit() && DriveManager.isUserRegistered() && getIndex() != null;
    }

    @Override
    protected AbstractFileSystem accessFileSystem0() {
        return new DriveFS();
    }

    private static class DriveFS implements AbstractFileSystem {

        @Override
        public void clear() throws IOException {
            DriveManager.executeClearFiles();
        }

        @Override
        public InputStream read(String filename) throws IOException {
            return DriveManager.executeGetFileStream(filename);
        }

        @Override
        public void write(String filename, InputStream input) throws IOException {
            DriveManager.executeInsertFile(filename, input);
        }

        @Override
        public void remove(String filename) throws IOException {
            try {
                DriveManager.executeRemoveFile(filename);
            } catch (IOException e) {
                if (e instanceof GoogleJsonResponseException) {
                    if (((GoogleJsonResponseException) e).getStatusCode() == 404) {
                        // Ignore not found exceptions
                        return;
                    }
                }
                throw e;
            }
        }

        @Override
        public void close() {
        }
    }


    private static class DriveInputLink implements InputLink {

        private final String driveFileName;

        private DriveInputLink(String driveFileName) {
            this.driveFileName = driveFileName;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            InputStream in = DriveManager.executeGetFileStream(driveFileName);

            if (in == null) {
                throw new FileNotFoundException();
            }

            return in;
        }
    }

    private class DriveOutputLink implements OutputLink {

        private final String driveFileName;

        private DriveOutputLink(String driveFileName) {
            this.driveFileName = driveFileName;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            if (!DriveManager.isInit()) {
                throw new IOException("Drive service is not initialized");
            }

            return new ByteArrayOutputStream() {
                @Override
                public void flush() throws IOException {
                    super.flush();

                    InputStream in = new ByteArrayInputStream(toByteArray());
                    DriveManager.executeInsertFile(driveFileName, in);
                }
            };
        }
    }

    private static class ReadOnlyOutputLink implements OutputLink {
        @Override
        public OutputStream getOutputStream() throws IOException {
            if (!DriveManager.isInit()) {
                throw new IOException("Drive service is not initialized");
            }

            throw new UnsupportedOperationException();
        }
    }
}
