package com.wx.invoicefx.model.entities;

import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.client.Purchase;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 10.05.17.
 */
public class ModelComparator {

    public static boolean deepEquals(Invoice a, Invoice b) {
        if (a == null) {
            return b == null;
        }

        ObservableList<Purchase> purchasesA = a.getPurchases();
        ObservableList<Purchase> purchasesB = b.getPurchases();

        if (purchasesA.size() != purchasesB.size()) {
            return false;
        }

        Map<Long, List<Purchase>> bMap = createPurchasesMap(purchasesB);

        for (Purchase purchase : purchasesA) {
            Long key = getPurchaseKey(purchase);

            List<Purchase> bSublist = bMap.get(key);
            if (bSublist == null) {
                return false;
            }

            if (!findAndRemove(bSublist, purchase)) {
                return false;
            }
        }

        return a.getId() == b.getId() &&
                Objects.equals(a.getPdfFileName(), b.getPdfFileName()) &&
                Objects.equals(a.getAddress(), b.getAddress()) &&
                Objects.equals(a.getDate(), b.getDate());
    }

    public static boolean deepEquals(Purchase a, Purchase b) {
        if (a == null) {
            return b == null;
        }

        return a.getItemCount() == b.getItemCount() &&
                Objects.equals(a.getFromDate(), b.getFromDate()) &&
                Objects.equals(a.getToDate(), b.getToDate()) &&
                Objects.equals(a.getDateEnabled(), b.getDateEnabled()) &&
                deepEquals(a.getClient(), b.getClient()) &&
                deepEquals(a.getItem(), b.getItem());
    }

    public static boolean deepEquals(Client a, Client b) {
        if (a == null) {
            return b == null;
        }

        return a.getId() == b.getId() &&
            Objects.equals(a.getName(), b.getName());
    }

    public static boolean shallowEquals(Client a, Client b) {
        if (a == null) {
            return b == null;
        }

        return Objects.equals(a.getName(), b.getName());
    }

    public static boolean deepEquals(Item a, Item b) {
        if (a == null) {
            return b == null;
        }

        return a.getId() == b.getId() &&
                Objects.equals(a.getName(), b.getName()) &&
                Objects.equals(a.getDefaultDateEnabled(), b.getDefaultDateEnabled()) &&
                a.getPrice() == b.getPrice() &&
                a.getVat() == b.getVat();
    }

    private static Map<Long, List<Purchase>> createPurchasesMap(List<Purchase> purchases) {
        Map<Long, List<Purchase>> map = new HashMap<>();

        purchases.forEach(purchase -> {
            Long key = getPurchaseKey(purchase);

            map.computeIfAbsent(key, id -> new LinkedList<>());
            map.get(key).add(purchase);
        });

        return map;
    }

    private static boolean findAndRemove(List<Purchase> purchases, Purchase target) {
        Iterator<Purchase> it = purchases.iterator();

        while (it.hasNext()) {
            Purchase potential = it.next();

            if (deepEquals(potential, target)) {
                it.remove();
                return true;
            }
        }

        return true;
    }

    private static Long getPurchaseKey(Purchase purchase) {
        Item item = purchase.getItem();

        Long key = null;
        if (item != null) {
            key = item.getId();
        }
        return key;
    }
}
