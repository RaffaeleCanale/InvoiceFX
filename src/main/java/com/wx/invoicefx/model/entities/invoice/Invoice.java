package com.wx.invoicefx.model.entities.invoice;

import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 02/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class Invoice {

    private long id;
    private String address;
    private LocalDate date;
    private String pdfFilename;
    private List<PurchaseGroup> purchaseGroups = new ArrayList<>();

    public double getSum() {
        return purchaseGroups.stream()
                .mapToDouble(PurchaseGroup::getSum)
                .sum();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPdfFilename() {
        return pdfFilename;
    }

    public void setPdfFilename(String pdfFilename) {
        this.pdfFilename = pdfFilename;
    }

    public List<PurchaseGroup> getPurchaseGroups() {
        return purchaseGroups;
    }

    public void setPurchaseGroups(List<PurchaseGroup> purchaseGroups) {
        this.purchaseGroups = purchaseGroups;
    }

    @Override
    public String toString() {
        return "[" + getId() + "] " + getPdfFilename() + "\n" +
                getAddress() + "\n" +
                getDate() + "\n" +
                "  > " + getPurchaseGroups().stream().map(Object::toString).collect(Collectors.joining("\n  > "));
    }


}
