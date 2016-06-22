package app.model.client;

import app.model.item.Item;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public class PurchasedItemTest {

    public static Item item(double price) {
        Item item = new Item();
        item.setPrice(price);
        item.setName("Some name");
        item.setId(1);

        return item;
    }

    @Test
    public void sumTest() {
        Item item = item(10.0);
        PurchasedItem purchasedItem = new PurchasedItem(new Client(), item);
        purchasedItem.setItemCount(1);

        assertEquals(10.0, purchasedItem.getSum().doubleValue(), 0.0);

        purchasedItem.setItemCount(2);
        assertEquals(20.0, purchasedItem.getSum().doubleValue(), 0.0);

        item.setPrice(11.0);
        assertEquals(22.0, purchasedItem.getSum().doubleValue(), 0.0);

        purchasedItem.setItem(item(-2.0));
        assertEquals(-4.0, purchasedItem.getSum().doubleValue(), 0.0);

        purchasedItem.setItemCount(0);
        assertEquals(0, purchasedItem.getSum().doubleValue(), 0.0);
    }

}