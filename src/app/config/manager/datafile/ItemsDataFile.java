package app.config.manager.datafile;

import app.model_legacy.item.ItemModel;
import com.wx.io.Accessor;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public class ItemsDataFile implements DataFile<ItemModel> {

    private final File file;

    public ItemsDataFile(File file) {
        this.file = file;
    }

    @Override
    public List<ItemModel> get() throws IOException {
        new Accessor().setIn(file);
        return null;
    }

    @Override
    public void set(List<ItemModel> values) throws IOException {
        Accessor accessor = new Accessor().setOut(file);

        for (ItemModel value : values) {
            // TODO: 15.06.16 Write ID
            accessor.writeDouble(value.getTva());
            accessor.writeDouble(value.getPrice());
            accessor.writeByte(value.getDefaultDateEnabled().ordinal());
            // TODO: 15.06.16 Write item
        }

    }
}
