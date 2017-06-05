package com.wx.invoicefx.model.save;

import com.wx.invoicefx.sync.repo.Local;
import com.wx.invoicefx.util.InvalidDataException;
import com.wx.properties.page.ResourcePage;

import java.io.File;
import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.05.17.
 */
public class IndexedSaveManager extends SaveManager {

    public static IndexedSaveManager loadOrCreate(File dataDirectory, File indexFile) throws IOException {
        ResourcePage indexPage = ResourcePage.builder()
                .fromFile(indexFile)
                .loadOrCreate();

        Local local = new Local(indexPage, dataDirectory);

        return new IndexedSaveManager(dataDirectory, indexPage, local);
    }


    private final Local local;

    private IndexedSaveManager(File dataDirectory, ResourcePage metadataPage, Local local) {
        super(dataDirectory, metadataPage);
        this.local = local;
    }

    public void initialize() throws InvalidDataException, IOException {
        if (local.getIndex().isEmpty()) {
            contentBasedIntegrityTest();
            local.createIndex();
        } else {
            indexBasedIntegrityTest();
        }
    }



    private void indexBasedIntegrityTest() throws InvalidDataException {
        local.dataIntegrityTest();
    }

    private void contentBasedIntegrityTest() throws InvalidDataException {
        // TODO: 15.05.17 Better test mechanism?
        try {
            getAllInvoices().collect();
        } catch (IOException e) {
            throw new InvalidDataException(e);
        }
    }
}
