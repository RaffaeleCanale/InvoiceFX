package com.wx.invoicefx.model.entities;

import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.entities.purchase.Purchase;
import com.wx.invoicefx.model.entities.purchase.PurchaseGroup;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 10.05.17.
 */
public class ModelComparator {

    public static boolean deepEquals(Invoice a, Invoice b) {
        if (a == null) {
            return b == null;
        }

        return a.getId() == b.getId() &&
                Objects.equals(a.getPdfFilepath(), b.getPdfFilepath()) &&
                Objects.equals(a.getAddress(), b.getAddress()) &&
                Objects.equals(a.getDate(), b.getDate()) &&
                listEquals(a.getPurchaseGroups(), b.getPurchaseGroups(), ModelComparator::deepEquals);
    }

    public static boolean deepEquals(PurchaseGroup a, PurchaseGroup b) {
        return Objects.equals(a.getId(), b.getId()) &&
                listEquals(a.getClients(), b.getClients(), ModelComparator::deepEquals) &&
                listEquals(a.getPurchases(), b.getPurchases(), ModelComparator::deepEquals);
    }

    public static boolean deepEquals(Purchase a, Purchase b) {
        if (a == null) {
            return b == null;
        }

//        return  Objects.equals(a.getId(), b.getId()) &&
        return Objects.equals(a.getItemCount(), b.getItemCount()) &&
                Objects.equals(a.getFromDate(), b.getFromDate()) &&
                Objects.equals(a.getToDate(), b.getToDate()) &&
                Objects.equals(a.getDateEnabled(), b.getDateEnabled()) &&
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


    private static <E> boolean listEquals(List<E> a, List<E> b, BiFunction<E,E,Boolean> equalsFunction) {
        if (a == null) {
            return b == null;
        }
        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i++) {
            if (!equalsFunction.apply(a.get(i), b.get(i))) {
                return false;
            }
        }

        return true;
    }

//    private static Map<Long, List<Purchase>> createPurchasesMap(List<Purchase> purchases) {
//        Map<Long, List<Purchase>> map = new HashMap<>();
//
//        purchases.forEach(purchase -> {
//            Long key = getPurchaseKey(purchase);
//
//            map.computeIfAbsent(key, id -> new LinkedList<>());
//            map.get(key).add(purchase);
//        });
//
//        return map;
//    }
//
//    private static boolean findAndRemove(List<Purchase> purchases, Purchase target) {
//        Iterator<Purchase> it = purchases.iterator();
//
//        while (it.hasNext()) {
//            Purchase potential = it.next();
//
//            if (deepEquals(potential, target)) {
//                it.remove();
//                return true;
//            }
//        }
//
//        return true;
//    }
//
//    private static Long getPurchaseKey(Purchase purchase) {
//        Item item = purchase.getItem();
//
//        Long key = null;
//        if (item != null) {
//            key = item.getId();
//        }
//        return key;
//    }
}
