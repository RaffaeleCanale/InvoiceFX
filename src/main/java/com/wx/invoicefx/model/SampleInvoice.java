package com.wx.invoicefx.model;

import com.wx.invoicefx.model.entities.DateEnabled;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.06.17.
 */
public class SampleInvoice {

    public static final Invoice SAMPLE_INVOICE = createSampleInvoice();
    public static final Invoice SAMPLE_INVOICE_SINGLE_ITEM = createSampleInvoiceSingleItem();

    private static Invoice createSampleInvoiceSingleItem() {
        Invoice sample = createSampleInvoice();

        sample.setPurchaseGroups(sample.getPurchaseGroups().subList(0, 1));
        PurchaseGroup group = sample.getPurchaseGroups().get(0);
        group.setPurchases(group.getPurchases().subList(1, 2));

        return sample;
    }

    private static Invoice createSampleInvoice() {
        LocalDate someDate = LocalDate.of(2017, 6, 22);

        Invoice invoice = new Invoice();
        invoice.setId(324);
        invoice.setDate(someDate);
        invoice.setAddress("TODO ADDRESS");

        Item item1 = new Item();
        item1.setName("TODO ITEM 1");
        item1.setVat(new Vat(3.8, 1));
        item1.setPrice(90.0);

        Item item2 = new Item();
        item2.setName("TODO ITEM 2");
        item2.setVat(new Vat(8.0, 2));
        item2.setPrice(25.0);


        Purchase purchase1 = new Purchase();
        purchase1.setItem(item1);
        purchase1.setDateEnabled(DateEnabled.BOTH);
        purchase1.setFromDate(someDate.minusDays(1));
        purchase1.setToDate(someDate);
        purchase1.setItemCount(1);

        Purchase purchase2 = new Purchase();
        purchase2.setItem(item2);
        purchase2.setDateEnabled(DateEnabled.NONE);
        purchase2.setItemCount(2);

        Purchase purchase3 = new Purchase();
        purchase3.setItem(item1);
        purchase3.setDateEnabled(DateEnabled.BOTH);
        purchase3.setFromDate(someDate.minusDays(1));
        purchase3.setToDate(someDate);
        purchase3.setItemCount(1);

        Client client1 = new Client();
        client1.setName("TODO NAME");


        Client client2 = new Client();
        client2.setName("TODO NAME 2");


        PurchaseGroup group1 = new PurchaseGroup();
        group1.setClients(Collections.singletonList(client1));
        group1.setPurchases(Arrays.asList(purchase1, purchase2));

        PurchaseGroup group2 = new PurchaseGroup();
        group2.setClients(Collections.singletonList(client2));
        group2.setPurchases(Collections.singletonList(purchase3));

        invoice.setPurchaseGroups(Arrays.asList(group1, group2));

        return invoice;
    }

}
