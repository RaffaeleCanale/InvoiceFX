package com.wx.invoicefx.model.entities.purchase;

import com.wx.invoicefx.model.entities.client.Client;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 18.05.17.
 */
public class PurchaseGroup {

    private long id;
    private List<Client> clients = new ArrayList<>();
    private List<Purchase> purchases = new ArrayList<>();

    public double getSum() {
        return purchases.stream()
                .mapToDouble(Purchase::getSum)
                .sum();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases;
    }

    @Override
    public String toString() {
        return "[" + id +"] " + clients + "  //  " + purchases;
    }
}
