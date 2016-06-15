package app.model.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public class ClientTest {

    public static PurchasedItem item(double price, int count) {
        PurchasedItem item = new PurchasedItem();
        item.setItem(PurchasedItemTest.item(price));
        item.setItemCount(count);

        return item;
    }

    @Test
    public void sumTest() {
        Client client = new Client();

        PurchasedItem item1 = item(10, 1);
        PurchasedItem item2 = item(30, 2);

        client.getPurchasedItems().addAll(item1, item2);

        assertEquals(70.0, client.getSum(), 0.0);

        client.getPurchasedItems().add(item(10, 10));
        assertEquals(170.0, client.getSum(), 0.0);

        item2.setItemCount(1);
        assertEquals(140.0, client.getSum(), 0.0);

        client.getPurchasedItems().set(2, item(0,0));
        assertEquals(40.0, client.getSum(), 0.0);

        item1.getItem().setPrice(110.0);
        assertEquals(140.0, client.getSum(), 0.0);

        item1.setItem(PurchasedItemTest.item(0.0));
        assertEquals(30.0, client.getSum(), 0.0);
    }



}