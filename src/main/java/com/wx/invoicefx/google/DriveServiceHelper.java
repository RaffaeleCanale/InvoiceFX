package com.wx.invoicefx.google;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.wx.io.Accessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
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

    public void downloadFile(String id, java.io.File destination) throws IOException {
        try (Accessor accessor = new Accessor()
                .setIn(downloadFile(id))
                .setOut(destination, false)) {
            accessor.pourInOut();
        }
    }

    public File insertFile(java.io.File fileContent) throws IOException {
        return insertFile(null, fileContent);
    }

    public File insertFile(String parentId, java.io.File fileContent) throws IOException {
        // File's metadata.
        File body = new File();
        body.setName(fileContent.getName());

        // Set the parent folder.
        if (parentId != null && parentId.length() > 0) {
            body.setParents(Collections.singletonList(parentId));
        }

        // File's content.
        FileContent mediaContent = new FileContent(null, fileContent);
        return service.files().create(body, mediaContent).execute();
    }

    public File updateFile(java.io.File fileContent, String id) throws IOException {
        // First retrieve the file from the API.
        File file = service.files().get(id).execute();
        file.setName(fileContent.getName());

        // File's new content.
        FileContent mediaContent = new FileContent(null, fileContent);

        // Send the request to the API.
        return service.files().update(id, file, mediaContent).execute();
    }



    public void removeFile(String id) throws IOException {
        service.files().delete(id).execute();
    }


    public void printFiles() throws IOException {
        FileList result = service.files().list()
                .setPageSize(10)
//                .setMaxResults(10)
                .execute();

        List<File> files = result.getFiles();
        if (files == null || files.size() == 0) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }

}
