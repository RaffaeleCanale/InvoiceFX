package com.wx.invoicefx.model.entities.invoice;

import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.time.LocalDate;
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
    private String pdfFilepath;
    private List<PurchaseGroup> purchaseGroups;

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

    public String getPdfFilepath() {
        return pdfFilepath;
    }

    public void setPdfFilepath(String pdfFilepath) {
        this.pdfFilepath = pdfFilepath;
    }

    public List<PurchaseGroup> getPurchaseGroups() {
        return purchaseGroups;
    }

    public void setPurchaseGroups(List<PurchaseGroup> purchaseGroups) {
        this.purchaseGroups = purchaseGroups;
    }

    //    private final LongProperty id = new SimpleLongProperty();
//    private final BooleanBinding idValidity = id.greaterThan(0);
//
//    private final StringProperty address = new SimpleStringProperty();
//    private final BooleanBinding addressValidity = address.isNotEmpty();
//
//    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
//    private final BooleanBinding dateValidity = date.isNotNull();
//
//    private final ObservableList<PurchaseGroup> purchaseGroups = FXCollections.observableArrayList();
//    private final BooleanBinding purchaseGroupssValidity = Bindings.isNotEmpty(purchaseGroups);
//
//    private final StringProperty pdfFileName = new SimpleStringProperty();
//    private final BooleanBinding pdfFileNameValidity = pdfFileName.isNotEmpty();
//
//    private final DoubleProperty sum = new SimpleDoubleProperty();
//
//    public Invoice() {
//        purchases.addListener((ListChangeListener<Purchase>) i -> recomputeTotal());
//    }
//
//    private void recomputeTotal() {
//        sum.unbind();
//
//        purchases.stream()
//                .map(i -> (DoubleExpression) i.sumProperty())
//                .reduce(DoubleExpression::add)
//                .ifPresent(sum::bind);
//    }
//
//
//    //<editor-fold desc="Getters & Setters" defaultstate="collapsed">
//    public long getId() {
//        return id.get();
//    }
//
//    public LongProperty idProperty() {
//        return id;
//    }
//
//    public void setId(long id) {
//        this.id.set(id);
//    }
//
//    public BooleanBinding idValidityProperty() {
//        return idValidity;
//    }
//
//    public String getAddress() {
//        return address.get();
//    }
//
//    public StringProperty addressProperty() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address.set(address);
//    }
//
//    public BooleanBinding addressValidityProperty() {
//        return addressValidity;
//    }
//
//    public LocalDate getDate() {
//        return date.get();
//    }
//
//    public ObjectProperty<LocalDate> dateProperty() {
//        return date;
//    }
//
//    public void setDate(LocalDate date) {
//        this.date.set(date);
//    }
//
//    public BooleanBinding dateValidityProperty() {
//        return dateValidity;
//    }
//
//    public ObservableList<Purchase> getPurchases() {
//        return purchases;
//    }
//
//    public BooleanBinding purchasesValidityProperty() {
//        return purchasesValidity;
//    }
//
//    public String getPdfFileName() {
//        return pdfFileName.get();
//    }
//
//    public StringProperty pdfFileNameProperty() {
//        return pdfFileName;
//    }
//
//    public void setPdfFileName(String pdfFileName) {
//        this.pdfFileName.set(pdfFileName);
//    }
//
//    public BooleanBinding pdfFileNameValidityProperty() {
//        return pdfFileNameValidity;
//    }
//
//    public double getSum() {
//        return sum.get();
//    }
//
//    public ReadOnlyDoubleProperty sumProperty() {
//        return ReadOnlyDoubleProperty.readOnlyDoubleProperty(sum);
//    }
//    //</editor-fold>


    @Override
    public String toString() {
        return "[" + getId() + "] " + getPdfFilepath() + "\n" +
                getAddress() + "\n" +
                getDate() + "\n" +
                "  > " + getPurchaseGroups().stream().map(Object::toString).collect(Collectors.joining("\n  > "));
    }


}
