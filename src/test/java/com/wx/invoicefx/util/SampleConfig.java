package com.wx.invoicefx.util;

import com.wx.invoicefx.Bootstrap;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.io.util.data.DummyData;
import com.wx.invoicefx.io.util.data.DummyModels;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.save.IndexedSaveManager;
import com.wx.invoicefx.model.save.SaveManager;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.wx.invoicefx.config.Places.Dirs.DATA_DIR;
import static com.wx.invoicefx.config.Places.Files.LOCAL_INDEX_FILE;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 12.05.17.
 */
public class SampleConfig {

    @Test
    @Ignore
    public void createSampleConfig() throws IOException {
        if (!Bootstrap.bootstrapWithoutUI()) {
            System.exit(1);
        }

        SaveManager saveManager = IndexedSaveManager.loadOrCreate(Places.getDir(DATA_DIR), Places.getFile(LOCAL_INDEX_FILE));
        List<Invoice> invoices = DummyModels.generateInvoices(10000);

        for (Invoice invoice : invoices) {
            System.out.println(invoice.getId());
            saveManager.addNewInvoice(invoice);
        }
    }

}
