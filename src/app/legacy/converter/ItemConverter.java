package app.legacy.converter;

import app.legacy.model.item.ItemModel;
import app.model.DateEnabled;
import app.model.item.Item;
import com.wx.util.representables.TypeCaster;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.06.16.
 */
public class ItemConverter implements TypeCaster<Item, app.legacy.model.item.ItemModel> {

    private final Map<String, Item> itemsBuffer = new HashMap<>();

    private final TypeCaster<DateEnabled, app.legacy.model.DateEnabled> dateEnabledConverter;

    public ItemConverter(TypeCaster<DateEnabled, app.legacy.model.DateEnabled> dateEnabledConverter) {
        this.dateEnabledConverter = dateEnabledConverter;
    }

    @Override
    public Item castIn(app.legacy.model.item.ItemModel legacyItem) throws ClassCastException {
        Item item = itemsBuffer.get(legacyItem.getItemName());

        if (item == null) {
            item = new Item();
            item.setId(itemsBuffer.values().stream().mapToLong(Item::getId).max().orElse(0L)  + 1);
            item.setVat(legacyItem.getTva());
            item.setDefaultDateEnabled(dateEnabledConverter.castIn(legacyItem.getDefaultDateEnabled()));
            item.setName(legacyItem.getItemName());
            item.setPrice(legacyItem.getPrice());

            itemsBuffer.put(item.getName(), item);
        }

        return item;
    }

    @Override
    public app.legacy.model.item.ItemModel castOut(Item item) throws ClassCastException {
        ItemModel legacyItem = new ItemModel();
        legacyItem.setPrice(item.getPrice());
        legacyItem.setDefaultDateEnabled(dateEnabledConverter.castOut(item.getDefaultDateEnabled()));
        legacyItem.setItemName(item.getName());
        legacyItem.setTva(item.getVat());

        return legacyItem;
    }
}
