package com.wx.invoicefx.legacy.converter;


import com.wx.invoicefx.dataset.impl.InvoiceFxDataSet;
import com.wx.invoicefx.legacy.config.manager.DefaultModelManager;
import com.wx.invoicefx.legacy.model.DateEnabled;
import com.wx.invoicefx.legacy.model.invoice.InvoiceList;
import com.wx.invoicefx.legacy.model.invoice.InvoiceModel;
import com.wx.invoicefx.legacy.model.item.ClientItem;
import com.wx.invoicefx.legacy.model.item.ItemModel;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import com.wx.invoicefx.util.DesktopUtils;
import com.wx.invoicefx.util.ModelUtil;
import com.wx.util.pair.Pair;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 25.06.16.
 */
public class LegacyConverter {

    public static void convertDataSet(File legacyInvoicesFile, File outputDirectory) throws IOException {
        DesktopUtils.deleteDirContent(outputDirectory);

        InvoiceFxDataSet outputDataSet = new InvoiceFxDataSet(outputDirectory, "tmp");
        outputDataSet.loadData();

        DefaultModelManager<InvoiceModel, InvoiceList> manager = new DefaultModelManager<>(InvoiceList.class, legacyInvoicesFile);
        manager.load();

        ObservableList<InvoiceModel> legacyInvoices = manager.get();
        List<Invoice> invoices = legacyInvoices.stream().map(LegacyConverter::convert).collect(Collectors.toList());

        outputDataSet.getModelSaver().addInvoices(invoices);
    }


    public static Invoice convert(InvoiceModel legacyInvoice) {
        Invoice invoice = new Invoice();

        invoice.setId(legacyInvoice.getId());
        invoice.setAddress(legacyInvoice.getAddress());
        invoice.setPdfFilename(legacyInvoice.getPdfFileName());
        invoice.setDate(legacyInvoice.getDate());

        PurchaseGroup lastGroup = null;
        for (ClientItem legacyClientItem : legacyInvoice.getItems()) {
            if (lastGroup == null || !legacyClientItem.getClientName().isEmpty()) {
                Pair<List<Client>, List<String>> parsed = ModelUtil.parseClients(legacyClientItem.getClientName());

                lastGroup = new PurchaseGroup();
                lastGroup.setClients(parsed.get1());
                lastGroup.setStopWords(parsed.get2());

                invoice.getPurchaseGroups().add(lastGroup);
            }

            lastGroup.getPurchases().add(convert(legacyClientItem));
        }

        return invoice;
    }


    private static Purchase convert(ClientItem legacyClientItem) {
        Purchase purchase = new Purchase();

        purchase.setItem(convert(legacyClientItem.getItem()));
        purchase.setItemCount(legacyClientItem.getItemCount());
        purchase.setDateEnabled(convert(legacyClientItem.getDateEnabled()));
        purchase.setFromDate(legacyClientItem.getFromDate());
        purchase.setToDate(legacyClientItem.getToDate());

        return purchase;
    }

    private static Item convert(ItemModel legacyItem) {
        Item item = new Item();

        item.setName(legacyItem.getItemName());
        item.setVat(getVat(legacyItem.getTva()));
        item.setActive(true);
        item.setPrice(legacyItem.getPrice());
        item.setDefaultDateEnabled(convert(legacyItem.getDefaultDateEnabled()));

        return item;
    }

    private static Vat getVat(double tva) {
        if (tva == 3.8) {
            return new Vat(3.8, 1);
        } else if (tva == 8.0) {
            return new Vat(8.0, 2);
        } else {
            throw new UnsupportedOperationException("Unknown VAT: " + tva);
        }
    }

    private static com.wx.invoicefx.model.entities.DateEnabled convert(DateEnabled legacyDateEnabled) {
        switch (legacyDateEnabled) {
            case BOTH:
                return com.wx.invoicefx.model.entities.DateEnabled.BOTH;
            case NONE:
                return com.wx.invoicefx.model.entities.DateEnabled.NONE;
            case ONLY_FROM:
                return com.wx.invoicefx.model.entities.DateEnabled.ONLY_FROM;
            default:
                throw new AssertionError();
        }
    }
}
