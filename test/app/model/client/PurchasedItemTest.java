package app.model.client;

import app.model.item.ItemModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public class PurchasedItemTest {

    public static ItemModel item(double price) {
        ItemModel item = new ItemModel();
        item.setPrice(price);
        item.setItemName("Some name");
        item.setItemId(1);

        return item;
    }

    @Test
    public void sumTest() {
        PurchasedItem purchasedItem = new PurchasedItem();
        ItemModel item = item(10.0);

        purchasedItem.setItem(item);
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