package com.wx.invoicefx.google;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * Simple wrapper around {@link Drive} service that facilitates common operations
 *
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 11.05.17.
 */
class DriveServiceHelper {

    private final Drive service;

    public DriveServiceHelper(Drive service) {
        this.service = service;
    }

    public FileList listFiles() throws IOException {
        return service.files().list().execute();
    }

    public File getFile(String id) throws IOException {
        return service.files().get(id).execute();
    }

    public String findIdByName(String name) throws IOException {
        FileList files = service.files().list().execute();


        for (File f : files.getFiles()) {
            if (name.equals(f.getName())) {
                return f.getId();
            }
        }

        return null;
    }

    public InputStream downloadFile(String id) throws IOException {
        return service.files().get(id).executeMediaAsInputStream();
    }

    public File insertFile(String parentId, String filename, InputStream in) throws IOException {
        return insertFile(parentId, filename, new InputStreamContent(null, in));
    }

    private File insertFile(String parentId, String filename, AbstractInputStreamContent mediaContent) throws IOException {
        File body = new File();
        body.setName(filename);

        if (parentId != null && parentId.length() > 0) {
            body.setParents(Collections.singletonList(parentId));
        }

        return service.files().create(body, mediaContent).execute();
    }

    public File updateFile(String id, InputStream in) throws IOException {
        return updateFile(id, new InputStreamContent(null, in));
    }

    private File updateFile(String id, AbstractInputStreamContent mediaContent) throws IOException {
        return service.files().update(id, new File(), mediaContent).execute();
    }



    public void removeFile(String id) throws IOException {
        service.files().delete(id).execute();
    }

}
