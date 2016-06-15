package app.config.manager;

import app.model_legacy.invoice.InvoiceModel;
import app.model_legacy.item.ItemModel;
import com.wx.util.pair.Pair;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public interface ManagerInterface {

    List<Pair<Integer, Long>> getRecordsTimestamps();

    Collection<ItemModel> getAllItems();

    Stream<InvoiceModel> getAllRecords();

}
